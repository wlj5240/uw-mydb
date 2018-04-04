package uw.mydb;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uw.mydb.conf.MydbConfig;
import uw.mydb.conf.MydbConfigManager;
import uw.mydb.route.RouteManager;
import uw.mydb.sqlparser.SqlParseResult;
import uw.mydb.sqlparser.SqlParser;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.All)//基准测试类型
@OutputTimeUnit(TimeUnit.MILLISECONDS)//基准测试结果的时间类型
@Warmup(iterations = 3)//预热的迭代次数
@Threads(10)//测试线程数量
@State(Scope.Benchmark)//该状态为每个线程独享
//度量:iterations进行测试的轮次，time每轮进行的时长，timeUnit时长单位,batchSize批次数量
@Measurement(iterations = 10, time = -1, timeUnit = TimeUnit.SECONDS, batchSize = -1)
@SpringBootApplication
public class SqlParserTest {

    private static MydbConfig.SchemaConfig schema = null;
    //    private static String selectSql = "select * from user_info";
    private static String insertSql = "insert into msc_user (id,msc_type,ref_id,ref_tag,username,password,real_name,nick_name,mobile,email,remark,auth_flag,ip_filter_type,ip_filter_value,state,create_date,modify_date) values (1000,0,0,null,null,null,null,'axeon',null,null,null,0,0,null,0,null,null)";

    @Benchmark
    public void testInsert() {
        SqlParser parser = new SqlParser(schema, insertSql);
        SqlParseResult result = parser.parse();
    }
//
//    private static updateSql = "update user_info set create_date=now() where mch_id=1000 ";
//    @Benchmark
//    public void testUpdate() {
//        SqlParser parser = new SqlParser(schema, updateSql);
//        SqlParseResult result = parser.parse();
//    }

    public static void main(String[] args) throws RunnerException {
        SpringApplication.run(UwMydbApplication.class, args);
        schema = MydbConfigManager.getDefaultSchemaConfig();
        //初始化路由管理器
        RouteManager.init();
        Options opt = new OptionsBuilder()
                .include(SqlParserTest.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(opt).run();
    }

//    private static String selectSql ="select * from user_info where id=1000 ";

//    @Benchmark
//    public void testSelect() {
//        SqlParser parser = new SqlParser(schema, selectSql);
//        SqlParseResult result = parser.parse();
//    }
}