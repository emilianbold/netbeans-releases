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



package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.UMLCoreModule;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Validator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class ConfigManager implements IConfigManager
{

    private String m_HomeLocation = "";
    private String m_PresentationTypes = "";
    private String m_StereotypeIconsLocation = "";
    private String m_Preferences = "";
    private String m_EventFramework = "";
    private String m_DTD = "";
    private String m_DefaultConfigLocation = "";
    private String m_DefaultResources = "";
    private String m_OverriddenResources = "";
    
    private class HiveElement
    {
        private String m_Id = "";
        private String m_CreateTransitionId = "";
        public HiveElement()
        {
        }
        public HiveElement(String id, String ctId)
        {
            m_Id = id;
            m_CreateTransitionId = ctId;
        }
    }
    
    // this should contain HiveName and hashtable of HiveElement
    private Hashtable<String, Hashtable<String, HiveElement>> m_Hives =
        new Hashtable<String, Hashtable<String, HiveElement>>();
    
    public ConfigManager()
    {
        // this((String)System.getProperty("embarcadero.home-dir",
        //    System.getProperty("user.home") + File.separatorChar + ".uml"));
        
        // The following line will get the netbeans.user property which should 
        //  be something like xyz/.jstudio/Ent81, where xyz is soe os specific
        //  user home (example Win - C:\Documents and Settings\Admin\), or
        //  the custom netbeans user dir setup by the developer.
        // If netbeans.user dir is not defined (will happen in Unit Test 
        //  context), the java.io.tmpdir property is used as the user dir.
        // In either case, .uml is appended to the end to form the location
        //  for the UML config directory
        this(
            System.getProperty("netbeans.user", // NOI18N
                System.getProperty("java.io.tmpdir")) // NOI18N
                    + File.separatorChar + ".uml"); // NOI18N
    }
    
    public ConfigManager(String homeLocation)
    {
        Debug.log(this, "uml config dir="+homeLocation);
        
        UMLCoreModule.checkInitUml1();
        
        m_HomeLocation = homeLocation;
        
        if (!m_HomeLocation.endsWith(File.separator))
        {
            m_HomeLocation += File.separator;
        }
        
        ingestConfigFile();
    }
    
    
    /**
     *
     * Retrieves the location of the bin directory.
     *
     * @param pVal[out] The bin directory. A '\' is the
     *                  last character on the returned string
     *
     * @return HRESULT
     *
     */
    public String getHomeLocation()
    {
        return m_HomeLocation;
    }
    
    /**
     *
     * Retrieves the location of the license directory.
     *
     * @param pVal[out] The bin directory. A '\' is the
     *                  last character on the returned string
     *
     * @return HRESULT
     *
     */
    public String getLicenseLocation()
    {
        String loc = "";
        if (m_HomeLocation.length() > 0)
        {
            loc = m_HomeLocation;
            File f = new File(new File(loc).getParent(), "License"); // NOI18N
//			   loc += "..\\License\\";
            
            loc = f.toString();
        }
        return loc;
    }
    
    /**
     *
     * Retrieves the location of the docs directory.
     *
     * @param pVal[out] The docs directory. A '\' is the
     *                  last character on the returned string
     *
     * @return HRESULT
     *
     */
    public String getDocsLocation()
    {
        String loc = "";
        if (m_HomeLocation.length() > 0)
        {
            loc = m_HomeLocation;
            loc = StringUtilities.changeFinalDirectory(loc, "docs"); // NOI18N
            loc += File.separator;
        }
        return null;
    }
    
    /**
     *
     * Retrieves the location of the PresentationTypes.etc file.
     *
     * @param pVal[out] The absolute path to the file
     *
     * @return HRESULT
     *
     */
    public String getPresentationTypesLocation()
    {
        return getLocation( m_PresentationTypes );
    }
    
    /**
     *
     * Sets the name of the Presentation types file. The default is
     * "PresentationTypes.etc"
     *
     * @param newVal[in] The new name of the file.
     *
     * @return HRESULT
     *
     */
    public void setPresentationTypesLocation(String value)
    {
        m_PresentationTypes = value;
    }
    
    /**
     *
     * Retrieves the location of the StereotypeIcons.etc file.
     *
     * @param pVal[out] The absolute path to the file
     *
     * @return HRESULT
     *
     */
    public String getStereotypeIconsLocation()
    {
        return getLocation( m_StereotypeIconsLocation);
    }
    
    /**
     *
     * Sets the name of the StereotypeIcons file. The default is
     * "StereotypeIcons.etc"
     *
     * @param newVal[in] The new name of the file.
     *
     * @return HRESULT
     *
     */
    public void setStereotypeIconsLocation(String value)
    {
        m_StereotypeIconsLocation = value;
    }
    
    /**
     *
     * Retrieves the location of the EventFramework.etc file.
     *
     * @param pVal[out] The absolute path to the file
     *
     * @return HRESULT
     *
     */
    public String getEventFrameworkLocation()
    {
        return getLocation( m_EventFramework);
    }
    
    /**
     *
     * Sets the new name of the Event framework etc file. The default
     * is "EventFramework.etc"
     *
     * @param newVal[in] The new name
     *
     * @return HRESULT
     *
     */
    public void setEventFrameworkLocation(String value)
    {
        m_EventFramework = value;
    }
    
    /**
     *
     * Retrieves the location of the UML_2.0EMBT.dtd file.
     *
     * @param pVal[out] The absolute path to the file
     *
     * @return HRESULT
     *
     */
    public String getDTDLocation()
    {
        return getLocation( m_DTD);
    }
    
    /**
     *
     * Sets the name of the dtd file. The default is
     * "UML_2.0EMBT.dtd".
     *
     * @param newVal[in] The new name
     *
     * @return HRESULT
     *
     */
    public void setDTDLocation(String value)
    {
        m_DTD = value;
    }
    
    /**
     *
     * Retrieves the id of an element in a hive.
     *
     * @param hive[in] Name of the hive to pull from
     * @param name[in] Name of the element
     * @param theID[out] The found id
     *
     * @return HRESULT
     * @see GetIDs()
     *
     */
    public String getID(String hive, String name)
    {
        return getIDs( hive, name, null );
    }
    
    /**
     *
     * Retrieves the data associated with a particular entry in a hive.
     *
     * @param hive[in] The name of the hive
     * @param name[in] Name of the hive element
     * @param stdID[out] The id of the element
     * @param createID[in] The createTransition id of the element
     *
     * @return HRESULT
     *
     */
    public String getIDs(String hive, String name, StringBuffer createId)
    {
        String retStr = ""; // NOI18N
        Hashtable < String, HiveElement > hiveHash = m_Hives.get(hive);
        if (hiveHash != null)
        {
            HiveElement elem = hiveHash.get(name);
            if (elem != null)
            {
                retStr = elem.m_Id;
                if (createId != null && elem.m_CreateTransitionId != null)
                {
                    createId.delete(0, createId.length());
                    createId.append(elem.m_CreateTransitionId);
                }
            }
        }
        return retStr;
    }
    
    public String getPreferenceLocation()
    {
        return getLocation( m_Preferences);
    }
    
    /**
     *
     * Retrieves a full path using the HomeLocation as the root.
     *
     * @param fileName[in] The filename to append to the Home location
     * @param pVal[out] The result
     *
     * @return HRESULT
     *
     */
    private String getLocation(String fileName)
    {
        String retStr = ""; // NOI18N
        if( m_HomeLocation.length() > 0)
        {
            retStr = m_HomeLocation;
            
            retStr += "config" + File.separator; // NOI18N
            retStr += fileName;
            
        }
        return retStr;
    }
    
    public void setPreferenceLocation(String value)
    {
        m_Preferences = value;
    }
    
    /**
     *
     * Retrieves the full path to the config directory, which holds some of the
     * required xml files for Describe.
     *
     * @param fileName[in] The filename to append to the Home location
     * @param pVal[out] The result
     *
     * @return HRESULT
     *
     */
    public String getDefaultConfigLocation()
    {
        String retStr = ""; // NOI18N
        if (m_HomeLocation.length() > 0)
        {
            retStr = (new File(m_HomeLocation, "config")).toString(); // NOI18N
            if (!retStr.endsWith(File.separator))
            {
                retStr += File.separator;
            }
        }
        return retStr;
    }
    
    private void ingestConfigFile()
    {
        if (m_HomeLocation.length() > 0)
        {
            String config = getLocation("EssentialConfig.etc"); // NOI18N
            boolean valid = Validator.verifyFileExists(config);
            if (valid)
            {
                Document doc = XMLManip.getDOMDocument(config);
                if (doc != null)
                {
                    loadConfigFile(doc);
                }
            }
        }
    }
    
    private void loadConfigFile(Document doc)
    {
        loadTopLevelProperties(doc);
        loadHives(doc);
    }
    
    /**
     *
     * Loads the contents of found hives.
     *
     * @param doc[in] The document
     *
     * @return HRESULT
     *
     */
    private void loadHives(Document doc)
    {
        List hiveNodes = doc.selectNodes("//Hive"); // NOI18N
        if (hiveNodes != null && hiveNodes.size() > 0)
        {
            for (int i=0; i<hiveNodes.size(); i++)
            {
                loadHive((Node)hiveNodes.get(i));
            }
        }
    }
    
    /**
     *
     * Loads a particular hive
     *
     * @param hiveNode[in] The node containing a hive
     *
     * @return HRESULT
     *
     */
    private void loadHive(Node node)
    {
        try
        {
            List list = XMLManip.selectNodeList(node, "./HiveElement"); // NOI18N
            if (list != null && list.size() > 0)
            {
                Hashtable < String, HiveElement > hiveElements = new Hashtable < String, HiveElement > ();
                for (int i=0; i<list.size(); i++)
                {
                    Node n = (Node)list.get(i);
                    String name = XMLManip.getAttributeValue(n, "name"); // NOI18N
                    String id = XMLManip.getAttributeValue(n, "id"); // NOI18N
                    String createId = XMLManip.getAttributeValue(
                        n, "createTransitionID"); // NOI18N
                    if (name.length()>0 && id.length()>0)
                    {
                        HiveElement hive = new HiveElement(id, createId);
                        hiveElements.put(name, hive);
                    }
                }
                String hiveName = XMLManip.getAttributeValue(node, "name"); // NOI18N
                if (hiveName.length() > 0)
                {
                    m_Hives.put(hiveName, hiveElements);
                }
            }
        }
        catch (Exception e)
        {
        }
    }
    
    /**
     *
     * Retrieves the data associated with some top level properties
     * in the .etc file.
     *
     * @param doc[in] The doc
     *
     * @return HRESULT
     *
     */
    private void loadTopLevelProperties(Document doc)
    {
        m_StereotypeIconsLocation = getTopLevelProperty(
            doc, "/EssentialConfig/StereotypeIcons" ); // NOI18N
        m_PresentationTypes = getTopLevelProperty(
            doc, "/EssentialConfig/PresentationTypes" ); // NOI18N
        m_DefaultResources = getTopLevelProperty( 
            doc, "/EssentialConfig/DefaultResources"); // NOI18N
        m_OverriddenResources = getTopLevelProperty( 
            doc, "/EssentialConfig/OverriddenResources"); // NOI18N
        m_Preferences = getTopLevelProperty( 
            doc, "/EssentialConfig/Preferences" ); // NOI18N
        m_EventFramework = getTopLevelProperty( 
            doc, "/EssentialConfig/EventFramework" ); // NOI18N
        m_DTD = getTopLevelProperty( doc, "/EssentialConfig/DTD" ); // NOI18N
    }
    
    /**
     *
     * Utility to retrieve the top level properties.
     *
     * @param doc[in] The document to query
     * @param query[in] The query to perform
     * @param value[out] The value found
     *
     * @return HRESULT
     *
     */
    private String getTopLevelProperty(Document doc, String query)
    {
        String value = "";
        try
        {
            org.dom4j.Node n = XMLManip.selectSingleNode(doc, query);
            if (n != null)
            {
                value = XMLManip.getAttributeValue(n, "href"); // NOI18N
            }
        }
        catch (Exception e)
        {
        }
        return value;
    }
    
    public void setDefaultResourcesLocation(String newVal)
    {
        m_DefaultResources = newVal;
    }
    
    public String getDefaultResourcesLocation()
    {
        return getLocation(m_DefaultResources);
    }
    
    public void setOverriddenResourcesLocation(String newVal)
    {
        m_OverriddenResources = newVal;
    }
    
    public String getOverriddenResourcesLocation()
    {
        return getLocation(m_OverriddenResources);
    }
}
