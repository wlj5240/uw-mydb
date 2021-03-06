package uw.mydb.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * mydb配置类。.
 *
 * @author axeon
 */
@ConfigurationProperties(prefix = "uw.mydb")
public class MydbConfig {

    /**
     * 服务器配置
     */
    private ServerConfig server = new ServerConfig();


    /**
     * 监控服务。
     */
    private Stats stats = new Stats();


    /**
     * 用户账号设置
     */
    private Map<String, UserConfig> users = new LinkedHashMap<>();

    /**
     * mysql配置组
     */
    private Map<String, MysqlGroupConfig> mysqlGroups = new LinkedHashMap<>();

    /**
     * schemas设置
     */
    private Map<String, SchemaConfig> schemas = new LinkedHashMap<>();

    /**
     * 分表路由设置。
     */
    private Map<String, RouteConfig> routes = new LinkedHashMap<>();

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Map<String, UserConfig> getUsers() {
        return users;
    }

    public void setUsers(Map<String, UserConfig> users) {
        this.users = users;
    }

    public Map<String, MysqlGroupConfig> getMysqlGroups() {
        return mysqlGroups;
    }

    public void setMysqlGroups(Map<String, MysqlGroupConfig> mysqlGroups) {
        this.mysqlGroups = mysqlGroups;
    }

    public Map<String, SchemaConfig> getSchemas() {
        return schemas;
    }

    public void setSchemas(Map<String, SchemaConfig> schemas) {
        this.schemas = schemas;
    }

