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

package org.netbeans.modules.uml.requirements.xmlprovider;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

//import org.netbeans.modules.uml.core.addinframework.IAddInDescriptor;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterSupport;
import org.netbeans.modules.uml.core.metamodel.structure.IRequirementArtifact;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementsProvider;
import org.netbeans.modules.uml.core.requirementsframework.RequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.RequirementUtility;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.util.NbBundle;

/**
 * @author josephg
 *
 */
public class ETReqProvider implements IDesignCenterSupport, IRequirementsProvider
{
   private String m_progID = "org.netbeans.modules.uml.ui.products.ad.requirementsprovider.etrequirementsprovider";
   private String m_Version;
   
   public String getProgID()
   {
      return m_progID;
   }
   
   public void setProgID(String newVal)
   {
      m_progID = newVal; 
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#deInitialize(java.lang.Object)
    */
   public long deInitialize(Object context)
   {
      // nothing in C++ code here
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getID()
    */
   public String getID()
   {
      return getProgID();
   }
   
   public String getDisplayName()
   {
      return Messages.getString("DisplayName");//$NON-NLS-1$
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getLocation()
    */
   public String getLocation()
   {
      JFileChooser chooser = new JFileChooser();
      chooser.setAcceptAllFileFilterUsed(false);
      
      chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
      {
         public boolean accept(File file)
         {
            return file.isDirectory() ||
                     file.toString().toLowerCase().endsWith(".etreq"); //$NON-NLS-1$
         }
         
         public String getDescription()
         {
            return Messages.getString("ETReqProvider.Requirements_(*.etreq)_2"); //$NON-NLS-1$
         }
      });
      
      chooser.setDialogTitle(Messages.getString("ETReqProvider.Open_Requirements_File_3"));       //$NON-NLS-1$
      
      int res = chooser.showOpenDialog(ProductHelper.getProxyUserInterface().getWindowHandle());
      try
      {
         return res == JFileChooser.APPROVE_OPTION? chooser.getSelectedFile().getCanonicalPath() : null;
      }
      catch(IOException e)
      {
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getName()
    */
   public String getName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getVersion()
    */
   public String getVersion()
   {
      return m_Version;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#initialize(java.lang.Object)
    */
   public long initialize(Object context)
   {
      // nothing in C++ code here
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#unLoad(java.lang.Object)
    */
   public long unLoad(Object context)
   {
      // nothing in C++ code here
      return 0;
   }
   
   public IRequirementSource displaySources(/*IAddInDescriptor addInDescriptor*/)
   {
      IRequirementSource requirementSource = new RequirementSource();
      requirementSource.setRequiresLogin(false);
      
      String id = XMLManip.retrieveDCEID();
      requirementSource.setID(id);
      
      String location = getLocation();
      
      if(location != null)
      {
          String fileN = StringUtilities.getFileName(location);
          String friendlyName = "SUN XML - " + fileN; //$NON-NLS-1$
          
          // Set the name displayed in the design center tree for the requirements source.
          requirementSource.setDisplayName(friendlyName);
          
         // Set the prog Id for the requirement source provider.
          //String value = addInDescriptor.getProgID();
          String value = getProgID();
          requirementSource.setProvider(value);
          
          // Set the location of the source provider's requirements file.
          requirementSource.setLocation(location);
          
          // Set the location of the proxy file (which connects Describe symbols with the
          // requirements that they fulfill).
          requirementSource.setProxyFile(""); //$NON-NLS-1$
          
          return requirementSource;
      }
      else
      {
         // Report error, no selection
      }
      return null;
   }

   public ETList<IRequirement> loadRequirements(IRequirementSource requirementSource)
   {
      ETList<IRequirement> retVal = null;
      
      Document doc = ReqUtils.getXMLDoc(requirementSource);
      
      if(doc != null)
      {
         Node xmlNode = doc.selectSingleNode("/RequirementsProject"); //$NON-NLS-1$
         
         if(xmlNode != null)
         {
            retVal = RequirementUtility.processChildElements(xmlNode,ETRequirement.class,null);
            
            if(retVal != null)
            {
               int reqCount = retVal.getCount();
               
               for(int index = 0; index < reqCount; index++)
               {
                  IRequirement requirement = retVal.item(index);
                  ReqUtils.addSatisfierChildElements(requirement,requirementSource);
               }
            }
         } 
      }
      return retVal;
   }
   
   public IRequirement getRequirement(IRequirementArtifact requirementArtifact, IRequirementSource requirementSource)
   {
      String id = requirementArtifact.getRequirementID();
      
      Document doc = ReqUtils.getXMLDoc(requirementSource);
      
      if(doc != null)
      {
         Node xmlNode = doc.selectSingleNode("//Requirement[@id='"+id+"']"); //$NON-NLS-1$ //$NON-NLS-2$

         if(xmlNode != null)
         {
            // Create a Dummy container node to hold node we care about, put cloned node of the matched node 
            // in container.  ProcessChildElements will build requirements from children of container node.
            Element dummyContainerNode = XMLManip.createElement(doc, "RequirementsProject"); //$NON-NLS-1$
            
            if(dummyContainerNode != null)
            {
               Node xmlNode2 = (Node)xmlNode.clone();
               dummyContainerNode.add(xmlNode2);
               
               // There should only be one Requirement matching ID, use the ProcessChildElements method to
               // turn XML into Requirements collection.
               ETList<IRequirement> requirements = RequirementUtility.processChildElements(dummyContainerNode,
                  ETRequirement.class,null);
                  
               if(requirements != null)
               {
                  int reqCount = requirements.getCount();
                  
                  if(reqCount > 0)
                  {
                     return requirements.item(0);
                  }
               }
            }
         }            
      }
      return null;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // IDesignCenterSupport Methods
   
   /** save the design center addin */
    public void save()
    {
        // There is nothing to save.
    }
    
    public String getDescription()
    {
        return NbBundle.getMessage(ETReqProvider.class, "IDS_XML_REQ_SOURCE_TITLE");
    }
}
