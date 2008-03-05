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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * 
 * @author Trey Spiva
 */
public abstract class PresentationTypesMgrImpl implements IPresentationTypesMgr
{
   public static final String SEQUENCE_DIAGRAM = "Sequence Diagram";
   public static final String CLASS_DIAGRAM = "Class Diagram";
   public static final String ACTIVITY_DIAGRAM = "Activity Diagram";
   public static final String COLLABORATION_DIAGRAM = "Collaboration Diagram";
   public static final String STATE_DIAGRAM = "State Diagram";
   public static final String USECASE_DIAGRAM = "Use Case Diagram";
   public static final String COMPONENT_DIAGRAM = "Component Diagram";
   public static final String DEPLOYMENT_DIAGRAM = "Deployment Diagram";
   public static final int DIAGRAM_ID_DONT_CARE = -1;
   
   private String m_CurrentVersion = ApplicationManagerResource.getString("ApplicationManager.Version"); //$NON-NLS-1$
   private Document m_Doc = null;
   private HashMap m_DiagramTable = new HashMap();
   private HashMap m_InitStringsTable = new HashMap();
   
   /**
    * Creates the default presentation file.
    * 
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#createDefaultXMLFile()
    */
   public void createDefaultXMLFile()
   {
      String location = retrieveDefaultPresentationTypesLocation();
      createDefaultXMLFile(location);
   }

   /**
    * Creates the default presentation file.
    * 
    * @param The location to store the file.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#createDefaultXMLFile()
    */
   public void createDefaultXMLFile(String location)
   {
      if(location.length() > 0)
      {
         Document doc = XMLManip.loadXML(ApplicationManagerResource.getString("ApplicationManager.IDR_PRESFILE_HEADER"));  //$NON-NLS-1$
         if(doc != null)
         {
            Element rootElement = doc.getRootElement();
            if(rootElement != null)
            {
               createVersionSection(rootElement);
               createDiagramsTableSection(rootElement);
               createInitStringsTableSection(rootElement);
               createButtonsSection(rootElement);
               createToolsSection(rootElement);
               createMetaTypesSection(rootElement);
               createPresentationSection(rootElement);
               createInitStringsSection(rootElement);
               createOwnerMetaTypesSection(rootElement);
               createInvalidDrawEnginesOnDiagramsSection(rootElement);
               XMLManip.save(doc, location);
            }
         }
      }
   }

   /**
    * Validates the file.  Right now it just makes sure it's a valid XML file.
    *
    * @return <code>true</code> if the file is valid and <code>false</code> if
    *         file is not valid.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#validateFile(boolean)
    */
   public boolean validateFile()
   {
      boolean retVal = false;
      
      if(m_Doc == null)
      {
         retVal = validateFileWithNoCreate();
         if(retVal = true)
         {
            String presVersion = getPresentationTypesMgrVersion();
            String fileVersion = getVersion();
            if(presVersion.equals(fileVersion) == false)
            {
               retVal = false;
            }
         }
         
         if(retVal == false)
         {
            createDefaultXMLFile();
            retVal = validateFileWithNoCreate();
         }
      }
      else
      {
         retVal = true;
      }
      
      return retVal;
      
   }

