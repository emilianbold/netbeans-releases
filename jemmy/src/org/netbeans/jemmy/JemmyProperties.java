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

package org.netbeans.jemmy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileInputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

import org.netbeans.jemmy.drivers.InputDriverInstaller;

/**
 * 
 * Keeps default Jemmy properties
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */

public class JemmyProperties {

    /**
     * The event queue model mask.
     * @see #getCurrentDispatchingModel()
     * @see #setCurrentDispatchingModel(int)
     */
    public static int QUEUE_MODEL_MASK = 1;

    /**
     * The robot using model mask.
     * @see #getCurrentDispatchingModel()
     * @see #setCurrentDispatchingModel(int)
     */
    public static int ROBOT_MODEL_MASK = 2;

    private static final int DEFAULT_DRAG_AND_DROP_STEP_LENGTH = 100;
    private static Stack propStack = null;

    Hashtable properties;

    protected JemmyProperties() {
	super();
	properties = new Hashtable();
	setProperty("timeouts", new Timeouts());
	setProperty("output", new TestOut());
	setProperty("resources", new BundleManager());
	setProperty("binding.map", new DefaultCharBindingMap());
	setProperty("dispatching.model", new Integer(getDefaultDispatchingModel()));
	setProperty("drag_and_drop.step_length", new Integer(DEFAULT_DRAG_AND_DROP_STEP_LENGTH));
    }

    /**
     * Returns major version (like 1.0)
     */
    public static String getMajorVersion() {
        return(extractValue(getProperties().getClass().
			    getClassLoader().getResourceAsStream("org/netbeans/jemmy/version_info"),
			    "Jemmy-MajorVersion"));
    }

    /**
     * Returns minor version (like 1)
     */
    public static String getMinorVersion() {
        return(extractValue(getProperties().getClass().
			    getClassLoader().getResourceAsStream("org/netbeans/jemmy/version_info"),
			    "Jemmy-MinorVersion"));
    }

    /**
     * Returns build (like 011231 (yymmdd))
     */
    public static String getBuild() {
        return(extractValue(getProperties().getClass().
			    getClassLoader().getResourceAsStream("org/netbeans/jemmy/version_info"),
			    "Jemmy-Build"));
    }

    /**
     * Returns full version string (like 1.0.1-011231)
     */
    public static String getVersion() {
	return(getMajorVersion() + "." +
	       getMinorVersion());
    }

    /**
     * Creates a copy of the current JemmyProperties object
     * and pushes it into the properties stack. 
     * @return New current properties.
     */
    public static JemmyProperties push() {
	return((JemmyProperties)propStack.push(getProperties().cloneThis()));
    }

    /**
     * Pops last pushed properties from the properties stack. 
     * If stack has just one element, does nothing.
     * @return Poped properties.
     */
    public static JemmyProperties pop() {
	JemmyProperties result = (JemmyProperties)propStack.pop();
	if(propStack.isEmpty()) {
	    propStack.push(result);
	}
	return(result);
    }

    /**
     * Just like getProperties().getProperty(propertyName)
     */
    public static Object getCurrentProperty(String propertyName) {
	return(getProperties().getProperty(propertyName));
    }

    /**
     * Just like getProperties().setProperty(propertyName, propertyValue)
     */
    public static Object setCurrentProperty(String propertyName, Object propertyValue) {
	return(getProperties().setProperty(propertyName, propertyValue));
    }

    public static Object removeCurrentProperty(String propertyName) {
	return(getProperties().removeProperty(propertyName));
    }

    public static String[] getCurrentKeys() {
	return(getProperties().getKeys());
    }

    /**
     * Just like getProperties().getTimeouts()
     */
    public static Timeouts getCurrentTimeouts() {
	return(getProperties().getTimeouts());
    }

    /**
     * Just like getProperties().setTimeouts(to)
     */
    public static Timeouts setCurrentTimeouts(Timeouts to) {
	return(getProperties().setTimeouts(to));
    }

    /**
     * Just like getProperties().getTimeouts().setTimeout(name, newValue)
     */
    public static long setCurrentTimeout(String name, long newValue) {
	return(getProperties().getTimeouts().setTimeout(name, newValue));
    }

    /**
     * Just like getProperties().getTimeouts().getTimeout(name)
     */
    public static long getCurrentTimeout(String name) {
	return(getProperties().getTimeouts().getTimeout(name));
    }

    /**
     * Just like getProperties().getTimeouts().initTimeout(name, newValue)
     */
    public static long initCurrentTimeout(String name, long newValue) {
	return(getProperties().getTimeouts().initTimeout(name, newValue));
    }

    /**
     * Just like getProperties().getOutput()
     */
    public static TestOut getCurrentOutput() {
	return(getProperties().getOutput());
    }

