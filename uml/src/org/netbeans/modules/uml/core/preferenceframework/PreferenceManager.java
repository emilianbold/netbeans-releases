/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.core.preferenceframework;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceObject;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionXML;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementXML;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionXML;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElementManager;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElementXML;
import org.netbeans.modules.uml.ui.support.DispatchHelper;

/**
 * @author sumitabhk
 *
 */
public class PreferenceManager implements IPreferenceManager2{

	private static final int CT_BOOLEAN = 0;
	private static final int CT_LIST = 1;
	private static final int CT_COMBO = 2;
	private static final int CT_EDIT = 3;

	private String m_DefaultFile = null;
	//std::map < CComBSTR, CComBSTR >   m_FileMap;
	private Hashtable < String, String > m_FileMap = new Hashtable < String, String >();
	//IPropertyElement[] m_Elements = null;
	private Vector < IPropertyElement > m_Elements = new Vector < IPropertyElement >();
	
	//IPropertyDefinition[] m_Definitions = null;
	private Vector<IPropertyDefinition> m_Definitions = new Vector<IPropertyDefinition>();

	private IPropertyElementManager m_EleManager = null;
	// The preference manager event dispatcher
	private IPreferenceManagerEventDispatcher m_PreferenceManagerEventDispatcher = null;
	
	private String m_DefaultFont = null;
	private String m_DefaultDocFont = null;
    
    private boolean batchTestMode = false;

	private Vector < IPropertyElement > m_SavedElements = null;

