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
import org.xml.sax.InputSource;
import com.sun.forte4j.modules.dd.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import com.sun.xml.tree.*;
import org.netbeans.modules.web.monitor.client.TransactionNode;

public class MonitorData extends BaseBean {

    static Vector comparators = new Vector();

    static public final String CLIENTDATA = "ClientData";
    static public final String SESSIONDATA = "SessionData";
    static public final String COOKIESDATA = "CookiesData";
    static public final String REQUESTDATA = "RequestData";
    static public final String SERVLETDATA = "ServletData";

    public MonitorData() {
	this(null, Common.USE_DEFAULT_VALUES);
    }

    public MonitorData(Node doc, int options) {
	this(Common.NO_DEFAULT_VALUES);
	if (doc == null)
	    {
		doc = GraphManager.createRootElementNode("MonitorData");
		if (doc == null)
		    throw new RuntimeException("failed to create a new DOM root!");
	    }
	Node n = GraphManager.getElementNode("MonitorData", doc);
	if (n == null)
	    throw new RuntimeException("doc root not found in the DOM graph!");

	this.graphManager.setXmlDocument(doc);

	// Entry point of the createBeans() recursive calls
	this.createBean(n, this.graphManager());
	this.initialize(options);
    }
    
    public MonitorData(int options) {
	super(MonitorData.comparators, new GenBeans.Version(1, 0, 6));
	// The graph manager is allocated in the bean root
	this.graphManager = new GraphManager(this);

	this.createRoot("MonitorData", "MonitorData",
			Common.TYPE_1 | Common.TYPE_BEAN, MonitorData.class);

	// Properties (see root bean comments for the bean graph)
	this.createProperty("ClientData", CLIENTDATA, 
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ClientData.class);
	this.createAttribute(CLIENTDATA, "protocol", "Protocol", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(CLIENTDATA, "remoteAddress", "RemoteAddress", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(CLIENTDATA, "software", "Software", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "locale", "Locale", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "formatsAccepted", "FormatsAccepted", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "encodingsAccepted", "EncodingsAccepted", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "charsetsAccepted", "CharsetsAccepted", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("SessionData", SESSIONDATA, 
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    SessionData.class);
	this.createAttribute(SESSIONDATA, "before", "Before", 
			     AttrProp.ENUM | AttrProp.REQUIRED,
			     new String[] {
				 "false",
				 "true"
			     }, "false");
	this.createAttribute(SESSIONDATA, "after", "After", 
			     AttrProp.ENUM | AttrProp.REQUIRED,
			     new String[] {
				 "false",
				 "true"
			     }, "false");
	this.createAttribute(SESSIONDATA, "id", "Id", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SESSIONDATA, "created", "Created", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("CookiesData", COOKIESDATA, 
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    CookiesData.class);

	// PENDING - I think I need to move this to the request data
	// class to make it an independent class that can be handled
	// on its own (if I want to pass less data about). 
	this.createProperty("RequestData", REQUESTDATA, 
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    RequestData.class);
	this.createAttribute(REQUESTDATA, "uri", "Uri", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(REQUESTDATA, "method", "Method", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);

	this.createAttribute(REQUESTDATA, "urlencoded", "Urlencoded", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createAttribute(REQUESTDATA, "queryString", "QueryString", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "protocol", "Protocol", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "ipaddress", "Ipaddress", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "scheme", "Scheme", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createAttribute(REQUESTDATA, "status", "Status", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("ServletData", SERVLETDATA, 
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ServletData.class);
	this.createAttribute(SERVLETDATA, "name", "Name", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "className", "ClassName", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "packageName", "PackageName", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "servletInfo", "ServletInfo", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "relPath", "RelPath", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "transPath", "TransPath", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "contextName", "ContextName", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "absPath", "AbsPath", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "jre", "Jre", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "platform", "Platform", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "serverPort", "ServerPort", 
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "serverName", "ServerName", 
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createAttribute("resource", "Resource", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute("timestamp", "Timestamp", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute("id", "Id", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute("method", "Metod", 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);

	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {
    }

    // This attribute is mandatory
    public void setClientData(ClientData value) {
	this.setValue(CLIENTDATA, value);
    }

    //
    public ClientData getClientData() {
	return (ClientData)this.getValue(CLIENTDATA);
    }

    // This attribute is optional
    public void setSessionData(SessionData value) {
	this.setValue(SESSIONDATA, value);
    }

    //
    public SessionData getSessionData() {
	return (SessionData)this.getValue(SESSIONDATA);
    }

    // This attribute is optional
    public void setCookiesData(CookiesData value) {
	this.setValue(COOKIESDATA, value);
    }

    //
    public CookiesData getCookiesData() {
	return (CookiesData)this.getValue(COOKIESDATA);
    }

    // This attribute is mandatory
    public void setRequestData(RequestData value) {
	this.setValue(REQUESTDATA, value);
    }

    //
    public RequestData getRequestData() {
	return (RequestData)this.getValue(REQUESTDATA);
    }

    // This attribute is optional
    public void setServletData(ServletData value) {
	this.setValue(SERVLETDATA, value);
    }

    //
    public ServletData getServletData() {
	return (ServletData)this.getValue(SERVLETDATA);
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    static public void addComparator(BeanComparator c) {
	MonitorData.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c) {
	MonitorData.comparators.remove(c);
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
    public void addPropertyChangeListener(String n, PropertyChangeListener l){
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

    //
    // This method returns the root of the bean graph
    // Each call creates a new bean graph from the specified DOM graph
    //
    public static MonitorData createGraph(Node doc) {
	return new MonitorData(doc, Common.NO_DEFAULT_VALUES);
    }

    public static MonitorData createGraph(java.io.Reader reader)
	throws IOException {
	try {
	    return MonitorData.createGraph(reader, false);
	}
	catch(IOException ioe) {
	    throw ioe;
	}
    }

    public static MonitorData createGraph(java.io.Reader reader, 
					  boolean validate) throws IOException {
	try {
	    InputSource insource = new InputSource(reader);
	    insource.setEncoding("UTF-8");
	    XmlDocument doc = 
		XmlDocument.createXmlDocument(insource, validate);
	    return MonitorData.createGraph(doc);
	}
	catch (Throwable t) {
	    throw new IOException();
	}
    }

    //
    // This method returns the root for a new empty bean graph
    //
    public static MonitorData createGraph() {
	return new MonitorData();
    }

    public void write(OutputStream out) throws IOException {
	throw new RuntimeException("Don't do this!");
    }
    
    public void write(Writer writer) throws IOException {
	
	if(this.graphManager != null) {
	    Document document = this.graphManager.getXmlDocument();
	    if(document == null)
		throw new NullPointerException(); 
	    if (document instanceof com.sun.xml.tree.XmlDocument) 
		((com.sun.xml.tree.XmlDocument)document).write(writer,
							       "UTF-8"); // NOI18N
	    else throw new IOException();
	}
    }
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	BaseBean n;
	str.append(indent);
	str.append("ClientData"); // NOI18N
	n = this.getClientData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(CLIENTDATA, 0, str, indent);

	str.append(indent);
	str.append("SessionData"); // NOI18N
	n = this.getSessionData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(SESSIONDATA, 0, str, indent);

	str.append(indent);
	str.append("CookiesData"); // NOI18N
	n = this.getCookiesData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(COOKIESDATA, 0, str, indent);

	str.append(indent);
	str.append("RequestData"); // NOI18N
	n = this.getRequestData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(REQUESTDATA, 0, str, indent);

	str.append(indent);
	str.append("ServletData"); // NOI18N
	n = this.getServletData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(SERVLETDATA, 0, str, indent);

	str.append(indent);

    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("MonitorData\n"); // NOI18N
	this.dump(str, "\n  "); // NOI18N
	return str.toString();
    }

    public TransactionNode createTransactionNode(boolean current) {
	TransactionNode node = 
	    new TransactionNode(this.getAttributeValue("id"), // NOI18N
				this.getAttributeValue("method"), // NOI18N
				this.getAttributeValue("resource")); // NOI18N
	node.setCurrent(current);
	return node;
    }
}
