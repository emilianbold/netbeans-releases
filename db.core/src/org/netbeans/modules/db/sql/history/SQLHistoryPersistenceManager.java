/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.sql.history;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.XMLDataObject;
import org.openide.util.NbPreferences;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Manage creation and updates to persisted SQL statements
 *
 * @author John Baker
 */
public class SQLHistoryPersistenceManager {
    public static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    public static final String SQL_HISTORY_FILE_NAME = "sql_history";  // NOI18N
    private static final int READ = 0;
    private static final int WRITE = 1;
    private static final int MODIFY = 2;
    public static final Logger LOGGER = Logger.getLogger(SQLHistoryPersistenceManager.class.getName());
    private static SQLHistoryPersistenceManager _instance = null;
    private static Document document;
    private List<SQLHistory> sqlHistoryList;
    private int numElemsToRemove = 0;

    private SQLHistoryPersistenceManager() {
    }

    public static SQLHistoryPersistenceManager getInstance() {
        if (null == _instance) {
            _instance = new SQLHistoryPersistenceManager();
        }
        return _instance;
    }

    public void removeHistoryFile(FileObject historyRoot) {
        try {
            FileObject folder = DataFolder.findFolder(historyRoot).getPrimaryFile();
            String fn = FileUtil.getFileDisplayName(folder) + File.separator + SQL_HISTORY_FILE_NAME + ".xml"; // NOI18N; 
            FileObject historyFo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fn)));
            historyFo.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    public void create(FileObject historyFileObject, List<SQLHistory> sqlHistoryList) throws SQLHistoryException {
        try {
            this.sqlHistoryList = sqlHistoryList;
            DataFolder df = DataFolder.findFolder(historyFileObject);
            AtomicFileAction writer = new AtomicFileAction(df, null, WRITE, sqlHistoryList);
            df.getPrimaryFile().getFileSystem().runAtomicAction(writer);
        } catch (IOException ex) {
            throw new SQLHistoryException();
        }
    }

    public List<SQLHistory> retrieve(String historyFilePath, FileObject historyFileObject) throws ClassNotFoundException, SQLHistoryException {
        Handler handler = null;
        try {
            handler = new Handler(historyFilePath);
            DataFolder df = DataFolder.findFolder(historyFileObject);
            AtomicFileAction reader = new AtomicFileAction(df, handler, READ, null);
            df.getPrimaryFile().getFileSystem().runAtomicAction(reader);

        } catch (IOException ex) {
            sqlHistoryList = handler.getXmlSqlHistoryList();
            throw new SQLHistoryException();
        }
        if (handler != null) {
            return handler.getXmlSqlHistoryList();
        } else {
            return new ArrayList<SQLHistory>();
        }
    }

    public List<SQLHistory> retrieve() {
        return sqlHistoryList;
    }

    public void setNumElemsToRemove(int elemsToRemove) {
        numElemsToRemove = elemsToRemove;
    }

    public int getNumElemsToRemove() {
        return numElemsToRemove;
    }

    public List<SQLHistory> updateSQLSaved(int limit, FileObject historyFileObject) throws SQLHistoryException {
        String historyFilePath = null;
        List<SQLHistory> updatedSQLHistoryList = null;
        try {
            historyFilePath = FileUtil.getFileDisplayName(historyFileObject) + File.separator + SQL_HISTORY_FILE_NAME + ".xml"; // NOI18N
            if (historyFileObject == null) {
                return new ArrayList<SQLHistory>();
            }
            updatedSQLHistoryList = retrieve(historyFilePath, historyFileObject);
            // Remove elements from list based on the number of statements to save that is set in the SQL History dialog
            if (limit < updatedSQLHistoryList.size()) {
                numElemsToRemove = updatedSQLHistoryList.size() - limit;
                boolean containsElems = true;
                containsElems = !updatedSQLHistoryList.isEmpty();
                if (containsElems && (limit == 0 || numElemsToRemove >= 0)) {
                    DataFolder df = DataFolder.findFolder(historyFileObject);
                    AtomicFileAction modifier = new AtomicFileAction(df, null, MODIFY, updatedSQLHistoryList);
                    df.getPrimaryFile().getFileSystem().runAtomicAction(modifier);
                }
            }
            updatedSQLHistoryList = retrieve(historyFilePath, historyFileObject);
        } catch (ClassNotFoundException ex) {
            throw new SQLHistoryException();
        } catch (IOException ex) {
            throw new SQLHistoryException();
        }
        return updatedSQLHistoryList;
    }

    private static final class AtomicFileAction implements FileSystem.AtomicAction {
        List<SQLHistory> sqlHistoryList;
        DataFolder parent;
        boolean remove;
        FileObject data;
        Handler handler;
        int actionType;

        AtomicFileAction(DataFolder parent, Handler handler, int actionType, List<SQLHistory> sqlHistoryList) {
            this.parent = parent;
            this.handler = handler;
            this.sqlHistoryList = sqlHistoryList;
            this.actionType = actionType;
        }

        public void run() throws IOException {
            FileLock lck = null;
            OutputStream ostm = null;
            PrintWriter writer = null;
            XmlWriter xmlWriter = null;
            DocumentBuilderFactory factory = null;
            DocumentBuilder builder = null;
            try {
                FileObject folder = parent.getPrimaryFile();
                String fn = FileUtil.getFileDisplayName(folder) + File.separator + SQL_HISTORY_FILE_NAME + ".xml"; // NOI18N
                // Read, Write or Update the persisted SQL file
                switch (actionType) {
                    case READ:
                        FileObject historyFo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fn)));
                        XMLDataObject obj = (XMLDataObject) XMLDataObject.find(historyFo);
                        InputSource inputSource = new InputSource(obj.getPrimaryFile().getInputStream());
                        inputSource.setSystemId(historyFo.getURL().toExternalForm());
                        XMLReader reader = XMLUtil.createXMLReader();
                        reader.setContentHandler(handler);
                        reader.setErrorHandler(handler);
                        reader.setEntityResolver(EntityCatalog.getDefault());
                        reader.parse(inputSource);
                        break;
                    case WRITE:
                        factory = DocumentBuilderFactory.newInstance();
                        builder = factory.newDocumentBuilder();
                        if (folder.getChildren().length == 0) {
                            data = folder.createData(SQL_HISTORY_FILE_NAME, "xml"); //NOI18N
                            lck = data.lock();
                            ostm = data.getOutputStream(lck);
                            writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                            document = builder.newDocument();
                            xmlWriter = new XmlWriter(data, sqlHistoryList, writer);
                            xmlWriter.write();
                        } else {
                            data = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fn)));
                            InputStream inputStream = data.getInputStream();
                            document = builder.parse(inputStream);
                            inputStream.close();
                            lck = data.lock();
                            ostm = data.getOutputStream(lck);
                            writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                        }
                        // Create or update then write the DOM
                        xmlWriter = new XmlWriter(data, sqlHistoryList, writer);
                        xmlWriter.write(xmlWriter.createElements(document), ""); // NOI18N
                        break;
                    case MODIFY:
                        factory = DocumentBuilderFactory.newInstance();
                        builder = factory.newDocumentBuilder();
                        if (folder.getChildren().length > 0) {
                            data = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fn)));
                            InputStream inputStream = data.getInputStream();
                            document = builder.parse(inputStream);
                            inputStream.close();
                            lck = data.lock();
                            ostm = data.getOutputStream(lck);
                            writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                        }
                        // Create or update then write the DOM
                        xmlWriter = new XmlWriter(data, sqlHistoryList, writer);
                        xmlWriter.write(); // NOI18N
                        xmlWriter.write(xmlWriter.removeElements(document), ""); // NOI18N
                        break;
                }
            } catch (ParserConfigurationException ex) {
                LOGGER.log(Level.INFO, ex.getMessage());
                throw new IOException();

            } catch (SAXException ex) {
                LOGGER.log(Level.INFO, ex.getMessage());
                throw new IOException();
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                    writer = null;
                }
                if (ostm != null) {
                    ostm.close();
                    ostm = null;
                }
                if (lck != null) {
                    lck.releaseLock();
                }
            }
        }
    }

    private static final class XmlWriter {
        private PrintWriter pw;
        private List<SQLHistory> sqlHistoryList;
        private FileObject data;

        public XmlWriter(FileObject data, List<SQLHistory> sqlHistoryList, PrintWriter pw) {
            this.data = data;
            this.sqlHistoryList = sqlHistoryList;
            this.pw = pw;
        }

        /**
         * 
         * create XML elements from SQL statements
         */
        private Node createElements(Document document) {
            Element newNode = null;
            Element nameNode = null;
            if (null == document.getDocumentElement()) {
                newNode = document.createElement("history");  // NOI18N
                for (SQLHistory sqlHistory : sqlHistoryList) {
                    nameNode = document.createElement("sql");  // NOI18N
                    nameNode.appendChild(document.createTextNode(sqlHistory.getSql()));
                    nameNode.setAttribute("url", sqlHistory.getUrl());  // NOI18N
                    nameNode.setAttribute("date", new Long(sqlHistory.getDate().getTime()).toString());  // NOI18N
                    newNode.appendChild(nameNode);
                }
                document.adoptNode(newNode);
            } else {
                newNode = document.getDocumentElement();
                for (SQLHistory sqlHistory : sqlHistoryList) {
                    nameNode = document.createElement("sql");  // NOI18N
                    nameNode.appendChild(document.createTextNode(sqlHistory.getSql()));
                    nameNode.setAttribute("url", sqlHistory.getUrl());  // NOI18N
                    nameNode.setAttribute("date", new Long(sqlHistory.getDate().getTime()).toString());  // NOI18N
                    newNode.insertBefore(nameNode, newNode.getFirstChild());
                }
            }
            return newNode;
        }

        /**
         * 
         * remove XML elements when the number of statements to save is reduced in the SQL History dialog   
         */
        private Node removeElements(Document document) {
            NodeList nodes = null;
            Element history = document.getDocumentElement();
            if (null != history) {
                nodes = history.getElementsByTagName("sql");
                int elemsToRemove = SQLHistoryPersistenceManager.getInstance().getNumElemsToRemove();
                // Statements to save was set to 0                   
                if (elemsToRemove == 0) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        if (nodes.item(0) != null) {
                            history.removeChild(nodes.item(0));
                        }
                    }
                }
                // Remove elements from the DOM
                for (int i = 0; i < elemsToRemove; i++) {
                    if (nodes.item(0) != null) {
                        history.removeChild(nodes.item(nodes.getLength() - 1));
                    }
                }
            }
            return history;
        }

        private void write() {
            pw.println("<?xml version='1.0' encoding='UTF-8' ?>");
        }

        /**
         * 
         * write the SQL statements as xml
         */
        private void write(Node node, String indent) {
            switch (node.getNodeType()) {
                case Node.DOCUMENT_NODE: {
                    Document doc = (Document) node;
                    pw.println(indent + "<?xml version='1.0'?>");  // NOI18N
                    Node child = doc.getFirstChild();
                    while (child != null) {
                        write(child, indent);
                        child = child.getNextSibling();
                    }
                    break;
                }
                case Node.ELEMENT_NODE: {
                    Element elt = (Element) node;
                    pw.print(indent + "<" + elt.getTagName());
                    NamedNodeMap attrs = elt.getAttributes();
                    for (int i = 0; i < attrs.getLength(); i++) {
                        Node a = attrs.item(i);
                        pw.print(" " + a.getNodeName() + "='" + fixup(a.getNodeValue()) + "'");  // NOI18N
                    }
                    pw.println(">");  // NOI18N
                    String newindent = indent + "    ";  // NOI18N
                    Node child = elt.getFirstChild();
                    while (child != null) {
                        write(child, newindent);
                        child = child.getNextSibling();
                    }

                    pw.println(indent + "</" + elt.getTagName() + ">");  // NOI18N
                    break;
                }
                case Node.TEXT_NODE: {
                    Text textNode = (Text) node;
                    String text = textNode.getData().trim();
                    if ((text != null) && text.length() > 0) {
                        pw.println(indent + fixup(text));
                    }
                    break;
                }
                default:
                    LOGGER.log(Level.INFO, "Ignoring node: " + node.getClass().getName());  // NOI18N
                    break;
            }

        }

        private String fixup(String s) {
            StringBuffer sb = new StringBuffer();
            int len = s.length();
            for (int i = 0; i < len; i++) {
                char c = s.charAt(i);
                switch (c) {
                    default:
                        sb.append(c);
                        break;
                    case '<':               // NOI18N
                        sb.append("&lt;");  // NOI18N
                        break;
                    case '>':               // NOI18N
                        sb.append("&gt;");  // NOI18N
                        break;
                    case '&':                // NOI18N
                        sb.append("&amp;");  // NOI18N
                        break;
                    case '"':                 // NOI18N
                        sb.append("&quot;");  // NOI18N
                        break;
                    case '\'':               // NOI18N
                        sb.append("&apos;"); // NOI18N
                        break;
                }
            }
            return sb.toString();
        }
    }

    /**
     * SAX handler for reading the XML file.
     */
    private static final class Handler extends DefaultHandler implements ContentHandler {

        private static final String ELEMENT_SQL = "sql"; // NOI18N
        private static final String ATTR_URL_PROPERTY_VALUE = "url"; // NOI18N
        private static final String ATTR_DATE_PROPERTY_VALUE = "date"; // NOI18N
        private final String sqlHistoryFileName;
        private static String url;
        private static StringBuilder sql;
        private static Date date;
        boolean matchingUrl = false;
        private List<SQLHistory> xmlSqlHistoryList = new ArrayList<SQLHistory>();
        static boolean isSql = false;
        private int limit = 10000;

        public Handler(String sqlHistoryFileName) {
            this.sqlHistoryFileName = sqlHistoryFileName;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if (ELEMENT_SQL.equals(qName)) {
                isSql = true;
                url = attrs.getValue(ATTR_URL_PROPERTY_VALUE);
                // #152486 SQL History:  if running NB in multiple locales the history file cannot be parsed
                if (attrs.getValue(ATTR_DATE_PROPERTY_VALUE).indexOf("/") != -1) { // NOI18N
                    try {
                        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                        date = df.parse(attrs.getValue(ATTR_DATE_PROPERTY_VALUE));
                    } catch (ParseException ex) {
                         // # 152486; Date stored is not parsable, so reset the date to the current timestamp
                         Calendar calendar = Calendar.getInstance();
                         date = calendar.getTime();
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(new Long(attrs.getValue(ATTR_DATE_PROPERTY_VALUE)).longValue());
                    date = calendar.getTime();
                }
            } else {
                isSql = false;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (ELEMENT_SQL.equals(qName)) {
                if (url != null && sql != null && date != null) {
                    addHistory(url, sql.toString(), date);
                    reset();
                }
            }
        }

        private static void reset() {
            // reset data
            url = null;
            date = null;
            sql = null;
        }

        private void addHistory(String url, String sql, Date date) {
            String sqlSetting = NbPreferences.forModule(SQLHistoryPersistenceManager.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", "");
            if (!sqlSetting.equals("")) { // NOI18N
                limit = Integer.parseInt(NbPreferences.forModule(SQLHistoryPersistenceManager.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", ""));  // NOI18N
            }
            if ((xmlSqlHistoryList.size() <= limit) || limit == 0) {
                xmlSqlHistoryList.add(new SQLHistory(url, sql, date));
                setXmlSqlHistoryList(xmlSqlHistoryList);
            } else {
                // remove a statement from the end of the list
                xmlSqlHistoryList.remove(xmlSqlHistoryList.size() - 1);
            }
        }

        @Override
        public void characters(char buf[], int offset, int length) {
            if (isSql) {
                String parsedValue = new String(buf, offset, length);
                if (sql == null) {
                    sql = new StringBuilder();
                    sql.append(parsedValue);
                } else {
                    sql.append(parsedValue);
                }
            }
        }

        public void setXmlSqlHistoryList(List<SQLHistory> sqlHistoryList) {
            xmlSqlHistoryList = sqlHistoryList;
        }

        public List<SQLHistory> getXmlSqlHistoryList() {
            if (xmlSqlHistoryList == null) {
                return new ArrayList<SQLHistory>();
            } else {
                return xmlSqlHistoryList;
            }
        }
    }
}
