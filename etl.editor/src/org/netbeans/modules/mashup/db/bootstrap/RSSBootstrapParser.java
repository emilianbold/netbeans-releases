/*
 * RSSBootstrapParser.java
 *
 * Created on April 16, 2007, 6:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mashup.db.bootstrap;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;
import org.w3c.dom.Element;

/**
 *
 * @author karthikeyan s
 */
public class RSSBootstrapParser implements FlatfileBootstrapParser {
    
    private static Map<String, String> columnsMap = new HashMap<String, String>();
    
    static {
        columnsMap.put("1", "TITLE");
        columnsMap.put("2", "LINK");
        columnsMap.put("3", "DESCRIPTION");
        columnsMap.put("4", "AUTHOR");
        columnsMap.put("5", "CATEGORY");
        columnsMap.put("6", "COMMENTS");
        columnsMap.put("7", "ENCLOSURE");
        columnsMap.put("8", "GUID");
        columnsMap.put("9", "PUBDATE");
        columnsMap.put("10", "SOURCE");
    }
    
    /** Creates a new instance of RSSBootstrapParser */
    public RSSBootstrapParser() {
    }
    
    public List buildFlatfileDBColumns(FlatfileDBTable table) throws FlatfileDBException {
        int defaultPrecision = 200;
        int jdbcType = SQLUtils.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == SQLUtils.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }
        try {
            defaultPrecision = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDDEFAULTPRECISION)).intValue();
        } catch (Exception e) {
            defaultPrecision = 200;
        }
        FlatfileDBColumn[] columns = getColumns(table);
        List<FlatfileDBColumn> colList = new ArrayList<FlatfileDBColumn>(columns.length);
        if(!acceptable(table)) {
            throw new FlatfileDBException("Not acceptable");
        }
        for(int i = 1; i <= 10; i++) {
            FlatfileDBColumn column = null;
            if (columns != null && i <= columns.length) {
                column = columns[i - 1];
            }
            if(column == null) {
                column = new FlatfileDBColumnImpl(columnsMap.get(String.valueOf(i)),
                        jdbcType, defaultPrecision, 0, true);
                column.setCardinalPosition(i);
            }
            colList.add(column);
        }
        return colList;
    }
    
    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException {
    }
    
    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException {
        String url = table.getProperty(PropertyKeys.URL);
        String[] urls = url.split(",");
        boolean accept = true;
        for(String u : urls) {
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
        if(url.indexOf("?") != -1) {
            String actualUrl = url.substring(0, url.indexOf("?"));
            conn = (HttpURLConnection) new URL(actualUrl).openConnection();
            String tempString = url.substring(url.indexOf("?") + 1);
            String[] props = tempString.split("&");
            for(String prop : props) {
                String[] val = prop.split("=");
                if(val.length == 2) {
                    conn.setRequestProperty(val[0], val[1]);
                } else if(val.length == 1) {
                    conn.setRequestProperty(val[0], null);
                }
            }
            conn.connect();
        } else {
            conn = (HttpURLConnection) new URL(url).openConnection();
        }
        Element elem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                conn.getInputStream()).getDocumentElement();
        if(!elem.getNodeName().equalsIgnoreCase("rss")) {
            throw new Exception("Document type not supported.");
        }
        return elem;
    }
    
    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }
}