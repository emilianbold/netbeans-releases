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

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Types;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class SpreadsheetBootstrapParser implements VirtualTableBootstrapParser {

    public SpreadsheetBootstrapParser() {
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
        boolean isFirstLineHeader = Boolean.valueOf(table.getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();
        VirtualDBColumn[] columns = getColumns(table);
        List<VirtualDBColumn> colList = new ArrayList<VirtualDBColumn>(columns.length);
        Workbook workbook = null;
        try {
            workbook = getWorkBook(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            //ignore
        }
        if (workbook != null) {
            Sheet sheet = workbook.getSheet(table.getProperty(PropertyKeys.SHEET));
            for (int i = 1; i <= sheet.getColumns(); i++) {
                VirtualDBColumn column = null;
                String columnName = sheet.getCell(i - 1, 0).getContents();
                if (columns != null && i <= columns.length) {
                    column = columns[i - 1];
                }
                if (!isFirstLineHeader) {
                    columnName = "FIELD_" + String.valueOf(i);
                }
                if (column == null) {
                    column = new VirtualDBColumn(columnName, jdbcType, defaultPrecision, 0, true);
                } else if (isFirstLineHeader) {
                    column.setName(columnName);
                }
                column.setCardinalPosition(i);
                colList.add(column);
            }
        }
        return colList;
    }

    public void makeGuess(VirtualDBTable table) throws VirtualDBException {
    }

    public boolean acceptable(VirtualDBTable table) throws VirtualDBException {
        try {
            getWorkBook(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private WorkbookSettings getWorkbookSettings() {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setDrawingsDisabled(true);
        settings.setAutoFilterDisabled(true);
        settings.setSuppressWarnings(true);
        settings.setNamesDisabled(true);
        settings.setIgnoreBlanks(true);
        settings.setCellValidationDisabled(true);
        settings.setFormulaAdjust(false);
        settings.setPropertySets(false);
        return settings;
    }

    private VirtualDBColumn[] getColumns(VirtualDBTable table) {
        VirtualDBColumn[] columns = new VirtualDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (VirtualDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }

    private Workbook getWorkBook(String fileName) throws Exception {
        fileName = VirtualDBUtil.escapeControlChars(fileName);
        InputStream in = null;
        File f = new File(fileName);
        if (f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = new URL(fileName).openStream();
        }
        Workbook spreadsheetData = Workbook.getWorkbook(in, getWorkbookSettings());
        return spreadsheetData;
    }
}