/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
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
    private static double timeoutsScale = -1;

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
        long timeout;
        if(contains(name) && timeouts.get(name) != null) {
            timeout = ((Long) timeouts.get(name)).longValue();
            timeout =  (long) ((double) timeout * getTimeoutsScale());
        } else if(this != defaults) {
            timeout = getDefault(name);
        } else {
            timeout = -1;
        }
        return timeout;
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
    
    /**
     * Get timeouts scale.
     * Uses jemmy.timeouts.scale system property to get the value.
     * @return timeouts scale or 1 if the property is not set.
     */
    public static double getTimeoutsScale() {
        if (timeoutsScale == -1) {
            String s = System.getProperty("jemmy.timeouts.scale", "1");
            try {
                timeoutsScale = Double.parseDouble(s);
            } catch (NumberFormatException e){
                timeoutsScale = 1;
            }
        }
        if (timeoutsScale < 0) {
            timeoutsScale = 1;
        }
        return timeoutsScale;
    }

    /**
     * This method is designed to be used by unit test for testing purpose.
     */
    static void resetTimeoutScale() {
        timeoutsScale = -1;
    }
}
