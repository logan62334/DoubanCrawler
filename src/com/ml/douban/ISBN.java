package com.ml.douban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;

import org.w3c.dom.Node;

import com.ml.utils.DBUtils;
import com.ml.utils.Parse;

public class ISBN {

	String apikey = "01ff9f3b34d0435a05feddb19e70ca0e";
	String isbnUrl = "http://api.douban.com/book/subject/isbn/";
	public static String dbisbn10, dbisbn13, dbtitle, dbpublisher,
			dbauthor = null;

	public static void main(String[] args) throws Exception {

		try {
			Connection conn = DBUtils.getConnection();
			String sql = "SELECT * FROM book";
			java.sql.Statement sm = conn.createStatement();
			java.sql.Statement sm1 = conn.createStatement();
			ResultSet rs = sm.executeQuery(sql);
			while (rs.next()) {
				dbisbn10 = rs.getString("isbn10");
				dbisbn13 = rs.getString("isbn13");
				dbtitle = rs.getString("title");
				dbpublisher = rs.getString("publisher");
				dbauthor = rs.getString("author").replace(" ", "");

				ISBN isbnTest = new ISBN();
				String isbn = dbisbn10;
				String xml = isbnTest.fetchBookInfoByXML(isbn);
				String xml1 = xml
						.replaceAll(
								"<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:db=\"http://www.douban.com/xmlns/\" xmlns:gd=\"http://schemas.google.com/g/2005\" xmlns:openSearch=\"http://a9.com/-/spec/opensearchrss/1.0/\" xmlns:opensearch=\"http://a9.com/-/spec/opensearchrss/1.0/\">",
								"<entry>");
				String xml2 = xml1.replaceAll("db:attribute", "dbattribute");
				String xml3 = xml2.replaceAll("db:tag", "dbtag");
				String xml4 = xml3.replaceAll("gd:rating", "gdrating");
				Parse xmlXPathUtil = new Parse();
				xmlXPathUtil.setEncoding("utf-8");
				Node fileNode = xmlXPathUtil.loadXMLResource(xml4);
				String isbn13 = xmlXPathUtil.evaluateXPath(
						"/entry/dbattribute[ @name='isbn13' ]/text()",
						fileNode, null);
				String title = xmlXPathUtil.evaluateXPath(
						"/entry/dbattribute[ @name='title' ]/text()", fileNode,
						null);
				String author = xmlXPathUtil.evaluateXPath(
						"/entry/dbattribute[ @name='author' ]/text()",
						fileNode, null).replace(" ", "");
				String publisher = xmlXPathUtil.evaluateXPath(
						"/entry/dbattribute[ @name='publisher' ]/text()",
						fileNode, null);
				System.out.println(author);
				if (isbn13.equals(dbisbn13) && title.equals(dbtitle)
						&& dbauthor.indexOf(author) != -1
						&& publisher.equals(dbpublisher)) {
					System.out.println("1");
					try {
						String sql1 = "update book set state=2 where isbn10='"
								+ dbisbn10 + "'";

						sm1.execute(sql1);

					} catch (Exception e) {

						e.printStackTrace();

					}
				} else {
					try {
						String sql1 = "update book set state=3 where isbn10='"
								+ dbisbn10 + "'";
						String sql2 = "update book set title='" + title
								+ "',author='" + author + "',publisher='" + publisher
								+ "' where title='" + dbtitle + "'";
						sm1.execute(sql1);
						sm1.execute(sql2);

					} catch (Exception e) {

						e.printStackTrace();

					}
				}

			}
			DBUtils.free(rs, sm, conn);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	public String fetchBookInfoByXML(String isbnNo) throws IOException {
		String requestUrl = isbnUrl + isbnNo;
		// + "?apikey=" + apikey;
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
