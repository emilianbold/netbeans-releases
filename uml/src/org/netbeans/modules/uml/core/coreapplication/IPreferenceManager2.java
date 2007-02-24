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


package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import java.util.Vector;

public interface IPreferenceManager2
{
	/**
	 * Returns whether the product is run in non-interactive testing mode.
	 * @return <code>true</code> if the product is in non-interactive testing
	 *          mode.
	 */
	public boolean isBatchTestMode();

	/**
	 * Sets whether the product is run in non-interactive testing mode.
	 * @param btm <code>true</code> if the product is in non-interactive testing
	 *              mode.
	 */
	public void setBatchTestMode(boolean btm);

	/**
	 * Register the passed-in file as the default preference file
	*/
	public long registerFile( String fileName );

	/**
	 * Remove the passed in file as the default file
	*/
	public long unregisterFile( String fileName );

	/**
	 * Validates the file for existence, read/write access, and validity against a preference DTD.
	*/
	public boolean validateFile( String fileName );

	/**
	 * property PropertyDefinitions
	*/
	public Vector<IPropertyDefinition> getPropertyDefinitions();

	/**
	 * property PropertyDefinitions
	*/
	public void setPropertyDefinitions( IPropertyDefinition[] value );

	/**
	 * property PropertyElements
	*/
	public IPropertyElement[] getPropertyElements();

	/**
	 * property PropertyElements
	*/
	public void setPropertyElements( IPropertyElement[] value );

	/**
	 * Use the passed in file and build the preference definitions and preference elements to be used by the Describe application.
	*/
	public long buildPreferences( String fileName );

	/**
	 * method ReloadPreferences
	*/
	public long reloadPreferences();

	/**
	 * Shortcut method to add a boolean preference to both the definitions and the elements.
	*/
	public long addBooleanPreference( IPreferenceObject prefObj );

	/**
	 * Remove the preference that represents the passed-in property element.  This will remove it from the preference managers array as well as the XML file that it is in.
	*/
	public long removePreference( IPropertyElement pEle );

	/**
	 * Register the passed-in file as an additional preference file using the passed-in key as its lookup value.
	*/
	public long registerFile( String Key, String fileName );

	/**
	 * Remove the passed in file from the map by its key.
	*/
	public long unregisterFile( String Key, String fileName );

	/**
	 * Shortcut method to add a list preference to both the definitions and the elements.
	*/
	public long addListPreference( IPreferenceObject prefObj );

	/**
	 * Shortcut method to add a combo preference to both the definitions and the elements.
	*/
	public long addComboPreference( IPreferenceObject prefObj );

	/**
	 * Shortcut method to add a edit preference to both the definitions and the elements.
	*/
	public long addEditPreference( IPreferenceObject prefObj );

	/**
	 * Get the value of the preference with the given fully-qualified path.
	 */
	public String getPreferenceValue( String path );
    
	/**
	 * Get the value of the passed-in preference name.  This routine searches in the default preference structure.  For example, DefaultFilter will return PSK_DATA.
	*/
	public String getPreferenceValue( String path, String prefName );

	/**
	 * method GetPreferenceValue2
	*/
	public String getPreferenceValue( String Key, String path, String prefName );

	/**
	 * The preference manager has been told to save its information.  It will loop through its definitions and its elements, saving them if necessary.
	*/
	public long save();

	/**
	 * Set the value of the passed in preference name.  This routine searches in the default preference structure.
	*/
	public long setPreferenceValue( String path, String prefName, String pVal );

	/**
	 * Set the value of the passed in preference name.  This routine searches in the key preference structure.
	*/
	public long setPreferenceValue( String Key, String path, String prefName, String pVal );

	/**
	 * Set the value of the passed in preference.
	*/
	public long setPreferenceValue( IPropertyElement pEle, String pVal );

	/**
	 * Retrieve the actual preference element that is found under the default top level element and is at the proper sub level that matches the passed in path and pref name.
	*/
	public IPropertyElement getPreferenceElement( String path, String prefName );

	/**
	 * Retrieve the actual preference element that is found under the key top level element and is at the proper sub level that matches the passed in path and pref name.
	*/
	public IPropertyElement getPreferenceElement( String Key, String path, String prefName );

	/**
	 * Retrieve the actual preference def that is found under the default top level def and is at the proper sub level that matches the passed in path and pref name.
	*/
	public IPropertyDefinition getPreferenceDefinition( String path, String prefName );

	/**
	 * Retrieve the actual preference def that is found under the key top level def and is at the proper sub level that matches the passed in path and pref name.
	*/
	public IPropertyDefinition getPreferenceDefinition( String Key, String path, String prefName );

	/**
	 * Get the translated value of the passed in preference name.  
	*/
	public String getTranslatedPreferenceValue( String path, String prefName );

	/**
	 * Get the translated value of the passed in preference name.
	*/
	public String getTranslatedPreferenceValue( String Key, String path, String prefName );

	/**
	 * Determines if a particular preference has a particular value, looking in Defaults
	*/
	public boolean matches( String path, String prefName, String prefValue );

	/**
	 * Determines if a particular preference has a particular value
	*/
	public boolean matches( String Key, String path, String prefName, String prefValue );

	/**
	 * Restore the preferences to their default values
	*/
	public long restore();

	/**
	 * This method is used to determine what the default font of the grid should be for the application
	*/
	public String getDefaultFont();

	/**
	 * This method is used to determine what the default font of the grid should be for the application
	*/
	public String getDefaultDocFont();

	/**
	 * Restore the preferences to their default values upon install
	*/
	public long restoreForInstall();

	/**
	 * Finds a preference of name sName under pEle.  The found element returned is the first sName found in that hive - at any child level, not just immediate children
	*/
	public IPropertyElement findElement( IPropertyElement pEle, String sName );

	public boolean isEditable(IPropertyElement pEle);

	public boolean getShowAliasedNames();
	public void setShowAliasedNames(boolean newVal);
	
	public void toggleShowAliasedNames();
	public String getPreferenceValueWithFullPath(String fullPathToPreference);
}
