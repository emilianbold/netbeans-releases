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
 * ApplicationVersion.java
 *
 * Created on February 25, 2004, 2:36 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

/**
 *  Enumerated types for Application Version
 *
 * @author Peter Williams
 */
public final class ApplicationVersion extends J2EEBaseVersion {
	
	/** Represents application version 2.3
	 */
	public static final ApplicationVersion APPLICATION_1_3 = new ApplicationVersion(
		"1.3", 1300,	// NOI18N
		DTDRegistryLink.SUN_APPLICATION_130_DTD_PUBLIC_ID, 
		DTDRegistryLink.SUN_APPLICATION_130_DTD_SYSTEM_ID);
	
	/** Represents application version 2.4
	 */
	public static final ApplicationVersion APPLICATION_1_4 = new ApplicationVersion(
		"1.4", 1400,	// NOI18N
		DTDRegistryLink.SUN_APPLICATION_140_DTD_PUBLIC_ID, 
		DTDRegistryLink.SUN_APPLICATION_140_DTD_SYSTEM_ID);
	
	/** -----------------------------------------------------------------------
	 *  Implementation
	 */
	
	/** Creates a new instance of ApplicationVersion 
	 */
	private ApplicationVersion(String version, int nv, String pubId, String sysId) {
		super(version, nv, pubId, sysId);
	}

	/** Comparator implementation that works only on ApplicationVersion objects
	 *
	 *  @param obj ApplicationVersion to compare with.
	 *  @return -1, 0, or 1 if this version is less than, equal to, or greater
	 *     than the version passed in as an argument.
	 *  @throws ClassCastException if obj is not a ApplicationVersion object.
	 */
	public int compareTo(Object obj) {
		ApplicationVersion target = (ApplicationVersion) obj;
		return numericCompare(target);
	}
	
	public static ApplicationVersion getApplicationVersion(String version) {
		ApplicationVersion result = null;
		
		if(APPLICATION_1_3.toString().equals(version)) {
			result = APPLICATION_1_3;
		} else if(APPLICATION_1_4.toString().equals(version)) {
			result = APPLICATION_1_4;
		}
		
		return result;
	}
}
