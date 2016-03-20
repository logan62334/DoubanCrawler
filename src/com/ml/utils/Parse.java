package com.ml.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ml.douban.ISBN;

public class Parse {
	private static final Log logger = LogFactory.getLog(ISBN.class);

	/** XML编码 */
	private String encoding = "utf-8";

	private NamespaceContext defaultNamespaceContext;

	public Parse() {
		defaultNamespaceContext = new NamespaceContext() {
			private Map<String, String> m_prefixMap = null;

			public String getNamespaceURI(String prefix) {
				if (null == prefix) {
					throw new NullPointerException("Null prefix");
				} else {
					if ("xml".equals(prefix)) {
						return XMLConstants.XML_NS_URI;
					}
					if (null != m_prefixMap) {
						for (String key : m_prefixMap.keySet()) {
							if (key.equals(prefix)) {
								return m_prefixMap.get(key);
							}
						}
					}
				}
				return XMLConstants.NULL_NS_URI;
			}

			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}

			public Iterator getPrefixes(String uri) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * 
	 * 获取XPath的值
	 * 
	 * @param query
	 * 
	 * @param xmlDocument
	 * 
	 * @param defaultNamespaceContext
	 * 
	 * @return String
	 */
	public String evaluateXPath(String query, Node xmlDocument,
			NamespaceContext defaultNamespaceContext) {
		String result = null;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		if (defaultNamespaceContext == null) {
			defaultNamespaceContext = this.defaultNamespaceContext;
		}
		xpath.setNamespaceContext(defaultNamespaceContext);
		XPathExpression expr = null;
		try {
			expr = xpath.compile(query);
		} catch (XPathExpressionException xpee) {
			Throwable x = xpee;
			if (null != xpee.getCause()) {
				x = xpee.getCause();
				if ("javax.xml.transform.TransformerException".equals(x
						.getClass().getName())) {
					if (logger.isDebugEnabled()) {
						logger.debug("xpath表达式错误：所有的命名空间需要转换。");
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("xpath表达式错误：可能表达式格式有误。");
					}
				}
			}
			return null;
		}
		try {
			result = (String) expr.evaluate(xmlDocument, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 加载XML String资源
	 * 
	 * @param xmlString
	 *            xml格式的字符串
	 * @return Node
	 * 
	 */
	public Node loadXMLResource(String xmlString) {
		if (0xFEFF == xmlString.charAt(0)) {
			xmlString = xmlString.substring(1);
		}
		InputSource source = new InputSource(new BufferedReader(
				new StringReader(xmlString)));
		return this.xmlSourceToDocument(source);
	}

	/**
	 * 加载XML byte[]资源
	 * 
	 * @param xmlFile
	 *            xml文件
	 * @return Node
	 * 
	 */
	public Node loadXMLResource(byte xmlByte[]) {
		String xmlString = "";
		try {
			xmlString = new String(xmlByte, encoding);
		} catch (UnsupportedEncodingException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}
		if (0xFEFF == xmlString.charAt(0)) {
			xmlString = xmlString.substring(1);
		}
		InputSource source = new InputSource(new BufferedReader(
				new StringReader(xmlString)));
		return this.xmlSourceToDocument(source);
	}

	/**
	 * 加载XML File资源
	 * 
	 * @param xmlFile
	 *            xml文件
	 * @return Node
	 * 
	 */
	public Node loadXMLResource(File xmlFile) {
		InputSource source = null;
		try {
			source = new InputSource(new FileInputStream(xmlFile));
		} catch (FileNotFoundException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}
		return this.xmlSourceToDocument(source);
	}

	/**
	 * 
	 * 把xml source 转换为Document
	 * 
	 * @param source
	 * 
	 * @return
	 * 
	 */
	private Node xmlSourceToDocument(InputSource source) {
		source.setEncoding(encoding);
		Document document = null;
		try {
			document = loadDocument(source);
		} catch (SAXParseException spe) {
			if (null != spe.getSystemId()) {
				if (logger.isDebugEnabled()) {
					logger.debug("xpath解析错误，出错的行数是：" + spe.getLineNumber()
							+ "，uri：" + spe.getSystemId());
					logger.debug(spe.getMessage());
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug(spe.getMessage());
				}
			}
			Exception x = spe;
			if (null != spe.getException()) {
				x = spe.getException();
			}
		} catch (SAXException se) {
			document = null;
			if (logger.isDebugEnabled()) {
				logger.debug("解析XML错误，请确保存在格式正确的XML文档。");
			}
			Exception x = se;
			if (null != se.getException()) {
				x = se.getException();
			}
		} catch (IOException ioe) {
			document = null;
			if (logger.isDebugEnabled()) {
				logger.debug("不能加载文档，文档不可读取。");
			}
		}
		return document;
	}

	/**
	 * 
	 * 从InputSource加载document
	 * 
	 * @param source
	 * @return Node
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document loadDocument(InputSource source) throws SAXException,
			IOException {
		Document document = null;
		DocumentBuilder parser = null;
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		try {
			parser = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			if (logger.isDebugEnabled()) {
				logger.debug(pce.getMessage());
			}
		}
		parser.reset();
		document = parser.parse(source);
		return document;
	}

	/**
	 * 
	 * 设置xml编码
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
