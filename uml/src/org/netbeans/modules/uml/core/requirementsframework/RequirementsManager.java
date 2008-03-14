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


/*
 * RequirementsManager.java
 *
 * Created on June 24, 2004, 2:23 PM
 */

package org.netbeans.modules.uml.core.requirementsframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Trey Spiva
 */
public class RequirementsManager /*extends AddInManagerImpl*/ implements IRequirementsManager
{
   private ResourceBundle m_Bundle = ResourceBundle.getBundle("org/netbeans/modules/uml/core/requirementsframework/Bundle");

   /** DOM document to load the RequirementSources.etrp file into. */
   protected Document m_ReqProxyDoc;

   /** Path and name of the RequirementSources.etd file */
   protected String m_ReqSourcesFile;

   /** Path and name of the RequirementSources.etrp file */
   protected String m_ReqProxyFile;

   /** DOM document to load the RequirementSources.etd file into. */
   private Document m_ReqSourcesDoc;
   
   private HashMap < String, IRequirementsProvider > mAddins = new HashMap < String, IRequirementsProvider >();

   public RequirementsManager()
   {
      initialize();
   }
   
   /**
	 *
	 * Initialize the requirements manager.  The layer file repository will 
    * be searched for all registered requiremennts addins.
	 *
	 * @return HRESULT
	 *
	 */
	public void initialize()
	{
      
      IRequirementsProvider[] addins = getAddinsFromRegistry("UML/requirements");
      for(IRequirementsProvider curAddin : addins)
      {
         String progID = curAddin.getProgID();
         mAddins.put(progID, curAddin);
      }
      
	}
   
   /**
    * Retrieves all of the reqistered requirements IAddIn instances
    */
   public IRequirementsProvider[] getAddIns()
   {
      IRequirementsProvider[] retVal = null;
      
      Collection < IRequirementsProvider > addins = mAddins.values();
      if(addins.size() > 0)
      {
         retVal = new IRequirementsProvider[addins.size()];
         addins.toArray(retVal);
      }
      else
      {
         retVal = new IRequirementsProvider[0];
      }
      
      return retVal;
   }
   
   /**
	 * The requirements manager knows about the requirements addins.
	 * This routine retrieves a particular addin based on the prog id passed in.
	 * 
	 *
	 * @param sProgID[in]		The prog id of the addin to get
	 * @param pAddIn[out]		The found add in
	 *
	 * @return HRESULT
	 *
	 */
	public IRequirementsProvider getRequirementsProvider(String sProgID) 
	{
		IRequirementsProvider pIn = mAddins.get(sProgID);
		return pIn;
	}
   
   /**
    *
    * Callback from ReqProxyManager, when the "Display Sources" button is pressed
    * on the Requirements Provider Dialog. The ReqProviderDialogImpl is in
    * the RequirementsFramework project. See RequirementsManagerImpl::PopulateTreeItem
    * for how the dialog is created and this requirements manager is set to 
    * recieve callbacks from the dialog.
    *
    * @param desc[in] RequirementSource for the addin selected in the Requirements Provider dialog.
    *
    * @return HRESULT
    *
    */
   public void processSource(IRequirementSource requirementSource) throws RequirementsException
   {
      boolean fileLoaded = getReqSourcesDoc();
      if (m_ReqSourcesDoc != null)
      {
         if (fileLoaded == false)
         {
            initializeReqSourcesDoc(m_ReqSourcesDoc);
         }

         String location = requirementSource.getLocation();
         String pattern = "RequirementSources/RequirementSource[@location='" + location + "']";

         Node requirementSourceNode = XMLManip.selectSingleNode(m_ReqSourcesDoc, pattern);
         if (requirementSourceNode != null)
         {
            throw new RequirementsException(RequirementsException.RP_E_DUPLICATESOURCE,      
                                            m_Bundle.getString("IDS_DUPLICATESOURCE"));
         }
         else
         {
             // check for duplicate display name as well
             // fix for bug 5107636
             String displayName = requirementSource.getDisplayName();
             pattern = "RequirementSources/RequirementSource[@displayName='" + displayName + "']";
             requirementSourceNode = XMLManip.selectSingleNode(m_ReqSourcesDoc, pattern);    
             if (requirementSourceNode != null)
             {
                throw new RequirementsException(RequirementsException.RP_E_DUPLICATESOURCE,      
                                                m_Bundle.getString("IDS_DUPLICATESOURCE"));
             }
             else
             {
                Element sourceElement = convertReqSourceToXMLElement(m_ReqSourcesDoc, requirementSource);
                if (sourceElement != null)
                {
                   //addReqSourceToDoc(m_ReqSourcesDoc, sourceElement);
                   saveReqSourcesDoc();
                }
             }
         }
      }
   }

