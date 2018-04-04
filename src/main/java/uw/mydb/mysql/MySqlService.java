package uw.mydb.mysql;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.mydb.conf.MydbConfig;
import uw.mydb.mysql.util.ConcurrentBag;
import uw.mydb.util.SystemClock;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static uw.mydb.mysql.util.ConcurrentBag.IConcurrentBagEntry.STATE_NORMAL;
import static uw.mydb.mysql.util.ConcurrentBag.IConcurrentBagEntry.STATE_USING;
import static uw.mydb.util.SystemClock.elapsedMillis;

/**
 * mysql服务。
 * 用来维护一个mysql服务器中所有的session。
 *
 * @author axeon
 */
public class MySqlService implements ConcurrentBag.IBagStateListener {

    private static final Logger logger = LoggerFactory.getLogger(MySqlService.class);
    /**
     * 新建连接服务。
     */
    private static ThreadPoolExecutor addSessionExecutor;
    /**
     * 当前启动状态.
     */
    private final AtomicBoolean status = new AtomicBoolean(false);
    /**
     * 异步创建线程实例。
     */
    private final SessionCreator SESSION_CREATOR = new SessionCreator();
    /**
     * acceptor线程。
     */
    private EventLoopGroup group = null;

    /**
     * bootstrap实例。
     */
    private Bootstrap bootstrap = new Bootstrap();

    /**
     * mysql配置信息。
     */
    private MydbConfig.MysqlConfig config;
    /**
     * 配置组信息
     */
    private MySqlGroupService mysqlGroupService;
    /**
     * 是否是slave主机。
     */
    private boolean isSlaveNode;
    /**
     * 存储可用连接池的地方。
     */
    private ConcurrentBag<MySqlSession> sessionBag;
    /**
     * 正在连接中的session数量。
     */
    private AtomicInteger pendingAddCount = new AtomicInteger(0);
    /**
     * 是否活着。
     */
    private boolean isAlive = true;

    private String name;

    public MySqlService(MySqlGroupService mysqlGroupService, MydbConfig.MysqlConfig config) {
        this.config = config;
        this.mysqlGroupService = mysqlGroupService;
        this.name = new StringBuilder().append(this.config.getUser()).append('@').append(this.config.getHost()).append(':').append(this.config.getPort()).toString();
        this.sessionBag = new ConcurrentBag<>(this);
    }

    /**
     * 获得服务信息。
     *
     * @return
     */
    public String getName() {
        return this.name;
    }