   /** (
    * Returns the initialization string for a specific button/diagram pair.
    *
    * @param sButtonName
    * @param nDiagramKind
    * @return
    * 
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getButtonInitString(java.lang.String, int, java.lang.StringBuffer)
    */
   public String getButtonInitString( String sButtonName, 
                                     /* DiagramKind */ int nDiagramKind )
   {
      String retVal = "";
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null) && 
         (sButtonName != null) && (sButtonName.length() > 0))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            StringBuffer buffer = new StringBuffer("./");
            buffer.append(getButtonSectionName());
            buffer.append("/");
            buffer.append(sButtonName);
            buffer.append("[@");
            buffer.append(getDiagramIDAttributeName());
            buffer.append("='");
            buffer.append(getDiagramID(nDiagramKind));
            buffer.append("']");
            
            Node node = XMLManip.selectSingleNode(root, buffer.toString());
            if(node == null)
            {     
               // This is not an error.  It simply means that this button does not 
               // have a specific overload for this diagram type.  Requery with a don't care
               // diagram type
               buffer .delete(0, buffer.length());
               buffer.append("./");
               buffer.append(getButtonSectionName());
               buffer.append("/");
               buffer.append(sButtonName);
               buffer.append("[@");
               buffer.append(getDiagramIDAttributeName());
               buffer.append("='");
               buffer.append(getDiagramID(""));
               buffer.append("']");    
               node = XMLManip.selectSingleNode(root, buffer.toString());
            }
            
            if(node != null)
            {
               int value = XMLManip.getAttributeIntValue(node, getInitStringIDAttributeName());
               retVal = getInitString(value);
            }
         }
      }
      
      return retVal;      
   }


   /**
    * Returns the initialization string that should be used when wishing to 
    * create a presentation objectbased on a UML type.  For example, this
    * happens when the user drags an IElement from the project tree onto the 
    * drawing area.  We come here to see what node/edge we should create.
    *
    * @param sMetaType The element type to be dropped onto the diagram
    * @param nDiagramKind The diagram kind this element is to be located on
    * @return The TS init string that should be used to create the PE.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getMetaTypeInitString(java.lang.String, int)
    */
   public String getMetaTypeInitString(String sMetaType, int nDiagramKind)
   {
      String retVal = "";
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null) && (sMetaType.length() > 0))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            StringBuffer buffer = new StringBuffer("./");
            buffer.append(getMetaTypesSectionName());
            buffer.append("/");
            buffer.append(sMetaType);
            buffer.append("[@");
            buffer.append(getDiagramIDAttributeName());
            buffer.append("='");
            buffer.append(getDiagramID(nDiagramKind));
            buffer.append("']");
            
            Node node = XMLManip.selectSingleNode(root, buffer.toString());
            if(node == null)
            {     
               // This is not an error.  It simply means that this button does not 
               // have a specific overload for this diagram type.  Requery with a don't care
               // diagram type
               buffer .delete(0, buffer.length());
               buffer.append("./");
               buffer.append(getMetaTypesSectionName());
               buffer.append("/");
               buffer.append(sMetaType);
               buffer.append("[@");
               buffer.append(getDiagramIDAttributeName());
               buffer.append("='");
               buffer.append(getDiagramID(""));
               buffer.append("']");    
               node = XMLManip.selectSingleNode(root, buffer.toString());
            }
            
            if(node != null)
            {
               int value = XMLManip.getAttributeIntValue(node, getInitStringIDAttributeName());
               retVal = getInitString(value);
            }
         }
      }
      
      return retVal; 
   }

   /** 
    * Same as GetMetaTypeInitString, except this one takes an IElement.
    *
    * This one grabs the type off the element and also deals with properties - such 
    * as on roles (PartFacades) where one role represents an actor and another a 
    * class or use case.
    *
    * @param pElement[in] The element to be queried
    * @param nDiagramKind[in] The diagram kind this element is to be located on
    * @return The TS init string that should be used to create the PE.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getMetaTypeInitString2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int)
    */
   public String getMetaTypeInitString(IElement pElement, int nDiagramKind)
   {
      String retVal = "";
      
      if(pElement != null)
      {
         String elementType = pElement.getElementType();
         if(elementType.equals("PartFacade") == true)
         {
            if (pElement instanceof IParameterableElement)
            {
               IParameterableElement paramElement = (IParameterableElement)pElement;
               String sTypeConstraint = paramElement.getTypeConstraint();
               elementType += sTypeConstraint;               
            }
         }
         
         retVal = getMetaTypeInitString(elementType, nDiagramKind);
      }
      
      return retVal;
   }

   /**
    * For a particular initialization string/diagram pair this returns the details.  What draw engine should
    * be created? What UML type should be created when this node/edge is dropped onto the diagram?  Is the object a
    * node or an edge.
    * 
    * @param sInitString The initialization String.
    * @param nDiagramKind The type of the diagram.  The value must be one of the 
    *                     IDiagramKind values.
    * @return The details of the item.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getInitStringDetails(java.lang.String, int, java.lang.StringBuffer, java.lang.StringBuffer, int)
    */
   public PresentationTypeDetails getInitStringDetails( String sInitString, 
                                                       /* DiagramKind */ int nDiagramKind)
   {
      PresentationTypeDetails retVal = new PresentationTypeDetails();
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null) && (sInitString.length() > 0))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            StringBuffer buffer = new StringBuffer("./");
            buffer.append(getInitStringsSectionName());
            buffer.append("/");
            buffer.append(getInitStringElementName());
            buffer.append("[@");
            buffer.append(getInitStringIDAttributeName());
            buffer.append("='");
            buffer.append(getInitStringID(sInitString));
            buffer.append("' and @");
            buffer.append(getDiagramIDAttributeName());
            buffer.append("='");
            buffer.append(getDiagramID(nDiagramKind));
            buffer.append("']");
      
            Node node = XMLManip.selectSingleNode(root, buffer.toString());
            if(node == null)
            {     
               // This is not an error.  It simply means that this button does not 
               // have a specific overload for this diagram type.  Requery with a don't care
               // diagram type
               buffer .delete(0, buffer.length());
               buffer.append(getInitStringsSectionName());
               buffer.append("/");
               buffer.append(getInitStringElementName());
               buffer.append("[@");
               buffer.append(getInitStringIDAttributeName());
               buffer.append("='");
               buffer.append(getInitStringID(sInitString));
               buffer.append("' and @");
               buffer.append(getDiagramIDAttributeName());
               buffer.append("='");
               buffer.append(getDiagramID(""));
               buffer.append("']");
               node = XMLManip.selectSingleNode(root, buffer.toString());
            }
      
            if(node != null)
            {
               String metaType = XMLManip.getAttributeValue(node, getMetaTypeAttributeName());
               retVal.setMetaType(metaType);
               
               String engineName = XMLManip.getAttributeValue(node, getEngineNameAttributeName());
               retVal.setEngineName(engineName);
                              
               String value = XMLManip.getAttributeValue(node, getGraphObjectKindAttributeName());
               if(value.length() > 0)
               {
                  retVal.setObjectKind(Integer.parseInt(value));
               }
            }
         }
      }
      
      return retVal;
   }
   
   public DiagramToolBarDefs getDiagramToolbarActions(/* DiagramKind */ int nDiagramKind)
   {
      DiagramToolBarDefs retVal = new DiagramToolBarDefs();
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            Node toolbarNode = XMLManip.selectSingleNode(root, "./ToolbarDefinitions");
            if(toolbarNode != null)
            {
               String query = "./DiagramToolbars/Diagram[@DiagramID='" + getDiagramID(nDiagramKind) + "']";
               Node node = XMLManip.selectSingleNode(toolbarNode, query);
               if(node != null)
               {
                  List toolbarRefs = XMLManip.selectNodeList(node, "./ToolbarRef");
                  for (Iterator iter = toolbarRefs.iterator(); iter.hasNext();)
                  {
                     Node toolBarRefNode = (Node)iter.next();
                     
                     String toolBarName = XMLManip.getAttributeValue(toolBarRefNode, "name");
                     boolean optional    = XMLManip.getAttributeBooleanValue(toolBarRefNode, "optional");
                     
                     String toolBarQuery = "./Toolbar[@name='" +  toolBarName + "']";
                     Node toolBarDefNode = XMLManip.selectSingleNode(toolbarNode, toolBarQuery);
                     if(toolBarDefNode != null)
                     {
                        DiagramToolDetails details = new DiagramToolDetails(toolBarDefNode,
                                                                            optional);
                     
                        retVal.addToolBarDetails(toolBarName, details);
                     }
                  }
               }
            }            
         }
      }
      
      return retVal;
   }
   
   
   public HashMap<String,String[]> getAllToolbarActions() {
      HashMap<String, String[]> hashMap = new HashMap<String, String[]>();
      
      boolean isValid = validateFile();
      if(m_Doc != null)
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            List<Node> toolbarNodeList = XMLManip.selectNodeList(root, "./ToolbarDefinitions/Toolbar");
            for(int index1=0 ; index1<toolbarNodeList.size() ; index1++) {                
                List<Node> buttonList= XMLManip.selectNodeList(toolbarNodeList.get(index1), "Button");
                for(int index2=0 ; index2<buttonList.size(); index2++) {
                    String id = XMLManip.getAttributeValue(buttonList.get(index2), "id");
                    String name = XMLManip.getAttributeValue(buttonList.get(index2), "name");
                    String tooltip = XMLManip.getAttributeValue(buttonList.get(index2), "tooltip");
                    String palette_id = XMLManip.getAttributeValue(buttonList.get(index2), "paletteid");
                    String icon = XMLManip.getAttributeValue(buttonList.get(index2), "icon");
                    String str[] = {name, tooltip, id, icon};
                    if(palette_id != null && (!palette_id.equals(""))) {
                        hashMap.put(palette_id, str);
                        Log.out("PresentationTypesMgrImpl():getAllToolbarActions() Adding " + palette_id + "  (" + name + ", " + id + ", " + icon);
                    }
                }
            }            
         }
      }
     Log.out("PresentationTypesMgrImpl():getAllToolbarActions()  Hashmap size  is " + hashMap.size());
      return hashMap;       
   }
  
   /**
    * Returns the version of the presentation file.
    *
    * @param sVersion
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getVersion(java.lang.StringBuffer)
    */
   public String getVersion()
   {
      String retVal = "";
      
      boolean isValid = validateFile();
      
      if((isValid == true) && (m_Doc != null))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            Node versionNode = XMLManip.selectSingleNode(root, "./" + getVersionSectionName());
            if(versionNode != null)
            {
               retVal = versionNode.getText();
            }
         }
      }
      
      return retVal;
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getPresentationTypesMgrVersion(java.lang.StringBuffer)
    */
   public String getPresentationTypesMgrVersion()
   {      
      return m_CurrentVersion;      
   }

   /**
    * Retrieves the owner elementtype for a sub-element, e.g. "Attribute" will return "Class".
    *
    * @param sElementType The subelement type
    * @return] The owner type
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getOwnerMetaType(java.lang.String)
    */
   public String getOwnerMetaType(String sElementType)
   {
      String retVal = "";
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null) && (sElementType.length() > 0))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {    
            // The user doesn't care about diagram name, so just search on the
            // button name.
            // Do a select like follows
            // ./MetaTypes/Class
            StringBuffer buffer = new StringBuffer("./");  
            buffer.append(getOwnerMetaTypesSectionName());
            buffer.append("/");
            buffer.append(sElementType);
            
            Node node = XMLManip.selectSingleNode(root, buffer.toString());
            if(node != null)
            {
               retVal = XMLManip.getAttributeValue(node, getInitStringIDAttributeName());
            }
         }
      }
      
      return retVal;
   }

   /**
    * Is this a valid drawengine on this diagram type?
    * 
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#isValidDrawEngine(int, java.lang.String)
    */
   public boolean isValidDrawEngine(int nDiagramKind, String sDrawEngineID)
   {
      boolean retVal = false;
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null) && (sDrawEngineID.length() > 0))
      {
         retVal = true;
         
         // The user doesn't care about diagram name, so just search on the
         // button name.
         // Do a select like follows
         // ./InvalidEngines/InvalidEngine[@DiagramID=1 and @Engine=LifelineDrawEngine]
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            StringBuffer buffer = new StringBuffer("./");
            buffer.append(getInvalidDrawEnginesSectionName());
            buffer.append("/");   
            buffer.append(getInvalidDrawEngineElementName());
            buffer.append("[@");
            buffer.append(getDiagramIDAttributeName());
            buffer.append("='");
            buffer.append(getDiagramID(nDiagramKind));
            buffer.append("' and @");
            buffer.append(getInvalidDrawEngineAttributeName());
            buffer.append("='");
            buffer.append(sDrawEngineID);
            buffer.append("']");
            
            Node node = XMLManip.selectSingleNode(root, buffer.toString());
            if(node != null)
            {
               retVal = false;
            }
         }
         
         // TODO: Some Developer License Stuff.  That will change.
      }
      
      return retVal;
   }

   /**
    * Returns the default description for a label view
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getDefaultLabelView()
    */
   public String getDefaultLabelView()
   {
      return "";
   }

   /** 
    * Returns the default description for a connector view
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getDefaultConnectorView()
    */
   public String getDefaultConnectorView()
   {
      return "";
   }

   /** 
    * Returns the metatype for the various types of edges
    *
    * @param sElementType The element type for which we need to get a 
    *                      presentation element for
    * @param sInitailizationString Used to disambiguate certain 
    *                              presentation element types
    * @return The presentation type we should create.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getPresentationElementMetaType(java.lang.String, java.lang.String)
    */
   public String getPresentationElementMetaType(String sElementType, 
                                                String sInitializationString)
   {
      String retVal = "";
      
      boolean isValid = validateFile();
      if((isValid == true) && (m_Doc != null) && (sElementType.length() > 0))
      {
         // Do a select like follows
         // ./PresentationElements/PresentationElementToCreate[ElementType='Association']
         
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            StringBuffer buffer = new StringBuffer("./");  
            buffer.append(getPresentationSectionName());
            buffer.append("/");
            buffer.append(getPresentationElementName());
            buffer.append("[@");
            buffer.append(getElementTypeAttributeName());
            buffer.append("='");
            buffer.append(sElementType);
            buffer.append("']");

            Node node = XMLManip.selectSingleNode(root, buffer.toString());
            if(node != null)
            {
               retVal = XMLManip.getAttributeValue(node, getPresentationAttributeName());
            }
         }
      }
      
      return retVal;
   }

   //**************************************************
   // Abstract Methods
   //**************************************************

   /**
    * Create the Invalid DrawEngines Section
    */
   protected abstract void createInvalidDrawEnginesOnDiagramsSection(Element rootElement);
   
   /**
    * reate the InitStrings Section
    */
   protected abstract void createInitStringsSection(Element rootElement);


   /**
    * Create the Presentation Element Section
    */
   protected abstract void createPresentationSection(Element rootElement);


   /**
    * Create the MetaTypes Section
    */
   protected abstract void createMetaTypesSection(Element rootElement);
   
   /**
    * Create the Buttons Section
    */
   protected abstract void createButtonsSection(Element rootElement);


   /**
    * Create the initialization strings section.
    */
   protected abstract void createInitStringsTableSection(Element rootElement);


   /**
    * Create the diagram section.
    */
   protected abstract void createDiagramsTableSection(Element rootElement);
   
   /**
    * Returns the diagram id for the diagramKind
    * 
    * @param nDiagramKind The type of the diagram.  Must be one of the 
    *                     IDiagramKind values.
    * @return The diagram id.
    */
   protected abstract long getDiagramID(int nDiagramKind);
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Creates the default presentation file.
    */
   protected String retrieveDefaultPresentationTypesLocation()
   {
      String retVal = ""; //$NON-NLS-1$
      
      IProduct product = ProductHelper.getProduct();
      
      if(product != null)
      {
         IConfigManager manager = product.getConfigManager();
         if(manager != null)
         {
            retVal = manager.getPresentationTypesLocation();
         }
      }
      
      return retVal;
   }
   
   


   /**
    * Create the Owner MetaTypes Section.
    *
    * This maps a element type to its node owner's metatype.
    *
    * @param rootElement The owner of the meta types selection.
    */
   protected void createOwnerMetaTypesSection(Element rootElement)
   {
      Element element = XMLManip.createElement(rootElement, getOwnerMetaTypesSectionName());      
      if(element != null)
      {
         createOwnerMetaTypesEntry(element, "Attribute", "Class");
         createOwnerMetaTypesEntry(element, "Operation", "Class");
      }
      
   }

   /**
    * Create the Buttons Section.
    * 
    * @param rootElement The owner of the meta types selection.
    */
   protected void createToolsSection(Element rootElement)
   {
      Element element = XMLManip.createElement(rootElement, getToolsTableSectionName());      
      if(element != null)
      {
         createToolEntry(element, "Node", TSGraphObjectKind.TSGOK_NODE);
         createToolEntry(element, "Edge", TSGraphObjectKind.TSGOK_EDGE);
         createToolEntry(element, "Label", TSGraphObjectKind.TSGOK_LABEL);
         createToolEntry(element, "NodeDecorator", TSGraphObjectKind.TSGOK_NODE_DECORATOR);
         createToolEntry(element, "NodeResize", TSGraphObjectKind.TSGOK_NODE_RESIZE);
      }
   }

   /**
    * Create the Version section identifying the version of this file.
    * 
    * @param rootElement The owner of the meta types selection.
    */
   protected void createVersionSection(Element rootElement)
   {
      Element element = XMLManip.createElement(rootElement, getVersionSectionName());      
      if(element != null)
      {
         element.setText(m_CurrentVersion);
      }
   }


   /**
    * Returns the names for the Buttons Section
    */
   protected String getVersionSectionName()
   {
      return "Version";
   }
   
   /**
    * Creates a button entry.
    * 
    * @param rootElement The owner of the meta types selection.
    * @param name The name of the tool.
    * @param id The ID of the tool.
    */
   protected void createToolEntry(Element rootElement, String name, int id)
   {
      if(name.length() > 0)
      {
         Element newElement = XMLManip.createElement(rootElement, getToolElementName());
         if(newElement != null)
         {
            XMLManip.setAttributeValue(newElement, 
                                       getToolIDElementName(), 
                                       Long.toString(id));
            newElement.setText(name);
         }
      }
   }

   /**
    * Returns the names for the Tools Section
    */
   protected String getToolsTableSectionName()
   {
      return "Tools";
   }

   /**
    * Returns the names for the XML tool tag
    */
   protected String getToolElementName()
   {
      return "Tool";
   }

   /**
    * Returns the names for the InitString Element
    */
   protected String getToolIDElementName()
   {
      return "id";
   }
   
   /**
    * Creates an Owner MetaTypes entry.
    * 
    * @param rootElement The owner of the meta types selection.
    * @param childMetaType The name of the metatype.
    * @param ownerMetaType The name of the metatype that owns the childMetaType.
    */
   protected void createOwnerMetaTypesEntry(Element rootElement, 
                                            String  childMetaType,
                                            String  ownerMetaType)
   {
      if((childMetaType.length() > 0) && (ownerMetaType.length() > 0))
      {
         Element newElement = XMLManip.createElement(rootElement, childMetaType);
         if(newElement != null)
         {
            XMLManip.setAttributeValue(newElement, 
                                       getInitStringIDAttributeName(), 
                                       ownerMetaType);
         }
      }
      
   }

   /**
    * Validates the file without the possibility of re-creating the file.
    * 
    * @return <code>true</code> if the file is valid
    */
   protected boolean validateFileWithNoCreate()
   {
      boolean retVal = false;
      
      if(m_Doc == null)
      {
         String location = retrieveDefaultPresentationTypesLocation(); 
         
         if(location.length() > 0)
         {
            File presentationFile = new File(location); 
            if(presentationFile.exists() == true)
            {
               m_Doc = XMLManip.getDOMDocument(location);
               if(m_Doc != null)
               {
                  retVal = true; 
               }
               else
               {
                  reset();
               }
            }
         }
      }
      else
      {
         retVal = true;
      }
      
      return retVal;
   }
   
   /**
    * Resets the Class
    */
   protected void reset()
   {
      m_Doc = null;
      clearDiagramTable();
      clearInitStringsTable();
   }

   /**
    * Returns the string for a specific init string id.
    *
    * @param id
    * @return The value of the init string.
    */
   protected String getInitString(int desiredValue)
   {
      String retVal = "";
   
      if(getInitStringsSize() <= 0)
      {
         readInitStringsTable();
      }
   
      //Collection valueCollection = m_InitStringsTable.values();
      Collection keyCollection = m_InitStringsTable.keySet();
      for (Iterator iter = keyCollection.iterator(); iter.hasNext();)
      {
         String curKey = (String)iter.next();
         Long curValue = (Long)m_InitStringsTable.get(curKey);
         if(desiredValue == curValue.intValue())
         {
            retVal = curKey;
            break;
         }
      }
   
      return retVal;
   }


   /**
    * Reads the init strings table.
    */
   protected void readInitStringsTable()
   {
      if((getInitStringsSize() <= 0) && (m_Doc != null))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            Node node = XMLManip.selectSingleNode(root, "./" + getInitStringsTableSectionName());
            if (node instanceof Element)
            {
               Element element = (Element)node;
               for (Iterator iter = element.elementIterator(); iter.hasNext();)
               {
                  Element child = (Element)iter.next();
                  if(child != null)
                  {
                     try
                     {
                        int initStringID = XMLManip.getAttributeIntValue(child, getInitStringIDAttributeName());
                        String initString = child.getText();
                     
                        if((initString.length() > 0) && 
                           (m_InitStringsTable.containsKey(initString) == false))
                        {
                           m_InitStringsTable.put(initString, new Long(initStringID));
                        }
                     }
                     catch (NumberFormatException e)
                     {
                        // Need to assert that the file is not correct.
                        // Skip the entry because the entry is not correct.
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Returns the name for the Buttons Section.
    */
   protected String getInitStringsTableSectionName()
   {
      return "InitStringsTable";
   }
   
   /**
    * Returns the name for the Buttons Section.
    */
   protected String getInitStringElementName()
   {
      return "InitString";
   }

   /**
    * Returns the name for the Buttons Section.
    */
   protected String getInitStringIDAttributeName()
   {
      return "InitStringID";
   }

   /**
    * Reads the diagrams table.
    */
   protected void readDiagramsTable()
   {
      if((getDiagramTableSize() <= 0) && (m_Doc != null))
      {
         Element root = m_Doc.getRootElement();
         if(root != null)
         {
            Node node = XMLManip.selectSingleNode(root, "./" + getDiagramsTableSectionName());
            if (node instanceof Element)
            {
               Element element = (Element)node;
               for (Iterator iter = element.elementIterator(); iter.hasNext();)
               {
                  Element child = (Element)iter.next();
                  if(child != null)
                  {
                     try
                     {
                        int diagramID = XMLManip.getAttributeIntValue(child, getDiagramIDAttributeName());
                        String diagramName = child.getText();
                     
                        if((diagramName.length() > 0) && 
                           (m_DiagramTable.containsKey(diagramName) == false))
                        {
                           m_DiagramTable.put(diagramName, new Long(diagramID));
                        }
                     }
                     catch (NumberFormatException e)
                     {
                        // Need to assert that the file is not correct.
                        // Skip the entry because the entry is not correct.
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Returns the name for the Buttons Section.
    */
   protected String getDiagramsTableSectionName()
   {
      return "DiagramsTable";
   }

   /**
    * Returns the names for the InitString Element
    */
   protected String getDiagramNameElementName()
   {
      return "DiagramName";
   }

   /**
    * Returns the name for the DiagramID attribute
    */
   protected String getDiagramIDAttributeName()
   {
      return "DiagramID";
   }

   /**
    * Returns the diagram id for the diagramName.
    * 
    * @param diagramName The name of the diagram.
    * @return The id of the diagram.
    */
   protected long getDiagramID(String diagramName)
   {
      long retVal = 0L;
   
      if(getDiagramTableSize() <= 0)
      {
         readDiagramsTable();
      }
   
      if(diagramName.length() == 0)
      {
         retVal = DIAGRAM_ID_DONT_CARE;
      }
      else
      {
         if(m_DiagramTable.containsKey(diagramName) == true)
         {
            Long id = (Long)m_DiagramTable.get(diagramName);
            if(id != null)
            {
               retVal = id.longValue();
            }
         }
      }
   
      return retVal;
   }
   
   /**
    * Returns the names for the MetaTypes Section
    */
   protected String getMetaTypesSectionName()
   {
      return "MetaTypes";
   }
   
   /**
    * Returns the names for the MetaType Attribute
    */
   protected String getMetaTypeAttributeName()
   {
      return "MetaType";
   }
   
   /**
    * Returns the names for the MetaTypes Section
    */
   protected String getOwnerMetaTypesSectionName()
   {
      return "OwnerMetaTypes";
   }
   
   /**
    * Returns the names for the InvalidEngines Attribute
    */
   protected String getInvalidDrawEngineAttributeName()
   {
      return "Engine";
   }


   /**
    * Returns the names for the InvalidEngines Element
    */
   protected String getInvalidDrawEngineElementName()
   {
      return "InvalidEngine";
   }

   /**
    * Returns the names for the MetaTypes Section
    */
   protected String getInvalidDrawEnginesSectionName()
   {
      return "InvalidDrawEngines";
   }
   
   /**
    * Returns the names for the MetaTypes Attribute
    */
   protected String getPresentationAttributeName()
   {
      return "PresentationElement";
   }


   /**
    * Returns the names for the MetaTypes Attribute
    */
   protected String getElementTypeAttributeName()
   {
      // TODO Auto-generated method stub
      return "ElementType";
   }


   /**
    * Returns the names for the MetaTypes Element
    */
   protected String getPresentationElementName()
   {
      return "PresentationElementToCreate";
   }


   /**
    * Returns the names for the MetaTypes Section
    */
   protected String getPresentationSectionName()
   {
      return "PresentationElements";
   }
   
   /**
    * Returns the names for the Buttons Section
    */
   protected String getButtonSectionName()
   {
      return "Buttons";
   }

   /**
    * Returns the names for the InitStrings Section
    */
   protected String getInitStringsSectionName()
   {
      return "InitStrings";
   }
    
   /**
    * Returns the names for the InitStrings Section
    */
   protected String getInitStringsElementName()
   {
      return "InitString";
   }
   
   /**
    * Returns the names for the EngineName Attribute
    */
   protected String getEngineNameAttributeName()
   {
      return "EngineName";
   }
   
   /**
    * Returns the names for the IsNode Attribute
    */
   protected String getGraphObjectKindAttributeName()
   {
      return "TSGraphObjectKind";
   }  
   /**
    * Clears the contents of the diagram table.
    */
   protected void clearDiagramTable()
   {
      m_DiagramTable.clear();
   }

   /**
    * Clears the contents of the Init Strings table.
    */
   protected void clearInitStringsTable()
   {
      m_InitStringsTable.clear();
   }

   /**
    * Retrieves the document that contains the information.
    * 
    * @return The DOM document.
    */
   protected Document getDocument()
   {
      return m_Doc;
   }
   
   /**
    * Retrieves the size of the diagram table.
    */
   protected int getDiagramTableSize()
   {
      return m_DiagramTable.size();
   }
   
   /**
    * Retrieves the size of the Init Strings table.
    */
   protected int getInitStringsSize()
   {
      return m_InitStringsTable.size();
   }
   
   /**
    * Creates an entry in the diagrams table.
    * 
    * @param parent The owner of table entry.
    * @param name The name of the diagram.
    * @param diagramID The id of the diagram
    */
   protected void createDiagramsTableEntry(Element parent, String name, int diagramID)
   {
      if((parent != null ) && (name.length() > 0))
      {         
         Element node = XMLManip.createElement(parent, getDiagramNameElementName());
         if(node != null)
         {
            String cleansedName = XMLManip.checkForIllegals(name);
            node.setText(cleansedName);
            
            Long value = new Long(diagramID); 
            XMLManip.setAttributeValue(node, 
                                       getDiagramIDAttributeName(), 
                                       value.toString());
                                       
            m_DiagramTable.put(name, value);
         }
      }
   }
   
   /**
    * Creates an entry in the diagrams table.
    *
    * @param parent The owner of the table entry.
    * @param initString The entry value.
    * @param id The id of the entry.
    *
    */
   protected void createInitStringsTableEntry(Element parent, 
                                              String  initString, 
                                              long    id)
   {
      if((parent != null) && (initString.length() > 0))
      {
         Element node = XMLManip.createElement(parent, getInitStringElementName());
         if(node != null)
         {
            String cleansedString = XMLManip.checkForIllegals(initString);
            node.setText(cleansedString);

            Long value = new Long(id); 
            XMLManip.setAttributeValue(node, 
                                       getInitStringIDAttributeName(), 
                                       value.toString());
                           
            m_InitStringsTable.put(initString, value);
         }
      }
   }
   
   /**
    * Creates a button entry
    * 
    * @param parent The owner of the table entry.
    * @param btnString The command of the button.
    * @param initString The initialization string contains the node to create 
    *                   any configuration information.
    */
   protected void createButtonEntry(Element parent,
                                    String  btnString,
                                    String  initString)
   {
      createButtonEntry(parent, btnString, initString, "");
   }
   
   /**
    * Creates a button entry
    * 
    * @param parent The owner of the table entry.
    * @param btnString The command of the button.
    * @param initString The initialization string contains the node to create 
    *                   any configuration information.
    * @param diagram The name of the diagram
    */
   protected void createButtonEntry(Element parent,
                                    String  btnString,
                                    String  initString,
                                    String  diagram)
   {
      if((parent != null) && 
         (initString.length() > 0) && 
         (btnString.length() > 0))
      {
         Element node = XMLManip.createElement(parent, btnString);
         if(node != null)
         {
            XMLManip.setAttributeValue(node, 
                                       getInitStringIDAttributeName(), 
                                       Long.toString(getInitStringID(initString)));
                                       
            XMLManip.setAttributeValue(node, 
                                       getDiagramIDAttributeName(), 
                                       Long.toString(getDiagramID(diagram)));
            
         }
      }
   }
   
   /**
    * Creates a MetaTypes entry.
    *
    * @param parent The owner of the table entry.
    * @param metaTypeString The name of the meta type.
    * @param initializationString The initialization string contains the node to create 
    *                             any configuration information.
    */
   protected void createMetaTypesEntry(Element parent,
                                       String  metaTypeString,
                                       String  initializationString)
   {
      createMetaTypesEntry(parent, metaTypeString, initializationString, "");
   }
   
   /**
    * Creates a MetaTypes entry.
    *
    * @param parent The owner of the table entry.
    * @param metaTypeString The name of the meta type.
    * @param initializationString The initialization string contains the node to create 
    *                             any configuration information.
    * @param diagram The name of the diagram
    */
   protected void createMetaTypesEntry(Element parent,
                                       String  metaTypeString,
                                       String  initializationString,
                                       String  diagram)
   {
      if((parent != null) && 
         (metaTypeString.length() > 0) && 
         (initializationString.length() > 0))
      {
         Element node = XMLManip.createElement(parent, metaTypeString);
         if(node != null)
         {
            XMLManip.setAttributeValue(node, 
                                       getInitStringIDAttributeName(), 
                                       Long.toString(getInitStringID(initializationString)));
                                    
            XMLManip.setAttributeValue(node, 
                                       getDiagramIDAttributeName(), 
                                       Long.toString(getDiagramID(diagram)));
         
         }
      }
   }

   /**
    * Creates a presentation elements entry.
    *
    * @param parent The owner of the table entry.
    * @param sElementType The name of the elements type.
    * @param sPresentationElementToCreate The name of the presentation element.
    */
   protected void createPresentationEntry(Element parent,
                                          String  sElementType,
                                          String  sPresentationElementToCreate)
   {
      if((parent != null) && 
         (sElementType.length() > 0) && 
         (sPresentationElementToCreate.length() > 0))
      {
         Element node = XMLManip.createElement(parent,  getPresentationElementName());
         if(node != null)
         {
            XMLManip.setAttributeValue(node, 
                                       getElementTypeAttributeName(), 
                                       sElementType);
                                    
            XMLManip.setAttributeValue(node, 
                                       getPresentationAttributeName(), 
                                       sPresentationElementToCreate);
         
         }
      }
   }

   /**
    * Creates a Invalid DrawEngines entry.
    *
    * @param parent The owner of the table entry.
    * @param sElementType The name of the elements type.
    * @param sPresentationElementToCreate The name of the presentation element.
    */
   protected void createInvalidDrawEnginesEntry(Element parent,
                                                String  diagramString,
                                                String  invalidDrawEngines)
   {
      if((parent != null) && 
         (diagramString.length() > 0) && 
         (invalidDrawEngines.length() > 0))
      {
         Element node = XMLManip.createElement(parent,  getInvalidDrawEngineElementName());
         if(node != null)
         {
            XMLManip.setAttributeValue(node, 
                                       getDiagramIDAttributeName(), 
                                       diagramString);
                                 
            XMLManip.setAttributeValue(node, 
                                       getInvalidDrawEngineAttributeName(), 
                                       invalidDrawEngines);
      
         }
      }
   }
   
   /**
    * Creates an InitStrings entry.
    *
    * @param parent The owner of the table entry.
    * @param initializationString The initialization string contains the node to create 
    *                             any configuration information.
    * @param metaType The name of the meta type.
    * @param engineName The name of the draw engine
    * @param nObjectKind The kind of object that is being initialized.  Must be
    *                    one of the TSGraphObjectKind values.
    * 
    * @see TSGraphObjectKind
    */
   protected void createInitStringsEntry(Element parent,
                                         String  initializationString,
                                         String  metaType,
                                         String  engineName,
                                         int     nObjectKind)
   {
      createInitStringsEntry(parent, 
                             initializationString, 
                             metaType, 
                             engineName, 
                             nObjectKind, 
                             "");
   }
   
   /**
    * Creates an InitStrings entry.
    *
    * @param parent The owner of the table entry.
    * @param initializationString The initialization string contains the node to create 
    *                             any configuration information.
    * @param metaType The name of the meta type.
    * @param engineName The name of the draw engine
    * @param nObjectKind The kind of object that is being initialized.  Must be
    *                    one of the TSGraphObjectKind values.
    * @param diagramName The diagram type name.
    * 
    * @see TSGraphObjectKind
    */
   protected void createInitStringsEntry(Element parent,
                                         String  initializationString,
                                         String  metaType,
                                         String  engineName,
                                         int     nObjectKind,
                                         String  diagramName)
   {
      if((parent != null) && (initializationString.length() > 0))
      {
         Element node = XMLManip.createElement(parent,  getInitStringElementName());
         if(node != null)
         {
            XMLManip.setAttributeValue(node, 
                                       getInitStringIDAttributeName(), 
                                       Long.toString(getInitStringID(initializationString)));
                        
            XMLManip.setAttributeValue(node, 
                                       getDiagramIDAttributeName(), 
                                       Long.toString(getDiagramID(diagramName)));
                                       
            XMLManip.setAttributeValue(node, getMetaTypeAttributeName(), metaType);
            XMLManip.setAttributeValue(node, getEngineNameAttributeName(), engineName);
            XMLManip.setAttributeValue(node, getGraphObjectKindAttributeName(), Long.toString(nObjectKind));

         }
      }
   }
   
   /**
    * @param initString
    * @return
    */
   protected long getInitStringID(String initString)
   {
      long retVal = 0l;
      
      if (m_InitStringsTable.size() <= 0)
      {
         readInitStringsTable();
      }
      
      if(m_InitStringsTable.containsKey(initString) == true)
      {
         Long value = (Long)m_InitStringsTable.get(initString);
         if(value != null)
         { 
            retVal = value.longValue();
         }
      }
      return retVal;
   }
}