	/**
	 * 
	 */
	public PreferenceManager() {
		super();
		m_EleManager = new PropertyElementManager();
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.preferenceManager());
		if (obj instanceof IPreferenceManagerEventDispatcher)
		{
			m_PreferenceManagerEventDispatcher = (IPreferenceManagerEventDispatcher)obj;
		}
	}
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2#isBatchTestMode()
     */
    public boolean isBatchTestMode()
    {
        return batchTestMode;
    }
    
    public void setBatchTestMode(boolean btm)
    {
        batchTestMode = btm;
    }

	/**
	 * Register the passed-in file as the "default" preference file.
	 * 
	 * @param fileName[in]	The absolute path of the file
	 *
	 * @return HRESULT
	 *
	 */
	public long registerFile(String fileName) {
		registerFile("Default", fileName);
		m_DefaultFile = fileName;
		return 0;
	}

	/**
	 * Remove the passed in file as the "default" file.
	 * 
	 *
	 * @param fileName[in]	The absolute path of the preference file
	 *
	 * @return HRESULT
	 *
	 */
	public long unregisterFile(String fileName) {
		unregisterFile("Default", fileName);
		return 0;
	}

	/**
	 * Validates the file for existence, read/write access, and validity against
	 * a preference DTD.
	 *
	 * @param fileName[in]	The absolute path of the preference file
	 *
	 * @return HRESULT
	 */
	public boolean validateFile(String fileName) {
		return XMLManip.getDOMDocument(fileName) != null;
	}

	public Vector<IPropertyDefinition> getPropertyDefinitions() {
		return m_Definitions;
	}

	public void setPropertyDefinitions(IPropertyDefinition[] value) 
	{
		if (value != null)
		{
			m_Definitions.clear();
			int count = value.length;
			for (int i=0; i<count; i++)
			{
				m_Definitions.add(value[i]);
			}
		}
	}

	public IPropertyElement[] getPropertyElements() 
	{
		return getElementsFromVector(m_Elements);
	}

	public void setPropertyElements(IPropertyElement[] value) 
	{
		if (m_Elements == null)
		{
			m_Elements = new Vector < IPropertyElement >();
		}
		
		if (value != null)
		{
			for (int i=0; i<value.length; i++)
			{
				IPropertyElement elem = value[i];
				m_Elements.add(elem);
			}
		}
	}

	/**
	 * Use the passed in file and build the preference definitions and preference elements
	 * to be used by the Describe application.
	 *
	 * @param fileName[in]	The absolute path of the preference file
	 *
	 * @return HRESULT
	 */
	public long buildPreferences(String fileName) 
	{
		buildDefinitions(fileName);
		String dataFile = getRelatedDataFile(fileName);
		if (dataFile != null && dataFile.length() > 0)
		{
			if (validateFile(dataFile))
			{
				buildElements(dataFile);
			}
		}
		return 0;
	}

	/**
	 * Builds the preference elements which tells Describe the values of the preferences.
	 *
	 * @param fileName[in]	The absolute path of the preference file
	 *
	 * @return HRESULT
	 */
	private void buildElements(String dataFile) {
		if (m_EleManager != null)
		{
			m_EleManager.setElementFile(dataFile);
			// now for every definition that has been built, we need to find its
			// corresponding elements in the passed in file and build them.  For the preferences right now,
			// this is a one for one, but it should be set up to handle multiples.
			int defCount = m_Definitions.size();
			int eleCount = 0;
			for (int i =0; i<defCount; i++)
			{
				IPropertyDefinition def = (IPropertyDefinition)m_Definitions.get(i);
				IPropertyElement[] elems = m_EleManager.buildElementsUsingXMLFile(def);
				if (elems != null)
				{
					for (int j=0; j<elems.length; j++)
					{
						m_Elements.add(elems[j]);
						eleCount++;
					}
				}
			}
		}
	}

	/**
	 * Builds the preference definitions which tells Describe how to display
	 * the preferences in the dialog.
	 * 
	 * @param fileName[in]	The absolute path of the preference file
	 *
	 * @return HRESULT
	 */
	private void buildDefinitions(String fileName) {
		IPropertyDefinitionFactory fact = new PropertyDefinitionFactory();
		fact.setDefinitionFile(fileName);
		// the factory knows how to build all definitions in a given file
		m_Definitions = fact.buildDefinitionsUsingFile();
	}


	public long reloadPreferences() {
		if (m_DefaultFile != null)
		{
			unregisterFile(m_DefaultFile);
			m_Definitions = new Vector<IPropertyDefinition>();
			m_Elements = new Vector<IPropertyElement>();
			registerFile(m_DefaultFile);
		}
		return 0;
	}

	/**
	 * Shortcut method to add a boolean preference to both the definitions and the elements.
	 *
	 * @param prefObj[in] The object that holds all of the values of a preference, including
	 *					the key, the name, the value (so we know what file and what the tree structure will be)
	 *
	 * @return HRESULT
	 */
	public long addBooleanPreference(IPreferenceObject prefObj) {
		addPreference(prefObj, CT_BOOLEAN);
		return 0;
	}

	/**
	 * Method that takes the preference object and the control type and creates a definition and
	 * an element, setting all of the proper values.
	 * 
	 *
	 * @param prefObj[in]	The object that holds all of the values of a preference, including
	 *						the key, the name, the value (so we know what file and what the tree structure will be)
	 * @param type[in]		An enumeration representing the type of preference it is (edit, list, etc)
	 *
	 * @return HRESULT
	 *
	 */
	private void addPreference(IPreferenceObject prefObj, int type) {
		buildPreference(prefObj, type);
		save();
	}

	/**
	 * Called when someone is trying to add a preference.  This method determines
	 * if a corresponding definition needs to be created as well as any other definitions
	 * to make its parent structure correct.  It then determines if a element needs to be
	 * created as well as any other elements to make its parent structure correct.
	 *
	 * @param prefObj[in]	An object that contains all of the preference information
	 * @param type[in]		The type of preference that is to be created
	 *
	 * @return HRESULT
	 */
	private void buildPreference(IPreferenceObject prefObj, int type) {
		String key = prefObj.getKey();
		if (key.length() == 0)
			key = "Default";
			
		// get the heading (path) of the preference to be created
		String heading = prefObj.getHeading();
		// get the name of the preference to be created
		String name = prefObj.getName();

		// find out if there is already a definition out there representing this preference
		IPropertyDefinition def = getPreferenceDefinition(key, heading, name);
		if (def != null)
		{
			// found a definition, so now see if there is a element out there representing this preference
			IPropertyElement elem = getPreferenceElement(key, heading, name);
			if (elem != null)
			{
				// done, found everything, so no need to create
			}
			else
			{
				// did not find a element, so create whatever is necessary to put this it in its
				// right place in the file
				IPropertyDefinition pDef = null;
				IPropertyElement pEle = null;
				buildNecessaryStructures(key, heading, pDef, pEle);
				if (pDef != null && pEle != null)
				{
					createPreference(prefObj, type, key, pDef, pEle);
				}
			}
		}
		else
		{
			// did not find a definition, so create whatever is necessary to put this it in its
			// right place in the file
			IPropertyDefinition pDef = null;
			IPropertyElement pEle = null;
			buildNecessaryStructures(key, heading, pDef, pEle);
			if (pDef != null && pEle != null)
			{
				createPreference(prefObj, type, key, pDef, pEle);
			}
		}
	}

	/**
 * Create a definition and an element representing the preference object that was built
 * by someone and then told the preference manager to add it.
 *
 * @param prefObj[in]	An object that contains all of the preference information
 * @param type[in]	The type of preference that is to be created
 * @param key[in]	The top level node of this preference structure
 * @param pDef[in]	The definition that will own the newly created preference
 * @param pEle[in]	The element that will own the newly created preference
 *
 * @return HRESULT
 */
	private void createPreference(IPreferenceObject prefObj, int type, String key, IPropertyDefinition pDef, IPropertyElement pEle) {
		// create definition
		IPropertyDefinitionXML newDef = new PropertyDefinitionXML();
		loadDefinition(prefObj, type, key, newDef);
		pDef.addSubDefinition(newDef);

		// create the corresponding element
		IPropertyElementXML newEle = new PropertyElementXML();
		String name = newDef.getName();
		newEle.setName(name);
		
		String file = getDefinitionFile(key);
		String file2 = getRelatedDataFile(file);
		
		String defVal = prefObj.getDefaultValue();
		if (defVal == null || defVal.length() == 0)
			defVal = " ";
			
		newEle.setValue(defVal);
		newEle.setPropertyDefinition(newDef);
		newEle.setModified(true);
		newEle.setPropertyElementManager(m_EleManager);
		pEle.addSubElement(newEle);
	}

	/**
	 * Populate the definition with the information from the preference object.
	 *
	 * @param prefObj[in]	An object that contains all of the preference information
	 * @param type[in]		The type of preference that is to be created
	 * @param key[in]		The top level node of this preference structure
	 * @param pDef[in]		The definition to populate
	 *
	 * @return HRESULT
	 */
	private void loadDefinition(IPreferenceObject prefObj, int type, String key, IPropertyDefinitionXML pDef) {
		String name = prefObj.getName();
		String dispName = prefObj.getDisplayName();
		if (dispName == null || dispName.length() == 0)
			dispName = name;
			
		String cType = getControlTypeAsString(type);
		String file = getDefinitionFile(key);
		
		pDef.setName(name);
		pDef.setDisplayName(dispName);
		if (cType != null && cType.length() > 0)
		{
			pDef.setControlType(cType);
			pDef.setRequired(false);
            pDef.setForceRefersh(false);
			pDef.setMultiplicity(1);
			
			String values = prefObj.getValues();
			pDef.setValidValues(values);
		}
		pDef.setModified(true);
		pDef.setFile(file);
	}

	/**
	 * Based on the key and the passed in path, this method builds any definitions and/or
	 * elements that are not found under the key and in the path.
	 *
	 * @param key[in]		The top level node of this preference structure
	 * @param path[in]			The path to the preference to be added
	 * @param pReturnDef[out]	The last found/created definition in the path
	 * @param pReturnEle[out]  The last found/created element in the path
	 *
	 * @return HRESULT
	 */
	private void buildNecessaryStructures(String key, String path, IPropertyDefinition pDef, IPropertyElement pEle) {
		String file;
		// get the top level definition
		IPropertyDefinition rootDef = getRootDefinition(key);
		if (rootDef == null)
		{
			// don't have a top level node for this "key", so create one and
			// set its information
			rootDef = new PropertyDefinitionXML();
			rootDef.setName(key);
			rootDef.setDisplayName(key);
			rootDef.setModified(true);
			file = getDefinitionFile(key);
			((IPropertyDefinitionXML)rootDef).setFile(file);
			m_Definitions.add(rootDef);
		}
		else
			file = "";
		
		// get the top level element
		IPropertyElement topEle = getRootElement(key);
		if (topEle == null)
		{
			topEle = new PropertyElementXML();
			topEle.setPropertyDefinition(rootDef);
			topEle.setName(key);
			topEle.setModified(true);
			String file2 = getRelatedDataFile(file);
			topEle.setPropertyElementManager(m_EleManager);
			m_Elements.add(topEle);
		}
		
		// continue if we have a top level definition and element
		if (rootDef != null && topEle != null)
		{
			// we are now going to navigate down the definition and element structures
			// looking for a child that matches each name in the passed in path string
			IPropertyDefinition prevDef = null;
			IPropertyElement prevEle = null;
			IPropertyDefinition tempDef = rootDef;
			IPropertyElement tempEle = topEle;
			if (path.length() > 0)
			{
				String[] strs = path.split("\\|");
				if (strs != null && strs.length > 0)
				{
					for (int i=0; i<strs.length; i++)
					{
						prevDef = tempDef;
						prevEle = tempEle;
						String str = strs[i];
						// is there a definition matching this string in the child nodes of the definition
						IPropertyDefinition def = getDefinition(prevDef, str);
						IPropertyElement ele = getElement(prevEle, str);
						// now if there wasn't a definition and/or element in the child nodes, we
						// will create one
						String file3 = getDefinitionFile(key);
						if (def == null)
						{
							// no definition found, so create one
							IPropertyDefinition pDef3 = new PropertyDefinitionXML();
							pDef3.setName(str);
							pDef3.setDisplayName(str);
							pDef3.setModified(true);
							((IPropertyDefinitionXML)pDef3).setFile(file3);
							prevDef.addSubDefinition(pDef3);
							// this new one now becomes our point of reference
							tempDef = pDef3;
						}
						else
						{
							// this one that we found now becomes the point of reference
							tempDef = def;
						}
						
						if (ele == null)
						{
							IPropertyElement pEle3 = new PropertyElementXML();
							pEle3.setName(str);
							pEle3.setModified(true);
							String file4 = getRelatedDataFile(file3);
							pEle3.setPropertyElementManager(m_EleManager);
							pEle3.setPropertyDefinition(tempDef);
							prevEle.addSubElement(pEle3);
							// this new one now becomes our point of reference
							tempEle = pEle3;
						}
						else
						{
							// this one that we found now becomes the point of reference
							tempEle = ele;
						}
					}
				}
			}
			if (tempDef != null)
			{
				pDef = tempDef;
			}
			if (tempEle != null)
			{
				pEle = tempEle;
			}
		}
	}

	/**
	 * Get the preference definition that is the root that matches the passed in key.
	 *
	 * @param key[in]	Name to search for
	 * @param pReturnDef[out]	Found preference definition
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyDefinition getRootDefinition(String key) {
		for (int i=0; i<m_Definitions.size(); i++)
		{
			IPropertyDefinition tDef = m_Definitions.elementAt(i);
			if (tDef.getName().equals(key))
			{
				return tDef;
			}
		}
		return null;
	}

	/**
	 * Get the preference element that is the root that matches the passed in key.
	 * 
	 *
	 * @param key[in]	Name to search for
	 * @param pReturnDef[out] Found preference element
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyElement getRootElement(String key) {
		for (int i=0; i<m_Elements.size(); i++)
		{
			IPropertyElement elem = m_Elements.elementAt(i);
			if (elem.getName().equals(key))
			{
				return elem;
			}
		}
		return null;
	}


	/**
	 * Need the string representation of the preference type in order to store it properly in the
	 * preference files.
	 * 
	 *
	 * @param type[in]	An enumeration representing the type of preference it is (edit, list, etc)
	 * @param pVal[out]	The string representation of the type
	 *
	 * @return HRESULT
	 *
	 */
	private String getControlTypeAsString(int type)
	{
		switch (type)
		{
			case CT_BOOLEAN:
				return "list";
			case CT_LIST:
				return "list";
			case CT_COMBO:
				return "combo";
			case CT_EDIT:
				return "edit";
			default:
				return "";
		}
	}

	/**
	 * Remove the preference that represents the passed-in property element.  This will
	 * remove it from the preference manager's array as well as the XML file that it is in.
	 *
	 * @param pEle[in]	The property element representing the preference that is to be removed
	 *
	 * @return HRESULT
	 */
	public long removePreference(IPropertyElement pEle) {
		String pathToRemove = pEle.getPath();
		if (m_Elements != null)
		{
			for(int i=0; i<m_Elements.size(); i++)
			{
				// must check the top level elements
				IPropertyElement elem = m_Elements.elementAt(i);
				String path = elem.getPath();
				if (path.equals(pathToRemove))
				{
					// found the preference at top level
					firePreferenceRemove(elem);
					m_Elements.remove(elem);
					break;
				}
				else
				{
					// was not a top level preference, so must process the sub elements
					removePreference(elem, pEle);
				}
			}
		}
		return 0;
	}

	/**
	 * Remove the preference that represents the passed in property element.  This will
	 * remove it from the preference manager's array as well as the xml file that it is in.
	 *
	 * @param pEleToSearch[in]	The property element to search
	 * @param pEle[in]			The property element representing the preference that is to be removed
	 *
	 * @return HRESULT
	 */
	private void removePreference(IPropertyElement elem, IPropertyElement pEle) {
		String pathToRemove = pEle.getPath();
		Vector subElems = elem.getSubElements();
		if (subElems != null)
		{
			for(int i=0; i<subElems.size(); i++)
			{
				IPropertyElement subElem = (IPropertyElement)subElems.elementAt(i);
				String path = subElem.getPath();
				if (path.equals(pathToRemove))
				{
					firePreferenceRemove(subElem);
					subElems.remove(subElem);
					break;
				}
				else
				{
					removePreference(subElem, pEle);
				}
			}
		}
	}

	/**
	 * Validates the passed-in file and if valid adds it to a map of key/files that
	 * will be used as a lookup table to determine where a particular preference is stored.
	 *
	 * @param key[in]
	 * @param fileName[in]
	 *
	 * @return HRESULT
	 *
	 */
	public long registerFile(String key, String fileName) {
		if (validateFile(fileName))
		{
			Object obj = m_FileMap.get(key);
			if (obj == null)
			{
				m_FileMap.put(key, fileName);
				File file = new File(fileName);
				
				//removed the check to see if file is writable, I want
				//to load preferences even if the file is writable.
				if (file.exists())  
				{
					buildPreferences(fileName);
				}
			}
		}
		return 0;
	}

	/**
	 * @param fileName
	 * @return
	 */
	private boolean canWrite(String fileName) {
		File f = new File(fileName);
		return f.canWrite();
	}

	public long unregisterFile(String key, String fileName) {
		if (m_FileMap.get(key) != null)
		{
			m_FileMap.remove(key);
		}
		return 0;
	}

	/**
	 * Shortcut method to add a list preference to both the definitions and the elements.
	 *
	 * @param prefObj[in]	The object that holds all of the values of a preference, including
	 *								the key, the name, the value (so we know what file and what the tree structure will be)
	 *
	 * @return HRESULT
	 */
	public long addListPreference(IPreferenceObject prefObj) {
		addPreference(prefObj, CT_LIST);
		return 0;
	}

	/**
	 * Shortcut method to add a combo preference to both the definitions and the elements.
	 *
	 * @param prefObj[in]	The object that holds all of the values of a preference, including
	 *								the key, the name, the value (so we know what file and what the tree structure will be)
	 *
	 * @return HRESULT
	 */
	public long addComboPreference(IPreferenceObject prefObj) {
		addPreference(prefObj, CT_COMBO);
		return 0;
	}

	/**
	 * Shortcut method to add a edit preference to both the definitions and the elements.
	 *
	 * @param prefObj[in]	The object that holds all of the values of a preference, including
	 *								the key, the name, the value (so we know what file and what the tree structure will be)
	 *
	 * @return HRESULT
	 */
	public long addEditPreference(IPreferenceObject prefObj) {
		addPreference(prefObj, CT_EDIT);
		return 0;
	}

	/**
	 * Get the value of the passed-in preference name.  This routine searches in the "default"
	 * preference structure.  For example, "DefaultFilter" will return "PSK_DATA".
	 *
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[out]		The value of the found preference
	 *
	 * @return HRESULT
	 *
	 */
	public String getPreferenceValue(String path, String prefName) {
		return getPreferenceValue("Default", path, prefName);
	}
    
    /**
     * Get the value of the passed-in preference name.  This routine searches in
     * the hive specified as the first segment of the path. 
     *
     * @param path[in]      A "|" delimited string telling what preference structure path to search
     * @return The preference.
     */
    public String getPreferenceValue(String fullPath)
    {
        int lastPos  = fullPath.lastIndexOf('|');
        int firstPos = fullPath.indexOf('|');
        if (firstPos != -1 && lastPos != -1 && firstPos != lastPos)
        {
            String hive     = fullPath.substring(0, firstPos);
            String prefPath = fullPath.substring(firstPos + 1, lastPos);
            String prefName = fullPath.substring(lastPos + 1);
            
            return getPreferenceValue(hive, prefPath, prefName);
        }
        return null;
    }

	/**
	 * Get the value of the passed in preference name.  This routine searches in the "key"
	 * preference structure. For example, "DefaultFilter" will return "PSK_DATA".
	 * 
	 *
	 * @param key[in]		The top preference to search
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[out]		The value of the found preference
	 *
	 * @return HRESULT
	 *
	 */
	public String getPreferenceValue(String key, String path, String prefName) {
		IPropertyElement elem = getPreferenceElement(key, path, prefName);
		return elem != null ? elem.getValue() : "";
	}

	/**
	 * The preference manager has been told to save its information.  It will loop through
	 * its definitions and its elements, saving them if necessary.
	 *
	 * @return HRESULT
	 */
	public long save() 
	{
		saveDefinitions();
		saveElements();
		return 0;
	}

	/**
	 * Process the property elements that the preference manager knows about, saving any
	 * if needed.
	 *
	 * @return HRESULT
	 */
	private void saveElements() 
	{
		if (m_SavedElements == null)
		{
			m_SavedElements = new Vector < IPropertyElement >();
		}
		if (m_Elements != null && m_Elements.size() > 0)
		{
			boolean continueFlag = true;
			int count = m_Elements.size();
			for (int i=0; i<count; i++)
			{
				IPropertyElement obj = m_Elements.get(i);
				if (obj instanceof IPropertyElementXML)
				{
					IPropertyElementXML ele = (IPropertyElementXML)obj;
					String name = ele.getName();
					String file = getDefinitionFile(name);
					String file2 = getRelatedDataFile(file);
					Document doc = XMLManip.getDOMDocument(file2);
					if (doc != null)
					{
						if (ele.getModified())
						{
							if (ele.save(doc))
							{
								firePreferenceChange(ele);
								m_SavedElements.add(ele);
							}
							else
							{
								continueFlag = false;
								break;
							}
						}
						if (saveSubElements(doc, ele))
						{
							XMLManip.save(doc, file2);
						}
						else
						{
							continueFlag = false;
							break;
						}
					}
				}
			}
			
			if (continueFlag)
			{
				if (m_SavedElements != null)
				{
					int len = m_SavedElements.size();
					if (len > 0)
					{
						firePreferencesChange(m_SavedElements);

						//now remove all the elements from this savedElements vector
						m_SavedElements.removeAllElements();
//						for(int x=len; x>=0; x--)
//						{
//							m_SavedElements.remove(x);
//						}
					}
				}
			}
		}
	}

	/**
	 * Call to the event dispatcher to fire a preferences change event.
	 *
	 * @param pEles[in]	The property elements that have changed
	 *
	 * @return HRESULT
	 */
	private void firePreferencesChange(Vector < IPropertyElement > elements) 
	{
		DispatchHelper helper = new DispatchHelper();
		IPreferenceManagerEventDispatcher disp = helper.getPreferenceManagerDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreferencesChange");
			IPropertyElement[] elems = getElementsFromVector(elements);
			disp.firePreferencesChange(elems, payload );
		}
	}
	
	private IPropertyElement[] getElementsFromVector(Vector < IPropertyElement > elements)
	{
		IPropertyElement[] retVal = new IPropertyElement[elements.size()];
		elements.toArray(retVal);
		
		return retVal;
		
//		int count = elements.size();
//		IPropertyElement[] elems = new IPropertyElement[count];
//		for (int i=0; i<count; i++)
//		{
//			IPropertyElement elem = (IPropertyElement)elements.elementAt(i);
//			elems[i] = elem;
//		}
//		return elems;
	}

	/**
	 * Call to the event dispatcher to fire a preference change event.
	 *
	 * @param pEle[in]	The property element that has changed
	 *
	 * @return HRESULT
	 */
	private void firePreferenceChange(IPropertyElement ele) 
	{
		DispatchHelper helper = new DispatchHelper();
		IPreferenceManagerEventDispatcher disp = helper.getPreferenceManagerDispatcher();
		if (disp != null)
		{
			disp.firePreferenceChange(ele.getName(), ele, disp.createPayload("PreferenceChange"));
		}
	}

	/**
	 * Call to the event dispatcher to fire a preference remove event.
	 *
	 * @param pEle[in]	The property element that is being removed
	 *
	 * @return HRESULT
	 */
	private void firePreferenceRemove(IPropertyElement ele) 
	{
		DispatchHelper helper = new DispatchHelper();
		IPreferenceManagerEventDispatcher disp = helper.getPreferenceManagerDispatcher();
		if (disp != null)
		{
			disp.firePreferenceRemove(ele.getName(), ele, disp.createPayload("PreferenceRemove") );
		}
	}

	/**
	 * We only keep track of the definition file in the preference manager.  The data
	 * file is the same name as the definition file but with a different extension.  This
	 * routine takes the definition file and translates it to the right data file.
	 *
	 * @param key[in]		The definition file string
	 * @param outFile[out]	The data file matching the passed in definition file string
	 *
	 * @return HRESULT
	 */
	private String getRelatedDataFile(String file) {
		String thepath = StringUtilities.getPath(file);
		String thefile = StringUtilities.getFileName(file);
		return StringUtilities.createFullPath(thepath, thefile, ".etcd");
	}

	/**
	 * Based on the passed-in key, get the file that is registered for it.
	 * If there is not a file, we will return the "default" file.
	 *
	 * @param key[in]		The key string
	 * @param outFile[out]	The file named "filed" for the key
	 *
	 * @return HRESULT
	 */
	private String getDefinitionFile(String name) {
		String outFile = m_FileMap.get(name);
		if (outFile == null)
		{
			outFile = m_FileMap.get("Default");
		}
		return outFile;
	}

	/**
	 * Save any of the sub elements of the passed in property element, if they have been
	 * marked as changed.
	 * 
	 *
	 * @param pEle[in]	The property element to process its subs
	 *
	 * @return HRESULT
	 */
	private boolean saveSubElements(Document doc, IPropertyElementXML ele) {
		boolean contFlag = true;
		Vector elems = ele.getSubElements();
		if (elems != null && elems.size() > 0)
		{
			for (int i=0; i<elems.size(); i++)
			{
				Object obj = elems.get(i);
				if (obj instanceof IPropertyElementXML)
				{
					IPropertyElementXML xEle = (IPropertyElementXML)obj;
					if (xEle.getModified())
					{
						if (xEle.save(doc))
						{
							firePreferenceChange(xEle);
							m_SavedElements.add(xEle);
						}
						else
						{
							contFlag = false;
							break;
						}
					}
					contFlag = saveSubElements(doc, xEle);
					if (!contFlag)
						break;
				}
			}
		}
		return contFlag;
	}

	/**
	 * Process the property definitions that the preference manager knows about, saving any
	 * if needed.
	 *
	 * @return HRESULT
	 */
	private void saveDefinitions() {
		if (m_Definitions != null && m_Definitions.size() > 0)
		{
			int count = m_Definitions.size();
			for (int i=0; i<count; i++)
			{
				Object obj = m_Definitions.get(i);
				if (obj instanceof IPropertyDefinition)
				{
					IPropertyDefinition def = (IPropertyDefinition)obj;
					if (def.isModified())
					{
						def.save();
					}
					saveSubDefinitions(def);
				}
			}
		}
	}

	/**
	 * Save any of the sub definitions of the passed in property def, if they have been
	 * marked as changed.
	 * 
	 *
	 * @param pDef[in]	The property definition to process its subs
	 *
	 * @return HRESULT
	 */
	private void saveSubDefinitions(IPropertyDefinition def) {
		Vector defs = def.getSubDefinitions();
		if (defs != null && defs.size() > 0)
		{
			for (int i=0; i<defs.size(); i++)
			{
				Object obj = defs.get(i);
				if (obj instanceof IPropertyDefinition)
				{
					IPropertyDefinition subDef = (IPropertyDefinition)obj;
					if (subDef.isModified())
					{
						subDef.save();
					}
					saveSubDefinitions(subDef);
				}
			}
		}
	}

	/**
	 * Set the value of the passed in preference name.  This routine searches in the "default"
	 * preference structure.
	 * 
	 *
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[in]		The value to set on the found preference
	 *
	 * @return HRESULT
	 *
	 */
	public long setPreferenceValue(String path, String prefName, String pVal) {
		setPreferenceValue("Default", path, prefName, pVal);
		return 0;
	}

	/**
	 * Set the value of the passed in preference name.  This routine searches in the "key"
	 * preference structure.
	 * 
	 *
	 *	@param key[in]		The top preference to search
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[in]		The value to set on the found preference
	 *
	 * @return HRESULT
	 *
	 */
	public long setPreferenceValue(String key, String path, String prefName, String pVal) {
		IPropertyElement pEle = getPreferenceElement(key, path, prefName);
		if (pEle != null)
		{
			setPreferenceValue(pEle, pVal);
		}
		return 0;
	}

	/**
	 * Set the value of the passed in preference.
	 * 
	 * @param pEle [in] The element to change
	 * @param newVal [in] The new value for this property element
	 */
	public long setPreferenceValue(IPropertyElement pEle, String pVal) {
		String oldVal = pEle != null ? pEle.getValue() : "";
		if (oldVal == null || !oldVal.equals(pVal))
		{
			pEle.setValue(pVal);
			pEle.setModified(true);
			save();
		}
		return 0;
	}

	/**
	 * Retrieve the actual preference element that is found under the "default" top level element 
	 * and is at the proper sub level that matches the passed in path and pref name.
	 *
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[out]		The found property element
	 *
	 * @return HRESULT
	 */
	public IPropertyElement getPreferenceElement(String path, String prefName) {
		return getPreferenceElement("Default", path, prefName);
	}

	/**
	 * Find the preference element given its key and its path that matches the prefName.  
	 * The path is a "|" delimited string which tells us the sub element
	 * structure in which to search (since the preference could be multiple levels deep)
	 * 
	 *
	 * @param key[in]			The top preference to search
	 * @param path[in]			A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]		The preference name
	 * @param pVal[out]			The found preference element
	 *
	 * @return HRESULT
	 */
	public IPropertyElement getPreferenceElement(String key, String path, String prefName) {
		IPropertyElement retEle = null;
		// get the root
		IPropertyElement rootEle = getRootElement(key);
		if(rootEle != null)
		{
			if (path.length() > 0)
			{
				// parse the path into its separate strings
				// and then drill down the element structure getting
				// each sub element of the previous element that matches the appropriate string
				IPropertyElement tempEle = rootEle;
				if (path.indexOf("|") >= 0)
				{
					String[] strs = path.split("\\|");
					if (strs != null && strs.length > 0)
					{
						for (int i=0; i<strs.length; i++)
						{
							if (tempEle != null)
							{
								IPropertyElement tempEle2 = getElement(tempEle, strs[i]);
								tempEle = tempEle2;
							}
						}
						// now that we have found the element at the bottom of the structure for the path
						if (tempEle != null)
						{
							// now search its children for the element matching the prefName
							retEle = getElement(tempEle, prefName);
						}
					}
				}
				else
				{
					tempEle = getElement(rootEle, path);
					retEle = getElement(tempEle, prefName);
				}
			}
			else
			{
				// if there is not a path specified, then the element may be found at the root
				// level, so search the root's children
				if (prefName.length() > 0)
				{
					retEle = getElement(rootEle, prefName);
				}
				else
				{
					// if there was no path and no pref name, then they must want the root back?
					retEle = rootEle;
				}
			}
		}
		return retEle;
	}

	/**
	 * Find the appropriately named sub element in the passed-in element.
	 *
	 * @param pEle[in]		The preference element to search
	 * @param name[in]		The name of the element to find
	 * @param pReturnEle[out]	The found preference element
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyElement getElement(IPropertyElement rootEle, String prefName) {
		IPropertyElement retEle = null;
		if (rootEle != null)
		{
			String name = rootEle.getName();
			if (name.equals(prefName))
			{
				retEle = rootEle;
			}
			else
			{
				Vector elems = rootEle.getSubElements();
				if (elems != null)
				{
					for (int i=0; i<elems.size(); i++)
					{
						IPropertyElement elem = (IPropertyElement)elems.elementAt(i);
						if (elem.getName().equals(prefName))
						{
							retEle = elem;
							break;
						}
					}
				}
			}
		}
		return retEle;
	}

	/**
	 * Retrieve the actual preference def that is found under the "default" top level def 
	 * and is at the proper sub level that matches the passed in path and pref name.
	 *
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[out]		The found property def
	 *
	 * @return HRESULT
	 */
	public IPropertyDefinition getPreferenceDefinition(String path, String prefName) {
		return getPreferenceDefinition("Default", path, prefName);
	}

	/**
	 * Find the preference def given its key and its path that matches the prefName.  
	 * The path is a "|" delimited string which tells us the sub def
	 * structure in which to search (since the preference could be multiple levels deep)
	 * 
	 *
	 * @param key[in]			The top preference to search
	 * @param path[in]			A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]		The preference name
	 * @param pVal[out]			The found preference def
	 *
	 * @return HRESULT
	 */
	public IPropertyDefinition getPreferenceDefinition(String key, String path, String prefName) {
		IPropertyDefinition retDef = null;
		// get the root
		IPropertyDefinition rootDef = getRootDefinition(key);
		if (rootDef != null)
		{
			if (path.length() > 0)
			{
				// parse the path into its separate strings
				// and then drill down the def structure getting
				// each sub def of the previous def that matches the appropriate string
				IPropertyDefinition tempDef = rootDef;
				String[] strs = path.split("\\|");
				if (strs != null && strs.length > 0)
				{
					for (int i=0; i<strs.length; i++)
					{
						if (tempDef != null)
						{
							IPropertyDefinition tempDef2 = getDefinition(tempDef, strs[i]);
							tempDef = tempDef2;
						}
					}
					// now that we have found the def at the bottom of the structure for the path
					if (tempDef != null)
					{
						// now search its children for the element matching the prefName
						IPropertyDefinition pDef = getDefinition(tempDef, prefName);
						if (pDef != null)
						{
							retDef = pDef;
						}
					}
				}
			}
			else
			{
				// if there is not a path specified, then the def may be found at the root
				// level, so search the root's children
				if (prefName.length() > 0)
				{
					IPropertyDefinition pDef = getDefinition(rootDef, prefName);
					if (pDef != null)
					{
						retDef = pDef;
					}
				}
				else
				{
					// if there was no path and no pref name, then they must want the root back?
					retDef = rootDef;
				}
			}
		}
		return retDef;
	}

	/**
	 * Find the appropriately named sub def in the passed in def.
	 *
	 * @param pDef[in]			The preference def to search
	 * @param name[in]			The name of the def to find
	 * @param pReturnDef[out]	The found preference def
	 *
	 * @return HRESULT
	 */
	private IPropertyDefinition getDefinition(IPropertyDefinition rootDef, String prefName) {
		IPropertyDefinition retDef = null;
		String name = rootDef.getName();
		if (name.equals(prefName))
		{
			retDef = rootDef;
		}
		else
		{
			Vector defs = rootDef.getSubDefinitions();
			if (defs != null && defs.size() > 0)
			{
				for (int i=0; i<defs.size(); i++)
				{
					IPropertyDefinition def = (IPropertyDefinition)defs.elementAt(i);
					if (prefName.equals(def.getName()))
					{
						retDef = def;
						break;
					}
				}
			}
		}
		return retDef;
	}

	/**
	 * Get the translated value of the passed in preference name.  
	 * This routine searches in the "default" preference structure.  
	 * This will convert any "specially marked" preferences to their IDS resource string
	 * which allows us to localize preferences.  For example, "DefaultFilter" will return "Data".
	 *
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[out]		The value of the found preference
	 *
	 * @return HRESULT
	 *
	 */
	public String getTranslatedPreferenceValue(String path, String prefName) {
		return getTranslatedPreferenceValue("Default", path, prefName);
	}

	/**
	 * Get the translated value of the passed-in preference name.  This routine searches in the "key"
	 * preference structure.  For example, "DefaultFilter" will return "Data".
	 * 
	 *
	 *	@param key[in]		The top preference to search
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param pVal[out]		The value of the found preference
	 *
	 * @return HRESULT
	 *
	 */
	public String getTranslatedPreferenceValue(String key, String path, String prefName) {
		IPropertyElement elem = getPreferenceElement(key, path, prefName);
		return elem != null ? elem.getTranslatedValue() : "";
	}

	/**
	 * Determines whether the passed in preference has the passed-in value.
	 *
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param prefValue[in]	The preference value to check for
	 * @param pVal[out]		Whether or not the preference's value matches the passed in string
	 *
	 * @return HRESULT
	 */
	public boolean matches(String path, String prefName, String prefValue) {
		return matches("Default", path, prefName, prefValue);
	}

	/**
	 * Find the preference def given its key and its path that matches the prefName.  
	 * The path is a "|" delimited string which tells us the sub def
	 * structure in which to search (since the preference could be multiple levels deep)
	 * 
	 * @param key[in]		The top preference to search
	 * @param path[in]		A "|" delimited string telling what preference structure path to search
	 * @param prefName[in]	The preference name
	 * @param prefValue[in]	The preference value to check for
	 * @param pVal[out]		Whether or not the preference's value matches the passed in string
	 *
	 * @return HRESULT
	 */
	public boolean matches(String key, String path, String prefName, String prefValue) {
		String value = getPreferenceValue(key, path, prefName);
		return value != null && value.equals(prefValue);
	}

	/**
	 * The preference manager has been told to restore its information to the state that it was when
	 * the application was installed.
	 *
	 * @return HRESULT
	 */
	public long restore() {
		if (m_Elements != null)
		{
			restoreElements(m_Elements, false);
		}
		return 0;
	}

	/**
	 * Needed a path to restore the preferences back to their original state (as they were installed)
	 * or as if it was a brand new install.
	 *
	 * Needed to distinguish between the two paths because if it is "false" then the default values of the
	 * preferences are filled in with values that were calculated when the app started.  Fonts = "Arial"
	 * ConfigLocation = c:\ uml\config.
	 *
	 * The path if the variable is "true" is more for internal reasons.  We wanted to set the Font and config
	 * values back to blank so that when the application runs for the first time, they get filled in, because
	 * it would be different on every box.
	 *
	 * @param pEles[in]			The property elements to restore
	 * @param forInstall[in]	Flag whether this restore is for a clean install 
	 *
	 * @return HRESULT
	 *
	 */
	private void restoreElements(Vector elems, boolean forInstall) {
		if (elems != null)
		{
			int count = elems.size();
			for (int i=0; i<count; i++)
			{
				IPropertyElement elem = (IPropertyElement)elems.elementAt(i);
				IPropertyDefinition def = elem.getPropertyDefinition();
				if (def != null)
				{
					boolean hasDefault = def.isDefaultExisting();
					if (hasDefault)
					{
						String defValue = def.getDefaultValue();
						if (defValue.length() == 0)
						{
							if (!forInstall)
							{
								defValue = def.getValidValues2();
							}
						}
						elem.setValue(defValue);
						elem.setModified(true);
					}
				}
				Vector subElems = elem.getSubElements();
				if (subElems != null)
				{
					restoreElements(subElems, forInstall);
				}
			}
		}
	}

	/**
	 * This method is used to determine what the default font of the grid should be for the
	 * application.  We have a preference that has this value, but the value needs to be different
	 * between a English OS and a Japanese OS.
	 *
	 * @param pVal[in]	The default grid font (Arial or MS Gothic)
	 *
	 * @return HRESULT
	 */
	public String getDefaultFont() {
		//Do I need to get the font from somewhere else? Sumitabh
		return "Arial";
	}

	/**
	 * This method is used to determine what the default font of the doc pane should be for the
	 * application.  We have a preference that has this value, but the value needs to be different
	 * between a English OS and a Japanese OS.
	 *
	 * @param pVal[in]	The default doc font (Arial or MS P Gothic)
	 *
	 * @return HRESULT
	 */
	public String getDefaultDocFont() {
		//Do I need to get the font from somewhere else? Sumitabh
		return "Arial";
	}

	/**
	 * The preference manager has been told to restore its information to the state that it was when
	 * the application was installed.
	 *
	 * @return HRESULT
	 */
	public long restoreForInstall() {
		if (m_Elements != null)
		{
			restoreElements(m_Elements, true);
		}
		return 0;
	}

	/**
	 * Finds a preference of name sName under pEle.  The found element returned is the 
	 * first sName found in that hive - at any child level, not just immediate children
	 *
	 * @param pEle[in]		The preference element to search
	 * @param name[in]		The name of the element to find
	 * @param pReturnEle[out]	The found preference element
	 */
	public IPropertyElement findElement(IPropertyElement pEle, String sName) {
		IPropertyElement retEle = null;
		String name = pEle.getName();
		if (name.equals(sName))
		{
			retEle = pEle;
		}
		else
		{
			Vector subElems = pEle.getSubElements();
			if (subElems != null)
			{
				for(int i=0; i<subElems.size(); i++)
				{
					IPropertyElement elem = (IPropertyElement)subElems.elementAt(i);
					if (sName.equals(elem.getName()))
					{
						retEle = elem;
						break;
					}
				}
				
				if (retEle == null)
				{
					// Go into the children and find this element among the children
					for(int i=0; i<subElems.size() && (retEle == null); i++)
					{
						IPropertyElement elem = (IPropertyElement)subElems.elementAt(i);
						retEle = findElement(elem, sName);
					}
				}
			}
		}
		return retEle;
	}

	/**
	 * @param pEle
	 * @return
	 */
	public boolean isEditable(IPropertyElement pEle)
	{
		//get the element file for this element manager
		return canWrite(m_EleManager.getElementFile());
	}


	/**
	 *
	 * Retrieves a preference value give the absolute path to the preference. 
	 * For example, 'Default|ConfigManagement|Enabled' would retrieve the preference 
	 * that indicated whether or not configuration management was enabled or not. 
	 * The hive must be the first '|' delimited token ( 'Default' in the example ), 
	 * and the actual preference name must be the last token ( 'Enabled' in the previous example ).
	 *
	 * @param fullPathToPreference[in]  See above for details
	 * @param pVal[out]                 The preference value
	 *
	 * @return HRESULT
	 *
	 */	
	public String getPreferenceValueWithFullPath(String fullPathToPreference)
	{
		if( fullPathToPreference != null && fullPathToPreference.length() > 0)
		{
			String prefPath = fullPathToPreference;
			int firstPos = prefPath.indexOf('|');
			if (firstPos > -1)
			{
				String hive = prefPath.substring(0, firstPos);
				int lastPos = prefPath.lastIndexOf('|');
				if (lastPos > -1)
				{
					String prefName = prefPath.substring(lastPos + 1);
				 	String path = prefPath.substring( firstPos + 1, lastPos);

					if( hive.length() > 0 && prefName.length() > 0 && path.length() > 0 )
					 {
						return getPreferenceValue( hive, path, prefName );
					 }
			  }
		   }
		}
		return "";
	}
	
}