    /**
     * Just like getProperties().setOutput(out)
     */
    public static TestOut setCurrentOutput(TestOut out) {
	return(getProperties().setOutput(out));
    }
   
    /**
     * Just like getProperties().getBundleManager()
     */
    public static BundleManager getCurrentBundleManager() {
	return(getProperties().getBundleManager());
    }

    /**
     * Just like getProperties().setBundleManager(resources)
     */
    public static BundleManager setCurrentBundleManager(BundleManager resources) {
	return(getProperties().setBundleManager(resources));
    }

    /**
     * Just like getProperties().getBundleManager().getResource(key)
     */
    public static String getCurrentResource(String key) {
	return(getProperties().getBundleManager().getResource(key));
    }

    /**
     * Just like getProperties().getBundleManager().getResource(bundleID, key)
     */
    public static String getCurrentResource(String bundleID, String key) {
	return(getProperties().getBundleManager().getResource(bundleID, key));
    }

    /**
     * Just like getProperties().getCharBindingMap()
     */
    public static CharBindingMap getCurrentCharBindingMap() {
	return(getProperties().getCharBindingMap());
    }

    /**
     * Just like getProperties().setCharBindingMap(map)
     */
    public static CharBindingMap setCurrentCharBindingMap(CharBindingMap map) {
	return(getProperties().setCharBindingMap(map));
    }

    /**
     * @return Event dispatching model.
     * @see #getDispatchingModel()
     * @see #setCurrentDispatchingModel(int)
     * @see #QUEUE_MODEL_MASK
     * @see #ROBOT_MODEL_MASK
     */
    public static int getCurrentDispatchingModel() {
	return(getProperties().getDispatchingModel());
    }

    /**
     * Defines event dispatching model.
     * If (model & ROBOT_MODEL_MASK) != 0 java.awt.Robot class
     * is used to reproduce user actions, otherwise actions
     * are reproduced by event posting.
     * If (model & QUEUE_MODEL_MASK) != 0 actions are reproduced through
     * event queue.
     * @param model New dispatching model value.
     * @return Previous dispatching model value.
     * @see #setDispatchingModel(int)
     * @see #getCurrentDispatchingModel()
     * @see #QUEUE_MODEL_MASK
     * @see #ROBOT_MODEL_MASK
     * @see #initDispatchingModel(boolean, boolean)
     * @see #initDispatchingModel()
     */
    public static int setCurrentDispatchingModel(int model) {
	return(getProperties().setDispatchingModel(model));
    }

    /**
     * Returns default event dispatching model.
     * @return QUEUE_MODEL_MASK
     * @see #setCurrentDispatchingModel(int)
     * @see #QUEUE_MODEL_MASK
     * @see #ROBOT_MODEL_MASK
     */
    public static int getDefaultDispatchingModel() {
	return(QUEUE_MODEL_MASK);
    }

    /**
     * @return Pixel count to move mouse during one drag'n'drop step.
     * @see #getDragAndDropStepLength()
     * @see #setCurrentDragAndDropStepLength(int)
     */
    public static int getCurrentDragAndDropStepLength() {
	return(getProperties().getDragAndDropStepLength());
    }

    /**
     * @param model Pixel count to move mouse during one drag'n'drop step.
     * @return Previous value.
     * @see #setDragAndDropStepLength(int)
     * @see #getCurrentDragAndDropStepLength()
     */
    public static int setCurrentDragAndDropStepLength(int model) {
	return(getProperties().setDragAndDropStepLength(model));
    }

    /**
     * Peeks upper JemmyProperties instance from stack.
     */
    public static JemmyProperties getProperties() {
	if(propStack == null) {
	    propStack = new Stack();
	}
	if(propStack.empty()) {
	    propStack.add(new JemmyProperties());
	}
	return((JemmyProperties)propStack.peek());
    }

    /**
     * Prints full version into satndart output.
     */
    public static void main(String[] argv) {
	System.out.println("Jemmy version : " + getVersion());
    }

    /**
     * Method to initialize timeouts and resources.
     * @param prop_file File to get filenames from. <BR>
     * Can contain definition of variables TIMEOUTS_FILE - full path to timeouts file, <BR>
     * RESOURCE_FILE - full path to resource file.
     * @see org.netbeans.jemmy.JemmyProperties#initProperties()
     */
    public void initProperties(String prop_file) {
	try {
	    getOutput().printLine("Loading properties from " + prop_file + " file");
	    Properties props = new Properties();
	    props.load(new FileInputStream(prop_file));
	    if(props.getProperty("TIMEOUTS_FILE") != null &&
	       !props.getProperty("TIMEOUTS_FILE").equals("")) {
		getOutput().printLine("Loading timeouts from " + props.getProperty("TIMEOUTS_FILE") + 
				      " file");
		getTimeouts().loadDefaults(props.getProperty("TIMEOUTS_FILE"));
	    }
	    if(props.getProperty("RESOURCE_FILE") != null &&
	       !props.getProperty("RESOURCE_FILE").equals("")) {
		getOutput().printLine("Loading resources from " + props.getProperty("RESOURCE_FILE") + 
				      " file");
		getBundleManager().loadBundleFromFile(props.getProperty("RESOURCE_FILE"), "");
	    }
	} catch(IOException e) {
	    getOutput().printStackTrace(e);
	}
    }

