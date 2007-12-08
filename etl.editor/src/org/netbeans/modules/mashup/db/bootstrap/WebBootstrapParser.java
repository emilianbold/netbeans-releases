/*
 * WebBootstrapParser.java
 *
 * Created on April 3, 2007, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mashup.db.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;

import com.sun.sql.framework.utils.StringUtil;
/**
 *
 * @author ks161616
 */
public class WebBootstrapParser implements FlatfileBootstrapParser {
    
    /** Creates a new instance of WebBootstrapParser */
    public WebBootstrapParser() {
    }
    
    public List buildFlatfileDBColumns(FlatfileDBTable table) throws FlatfileDBException {
        if (table == null || table.getProperties() == null || table.getProperties().size() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        String fieldSep = table.getProperty(PropertyKeys.FIELDDELIMITER);
        if (fieldSep.equalsIgnoreCase("UserDefined")) {
            fieldSep = table.getProperty(PropertyKeys.WIZARDCUSTOMFIELDDELIMITER);
            table.setProperty(PropertyKeys.FIELDDELIMITER, fieldSep);
            if (StringUtil.isNullString(fieldSep)) {
                throw new FlatfileDBException("Please supply valid custom delimiter.");
            }
        }
        
        final String recordSep = table.getProperty(PropertyKeys.RECORDDELIMITER);
        final String qualifier = table.getProperty(PropertyKeys.QUALIFIER);
        boolean isFirstLineHeader = Boolean.valueOf(table.getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();
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
        
        javax.swing.text.Element element = null;
        try {
            element = getElement(table.getProperty(PropertyKeys.URL),
                    Integer.parseInt(table.getProperty("TABLENUMBER")));
        }  catch (Exception ex) {
            ex.printStackTrace();
        }
        if(element != null) {
            ElementIterator it = new ElementIterator(element);
            javax.swing.text.Element elem = null;
            while((elem = it.next()) != null) {
                if(elem.getName().equalsIgnoreCase("tr")) {
                    break;
                }
            }
            ElementIterator rowIt = new ElementIterator(elem);
            HTMLDocument doc = (HTMLDocument) elem.getDocument();
            int count = 1;
            while((element = rowIt.next()) != null) {
                if(element.getName().equalsIgnoreCase("td")) {
                    String field = "FIELD_" + count;
                    try {
                        if(isFirstLineHeader) {
                            field = doc.getText(element.getStartOffset(),
                                    (element.getEndOffset() - element.getStartOffset())).trim();
                            field = StringUtil.escapeNonAlphaNumericCharacters(field);
                            field = StringUtil.createColumnNameFromFieldName(field);
                        }
                    } catch (BadLocationException ex) {
                        //ignore
                    }
                    FlatfileDBColumn column = null;
                    if (columns != null && count <= columns.length) {
                        column = columns[count - 1];
                    }
                    if(column == null) {
                        column = new FlatfileDBColumnImpl(field, jdbcType, defaultPrecision, 0, true);
                    } else {
                        if(isFirstLineHeader) {
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
    
    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException {
    }
    
    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException {
        String url = table.getProperty(PropertyKeys.URL).toLowerCase();
        if(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.endsWith(".html") || url.endsWith(".htm")){
            try{
                getElement(table.getProperty(PropertyKeys.URL), 1);
            } catch(Exception e){
                return false;
            }
            return true;
        }
        return false;
    }
    
    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }
    
    private javax.swing.text.Element getElement(String url, int depth) throws Exception {
        InputStream in = null;
        url = StringUtil.escapeControlChars(url);
        File f = new File(url);
        if(f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = new URL(url).openStream();
        }
        
        EditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument)kit.createDefaultDocument();
        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        kit.read(in, doc, 0);
        int tableCount = 1;
        ElementIterator it = new ElementIterator(doc);
        javax.swing.text.Element element = null;
        while ((element = it.next()) != null ) {
            // read all table elements.
            if ("table".equalsIgnoreCase(element.getName())) {
                if(tableCount++ == depth) {
                    return element;
                }
            }
        }
        return null;
    }
}