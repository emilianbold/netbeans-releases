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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

public class Version implements java.io.Serializable {
    public final static int MAJVER = 5;
    public final static int MINVER = 0;
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
