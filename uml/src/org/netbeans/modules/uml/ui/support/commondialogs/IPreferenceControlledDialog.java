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


package org.netbeans.modules.uml.ui.support.commondialogs;

public interface IPreferenceControlledDialog extends ISilentDialog
{
	/**
	 * Set/Get whether the preference file should be updated when the PreferenceValue is set.
	*/
	public void setAutoUpdatePreference( boolean value );

	/**
	 * Set/Get whether the preference file should be updated when the PreferenceValue is set.
	*/
	public boolean getAutoUpdatePreference();

	/**
	 * Set/Get the preference key. If no key is specified, Default is assumed.
	*/
	public void setPrefKey( String value );

	/**
	 * Set/Get the preference key. If no key is specified, Default is assumed.
	*/
	public String getPrefKey();

	/**
	 * Set/Get the preference path. The path is the part between the key and the name.
	*/
	public void setPrefPath( String value );

	/**
	 * Set/Get the preference path. The path is the part between the key and the name.
	*/
	public String getPrefPath();

	/**
	 * Set/Get the preference name.
	*/
	public void setPrefName( String value );

	/**
	 * Set/Get the preference name.
	*/
	public String getPrefName();

	/**
	 * Set/Get the preference value from the preference manager. If AutoUpdatePreference is False, setting this property does nothing.
	*/
	public void setPreferenceValue( String value );

	/**
	 * Set/Get the preference value from the preference manager. If AutoUpdatePreference is False, setting this property does nothing.
	*/
	public String getPreferenceValue();

	/**
	 * Set the preference values from the preference manager. If AutoUpdatePreference is False, setting this property does nothing.
	*/
	public long preferenceInformation( String sPreferenceKey, String sPreferencePath, String sPreferenceName, boolean bAutoUpdatePreference );

}
