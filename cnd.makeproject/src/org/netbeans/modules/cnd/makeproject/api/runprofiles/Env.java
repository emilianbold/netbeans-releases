/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.runprofiles;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Generic manipulation and management of unix-like environment variables.
 * Allows easy setup of environment variables in order to be passed to
 * Runtime.exec().
 */

public class Env {
    private Vector environ;

    public Env() {
	environ = new Vector();
    }

    public void removeAll() {
	environ = new Vector();
    }

    /**
     * Remove the entry with the given name
     */
    public void removeByName(String name) {
	if (name == null)
	    return;

	String[] entry = getenvAsPair(name);
	environ.removeElement(entry);
    }

    /**
     * Returns the whole entry in the form of <code>name=value</code>.
     */
    public String getenvEntry(String name) {
	String value = getenv(name);
	if (value != null)
	    return name + "=" + value; // NOI18N
	else
	    return null;
    } 

    /**
     * Returns the entry in the form of String[2]
     */
    public String[] getenvAsPair(String name) {
	for (Enumeration e = environ.elements() ; e.hasMoreElements() ;) {
	    String[] nameValue  = (String[])e.nextElement();
	    if (nameValue[0].equals(name)) {
		return nameValue;
	    }
	}
	return null;
    } 

    /**
     * Returns just the value, like getenv(3).
     */
    public String getenv(String name) {
	for (Enumeration e = environ.elements() ; e.hasMoreElements() ;) {
	    String[] nameValue  = (String[])e.nextElement();
	    if (nameValue[0].equals(name)) {
		return nameValue[1];
	    }
	}
	return null;
    }

    public String toString() {
	String[] envStrings = getenv();
	boolean addSep = false;
	StringBuilder envString = new StringBuilder();
	for (int i = 0; i < envStrings.length; i++) {
	    if (addSep)
	    envString.append(";"); // NOI18N
	    envString.append(envStrings[i]);
	    addSep = true;
	}
	return envString.toString();
    }
    
    public String encode() {
        return toString();
    }
    
    public void decode(String envlist) {
        StringTokenizer tokenizer = new StringTokenizer(envlist, " ;"); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            putenv(tokenizer.nextToken());
        }
    }
            

    public boolean equals(Object o) {
	boolean eq = false;
	if (o instanceof Env) {
	    Env env = (Env)o;
	    eq = toString().equals(env.toString());
	}
	return eq;
    }

    /**
     * Takes <code>name=value</code> format.
     */
    public void putenv(String entry) {
	int equalx = entry.indexOf('='); // NOI18N
	if (equalx == -1) {
	    System.err.println("Env.putenv(): odd entry '" + entry + "'"); // NOI18N
	    return;
	}
	String name = entry.substring(0, equalx);
	String value = entry.substring(equalx+1);
	putenv(name, value);
    } 

    /**
     * Sets or creates a new environment variable
     */
    public void putenv(String name, String value) {
	String[] entry = getenvAsPair(name);
	if (entry != null)
	    entry[1] = value;
	else
	    environ.add(new String[] {name, value});
    } 

    /**
     * Convert the internal representation to an array of Strings
     * Suitable for passing to Runtime.exec.
     */
    public String[] getenv() {
	String array[] = new String[environ.size()];

	int index = 0;
	for (Enumeration e = environ.elements() ; e.hasMoreElements() ;) {
	    String[] nameValue  = (String[])e.nextElement();
	    array[index++] = nameValue[0] + "=" + nameValue[1]; // NOI18N
	}
	return array;
    } 

    /**
     * Converts the internal representation to an array of variable/value pairs
     */
    public String[][] getenvAsPairs() {
	String array[][] = new String[environ.size()][2];

	int index = 0;
	for (Enumeration e = environ.elements() ; e.hasMoreElements() ;) {
	    String[] nameValue  = (String[])e.nextElement();
	    array[index++] = nameValue;
	}
	return array;
    } 

    public void assign(Env env) {
	removeAll();
	String[][] pairs = env.getenvAsPairs();
	for (int i = 0; i < pairs.length; i++)
	    putenv(pairs[i][0], pairs[i][1]);
    }

    /**
     * Clone the environment creating an identical copy.
     */
    public Env cloneEnv() {
	return (Env)clone();
    }

    public Object clone() {
	Env clone = new Env();
	String[][] pairs = getenvAsPairs();
	for (int i = 0; i < pairs.length; i++)
	    clone.putenv(pairs[i][0], pairs[i][1]);
	return clone;
    }
}
