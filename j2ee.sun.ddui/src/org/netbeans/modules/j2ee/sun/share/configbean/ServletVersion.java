/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ServletVersion.java
 *
 * Created on February 25, 2004, 2:36 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

/**
 *  Enumerated types for Servlet Version
 *
 * @author Peter Williams
 */
public final class ServletVersion extends J2EEBaseVersion {
	
	/** Represents servlet version 2.3
	 */
	public static final ServletVersion SERVLET_2_3 = new ServletVersion(
		"2.3", 2300,	// NOI18N
		DTDRegistryLink.SUN_WEBAPP_230_DTD_PUBLIC_ID, 
		DTDRegistryLink.SUN_WEBAPP_230_DTD_SYSTEM_ID);
	
	/** Represents servlet version 2.4
	 */
	public static final ServletVersion SERVLET_2_4 = new ServletVersion(
		"2.4", 2400,	// NOI18N
		DTDRegistryLink.SUN_WEBAPP_240_DTD_PUBLIC_ID, 
		DTDRegistryLink.SUN_WEBAPP_240_DTD_SYSTEM_ID);
	
	/** -----------------------------------------------------------------------
	 *  Implementation
	 */
	
	/** Creates a new instance of ServletVersion 
	 */
	private ServletVersion(String version, int nv, String pubId, String sysId) {
		super(version, nv, pubId, sysId);
	}

	/** Comparator implementation that works only on ServletVersion objects
	 *
	 *  @param obj ServletVersion to compare with.
	 *  @return -1, 0, or 1 if this version is less than, equal to, or greater
	 *     than the version passed in as an argument.
	 *  @throws ClassCastException if obj is not a ServletVersion object.
	 */
	public int compareTo(Object obj) {
		ServletVersion target = (ServletVersion) obj;
		return numericCompare(target);
	}
	
	public static ServletVersion getServletVersion(String version) {
		ServletVersion result = null;
		
		if(SERVLET_2_3.toString().equals(version)) {
			result = SERVLET_2_3;
		} else if(SERVLET_2_4.toString().equals(version)) {
			result = SERVLET_2_4;
		}
		
		return result;
	}
}