   /**
    *
    * Given a RequirementSourceID, read the etd file and return the 
    * RequirementSource.
    *
    * @param strRequirementSourceID RequirementSource Identifier
    * @return RequirementSource 
    *
    */
   public IRequirementSource getSource(String strRequirementSourceID)
   {
      IRequirementSource requirementSource = null;
      
      boolean bFileLoaded = getReqSourcesDoc();
      if (m_ReqSourcesDoc != null)
      {         
         if (bFileLoaded != true)
         {
            initializeReqSourcesDoc(m_ReqSourcesDoc);
         }
         // Find the RequirementSourceID
         String strPattern = "RequirementSources/RequirementSource[@id='";
         strPattern += strRequirementSourceID;
         strPattern += "']";

         Node requirementSourceNode = m_ReqSourcesDoc.selectSingleNode( strPattern );

         if ( requirementSourceNode != null )
         {
            requirementSource = new RequirementSource();

            requirementSource.setID(strRequirementSourceID);

            if (requirementSourceNode instanceof Element)
            {
               Element sourceElement = (Element)requirementSourceNode;

               String strLocation = sourceElement.attributeValue("location");
               if (strLocation != null)
               {
                  requirementSource.setLocation(strLocation);
               }
            }
         }
      }

      return requirementSource;
   }

   /**
    *
    * Callback from ReqProxyManager when a Requirement(s) has been dropped on a 
    * presentation element.  Creates <code><RequirementProxy><Requirement>
    * <Satisfiers><Satisfier> </code> nodes as needed.
    *
    * @param requirement The requirement.
    * @param satisfier The requirements satisfier.
    *
    */
   public void processProxy(IRequirement requirement, ISatisfier satisfier)
   {
      String strRequirementSourceID = requirement.getSourceID();
      String strRequirementID = requirement.getID();
      String strRequirementName = requirement.getName();

      String strSatisfierID = satisfier.getXMIID();
      String strSatisfierName = satisfier.getName();
      String strProjectName = satisfier.getProjectName();
      String strProjectID = satisfier.getProjectID();

      boolean bFileLoaded = getReqProxyDoc();
      if (m_ReqProxyDoc != null)
      {     
         if(bFileLoaded == false)
         {
            // File does NOT exist: Add the XML declaration and all child nodes.
            //m_ReqProxyDoc.addProcessingInstruction("xml", "version = '1.0' ");

            Element reqProxiesElement = XMLManip.createElement( m_ReqProxyDoc, "RequirementProxies" );

            // Append RequirementProxy element and set source equal to Provider's RequirementSourceID.
            if (reqProxiesElement != null)
            {
               Element reqProxyElement = XMLManip.createElement( reqProxiesElement, "RequirementProxy");

               // Append RequirementProxy element and set source equal to Provider's RequirementSourceID.
               if (reqProxyElement != null)
               {
                  reqProxyElement.addAttribute("source", strRequirementSourceID);

                  Element requirementElement = XMLManip.createElement( reqProxyElement, "Requirement");

                  // Append Requirement element and set RequirementID and Name.
                  if ( requirementElement != null )
                  {
                     requirementElement.addAttribute("id", strRequirementID);
                     requirementElement.addAttribute("name", strRequirementName);

                     Element satisfiersElement = XMLManip.createElement( requirementElement, "Satisfiers");

                     // Append <Satisfiers> container element. 
                     if (satisfiersElement != null)
                     {
                        Element satisfierElement = XMLManip.createElement( satisfiersElement, "Satisfier");

                        // Add Satisfier element with Name and XMIID of the Model element.
                        if (satisfierElement != null)
                        {
                           satisfierElement.addAttribute("name", strSatisfierName);
                           satisfierElement.addAttribute("xmiid", strSatisfierID);
                           satisfierElement.addAttribute("projectname", strProjectName);
                           satisfierElement.addAttribute("projectid", strProjectID);
                        }
                     }
                  }
               }
            }
         
            updateProxyFileLocation(strRequirementSourceID);
         }
         else
         {
            // The Proxy file exists. Repeatedly call findOrCreate(...) for each element in 
            // <RequirementProxies><RequirementProxy><Requirement><Satisfiers><Satisfier>.

            Element reqProxiesElement = findOrCreateElement(m_ReqProxyDoc, "RequirementProxies", "", "");

            if (reqProxiesElement != null)
            {
               Element reqProxyElement = findOrCreateElement(reqProxiesElement, "RequirementProxy", "source", strRequirementSourceID);

               if (reqProxyElement != null )
               {
                  reqProxyElement.addAttribute("source", strRequirementSourceID );
                  Element requirementElement = findOrCreateElement(reqProxyElement, "Requirement", "id", strRequirementID);

                  if (requirementElement != null)
                  {
                     requirementElement.addAttribute("id", strRequirementID);
                     requirementElement.addAttribute("name", strRequirementName);

                     Element satisfiersElement = findOrCreateElement(requirementElement, "Satisfiers", "", "");

                     if (satisfiersElement != null)
                     {
                        Element satisfierElement = findOrCreateElement(satisfiersElement, "Satisfier", "xmiid", strSatisfierID);

                        if (satisfierElement != null)
                        {
                           satisfierElement.addAttribute("name", strSatisfierName);
                           satisfierElement.addAttribute("xmiid", strSatisfierID);
                           satisfierElement.addAttribute("projectname", strProjectName);
                           satisfierElement.addAttribute("projectid", strProjectID);
                        }
                     }
                  }

               }
            }
         }
         XMLManip.save(m_ReqProxyDoc,m_ReqProxyFile);
      }
   }

