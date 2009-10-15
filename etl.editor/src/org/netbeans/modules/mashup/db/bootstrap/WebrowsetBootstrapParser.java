/*
 * WebrowsetBootstrapParser.java
 *
 * Created on April 3, 2007, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mashup.db.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.WebRowSet;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;

import com.sun.rowset.WebRowSetImpl;

import com.sun.sql.framework.utils.StringUtil;

/**
 *
 * @author ks161616
 */
public class WebrowsetBootstrapParser implements FlatfileBootstrapParser {
    
    /** Creates a new instance of WebrowsetBootstrapParser */
    public WebrowsetBootstrapParser() {
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
        WebRowSet wrs = null;
        try {
            wrs = getWebRowset(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            //ignore
        }
        if(wrs != null ){
            try {
                int count = wrs.getMetaData().getColumnCount();
                for(int i = 1; i <= count; i++) {
                    FlatfileDBColumn column = null;
                    if (columns != null && i <= columns.length) {
                        column = columns[i - 1];
                    }
                    if(column == null) {
                        String columnName = wrs.getMetaData().getColumnName(i);
                        if(columnName != null && !columnName.equals("") &&
                                columnName.trim().length() != 0) {
                            columnName = StringUtil.escapeNonAlphaNumericCharacters(columnName.trim());
                            columnName = StringUtil.createColumnNameFromFieldName(columnName.trim());
                            column = new FlatfileDBColumnImpl(columnName,
                                    jdbcType, defaultPrecision, 0, true);
                        } else {
                            column = new FlatfileDBColumnImpl("FIELD_" + String.valueOf(i),
                                    jdbcType, defaultPrecision, 0, true);
                        }
                        column.setCardinalPosition(i);
                    }
                    colList.add(column);
                }
            } catch (SQLException ex) {
                //ignore
            }
        }
        return colList;
    }
    
    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }
    
    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException {
    }
    
    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException {
        try {
            getWebRowset(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    private WebRowSet getWebRowset(String string) throws Exception {
        try {
            WebRowSet wrs = new WebRowSetImpl();
            string = StringUtil.escapeControlChars(string);
            File f = new File(string);
            InputStream is = null;
            if(f.exists()) {
                is = new FileInputStream(f);
            } else {
                is = new URL(string).openStream();
            }
            wrs.readXml(is);
            wrs.getMetaData().getColumnCount();
            return wrs;
        } catch (Exception e) {
            throw new FlatfileDBException("Unable to parse: " + string);
        }
    }
}
