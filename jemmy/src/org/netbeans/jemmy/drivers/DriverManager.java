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

/**
 * Manages driver set.
 */
public class DriverManager {
    /**
     * Symbolic constant - prefix for drivers names.
     */
    public static final String DRIVER_ID = "drivers.";
    /**
     * Symbolic constant for tree drivers.
     */
    public static final String TREE_DRIVER_ID = DRIVER_ID + "tree";
    /**
     * Symbolic constant for text drivers.
     */
    public static final String TEXT_DRIVER_ID = DRIVER_ID + "text";
    /**
     * Symbolic constant for key drivers.
     */
    public static final String KEY_DRIVER_ID = DRIVER_ID + "key";
    /**
     * Symbolic constant for mouse drivers.
     */
    public static final String MOUSE_DRIVER_ID = DRIVER_ID + "mouse";
    /**
     * Symbolic constant for scroll drivers.
     */
    public static final String SCROLL_DRIVER_ID = DRIVER_ID + "scroll";
    /**
     * Symbolic constant for button drivers.
     */
    public static final String BUTTON_DRIVER_ID = DRIVER_ID + "button";
    /**
     * Symbolic constant for list drivers.
     */
    public static final String LIST_DRIVER_ID = DRIVER_ID + "list";
    /**
     * Symbolic constant for multiselection list drivers.
     */
    public static final String MULTISELLIST_DRIVER_ID = DRIVER_ID + "multisellist";
    /**
     * Symbolic constant for reorderable list drivers.
     */
    public static final String ORDEREDLIST_DRIVER_ID = DRIVER_ID + "orderedlist";
    /**
     * Symbolic constant for table drivers.
     */
    public static final String TABLE_DRIVER_ID = DRIVER_ID + "table";
    /**
     * Symbolic constant for window drivers.
     */
    public static final String WINDOW_DRIVER_ID = DRIVER_ID + "window";
    /**
     * Symbolic constant for window drivers.
     */
    public static final String FRAME_DRIVER_ID = DRIVER_ID + "frame";
    /**
     * Symbolic constant for frame drivers.
     */
    public static final String FOCUS_DRIVER_ID = DRIVER_ID + "focus";
    /**
     * Symbolic constant for menu drivers.
     */
    public static final String MENU_DRIVER_ID = DRIVER_ID + "menu";

    //cannot be instantiated!
    private DriverManager() {
    }

    /**
     * Searches a driver.
     * @param id Driver type id.
     * @param operatorClass Class to get an driver for.
     * @param JemmyProperties Instance to get driver from.
     */ 
    public static Driver getDriver(String id, Class operatorClass, JemmyProperties props) {
	Driver result = getADriver(id, operatorClass, props);
	if(result == null) {
	    return(getDriver(id, operatorClass));
	} else {
	    return(result);
	}
    }

    /**
     * Searches a driver. Uses <code>operator.getProperties()</code> to
     * receive JemmyProperties instance.
     * @param id Driver type id.
     * @param operator Operator to get an driver for.
     */ 
    public static Driver getDriver(String id, ComponentOperator operator) {
	return(getDriver(id, operator.getClass(), operator.getProperties()));
    }

    /**
     * Searches a driver.
     * Uses current JemmyProperties.
     * @param id Driver type id.
     * @param operatorClass Class to get an driver for.
     */ 
    public static Driver getDriver(String id, Class operatorClass) {
	Driver result = getADriver(id, operatorClass, JemmyProperties.getProperties());
	if(result == null) {
	    throw(new JemmyException("No \"" + id + "\" driver registered for " +
				     operatorClass.getName() + " class!"));
	} else {
	    return(result);
	}
    }

    /**
     * Sets driver for an operator class.
     * @param id Driver type id.
     * @param driver 
     * @param operatorClass Class to set driver for.
     */
    public static void setDriver(String id, Driver driver, Class operatorClass) {
	JemmyProperties.
	    setCurrentProperty(makeID(id, operatorClass), driver);
	if(Boolean.getBoolean(DRIVER_ID + "trace_output")) {
	    JemmyProperties.getCurrentOutput().printLine("Installing " +
							 driver.getClass().getName() +
							 " drifer for " +
							 operatorClass.getName() +
							 " operators.");
	}
    }

