/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

public class Version implements java.io.Serializable {
    public final static int MAJVER = 4;
    public final static int MINVER = 1;
    public final static int PTCVER = 0;

	private int major;
	private int minor;
	private int patch;
	
	public Version(int major, int minor, int patch) {
	    this.major = major;
	    this.minor = minor;
	    this.patch = patch;
	}
	
	public int getMajor() {
	    return this.major;
	}
	
	public int getMinor() {
	    return this.minor;
	}
	
	public int getPatch() {
	    return this.patch;
	}

    /**
     * Returns the current version of the runtime system.
     */
	public static String getVersion() {
	    return "version " + MAJVER + "." + MINVER + "." + PTCVER;
	}
}
