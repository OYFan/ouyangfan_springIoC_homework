package com.lagou.edu.utils;

import com.lagou.edu.annotation.Autowired;
import com.lagou.edu.annotation.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author 应癫
 *
 * 事务管理器类：负责手动事务的开启、提交、回滚
 */
@Component
public class TransactionManager {

    @Autowired
    private ConnectionUtils connectionUtils;

    // 开启手动事务控制
    public void beginTransaction() throws SQLException {
        Connection currentThreadConn = connectionUtils.getCurrentThreadConn();
        System.out.println("begin"+currentThreadConn);
        currentThreadConn.setAutoCommit(false);
    }


    // 提交事务
    public void commit() throws SQLException {
        Connection currentThreadConn = connectionUtils.getCurrentThreadConn();
        System.out.println("commit"+currentThreadConn);

        currentThreadConn.commit();
    }


    // 回滚事务
    public void rollback() throws SQLException {
        Connection currentThreadConn = connectionUtils.getCurrentThreadConn();
        System.out.println("rollback" + connectionUtils.getCurrentThreadConn());

        currentThreadConn.rollback();
    }
}
