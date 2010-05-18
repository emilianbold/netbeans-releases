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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class XMLBootstrapParser implements VirtualTableBootstrapParser {

    public XMLBootstrapParser() {
    }

    public List buildVirtualDBColumns(VirtualDBTable table) throws VirtualDBException {
        int defaultPrecision = 60;
        int jdbcType = VirtualDBUtil.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == VirtualDBUtil.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }
        try {
            defaultPrecision = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDDEFAULTPRECISION)).intValue();
        } catch (Exception e) {
            defaultPrecision = 60;
        }
        VirtualDBColumn[] columns = getColumns(table);
        List<VirtualDBColumn> colList = new ArrayList<VirtualDBColumn>(columns.length);
        Element root = null;
        try {
            root = getRootElement(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            //ignore
        }
        if (root != null) {
            Element row = getRowElement(root);
            if (row != null) {
                int count = getColumnCount(row);
                for (int i = 1; i <= count; i++) {
                    VirtualDBColumn column = null;
                    if (columns != null && i <= columns.length) {
                        column = columns[i - 1];
                    }
                    if (column == null) {
                        String columnName = getColumnName(row, i);
                        if (columnName != null && !columnName.equals("") &&
                                !VirtualDBUtil.isNullString(columnName)) {
                            columnName = VirtualDBUtil.escapeNonAlphaNumericCharacters(columnName.trim());
                            columnName = VirtualDBUtil.createColumnNameFromFieldName(columnName.trim());
                            column = new VirtualDBColumn(columnName,
                                    jdbcType, defaultPrecision, 0, true);
                        } else {
                            column = new VirtualDBColumn("FIELD_" + String.valueOf(i),
                                    jdbcType, defaultPrecision, 0, true); // NOI18N
                        }
                        column.setCardinalPosition(i);
                    }
                    colList.add(column);
                }
            }
        }
        return colList;
    }

    public void makeGuess(VirtualDBTable table) throws VirtualDBException {
    }

    public boolean acceptable(VirtualDBTable table) throws VirtualDBException {
        try {
            getRootElement(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private VirtualDBColumn[] getColumns(VirtualDBTable table) {
        VirtualDBColumn[] columns = new VirtualDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (VirtualDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }

    private Element getRootElement(String file) throws Exception {
        file = VirtualDBUtil.escapeControlChars(file.trim());
        File f = new File(file);
        InputStream is = null;
        if (f.exists()) {
            is = new FileInputStream(f);
        } else {
            is = new URL(file).openStream();
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);
        return document.getDocumentElement();
    }

    private Element getRowElement(Element root) {
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node nd = children.item(i);
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) nd;
            }
        }
        return null;
    }

    private int getColumnCount(Element row) {
        int count = 0;
        NodeList children = row.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node nd = children.item(i);
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }

    private String getColumnName(Element row, int i) {
        NodeList children = row.getChildNodes();
        int count = 0;
        Element column = null;
        for (int j = 0; j < children.getLength(); j++) {
            Node nd = children.item(j);
            if (nd.getNodeType() == Node.ELEMENT_NODE) {
                count++;
                if (count == i) {
                    column = (Element) nd;
                    break;
                }
            }
        }
        String name = "";
        if (column != null) {
            name = column.getNodeName();
        }
        return name;
    }
}