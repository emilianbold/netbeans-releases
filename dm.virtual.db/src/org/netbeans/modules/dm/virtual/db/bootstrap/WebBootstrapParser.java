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
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.openide.util.NbBundle;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class WebBootstrapParser implements VirtualTableBootstrapParser {

    public WebBootstrapParser() {
    }

    public List buildVirtualDBColumns(VirtualDBTable table) throws VirtualDBException {
        if (table == null || table.getProperties() == null || table.getProperties().size() == 0) {
            return Collections.EMPTY_LIST;
        }

        String fieldSep = table.getProperty(PropertyKeys.FIELDDELIMITER);
        if (fieldSep.equalsIgnoreCase("userdefined")) { // NOI18N
            fieldSep = table.getProperty(PropertyKeys.WIZARDCUSTOMFIELDDELIMITER);
            table.setProperty(PropertyKeys.FIELDDELIMITER, fieldSep);
            if (VirtualDBUtil.isNullString(fieldSep)) {
                throw new VirtualDBException(NbBundle.getMessage(WebBootstrapParser.class, "MSG_custom_delimiter"));
            }
        }

        final String recordSep = table.getProperty(PropertyKeys.RECORDDELIMITER);
        final String qualifier = table.getProperty(PropertyKeys.QUALIFIER);
        boolean isFirstLineHeader = Boolean.valueOf(table.getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();
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

        javax.swing.text.Element element = null;
        try {
            element = getElement(table.getProperty(PropertyKeys.URL),
                    Integer.parseInt(table.getProperty("TABLENUMBER"))); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (element != null) {
            ElementIterator it = new ElementIterator(element);
            javax.swing.text.Element elem = null;
            while ((elem = it.next()) != null) {
                if (elem.getName().equalsIgnoreCase("tr")) { // NOI18N
                    break;
                }
            }
            ElementIterator rowIt = new ElementIterator(elem);
            HTMLDocument doc = (HTMLDocument) elem.getDocument();
            int count = 1;
            while ((element = rowIt.next()) != null) {
                if (element.getName().equalsIgnoreCase("td")) { // NOI18N
                    String field = "FIELD_" + count; // NOI18N
                    try {
                        if (isFirstLineHeader) {
                            field = doc.getText(element.getStartOffset(),
                                    (element.getEndOffset() - element.getStartOffset())).trim();
                            field = VirtualDBUtil.escapeNonAlphaNumericCharacters(field);
                            field = VirtualDBUtil.createColumnNameFromFieldName(field);
                        }
                    } catch (BadLocationException ex) {
                        //ignore
                    }
                    VirtualDBColumn column = null;
                    if (columns != null && count <= columns.length) {
                        column = columns[count - 1];
                    }
                    if (column == null) {
                        column = new VirtualDBColumn(field, jdbcType, defaultPrecision, 0, true);
                    } else {
                        if (isFirstLineHeader) {
                            column.setName(field);
                        }
                    }
                    column.setCardinalPosition(count++);
                    colList.add(column);
                }
            }
        }
        return colList;
    }

    public void makeGuess(VirtualDBTable table) throws VirtualDBException {
    }

    public boolean acceptable(VirtualDBTable table) throws VirtualDBException {
        String url = table.getProperty(PropertyKeys.URL).toLowerCase();
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.endsWith(".html") || url.endsWith(".htm")) { // NOI18N
            try {
                getElement(table.getProperty(PropertyKeys.URL), 1);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    private VirtualDBColumn[] getColumns(VirtualDBTable table) {
        VirtualDBColumn[] columns = new VirtualDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (VirtualDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }

    private javax.swing.text.Element getElement(String url, int depth) throws Exception {
        InputStream in = null;
        url = VirtualDBUtil.escapeControlChars(url);
        File f = new File(url);
        if (f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = new URL(url).openStream();
        }

        EditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE); // NOI18N
        kit.read(in, doc, 0);
        int tableCount = 1;
        ElementIterator it = new ElementIterator(doc);
        javax.swing.text.Element element = null;
        while ((element = it.next()) != null) {
            // read all table elements.
            if ("table".equalsIgnoreCase(element.getName())) { // NOI18N
                if (tableCount++ == depth) {
                    return element;
                }
            }
        }
        return null;
    }
}