   /**
    *
    * Callback from ReqProxyManager when a Requirement(s) has been dropped on a presentation
    * element.  Creates <RequirementProxy><Requirement><Satisfiers><Satisfier> nodes as
    * needed.
    *
    * @param strRequirementSourceID[in] RequirementSourceID of the Provider.
    * @param strRequirementID[in] RequirementID. 
    * @param strRequirementName[in] RequirementName.
    * @param strSatisfierID[in] SatisfierID of the Presentation element. - Not Used Yet, need to be
    *                            able to retrieve this from the TreeItem.
    * @param strSatisfierName[in] SatisfierName of the Presentation element.
    * 
    * @return HRESULT
    *
    */
   public void deleteProxy(IRequirement requirement, ISatisfier satisfier)
   {
      String strRequirementSourceID = requirement.getSourceID();
      String strRequirementID = requirement.getID();

      String strSatisfierID = satisfier.getXMIID();

      boolean bFileLoaded = getReqProxyDoc();

      if (m_ReqProxyDoc != null)
      {
         if (bFileLoaded != true)
         {
            // TODO: Cannot maintain proxy if it is inaccessible.
         }
         else
         {
            // Get the <RequiremenProxy> element matching the RequirementSource/.
            String strPattern = "RequirementProxies/RequirementProxy[@source='";
            strPattern += strRequirementSourceID;
            strPattern += "']";

            Node requirementProxyNode = m_ReqProxyDoc.selectSingleNode(strPattern);
            if (requirementProxyNode != null)
            {
               // Get the <Requirement> Node.
               strPattern = "Requirement[@id='";
               strPattern += strRequirementID;
               strPattern += "']";

               Node requirementNode = requirementProxyNode.selectSingleNode(strPattern);
               if (requirementNode != null)
               {
                  // Get the <Satisfiers> container node.
                  strPattern = "Satisfiers";

                  Node satisfiersNode = requirementNode.selectSingleNode(strPattern);
                  if (satisfiersNode != null)
                  {
                     // Get the <Satisfier>: currently matching on name, switch to xmiid when available.
                     strPattern = "Satisfier[@xmiid=\"";
                     strPattern += strSatisfierID;
                     strPattern += "\"]";

                     Node satisfierNode = satisfiersNode.selectSingleNode(strPattern);

                     if (satisfierNode != null)
                     {
                        // Delete the Satisfier from the Satisfiers container.
                        satisfierNode.detach();

                        // Save the Proxy XML file.   
                        XMLManip.save(m_ReqProxyDoc,m_ReqProxyFile);
                     }
                  }
               }
            }
         }
      }
   }

   ////////////////////////////////////////////////////////////////////////////////////////////////////   

