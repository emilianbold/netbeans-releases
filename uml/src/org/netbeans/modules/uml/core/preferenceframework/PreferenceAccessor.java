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

package org.netbeans.modules.uml.core.preferenceframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * @author sumitabhk
 *
 */
public class PreferenceAccessor implements IPreferenceAccessor{

	private static PreferenceAccessor m_Instance = null;
	
	private PreferenceAccessor()
	{
	}
	
	public static PreferenceAccessor instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new PreferenceAccessor();
		}
		return m_Instance;
	}

	/**
	 * Retrieves the default name to use for a new project from the preference file.
	 *
	 * @param name[out]	The default name
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultProjectName() {
		String value = getTranslatedPreferenceValue("", "NewProject", "DefaultProjectName");
		return value;
	}

	/**
	 * Gets the IDType preference from the preference file.  This is the
	 * type of id to create when generating a unique id.
	 *
	 * @param type[out]	The id type found in the file
	 *
	 * @return HRESULT
	 *
	 */
	public int getIDType() {
		int type = 0;
		String value = getTranslatedPreferenceValue("", "NewProject", "IDType");
		if (value.equals("PSK_NORMAL"))
			type = 0;
		return type;
	}

	/**
	 * Gets the default element name from the preferences file.  This is the
	 * name to be used when a new element is created.
	 * 
	 *
	 * @param *name[out] The name to use for unnamed elements
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultElementName() {
		String value = getTranslatedPreferenceValue("", "NewProject", "DefaultElementName");
		return value;
	}

	/**
	 * Gets a flag from the preferences file which tells us whether or not to create a classifier
	 * if Describe cannot resolve it to the model or any of its imported packages.
	 * 
	 *
	 * @param val[out]	Whether or not to create
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getUnknownClassifierCreate() {
		boolean retVal = false;
		String value = getPreferenceValue("", "NewProject|UnknownClassifier", "UnknownClassifierCreate");
		if (value.equals("PSK_YES"))
			retVal = true;
		return retVal;
	}

	/**
	 * When Describe has been told to create a classifier if it cannot 
	 * be resolved in the model or any imported packages, this is the 
	 * type of element to create.  This is retrieved from the preferences file.
	 *
	 * @param val[out]	The type of element to create
	 *
	 * @return HRESULT
	 *
	 */
	public String getUnknownClassifierType() {
		String value = getTranslatedPreferenceValue("", "NewProject|UnknownClassifier", "UnknownClassifierType");
		return value;
	}

	/**
	 * Asks the preference manager to read the preference file and get the asked
	 * for information and then translate the value from its coded string into its real value.
	 * 
	 *
	 * @param key[in]	"key" preference is found under, if blank, look in "Default"
	 * @param path[in]	Path to the preference (if preference is nested, this is the "|" delimited path
	 * @param name[in]	Name of preference
	 * @param pVal[out]	Preference value
	 *
	 * @return HRESULT
	 *
	 */
	private String getTranslatedPreferenceValue(String key, String path, String name) {
		String value = "";
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IPreferenceManager2 pMan = prod.getPreferenceManager();
			if (pMan != null)
			{
				if (key.length() > 0)
				{
					value = pMan.getTranslatedPreferenceValue(key, path, name);
				}
				else
				{
					value = pMan.getTranslatedPreferenceValue(path, name);
				}
			}
		}
		return value;
	}

	/**
	 * The default mode of a new Describe session found in the preferences file.
	 *
	 * @param *val[out]	The mode
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultMode() {
		String value = getPreferenceValue("", "NewProject", "DefaultMode");
		return value;
	}

	/**
	 * The default language for a new Describe session based on the mode in the preferences file.
	 *
	 * @param mode[in]	The mode
	 * @param val[out]	The default language
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultLanguage(String mode) {
		String str = "Modes|";
		str += mode;
		String value = getPreferenceValue("", str, "Language");
		return value;
	}

	/**
	 *
	 * Based on a particular language and behavior type, this 
	 * retrieves the behavior value in the preferences file.
	 *
	 * @param lang[in]		The language
	 * @param behavior[in]	The behavior type
	 * @param *val[out]		The value of the behavior
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultRoundTripBehavior(String lang, String behavior) {
		// String str = "RoundTrip|";
		String str = "RoundTrip|";
		str += lang;
		String value = getPreferenceValue("", str, behavior);
		return value;
	}

	/**
	 * Retrieves the default customization file to be used for the property editor specified 
	 * in the preferences file.
	 * 
	 *
	 * @param val[out] The file
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultEditorCustomizationFile() {
		String value = getPreferenceValue("", "PropertyEditor", "CustomizationFile");
		return value;
	}

	/**
	 * Retrieves the default filter from the preferences file to be used for the property editor.
	 *
	 * @param *val[out] The default filter
	 *
	 * @return HRESULT
	 *
	 */
	public String getDefaultEditorFilter() {
		String value = getPreferenceValue("", "PropertyEditor", "DefaultFilter");
		return value;
	}

	/**
	 * Retrieves the max number to select from the preference file for the property editor.
	 *
	 * @param *val[out]	The max number for selection
	 *
	 * @return HRESULT
	 *
	 */
	public int getDefaultEditorSelect() {
		int val = 10;
		String value = getPreferenceValue("", "PropertyEditor", "MaxSelect");

		// cvc - CR 6293191
		// if prefs are not read in properly, this value will be an 
		//  empty string and throw a NumberFormatException, so need
		//  to have a default in case the config default isn't available
		if (value == null || value.equals(""))
			value = "2";
		
		if (value.length() > 0)
		{
			val = Integer.valueOf(value).intValue();
		}
		return val;
	}

	/**
	 *	Retrieves a value for a particular expansion variable in the preferences file.
	 *
	 * @param name[in]	The variable to find
	 * @param val[in]	The value
	 *
	 * @return HRESULT
	 *
	 */
	public String getExpansionVariable(String name) {
		String value = getPreferenceValue("", "ExpansionVariables", name);
		return value;
	}

	/**
	 * Asks the preference manager to read the preference file and get the asked
	 * for information.
	 * 
	 *
	 * @param key[in]	"key" preference is found under, if blank, look in "Default"
	 * @param path[in]	Path to the preference (if preference is nested, this is the "|" delimited path
	 * @param name[in]	Name of preference
	 * @param pVal[out]	Preference value
	 *
	 * @return HRESULT
	 *
	 */
	private String getPreferenceValue(String key, String path, String name) {
		String value = "";
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IPreferenceManager2 prefMan = prod.getPreferenceManager();
			if (prefMan != null)
			{
				if (key.length() > 0)
				{
					value = prefMan.getPreferenceValue(key, path, name);
				}
				else
				{
					value = prefMan.getPreferenceValue(path, name);
				}
			}
		}
		return value;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * the font name value in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		The font name for this category
	 *
	 * @return HRESULT
	 */
	public String getFontName(String category) {
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "FaceName");
		return value;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * the font size value in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		The font size for this category
	 *
	 * @return HRESULT
	 */
	public String getFontSize(String category) {
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "Height");
		
		// cvc - CR 6293191
		// if prefs are not read in properly, this value will be an 
		//  empty string and throw a NumberFormatException, so need
		//  to have a default in case the config default isn't available
		if (value == null || value.equals(""))
			value = "11";

		return value;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * whether the font is bold or not in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		Whether or not the font is bold for this category
	 *
	 * @return HRESULT
	 */
	public boolean getFontBold(String category) {
		boolean val = false;
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "Weight");
		
		// cvc - CR 6293191
		// if prefs are not read in properly, this value will be an 
		//  empty string and throw a NumberFormatException, so need
		//  to have a default in case the config default isn't available
		if (value == null || value.equals(""))
			value = "400";

		if (Integer.parseInt(value) > 400)
			val = true;
		return val;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * whether the font is italic or not in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		Whether or not the font is italic for this category
	 *
	 * @return HRESULT
	 */
	public boolean getFontItalic(String category) {
		boolean val = false;
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "Italic");
		if (!value.equals("0"))
			val = true;
		return val;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * whether the font is strikeout or not in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		Whether or not the font is strikeout for this category
	 *
	 * @return HRESULT
	 */
	public boolean getFontStrikeout(String category) {
		boolean val = false;
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "Strikeout");
		if (!value.equals("0"))
			val = true;
		return val;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * whether the font is underlined or not in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		Whether or not the font is underlined for this category
	 *
	 * @return HRESULT
	 */
	public boolean getFontUnderline(String category) {
		boolean val = false;
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "Underline");
		if (!value.equals("0"))
			val = true;
		return val;
	}

	/**
	 * Based on a particular font category (DefaultClassFont, DefaultFont, etc), this retrieves 
	 * the font color value in the preferences file.
	 *
	 * @param category[in]	The font category
	 * @param *val[out]		The font color for this category
	 *
	 * @return HRESULT
	 */
	public String getFontColor(String category) {
		String str = "Presentation|";
		str += category;
		String value = getPreferenceValue("", str, "Color");
		return value;
	}

	/**
	 * Asks the preference manager to read the preference file and set the given
	 * preference.
	 * 
	 *
	 * @param key[in]	"key" preference is found under, if blank, look in "Default"
	 * @param path[in]	Path to the preference (if preference is nested, this is the "|" delimited path
	 * @param name[in]	Name of preference
	 * @param pVal[out]	Preference value
	 *
	 * @return HRESULT
	 *
	 */
	public void setPreferenceValue(String key, String path, String name, String value)
	{
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IPreferenceManager2 prefMan = prod.getPreferenceManager();
			if (prefMan != null)
			{
				if (key.length() > 0)
				{
					prefMan.setPreferenceValue(key, path, name, value);
				}
				else
				{
					prefMan.setPreferenceValue(path, name, value);
				}
			}
		}
	}

}