    public Map<String, RouteConfig> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, RouteConfig> routes) {
        this.routes = routes;
    }


    /**
     * 路由类型枚举。
     */
    public enum MatchTypeEnum {
        /**
         * 精确匹配。
         */
        MATCH_FIX,

        /**
         * 匹配默认值，如果没有匹配值，可以匹配到默认值上。
         */
        MATCH_DEFAULT,

        /**
         * 匹配全部值，如果没有匹配值，则匹配所有数值。
         */
        MATCH_ALL;
    }

    /**
     * 统计信息。
     */
    public static class Stats {

        /**
         * 服务器的统计。
         */
        private boolean serverMetrics;

        /**
         * 客户端统计。
         */
        private boolean clientMetrics;

        /**
         * schema统计。
         */
        private boolean schemaMetrics;

        /**
         * mysql统计.
         */
        private boolean mysqlMetrics;

        /**
         * slowQuery超时毫秒数。
         */
        private long slowQueryTimeout;

        /**
         * metric服务配置。
         */
        private MetricService metricService = new MetricService();

        /**
         * elastic search peizhi配置。
         */
        private EsService esService = new EsService();

        public boolean isServerMetrics() {
            return serverMetrics;
        }

        public void setServerMetrics(boolean serverMetrics) {
            this.serverMetrics = serverMetrics;
        }

        public boolean isClientMetrics() {
            return clientMetrics;
        }

        public void setClientMetrics(boolean clientMetrics) {
            this.clientMetrics = clientMetrics;
        }

        public boolean isSchemaMetrics() {
            return schemaMetrics;
        }

        public void setSchemaMetrics(boolean schemaMetrics) {
            this.schemaMetrics = schemaMetrics;
        }

        public boolean isMysqlMetrics() {
            return mysqlMetrics;
        }

        public void setMysqlMetrics(boolean mysqlMetrics) {
            this.mysqlMetrics = mysqlMetrics;
        }

        public long getSlowQueryTimeout() {
            return slowQueryTimeout;
        }

        public void setSlowQueryTimeout(long slowQueryTimeout) {
            this.slowQueryTimeout = slowQueryTimeout;
        }

        public MetricService getMetricService() {
            return metricService;
        }

        public void setMetricService(MetricService metricService) {
            this.metricService = metricService;
        }
    }

    /**
     * 服务配置。
     */
    public static class ServerConfig {

        /**
         * 绑定的数据传输IP地址
         */
        private String ip = "0.0.0.0";
        /**
         * 绑定的数据传输端口
         */
        private int port = 3300;


        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

    /**
     * 用户账号配置
     */
    public static class UserConfig {
        /**
         * 密码
         */
        private String password;

        /**
         * 可以访问的schemas列表
         */
        private List<String> schemas;

        /**
         * 允许访问的主机地址
         */
        private List<String> hosts;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getSchemas() {
            return schemas;
        }

        public void setSchemas(List<String> schemas) {
            this.schemas = schemas;
        }

        public List<String> getHosts() {
            return hosts;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }
    }

    /**
     * mysql组配置
     */
    public static class MysqlGroupConfig {

        /**
         * 名称
         */
        private String name;

        /**
         * 复制组类型
         */
        private GroupTypeEnum groupType;

        /**
         * 切换类型
         */
        private GroupSwitchTypeEnum switchType;

        /**
         * mysql主机列表
         */
        private List<MysqlConfig> masters = new ArrayList<>();

        /**
         * mysql从机列表
         */
        private List<MysqlConfig> slaves = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public GroupTypeEnum getGroupType() {
            return groupType;
        }

        public void setGroupType(GroupTypeEnum groupType) {
            this.groupType = groupType;
        }

        public GroupSwitchTypeEnum getSwitchType() {
            return switchType;
        }

        public void setSwitchType(GroupSwitchTypeEnum switchType) {
            this.switchType = switchType;
        }

        public List<MysqlConfig> getMasters() {
            return masters;
        }

        public void setMasters(List<MysqlConfig> masters) {
            this.masters = masters;
        }

        public List<MysqlConfig> getSlaves() {
            return slaves;
        }

        public void setSlaves(List<MysqlConfig> slaves) {
            this.slaves = slaves;
        }

        public enum GroupSwitchTypeEnum {
            NOT_SWITCH, SWITCH;
        }

        public enum GroupTypeEnum {
            // 单一节点
            SINGLE_NODE(GlobalConstants.SINGLE_NODE_HEARTBEAT_SQL, GlobalConstants.MYSQL_SLAVE_STATUS_COLUMNS),
            // 普通主从
            MASTER_SLAVE(GlobalConstants.MASTER_SLAVE_HEARTBEAT_SQL, GlobalConstants.MYSQL_SLAVE_STATUS_COLUMNS),
            // 普通基于garela cluster集群
            GARELA_CLUSTER(GlobalConstants.GARELA_CLUSTER_HEARTBEAT_SQL, GlobalConstants.MYSQL_CLUSTER_STATUS_COLUMNS);

            private String heartbeatSQL;
            private String[] fetchCols;

            GroupTypeEnum(String heartbeatSQL, String[] fetchCols) {
                this.heartbeatSQL = heartbeatSQL;
                this.fetchCols = fetchCols;
            }

            public String getHeartbeatSQL() {
                return heartbeatSQL;
            }

            public String[] getFetchCols() {
                return fetchCols;
            }
        }
    }

    /**
     * mysql链接配置
     */
    public static class MysqlConfig {

        /**
         * 读取权重
         */
        private int weight = 1;

        /**
         * 主机
         */
        private String host;
        /**
         * 端口号
         */
        private int port;
        /**
         * 用户名
         */
        private String user;
        /**
         * 密码
         */
        private String password;
        /**
         * 最大连接数
         */
        private int maxConn = 1000;
        /**
         * 最小连接数
         */
        private int minConn = 1;
        /**
         * 最大重试次数
         */
        private int maxRetry = GlobalConstants.MAX_RETRY_COUNT;

        /**
         * 连接闲时超时秒数.
         */
        private int connIdleTimeout = 180;

        /**
         * 连接忙时超时秒数.
         */
        private int connBusyTimeout = 180;

        /**
         * 连接最大寿命秒数.
         */
        private int connMaxAge = 1800;


        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getMaxConn() {
            return maxConn;
        }

        public void setMaxConn(int maxConn) {
            this.maxConn = maxConn;
        }

        public int getMinConn() {
            return minConn;
        }

        public void setMinConn(int minConn) {
            this.minConn = minConn;
        }

        public int getMaxRetry() {
            return maxRetry;
        }

        public void setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        public int getConnIdleTimeout() {
            return connIdleTimeout;
        }

        public void setConnIdleTimeout(int connIdleTimeout) {
            this.connIdleTimeout = connIdleTimeout;
        }

        public int getConnBusyTimeout() {
            return connBusyTimeout;
        }

        public void setConnBusyTimeout(int connBusyTimeout) {
            this.connBusyTimeout = connBusyTimeout;
        }

        public int getConnMaxAge() {
            return connMaxAge;
        }

        public void setConnMaxAge(int connMaxAge) {
            this.connMaxAge = connMaxAge;
        }
    }

    /**
     * 虚拟数据库集群。
     */
    public static class SchemaConfig {

        /**
         * 库名
         */
        private String name;

        /**
         * 建库的sql。
         */
        private String createSql;

        /**
         * 基础库，提供库表结构参考。
         */
        private String baseNode;

        /**
         * 单独的表配置。
         */
        private Map<String, TableConfig> tables = new LinkedHashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreateSql() {
            return createSql;
        }

        public void setCreateSql(String createSql) {
            this.createSql = createSql;
        }

        public String getBaseNode() {
            return baseNode;
        }

        public void setBaseNode(String baseNode) {
            this.baseNode = baseNode;
        }

        public Map<String, TableConfig> getTables() {
            return tables;
        }

        public void setTables(Map<String, TableConfig> tables) {
            this.tables = tables;
        }
    }

    /**
     * 表配置。
     */
    public static class TableConfig {

        /**
         * 表名。
         */
        private String name;

        /**
         * 建表的sql语句。
         */
        private String createSql;

        /**
         * 路由设置。
         */
        private String route;


        /**
         * 匹配类型。
         * MATCH_FIX精确匹配：必须有匹配值，才能匹配，否则返回无法匹配。
         * MATCH_DEFAULT允许匹配有默认值：如果没有匹配值，可以匹配到默认值上。
         * MATCH_ALL允许匹配全量：如果没有匹配值，则全部匹配。
         */
        private MatchTypeEnum matchType = MatchTypeEnum.MATCH_FIX;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreateSql() {
            return createSql;
        }

        public void setCreateSql(String createSql) {
            this.createSql = createSql;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public MatchTypeEnum getMatchType() {
            return matchType;
        }

        public void setMatchType(MatchTypeEnum matchType) {
            this.matchType = matchType;
        }
    }

    /**
     * 路由规则配置。
     */
    public static class RouteConfig {

        /**
         * 路由配置名称。
         */
        private String name;

        /**
         * 上级路由名称，会继承上级路由的信息，只能继承一级。
         */
        private String parent;

        /**
         * 分布的节点。如果是单表，可以指向单个dataNode，此时不分表。
         */
        private List<DataNodeConfig> dataNodes = new ArrayList<>();

        /**
         * 使用的算法列表。
         */
        private List<AlgorithmConfig> algorithms = new ArrayList<>();


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public List<DataNodeConfig> getDataNodes() {
            return dataNodes;
        }

        public void setDataNodes(List<DataNodeConfig> dataNodes) {
            this.dataNodes = dataNodes;
        }

        public List<AlgorithmConfig> getAlgorithms() {
            return algorithms;
        }

        public void setAlgorithms(List<AlgorithmConfig> algorithms) {
            this.algorithms = algorithms;
        }
    }

    /**
     * 路由算法配置。
     */
    public static class AlgorithmConfig {

        /**
         * 算法类名。
         */
        private String algorithm;

        /**
         * 算法参数，可能为空。
         */
        private Map<String, String> params = new HashMap<>();

        /**
         * 路由键。
         */
        private String routeKey;


        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public String getRouteKey() {
            return routeKey;
        }

        public void setRouteKey(String routeKey) {
            this.routeKey = routeKey;
        }
    }

    /**
     * 数据节点配置
     */
    public static class DataNodeConfig {

        /**
         * mysql组配置
         */
        private String mysqlGroup;

        /**
         * mysql数据库配置，此配置支持缩写模式，比如db1-db20,db30这种。
         */
        private List<String> dbConfig = new ArrayList<>();

        /**
         * mysql数据库，存放展开后的信息。
         */
        private Set<String> databases = new LinkedHashSet<>();

        public String getMysqlGroup() {
            return mysqlGroup;
        }

        public void setMysqlGroup(String mysqlGroup) {
            this.mysqlGroup = mysqlGroup;
        }

        public Set<String> getDatabases() {
            return databases;
        }

        public void setDatabases(Set<String> databases) {
            this.databases = databases;
        }

        public List<String> getDbConfig() {
            return dbConfig;
        }

        public void setDbConfig(List<String> dbConfig) {
            this.dbConfig = dbConfig;
        }
    }


    /**
     * ES主机配置
     */
    public static class EsService {

        /**
         * 连接超时
         */
        private long connectTimeout = 10000;

        /**
         * 读超时
         */
        private long readTimeout = 10000;

        /**
         * 写超时
         */
        private long writeTimeout = 10000;

        /**
         * ES集群HTTP REST地址
         */
        private String clusters = null;

        public long getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public long getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
        }

        public long getWriteTimeout() {
            return writeTimeout;
        }

        public void setWriteTimeout(long writeTimeout) {
            this.writeTimeout = writeTimeout;
        }

        public String getClusters() {
            return clusters;
        }

        public void setClusters(String clusters) {
            this.clusters = clusters;
        }

    }
    /**
     * 监控信息配置
     *
     * @author axeon
     */
    public static class MetricService {

        /**
         * influxdb主机
         */
        private String host;

        /**
         * influxdb用户名
         */
        private String username;

        /**
         * influx密码
         */
        private String password;

        /**
         * influx数据库
         */
        private String database;

        /**
         * 间隔时间。
         */
        private long interval = 30000;

        /**
         * 检测是否启用。
         *
         * @return
         */
        public boolean isEnabled() {
            return host != null;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }
    }

}
