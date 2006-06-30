/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * Class to store and process a set of timeout values.
 *
 * @see #setDefault(String, long)
 * @see #getDefault(String)
 * @see #setTimeout(String, long)
 * @see #getTimeout(String)
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class Timeouts extends Object{

    private static final long DELTA_TIME = 100;

    private static Timeouts defaults;

    private Hashtable timeouts;

    /**
     * Creates empty Timeouts object.
     */
    public Timeouts() {
	super();
	timeouts = new Hashtable();
	setTimeout("Timeouts.DeltaTimeout", DELTA_TIME);
	try {
	    load();
	} catch(IOException e) {
	}
    }

    /**
     * Stores default timeout value.
     * @param name Timeout name.
     * @param newValue Timeout value.
     * @see #getDefault(String)
     * @see #initDefault(String, long)
     * @see #containsDefault(String)
     */
    public static void setDefault(String name, long newValue) {
	defaults.setTimeout(name, newValue);
    }

    /**
     * Sets default timeout value if it was not set before.
     * @param name Timeout name.
     * @param newValue Timeout value.
     * @see #setDefault(String, long)
     * @see #getDefault(String)
     * @see #containsDefault(String)
     */
    public static void initDefault(String name, long newValue) {
	defaults.initTimeout(name, newValue);
    }

    /**
     * Gets default timeout value.
     * @param name Timeout name.
     * @return Timeout value or -1 if timeout is not defined.
     * @see #setDefault(String, long)
     * @see #initDefault(String, long)
     * @see #containsDefault(String)
     */
    public static long getDefault(String name) {
	return(defaults.getTimeout(name));
    }

    /**
     * Check that default timeout value was defined.
     * @param name Timeout name.
     * @return True if timeout has been defined, false otherwise.
     * @see #setDefault(String, long)
     * @see #getDefault(String)
     * @see #initDefault(String, long)
     */
    public static boolean containsDefault(String name) {
	return(defaults.contains(name));
    }

    static {
	defaults = new Timeouts();
    }

    /**
     * Loads default timeouts values.
     * 
     * @param	stream Stream to load timeouts from.
     * @see	org.netbeans.jemmy.Timeouts#loadDefaults(String)
     * @see	org.netbeans.jemmy.Timeouts#loadDefaults()
     * @exception	IOException
     */
    public void loadDefaults(InputStream stream) 
	throws IOException{
	defaults.load(stream);
    }

    /**
     * Loads default timeouts values from file.
     * 
     * @param	fileName File to load timeouts from.
     * @see	org.netbeans.jemmy.Timeouts#loadDefaults(InputStream)
     * @see	org.netbeans.jemmy.Timeouts#loadDefaults(String)
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void loadDefaults(String fileName) 
	throws FileNotFoundException, IOException {
	defaults.load(fileName);
    }

    /**
     * Loads default timeouts values.
     * Uses jemmy.timeouts system property to get timeouts file.
     * 
     * @see	org.netbeans.jemmy.Timeouts#loadDefaults(InputStream)
     * @see	org.netbeans.jemmy.Timeouts#loadDefaults(String)
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void loadDefaults() 
	throws FileNotFoundException, IOException {
	defaults.load();
    }

    /**
     * Creates Timeout new object by name and getTimeout(name) value.
     * @param	name Timeout name.
     * @return a Timeout instance.
     */
    public Timeout create(String name) {
	return(new Timeout(name, getTimeout(name)));
    }

    /**
     * Create timeout for "Timeouts.DeltaTimeout" name.
     * @return a Timeout instance.
     */
    public Timeout createDelta() {
	return(create("Timeouts.DeltaTimeout"));
    }

    /**
     * Checks if timeout has already been defined in this timeout instance.
     * @param name Timeout name.
     * @return True if timeout has been defined, false otherwise.
     * @see #containsDefault(String)
     */
    public boolean contains(String name) {
	return(timeouts.containsKey(name));
    }

    /**
     * Sets new timeout value.
     * @param name Timeout name.
     * @param newValue Timeout value.
     * @return old timeout value
     * @see #getTimeout
     */
    public long setTimeout(String name, long newValue) {
	long oldValue = -1;
	if(contains(name)) {
	    oldValue = getTimeout(name);
	    timeouts.remove(name);
	}
	timeouts.put(name, new Long(newValue));
	return(oldValue);
    }

    /**
     * Gets timeout value.
     * It timeout was not defined in this instance,
     * returns default timeout value.
     * @param name Timeout name.
     * @return Timeout value.
     * @see #getDefault(String)
     * @see #setTimeout
     */
    public long getTimeout(String name) {
	if(contains(name)) {
            Long value = (Long)timeouts.get(name);
            if(value != null) {
                return(value.longValue());
            }
        }
        if(this != defaults) {
            return(getDefault(name));
        } else {
            return(-1);
        }
    }

    /**
     * Gets "Timeouts.DeltaTimeout" timeout value.
     * @return Timeout value.
     * @see #getDefault(String)
     */
    public long getDeltaTimeout() {
	return(getTimeout("Timeouts.DeltaTimeout"));
    }

    /**
     * Sets timeout value if it was not set before.
     * @param name Timeout name.
     * @param newValue Timeout value.
     * @return old timeout value
     */
    public long initTimeout(String name, long newValue) {
	long result = getTimeout(name);
	if(!contains(name)) {
	    setTimeout(name, newValue);
	}
	return(result);
    }

    /**
     * Creates a copy of the current timeouts set.
     * @return A copy.
     */
    public Timeouts cloneThis() {
	Timeouts t = new Timeouts();
	Enumeration e = timeouts.keys();
	String name = "";
	while(e.hasMoreElements()) {
	    name = (String)e.nextElement();
	    t.setTimeout(name,
			 getTimeout(name));
	}
	return(t);
    }

    /**
     * Sleeps for the "name" timeout value.
     * Can throw InterruptedException if current thread was interrupted.
     * 
     * @param	name Timeout name.
     * @exception	InterruptedException
     */
    public void eSleep(String name) throws InterruptedException{
	if(contains(name) ||
	   defaults.contains(name)) {
	    Thread.currentThread().sleep(getTimeout(name));
	}
    }

    /**
     * Sleeps for the "name" timeout value.
     * Does not throw InterruptedException anyway.
     * @param name Timeout name.
     */
    public void sleep(String name) {
	create(name).sleep();
    }

    /**
     * Prins all defined timeouts.
     * @param pw PrintWriter to print into.
     */
    public void print(PrintWriter pw) {
	Enumeration e = timeouts.keys();
	String name = "";
	while(e.hasMoreElements()) {
	    name = (String)e.nextElement();
	    pw.println(name + " = " + Long.toString(getTimeout(name)));
	}
	pw.println("Default values:");
	e = defaults.timeouts.keys();
	name = "";
	while(e.hasMoreElements()) {
	    name = (String)e.nextElement();
	    if(!contains(name)) {
		pw.println(name + " = " + Long.toString(getDefault(name)));
	    }
	}
    }

    /**
     * Prins all defined timeouts.
     * @param ps PrintStream to print into.
     */
    public void print(PrintStream ps) {
	print(new PrintWriter(ps));
	Timeouts t = new Timeouts();
    }

    /**
     * Loads timeouts values.
     * 
     * @param	stream Stream to load timeouts from.
     * @see	org.netbeans.jemmy.Timeouts#load(String)
     * @see	org.netbeans.jemmy.Timeouts#load()
     * @exception	IOException
     */
    public void load(InputStream stream) 
	throws IOException{
	Properties props = new Properties();
	props.load(stream);
	Enumeration propNames = props.propertyNames();
	long propValue = -1;
	String propName = null;
	while(propNames.hasMoreElements()) {
	    propName = (String)propNames.nextElement();
	    propValue = -1;
	    propValue = (new Long(props.getProperty(propName))).longValue();
	    setTimeout(propName, propValue);
	}
    }

    /**
     * Loads timeouts values from file.
     * 
     * @param	fileName File to load timeouts from.
     * @see	org.netbeans.jemmy.Timeouts#load(InputStream)
     * @see	org.netbeans.jemmy.Timeouts#load(String)
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void load(String fileName) 
	throws FileNotFoundException, IOException {
	load(new FileInputStream(fileName));
    }

    /**
     * Loads timeouts values.
     * Uses jemmy.timeouts system property to get timeouts file.
     * 
     * @see	org.netbeans.jemmy.Timeouts#load(InputStream)
     * @see	org.netbeans.jemmy.Timeouts#load(String)
     * @exception	IOException
     * @exception	FileNotFoundException
     */
    public void load() 
	throws FileNotFoundException, IOException {
	if(System.getProperty("jemmy.timeouts") != null &&
	   !System.getProperty("jemmy.timeouts").equals("")) {
	    load(System.getProperty("jemmy.timeouts"));
	}
    }

    /**
     * Loads debug timeouts values.
     * 
     * @exception	IOException
     */
    public void loadDebugTimeouts() throws IOException {
        load(getClass().getClassLoader().getResourceAsStream("org/netbeans/jemmy/debug.timeouts"));
    }
}
