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

package org.netbeans.modules.web.monitor.client; 

import java.util.*;
import javax.servlet.http.HttpUtils;
import org.netbeans.modules.web.monitor.data.*;


/**
 * Util.java
 *
 * For the next proper release of the monitor module, these methods
 * should move in with their respective data objects. I can't do that
 * for now because I would break compatibility with tomcat-monitor.jar
 * which include copies of the data files. 
 *
 * Created: Thu Aug 30 17:43:28 2001
 *
 * @author Ana von Klopp
 * @version
 */

public class Util  {

    private final static boolean debug = false;

    public Util() {}

    /**
     * We use this method to compose a query string from the
     * parameters instead of using the query string we recorded.  This
     * is used by edit/replay, and also as a workaround for regular
     * replay as getParameters() seems to be better implemented to
     * deal with multibyte than getQueryString()...  */

    public static void composeQueryString(RequestData rd) { 
	
	if(debug) System.out.println("Doing query string");
	
	if(rd.sizeParam() == 0) return;
	
	Param[] params = rd.getParam();
	StringBuffer buf = new StringBuffer(512);
	String name, value;
	 
	for(int i=0; i < params.length; i++) {

	    try {
		name = params[i].getName().trim();
		if(debug) System.out.println("name: " + name);
		value = params[i].getValue(); 
		if(debug) System.out.println("value: " + value);
	    }
	    catch(Exception ex) { 
		continue;
	    }
	    if(name.equals("")) continue; 

	    if (value != null) value = value.trim();
	    else value = "";
	    
	    if(i>0) buf.append('&'); // NOI18N
	    buf.append(name);
	    buf.append('=');
	    buf.append(value);
	}
	rd.setAttributeValue("queryString", buf.toString());
	rd.setParam(new Param[0]);
	
	if (debug) 
	    System.out.println("EditPanel::composedQueryString: [" +
			       buf.toString() + "]");
    }

    static boolean removeParametersFromQuery(RequestData rd) {
		 
	// Data wasn't parameterized
	if(rd.sizeParam() == 0) return false;
	
	String queryString =
	    rd.getAttributeValue("queryString");

	// MULTIBYTE - I think this isn't working... 
	Hashtable ht = null;
	try {
	    ht = javax.servlet.http.HttpUtils.parseQueryString(queryString);
	}
	catch(IllegalArgumentException iae) {
	    // do nothing, that's OK
	    return false;
	}
	if(ht == null || ht.isEmpty()) return false;
	
	Enumeration e = ht.keys();

	while(e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    try {
		String[] value = (String[])(ht.get(name));
		for(int i=0; i<value.length; ++i) {
		    if(debug) System.out.println("Removing " + name + " " +  value);
		    Param p = findParam(rd.getParam(), name, value[i]);
		    rd.removeParam(p);
		}
	    }
	    catch(Exception ex) {
	    }
	}
	return true;
    }

    static void addParametersToQuery(RequestData rd) {

	Hashtable ht = null;
	String queryString = rd.getAttributeValue("queryString");
	try {
	    ht = javax.servlet.http.HttpUtils.parseQueryString(queryString);
	}
	catch(Exception ex) { }
			    
	if(ht != null && ht.size() > 0) {
	    Enumeration e = ht.keys();
	    while(e.hasMoreElements()) {
		String name = (String)e.nextElement();
		String[] value = (String[])(ht.get(name));
		for(int i=0; i<value.length; ++i) {
		    if(debug) 
			System.out.println("Adding " + name +  //NOI18N
					   " " +  value); //NOI18N
		    Param p = new Param(name, value[i]);
		    rd.addParam(p);
		}
	    }
	}
    }

    /**
     * The session cookie and the actual session that was used might
     * be out of synch. This method makes sure that if the request
     * contained an incoming session cookie and a session was used 
     * then the IDs will match (the session ID will be the one from
     * the session, not the one from the cookie). 
     */
      
    public static void setSessionCookieHeader(MonitorData md) {
	
	// First we check *whether* we have a session cookie at
	// all... 
	Headers headers = md.getRequestData().getHeaders();
	int numParams = headers.sizeParam();
	
	if(numParams == 0) return;

	boolean sessionCookie = false;
	Param[] params = headers.getParam(); 
	StringBuffer cookiesOut = new StringBuffer("");
	 
	for(int i=0; i<numParams; ++i) {

	    Param p = params[i];
	    
	    if(p.getAttributeValue("name").equals("Cookie")) {
		
		String cookies = p.getAttributeValue("value");
		
		StringTokenizer st = new StringTokenizer(cookies, ";");
		
		while(st.hasMoreTokens()) {
		    String cookie = st.nextToken();
		    if(debug) System.out.println("Now doing " + cookie);
		    if(cookie.startsWith("JSESSIONID")) {
			sessionCookie = true;
			if(debug) System.out.println("Found session cookie");
			if(debug) System.out.println("Getting session ID");
			String sessionID = null; 
			try {
			    sessionID = 
				md.getSessionData().getAttributeValue("id");
			    if(debug) System.out.println("..." + sessionID);
			}
			catch(Exception ex) {}
			if(debug) System.out.println("Setting session cookie");
			cookiesOut.append("JSESSIONID=");
			cookiesOut.append(sessionID);
			cookiesOut.append(";");
		    }
		    else {
			if(debug) System.out.println("Appending " + cookie); 
			cookiesOut.append(cookie);
			cookiesOut.append(";");
		    }
		}
		if(debug) 
		    System.out.println("Cookie string: " +
				       cookiesOut.toString()); 
		if(sessionCookie) {
		    if(debug) System.out.println("Found session cookie");
		    p.setAttributeValue("value",
					cookiesOut.toString());
		}
	    }
	}
	if(debug) 
	    System.out.println
		(md.getRequestData().getHeaders().getHashtable().get("Cookie")); 
    }

    /**
     * find the param with the given name and value from the list.
     */
    public static Param findParam(Param [] myParams, String name,
				  String value) { 
	for (int i=0; i < myParams.length; i++) {
	    Param param = myParams[i];
	    if (name.equals(param.getName()) &&
		value.equals(param.getValue()) ) {
		return param;
	    }
	}
	return null;
    }
}