   /**
    *
    * Creates an XML element for the passed in IAddInDescriptor and writes it to
    * the RequirementSources.etd file.
    *
    * @param *bFileLoaded[out] Returns true if the RequirementSources.etd file is loaded.
    *
    * @return void
    *
    */
   protected boolean getReqSourcesDoc()
   {
      boolean bFileLoaded = true;

      if (null == m_ReqSourcesDoc)
      {
         // Load the RequirementSources.etd file into the DOM. The .etd file's
         // elements contains AddIn data for each requirement provider.
         String strFile = getReqSourcesFile();
         m_ReqSourcesDoc = XMLManip.getDOMDocument( strFile );
         
         // The requiriements file must not exist yet so lets just create a new
         // documents.
         if(m_ReqSourcesDoc == null)
         {
            m_ReqSourcesDoc = XMLManip.getDOMDocument();
            bFileLoaded = false;
         }
      }

      return bFileLoaded;
   }

   /**
    *
    * Gets or creates the .RequirementSources.etrp proxy file
    *
    * @param *bFileLoaded[out] Returns true if the RequirementSources.etrp file is loaded.
    *
    * @return void
    *
    */
   protected boolean getReqProxyDoc()
   {
      boolean bFileLoaded = true;

      if (null == m_ReqProxyDoc)
      {
         // Load the RequirementSources.etrp file into the DOM. The .etd file's
         // elements contains AddIn data for each requirement provider.

         String strFile = "";
         if ((m_ReqProxyFile != null) && (m_ReqProxyFile.length() > 0))
         {
            strFile = m_ReqProxyFile;
         }
         else
         {
            String strConfigDir = getConfigDir();
            if (strConfigDir.length() > 0)
            {
               strFile = strConfigDir + "RequirementSources.etrp";
               m_ReqProxyFile = strFile;
            }
         }

         m_ReqProxyDoc = XMLManip.getDOMDocument(strFile);
         
         // The requiriements file must not exist yet so lets just create a new
         // documents.
         if(m_ReqProxyDoc == null)
         {
            m_ReqProxyDoc = XMLManip.getDOMDocument();
            
            if(m_ReqProxyDoc.getRootElement() != null)
            {
               m_ReqProxyDoc.getRootElement().detach();
            }
            bFileLoaded = false;
         }
         
      }

      return bFileLoaded;
   }

   /**
    * Updates the location of the proxy file in the RequirementSources.etd document
    *
    * @param *bFileLoaded[out] Returns true if the RequirementSources.etrp file is loaded.
    *
    * @return void
    *
    */
   protected void updateProxyFileLocation(String strRequirementSourceID)
   {

      if (m_ReqSourcesDoc != null)
      {
         boolean bFileLoaded = getReqSourcesDoc();
         if (bFileLoaded == true)
         {
            String strPattern = "RequirementSources/RequirementSource";
            strPattern += "[@id='";
            strPattern += strRequirementSourceID;
            strPattern += "']";

            // Update the <proxyFile> location element in the .etd file
            Node reqSourceNode = m_ReqSourcesDoc.selectSingleNode(strPattern);
            if (reqSourceNode instanceof Element)
            {
               Element reqSourceElement = (Element)reqSourceNode;
               if (reqSourceElement != null)
               {
                  reqSourceElement.addAttribute("proxyFile", m_ReqProxyFile);
                  saveReqSourcesDoc();
               }
            }
         }
      }
   }

   /**
    *
    * Creates the initial elements for a RequirementSources.etd document.
    *
    * @param *document[in] XML Document to initialize.
    *
    * @return void
    *
    */
   protected void initializeReqSourcesDoc(Document document)
   {
      if (null == document)
         throw new IllegalArgumentException();

      // Add the XML declaration
      //document.addProcessingInstruction("xml", "version = '1.0' ");
      
      // I want the RequirementSources to be the root element.
      Element root = document.getRootElement();
      if(root != null)
      {
         root.detach();
      }
      Element reqSourcesElem = XMLManip.createElement(document, "RequirementSources");
//      if (reqSourcesElem != null)
//      {
//         document.getRootElement().add(reqSourcesElem);
//      }
   }
   /**
    *
    * Returns the path and file name for the RequirementSources.etd file,
    * which contains data for the available requirement provider addins.
    *
    * @return String Location the RequirementsSources.etd file.
    *
    */
   protected String getReqSourcesFile()
   {
      String strFile = "";

      if ((m_ReqSourcesFile != null) && (m_ReqSourcesFile.length() > 0))
      {
         strFile = m_ReqSourcesFile;
      }
      else
      {
         String strConfigDir = getConfigDir();
         if (strConfigDir.length() > 0)
         {
            strFile = strConfigDir + "RequirementSources.etd";
         }
      }
      return strFile;
   }

