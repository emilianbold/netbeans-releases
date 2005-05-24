/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *	This generated bean class ObjectSix matches the schema element object-six
 *
 */

package org.netbeans.modules.j2ee.sun.validation.samples.simple.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ObjectSix extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String PROPERTY_ONE = "PropertyOne";	// NOI18N
	static public final String PROPERTY_TWO = "PropertyTwo";	// NOI18N

	public ObjectSix() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ObjectSix(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("property-one", 	// NOI18N
			PROPERTY_ONE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("property-two", 	// NOI18N
			PROPERTY_TWO, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is optional
	public void setPropertyOne(String value) {
		this.setValue(PROPERTY_ONE, value);
	}

	//
	public String getPropertyOne() {
		return (String)this.getValue(PROPERTY_ONE);
	}

	// This attribute is optional
	public void setPropertyTwo(String value) {
		this.setValue(PROPERTY_TWO, value);
	}

	//
	public String getPropertyTwo() {
		return (String)this.getValue(PROPERTY_TWO);
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
		// Validating property propertyOne
		if (getPropertyOne() != null) {
		}
		// Validating property propertyTwo
		if (getPropertyTwo() != null) {
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PropertyOne");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPropertyOne();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROPERTY_ONE, 0, str, indent);

		str.append(indent);
		str.append("PropertyTwo");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPropertyTwo();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PROPERTY_TWO, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ObjectSix\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : sample.dtd
    Created on : May 8, 2003, 11:18 AM
    Author     : Rajeshwar Patil
    Description:
        Purpose of the document follows.
-->

<!--
-->
<!ELEMENT root-element (non-zero-length-property, number-property+,
    boolean-property*, range-property?, enumeration-property,
    object-one, object-two?, object-three?, object-four,
    object-five*, object-six+, object-seven*,object-eight*)>

<!--
-->
<!ELEMENT object-one (property-one, property-two?)>


<!--
-->
<!ELEMENT object-two (property-one, property-two)>


<!--
-->
<!ELEMENT object-three (property-one, property-two?,
    property-three+, property-four*)>


<!--
-->
<!ELEMENT object-four (property-one?, property-two?)>


<!--
-->
<!ELEMENT object-five (property-one, property-two?)>


<!--
-->
<!ELEMENT object-six (property-one?, property-two?)>


<!--
-->
<!ELEMENT object-seven (property-one, property-two)>


<!--
-->
<!ELEMENT object-eight (property-one, property-two?, child-object-one,
    child-object-two?, child-object-three*, child-object-four+)>


<!--
-->
<!ELEMENT child-object-one (property-one, property-two?)>


<!--
-->
<!ELEMENT child-object-two (property-one, property-two)>


<!--
-->
<!ELEMENT child-object-three (property-one?, property-two?)>


<!--
-->
<!ELEMENT child-object-four (property-one, property-two?,
    property-three+, property-four*)>


<!ELEMENT boolean-property (#PCDATA)>
<!ELEMENT number-property (#PCDATA)>
<!ELEMENT non-zero-length-property (#PCDATA)>
<!ELEMENT range-property (#PCDATA)>
<!ELEMENT enumeration-property (#PCDATA)>
<!ELEMENT property-one (#PCDATA)>
<!ELEMENT property-two (#PCDATA)>
<!ELEMENT property-three (#PCDATA)>
<!ELEMENT property-four (#PCDATA)>

*/
