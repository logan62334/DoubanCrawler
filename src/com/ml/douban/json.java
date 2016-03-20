package com.ml.douban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringEscapeUtils;

import com.ml.utils.DBUtils;

public class json {
	String isbnUrl = "https://api.douban.com/v2/book/search?q=";
	public static String dbisbn10, dbisbn13, dbtitle, dbpublisher, dbauthor,
			dbpages, dbpubdate, dbprice = null;
	private static String xml = null;

	public static void main(String[] args) throws Exception {
		try {

			Connection conn = DBUtils.getConnection();
			java.sql.Statement sm = conn.createStatement();
			java.sql.Statement sm1 = conn.createStatement();
			ResultSet rs = sm.executeQuery("SELECT * FROM book");
			while (rs.next()) {
				dbtitle = rs.getString("title");
				dbpublisher = rs.getString("publisher");
				dbauthor = rs.getString("author").replace(" ", "");
				dbisbn10 = rs.getString("isbn10");
				dbisbn13 = rs.getString("isbn13");
				dbpages = rs.getString("pages");
				dbpubdate = rs.getString("pubdate");
				dbprice = rs.getString("price");

				String Title = dbtitle;
				String Publisher = dbpublisher;
				String Author = dbauthor;
				json isbnTest = new json();
				xml = isbnTest.fetchIsbnByBookInfo(Title, Author, Publisher);
				try {
					JSONObject obj = JSONObject.fromObject(xml);
					JSONArray obj2 = obj.getJSONArray("books");
					int jsonLength = obj2.size();
					for (int i = 0; i < jsonLength; i++) {
						JSONObject tempJson = JSONObject
								.fromObject(obj2.get(i));
						String isbn10 = StringEscapeUtils.escapeSql(tempJson
								.getString("isbn10"));
						String isbn13 = StringEscapeUtils.escapeSql(tempJson
								.getString("isbn13"));
						String pages = StringEscapeUtils.escapeSql(tempJson
								.getString("pages"));
						String pubdate = StringEscapeUtils.escapeSql(tempJson
								.getString("pubdate"));
						String price = StringEscapeUtils.escapeSql(tempJson
								.getString("price"));
						if (isbn13.equals(dbisbn13) && isbn10.equals(dbisbn10)) {
							if (pages.equals(dbpages)
									&& pubdate.equals(dbpubdate)
									&& price.equals(dbprice)) {
								try {
									String sql1 = "update book set state=2 where title='"
											+ dbtitle + "'";

									sm1.execute(sql1);

								} catch (Exception e) {

									e.printStackTrace();

								}
							} else {
								try {
									String sql1 = "update book set state=3 where title='"
											+ dbtitle + "'";
									String sql2 = "update book set pages='"
											+ pages + "',pubdate='" + pubdate
											+ "',price='" + price
											+ "' where title='" + dbtitle + "'";
									sm1.execute(sql1);
									sm1.execute(sql2);

								} catch (Exception e) {

									e.printStackTrace();

								}

							}

						} else {
							try {
								String sql1 = "update book set state=3 where title='"
										+ dbtitle + "'";
								String sql2 = "update book set pages='" + pages
										+ "',pubdate='" + pubdate + "',price='"
										+ price + "',isbn13='" + isbn13
										+ "',isbn10='" + isbn10
										+ "' where title='" + dbtitle + "'";
								sm1.execute(sql1);
								sm1.execute(sql2);

							} catch (Exception e) {

								e.printStackTrace();

							}
						}

					}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("error");
				}

			}
			DBUtils.free(rs, sm, conn);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public String fetchIsbnByBookInfo(String Title, String Author,
			String Publisher) throws IOException {
		String requestUrl = isbnUrl + Title + "+" + Author + "+" + Publisher;

		URL url = new URL(requestUrl);
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, "utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();

		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
}