   protected String getConfigDir()
   {
      String strDir = "";

      ICoreProduct product = ProductRetriever.retrieveProduct();
      if (product != null)
      {
         IConfigManager configMgr = product.getConfigManager();

         if (configMgr != null)
         {
            String strLoc = configMgr.getDefaultConfigLocation();
            if (strLoc != null)
            {
               strDir = strLoc;
            }
         }
      }

      return strDir;
   }

   /**
    *
    * Create a <RequirementSource> element for the RequirementSources.etd file, from an
    * IRequriementsSource class.
    *
    * @param *document[in] XML Document to create the element for.
    * @param *requirementSource[in] Describe IRequirementSource that contains the data. 
    * @param *sourceElem[out] Returns the new XML Element.
    *
    * @return void
    *
    */
   protected Element convertReqSourceToXMLElement(Document document, IRequirementSource requirementSource)
   {
      if (null == document)
         throw new IllegalArgumentException();
      if (null == requirementSource)
         throw new IllegalArgumentException();

      String strPattern = "/RequirementSources";
      Node reqSourcesNode = document.selectSingleNode(strPattern);
      
      Element sourceElem = null;
      if (reqSourcesNode instanceof Element)
      {
         Element sourcesBranch = (Element)reqSourcesNode;                  
         sourceElem = XMLManip.createElement(sourcesBranch, "RequirementSource");
         if (sourceElem != null)
         {
            // Set the id for the element.
            String strID = requirementSource.getID();
            sourceElem.addAttribute("id", strID);

            // Set the display name attr for the element
            String strDisplayName = requirementSource.getDisplayName();
            sourceElem.addAttribute("displayName", strDisplayName);

            // Set the provider (addin Prog_ID) attr for the element
            String strProgID = requirementSource.getProvider();
            sourceElem.addAttribute("provider", strProgID);

            // Set the location of the provider's requirements file.
            String strLocation = requirementSource.getLocation();
            sourceElem.addAttribute("location", strLocation);

            // Set whether a login is required for this requirement source.
            String strRequiresLogin;
            if (requirementSource.getRequiresLogin())
            {
               strRequiresLogin = "True";
            }
            else
            {
               strRequiresLogin = "False";
            }
            sourceElem.addAttribute("loginRequired", strRequiresLogin);

            // Set the location of the proxy file (which connects Describe symbols with the
            // requirements that they fullfill).
            String strProxyFile = requirementSource.getProxyFile();
            sourceElem.addAttribute("proxyFile", strProxyFile);
         }
      }

      return sourceElem;
   }

   /**
    *
    * Insert the passed in <RequirementSource> element to the RequirementsSources.etd file.
    *
    * @param *document[in] The XML document to add the passed in element to.
    * @param *sourceElem[in] The XML element to add to the document.
    *
    * @return void
    *
    */
   protected void addReqSourceToDoc(Document document, Element sourceElem)
   {
      if (null == document)
         throw new IllegalArgumentException();
      if (null == sourceElem)
         throw new IllegalArgumentException();

      //ptemp - do we want to make sure there are no duplicate provider names
      //        under "Requirements" ??
      String strPattern = "/RequirementSources";
      Node reqSourcesNode = document.selectSingleNode(strPattern);
      
      if (reqSourcesNode instanceof Branch)
      {
         Branch sourcesBranch = (Branch)reqSourcesNode;

         sourceElem.detach();         
         sourcesBranch.add(sourceElem);
      }
   }

   /**
    *
    * Looks for a child node on the parent. Returns found node or newly created node.
    *
    * @param parentNode[in] Node where search will start from. 
    * @param childElement[in,out] The found or newly created node.
    * @param strNodeName[in] Node Name
    * @param strAttributeName[in] Attribute name ( if null, will match on first selected node eq to node name.
    * @param strAttributeValue[in] Attribute Value, used in conjunction with non null attribute name.
    *
    * @return void
    *
    */
   protected Element findOrCreateElement(Node parentNode, String strNodeName, String strAttributeName, String strAttributeValue)
   {
      Element childElement = null;

      String strPattern = strNodeName;

      // Search for node equal to input arg's node name.  Optionally match on attribute.
      if (strAttributeName.length() > 0)
      {
         strPattern += "[@";
         strPattern += strAttributeName;
         strPattern += "='";
         strPattern += strAttributeValue;
         strPattern += "']";
      }

      Node node = parentNode.selectSingleNode(strPattern);
      if (node != null)
      {
         // Return found node.
         if (node instanceof Element)
         {
            childElement = (Element)node;
         }
      }
      else
      {
         // Create new node and append to the parent. 
         if(parentNode instanceof Element)
         {
            childElement = XMLManip.createElement( (Element)parentNode, strNodeName);
         }
         else
         {
            childElement = XMLManip.createElement( m_ReqProxyDoc, strNodeName);
         }
      }

      return childElement;
   }

