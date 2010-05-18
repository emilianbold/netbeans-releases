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

 /*
 * DoorsRequirement.java
 *
 * Created on July 2, 2004, 8:51 AM
 */

package org.netbeans.modules.uml.requirements.doorsprovider;

import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.ISatisfier;
import org.netbeans.modules.uml.core.requirementsframework.Satisfier;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.core.requirementsframework.RequirementUtility;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.IComparableTreeData;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Trey Spiva
 */
public class DoorsRequirement implements IDoorsRequirement, IComparableTreeData
{
   private boolean m_IsCategory = false;
   private String m_Name = "";
   private String m_ID = "";
   private String m_Description = "";
  	private String m_Type = "";
   private String m_ModName = "";
   private String m_ProjectName = "";
   private String m_ProviderID = "";
   private String m_SourceID = "";
   
   private ETList < IRequirement > m_Requirements = new ETArrayList < IRequirement >();
   private ETList < ISatisfier > m_Satisfiers = new ETArrayList < ISatisfier >();
   
   /** Creates a new instance of DoorsRequirement */
   public DoorsRequirement()
   {
   }
   
   /**
    *
    * A category has no requirement text, but does contain a group of requirements
    *
    * @return value of IsCategory Property.
    */
   public boolean isCategory()
   {
      return m_IsCategory;
   }
   
   /**
    * A category has no requirement text, but does contain a group of requirements
    *
    * @param newVal Value to set the IsCategory property to.
    */
   public void setIsCategory( boolean newVal )
   {
      m_IsCategory = newVal;
   }
   
   /**
    * Get the text of the requirement.
    *
    * @return The requirement text.
    */
   public String getDescription(  )
   {
      return m_Description;
   }
   
   /**
    *
    * Set the text of the requirement.
    *
    * @param newVal[in] The requirement text.
    *
    * @return void
    *
    */
   public void setDescription( String newVal )
   {
      m_Description = newVal;
   }
   
   public ETList < IRequirement > getRequirements()
   {
      return m_Requirements; 
   }
   
   public void setRequirements( ETList < IRequirement > pNewVal )
   {
      m_Requirements = pNewVal;
   }
   
   public ETList < ISatisfier > getSatisfiers()
   {
      return m_Satisfiers;
   }
   
   public void setSatisfiers( ETList < ISatisfier > pNewVal )
   {
      m_Satisfiers = pNewVal;
   }
   
   public String getName()
   {
      
      return m_Name;
   }
   
   public void setName(String newVal)
   {
      m_Name = newVal;
   }
   
   public String getID()
   {
      return m_ID;
   }
   
   public void setID(String newVal)
   {
      
      m_ID = newVal;
   }
   
   
   public String getType()
   {
      return m_Type;
      
   }
   
   public void setType(String newVal)
   {
      m_Type = newVal;
   }
   
   public String getModName()
   {
      return m_ModName;
   }
   
   public void setModName(String newVal)
   {
      m_ModName = newVal;
   }
   public String getProjectName()
   {
      return m_ProjectName;
   }
   
   public void setProjectName(String newVal)
   {
      m_ProjectName = newVal;
   }
   
   public String getProviderID()
   {
      return m_ProviderID;
   }
   
   public void setProviderID(String newVal)
   {
      m_ProviderID = newVal;
   }
   
   public String getSourceID()
   {
      return m_SourceID;
   }
   
   public void setSourceID(String newVal)
   {
      m_SourceID = newVal;
   }
   
