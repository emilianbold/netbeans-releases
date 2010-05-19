/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.w3c.dom.Element;
import org.openide.util.NbBundle;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class RSSBootstrapParser implements VirtualTableBootstrapParser {

    private static Map<String, String> columnsMap = new HashMap<String, String>();
    

    static {
        columnsMap.put("1", "TITLE");        // NOI18N
        columnsMap.put("2", "LINK");         // NOI18N
        columnsMap.put("3", "DESCRIPTION");  // NOI18N
        columnsMap.put("4", "AUTHOR");       // NOI18N
        columnsMap.put("5", "CATEGORY");     // NOI18N
        columnsMap.put("6", "COMMENTS");     // NOI18N
        columnsMap.put("7", "ENCLOSURE");    // NOI18N
        columnsMap.put("8", "GUID");         // NOI18N
        columnsMap.put("9", "PUBDATE");      // NOI18N
        columnsMap.put("10", "SOURCE");      // NOI18N
    }

    public RSSBootstrapParser() {
    }

    public List buildVirtualDBColumns(VirtualDBTable table) throws VirtualDBException {
        int defaultPrecision = 200;
        int jdbcType = VirtualDBUtil.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == VirtualDBUtil.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }
        try {
            defaultPrecision = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDDEFAULTPRECISION)).intValue();
        } catch (Exception e) {
            defaultPrecision = 200;
        }
        VirtualDBColumn[] columns = getColumns(table);
        List<VirtualDBColumn> colList = new ArrayList<VirtualDBColumn>(columns.length);
        if (!acceptable(table)) {
            throw new VirtualDBException(NbBundle.getMessage(RSSBootstrapParser.class, "MSG_not_acceptable"));
        }
        for (int i = 1; i <= 10; i++) {
            VirtualDBColumn column = null;
            if (columns != null && i <= columns.length) {
                column = columns[i - 1];
            }
            if (column == null) {
                column = new VirtualDBColumn(columnsMap.get(String.valueOf(i)),
                        jdbcType, defaultPrecision, 0, true);
                column.setCardinalPosition(i);
            }
            colList.add(column);
        }
        return colList;
    }

    public void makeGuess(VirtualDBTable table) throws VirtualDBException {
    }

    public boolean acceptable(VirtualDBTable table) throws VirtualDBException {
        String url = table.getProperty(PropertyKeys.URL);
        String[] urls = url.split(",");
        boolean accept = true;
        for (String u : urls) {
            try {
                getRootElement(u);
            } catch (Exception ex) {
                accept = false;
            }
        }
        return accept;
    }

    private Element getRootElement(String url) throws Exception {
        HttpURLConnection conn = null;
        if (url.indexOf("?") != -1) {
            String actualUrl = url.substring(0, url.indexOf("?"));
            conn = (HttpURLConnection) new URL(actualUrl).openConnection();
            String tempString = url.substring(url.indexOf("?") + 1);
            String[] props = tempString.split("&");
            for (String prop : props) {
                String[] val = prop.split("=");
                if (val.length == 2) {
                    conn.setRequestProperty(val[0], val[1]);
                } else if (val.length == 1) {
                    conn.setRequestProperty(val[0], null);
                }
            }
            conn.connect();
        } else {
            conn = (HttpURLConnection) new URL(url).openConnection();
        }
        Element elem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                conn.getInputStream()).getDocumentElement();
        if (!elem.getNodeName().equalsIgnoreCase("rss")) {
            throw new Exception(NbBundle.getMessage(RSSBootstrapParser.class, "MSG_document_type"));
        }
        return elem;
    }

    private VirtualDBColumn[] getColumns(VirtualDBTable table) {
        VirtualDBColumn[] columns = new VirtualDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (VirtualDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }
}