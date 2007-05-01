/*
 * XMLBootstrapParser.java
 *
 * Created on April 3, 2007, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mashup.db.bootstrap;

import com.sun.sql.framework.utils.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ks161616
 */
public class XMLBootstrapParser implements FlatfileBootstrapParser {
    
    /** Creates a new instance of XMLBootstrapParser */
    public XMLBootstrapParser() {
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
        Element root = null;
        try {
            root = getRootElement(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            //ignore
        }
        if(root != null ){
            Element row = getRowElement(root);
            if(row != null) {
                int count = getColumnCount(row);
                for(int i = 1; i <= count; i++) {
                    FlatfileDBColumn column = null;
                    if (columns != null && i <= columns.length) {
                        column = columns[i - 1];
                    }
                    if(column == null) {
                        String columnName = getColumnName(row, i);
                        if(columnName != null && !columnName.equals("") && 
                                !StringUtil.isNullString(columnName)) {
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
            }
        }
        return colList;
    }
    
    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException {
    }
    
    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException {
        try {
            getRootElement(table.getProperty(PropertyKeys.URL));
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    
    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }
    
    private Element getRootElement(String file) throws Exception {
        file = StringUtil.escapeControlChars(file.trim());
        File f = new File(file);
        InputStream is = null;
        if(f.exists()) {
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
        for(int i = 0; i < children.getLength(); i++) {
            Node nd = children.item(i);
            if(nd.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)nd;
            }
        }
        return null;
    }
    
    private int getColumnCount(Element row) {
        int count = 0;
        NodeList children = row.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node nd = children.item(i);
            if(nd.getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }
    
    private String getColumnName(Element row, int i) {
        NodeList children = row.getChildNodes();
        int count = 0;
        Element column = null;
        for(int j = 0; j < children.getLength(); j++) {
            Node nd = children.item(j);
            if(nd.getNodeType() == Node.ELEMENT_NODE) {
                count++;
                if(count == i) {
                    column = (Element)nd;
                    break;
                }
            }
        }
        String name = "";
        if(column != null) {
            name = column.getNodeName();
        }
        return name;
    }
}