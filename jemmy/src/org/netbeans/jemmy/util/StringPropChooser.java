/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.util;

import java.util.StringTokenizer;

/**
 * 
 * Implementation of org.netbeans.jemmy.ComponentChooser interface.
 * Class can be used to find component by its field/methods values converted to String.
 * 
 * Example:
 *	    JLabel label = JLabelOperator.findJLabel(frm0, new StringPropChooser("getText=JLabel",
 *										 false, true));
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class StringPropChooser extends PropChooser{

    private boolean compareExactly;
    private boolean compareCaseSensitive;

    /**
     * @param propNames Names of methods/fields
     * @param params Parameters values for methods. <BR>
     * @param classes Parameters classes.
     * @param results Objects to compare converted to String method/field values to.
     * @param ce Compare exactly.<BR>
     * If true, compare exactly (<value>.toString().equals(<result>)) <BR>
     * If false, compare as substring (<value>.toString().indexOf(<result>) != -1)
     * @param ccs Compare case sensitive. <BR>
     * if false convert both <value>.toString() and <result> to uppercase before comparison.
     */
    public StringPropChooser(String[] propNames, 
			     Object[][] params, 
			     Class[][] classes, 
			     String[] results,
			     boolean ce,
			     boolean ccs) {
	super(propNames, params, classes, results);
	compareExactly = ce;
	compareCaseSensitive = ccs;
    }

    /**
     * @param propNames Names of methods/fields
     * @param results Objects to compare converted to String method/field values to.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     */
    public StringPropChooser(String[] propNames, 
			     String[] results,
			     boolean ce,
			     boolean ccs) {
	this(propNames, (Object[][])null, (Class[][])null, results, ce, ccs);
    }

    /**
     * @param props Method/field names && values <BR>
     * Like "getText=button;isVisible=true"
     * @param semicolonChar Method(field) names separator.
     * @param equalChar Method(field) name - expected value separator.
     * @param params Parameters values for methods.
     * @param classes Parameters classes.
     * @param results Objects to compare converted to String method/field values to.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     */
    public StringPropChooser(String props, 
			     String semicolonChar,
			     String equalChar,
			     Object[][] params, 
			     Class[][] classes, 
			     boolean ce,
			     boolean ccs) {
	this(cutToArray(props, semicolonChar, equalChar, true), params, classes,
	     cutToArray(props, semicolonChar, equalChar, false), ce, ccs);
    }

    /**
     * @param props Method/field names && values
     * @param semicolonChar Method(field) names separator.
     * @param equalChar Method(field) name - expected value separator.
     * @param results Objects to compare converted to String method/field values to.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     */
    public StringPropChooser(String props, 
			     String semicolonChar,
			     String equalChar,
			     boolean ce,
			     boolean ccs) {
	this(props, semicolonChar, equalChar, (Object[][])null, (Class[][])null, ce, ccs);
    }

    /**
     * @param props Method/field names && values <BR>
     * ";" is used as a method(field) names separator. <BR>
     * "=" is used as a method(field) name - expected value separator.
     * @param semicolonChar Char to divide one method/field from another.
     * @param classes Parameters classes.
     * @param results Objects to compare converted to String method/field values to.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     */
    public StringPropChooser(String props, 
			     Object[][] params, 
			     Class[][] classes, 
			     boolean ce,
			     boolean ccs) {
	this(props, ";", "=", params, classes, ce, ccs);
    }

    /**
     * @param props Method/field names && values
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     */
    public StringPropChooser(String props, 
			     boolean ce,
			     boolean ccs) {
	this(props, (Object[][])null, (Class[][])null, ce, ccs);
    }

    /**
     * @see org.netbeans.jemmy.ComponentChooser
     */
    public String getDescription() {
	String result = "";
	for(int i = 0; i < propNames.length; i++) {
	    if(!result.equals("")) {
		result = result + ";";
	    }
	    result = result + propNames[i] + "=" + (String)results[i];
	}
	return("Component by properties array\n    : " + result);
    }

    /**
     * Method to check property.
     * Compares <value>.toString() to (String)etalon according ce and ccs constructor parameters.
     * @param value Method/field value
     * @param etalon Object to compare to.
     */
    protected boolean checkProperty(Object value, Object etalon) {
	String v, e;
	if(compareCaseSensitive) {
	    v = value.toString().toUpperCase();
	    e = ((String)etalon).toUpperCase();
	} else {
	    v = value.toString();
	    e = ((String)etalon);
	}
	if(compareExactly) {
	    return(v.equals(e));
	} else {
	    return(v.indexOf(e) != -1);
	}
    }

    /*split string to array*/
    private static String[] cutToArray(String resources, String semicolon, String equal, boolean names) {
 	StringTokenizer token = new StringTokenizer(resources, semicolon);
 	String[] props = new String[token.countTokens()];
 	String nextProp;
 	int ind = 0;
 	while(token.hasMoreTokens()) {
 	    nextProp = token.nextToken();
 	    StringTokenizer subtoken = new StringTokenizer(nextProp, equal);
 	    if(subtoken.countTokens() == 2) {
		props[ind] = subtoken.nextToken();
		if(!names) {
		    props[ind] = subtoken.nextToken();
		}
 	    } else {
		props[ind] = null;
 	    }
	    ind++;
	}
	return(props);
    }

}
