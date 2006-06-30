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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 *	This generated bean class OptionValuePair matches the schema element option-value-pair
 *
 *	Generated on Thu Sep 25 15:18:26 PDT 2003
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class OptionValuePair extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String OPTION_NAME = "OptionName";	// NOI18N
	static public final String CONDITIONAL_VALUE = "ConditionalValue";	// NOI18N

	public OptionValuePair() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public OptionValuePair(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("option-name", 	// NOI18N
			OPTION_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("conditional-value", 	// NOI18N
			CONDITIONAL_VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is mandatory
	public void setOptionName(String value) {
		this.setValue(OPTION_NAME, value);
	}

	//
	public String getOptionName() {
		return (String)this.getValue(OPTION_NAME);
	}

	// This attribute is mandatory
	public void setConditionalValue(String value) {
		this.setValue(CONDITIONAL_VALUE, value);
	}

	//
	public String getConditionalValue() {
		return (String)this.getValue(CONDITIONAL_VALUE);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property optionName
		if (getOptionName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getOptionName() == null", "optionName", this);	// NOI18N
		}
		// Validating property conditionalValue
		if (getConditionalValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getConditionalValue() == null", "conditionalValue", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("OptionName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getOptionName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(OPTION_NAME, 0, str, indent);

		str.append(indent);
		str.append("ConditionalValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getConditionalValue();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONDITIONAL_VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("OptionValuePair\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!ELEMENT wizard (name, field-group+)>
<!ELEMENT field-group (name, field+)>
<!ELEMENT field (name, field-value, tag?)>
<!ATTLIST field  field-type                 CDATA     "string"
                 required                   CDATA     "true">
<!ELEMENT field-value (default-field-value, option-value-pair*)>
<!ELEMENT option-value-pair (option-name, conditional-value)>
<!ELEMENT name (#PCDATA)>
<!ELEMENT default-field-value (#PCDATA)>
<!ELEMENT option-name (#PCDATA)>
<!ELEMENT conditional-value (#PCDATA)>
<!ELEMENT tag (tag-item*)>
<!ELEMENT tag-item (#PCDATA)>



*/
