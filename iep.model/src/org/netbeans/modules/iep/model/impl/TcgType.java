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


package org.netbeans.modules.iep.model.impl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Enumerated type defining supported types such as Integer, Icon, Double,
 * etc...
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public abstract class TcgType <C extends Object> implements Serializable {
    private static final long serialVersionUID = -4679746282355252393L;    
    
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");


    
    /**
     * Constant INTEGER type
     */
    public static final TcgType INTEGER = new TcgType("integer") {

        public Integer parse(String value) {

            if ((value == null) || value.equals("")) {
                return null;
            }

            return new Integer(value);
        }
       
    };

    /**
     * Constant INTEGER_LIST type
     */
    public static final TcgType INTEGER_LIST = new TcgType("integerList") {

        public String format(Integer value) {
            return formatList(INTEGER, value);
        }

        public List<Integer> parse(String value) {
            return getList(INTEGER, value);
        }
        
        public boolean isListType() {
            return true;
        }
    
        public TcgType getElementType() {
            return INTEGER;
        }
    };

    /**
     * Constant LONG type
     */
    public static final TcgType LONG = new TcgType("long") {

        public Long parse(String value) {

            if ((value == null) || value.equals("")) {
                return null;
            }

            return new Long(value);
        }
    };

    /**
     * Constant LONG_LIST type
     */
    public static final TcgType LONG_LIST = new TcgType("longList") {

        public String format(List<Long> value) {
            return formatList(LONG, value);
        }

        public List<Long> parse(String value) {
            return getList(LONG, value);
        }
        
        public boolean isListType() {
            return true;
        }
    
        public TcgType getElementType() {
            return LONG;
        }
    };

    /**
     * Constant DOUBLE type
     */
    public static final TcgType DOUBLE = new TcgType("double") {

        public Double parse(String value) {

            if ((value == null) || value.equals("")) {
                return null;
            }

            return new Double(value);
        }
    };

    /**
     * Constant DOUBLE_LIST type
     */
    public static final TcgType DOUBLE_LIST = new TcgType("doubleList") {

        public String format(List<Double> value) {
            return formatList(DOUBLE, value);
        }

        public List<Double> parse(String value) {
            return getList(DOUBLE, value);
        }

        public boolean isListType() {
            return true;
        }

        public TcgType getElementType() {
            return DOUBLE;
        }
    };

    /**
     * Constant BOOLEAN type
     */
    public static final TcgType BOOLEAN = new TcgType("boolean") {

        public Boolean parse(String value) {

            if ((value == null) || value.equals("")) {
                return null;
            }

            return Boolean.valueOf(value);
        }
    };

    /**
     * Constant BOOLEAN_LIST type
     */
    public static final TcgType BOOLEAN_LIST = new TcgType("booleanList") {

        public String format(Boolean value) {
            return formatList(BOOLEAN, value);
        }

        public List<Boolean> parse(String value) {
            return getList(BOOLEAN, value);
        }
    
        public boolean isListType() {
            return true;
        }

        public TcgType getElementType() {
            return BOOLEAN;
        }
    };

    /**
     * Constant STRING type
     */
    public static final TcgType STRING = new TcgType("string") {
        public String parse(String value) {
            return value;
        }
    };

    /**
     * Constant STRING_LIST type
     */
    public static final TcgType STRING_LIST = new TcgType("stringList") {

        public String format(List<String> value) {
            return formatList(STRING, value);
        }

        public List<String> parse(String value) {
            return getList(STRING, value);
        }

        public boolean isListType() {
            return true;
        }
        
        public TcgType getElementType() {
            return STRING;
        }
    };

    /**
     * Constant DATE type
     */
    public static final TcgType DATE = new TcgType("date") {
        public String format(Date value) {
            return (value == null)? "" : DATE_FORMAT.format(value);
        }

        public Date parse(String value) {
            try {
                return DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                return null;
            }
        }
    };

    /**
     * Constant STRING_LIST type
     */
    public static final TcgType DATE_LIST = new TcgType("dateList") {

        public String format(List<Date> value) {
            return formatList(DATE, value);
        }

        public List<Date> parse(String value) {
            return getList(DATE, value);
        }

        public boolean isListType() {
            return true;
        }
        
        public TcgType getElementType() {
            return DATE;
        }
    };

    /**
     * Constant OBJECT type
     */
    public static final TcgType OBJECT = new TcgType("object") {
        // This method should never be called
        public String format(Object value) {
            return (value == null) ? "null" : "" + value;
        }

        // This method should never be called
        // expect when parsing the default value during initialization
        public Object parse(String value) {
            return value;
        }
    };    
    
    private String mCode;

    private TcgType(String code) {
        mCode = code;
    }

  
    
    /**
     * Gets the tcgType given the code
     *
     * @param code String value to match
     *
     * @return The tcgType matching input code value
     */
    public static TcgType getType(String code) {
        if (code.equals("integer")) {
            return INTEGER;
        }

        if (code.equals("integerList")) {
            return INTEGER_LIST;
        }

        if (code.equals("double")) {
            return DOUBLE;
        }

        if (code.equals("doubleList")) {
            return DOUBLE_LIST;
        }

        if (code.equals("boolean")) {
            return BOOLEAN;
        }

        if (code.equals("booleanList")) {
            return BOOLEAN_LIST;
        }

        if (code.equals("string")) {
            return STRING;
        }

        if (code.equals("stringList")) {
            return STRING_LIST;
        }

        if (code.equals("object")) {
            return OBJECT;
        }        
        
        if (code.equals("long")) {
            return LONG;
        }
        
        if (code.equals("longList")) {
            return LONG_LIST;
        }
        if (code.equals("date")) {
            return DATE;
        }
        
        if (code.equals("dateList")) {
            return DATE_LIST;
        }
        return null;
    }

    /**
     * Gets the string representationreal for the value
     *
     * @param value the real value for which a string representation is desired
     *
     * @return the string representationreal for the value
     */
    public String format(C value) {

        return (value == null)
               ? ""
               : value.toString();
    }
    
    /**
     * Gets the real value for its string representation
     *
     * @param value the string representation for the real value
     *
     * @return the value behind the string representation
     */
    public C parse(String value) {
        return null;
    }
    
    public boolean isListType() {
        return false;
    }
    
    public TcgType getElementType() {
        return null;
    }
    
    /**
     * Gets the code name for this object
     *
     * @return the code name for this object
     */
    public String toString() {
        return mCode;
    }

    public String getCode() {
        return mCode;
    }    
    
    private static List getList(TcgType singleType, String value) {

        List l = new ArrayList();

        if ((value == null) || value.equals("")) {
            return l;
        }

        StringTokenizer st = new StringTokenizer(value, "\n\\");

        while (st.hasMoreTokens()) {
            String t = st.nextToken().trim();

            if (!t.equals("")) {
                l.add(singleType.parse(t));
            }
        }

        return l;
    }

    private static String formatList(TcgType singleType, Object value) {

        if (value == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        List l = (List) value;

        for (int i = 0, size = l.size(); i < size; i++) {
            if (i > 0) {
                sb.append("\\");
            }

            sb.append(singleType.format(l.get(i)));
        }

        return sb.toString();
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
