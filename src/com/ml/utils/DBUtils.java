package com.ml.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {

	Connection conn = null;
	// ����MySQL�����ݿ���������
	private static String DBDRIVER = "com.mysql.jdbc.Driver";
	// ����MySQL���ݿ�����ӵ�ַ
	private static String DBURL = "jdbc:mysql://localhost:3306/douban";
	// MySQL���ݿ�������û���
	private static String DBUSER = "root";
	// MySQL���ݿ����������
	private static String DBPASS = "623340";

	private DBUtils() {
	}

	static {
		try {
			Class.forName(DBDRIVER);
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DBURL, DBUSER, DBPASS);
	}

	public static void free(ResultSet rs, Statement st, Connection conn) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
		}
	}

}