    /**
     * Sets driver for all classes supported by driver.
     * @param id Driver type id.
     * @param driver 
     * @param operatorClass Class to set driver for.
     */
    public static void setDriver(String id, Driver driver) {
	Class[] supported = driver.getSupported();
	for(int i = 0; i < supported.length; i++) {
	    setDriver(id, driver, supported[i]);
	}
    }

    /**
     * Removes driver for operator class.
     * @param id Driver type to remove.
     * @param operatorClass Class to remove driver for.
     */
    public static void removeDriver(String id, Class operatorClass) {
	JemmyProperties.
	    removeCurrentProperty(makeID(id, operatorClass));
	if(Boolean.getBoolean(DRIVER_ID + "trace_output")) {
	    JemmyProperties.getCurrentOutput().printLine("Uninstalling a drifer for " +
							 operatorClass.getName() +
							 " operators.");
	}
    }

    /**
     * Removes driver for operator classes.
     * @param id Driver type to remove.
     * @param operatorClasses Classes to remove driver for.
     */
    public static void removeDriver(String id, Class[] operatorClasses) {
	for(int i = 0; i < operatorClasses.length; i++) {
	    removeDriver(id, operatorClasses[i]);
	}
    }

    /**
     * Removes driver for all supported classes.
     * @param id Driver type to remove.
     */
    public static void removeDrivers(String id) {
	String[] keys = JemmyProperties.getCurrentKeys();
	for(int i = 0; i < keys.length; i++) {
	    if(keys[i].startsWith(id)) {
		JemmyProperties.
		    removeCurrentProperty(keys[i]);
	    }
	}
    }