    /**
     * Method to initialize timeouts and resources. <BR>
     * Uses jemmy.properties system property to find file.
     * @see org.netbeans.jemmy.JemmyProperties#initProperties(String)
     */
    public void initProperties() {
	if(System.getProperty("jemmy.properties") != null && 
	   !System.getProperty("jemmy.properties").equals("")) {
	    initProperties(System.getProperty("jemmy.properties"));
	} else {
	    try {
		getTimeouts().load();
		getBundleManager().load();
	    } catch(IOException e) {
		getOutput().printStackTrace(e);
	    }
	}
    }

    /**
     * Initializes dispatching model.
     * @param queue Notifies that event queue dispatching should be used.
     * @param robot Notifies that robot dispatching should be used.
     */
    public void initDispatchingModel(boolean queue, boolean robot) {
	int model = 0;
	getOutput().print("Reproduce user actions ");
	if(queue) {
	    model = QUEUE_MODEL_MASK;
	    getOutput().printLine("through event queue.");
	} else {
	    model = 0;
	    getOutput().printLine("directly.");
	}
	getOutput().print("Use ");
	if(robot) {
	    model = model | ROBOT_MODEL_MASK;
	    getOutput().print("java.awt.Robot class");
	} else {
	    model = model;
	    getOutput().print("event dispatching");
	}
	getOutput().printLine(" to reproduce user actions");
	setDispatchingModel(model);
    }

    /**
     * Initializes dispatching model.
     * Uses "jemmy.queue_dispatching" and "jemmy.robot_dispatching" system properties
     * to determine what model should be used.
     * Possible values for the both properties: <BR>
     * "off" - switch mode off. <BR>
     * "on" - switch mode on. <BR>
     * "" - use default value.
     * @see #getDefaultDispatchingModel()
     */
    public void initDispatchingModel() {
	boolean qmask = ((getDefaultDispatchingModel() & QUEUE_MODEL_MASK) != 0);
	boolean rmask = ((getDefaultDispatchingModel() & ROBOT_MODEL_MASK) != 0);
	if( System.getProperty("jemmy.queue_dispatching") != null &&
	   !System.getProperty("jemmy.queue_dispatching").equals("")) {
	    qmask = System.getProperty("jemmy.queue_dispatching").equals("on");
	}
	if( System.getProperty("jemmy.robot_dispatching") != null &&
	   !System.getProperty("jemmy.robot_dispatching").equals("")) {
	    rmask = System.getProperty("jemmy.robot_dispatching").equals("on");
	}

	initDispatchingModel(qmask, rmask);
    }

    /**
     * Inits properties and dispatching model from system environment variables.
     * @see #initProperties()
     * @see #initDispatchingModel()
     */
    public void init() {
	initProperties();
	initDispatchingModel();
    }

    /**
     * Returns timeouts.
     */
    public Timeouts getTimeouts() {
	return((Timeouts)getProperty("timeouts"));
    }

    /**
     * Changes timeouts.
     */
    public Timeouts setTimeouts(Timeouts to) {
	return((Timeouts)setProperty("timeouts", to));
    }

    /**
     * Changes a timeouts value.
     * @param name Timeout name
     * @param newValue New timeout value
     */
    public long setTimeout(String name, long newValue) {
	return(getTimeouts().setTimeout(name, newValue));
    }

    /**
     * Returns a timeouts value.
     * @param name Timeout name
     */
    public long getTimeout(String name) {
	return(getTimeouts().getTimeout(name));
    }

    /**
     * Inits a timeouts value.
     * @param name Timeout name
     * @param newValue New timeout value
     */
    public long initTimeout(String name, long newValue) {
	return(getTimeouts().initTimeout(name, newValue));
    }

    /**
     * Returns output.
     */
    public TestOut getOutput() {
	return((TestOut)getProperty("output"));
    }

    /**
     * Changes output.
     */
    public TestOut setOutput(TestOut out) {
	return((TestOut)setProperty("output", out));
    }

    /**
     * Returns bundle manager.
     */
    public BundleManager getBundleManager() {
	return((BundleManager)getProperty("resources"));
    }

    /**
     * Changes bundle manager.
     */
    public BundleManager setBundleManager(BundleManager resources) {
	return((BundleManager)setProperty("resources", resources));
    }

