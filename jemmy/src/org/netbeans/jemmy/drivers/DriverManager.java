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

package org.netbeans.jemmy.drivers;

import java.util.Hashtable;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;

public class DriverManager {
    public static final String DRIVER_ID = "drivers.";
    public static final String TREE_DRIVER_ID = DRIVER_ID + "tree";
    public static final String TEXT_DRIVER_ID = DRIVER_ID + "text";
    public static final String KEY_DRIVER_ID = DRIVER_ID + "key";
    public static final String MOUSE_DRIVER_ID = DRIVER_ID + "mouse";
    public static final String SCROLL_DRIVER_ID = DRIVER_ID + "scroll";
    public static final String BUTTON_DRIVER_ID = DRIVER_ID + "button";
    public static final String LIST_DRIVER_ID = DRIVER_ID + "list";
    public static final String MULTISELLIST_DRIVER_ID = DRIVER_ID + "multisellist";
    public static final String TABLE_DRIVER_ID = DRIVER_ID + "table";
    public static Driver getDriver(String id, Class operatorClass, JemmyProperties props) {
	Driver result = getADriver(id, operatorClass, props);
	if(result == null) {
	    return(getDriver(id, operatorClass));
	} else {
	    return(result);
	}
    }
    public static Driver getDriver(String id, Class operatorClass) {
	Driver result = getADriver(id, operatorClass, JemmyProperties.getProperties());
	if(result == null) {
	    throw(new JemmyException("No \"" + id + "\" driver registered for " +
				     operatorClass.getName() + " class!"));
	} else {
	    return(result);
	}
    }
    public static void setDriver(String id, Driver driver, Class operatorClass) {
	JemmyProperties.
	    setCurrentProperty(makeID(id, operatorClass), driver);
    }
    public static void setDriver(String id, Driver driver) {
	Class[] supported = driver.getSupported();
	for(int i = 0; i < supported.length; i++) {
	    setDriver(id, driver, supported[i]);
	}
    }
    public static void removeDriver(String id, Class operatorClass) {
	JemmyProperties.
	    removeCurrentProperty(makeID(id, operatorClass));
    }
    public static void removeDriver(String id, Class[] operatorClasses) {
	for(int i = 0; i < operatorClasses.length; i++) {
	    removeDriver(id, operatorClasses[i]);
	}
    }
    public static TreeDriver getTreeDriver(Class operatorClass) {
	return((TreeDriver)getDriver(TREE_DRIVER_ID, operatorClass));
    }
    public static void setTreeDriver(TreeDriver driver) {
	setDriver(TREE_DRIVER_ID, driver);
    }
    public static TextDriver getTextDriver(Class operatorClass) {
	return((TextDriver)getDriver(TEXT_DRIVER_ID, operatorClass));
    }
    public static void setTextDriver(TextDriver driver) {
	setDriver(TEXT_DRIVER_ID, driver);
    }
    public static KeyDriver getKeyDriver(Class operatorClass) {
	return((KeyDriver)getDriver(KEY_DRIVER_ID, operatorClass));
    }
    public static void setKeyDriver(KeyDriver driver) {
	setDriver(KEY_DRIVER_ID, driver);
    }
    public static MouseDriver getMouseDriver(Class operatorClass) {
	return((MouseDriver)getDriver(MOUSE_DRIVER_ID, operatorClass));
    }
    public static void setMouseDriver(MouseDriver driver) {
	setDriver(MOUSE_DRIVER_ID, driver);
    }
    public static ScrollDriver getScrollDriver(Class operatorClass) {
	return((ScrollDriver)getDriver(SCROLL_DRIVER_ID, operatorClass));
    }
    public static void setScrollDriver(ScrollDriver driver) {
	setDriver(SCROLL_DRIVER_ID, driver);
    }
    public static ButtonDriver getButtonDriver(Class operatorClass) {
	return((ButtonDriver)getDriver(BUTTON_DRIVER_ID, operatorClass));
    }
    public static void setButtonDriver(ButtonDriver driver) {
	setDriver(BUTTON_DRIVER_ID, driver);
    }
    public static ListDriver getListDriver(Class operatorClass) {
	return((ListDriver)getDriver(LIST_DRIVER_ID, operatorClass));
    }
    public static void setListDriver(ListDriver driver) {
	setDriver(LIST_DRIVER_ID, driver);
    }
    public static MultiSelListDriver getMultiSelListDriver(Class operatorClass) {
	return((MultiSelListDriver)getDriver(MULTISELLIST_DRIVER_ID, operatorClass));
    }
    public static void setMultiSelListDriver(MultiSelListDriver driver) {
	setDriver(MULTISELLIST_DRIVER_ID, driver);
    }
    public static TableDriver getTableDriver(Class operatorClass) {
	return((TableDriver)getDriver(TABLE_DRIVER_ID, operatorClass));
    }
    public static void setTableDriver(TableDriver driver) {
	setDriver(TABLE_DRIVER_ID, driver);
    }
    private static String makeID(String id, Class operatorClass) {
	return(id + "." + operatorClass.getName());
    }
    private static Driver getADriver(String id, Class operatorClass, JemmyProperties props) {
	Class superClass = operatorClass;
	Driver drvr;
	do {
	    drvr = (Driver)props.
		getProperty(makeID(id, superClass));
	    if(drvr != null) {
		return(drvr);
	    }
	} while(ComponentOperator.class.
		isAssignableFrom(superClass = superClass.getSuperclass()));
	return(null);
    }
    static {
	new InputDriverInstaller().install();
	new DefaultDriverInstaller().install();
    }
}
