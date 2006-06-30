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