   public void addSatisfier(ISatisfier pSatisfier)
      throws RequirementsException
   {
      if( null == pSatisfier)
      {
         throw new IllegalArgumentException();
      }
         
      String strSatisfierXMIID = pSatisfier.getXMIID();
      boolean bSatisfierExists = false;

      // Create member collection as necessary, then add to it..
      if( m_Satisfiers.size() > 0 )
      {               
         Iterator < ISatisfier > iter = m_Satisfiers.iterator();
         while( iter.hasNext() == true )
         {
            ISatisfier  cpSatisfier = iter.next();

            String strSatisfierID = cpSatisfier.getXMIID();

            if( strSatisfierID.equals(strSatisfierXMIID) == true )
            {
               bSatisfierExists = true;
               break;
            }
         }
      }

      if(bSatisfierExists == false)
      {
         // Retrieve info from Satisifer, we'll be populating DOORs with this.
         String strSatisfierName = pSatisfier.getName();
         String strSatisfierType = pSatisfier.getType();
         String strProjectName = pSatisfier.getProjectName();
         String strProjectID = pSatisfier.getProjectID();

         // Call Doors object to execute DXL CreateLinkModule  function.
//               DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );

         String strRequest = "#include \"GetProjectInfo.dxl\";";               
         strRequest += "CreateLinkModule(\"";
         strRequest += m_SourceID;  // Get Workspace Name
         strRequest += "\", \"";
         strRequest += "Describe Links";
         strRequest += "\")";

//               pDoors.runStr( strRequest )
//               String strResult = pDoors.getresult();
         String strResult = DoorUtility.sendRequestToDoors(strRequest);

         if( strResult.equals("") == true )
         {
            // Call Doors object to execute DXL CreateFormalModule function.
            strRequest = "#include \"GetProjectInfo.dxl\";";
            strRequest += "CreateFormalModule2(\"";
            strRequest += m_SourceID;

            // TODO: Get Workspace Name
            strRequest += "\", \"";
            strRequest += strProjectName;
            strRequest += "\")";

//                  pDoors.runStr( strRequest )
//                  strResult = pDoors.getresult();
            strResult = DoorUtility.sendRequestToDoors(strRequest);
         }

         if( strResult.equals("") == true )
         {
            // Call Doors object to execute DXL AddSatisfier function.
            strRequest = "#include \"GetProjectInfo.dxl\";";                  
            strRequest += "InsertAndLinkObject(\"";
            strRequest += m_SourceID;
            strRequest += "\", \"";
            strRequest += strProjectName;
            strRequest += "\", \"";
            strRequest += strSatisfierName;
            strRequest += "\", \"";
            strRequest += strSatisfierXMIID;
            strRequest += "\", \"";
            strRequest += strProjectID;
            strRequest += "\", \"";
            strRequest += strSatisfierType;
            strRequest += "\", \"";
            strRequest += m_ProjectName;
            strRequest += "\", \"";
            strRequest += m_ModName;
            strRequest += "\", \"";
            strRequest += m_Name;
            strRequest += "\")";

//                  pDoors.runStr( strRequest )
//                  strResult = pDoors.getresult();
            strResult = DoorUtility.sendRequestToDoors(strRequest);
            if( strResult.equals("Link Created") == true  )
            {
               m_Satisfiers.add( pSatisfier );
            }
            else
            {
               String msg = NbBundle.getMessage(DoorsRequirement.class, "IDS_SHAREDEDITNOTSUPPORTED");
               throw new RequirementsException(RequirementsException.RP_E_SHAREDEDITNOTSUPPORTED, msg);
            }
         }
      }
   }
   
   /**
    *
    * Add Satisfier sub-tree item{s} to the passed in IProjectTreeItem.
    *
    * @param *pTree[in] Parent Tree item.
    * @param strName[in] Name of RequirementID to look for.
    * @param strRequirementSourceID[in] Name of Requirement Source provider.
    *
    * @return void
    *
    */
   public void addSatisfierChildElements( )
   {      
      try
      {
   //         DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );
         String strRequest = "#include \"GetProjectInfo.dxl\";";         
         strRequest += "GetObjectSatisfiers2(\"";
         strRequest += m_ProjectName;
         strRequest += "\", \"";
         strRequest += m_ModName;
         strRequest += "\", ";
         strRequest += m_ID;
         strRequest += ")";

   //         pDoors.runStr( strRequest );         
   //         String strResult = pDoors.getresult();
         String strResult = DoorUtility.sendRequestToDoors(strRequest);

         // Load the returned XML into a doc and send off to the ProcessChild to
         // build a nested IRequirements structure.

         Document pDoc = XMLManip.loadXML(strResult);
         if( pDoc != null )
         {
            String strPattern = "RequirementProxy/Requirement";            
            strPattern	+= "[@id='";
            strPattern	+= m_ID;
            strPattern	+= "']";

            // Update the <proxyFile> location element in the .etd file
            Node cpRequirementNode = XMLManip.selectSingleNode(pDoc, strPattern );

            if( cpRequirementNode != null)
            {
               Node cpSatisfiersNode = XMLManip.selectSingleNode(cpRequirementNode, "Satisfiers");               
               if( cpSatisfiersNode instanceof Branch)
               {
                  Branch statisfierBranch = (Branch)cpSatisfiersNode;
                  List cpSatisfierNodeList = statisfierBranch.content();

                  if( cpSatisfierNodeList != null)
                  {
                     ETList < ISatisfier > cpSatisfiers = new ETArrayList < ISatisfier >();
                     Iterator iter = cpSatisfierNodeList.iterator();

                     while(iter.hasNext() == true)
                     {
                        Object cpSatisfierNode = iter.next();                        
                        if( cpSatisfierNode instanceof Element )
                        {
                           Element cpSatisfierElement = (Element)cpSatisfierNode;
                           String strName = cpSatisfierElement.attributeValue("name");
                           String strXMIID = cpSatisfierElement.attributeValue("xmiid");
                           String strProjectID = cpSatisfierElement.attributeValue("project");
                           String strProjectName = cpSatisfierElement.attributeValue("projectname");

                           ISatisfier cpSatisfier = new Satisfier();                           
                           cpSatisfier.setName( strName );
                           cpSatisfier.setXMIID( strXMIID );
                           cpSatisfier.setProjectID( strProjectID );
                           cpSatisfier.setProjectName( strProjectName );

                           cpSatisfiers.add( cpSatisfier );

                        }	// EndIf - Cast to DOMElement

                     }	// End - Iterate through NodeList

                     setSatisfiers( cpSatisfiers );

                  }	// EndIf - Satisfers child NodeList retrieved

               }	// EndIf - Satisfiers node not null

            }	// EndIf - Requirement node not null

         }	// EndIf - Retrieved ProxyFile
      }
      catch(RequirementsException e)
      {
         UMLMessagingHelper helper = new UMLMessagingHelper();
         helper.sendExceptionMessage(e);
      }
   }
   
