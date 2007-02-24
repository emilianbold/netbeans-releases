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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.metamodel.core.foundation;

public interface IConfigManager
{
	/**
	 * Retrieves the location of the bin directory.
	*/
	public String getHomeLocation();

	/**
	 * Retrieves the location of the license file directory.
	*/
	public String getLicenseLocation();

	/**
	 * Retrieves the location of the docs directory.
	*/
	public String getDocsLocation();

	/**
	 * Retrieves the absolute path to the Presentation types etc file.
	*/
	public String getPresentationTypesLocation();

	/**
	 * Retrieves the absolute path to the Presentation types etc file.
	*/
	public void setPresentationTypesLocation( String value );

	/**
	 * Retrieves the absolute path to the stereotype icons etc file.
	*/
	public String getStereotypeIconsLocation();

	/**
	 * Retrieves the absolute path to the stereotype icons etc file.
	*/
	public void setStereotypeIconsLocation( String value );

	/**
	 * Retrieves the absolute path to the Event framework .etc file.
	*/
	public String getEventFrameworkLocation();

	/**
	 * Retrieves the absolute path to the Event framework .etc file.
	*/
	public void setEventFrameworkLocation( String value );

	/**
	 * Retrieves the absolute path to the DTD used for all new projects.
	*/
	public String getDTDLocation();

	/**
	 * Retrieves the absolute path to the DTD used for all new projects.
	*/
	public void setDTDLocation( String value );

	/**
	 * Retrieves an identifying string found in the EssentialConfig.etc file.
	*/
	public String getID( String hive, String name );

	/**
	 * Retrieves an identifying string found in the EssentialConfig.etc file.
	*/
	public String getIDs( String hive, String name, StringBuffer stdID );

	/**
	 * Retrieves the absolute location of the default preferences file.
	*/
	public String getPreferenceLocation();

	/**
	 * Retrieves the absolute location of the default preferences file.
	*/
	public void setPreferenceLocation( String value );

	/**
	 * Retrieves the location of the config directory.
	*/
	public String getDefaultConfigLocation();
	
	public void setDefaultResourcesLocation(String newVal);

	public String getDefaultResourcesLocation();
	
	public void setOverriddenResourcesLocation(String newVal);
	
	public String getOverriddenResourcesLocation();
}
