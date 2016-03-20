package com.ml.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ml.dao.BookDao;
import com.ml.domain.Book;
import com.ml.utils.DBUtils;

public class BookDaoJdbcImpl implements BookDao {

	@Override
	public void update(Book book) {
		// TODO Auto-generated method stub
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DBUtils.getConnection();
			String sql = "update book set title=?, publisher=?, author=?,isbn10=?,isbn13=?,pubdate=?,price=?,pages=?,subtitle=?,state=? where id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, book.getTitle());
			ps.setString(2, book.getPublisher());
			ps.setString(3, book.getAuthor());
			ps.setString(4, book.getIsbn10());
			ps.setString(5, book.getIsbn13());
			ps.setString(6, book.getPubdate());
			ps.setString(7, book.getPrice());
			ps.setString(8, book.getPages());
			ps.setString(9, book.getSubtitle());
			ps.setInt(10, book.getState());
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtils.free(rs, ps, conn);
		}
	}

}
