package uw.mydb.route.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.mydb.conf.MydbConfig;
import uw.mydb.route.RouteAlgorithm;

import java.util.List;
import java.util.Map;

/**
 * 按照固定的long值range来路由。
 * 参数：start:起始位置 range:分区大小
 *
 * @author axeon
 */
public class RouteDatabaseByRange extends RouteAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(RouteDatabaseByRange.class);

    /**
     * 起始计算位置。有些人喜欢从10000开始计数。
     */
    private long start = 0;
    /**
     * 批次的范围大小。
     */
    private long range = 100;

    @Override
    public void config() {
        //获得默认range参数。
        Map<String, String> params = algorithmConfig.getParams();
        String rangeString = params.getOrDefault("range", "100");
        String startString = params.getOrDefault("start", "0");
        try {
            start = Long.parseLong(startString);
        } catch (Exception e) {
            logger.error("start:[{}]参数配置错误！", start);

        }
        try {
            range = Long.parseLong(rangeString);
        } catch (Exception e) {
            logger.error("range:[{}]参数配置错误！", range);

        }
    }

    @Override
    public RouteInfo calculate(MydbConfig.TableConfig tableConfig, RouteInfo routeInfo, String value) throws RouteException {

        long longValue = -1L;

        try {
            longValue = Long.parseLong(value);
        } catch (Exception e) {
            throw new RouteException("参数值错误，无法格式化为long类型！");
        }

        if (longValue == -1) {
            DataNode dataNode = dataNodes.get(0);
            routeInfo.setDataNode(dataNode);
            logger.error("calculate分库计算失败，参数值错误！");
            throw new RouteException("calculate分库计算失败，参数值错误！");
        } else {
            int pos = (int) Math.ceil((longValue - start) / range);
            if (pos < 0 || pos >= dataNodes.size()) {
                logger.error("calculate分库计算失败，节点计算越界!");
                throw new RouteException("calculate分库计算失败，节点计算越界!");
            } else {
                DataNode dataNode = dataNodes.get(pos);
                routeInfo.setDataNode(dataNode);
            }
        }
        return routeInfo;
    }

    @Override
    public List<RouteInfo> calculateRange(MydbConfig.TableConfig tableConfig, List<RouteInfo> routeInfos, String startValue, String endValue) throws RouteException {
        long startNum = -1, endNum = -1;
        try {
            startNum = Long.parseLong(startValue);
        } catch (Exception e) {
            throw new RouteException("指定的value无法格式化为long!");
        }
        try {
            endNum = Long.parseLong(endValue);
        } catch (Exception e) {
            throw new RouteException("指定的value无法格式化为long!");
        }
        if (startNum == -1 || endNum == -1) {
            logger.warn("calculateRange[{}]分库计算失败，参数值[{}-{}]错误！");
            throw new RouteException("分表参数错误!");
        }
        if (startNum > endNum) {
            logger.warn("calculateRange[{}]分库计算失败，起始值超越结束值{}>{}");
            throw new RouteException("分表参数错误!");
        }
        int pos = (int) Math.ceil(((startNum - start) / range));
        int endPos = (int) Math.ceil(((endNum - start) / range));

        //循环匹配
        for (; pos <= endPos; pos++) {
            if (pos >= dataNodes.size()) {
                logger.error("calculate[{}]分库计算失败，节点计算越界{}>{}");
                throw new RouteException("节点计算越界!");
            } else {
                DataNode dataNode = dataNodes.get(pos);
                RouteInfo routeInfo = RouteInfo.newDataWithTable(tableConfig.getName());
                routeInfo.setDataNode(dataNode);
                if (!routeInfos.contains(routeInfo)) {
                    routeInfos.add(routeInfo);
                }
            }
        }
        return routeInfos;
    }
}
