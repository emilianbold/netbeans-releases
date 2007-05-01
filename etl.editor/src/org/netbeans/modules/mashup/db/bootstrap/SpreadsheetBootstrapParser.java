/*
 * SpreadsheetBootstrapParser.java
 *
 * Created on April 3, 2007, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mashup.db.bootstrap;

import com.sun.sql.framework.utils.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Types;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
/**
 *
 * @author ks161616
 */
public class SpreadsheetBootstrapParser implements FlatfileBootstrapParser {
    
    /** Creates a new instance of SpreadsheetBootstrapParser */
    public SpreadsheetBootstrapParser() {
    }
    
    public List buildFlatfileDBColumns(FlatfileDBTable table) throws FlatfileDBException {
        int defaultPrecision = 60;
        int jdbcType = SQLUtils.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == SQLUtils.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }
        try {
            defaultPrecision = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDDEFAULTPRECISION)).intValue();
        } catch (Exception e) {
            defaultPrecision = 60;
        }
        FlatfileDBColumn[] columns = getColumns(table);
        List<FlatfileDBColumn> colList = new ArrayList<FlatfileDBColumn>(columns.length);
        Workbook workbook = null;
        try {
            workbook = getWorkBook(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            //ignore
        }
        if(workbook != null ){
            Sheet sheet = workbook.getSheet(table.getProperty(PropertyKeys.SHEET));
            for(int i = 1; i <= sheet.getColumns(); i++) {
                FlatfileDBColumn column = null;
                if (columns != null && i <= columns.length) {
                    column = columns[i - 1];
                }
                if(column == null) {
                    column = new FlatfileDBColumnImpl("FIELD_" + String.valueOf(i),
                            jdbcType, defaultPrecision, 0, true);
                    column.setCardinalPosition(i);
                }
                colList.add(column);
            }
        }
        return colList;
    }
    
    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException {
    }
    
    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException {
        try {
            getWorkBook(table.getProperty(PropertyKeys.URL));
        } catch(Exception ex) {
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
    
    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }
    
    private Workbook getWorkBook(String fileName) throws Exception {
        fileName = StringUtil.escapeControlChars(fileName);
        InputStream in = null;
        File f = new File(fileName);
        if(f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = new URL(fileName).openStream();
        }
        Workbook spreadsheetData = Workbook.getWorkbook(in, getWorkbookSettings());
        return spreadsheetData;
    }
}