    /**
     * Returns resource value.
     * @param key Resource key.
     */
    public String getResource(String key) {
	return(getBundleManager().getResource(key));
    }

    /**
     * Returns resource value from the specified bundle.
     * @param bundleID Id of a bundle to get resource from.
     * @param key Resource key.
     */
    public String getResource(String bundleID, String key) {
	return(getBundleManager().getResource(bundleID, key));
    }

    /**
     * Returns char binding map.
     */
    public CharBindingMap getCharBindingMap() {
	return((CharBindingMap)getProperty("binding.map"));
    }

    /**
     * Changes char binding map.
     */
    public CharBindingMap setCharBindingMap(CharBindingMap map) {
	return((CharBindingMap)setProperty("binding.map", map));
    }

    /**
     * @return Event dispatching model.
     * @see #getCurrentDispatchingModel()
     * @see #setDispatchingModel(int)
     * @see #QUEUE_MODEL_MASK
     * @see #ROBOT_MODEL_MASK
     */
    public int getDispatchingModel() {
	return(((Integer)getProperty("dispatching.model")).intValue());
    }

    /**
     * @param model New dispatching model value.
     * @return Previous dispatching model value.
     * @see #setCurrentDispatchingModel(int)
     * @see #getDispatchingModel()
     * @see #QUEUE_MODEL_MASK
     * @see #ROBOT_MODEL_MASK
     */
    public int setDispatchingModel(int model) {
	new InputDriverInstaller((model & ROBOT_MODEL_MASK) == 0).install();
	return(((Integer)setProperty("dispatching.model", new Integer(model))).intValue());
    }

    /**
     * @return Pixel count to move mouse during one drag'n'drop step.
     * @see #getCurrentDragAndDropStepLength()
     * @see #setDragAndDropStepLength(int)
     */
    public int getDragAndDropStepLength() {
	return(((Integer)getProperty("drag_and_drop.step_length")).intValue());
    }

    /**
     * @param length Pixel count to move mouse during one drag'n'drop step.
     * @return Previous value.
     * @see #setCurrentDragAndDropStepLength(int)
     * @see #getDragAndDropStepLength()
     */
    public int setDragAndDropStepLength(int length) {
	return(((Integer)setProperty("drag_and_drop.step_length", new Integer(length))).intValue());
    }

    /**
     * Checks if "name" propery currently has a value.
     * @param name Property name. Should by unique.
     * @return true if property was defined.
     * @see #setProperty(String, Object)
     * @see #getProperty(String)
     */
    public boolean contains(String name) {
	return(properties.containsKey(name));
    }

    /**
     * Saves object as a static link to be used by other objects.
     * @param name Property name. Should by unique.
     * @param newValue Property value.
     * @return Previous value of "name" property.
     * @see #setCurrentProperty(String, Object)
     * @see #getProperty(String)
     * @see #contains(String)
     */
    public Object setProperty(String name, Object newValue) {
	Object oldValue = null;
	if(contains(name)) {
	    oldValue = properties.get(name);
	    properties.remove(name);
	}
	properties.put(name, newValue);
	return(oldValue);
    }

    /**
     * @param name Property name. Should by unique.
     * @return Property value stored by setProperty(String, Object) method.
     * @see #getCurrentProperty(String)
     * @see #setProperty(String, Object)
     * @see #contains(String)
     */
    public Object getProperty(String name) {
	if(contains(name)) {
	    return(properties.get(name));
	} else {
	    return(null);
	}
    }

    public Object removeProperty(String name) {
	if(contains(name)) {
	    return(properties.remove(name));
	} else {
	    return(null);
	}
    }

    public String[] getKeys() {
	Enumeration keys = properties.keys();
	String[] result = new String[properties.size()];
	int i = 0;
	while(keys.hasMoreElements()) {
	    result[i] = (String)keys.nextElement();
	}
	return(result);
    }

    private JemmyProperties cloneThis() {
	JemmyProperties result = new JemmyProperties();
	Enumeration keys = properties.keys();
	while(keys.hasMoreElements()) {
	    String elem = (String)keys.nextElement();
	    result.setProperty(elem, getProperty(elem));
	}
	//some should be cloned
 	result.setTimeouts(getTimeouts().cloneThis());
 	result.setBundleManager(getBundleManager().cloneThis());
	return(result);
    }

    private static String extractValue(InputStream stream, String varName) {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	    StringTokenizer token;
	    String nextLine;
	    while((nextLine = reader.readLine()) != null) {
		token = new StringTokenizer(nextLine, ":");
		String nextToken = token.nextToken();
		if(nextToken != null &&
		   nextToken.trim().equals(varName)) {
		    return(token.nextToken().trim());
		}
	    }
	    return("");
	} catch(IOException e) {
	    getCurrentOutput().printStackTrace(e);
	    return("");
	}
    }

}