   /**
    * Delete Satisfier from this requirement's ISatisfiers collection and also 
    * from DOORS
    *
    * @param pSatisfiern The Satisfier in the Requirements collection which 
    *                    matches the treeitem satisifer.xmiid
    */
   public void removeSatisfier( ISatisfier pSatisfier )
      throws RequirementsException
   {
      if( null == pSatisfier)
      {
         throw new IllegalArgumentException();
      }
         
      String strSatisfierXMIID = pSatisfier.getXMIID();

      // Call Doors object to execute DXL DeleteObjectSatisfier
//         DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );

      String strRequest = "#include \"GetProjectInfo.dxl\";";
      strRequest += "DeleteObjectSatisfier(\"";
      strRequest +=  m_ProjectName;
      strRequest += "\", \"";
      strRequest +=  m_ModName;
      strRequest += "\", \"";
      strRequest +=  m_ID;
      strRequest += "\", \"";
      strRequest +=  strSatisfierXMIID;
      strRequest += "\")";

//         pDoors.runStr( strRequest );
//         String strResult = pDoors.getresult();
      String strResult = DoorUtility.sendRequestToDoors(strRequest);

      if( strResult.equals("Deleted") == true  )
      {
         // Delete from internal collection
         Iterator < ISatisfier > iter = m_Satisfiers.iterator();
         for(int index = 0; iter.hasNext() == true; index++)
         {
            ISatisfier cpSatisfier = iter.next(); 

            String strSatisfierID = cpSatisfier.getXMIID();

            //TODO: Need a different 'remove; collection method; one that takes the object instead of index.
            if( strSatisfierID.equals(strSatisfierXMIID) == true )
            {
               m_Satisfiers.remove( index );
               break;
            }
         }
      }
      else
      {
         String msg = NbBundle.getMessage(DoorsRequirement.class, "IDS_SHAREDEDITNOTSUPPORTED");
         throw new RequirementsException(RequirementsException.RP_E_SHAREDEDITNOTSUPPORTED, msg);
      }
   }
   
   /**
    * Go back to the Requirements Source and get this. Requirement's children
    *
    * @param pRequirementSource Not used by this type of Requirement
    * @return Requirements Collection
    */
   public ETList < IRequirement > getSubRequirements( IRequirementSource pRequirementSource)
      throws RequirementsException
   {
      ETList < IRequirement > retVal = new ETArrayList < IRequirement >();
      if( null == pRequirementSource)
      {
         throw new IllegalArgumentException();
      }
         
         
      // Call Doors object to execute DXL GetModuleXML or GetObjectChildrenByIDXML.

//      DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );
      String strRequest = "#include \"GetProjectInfo.dxl\";";
      if( m_Type.equals("Category") )
      {
         strRequest += "GetModuleXML2(\"";
         strRequest += m_ModName;
         strRequest += "\", \"";
         strRequest += m_Name;
         strRequest += "\")";
      }
      else
      {
         strRequest += "GetObjectChildrenByIDXML2(\"";
         strRequest +=  m_ProjectName;
         strRequest += "\", \"";
         strRequest +=  m_ModName;
         strRequest += "\", ";
         strRequest +=  m_ID;
         strRequest += ")";
      }

//      pDoors.runStr( strRequest )
//      String strResult = pDoors.getresult();
      String strResult = DoorUtility.sendRequestToDoors(strRequest);

      // Load the returned XML into a doc and send off to the ProcessChild to
      // build a nested IRequirements structure.

      Document pDoc = XMLManip.loadXML(strResult);
      if( pDoc != null )
      {         
         String strtPattern = "/RequirementsProject";
         Node pXMLNode = XMLManip.selectSingleNode(pDoc, strtPattern);

         if(pXMLNode != null)
         {
            // processChildElements( pXMLNode, ppRequirements )
            RequirementUtility.processChildElements( pXMLNode, DoorsRequirement.class, retVal );

            if( m_Type.equals("Category") == false )
            {
               // Add Satisfier elements
               addSatisfierChildElements();
            }
         }
      }
      else
      {
         String msg = NbBundle.getMessage(DoorsRequirement.class, "IDS_NOSELECTION");
         throw new RequirementsException(RequirementsException.RP_E_NO_SELECTION, msg);
      }
      
      return retVal;
   }
 
   /** Is this IComparableTreeData the same as the argument? */ 
   public boolean isSame( IComparableTreeData pOtherData )
   {
      return true;
   }
   
   /** Checks if the node can participate in a drag operation. */
   public boolean isAllowedToDrag()
   {
      boolean retVal = true;
      
      String strType = getType();
      if(strType.equals("Category") == true)
      {
         retVal = false;
      }
      
      return retVal;
   }
}
