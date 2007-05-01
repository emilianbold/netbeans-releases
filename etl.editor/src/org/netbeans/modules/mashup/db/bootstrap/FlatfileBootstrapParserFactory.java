/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;

import com.sun.sql.framework.utils.Logger;

/**
 * Factory for creating instances of FlatfileBootstrapParser.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 * @see org.netbeans.modules.mashup.db.bootstrap.FlatfileBootstrapParser
 */
public final class FlatfileBootstrapParserFactory {
    
    class NullBootstrapParser implements FlatfileBootstrapParser {
        public List buildFlatfileDBColumns(FlatfileDBTable table) {
            return Collections.EMPTY_LIST;
        }
        
        public void makeGuess(FlatfileDBTable file) {
        }
        
        public boolean acceptable(FlatfileDBTable table) {
            return false;
        }
    }
    
    /* Singleton instance of FlatfileBootstrapParserFactory */
    private static FlatfileBootstrapParserFactory instance;
    
    /* Log4J category string */
    private static final String LOG_CATEGORY = FlatfileBootstrapParserFactory.class.getName();
    
    private static final String[][] PARSER_MAP_INFO = new String[][] {
        { PropertyKeys.DELIMITED, "org.netbeans.modules.mashup.db.bootstrap.DelimitedBootstrapParser"},
        { PropertyKeys.FIXEDWIDTH, "org.netbeans.modules.mashup.db.bootstrap.FixedWidthBootstrapParser"},
        { PropertyKeys.XML, "org.netbeans.modules.mashup.db.bootstrap.XMLBootstrapParser"},
        { PropertyKeys.RSS, "org.netbeans.modules.mashup.db.bootstrap.RSSBootstrapParser"},
        { PropertyKeys.WEB, "org.netbeans.modules.mashup.db.bootstrap.WebBootstrapParser"},
        { PropertyKeys.WEBROWSET, "org.netbeans.modules.mashup.db.bootstrap.WebrowsetBootstrapParser"},
        { PropertyKeys.SPREADSHEET, "org.netbeans.modules.mashup.db.bootstrap.SpreadsheetBootstrapParser"}
    };
    
    /**
     * Gets an instance of FlatfileBootstrapParserFactory.
     *
     * @return FlatfileBootstrapParserFactory instance
     */
    public static FlatfileBootstrapParserFactory getInstance() {
        if (instance == null) {
            instance = new FlatfileBootstrapParserFactory();
        }
        return instance;
    }
    
    public static Collection getParserTypes() {
        List<String> types = new ArrayList<String>(7);
        types.add(PropertyKeys.FIXEDWIDTH);
        types.add(PropertyKeys.RSS);
        types.add(PropertyKeys.WEBROWSET);
        types.add(PropertyKeys.SPREADSHEET);
        types.add(PropertyKeys.XML);
        types.add(PropertyKeys.WEB);        
        types.add(PropertyKeys.DELIMITED);
        return types;
    }
    
    /* Map of parser type to FlatfileBootstrapParser instance */
    private Map keynameToClassMap;
    
    /** Creates a private default instance of FlatfileBootstrapParserFactory. */
    private FlatfileBootstrapParserFactory() {
        keynameToClassMap = new HashMap();
        
        for (int i = 0; i < PARSER_MAP_INFO.length; i++) {
            String key = PARSER_MAP_INFO[i][0];
            String parserClass = PARSER_MAP_INFO[i][1];
            
            try {
                keynameToClassMap.put(key, Class.forName(parserClass));
            } catch (Exception ignore) {
                // Ignore: Log but continue
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught error while loading parser class names.", ignore);
            }
        }
    }
    
    /**
     * Gets an instance of FlatfileBootstrapParser, if any, associated with the given type
     * name.
     *
     * @param type type name of FlatfileBootstrapParser to be retrieved
     * @return an instance of the associated FlatfileBootstrapParser, or null if no such
     *         instance is associated with type
     */
    public synchronized FlatfileBootstrapParser getBootstrapParser(String type) {
        Class parserClass = (Class) keynameToClassMap.get(type);
        if (parserClass == null) {
            return null;
        }
        
        try {
            return (FlatfileBootstrapParser) parserClass.newInstance();
        } catch (Exception e) {
            return new NullBootstrapParser();
        }
    }
    
    public String getParserType(FlatfileDBTable table) {
        String result = PropertyKeys.DELIMITED;
        FlatfileBootstrapParser bootstrapParser = null;
        String[] parsers = (String[]) getParserTypes().toArray(new String[0]);
        for(String parser : parsers) {
            bootstrapParser = getBootstrapParser(parser);
            try {
                if(bootstrapParser.acceptable(table)) {
                    result = parser;
                    break;
                }
            } catch(Exception e) {
                // ignore
            }
        }
        return result;
    }
}
