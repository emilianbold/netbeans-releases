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
import javax.servlet.http.Cookie;

public class CookieOut extends BaseBean
{

    static Vector comparators = new Vector();

    public CookieOut()
    {
	this(Common.USE_DEFAULT_VALUES);
    }

    public CookieOut(Cookie cookie) {

	// Note - the XML beans library does not treat NMTOKENS as
	// special - they have to be set as strings! 
	setAttributeValue("name", cookie.getName());
	setAttributeValue("value", cookie.getValue());
	//setAttributeValue("maxAge", cookie.getMaxAge());
	setAttributeValue("maxAge", String.valueOf(cookie.getMaxAge()));
	//setAttributeValue("version", cookie.getVersion());
	setAttributeValue("version", String.valueOf(cookie.getVersion()));

	String domain = "";  //NOI18N
	try { 
	    domain = cookie.getDomain();
	} 
	catch(NullPointerException ne) {} 
	if(domain != null) {
	    if(domain.trim().equals("")) //NOI18N
		setAttributeValue("domain", "");
	    else 
		setAttributeValue("domain", domain);  
	}

	String path = "";  //NOI18N
	try { 
	    path = cookie.getPath();
	} 
	catch(NullPointerException ne) {} 
	if(path != null) {
	    if(path.trim().equals("")) //NOI18N
		setAttributeValue("path", "");
	    else 
		setAttributeValue("path", path);  
	}

	String comment = "";  //NOI18N
	try { 
	    comment = cookie.getComment();
	} 
	catch(NullPointerException ne) {} 
	if(comment != null) {
	    if(comment.trim().equals("")) //NOI18N
		setAttributeValue("comment", "");
	    else 
		setAttributeValue("comment", comment);  
	}
	
	int version = cookie.getVersion();   
	// XML Beans...
	//if(version != 0) setAttributeValue("version", version);  
	if(version != 0) setAttributeValue("version",
					   String.valueOf(version));  
      
	try { 
	    if(cookie.getSecure()) 
		// XMLBeans library... 
		setAttributeValue("secure",
				  String.valueOf(cookie.getSecure())); 
	    
	}
	catch(Exception exc) {}
    }


    public CookieOut(int options)
    {
	super(CookieOut.comparators, new GenBeans.Version(1, 0, 5));
	// Properties (see root bean comments for the bean graph)
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options)
    {

    }

    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

    //
    static public void addComparator(BeanComparator c)
    {
	CookieOut.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c)
    {
	CookieOut.comparators.remove(c);
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
    }

    public String dumpBeanNode()
    {
	StringBuffer str = new StringBuffer();
	str.append("CookieOut\n");
	this.dump(str, "\n  ");
	return str.toString();
    }
}

