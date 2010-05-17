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
 * ADRequirementsManager.java
 *
 * Created on June 24, 2004, 5:01 PM
 */

package org.netbeans.modules.uml.requirements;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterSupport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IRequirementArtifact;
import org.netbeans.modules.uml.core.requirementsframework.IReqProviderDialog;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementsProvider;
import org.netbeans.modules.uml.core.requirementsframework.ISatisfier;
import org.netbeans.modules.uml.core.requirementsframework.ReqProviderDialog;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsManager;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ElementReloader;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.commonresources.ICommonResourceManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.designpattern.IActionProvider;
import org.netbeans.modules.uml.designpattern.IDesignCenterSupportGUI;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.IDResolver;
import org.dom4j.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Trey Spiva
 */
public class ADRequirementsManager extends RequirementsManager
//implements IDesignCenterSupport, IDesignCenterSupportGUI, IViewActionDelegate, IActionProvider
implements IDesignCenterSupport, IDesignCenterSupportGUI, IActionProvider
{
   public static int sortPriority = 0;

   private ETList < IRequirementsProvider >  m_ReqProviderAddIns = null;

   private HashMap < String, IRequirementsProvider >  m_ProviderMap = new HashMap < String, IRequirementsProvider >();

   private HashMap < String, ETList < IRequirementSource > >  m_RequirementSourcesMap = new HashMap < String, ETList < IRequirementSource > >();

   // Use this guy to get icons
   private ICommonResourceManager m_ResourceMgr;

   // The version of this addin
   private long m_Version = 0;

   private static IProjectTreeControl m_ProjectTree = null;

   private IProjectTreeItem m_ReqManagerTreeItem = null;

   // The config directory
   private String m_ConfigLoc = "";

   // The file that houses the design pattern projects to load in the design center
   private String m_File = "";

   // Addin's name
   private String m_FriendlyName;

   // The event handler for the Requirements Manager.
   private static ReqEventsSink m_EventsSink = null;

   // The cookies associated with advising for the above sinks
   private long m_DrawingAreaEventsCookie = 0;
   private long m_CoreProductInitEventsCookie = 0;
   private long m_ProjectTreeEventsCookie = 0;

   private static String m_Location = "";
   private static ADRequirementsManager m_RequirementsManager = null;

//   static
//   {
//      String configLoc = ProductHelper.getConfigManager().getDefaultConfigLocation();
//      m_Location = configLoc + "..";
//      m_Location += File.separatorChar + "Addins";
//      m_Location += File.separatorChar + "DesignCenter";
//      m_Location += File.separatorChar + "org.netbeans.modules.uml.ui.products.ad.requirements";
//   }

   /** Creates a new instance of ADRequirementsManager */
   public ADRequirementsManager()
   {
      initialize();
   }


   ////////////////////////////////////////////////////////////////////////////
   // IAddin Methods

   public long initialize(Object context)
   {
      
      initialize();
      return 0;
   }
   
   public static ADRequirementsManager instance()
   {
       return m_RequirementsManager;
   }

   public void initialize()
   {
      super.initialize();
      m_ResourceMgr = CommonResourceManager.instance();

      if(m_RequirementsManager == null)
      {
         m_RequirementsManager = this;
      }

      createEventsSink();
   }
   
   public long deInitialize(Object context)
   {
      DispatchHelper helper = new DispatchHelper();
//      helper.revokeDrawingAreaSink(m_EventsSink);
      helper.revokeInitSink(m_EventsSink);
      helper.revokeProjectTreeSink(m_EventsSink);
      m_EventsSink = null;
      setProjectTree(null);

      return 0;
   }

   public long unLoad(Object context)
   {
      return 0;
   }

   public String getID()
   {
      return "org.netbeans.modules.uml.ui.products.ad.requirements.ADRequirementsManager";
   }
   
   public String getVersion()
   {
      return Long.toString(m_Version);
   }

   public String getLocation()
   {
      return m_Location;
   }

   public String getName()
   {
      return NbBundle.getMessage(ADRequirementsManager.class, "REQUIREMENTS_MANAGER_NAME");
   }

   ////////////////////////////////////////////////////////////////////////////
   // IDesignCenterSupport Methods
   
   /** save the design center addin */
    public void save()
    {
        // There is nothing to save.
    }
    
   ////////////////////////////////////////////////////////////////////////////
   // IDesignCenterSupportGUI Methods

   /**
    * Get the top level of the project tree for this addin.
    *
    * @retun The project tree
    */
   public IProjectTreeControl getProjectTree()
   {
      IProjectTreeControl retVal = null;
      if((m_RequirementsManager != null) &&
         (m_RequirementsManager != this))
      {
         retVal = m_RequirementsManager.getProjectTree();
      }
      else
      {
         retVal = m_ProjectTree;
      }
      return retVal;
   }

   /**
    * Set the top level of the project tree into this addin.
    *
    * @param newVal The new project tree
    */
   public void setProjectTree(IProjectTreeControl newVal)
   {
      if((m_RequirementsManager != null) &&
         (m_RequirementsManager != this))
      {
         m_RequirementsManager.setProjectTree(newVal);
      }
      else
      {
         m_ProjectTree = newVal;
      }
   }

   /**
    * Called upon a request to expand this addin's items in the tree.
    *
    * @param pParent The currently selected tree Item.
    */
   public void populateTreeItem(Object pParent)
   {
      if(pParent instanceof IProjectTreeItem)
      {
         // Save the tree item so that it will be available when the ProviderDialog
         // callback ADRequirementsManagerImpl::StoreAddIn(...) is called.
         m_RequirementsManager.setTreeItem((IProjectTreeItem)pParent);
         
         //m_ReqManagerTreeItem = (IProjectTreeItem)pParent;

         // Reset nSortPriority on Entry and on Refresh.
         sortPriority = 0;

         // If a RequirementSources.etd file exist, load the provider addins from
         // the .etd file.
         if(requirementSourcesExist() == true)
         {
            loadReqProviderAddins(m_RequirementsManager.getTreeItem());
         }
         //If the Requirements node has no children, the Requirements Provider dialog will no longer
         //be displayed.
         /*
         else
         {
            IReqProviderDialog dlg = new ReqProviderDialog();

            IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
            dlg.display(ui.getWindowHandle(), this);
         }
          */
      }
      else // Logic for Populating RequirementsProvider tree items.
      {

      }
   }

   public IProjectTreeItem getTreeItem()
   {
      return m_ReqManagerTreeItem;
   }

   public void setTreeItem(IProjectTreeItem item)
   {
      m_ReqManagerTreeItem = item;
   }

   ////////////////////////////////////////////////////////////////////////////
   // Protected methods

   /**
    * Loads the Requirements Provider addins from the registry key.
    */
   protected void loadAddIns()
   {
      // If the addins collection is NULL then I know that we have not previously
      // loaded the addins.  If the addins collection has been created but is
      // empty then we do not have any providers.
      if(m_ReqProviderAddIns == null)
      {
         m_ReqProviderAddIns = new ETArrayList < IRequirementsProvider >();

         IRequirementsProvider[] addins = getAddIns();
//         IAddInDescriptor[] descs = getAddInDescriptors();  
//         for(int index = 0; index < descs.length; index++)
         for(IRequirementsProvider provider : addins)
         {
//            IAddInDescriptor desc = descs[index];
//            IAddIn addin = initializeAddIn(desc, null, false); 

//            if(addin != null)
            {
//               if(addin instanceof IRequirementsProvider)
               {
//                  IRequirementsProvider reqProvider = (IRequirementsProvider)addin;
                  String progID = provider.getProgID();

//                  reqProvider.setProgID(progID);
                  m_ProviderMap.put(progID, provider);
               }
            }
         }
      }
   }

   /**
    * Creates the event sinks for the Requirements Manager. For example it sets
    * up the IDrawingAreaEventsSink to facilitate drag/drop of requirements onto
    * the drawing area.
    */
   protected void createEventsSink()
   {
      if(m_EventsSink == null)
      {
         m_EventsSink = new ReqEventsSink(m_RequirementsManager);

         DispatchHelper helper = new DispatchHelper();
//         helper.registerDrawingAreaEvents(m_EventsSink);
         helper.registerForInitEvents(m_EventsSink);
         helper.registerProjectTreeEvents(m_EventsSink);
      }
   }

   /**
    * Open the Requirement Sources file and load the Requirement Provider addins
    * that it specifies.
    *
    * @param treeItem The currently selected tree Item, parent of added
    *                 requirements providers.
    */
   protected void loadReqProviderAddins(IProjectTreeItem treeItem)
   {
      if(m_ReqSourcesFile.length() > 0)
      {
         // Load the RequirementSources.etd file into the DOM:its elements
         // will be used to create an addin for each Requirements provider.
         Document reqSorucesDoc = XMLManip.getDOMDocument(m_ReqSourcesFile, new IDResolver("id"));

         if(reqSorucesDoc != null)
         {
            List nodes = XMLManip.selectNodeList(reqSorucesDoc, "/RequirementSources/RequirementSource");
            if((nodes != null) && (nodes.size() > 0))
            {
               for(int index = 0; index < nodes.size(); index++)
               {
                  Node reqSourceNode = (Node)nodes.get(index);
                  if(reqSourceNode != null)
                  {
                     if(reqSourceNode instanceof Element)
                     {
                        Element reqSourceElem = (Element)reqSourceNode;
                        IRequirementSource reqSource = new org.netbeans.modules.uml.core.requirementsframework.RequirementSource();
                        convertXMLElementToReqSource(reqSourceElem, reqSource);

                        if(reqSource != null)
                        {
                           loadReqProviderAddin(treeItem, reqSource);
                        }
                     }
                  }
               }
            }

         }
      }
   }

   /**
    * Loads the Requirements Provider AddIn Source instance defined in the XML
    * <RequirementSource> element. The element is obtained from the
    * RequirementSources.etd file. Then, add the top-level contents of the
    * Requirements Source to the tree. Note: if content loading fails, the catch
    * logic will remove the Source's Tree Item from the tree.
    *
    * @param treeItem The currently selected tree Item, parent of new
    *                 requirement provider.
    * @param requirementSource The requirement source that represents a
    *                          requirements provider addin
    */
   protected void loadReqProviderAddin(IProjectTreeItem treeItem,
                                       IRequirementSource requirementSource)
   {
      IProjectTreeItem newItem = null;
      if((treeItem != null) && (requirementSource != null))
      {
         try
         {
            String progID = requirementSource.getProvider();
            IRequirementsProvider addin = getRequirementsProvider(progID);

            if(addin != null)
            {
               // Add a sub-item to the tree for this Requirements Source.
               newItem = loadTreeItem(treeItem, requirementSource);
               newItem.setAsAddinNode(true);

               // Add the requirement source to the list of RequirementSources
               // managed by this addin.
               addRequirementSourceContents(addin, newItem, requirementSource);
            }
         }
         catch(RequirementsException e)
         {
            if(e.getExceptionCode() == RequirementsException.RP_E_REQUIREMENTSOURCENOTFOUND)
            {
               Frame handle = null;

               IProxyUserInterface proxyUserInterface = ProductHelper.getProxyUserInterface();
               if(proxyUserInterface != null)
               {
                  handle = proxyUserInterface.getWindowHandle();
               }

               String displayName = requirementSource.getDisplayName();
               String msg = NbBundle.getMessage(ADRequirementsManager.class,"IDS_REQUIREMENTSOURCENOTFOUND", displayName);
               String msgTitle = NbBundle.getMessage(ADRequirementsManager.class,"IDS_REQUIREMENTSOURCENOTFOUNDTITLE");

               IQuestionDialog question = UIFactory.createQuestionDialog();
               question.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_OK,
                                                    MessageIconKindEnum.EDIK_ICONWARNING,
                                                    msg,
                                                    MessageResultKindEnum.SQDRK_RESULT_YES,
                                                    handle,
                                                    msgTitle);
            }

            if(newItem != null)
            {
               IProjectTreeItem[] pRemovedItems =
               {newItem};

               IProjectTreeControl ctrl = getProjectTree();
               if(ctrl != null)
               {
                  ctrl.removeFromTree(pRemovedItems);
               }
            }
         }
      }
   }


   /**
    *
    * Creates a sub-tree item for the passed in requirements source.
    *
    * @param *pTree[in] The parent tree item for new requirement provider Addin.
    * @param *pRequirementSource[in] Represents a requirements provider addin
    *
    * @return void
    *
    */

   public IProjectTreeItem loadTreeItem( IProjectTreeItem pTreeItem,
                                         IRequirementSource pRequirementSource )
   {
      IProjectTreeItem retVal = null;

      if( (null == pTreeItem) ||
          (pRequirementSource == null))
      {
         throw new IllegalArgumentException();
      }

      String strDispName = "";
      String strProgId = "";

      IProjectTreeControl ctrl = getProjectTree();
      if( ctrl != null)
      {
         strDispName = pRequirementSource.getDisplayName( );
         strProgId = pRequirementSource.getProvider( );

         //Action[] actions = {new AddSourceAction()};
         retVal = ctrl.addItem( pTreeItem,          // Parent tree item
                                "ReqProject",       // Program Name
                                strDispName,        // displayed (new) tree item name
                                1,                  // Sort Priority
                                null,               // IElement* this tree item is
                                strProgId);         // Description not used for Req Mgr.
         //retVal.setActions(actions);


         if( retVal != null )
         {
            // TODO: Figure out what to do about the setDispatch.
            ctrl.setDispatch( retVal, pRequirementSource);
            ctrl.setSecondaryDescription( retVal, strProgId );
            setImage( retVal,  "ReqProject"  );
         }
      }

      return retVal;
   }

   /**
    * Implements the IActionProvider interface.
    */
   public Action[] getActions()
   {
       Action[] actions = {new AddSourceAction()};
       return actions;
   }
   
   /**
    *
    * Determines if the RequirementsSouces.etd file exists,
    *
    * @return boolean Returns true if the file exists.
    *
    */
   public boolean requirementSourcesExist()
   {
      boolean bRtn = false;

      m_ReqSourcesFile = getReqSourcesFile();
      if( m_ReqSourcesFile.length() > 0)
      {
         bRtn = new File( m_ReqSourcesFile ).exists();
      }
      return bRtn;
   }

   /**
    *
    * Callback from IReqProviderDialog, when the "Display Souces" button is pressed
    * on the Requirements Provider Dialog. The ReqProviderDialogImpl is in
    * the RequirementsFramework project. See PopulateTreeItem
    * for how the dialog is created and this requirements manager is set to
    * recieve callbacks from the dialog.
    *
    * @param desc[in] Descriptor for the addin selected in the Requirements Provider dialog.
    *
    * @return void
    *
   void storeAddIn( IAddInDescriptor* desc )
   {
      void hr = S_OK;

      return hr;
   }
    */

   /**
    *
    * Adds the Source Provider entry to the etd and to the tree.
    * requirements provider addin that manages it.
    *
    * @param *pReqSource[in] Represents a requirements provider addin
    *
    * @return void
    *
    */
   public void processSource( IRequirementSource pRequirementSource ) throws RequirementsException
   {
      try
      {
         super.processSource( pRequirementSource );

         // Add a tree sub-element for this req source and put the requirements
         // source under management of the requirements source provider addin.
         if( m_RequirementsManager.getTreeItem() != null)
         {
            // TODO: Expand to Provider Description Node.
            loadReqProviderAddin( m_RequirementsManager.getTreeItem(), pRequirementSource );
         }

      }
      catch( RequirementsException e )
      {
         if( e.getExceptionCode() == RequirementsException.RP_E_DUPLICATESOURCE )
         {
            String msgText = NbBundle.getMessage(ADRequirementsManager.class, "IDS_DUPSOURCEMESSAGE");
            String msgTitle = NbBundle.getMessage(ADRequirementsManager.class, "IDS_DUPSOURCETITLE");

            Frame handle = ProductHelper.getProxyUserInterface().getWindowHandle();
            IQuestionDialog question = UIFactory.createQuestionDialog();
            question.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_OK,
                                                 MessageIconKindEnum.EDIK_ICONEXCLAMATION,
                                                 msgText,
                                                 MessageResultKindEnum.SQDRK_RESULT_OK,
                                                 handle,
                                                 msgTitle);
         }
         throw e;
      }
   }

   /**
    * Message from the sink that something has been selected
    */
   public void handleSelection( IProductContextMenu pContextMenu,
                                IProductContextMenuItem pSelectedItem )
   {
      if( null == pContextMenu && pSelectedItem == null)
      {
         throw new IllegalArgumentException();
      }

      try
      {
         if( pContextMenu != null)
         {
            Object pDisp = pContextMenu.getParentControl( );

            if( pDisp instanceof IProjectTreeControl)
            {
               IProjectTreeControl pTreeControl = (IProjectTreeControl)pDisp;
               IProjectTreeItem[] projectTreeItems = pTreeControl.getSelected();

               ETList < IProjectTreeItem > projectTreeItems2 = new ETArrayList < IProjectTreeItem >();

               String strButtonSource = pSelectedItem.getButtonSource();

               if( strButtonSource.equals("MBK_REMOVE_SATISFIER") )
               {
                  if( projectTreeItems.length > 0 )
                  {
                     String strNodeName;
                     String strParentNodeName;

                     for( int lIdx = 0; lIdx < projectTreeItems.length; lIdx++ )
                     {
                        // Get the Next Selected item.
                        IProjectTreeItem projectTreeItem = projectTreeItems[lIdx];

                        // Also get its Parent.
                        IProjectTreeItem parentProjectTreeItem = pTreeControl.getParent(projectTreeItem);

                        // Retrieve Names of Selected Node and its Parent.
                        strParentNodeName = parentProjectTreeItem.getItemText();
                        strNodeName = projectTreeItem.getItemText();

                        // Filter out only 'Satisfiers' (happens when a multi-range selection is made from tree).
                        if( strNodeName.equals(" Satisfiers") || strParentNodeName.equals(" Satisfiers") )
                        {
                           // Add elements to a second collection to be deleted later.
                           //	 projectTreeItems2.add( projectTreeItem )

                           if( strParentNodeName.equals(" Satisfiers") )
                           {

                              removeItemFromProxy( pTreeControl, projectTreeItem, parentProjectTreeItem );

                              // Add elements to a second collection to be deleted later.
                              projectTreeItems2.add( projectTreeItem );
                           }
                           else if( strNodeName.equals(" Satisfiers") )
                           {
                              // Remove all of the Children.
                              ETList < IProjectTreeItem > childProjectTreeItems = pTreeControl.getChildren( projectTreeItem );
                              long lChildCount = childProjectTreeItems.size();

                              // Iterate through all the children.
                              for( int childIndex = 0; childIndex < lChildCount; childIndex++ )
                              {
                                 IProjectTreeItem childProjectTreeItem = childProjectTreeItems.get( childIndex );

                                 // Delete the child from the Proxy file.
                                 removeItemFromProxy( pTreeControl, childProjectTreeItem, projectTreeItem );

                                 // Add elements to a second collection to be deleted later.
                                 projectTreeItems2.add( projectTreeItem );
                              }  // End For..Loop
                           }  // EndElse - Node is a Satisfiers folder
                        }  // EndIf - Node is a satisfier(s} folder

                     }  // EndIf - TreeItem selectedItems > 0

                     // Delete the items from the second 'filtered' collection.

                     if(projectTreeItems2.size() > 0)
                     {
                        IProjectTreeItem[] removeItems = new IProjectTreeItem[projectTreeItems2.size()];
                        projectTreeItems2.toArray(removeItems);
                        pTreeControl.removeFromTree( removeItems );
                     }
                  }

               }
               else if( strButtonSource.equals("MBK_ADD_REQUIREMENT_SOURCE") )
               {
                  // Display the Requirement Providers dialog.
                  IReqProviderDialog pDlg = new  ReqProviderDialog();
                  pDlg.display( null, this );

               } // End Switch
            }
         }
      }
      catch(RequirementsException e)
      {
         UMLMessagingHelper helper = new UMLMessagingHelper();
         helper.sendExceptionMessage(e);
      }
   }

   /**
    * Removes the Satisfer element from the Requirements Provider and also from
    * the Proxy file.
    *
    * @param pTreeControl [in] The DesignCenter's TreeControl
    * @param pProjectTreeItem [in] The Satisfier node
    * @param pParentProjectTreeItem [in] The 'Satisfiers Folder' node.
    */
   public void removeItemFromProxy( IProjectTreeControl pTreeControl,
                                    IProjectTreeItem pProjectTreeItem,
                                    IProjectTreeItem pParentProjectTreeItem )
      throws RequirementsException
   {
      if( null == pTreeControl ||
          pProjectTreeItem == null ||
          pParentProjectTreeItem == null)
      {
         throw new IllegalArgumentException();
      }

      try
      {
         // Satisfiers Folder node's parent is the Requirement.
         IProjectTreeItem  cpRequirementParentProjectTreeItem = pTreeControl.getParent( pParentProjectTreeItem );

         IRequirement cpRequirement = getRequirementFromTreeItem( cpRequirementParentProjectTreeItem );

         String strSatisfierXMIID = pProjectTreeItem.getSecondaryDescription( );

         ETList < ISatisfier >  cpSatisfiers = cpRequirement.getSatisfiers();

         long lSatisfierCount = cpSatisfiers.size();

         for( int lIdx = 0; lIdx < lSatisfierCount; lIdx++ )
         {
            ISatisfier cpSatisfier = cpSatisfiers.get(lIdx);

            String strSatisfierID = cpSatisfier.getXMIID();
            if( strSatisfierID.equals(strSatisfierXMIID) == true )
            {
               // Delete from IRequirement's collection.
               cpRequirement.removeSatisfier( cpSatisfier );

               // Delete from the Proxy File
               deleteProxy( cpRequirement, cpSatisfier );

               // Now remove the Requirement Artifact from the owning element.

               // Get the Element's ProjectID.
               String strProjectID = cpSatisfier.getProjectID();
               String strProjectName = cpSatisfier.getProjectName();

               // Get the current Application so we can ask it things.
               IApplication cpApplication = ProductHelper.getApplication();

               // Ask the App for the ProjectInstance using the element's toplevelid.
               IProject cpProject = cpApplication.getProjectByID( strProjectID );

               ElementReloader reloader = new ElementReloader();
               IElement pElement = reloader.getElement(strProjectID, strSatisfierID);
               if (pElement instanceof INamespace)
               {
                  INamespace  cpNamespace = (INamespace)pElement;
                  if( cpNamespace != null )
                  {
                     ETList < INamedElement > cpNamedElements = cpNamespace.getOwnedElements();

                     if( (cpNamedElements != null) && (cpNamedElements.size() > 0) )
                     {
                        long lCnt = cpNamedElements.size();

                        String strRequirementID = cpRequirement.getID();
                        String strRequirementSourceID = cpRequirement.getSourceID();

                        for( int lIndx=0; lIndx<lCnt; lIndx++ )
                        {
                           INamedElement cpNamedElement = cpNamedElements.get(lIndx);
                           if( cpNamedElement instanceof IRequirementArtifact )
                           {
                              IRequirementArtifact cpArtifact = (IRequirementArtifact)cpNamedElement;
                              String strArtifactRequirementID = cpArtifact.getRequirementID();

                              String strArtifactRequirementSourceID = cpArtifact.getRequirementSourceID();

                              if( (strRequirementID.equals(strArtifactRequirementID) == true) &&
                                  (strRequirementSourceID.equals(strArtifactRequirementSourceID) == true))
                              {
                                 cpArtifact.delete();
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
               break;
            }
         }
      }
      catch( RequirementsException err )
      {
         if( err.getExceptionCode() == RequirementsException.RP_E_SHAREDEDITNOTSUPPORTED )
         {
            Frame handle = null;

            IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
            if( cpProxyUserInterface != null)
            {
               handle = cpProxyUserInterface.getWindowHandle();
            }

            String msgText = err.getLocalizedMessage();
            String msgTitle = NbBundle.getMessage(ADRequirementsManager.class,"IDS_REQUIRENTERRORTITLE" );

            IQuestionDialog question = UIFactory.createQuestionDialog();
            question.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_OK,
                                                 MessageIconKindEnum.EDIK_ICONWARNING,
                                                 msgText,
                                                 MessageResultKindEnum.SQDRK_RESULT_OK,
                                                 handle,
                                                 msgTitle);
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////////////
   // IProjectTreeEventsSink

   /**
    * Forwarded msg from EventSink when user clicks on a TreeItem with an embedded
    * Requirement.
    */

   public void onItemExpanding( IProjectTreeItem pProjectTreeItem,
                                IRequirement pRequirement )
   {
      if( (null == pProjectTreeItem) || (pRequirement == null))
      {
         throw new IllegalArgumentException();
      }

      try
      {
         String strProviderID = pRequirement.getProviderID();
         String strSourceID = pRequirement.getSourceID();

         IRequirementSource cpRequirementSource = getSource( strSourceID);

         if( cpRequirementSource  != null)
         {
            ETList < IRequirement > cpRequirements = pRequirement.getSubRequirements( cpRequirementSource);

            if( cpRequirements != null)
            {
               loadTreeItems( pProjectTreeItem, strProviderID, strSourceID, cpRequirements );
            }

            // Create a child TreeItem under the TreeItem for each Satisfier.
            ETList < ISatisfier > cpSatisfiers = pRequirement.getSatisfiers( );

            if( cpSatisfiers != null)
            {
               long lSatisfierCount = cpSatisfiers.size();

               // Iterate through the Sources.
               for( int lIdx = 0; lIdx < lSatisfierCount; lIdx++ )
               {
                  ISatisfier cpSatisfier = cpSatisfiers.get( lIdx);

                  IProjectTreeItem cpNewSatisfierTreeItem = loadTreeItem( pProjectTreeItem, cpSatisfier);
               }
            }

         }
      }
      catch(RequirementsException e)
      {
         UMLMessagingHelper helper = new UMLMessagingHelper();
         helper.sendExceptionMessage(e);
      }
   }

   /**
    *
    * Add the passed in Requirements Source to the map of RequirementSources managed
    * by the Provider..
    *
    * @param pAddin[in] Requirments Provider addin to add the Requirement Source to.
    * @param pReqSouce[in] Requirements source to add to the addin.
    *
    */
   public void addRequirementSourceContents( IRequirementsProvider cpRequirementsProvider,
                                             IProjectTreeItem pTreeItem,
                                             IRequirementSource pRequirementSource )
      throws RequirementsException
   {
      if( null == cpRequirementsProvider || (pRequirementSource == null))
      {
         throw new IllegalArgumentException();
      }

      try
      {
//         if( pAddIn instanceof IRequirementsProvider)
         {
//            IRequirementsProvider cpRequirementsProvider = (IRequirementsProvider)pAddIn;

            String strProgID = cpRequirementsProvider.getProgID();
            String strRequirementSourceID = pRequirementSource.getID();

            // Get the Requirements Provider's associated RequirementSources.
            ETList < IRequirementSource > cpRequirementSources = m_RequirementSourcesMap.get( strProgID );

            if( cpRequirementSources != null )
            {
               cpRequirementSources.add( pRequirementSource );
            }
            else
            {
               cpRequirementSources = new ETArrayList < IRequirementSource >();
               cpRequirementSources.add( pRequirementSource );

               m_RequirementSourcesMap.put(strProgID, cpRequirementSources);
            }

            // Provider will use info in RequirementSource to create an IRequirements collection.
             ETList < IRequirement > cpRequirements = cpRequirementsProvider.loadRequirements( pRequirementSource);

            if(cpRequirements != null)
            {
               loadTreeItems( pTreeItem, strProgID, strRequirementSourceID, cpRequirements );
            }

         }
      }
      catch( RequirementsException  err )
      {
         // I do not want to do anything.
      }
   }

   /**
    * Recursively Load Requirements and their Requirements collections.
    *
    * @param pTreeItem[in] Starting TreeItem, item that received 'AddNode' event.
    * @param strProgID[in] ProgID extracted from the AddIn.
    * @param strRequirementSourceID [in] Requirements source ID from .etreq file
    * @param pRequirements[in] Requirements collection built by the Provider.
    *
    * @return void
    *
    */
   public void loadTreeItems( IProjectTreeItem pTreeItem,
                              String strProgID,
                              String strRequirementSourceID,
                              ETList < IRequirement > pRequirements )
   {
      if( (null == pTreeItem) || (pRequirements == null))
      {
         throw new IllegalArgumentException();
      }

      // Add all Requirements in the returned collection to the tree.
      // Iterate through the Requirements.
      Iterator < IRequirement > iter = pRequirements.iterator();
      while(iter.hasNext() == true)
      {
         IRequirement cpRequirement = iter.next();
         cpRequirement.setProviderID( strProgID );
         cpRequirement.setSourceID( strRequirementSourceID );

         // Create a TreeItem for each Requirement.
         IProjectTreeItem cpNewTreeItem = loadTreeItem( pTreeItem, cpRequirement);

         // Create a child TreeItem under the TreeItem for each SubRequirement.
         ETList < IRequirement > cpSubRequirements = cpRequirement.getRequirements();

         if( cpSubRequirements != null)
         {
            loadTreeItems( cpNewTreeItem, strProgID, strRequirementSourceID, cpSubRequirements );
         }

         // Create a child TreeItem under the TreeItem for each Satisfier.
         ETList < ISatisfier > cpSatisfiers = cpRequirement.getSatisfiers();
         if( cpSatisfiers != null)
         {
            // Iterate through the Sources.
            Iterator < ISatisfier > satisfierIter = cpSatisfiers.iterator();
            while(satisfierIter.hasNext() == true)
            {
               ISatisfier  cpSatisfier = satisfierIter.next();

               IProjectTreeItem  cpNewSatisfierTreeItem = loadTreeItem( cpNewTreeItem, cpSatisfier);
            }
         }
      }
   }

   /**
    *
    *	Revokes event sinks that were registered for the ADRequirements addin.
    *
    * @return void
    *
    */
   public void disconnectEventSinks()
   {
/*
   try
   {
      CDispatcherHelper oDispatcherHelper;
      oDispatcherHelper.revokeDrawingAreaSink( m_nDrawingAreaEventsCookie );
      oDispatcherHelper.revokeInitSink( m_nCoreProductInitEventsCookie );
   }
   catch( _com_error& err )
   {
      hr = COMErrorManager.reportError( err );
   }
 */
   }

   /**
    *
    * Handles the IDrawingAreaEventsSink OnDrawingAreaPreDrop event
    * for the ADRequirements Addin. This event is Fired right before
    * items are dropped onto the diagram.
    *
    * @param pParentDiagram[in] The diagram where the drop occured
    * @param pContext[in] Deatails of what has been dropped onto the diagram
    * @param cell[in] The result cell from the original event.
    *
    * @return void
    *
    */

// TODO: meteora
//   public void onDrawingAreaPreDrop( IDiagram pParentDiagram,
//                                     IDrawingAreaDropContext pContext,
//                                     IResultCell cell )
//   {
//   }

   /**
    *
    * Handles the IDrawingAreaEventsSink OnDrawingAreaPostDrop event
    * for the ADRequirements Addin. This event is Fired after items
    * are are dropped onto the diagram.
    *
    * @param pParentDiagram[in] The diagram where the drop occured
    * @param pContext[in] Deatails of what has been dropped onto the diagram
    * @param cell[in] The result cell from the original event.
    *
    * @return void
    *
    */
//   public void onDrawingAreaPostDrop( IDiagram pParentDiagram,
//                                      IDrawingAreaDropContext pContext,
//                                      IResultCell cell )
//   {
//      if( (null == pParentDiagram) || (pContext == null))
//      {
//         throw new IllegalArgumentException();
//      }
//
//      // The presentation element dropped upon, could be null.
//      IPresentationElement  cpPresElement = pContext.getPEDroppedOn();
//
//      if( cpPresElement != null)
//      {
//	  ETArrayList<IPresentationElement> satisfiers = new  ETArrayList<IPresentationElement> (1);
//	  satisfiers.add(cpPresElement);
//
//	  // Get the DesignCenter's TreeControl.
//	  IProjectTreeControl cpProjectTreeControl = ProductHelper.getDesignCenterTree();
//	  
//	  // Ask the TreeControl for its selected items.
//	  IProjectTreeItem[] projectTreeItems = cpProjectTreeControl.getSelected();
//
//	  ETArrayList<IProjectTreeItem> itemsList = new ETArrayList<IProjectTreeItem>(projectTreeItems.length);
//
//	  for( int lIdx = 0; lIdx < projectTreeItems.length; lIdx++ )
//	  {
//	      IProjectTreeItem  projectTreeItem = projectTreeItems[lIdx];
//	      if (projectTreeItem != null) 
//		  itemsList.add(projectTreeItem);
//	  }
//	  
//	  addSatisfiers(satisfiers, itemsList);
//
//      } // EndIf - We have been dropped on a Presentation Element
//
//   }

   /**
    *  Associates the satisfiers with the requirements
    *
    */
   public void addSatisfiers(List<IPresentationElement> satisfiers, List<IProjectTreeItem> projectTreeItems)
   {

       if (satisfiers == null || projectTreeItems == null) {
	   return;
       }

       Iterator<IPresentationElement> iter = satisfiers.iterator();
       while(iter.hasNext()) 
       {
	   IPresentationElement cpPresElement = iter.next();

      if( cpPresElement != null)
      {
         // Ask the PresentationElement for its model element.
         IElement cpElement = cpPresElement.getFirstSubject();

         if( cpElement != null)
         {
            String strElementName = "";
            String strElementType = "";
            String strElementXMIID = "";

            if( cpElement instanceof INamedElement)
            {
               INamedElement cpNamedElement = (INamedElement)cpElement;

               strElementName = cpNamedElement.getName();
               strElementType = cpNamedElement.getElementType();
               strElementXMIID = cpNamedElement.getXMIID();
               if((strElementName == null) || (strElementName.length() <= 0 ))
               {
                  strElementName = "Unnamed ";
                  strElementName += strElementType;
               }
            }

		   Iterator<IProjectTreeItem> itemsIter = projectTreeItems.iterator();
		   while(itemsIter.hasNext())
		   {
		       IProjectTreeItem projectTreeItem = itemsIter.next();

               IRequirement cpRequirement = getRequirementFromTreeItem( projectTreeItem );

               if( cpRequirement  != null)
               {
                  String strRequirementID = cpRequirement.getID();
                  String strRequirementName = cpRequirement.getName();
                  String strRequirementProviderID = cpRequirement.getProviderID();
                  String strRequirementSourceID = cpRequirement.getSourceID();
                  String strRequirementProjectName = cpRequirement.getProjectName();
                  String strRequirementModName = cpRequirement.getModName();

                  // Add the requirement info as a RequirementArtifact on the model element
                  // that the requirement was dropped on.
                  TypedFactoryRetriever < IRequirementArtifact >fact = new TypedFactoryRetriever < IRequirementArtifact >();
                  IRequirementArtifact cpRequirementArtifact = fact.createType("RequirementArtifact");

                  if( cpRequirementArtifact != null)
                  {
                      cpRequirementArtifact.setRequirementID(strRequirementID);
                      cpRequirementArtifact.setName(strRequirementName);
                      cpRequirementArtifact.setRequirementProviderID(strRequirementProviderID);
                      cpRequirementArtifact.setRequirementSourceID(strRequirementSourceID);
                      cpRequirementArtifact.setRequirementProjectName(strRequirementProjectName);
                      cpRequirementArtifact.setRequirementModName(strRequirementModName);
                  }

                  // Add the Artifact to the Model Element.  If the cpElement is a namespace
                  // then call add owned element so the element added to namespace event gets fired
                  if(cpElement instanceof INamespace)
                  {
                     INamespace cpNamespaceElement = (INamespace)cpElement;
                     cpNamespaceElement.addOwnedElement( cpRequirementArtifact );
                  }
                  else
                  {
                     cpElement.addElement( cpRequirementArtifact );
                  }

                  // Get the Element's ProjectID.
                  String strTopLevelID = cpElement.getTopLevelId();

                  // Get the current Application so we can ask it things.
                  IApplication cpApplication = ProductHelper.getApplication();

                  // Ask the App for the ProjectInstance using the element's toplevelid.
                  IProject cpProject = cpApplication.getProjectByID( strTopLevelID);

                  // Get the name of the project.
                  String strProjectName = cpProject.getName();

                  ISatisfier cpSatisfier = new org.netbeans.modules.uml.core.requirementsframework.Satisfier();
                  cpSatisfier.setName( strElementName );
                  cpSatisfier.setType( strElementType );
                  cpSatisfier.setXMIID( strElementXMIID );
                  cpSatisfier.setProjectName( strProjectName );
                  cpSatisfier.setProjectID( strTopLevelID );

                  // Add Satisfier element to the Owning Requirement element in the Proxy file.
                  processProxy( cpRequirement, cpSatisfier );

                  try
                  {
                     // Tell Requirement to Add the Satisfer to its collection.
                     cpRequirement.addSatisfier( cpSatisfier );

                     // Add a 'SatisfiedBy' node to the ProjectTreeItem.
                     IProjectTreeItem cpNewTreeItem = loadTreeItem( projectTreeItem, cpSatisfier);

                  }
                  catch( RequirementsException err )
                  {
                     if( err.getExceptionCode() == RequirementsException.RP_E_SHAREDEDITNOTSUPPORTED )
                     {
                        Frame handle = null;

                        IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
                        if( cpProxyUserInterface != null)
                        {
                           handle = cpProxyUserInterface.getWindowHandle();
                        }

                        String msgText = err.getLocalizedMessage();
                        String msgTitle = NbBundle.getMessage(ADRequirementsManager.class,"IDS_REQUIRENTERRORTITLE" );

                        IQuestionDialog question = UIFactory.createQuestionDialog();
                        question.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_OK,
                                                             MessageIconKindEnum.EDIK_ICONWARNING,
                                                             msgText,
                                                             MessageResultKindEnum.SQDRK_RESULT_OK,
                                                             handle,
                                                             msgTitle);
                     }
                  }

               } // EndIf - TreeItem has an IRequirement instance in its DispatchElement

            } // End While..Loop - Project Tree Iteration

         } // EndIf - We are a Model Element

      } // EndIf -  Presentation Element

       } // End While..Loop - satisfiers PEs iteration

   }

   /**
    *
    * Handles the OnDrawingAreaPostDrop event
    * for the ADRequirementsManager Addin. This event is fired on when
    * the user right-clicks in the DesignCenter TreeControl.
    *
    * @param pMenu[in] The Context Menu
    * @return The menu titles
    */
   public ETList <String>  onProjectTreeContextMenuPrepare( IProductContextMenu pMenu)
   {
      // This implementation will not return any menu titles.
      ETList <String> retVal = new ETArrayList <String>();
//
//      Object parent = pMenu.getParentControl( );
//
//      // Ensure that we come from a tree control.
//      if( parent instanceof  IProjectTreeControl)
//      {
//         IProjectTreeControl pTreeControl = (IProjectTreeControl)parent;
//
//         IProjectTreeItem[] projectTreeItems = pTreeControl.getSelected();
//         if( projectTreeItems.length > 0 )
//         {
//            // Get the First Selected item.
//            IProjectTreeItem projectTreeItem = projectTreeItems[0];
//
//            // Retrieve Names of Selected Node and its Parent.
//            String strNodeName = projectTreeItem.getItemText();
//
//            IProjectTreeItem parentProjectTreeItem = pTreeControl.getParent(projectTreeItem);
//
//            if( parentProjectTreeItem != null)
//            {
//               String strParentNodeName = parentProjectTreeItem.getItemText();
//
//               String strAddInFriendlyName = NbBundle.getMessage(ADRequirementsManager.class,"IDS_ADDIN_FRIENDLY_NAME");
//
//               if( (strNodeName.equals(strAddInFriendlyName) == true) ||
//                    (strParentNodeName.equals(strAddInFriendlyName) == true))
//               {
//                  // Get the text for the apply button.
//                  String name = m_Bundle.getString("IDS_ADD_REQUIREMENTSOURCE" );
//
//                  // Ddetermine whether or not this button should be greyed out or not.
//                  boolean bSensitive = true;
//
//                  // Get the SubMenus of the passed in menu.
//                  ETList < IProductContextMenuItem > pMenuItems = pMenu.getSubMenus();
//
//                  if(pMenuItems != null)
//                  {
//                     // Create the menu item.
//                     IProductContextMenuItem pTemp = new ProductContextMenuItem();
//                     pTemp.setMenuString( name );
//                     pTemp.setButtonSource(  "MBK_ADD_REQUIREMENT_SOURCE"  ); //NOI18N
//                     pTemp.setSelectionHandler( m_EventsSink );
//                     pTemp.setSensitive( bSensitive );
//                     pMenuItems.add( pTemp );
//                  }
//
//               }
//
//               if( strNodeName.equals(" Satisfiers") || strParentNodeName.equals(" Satisfiers") )
//               {
//                  // Get the text for the apply button.
//                  String name = m_Bundle.getString("IDS_REMOVE_SATISFIER");
//
//                  // Ddetermine whether or not this button should be greyed out or not.
//                  boolean bSensitive = true;
//
//                  // Get the SubMenus of the passed in menu.
//                  ETList < IProductContextMenuItem > pMenuItems = pMenu.getSubMenus();
//
//                  if( pMenuItems != null)
//                  {
//                     // Create the menu item.
//                     IProductContextMenuItem  pTemp = new ProductContextMenuItem();
//
//                     if( pTemp != null)
//                     {
//                        pTemp.setMenuString( name );
//                        pTemp.setButtonSource(  "MBK_REMOVE_SATISFIER"  );
//                        pTemp.setSelectionHandler( m_EventsSink );
//                        pTemp.setSensitive( bSensitive );
//                        pMenuItems.add( pTemp );
//                     }
//                  }
//               }
//            }
//         }
//      }
//
      return retVal;
   }

   public void onProjectTreeContextMenuPrepared(IProductContextMenu pMenu)
   {
   }

   /**
    * Extracts the Description attribute from the passed in XML string. The XML
    * String is returned from the clipboard after a drag/drop is performed.
    *
    * @param strXML[in] An XML String from the clipboard after a drag/drop
    * @return The returned value of the Description attribute.
    */
   public String extractDescriptionAttribute( String strXML)
   {
      String retVal = "";


      Document xmlDoc = XMLManip.loadXML(strXML);

      if( xmlDoc != null)
      {

         String strtPattern = "/DRAGGEDITEMS/GENERICELEMENT/@DESCSTRING";
         Node node = XMLManip.selectSingleNode(xmlDoc, strtPattern);

         if( node != null)
         {
            retVal = node.getText();
         }
      }

      return retVal;
   }

   /**
    *
    * Add a sub-tree item to the passed in IProjectTreeItem.
    *
    * @param pParent[in] Parent Tree item.
    * @param pSatisfier[in] Name of Satisifer to find or add
    * @return The newly created sub-tree item.
    */
   public IProjectTreeItem loadTreeItem( IProjectTreeItem pParent,
                                         ISatisfier pSatisfier)
   {
      IProjectTreeItem retVal = null;

      if(null == pParent)
      {
         throw new IllegalArgumentException();
      }


      boolean bRequirementIsSatisfied = false;

      String strName = pSatisfier.getName();
      String strXMIID = pSatisfier.getXMIID();

      IProjectTreeItem projectTreeItem = null;

      IProjectTreeControl ctrl = getProjectTree();
      if(ctrl != null)
      {
         ETList < IProjectTreeItem > projectTreeItems = ctrl.getChildren( pParent);

         // Get Child folder item 'Satisfier' and create it if it doesn't exist.
         long lChildCount =  projectTreeItems.size();

         if( lChildCount == 0 )
         {
            // Then there is no 'Satisfiers' container folder, so add it.
            projectTreeItem = loadTreeItem( pParent, " Satisfiers", " Satisfiers");
         }
         else
         {
            // Search children for the Satisfiers folder (some children may be SubRequirements).

            boolean bFoundSatisfersContainer = false;

            // Iterate through all the children, find container folder.
            for( int lIdx = 0; lIdx < lChildCount; lIdx++ )
            {
               IProjectTreeItem cpChildProjectTreeItem = projectTreeItems.get(lIdx);

               String strNodeName = cpChildProjectTreeItem.getItemText();
               if( strNodeName.equals(" Satisfiers") )
               {
                  projectTreeItem = cpChildProjectTreeItem;
                  bFoundSatisfersContainer = true;
                  break;
               }

            }

            if ( ! bFoundSatisfersContainer )
            {
               // Then there is no 'Satisfiers' container folder, so add it.
               projectTreeItem = loadTreeItem( pParent, " Satisfiers", " Satisfiers");
            }
            else
            {
               // Check that the Requirement is not already in Satisfier folder (Prevent adding duplicate).
               ETList<IProjectTreeItem> cpSatisfierProjectTreeItems = ctrl.getChildren( projectTreeItem);

               // Check each Satisfer node to see if the model element is already satisfying the Requirement.
               for( int lIdx = 0; lIdx < cpSatisfierProjectTreeItems.size(); lIdx++ )
               {
                  IProjectTreeItem  cpSatisfierProjectTreeItem = cpSatisfierProjectTreeItems.get(lIdx);
                  String strSatisfierXMIID = cpSatisfierProjectTreeItem.getSecondaryDescription();

                  // If the name of any child within <Satisfiers> is the same as the input (ModelElement) arg,
                  // then this Requirement is already satisified by the model element.
                  if( strXMIID.equals(strSatisfierXMIID) == true )
                  {
                     bRequirementIsSatisfied = true;
                     break;
                  }
               }

            }

         }
      }

      if( ! bRequirementIsSatisfied )
      {
         retVal = loadTreeItem( projectTreeItem, strName, strXMIID);
      }


      return retVal;
   }

   /**
    *
    * Add a sub-tree item to the passed in IProjectTreeItem.
    *
    * @param pTreeItem[in] Parent Tree item.
    * @param strProgID[in] Name of the RequirementsProvider managing the new sub-tree item.
    * @param strRequirementSourceID[in] ID of RequirementsSource
    * @param pRequirement[in] an instance of IRequirement
    * @param ppNewTreeItem[in] Return the newly created sub-tree item.
    *
    * @return void
    *
    */
   public IProjectTreeItem loadTreeItem( IProjectTreeItem pTreeItem,
                                         IRequirement pRequirement)
   {
      IProjectTreeItem retVal = null;
      if( (null == pTreeItem) || (pRequirement == null))
      {
         throw new IllegalArgumentException();
      }

      IProjectTreeControl ctrl = getProjectTree();
      if( ctrl != null)
      {
         String strRequirementName = pRequirement.getName();
         String strRequirementType = pRequirement.getType( );

         String programName = "ReqRequirement";
         if( strRequirementType.equals("Category") == true)
         {
            programName = "ReqCategory";
         }
         retVal = ctrl.addItem( pTreeItem,          // Parent tree item
                                programName,
                                strRequirementName, // Displayed tree item name
                                ++sortPriority,    // Sort Priority
                                null,              // IElement* this tree item represents
                                strRequirementName); // Description.

         if(retVal != null)
         {
            // Add the text of the requirement
            ctrl.setDispatch( retVal, pRequirement);
            setImage( retVal,  programName  );
            
//            Action[] actions = { new RemoveSatisfierAction(retVal, ctrl) };
//            retVal.setActions(actions);
         }
      }

      return retVal;
   }

   /**
    *
    * Add a sub-tree item to the passed in IProjectTreeItem.
    *
    * @param pTreeItem[in] Parent Tree item.
    * @param strName[in] Name of either 1) New folder (will be equal to "Satisfiers"), or
                                     2) the model element name being satisfied
    * @param strXMIID[in] ID of model element
    * @param ppNewTreeItem[in] Return the newly created sub-tree item.
    *
    * @return void
    *
    */
   public IProjectTreeItem loadTreeItem( IProjectTreeItem pTreeItem,
                                          String strName,
                                          String strXMIID)
   {
      IProjectTreeItem retVal = null;

      if( null == pTreeItem)
      {
         throw new IllegalArgumentException();
      }

      IProjectTreeControl ctrl = getProjectTree();
      if( ctrl != null)
      {
         String prgName = "ReqModelElement";
         if( strName.equals(" Satisfiers") == true)
         {
            prgName = "ReqModelElementFolder";
         }

         retVal = ctrl.addItem( pTreeItem, // Parent tree item
                                prgName,   // Program Name
                                strName,   // Displayed tree item name
                                1,         // Sort Priority
                                null,      // IElement* this tree item represents
                                strXMIID); // Not used for Req Provider.
         

         if( retVal != null )
         {
             Action[] actions = { new RemoveSatisfierAction(retVal, ctrl) };
             retVal.setActions(actions);
         
            // Add the text of the requirement.
            if( strName.equals(" Satisfiers") == true)
            {
               String strItemText = pTreeItem.getItemText();
               ctrl.setSecondaryDescription( retVal, strItemText );

               setImage( retVal,  "ReqModelElementFolder"  );
            }
            else
            {
               ctrl.setSecondaryDescription( retVal, strXMIID );
               setImage( retVal,  "ReqModelElement"  );
            }
         }

      }

      return retVal;
   }

   public IRequirement getRequirementFromTreeItem( IProjectTreeItem pTreeItem)
   {
      IRequirement retVal = null;

      if( null == pTreeItem)
      {
         throw new IllegalArgumentException();
      }


      Object cpDispatch = pTreeItem.getData();

      if( cpDispatch instanceof IRequirement )
      {
         retVal = (IRequirement)cpDispatch;
      }

      return retVal;
   }
   /**
    * Given a key String this looks the String up in the resource manager and returns
    * the icon that should be used for that type.
    *
    * @param pTreeItem [in] Contains the HTREEITEM that is to receive the icon
    * @param sIconType [in] The keyString that should be used to lookup the icon (usually the element name or folder name)
    */
   public void setImage( IProjectTreeItem pTreeItem,
                         String sIconType )
   {
      if((pTreeItem == null) ||
         (sIconType.length() <= 0))
      {
         throw new IllegalArgumentException();
      }

      boolean bFound = false;

      IProjectTreeControl ctrl = getProjectTree();
      if ((m_ResourceMgr != null) &&
          (ctrl != null))
      {
         long nIcon = 0;

         ctrl.setNodeName(pTreeItem, sIconType);
//         String sLibrary = m_ResourceMgr.getIconDetailsForElementType(sIconType);
//         CommonResourceManager resource = CommonResourceManager.instance();
//         Icon c = resource.getIconForElementType(sIconType);
//
//         //if (nIcon && sLibrary.length() )
//         if (sLibrary.length() > 0)
//         {
////            ctrl.setImage(pTreeItem,
////                          sLibrary,
////                          nIcon);
//            bFound = true;
//         }
      }

      if (! bFound )
      {
         //     UMLMessagingHelper messageService(_Module.getModuleInstance(), IDS_MESSAGINGFACILITY);
         //     messageService.sendWarningMessage(_Module.getModuleInstance(), IDS_COULDNOTSETIMAGE ) ;
      }
   }


   ////////////////////////////////////////////////////////////////////////////
   // IViewDelegate Methods

   public void init(ApplicationView view)
   {
   }

   public void run(ActionEvent e)
   {
      if(e.getActionCommand().equals(NbBundle.getMessage(ADRequirementsManager.class, "IDS_ADD_REQUIREMENTSOURCE")) == true)
      {
         IReqProviderDialog dlg = new ReqProviderDialog();

         IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
         dlg.display(ui.getWindowHandle(), this);
      }
      else if(e.getActionCommand().equals(NbBundle.getMessage(ADRequirementsManager.class, "IDS_REMOVE_SATISFIER")) == true)
      {
         IProjectTreeControl tree = m_RequirementsManager.getProjectTree();
         if((tree != null) && (tree.getConfigMgrName().equals("DesignCenter") == true))
         {
            IProjectTreeItem[] selItems = tree.getSelected();
            if(selItems.length >= 1)
            {
               removeSatisfiers(tree, selItems);
            }
         }
      }
   }

//   public void selectionChanged(PluginAction action, ISelection selection)
//   {
//   }

//   public boolean validate(ApplicationView view, IContributionItem item, IMenuManager mgr)
//   {
//      boolean retVal = false;
//
//      if(view instanceof IProjectTreeControl)
//      {
//         IProjectTreeControl tree = (IProjectTreeControl)view;
//         if(tree.getConfigMgrName().equals("DesignCenter") == true)
//         {
//            IProjectTreeItem[] selItems = tree.getSelected();
//            if((selItems != null) && (selItems.length >= 1))
//            {
//               String label = item.getLabel();
//               if(label.equals(NbBundle.getMessage(ADRequirementsManager.class, "IDS_ADD_REQUIREMENTSOURCE")) == true)
//               {
//                  if(selItems.length == 1)
//                  {
//                     Object data = selItems[0].getData();
//                     if(data instanceof IRequirementSource)
//                     {
//                        retVal = true;
//                     }
//                     else if(data instanceof IRequirementsManager)
//                     {
//                        retVal = true;
//                     }
//                  }
//               }
//               else if(label.equals(NbBundle.getMessage(ADRequirementsManager.class, "IDS_REMOVE_SATISFIER")) == true)
//               {
//                  ITreeItem[] path = selItems[0].getPath();
//                  ITreeItem selItem = path[path.length - 1];
//                  String itemName = selItem.getName();
//                  if((itemName.equals("ReqModelElement") == true) ||
//                     (itemName.equals("ReqModelElementFolder") == true))
//                  {
//                     retVal = true;
//                  }
//               }
//            }
//
//         }
//      }
//
//      return retVal;
//   }

   protected void removeSatisfiers(IProjectTreeControl pTreeControl, IProjectTreeItem[] projectTreeItems)
   {
      if( projectTreeItems.length > 0 )
      {
         try
         {
            String strNodeName;
            String strParentNodeName;

            ETList < IProjectTreeItem > projectTreeItems2 = new ETArrayList < IProjectTreeItem >();
            for( int lIdx = 0; lIdx < projectTreeItems.length; lIdx++ )
            {
               // Get the Next Selected item.
               IProjectTreeItem projectTreeItem = projectTreeItems[lIdx];

               // Also get its Parent.
               IProjectTreeItem parentProjectTreeItem = pTreeControl.getParent(projectTreeItem);

               // Retrieve Names of Selected Node and its Parent.
               strParentNodeName = parentProjectTreeItem.getItemText();
               strNodeName = projectTreeItem.getItemText();

               // Filter out only 'Satisfiers' (happens when a multi-range selection is made from tree).
               if( strNodeName.equals(" Satisfiers") || strParentNodeName.equals(" Satisfiers") )
               {
                  // Add elements to a second collection to be deleted later.
                  //	 projectTreeItems2.add( projectTreeItem )

                  if( strParentNodeName.equals(" Satisfiers") )
                  {

                     removeItemFromProxy( pTreeControl, projectTreeItem, parentProjectTreeItem );

                     // Add elements to a second collection to be deleted later.
                     projectTreeItems2.add( projectTreeItem );
                  }
                  else if( strNodeName.equals(" Satisfiers") )
                  {
                     // Remove all of the Children.
                     ETList < IProjectTreeItem > childProjectTreeItems = pTreeControl.getChildren( projectTreeItem );
                     long lChildCount = childProjectTreeItems.size();

                     // Iterate through all the children.
                     for( int childIndex = 0; childIndex < lChildCount; childIndex++ )
                     {
                        IProjectTreeItem childProjectTreeItem = childProjectTreeItems.get( childIndex );

                        // Delete the child from the Proxy file.
                        removeItemFromProxy( pTreeControl, childProjectTreeItem, projectTreeItem );

                        // Add elements to a second collection to be deleted later.
                        projectTreeItems2.add( projectTreeItem );
                     }  // End For..Loop
                  }  // EndElse - Node is a Satisfiers folder
               }  // EndIf - Node is a satisfier(s} folder

            }  // EndIf - TreeItem selectedItems > 0

            // Delete the items from the second 'filtered' collection.

            if(projectTreeItems2.size() > 0)
            {
               IProjectTreeItem[] removeItems = new IProjectTreeItem[projectTreeItems2.size()];
               projectTreeItems2.toArray(removeItems);
               pTreeControl.removeFromTree( removeItems );
            }
         }
         catch(RequirementsException e)
         {

         }
      }
   }
}
