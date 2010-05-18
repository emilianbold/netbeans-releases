/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class FacilityManager implements IFacilityManager
{
    String m_ConfigurationFile;
    String name;
    HashMap<String, IFacility> m_FacilityMap = new HashMap<String, IFacility>();
    HashMap<String, IFacilityManager> m_ManagerMap = new HashMap<String, IFacilityManager>();

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager#retrieveFacility(java.lang.String)
	 */
	public IFacility retrieveFacility(String name) 
    {
        if(name == null) return null;


        // If there is a '.' in the facility name then the facility is defined in a 
        // sub facility.
        int pos = 0;

        if((pos = name.indexOf(".")) != -1)
        {
            String managerName = name.substring(0, pos);
            String facilityName = name.substring(pos + 1);
            return retrieveFromSubManager(managerName, facilityName);
        }
        else
        {
            return retrieveFromManager(name);
        }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager#getConfigurationFile()
	 */
	public String getConfigurationFile() {
		return m_ConfigurationFile;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager#setConfigurationFile(java.lang.String)
	 */
	public void setConfigurationFile(String value) {
        if(value == null) return;
        
        m_ConfigurationFile = getConfigLocation(value);
        initializeManager(m_ConfigurationFile);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager#getFacilityNames()
	 */
	public IStrings getFacilityNames() {
        
        IStrings pFacilityNames = new Strings();
        getFacilityNames(pFacilityNames);
        return getSubFacilityNames(pFacilityNames);
	}
    
    /**
     * Retrieve the names of all the facilities in a configuration file.
     * 
     * @param pFacilityNames [in] The collection to be filled
     *
     * @return 
     *
     */

    private IStrings getFacilityNames(IStrings pFacilityNames)
    {
        if(pFacilityNames == null) return null;

        Document pConfigDoc = getConfigurationDocument();
      
        if(pConfigDoc != null)
        {
            String xpath = "//Facility";
         
            List pNodeList = XMLManip.selectNodeList(pConfigDoc, xpath);
         
            if(pNodeList != null)
            {
                int max = pNodeList.size();            
                for(int index = 0; index < max; index++)
                {
                    Node pNode = (Node)pNodeList.get(index);
               
                    if(pNode != null)
                    {
                        String name = XMLManip.getAttributeValue(pNode, "name");
                        if(name != null)
                            pFacilityNames.add(name);
                    }
                }
            }
        }
        return pFacilityNames;
    }
        
    /**
     *
     * Determines the location of the configuration file.  If the specified
     * filename is an absolute path to the configuration file then the 
     * supplied file name is returned.  Otherwise the file is assumed to live
     * in the home location of the perference manager.
     *
     * @param filename [in] The name of the configuration file
     * @return String location
     *
     */
    private String getConfigLocation(String fileName)
    {
        if(fileName == null) return null;
        
        File cfg = new File(fileName);
        
        if(cfg.isAbsolute()) return fileName;
        
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        if(prod != null)
        {
            IConfigManager configMan = prod.getConfigManager();
            if(configMan != null)
            {
                String homeLocation = configMan.getDefaultConfigLocation();
 
                if( homeLocation != null && homeLocation.trim().length() > 0)
                {
                    File home = new File(homeLocation,fileName);
                    return home.getAbsolutePath();
                }
            }        
        }
        return null;
    }
    /**
     * Retrieves an instance of the facility.  The CLSID that is to be created
     * is specified in the XML config file.  The <I>progid</I> attribute specifies
     * CoClass to create.
     *
     * @param name [in] The name of facility
     * @param pFacilityNode [in] The XML node that contains the facility information
     * @param pFacility [in] The facility to update
     */
    private IFacility getFacilityInstance(String name, Node facilityNode)
    {
        if (facilityNode == null) return null;
        
        // Create the specified facility.  The CoClass to create is specified in the Facility
        // XML configuration file.         
        String classname = XMLManip.getAttributeValue(facilityNode, "progid");
        try
        {
            return (IFacility) Class.forName(classname).newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the XML Document that is the Facility configuration.
     * 
     * @param pVal [out] The configuraton document
     */
    private Document getConfigurationDocument()
    {
        return XMLManip.getDOMDocument(getConfigurationFile());
    }

    /**
     * Initializes a facility with it's description.  The description
     * is retrieved from an XML configuration file.
     * 
     * @param manip [in] XML helper
     * @param pFacilityNode [in] The XML node that contains the facility information
     * @param pFacility [in] The facility to update
     */
    private IFacility getDescription(Node pFacilityNode, IFacility pFacility)
    {
        if(pFacility == null || pFacilityNode == null)
            return null;
    
        String desc = XMLManip.retrieveNodeTextValue(pFacilityNode, "Description");
        if(desc != null)
            pFacility.setDescription(desc);
        return pFacility;
    }
    
    /**
     * Initializes a facility with it's properties.  The properties information
     * is retrieved from a XML configuration file.
     * 
     * @param manip [in] XML helper
     * @param pFacilityNode [in] The XML node that contains the facility information
     * @param pFacility [in] The facility to update
     */
    private void getProperties(Node pFacilityNode, 
                                            IFacility pFacility)
    {
        if(pFacilityNode == null || pFacility == null)
            return;

        String xpath = "Properties/Property";
        List pNodeList = XMLManip.selectNodeList(pFacilityNode, xpath);

        if(pNodeList != null)
        {
            int max = pNodeList.size();
            for(int i = 0; i < max; i++)
            {
                Node pNode = (Node)pNodeList.get(i);
            
                if(pNode != null)
                {
                    String propertyName = XMLManip.getAttributeValue(pNode, "name");
                    String propertyValue = XMLManip.getAttributeValue(pNode, "value");
                    pFacility.addProperty(propertyName, propertyValue);
                }
            }
        }
    }
    
    /**
     * Creates and initializes an instance of a facility.  The facility details are 
     * retrieved from a XML file.
     * 
     * @param name [in] The name of the facility
     * @param pFacilityNode [in] The XML node that contains the facility information
     * @param pVal [out] The created facility
     */
    private IFacility retrieveFacilityDetails(String name, Node pFacilityNode)
    {
        if(pFacilityNode == null) return null;

        IFacility pNewFac = getFacilityInstance(name, pFacilityNode);
        getProperties(pFacilityNode, pNewFac); 
        getDescription(pFacilityNode, pNewFac);
        return pNewFac;
    }
    
     
    /**
     * Loads all of the sub facilities manager that are defined in a
     * facility configuration file.
     *
     * @param pConfigFile [in] The facility manager config XML document
     */

    private void loadSubFacilityManagers(Document pConfigFile)
    {
       if(pConfigFile == null) return;

       List pNodeList = XMLManip.selectNodeList(pConfigFile, "//FacilityManager");

        if(pNodeList != null)
        {
            int max = pNodeList.size();
            for(int i = 0; i < max; i++)
            {
                Node pNode = (Node)pNodeList.get(i);            
                if(pNode != null)
                {
                    String managerName = XMLManip.getAttributeValue(pNode, "name");
                    String managerConfigFile = XMLManip.getAttributeValue(pNode, "config");
            
                    IFacilityManager pManager = new FacilityManager();
                    pManager.setConfigurationFile(managerConfigFile);
                    pManager.setName(managerName);
                    m_ManagerMap.put(managerName, pManager);
                }
            }
        }
    }
    
    /**
     * Retrieves the specified facility from a sub facility manager.  When the name contains
     * a <i>'.'</i> character then the facility is retrieve from a child manager.
     * 
     * @param managerName [in] The name of the child manager
     * @param facilityName [in] The name of the facility
     * @param pVal [out] The facility
     */
    private IFacility retrieveFromSubManager(String managerName, String facilityName)
    {
        if(managerName == null || facilityName == null) return null;
        
        IFacilityManager pManager 
                = (IFacilityManager)m_ManagerMap.get(managerName);
                
        if(pManager != null)
        {
             return pManager.retrieveFacility(facilityName);
        }
        else return null;
    }  
    
    /**
     * Checks if the facility is specified as a singleton.
     *
     * @param pFacilityNode [in] The XML node that contains the facility information
     * @return true if the facility is a singleton, false otherwise.
     */
    private boolean isSingleton(Node pFacilityNode)
    {
        if(pFacilityNode == null) return false;

        return Boolean.valueOf(XMLManip.getAttributeValue(
                                        pFacilityNode, "singleton"))
                      .booleanValue();
    }
    
    /**
     * Checks in the map of facilities that have already been loaded.  
     * 
     * @param name [in] The name of the facility
     * @param pVal [out] The facility
     */
    private IFacility lookupFacility(String name)
    {
        if(name == null) return null;
       
        return m_FacilityMap.get(name);
    }
    
    /**
     * Retrieves the specified facility from the manager.  If the facility has already
     * be initialized then retrieve it.  If the faciltiy has not already been initialized,
     * create an instance of the facility and initialize the properties.
     * 
     * @param name [in] The name of the facility
     * @param pVal [out] The facility
     */
    private IFacility retrieveFromManager(String name)
    {
        IFacility pFacility = lookupFacility(name);

        // If the lookup failed to retrieve the facility then find the facility
        // in the configuration file and retrieve its details.
        if(pFacility == null)
        {
            Document pConfigDoc = getConfigurationDocument();

            if(pConfigDoc != null)
            {
                String xpath = "//Facility[@name=\"" + name + "\"]";

                Node pNode = XMLManip.selectSingleNode(pConfigDoc, xpath);

                if(pNode != null)
                {
                    pFacility = retrieveFacilityDetails(name, pNode);

                    if(pFacility != null && isSingleton(pNode) == true)
                    {
                        m_FacilityMap.put(name, pFacility);
                    }  
                }
            }
        }
        return pFacility;
    }
    
    /**
     *
     * Populates this manager with the contents of the given
     * configuration file
     *
     * @return HRESULT
     *
     */
    private void initializeManager(String configLoc)
    {
        if(configLoc != null)
        {
            Document pDoc = XMLManip.getDOMDocument(m_ConfigurationFile);

            if(pDoc != null)
            {
                loadSubFacilityManagers(pDoc);
            }
        }
    }
    
    /**
     * Retrieves the names of the subfacilities defined in a configuration 
     * file.
     *
     * @param pFacilityNames[in] The names of the facility contexts
     */

    private IStrings getSubFacilityNames(IStrings pFacilityNames)
    {
        if(pFacilityNames == null) return null;

        Collection col = m_ManagerMap.values();
        if(col != null)
        {
            Iterator iter = col.iterator();
            IFacilityManager pManager;
            String separator = ".";
            while(iter.hasNext())
            {
                pManager = (IFacilityManager)iter.next();
                if(pManager != null)
                {
                    IStrings pSubFacilityNames = pManager.getFacilityNames();
                    String managerName = pManager.getName();
                    if(pSubFacilityNames != null)
                    {
                        int count = pSubFacilityNames.getCount();
                        for(int index = 0; index < count; index++)
                        {
                            String name = pSubFacilityNames.item(index);
      
                            if(name != null)
                            {
                                String facilityName = managerName;
                                facilityName += separator;
                                facilityName += name;
         
                                pFacilityNames.add(facilityName);
                            }
                       }
                    }
                 }
            }
        }
        return pFacilityNames;
    }
    
    public void setName(String nm)      
    {
        name = nm;
    }
    
    public String getName()
    {
        return name;
    }
}


