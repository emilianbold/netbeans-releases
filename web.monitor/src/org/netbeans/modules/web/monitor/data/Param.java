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

public class Param extends BaseBean {

    static Vector comparators = new Vector();


    public Param() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public Param(String name, String value) {
	setAttributeValue("name", name);
	setAttributeValue("value", value);
    }

    public Param(int options) {
	super(Param.comparators, new GenBeans.Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)
	this.initialize(options);
    }


    public String getName() {
	return getAttributeValue("name"); // NOI18N
    }
    public void   setName(String value) {
	setAttributeValue("name", value); // NOI18N
    }

    public String getValue() {
	return getAttributeValue("value"); // NOI18N
    }
    public void   setValue(String value) {
	setAttributeValue("value", value); // NOI18N
    }


    // Setting the default values of the properties
    void initialize(int options) {

    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    //
    static public void addComparator(BeanComparator c) {
	Param.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c) {
	Param.comparators.remove(c);
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
    public void addPropertyChangeListener(String n, PropertyChangeListener l) {
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
    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("Param\n");
	this.dump(str, "\n  ");
	return str.toString();
    }
}

