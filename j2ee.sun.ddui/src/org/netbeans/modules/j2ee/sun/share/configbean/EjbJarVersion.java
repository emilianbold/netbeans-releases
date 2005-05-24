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
 * EjbJarVersion.java
 *
 * Created on February 25, 2004, 2:36 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

/**
 *  Enumerated types for EjbJar Version
 *
 * @author Peter Williams
 */
public final class EjbJarVersion extends J2EEBaseVersion {
	
	/** Represents ejbjar version 2.0
	 */
	public static final EjbJarVersion EJBJAR_2_0 = new EjbJarVersion(
		"2.0", 2000,	// NOI18N
		DTDRegistryLink.SUN_EJBJAR_200_DTD_PUBLIC_ID, 
		DTDRegistryLink.SUN_EJBJAR_200_DTD_SYSTEM_ID);
	
	/** Represents ejbjar version 2.1
	 */
	public static final EjbJarVersion EJBJAR_2_1 = new EjbJarVersion(
		"2.1", 2400,	// NOI18N
		DTDRegistryLink.SUN_EJBJAR_210_DTD_PUBLIC_ID, 
		DTDRegistryLink.SUN_EJBJAR_210_DTD_SYSTEM_ID);
	
	/** -----------------------------------------------------------------------
	 *  Implementation
	 */
	
	/** Creates a new instance of EjbJarVersion 
	 */
	private EjbJarVersion(String version, int nv, String pubId, String sysId) {
		super(version, nv, pubId, sysId);
	}

	/** Comparator implementation that works only on EjbJarVersion objects
	 *
	 *  @param obj EjbJarVersion to compare with.
	 *  @return -1, 0, or 1 if this version is less than, equal to, or greater
	 *     than the version passed in as an argument.
	 *  @throws ClassCastException if obj is not a EjbJarVersion object.
	 */
	public int compareTo(Object obj) {
		EjbJarVersion target = (EjbJarVersion) obj;
		return numericCompare(target);
	}
	
	public static EjbJarVersion getEjbJarVersion(String version) {
		EjbJarVersion result = null;
		
		if(EJBJAR_2_0.toString().equals(version)) {
			result = EJBJAR_2_0;
		} else if(EJBJAR_2_1.toString().equals(version)) {
			result = EJBJAR_2_1;
		}
		
		return result;
	}
}
