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

package org.netbeans.modules.web.monitor.data;

import org.w3c.dom.*;
import com.sun.forte4j.modules.dd.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import com.sun.xml.tree.*;

public class RequestData extends BaseBean {

    static Vector comparators = new Vector();

    static public final String PARAM = "Param";
    static public final String HEADERS = "Headers";

    static private final boolean debug = false;
    

    public RequestData() {
	this(Common.USE_DEFAULT_VALUES);
    }


    public RequestData(Node doc, int options) {
	this(Common.NO_DEFAULT_VALUES);
	if (doc == null)
	    {
		doc = GraphManager.createRootElementNode("RequestData");
		if (doc == null)
		    throw new RuntimeException("failed to create a new DOM root!");
	    }
	Node n = GraphManager.getElementNode("RequestData", doc);
	if (n == null)
	    throw new RuntimeException("doc root not found in the DOM graph!");

	this.graphManager.setXmlDocument(doc);

	// Entry point of the createBeans() recursive calls
	this.createBean(n, this.graphManager());
	this.initialize(options);
    }

    public RequestData(int options)	{
	super(RequestData.comparators, new GenBeans.Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)

	this.createProperty("Headers", HEADERS, 
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    Headers.class);

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

    // This attribute is mandatory
    public void setHeaders(Headers value) {
	this.setValue(HEADERS, value);
    }

    //
    public Headers getHeaders() {
	return (Headers)this.getValue(HEADERS);
    }


    // This attribute is an array, possibly empty
    public void setParam(int index, Param value)
    {
	this.setValue(PARAM, index, value);
    }

    //
    public Param getParam(int index)
    {
	return (Param)this.getValue(PARAM, index);
    }

    // This attribute is an array, possibly empty
    public void setParam(Param[] value)
    {
	if(debug) System.out.println("setParam(Param[] value)");
	try {
	    this.setValue(PARAM, value);
	}
	catch(Exception ex) {
	    ex.printStackTrace();
	}
    }

    //
    public Param[] getParam()
    {
	return (Param[])this.getValues(PARAM);
    }

    // Return the number of properties
    public int sizeParam()
    {
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
    public int removeParam(Param value)
    {
	return this.removeValue(PARAM, value);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

    //
    static public void addComparator(BeanComparator c)
    {
	RequestData.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c)
    {
	RequestData.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
	String s;
	BaseBean n;

	str.append(indent);
	str.append("Headers");
	n = this.getHeaders();
	if (n != null)
	    n.dump(str, indent + "\t");
	else
	    str.append(indent+"\tnull");
	this.dumpAttributes(HEADERS, 0, str, indent);

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
	str.append("RequestData\n");
	this.dump(str, "\n  ");
	return str.toString();
    }
    
    //
    // This method returns the root of the bean graph
    // Each call creates a new bean graph from the specified DOM graph
    //
    public static RequestData createGraph(Node doc) {
	return new RequestData(doc, Common.NO_DEFAULT_VALUES);
    }

    public static RequestData createGraph(InputStream in) {
	return RequestData.createGraph(in, false);
    }

    public static RequestData createGraph(InputStream in, boolean validate) {
	try {
	    XmlDocument doc = XmlDocument.createXmlDocument(in, validate);
	    return RequestData.createGraph(doc);
	}
	catch (Throwable t) {
	    throw new RuntimeException("DOM graph creation failed: "+
				       t.getMessage()); 
	}
    }

    //
    // This method returns the root for a new empty bean graph
    //
    public static RequestData createGraph() {
	return new RequestData();
    }


}

