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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.db.sql.execute.SQLHistory;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Manage creation and updates to persisted SQL statements
 *
 * @author John Baker
 */
public class SQLHistoryPersistenceManager {
    public static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    public static final String SQL_HISTORY_FILE_NAME = "sql_history";  // NOI18N
    public static final Logger LOGGER = Logger.getLogger(SQLHistoryPersistenceManager.class.getName());
    private static SQLHistoryPersistenceManager _instance = null;
    private static Document document;
    private List<SQLHistory> sqlHistoryList;

    private SQLHistoryPersistenceManager() {
    }

    public static SQLHistoryPersistenceManager getInstance() {
        if (null == _instance) {
            _instance = new SQLHistoryPersistenceManager();
        }
        return _instance;
    }

    public void create(FileObject fo, List<SQLHistory> sqlHistoryList) throws IOException {
        this.sqlHistoryList = sqlHistoryList;
        DataFolder df = DataFolder.findFolder(fo);
        AtomicWriter writer = new AtomicWriter(sqlHistoryList, df, SQL_HISTORY_FILE_NAME);
        df.getPrimaryFile().getFileSystem().runAtomicAction(writer);
        this.sqlHistoryList = null; // reset
    }

    /**
     * Atomic writer for adding and removing SQL to sql_history.xml.
     */
    private static final class AtomicWriter implements FileSystem.AtomicAction {
        List<SQLHistory> sqlHistoryList;
        String fileName;
        DataFolder parent;
        boolean remove;
        FileObject data;
        XmlWriter xmlWriter;

        AtomicWriter(List<SQLHistory> sqlHistoryList, DataFolder parent, String fileName) {
            this.sqlHistoryList = sqlHistoryList;
            this.fileName = fileName;
            this.parent = parent;
        }
        
        public void run() throws java.io.IOException {
            FileLock lck = null;
            OutputStream ostm = null;
            PrintWriter writer = null;
            try {
                FileObject folder = parent.getPrimaryFile();
                String fn = FileUtil.getFileDisplayName(folder) + File.separator + SQL_HISTORY_FILE_NAME + ".xml"; // NOI18N
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                // Unit test, DataTypeTest, considers "folder" to be a Multi-Filesystem
                if (!FileUtil.getFileDisplayName(folder).equals(("Databases/SQLHISTORY in Multi-Filesystem"))) { // NOI18N
                    if (folder.getChildren().length == 0) {
                        data = folder.createData(SQL_HISTORY_FILE_NAME, "xml"); //NOI18N
                        lck = data.lock();
                        ostm = data.getOutputStream(lck);
                        writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N                    

                        document = builder.newDocument();
                    } else {                                        
                        data = FileUtil.toFileObject(FileUtil.normalizeFile(new File(fn)));
                        InputStream is = data.getInputStream();
                        document = builder.parse(is);

                        lck = data.lock();
                        ostm = data.getOutputStream(lck);
                        writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                    }
                    // Create or update then write the DOM
                    xmlWriter = new XmlWriter(data, sqlHistoryList, writer);
                    xmlWriter.write(xmlWriter.createElements(document), ""); // NOI18N
                    writer.flush();
                    writer.close();
                    ostm.close();
                    writer = null;
                    ostm = null;
                }
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (null == lck) {
                    LOGGER.log(Level.WARNING, "Error saving SQL that was executed");
                } else {
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
                    nameNode.setAttribute("date", DateFormat.getInstance().format(sqlHistory.getDate()));  // NOI18N
                    newNode.appendChild(nameNode);
                }
                document.adoptNode(newNode);
            } else {
                newNode = document.getDocumentElement();
                for (SQLHistory sqlHistory : sqlHistoryList) {
                    nameNode = document.createElement("sql");  // NOI18N
                    nameNode.appendChild(document.createTextNode(sqlHistory.getSql()));
                    nameNode.setAttribute("url", sqlHistory.getUrl());  // NOI18N
                    nameNode.setAttribute("date", DateFormat.getInstance().format(sqlHistory.getDate()));  // NOI18N
                    newNode.appendChild(nameNode);
                }
            }
            return newNode;
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
}
