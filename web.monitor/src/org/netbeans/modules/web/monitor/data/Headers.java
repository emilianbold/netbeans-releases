/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *	This generated bean class Headers matches the DTD element Headers
 *
 *	Generated on Thu Jan 11 18:34:12 PST 2001
 */

package org.netbeans.modules.web.monitor.data;
import org.w3c.dom.*;
import com.sun.forte4j.modules.dd.*;
import java.beans.*;
import java.util.*;

public class Headers extends BaseBean {

    static Vector comparators = new Vector();

    static public final String PARAM = "Param";

    public Headers() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public Headers(int options) {
	super(Headers.comparators, new GenBeans.Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)
	this.createProperty("Param", PARAM, 
			    Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    Param.class);
	this.createAttribute(PARAM, "name", "Name", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(PARAM, "value", "Value", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {

    }

    // This attribute is an array, possibly empty
    public void setParam(int index, Param value) {
	this.setValue(PARAM, index, value);
    }

    //
    public Param getParam(int index) {
	return (Param)this.getValue(PARAM, index);
    }

    // This attribute is an array, possibly empty
    public void setParam(Param[] value) {
	this.setValue(PARAM, value);
    }

    //
    public Param[] getParam() {
	return (Param[])this.getValues(PARAM);
    }

    public Hashtable getHashtable() {

	Param[] headers =  (Param[])this.getValues(PARAM);
	int numHeaders = headers.length;
	Hashtable ht = new Hashtable(numHeaders);
	
	for(int i=0; i<numHeaders; ++i) {
	    String name =  headers[i].getAttributeValue("name");
	    String value = headers[i].getAttributeValue("value");
	    ht.put(name, value);
	}	
	return ht;
    }

    // This ain't working
    /*
    public void setHashtable(Hashtable ht) {

	int numHeaders = ht.size();
	
	Param[] headers = new Param[numHeaders];
	
	Enumeration e = ht.keys();	
	int num = 0;
	while(e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    String value= (String)(ht.get(name));
	    headers[num] = new Param(name, value);
	    ++num;
	}
	this.setParam(headers); 
    }
    */

    // Return the number of properties
    public int sizeParam() {
	return this.size(PARAM);
    }

    // Add a new element returning its index in the list
    public int addParam(Param value) {
	return this.addValue(PARAM, value);
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeParam(Param value) {
	return this.removeValue(PARAM, value);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    static public void addComparator(BeanComparator c) {
	Headers.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c) {
	Headers.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n,
					  PropertyChangeListener l) {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n,
					     PropertyChangeListener l) { 
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	BaseBean n;
	str.append(indent);
	str.append("Param["+this.sizeParam()+"]");
	for(int i=0; i<this.sizeParam(); i++)
	    {
		str.append(indent+"\t");
		str.append("#"+i+":");
		n = this.getParam(i);
		if (n != null)
		    n.dump(str, indent + "\t");
		else
		    str.append(indent+"\tnull");
		this.dumpAttributes(PARAM, i, str, indent);
	    }

    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("Headers\n");
	this.dump(str, "\n  ");
	return str.toString();
    }

    public String toString() {
	StringBuffer buf = new StringBuffer("Request Headers\n");
	
	Param[] params = getParam();
	buf.append(String.valueOf(params.length));
	buf.append(" header lines\n");
	for(int i=0; i<params.length; ++i) {
	    buf.append(String.valueOf(i));
	    buf.append(". Attribute: ");
	    buf.append(params[i].getAttributeValue("name"));
	    buf.append(", Value: ");
	    buf.append(params[i].getAttributeValue("value"));
	    buf.append("\n");
	}
	return buf.toString();
    }
}