    /**
     * 启动服务。
     *
     * @return
     */
    public boolean start() {
        if (status.compareAndSet(false, true)) {
            group = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("mysql_" + name).build());
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.TCP_NODELAY, false)
                    .handler(new MySqlDataHandlerFactory());
            addSessionExecutor = new ThreadPoolExecutor(1, 10, 20, SECONDS, new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat("mysql-house-keeping-%d").setDaemon(true).build(), new ThreadPoolExecutor.DiscardPolicy());
            MySqlMaintenanceService.scheduleHouseKeeping(new HouseKeeper());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭服务。
     *
     * @return
     */
    public boolean stop() {
        if (status.compareAndSet(true, false)) {
            sessionBag.close();
            addSessionExecutor.shutdown();
            group.shutdownGracefully();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 初始化
     */
    public void init() {
    }

    /**
     * 是否存活。
     *
     * @return
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * 获得配置文件。
     *
     * @return
     */
    public MydbConfig.MysqlConfig getConfig() {
        return config;
    }

    /**
     * 设置配置文件。
     *
     * @param config
     */
    public void setConfig(MydbConfig.MysqlConfig config) {
        this.config = config;
    }

    /**
     * 是否是salve节点。
     *
     * @return
     */
    public boolean isSlaveNode() {
        return isSlaveNode;
    }

    /**
     * 设置是否slave节点状态。
     *
     * @param slaveNode
     */
    public void setSlaveNode(boolean slaveNode) {
        isSlaveNode = slaveNode;
    }

    /**
     * 同步创建一个session。
     */
    private MySqlSession createSession(String msg) {
        MySqlSession session = null;
        try {
            ChannelFuture cf = bootstrap.connect(config.getHost(), config.getPort());
            Channel channel = cf.channel();
            session = new MySqlSession(this, channel);
            channel.attr(MySqlDataHandler.MYSQL_SESSION).set(session);
            cf.sync();
            logger.info("MySqlService[{}]({}+{}) create session {} by {}", this.getName(), sessionBag.size(), pendingAddCount.get(), session, msg);
        } catch (InterruptedException e) {
            logger.error("---> MySqlService[{}]({}+{}) create session {} error: {}", this.getName(), sessionBag.size(), pendingAddCount.get(), msg, e.getMessage());
        }
        return session;
    }

    /**
     * 向bag中增加一个session。
     *
     * @param session
     */
    void addSession(MySqlSession session) {
        sessionBag.add(session);
        pendingAddCount.decrementAndGet();
        logger.debug("{} - Added connection {}", name, session);
    }


    /**
     * 获得一个可用的session。
     *
     * @return
     */
    public MySqlSession getSession(MySqlSessionCallback mysqlSessionCallback) {
        final long startTime = SystemClock.now();
        try {
            long timeout = 10_000;
            do {
                MySqlSession session = sessionBag.borrow(timeout);
                if (session == null) {
                    logger.warn("session is null");
                    continue; // We timed out... break and throw exception
                }
                //检查session状态。
                if (!session.isAlive()) {
                    //此处应尝试关闭。
                    sessionBag.reserve(STATE_USING, session);
                    closeSession(session, "check session is not alive!");
                    continue;
                }
                //检查是否超过最大寿命。因为在后台检查中可能无法进入寿命检查状态。
                if (SystemClock.elapsedMillis(session.createTime, startTime) > SECONDS.toMillis(config.getConnMaxAge())) {
                    sessionBag.reserve(STATE_USING, session);
                    closeSession(session, "(connection has maxAge timeout)");
                    continue;
                }
                session.bind(mysqlSessionCallback);
                return session;
            } while (timeout >= elapsedMillis(startTime));
            //匹配不到，返回null吧。
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during connection acquisition");
        }
    }

    /**
     * 填充连接池。
     */
    private synchronized void fillPool() {
        addSessionExecutor.submit(SESSION_CREATOR);
    }

    /**
     * 当前session总数。
     *
     * @return
     */
    public int getTotalSessions() {
        return sessionBag.size();
    }

    /**
     * 空闲session数量。
     *
     * @return
     */
    public int getIdleSessions() {
        return sessionBag.getCount(STATE_NORMAL);
    }

    /**
     * 等待线程数。
     */
    public int getAwaitingThreads() {
        return sessionBag.getWaitingThreadCount();
    }

    /**
     * 永久关闭一个连接。
     *
     * @param session
     * @param closureReason reason to close
     */
    void closeSession(final MySqlSession session, final String closureReason) {
        logger.info("MySqlService[{}]({}) close session {} by {}", this.getName(), sessionBag.size(), session, closureReason);
        if (sessionBag.remove(session)) {
            MySqlMaintenanceService.queueCloseSession(new Runnable() {
                @Override
                public void run() {
                    session.trueClose();
                }
            });
        }
    }

    /**
     * 增加一个bagItem。
     * 为了防止频繁的添加创建session任务，使用pendingAddCount来校验。
     *
     * @param waiting
     */
    @Override
    public void addBagItem(int waiting) {
        if (pendingAddCount.get() == 0) {
            logger.info("waiting:{} - pendingAddCount:{}>0", waiting, pendingAddCount.get());
            addSessionExecutor.submit(SESSION_CREATOR);
        }
    }

    /**
     * 归还session。
     *
     * @param mysqlSession
     */
    public void requiteSession(MySqlSession mysqlSession) {
        sessionBag.requite(mysqlSession);
    }

    /**
     * 异步创建连接线程。
     */
    public final class SessionCreator implements Runnable {

        @Override
        public void run() {
            while (shouldCreateAnotherSession()) {
                pendingAddCount.incrementAndGet();
                final MySqlSession session = createSession("auto create");
                if (session == null) {
                    pendingAddCount.decrementAndGet();
                    break;
                }
            }
        }

        /**
         * 判断是否需要创建新的session。
         *
         * @return
         */
        private boolean shouldCreateAnotherSession() {
            return (getTotalSessions() + pendingAddCount.get()) < config.getMaxConn() &&
                    (sessionBag.getWaitingThreadCount() > pendingAddCount.get() || (getIdleSessions()) < config.getMinConn());
        }

    }

    /**
     * House Keeping后台线程。
     */
    public final class HouseKeeper implements Runnable {

        @Override
        public void run() {
            try {
                final long now = SystemClock.now();
                int idleCount = 0, busyCount = 0;
                final List<MySqlSession> list = sessionBag.sourceList();
                for (MySqlSession session : list) {
                    switch (session.getState()) {
                        case STATE_USING:
                            busyCount++;
                            checkBusyTimeout(session, now);
                            break;
                        case STATE_NORMAL:
                            idleCount++;
                            checkIdleTimeout(session, now, idleCount);
                            break;
                        default:
                            break;
                    }
                }
                fillPool(); // Try to maintain minimum connections
            } catch (Exception e) {
                logger.error("Unexpected exception in housekeeping task", e);
            }
        }

        /**
         * 检查忙超时。
         *
         * @param session
         * @param now
         */
        void checkBusyTimeout(MySqlSession session, long now) {
            if (SystemClock.elapsedMillis(session.lastAccess, now) > SECONDS.toMillis(config.getConnBusyTimeout()) && sessionBag.reserve(STATE_USING, session)) {
                closeSession(session, "(connection has busy timeout)");
            } else {
                checkAgeTimeout(session, now);
            }
        }

        /**
         * 检查闲超时。
         *
         * @param session
         * @param now
         */
        void checkIdleTimeout(MySqlSession session, long now, int idleCount) {
            if (idleCount > config.getMinConn() && SystemClock.elapsedMillis(session.lastAccess, now) > SECONDS.toMillis(config.getConnIdleTimeout()) && sessionBag.reserve(STATE_NORMAL, session)) {
                closeSession(session, "(connection has idle timeout)");
            } else {
                //检查寿命超时。
                checkAgeTimeout(session, now);
            }
        }

        /**
         * 检查寿命超时。
         *
         * @param session
         * @param now
         */
        void checkAgeTimeout(MySqlSession session, long now) {
            if (SystemClock.elapsedMillis(session.createTime, now) > SECONDS.toMillis(config.getConnMaxAge()) && sessionBag.reserve(STATE_NORMAL, session)) {
                closeSession(session, "(connection has maxAge timeout)");
            }
        }

    }
}