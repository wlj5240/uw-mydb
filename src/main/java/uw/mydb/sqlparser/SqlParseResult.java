package uw.mydb.sqlparser;

import uw.mydb.protocol.packet.CommandPacket;
import uw.mydb.protocol.packet.MySqlPacket;

import java.util.ArrayList;

/**
 * 最终的路由结果。
 *
 * @author axeon
 */
public class SqlParseResult {

    /**
     * 原始的sql语句。
     */
    private String sql;

    /**
     * 是否单一路由？
     */
    private Boolean isSingle;

    /**
     * 是否主库操作。
     */
    private Boolean isMaster;

    /**
     * 单sql结果
     */
    private SqlInfo sqlInfo = null;

    /**
     * 多sql结果。
     */
    private ArrayList<SqlInfo> sqlInfos = null;

    public SqlParseResult(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public void setMasterIfNull(boolean master) {
        if (this.isMaster == null) {
            isMaster = master;
        }
    }


    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }

    public SqlInfo getSqlInfo() {
        return sqlInfo;
    }

    public void setSqlInfo(SqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    public ArrayList<SqlInfo> getSqlInfos() {
        return sqlInfos;
    }

    public void setSqlInfos(ArrayList<SqlInfo> sqlInfos) {
        this.sqlInfos = sqlInfos;
    }

    /**
     * sql信息。
     */
    public static class SqlInfo {

        /**
         * 指定的mysqlGroup.
         */
        private String mysqlGroup;

        /**
         * 主库名。
         */
        private String database;

        /**
         * sql信息。
         */
        private StringBuilder newSqlBuf;

        /**
         * 新的sql。
         */
        private String newSql;

        public SqlInfo(int sqlSize) {
            newSqlBuf = new StringBuilder(sqlSize);
        }

        public SqlInfo(String sql) {
            newSql = sql;
        }

        public String getMysqlGroup() {
            return mysqlGroup;
        }

        public void setMysqlGroup(String mysqlGroup) {
            this.mysqlGroup = mysqlGroup;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getNewSql() {
            if (newSql == null) {
                newSql = newSqlBuf.toString();
            }
            return newSql;
        }

        public SqlInfo appendSql(String text) {
            this.newSqlBuf.append(text);
            return this;
        }

        /**
         * 生成packet。
         *
         * @return
         */
        public CommandPacket genPacket() {
            CommandPacket packet = new CommandPacket();
            packet.command = MySqlPacket.CMD_QUERY;
            packet.arg = getNewSql().getBytes();
            return packet;
        }
    }

}