    /**
     * Returns <code>TREE_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static TreeDriver getTreeDriver(Class operatorClass) {
	return((TreeDriver)getDriver(TREE_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>TREE_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static TreeDriver getTreeDriver(ComponentOperator operator) {
	return((TreeDriver)getDriver(TREE_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>TREE_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setTreeDriver(TreeDriver driver) {
	setDriver(TREE_DRIVER_ID, driver);
    }
    /**
     * Returns <code>TEXT_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static TextDriver getTextDriver(Class operatorClass) {
	return((TextDriver)getDriver(TEXT_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>TEXT_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static TextDriver getTextDriver(ComponentOperator operator) {
	return((TextDriver)getDriver(TEXT_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>TEXT_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setTextDriver(TextDriver driver) {
	setDriver(TEXT_DRIVER_ID, driver);
    }
    /**
     * Returns <code>KEY_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static KeyDriver getKeyDriver(Class operatorClass) {
	return((KeyDriver)getDriver(KEY_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>KEY_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static KeyDriver getKeyDriver(ComponentOperator operator) {
	return((KeyDriver)getDriver(KEY_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>KEY_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setKeyDriver(KeyDriver driver) {
	setDriver(KEY_DRIVER_ID, driver);
    }
    /**
     * Returns <code>MOUSE_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static MouseDriver getMouseDriver(Class operatorClass) {
	return((MouseDriver)getDriver(MOUSE_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>MOUSE_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static MouseDriver getMouseDriver(ComponentOperator operator) {
	return((MouseDriver)getDriver(MOUSE_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>MOUSE_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setMouseDriver(MouseDriver driver) {
	setDriver(MOUSE_DRIVER_ID, driver);
    }
    /**
     * Returns <code>SCROLL_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static ScrollDriver getScrollDriver(Class operatorClass) {
	return((ScrollDriver)getDriver(SCROLL_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>SCROLL_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static ScrollDriver getScrollDriver(ComponentOperator operator) {
	return((ScrollDriver)getDriver(SCROLL_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>SCROLL_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setScrollDriver(ScrollDriver driver) {
	setDriver(SCROLL_DRIVER_ID, driver);
    }
    /**
     * Returns <code>BUTTON_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static ButtonDriver getButtonDriver(Class operatorClass) {
	return((ButtonDriver)getDriver(BUTTON_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>BUTTON_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static ButtonDriver getButtonDriver(ComponentOperator operator) {
	return((ButtonDriver)getDriver(BUTTON_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>BUTTON_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setButtonDriver(ButtonDriver driver) {
	setDriver(BUTTON_DRIVER_ID, driver);
    }
    /**
     * Returns <code>LIST_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static ListDriver getListDriver(Class operatorClass) {
	return((ListDriver)getDriver(LIST_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>LIST_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static ListDriver getListDriver(ComponentOperator operator) {
	return((ListDriver)getDriver(LIST_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>LIST_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setListDriver(ListDriver driver) {
	setDriver(LIST_DRIVER_ID, driver);
    }
    /**
     * Returns <code>MULTISELLIST_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static MultiSelListDriver getMultiSelListDriver(Class operatorClass) {
	return((MultiSelListDriver)getDriver(MULTISELLIST_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>MULTISELLIST_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static MultiSelListDriver getMultiSelListDriver(ComponentOperator operator) {
	return((MultiSelListDriver)getDriver(MULTISELLIST_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>MULTISELLIST_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setMultiSelListDriver(MultiSelListDriver driver) {
	setDriver(MULTISELLIST_DRIVER_ID, driver);
    }
    /**
     * Returns <code>ORDEREDLIST_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static OrderedListDriver getOrderedListDriver(Class operatorClass) {
	return((OrderedListDriver)getDriver(ORDEREDLIST_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>ORDEREDLIST_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static OrderedListDriver getOrderedListDriver(ComponentOperator operator) {
	return((OrderedListDriver)getDriver(ORDEREDLIST_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>ORDEREDLIST_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setOrderedListDriver(OrderedListDriver driver) {
	setDriver(ORDEREDLIST_DRIVER_ID, driver);
    }
    /**
     * Returns <code>TABLE_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static TableDriver getTableDriver(Class operatorClass) {
	return((TableDriver)getDriver(TABLE_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>TABLE_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static TableDriver getTableDriver(ComponentOperator operator) {
	return((TableDriver)getDriver(TABLE_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>TABLE_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setTableDriver(TableDriver driver) {
	setDriver(TABLE_DRIVER_ID, driver);
    }
    /**
     * Returns <code>WINDOW_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static WindowDriver getWindowDriver(Class operatorClass) {
	return((WindowDriver)getDriver(WINDOW_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>WINDOW_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static WindowDriver getWindowDriver(ComponentOperator operator) {
	return((WindowDriver)getDriver(WINDOW_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>WINDOW_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setWindowDriver(WindowDriver driver) {
	setDriver(WINDOW_DRIVER_ID, driver);
    }
    /**
     * Returns <code>FRAME_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static FrameDriver getFrameDriver(Class operatorClass) {
	return((FrameDriver)getDriver(FRAME_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>FRAME_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static FrameDriver getFrameDriver(ComponentOperator operator) {
	return((FrameDriver)getDriver(FRAME_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>FRAME_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setFrameDriver(FrameDriver driver) {
	setDriver(FRAME_DRIVER_ID, driver);
    }
    /**
     * Returns <code>FOCUS_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static FocusDriver getFocusDriver(Class operatorClass) {
	return((FocusDriver)getDriver(FOCUS_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>FOCUS_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static FocusDriver getFocusDriver(ComponentOperator operator) {
	return((FocusDriver)getDriver(FOCUS_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>FOCUS_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setFocusDriver(FocusDriver driver) {
	setDriver(FOCUS_DRIVER_ID, driver);
    }
    /**
     * Returns <code>MENU_DRIVER_ID</code> driver
     * @param operatorClass Class to find driver for.
     */
    public static MenuDriver getMenuDriver(Class operatorClass) {
	return((MenuDriver)getDriver(MENU_DRIVER_ID, operatorClass));
    }
    /**
     * Returns <code>MENU_DRIVER_ID</code> driver
     * @param operator Operator to find driver for.
     */
    public static MenuDriver getMenuDriver(ComponentOperator operator) {
	return((MenuDriver)getDriver(MENU_DRIVER_ID, operator.getClass()));
    }
    /**
     * Defines <code>MENU_DRIVER_ID</code> driver
     * @param driver
     */
    public static void setMenuDriver(MenuDriver driver) {
	setDriver(MENU_DRIVER_ID, driver);
    }

    //creates driver id
    private static String makeID(String id, Class operatorClass) {
	return(id + "." + operatorClass.getName());
    }

    //returns a driver
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
