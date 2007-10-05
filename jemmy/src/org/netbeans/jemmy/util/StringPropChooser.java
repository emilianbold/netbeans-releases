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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.util;

import java.util.StringTokenizer;

import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.jemmy.operators.Operator.StringComparator;

/**
 *
 * Implementation of org.netbeans.jemmy.ComponentChooser interface.
 * Class can be used to find component by its field/methods values converted to String.<br>
 *
 * Example:
 * <pre>
 *	    JLabel label = JLabelOperator.findJLabel(frm0, new StringPropChooser("getText=JLabel",
 *										 false, true));
 * </pre>
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class StringPropChooser extends PropChooser{

    private StringComparator comparator;

    /**
     * Constructs a StringPropChooser object.
     * @param propNames Names of methods/fields
     * @param params Parameters values for methods. <BR>
     * @param classes Parameters classes.
     * @param results Objects to compare converted to String method/field values to.
     * @param comparator Defines string comparision criteria.
     */
    public StringPropChooser(String[] propNames, 
			     Object[][] params, 
			     Class[][] classes, 
			     String[] results,
			     StringComparator comparator) {
	super(propNames, params, classes, results);
        this.comparator = comparator;
    }

    /**
     * Constructs a StringPropChooser object.
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
        this(propNames, params, classes, results, new DefaultStringComparator(ce, ccs));
    }

    /**
     * Constructs a StringPropChooser object.
     * @param propNames Names of methods/fields
     * @param results Objects to compare converted to String method/field values to.
     * @param comparator Defines string comparision criteria.
     */
    public StringPropChooser(String[] propNames, 
			     String[] results,
			     StringComparator comparator) {
	this(propNames, (Object[][])null, (Class[][])null, results, comparator);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param propNames Names of methods/fields
     * @param results Objects to compare converted to String method/field values to.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     * @param @deprecated Use constructors with <code>StringComparator</code> parameters.
     */
    public StringPropChooser(String[] propNames, 
			     String[] results,
			     boolean ce,
			     boolean ccs) {
	this(propNames, (Object[][])null, (Class[][])null, results, ce, ccs);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values <BR>
     * Like "getText=button;isVisible=true"
     * @param semicolonChar Method(field) names separator.
     * @param equalChar Method(field) name - expected value separator.
     * @param params Parameters values for methods.
     * @param classes Parameters classes.
     * @param comparator Defines string comparision criteria.
     */
    public StringPropChooser(String props, 
			     String semicolonChar,
			     String equalChar,
			     Object[][] params, 
			     Class[][] classes, 
			     StringComparator comparator) {
	this(cutToArray(props, semicolonChar, equalChar, true), params, classes,
	     cutToArray(props, semicolonChar, equalChar, false), comparator);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values <BR>
     * Like "getText=button;isVisible=true"
     * @param semicolonChar Method(field) names separator.
     * @param equalChar Method(field) name - expected value separator.
     * @param params Parameters values for methods.
     * @param classes Parameters classes.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     * @param @deprecated Use constructors with <code>StringComparator</code> parameters.
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
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values
     * @param semicolonChar Method(field) names separator.
     * @param equalChar Method(field) name - expected value separator.
     * @param comparator Defines string comparision criteria.
     */
    public StringPropChooser(String props, 
			     String semicolonChar,
			     String equalChar,
			     StringComparator comparator) {
	this(props, semicolonChar, equalChar, (Object[][])null, (Class[][])null, comparator);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values
     * @param semicolonChar Method(field) names separator.
     * @param equalChar Method(field) name - expected value separator.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     * @param @deprecated Use constructors with <code>StringComparator</code> parameters.
     */
    public StringPropChooser(String props, 
			     String semicolonChar,
			     String equalChar,
			     boolean ce,
			     boolean ccs) {
	this(props, semicolonChar, equalChar, (Object[][])null, (Class[][])null, ce, ccs);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values <BR>
     * ";" is used as a method(field) names separator. <BR>
     * "=" is used as a method(field) name - expected value separator.
     * @param params Parameters values for methods.
     * @param classes Parameters classes.
     * @param comparator Defines string comparision criteria.
     */
    public StringPropChooser(String props, 
			     Object[][] params, 
			     Class[][] classes, 
			     StringComparator comparator) {
	this(props, ";", "=", params, classes, comparator);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values <BR>
     * ";" is used as a method(field) names separator. <BR>
     * "=" is used as a method(field) name - expected value separator.
     * @param params Parameters values for methods.
     * @param classes Parameters classes.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     * @param @deprecated Use constructors with <code>StringComparator</code> parameters.
     */
    public StringPropChooser(String props, 
			     Object[][] params, 
			     Class[][] classes, 
			     boolean ce,
			     boolean ccs) {
	this(props, ";", "=", params, classes, ce, ccs);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values
     * ";" is used as a method(field) names separator. <BR>
     * "=" is used as a method(field) name - expected value separator.
     * @param comparator Defines string comparision criteria.
     */
    public StringPropChooser(String props, 
			     StringComparator comparator) {
	this(props, (Object[][])null, (Class[][])null, comparator);
    }

    /**
     * Constructs a StringPropChooser object.
     * @param props Method/field names && values
     * ";" is used as a method(field) names separator. <BR>
     * "=" is used as a method(field) name - expected value separator.
     * @param ce Compare exactly.
     * @param ccs Compare case sensitive.
     * @param @deprecated Use constructors with <code>StringComparator</code> parameters.
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
     * Compares "value".toString() to (String)etalon according ce and ccs constructor parameters.
     * @param value Method/field value
     * @param etalon Object to compare to.
     * @return true if the value matches the etalon.
     */
    protected boolean checkProperty(Object value, Object etalon) {
        return(comparator.equals(value.toString(), (String)etalon));
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