   /**
    *
    * Saves the RequirementSources.etd document to disk.
    *
    * @return void
    *
    */
   protected void saveReqSourcesDoc()
   {
      if (m_ReqSourcesDoc != null)
      {
         String strReqFile = getReqSourcesFile();
         if (strReqFile.length() > 0)
         {
            XMLManip.save(m_ReqSourcesDoc,strReqFile);
         }
      }
   }

   /**
    *
    * Load the passed in IRequriementsSource class from the passed in <RequirementSource>
    * element (element type that goes in the RequirementSources.etd file) .
    *
    * @param *sourceElem[in] The XML element that contains the Requirement Source data.
    * @param *requirementSource[in] The IRequirements object to fill with data.
    *
    * @return void
    *
   */
   protected void convertXMLElementToReqSource(Element sourceElem, IRequirementSource requirementSource)
   {
      if (null == sourceElem)
         throw new IllegalArgumentException();
      if (null == requirementSource)
         throw new IllegalArgumentException();

      String strValue;

      // Set the id of the requirement source.
      strValue = sourceElem.attributeValue("id");
      if (strValue != null)
      {
         requirementSource.setID(strValue);
      }

      // Set the displayed name of the requirement source.
      strValue = sourceElem.attributeValue("displayName");
      if (strValue != null)
      {
         requirementSource.setDisplayName(strValue);
      }

      // Set the provider (addin Prog_ID) of the requierment soruce.
      strValue = sourceElem.attributeValue("provider");
      if (strValue != null)
      {
         requirementSource.setProvider(strValue);
      }

      // Set the location of the requirement source's requirement file.
      strValue = sourceElem.attributeValue("location");
      if (strValue != null)
      {
         requirementSource.setLocation(strValue);
      }

      // Set whether a login is required for this requirement source.
      strValue = sourceElem.attributeValue("loginRequired");
      if (strValue != null)
      {
         boolean bLogin = strValue.equals("True");
         requirementSource.setRequiresLogin(bLogin);
      }

      // Set the location of the proxy file (which connects Describe symbols with the
      // requirements that they fullfill).
      strValue = sourceElem.attributeValue("proxyFile");
      if (strValue != null)
      {
         requirementSource.setProxyFile(strValue);
      }
   }
   
   /**
	 * The registry information that is retrieved from layer files to build
	 * the list of design center addins supported by this node.
	 *
	 * @param path The registry path that is used for the lookup.
	 * @return The list of addins in the path.  null will be used if when
	 *         seperators can be placed.
	 */
	protected IRequirementsProvider[] getAddinsFromRegistry(String path)
	{
		ArrayList < IRequirementsProvider > addins = new ArrayList < IRequirementsProvider >();
		FileSystem system = Repository.getDefault().getDefaultFileSystem();
		try
		{
			if(system != null)
			{
				org.openide.filesystems.FileObject lookupDir = system.findResource(path);
				if(lookupDir != null)
				{
					org.openide.filesystems.FileObject[] children = lookupDir.getChildren();
					
					for(FileObject curObj : children)
					{
						try
						{                     
							DataObject dObj = DataObject.find(curObj);
							if(dObj != null)
							{
								InstanceCookie cookie = (InstanceCookie)dObj.getCookie(InstanceCookie.class);
								if(cookie != null)
								{
									Object obj = cookie.instanceCreate();
									if(obj instanceof IRequirementsProvider)
									{
                              //String id = (String)curObj.getAttribute("id");
										addins.add((IRequirementsProvider)obj);
									}
								}
							}
						}
						catch(ClassNotFoundException e)
						{
							// Unable to create the instance for some reason.  So the
							// do not worry about adding the instance to the list.
						}
					}
				}
			}
		}
		catch(org.openide.loaders.DataObjectNotFoundException e)
		{
			// Basically Bail at this time.
		}
		catch(java.io.IOException ioE)
		{

		}
      
      IRequirementsProvider[] retVal = new IRequirementsProvider[addins.size()];
		addins.toArray(retVal);
		return retVal;
   }
}
