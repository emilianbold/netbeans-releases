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


package org.netbeans.modules.uml.ui.addins.diagramcreator;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IStructuralDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.UMLParsingIntegrator;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageService;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADRelationshipDiscovery;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.ThermProgress;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypeDetails;
import org.netbeans.modules.uml.ui.support.applicationmanager.TSGraphObjectKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IPromptDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.ProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingPromptDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;

/**
 * @author sumitabhk
 *
 */
//public class DiagCreatorAddIn implements IDiagCreatorAddIn, IAddIn, IViewActionDelegate, IAcceleratorListener, IDrawingAreaEventsSink
public class DiagCreatorAddIn implements IDiagCreatorAddIn, IAcceleratorListener, IDrawingAreaEventsSink
{
   //enum ChildRetrievalBehavior
   public static int CRB_NONE = 0;
   public static int CRB_GET_ALL = 1;
   public static int CRB_GET_STATE_CHILDREN = 2;

   private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.addins.diagramcreator.Bundle"; //$NON-NLS-1$

   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

   private IMessageService m_MessageService = null;
   private AddInEventSink m_EventsSink = null;
   private DispatchHelper m_Helper = new DispatchHelper();
   private String m_Version = "1";
   private Vector < DiagramHandler > m_Callbacks = new Vector < DiagramHandler >();
   private IUMLParsingIntegrator m_UMLParsingIntegrator = null;
   private ApplicationView m_View = null;
   private boolean m_bAcceleratorsRegistered = false;
   private static JComponent m_hook = null;

   /**
    * 
    */
   public DiagCreatorAddIn()
   {
      super();
   }

   /**
    * Executes the 'Create Structural Diagram From All Elements' button
    *
    * @param pExistingDiagram [in] If this argument is non-NULL then this diagram is used to create the presentation
    * elements.  If this argument is NULL the the user is asked to create a diagram and that one is used.
    */
   public long guiCreateDiagramFromProjectTreeElements(IProjectTreeControl pProjectTree)
   {
      // Determine the selected elements from the (input) project tree
      ETList < IElement > cpElems = getSelectedElements(pProjectTree);

      guiCreateDiagramFromElements(cpElems, null, pProjectTree);
      return 0;
   }

   /**
    * Creates a diagram from the input elements
    */
   public long guiCreateDiagramFromElements(ETList < IElement > pElements,                 
                                            IElement pParentElement, 
                                            IProjectTreeControl pProjectTree)
   {
      IElement firstSelModEle = pParentElement != null ? pParentElement : getFirstElement(pElements);
      if (firstSelModEle != null)
      {
         ETSmartWaitCursor waitCursor = null;
         try
         {
            waitCursor = new ETSmartWaitCursor();

            DiagramHandler handler = createDiagramCallback(pElements, firstSelModEle, pProjectTree, true);
            
            if(pProjectTree != null)
            {
               String name = pProjectTree.getConfigMgrName();
               if (name != null && name.equals("DesignCenter"))
               {
                  // if we are in the design center, we need to set up the context for the new dialog
                  // a little differently than if we were in the project area
               	  //This has to be uncommented after resolving cyclic dependency issues
                  //setUpNewDialogContext(pProjectTree);
                  waitCursor.restore();

                  guiCreateDiagram(handler);
               }
               else
               {
                  guiCreateDiagram(handler);
               }
            }
            else
            {
               guiCreateDiagram(handler);
            }
         }
         finally
         {
            waitCursor.stop();
         }
      }

      return 0;
   }

   public long guiCreateDiagramFromElements(ETList < IElement > pElements,                 
                                            IElement pParentElement, 
                                            IProjectTreeModel pProjectTreeModel)                                         
   {
      IElement firstSelModEle = pParentElement != null ? pParentElement : getFirstElement(pElements);
      if (firstSelModEle != null)
      {
         ETSmartWaitCursor waitCursor = null;
         try
         {
            waitCursor = new ETSmartWaitCursor();
            DiagramHandler handler = createDiagramCallback(pElements, firstSelModEle, pProjectTreeModel, true);
            guiCreateDiagram(handler);
         }
         finally
         {
            waitCursor.stop();
         }
      }
      return 0;
   }
   
   /**
    * Creates diagram using the default mechanism
    *
    * @param pDiagram [in] The diagram that should be created
    * @param pElements [in] The selected elements on the tree
    * @param pFirstSelectedElement [in] The first selected element
    */
   private void defaultCreateDiagramMechanism(IDiagram pDiagram, ETList < IElement > pElements)
   {
      IteratorT < IElement > removePortsIter = new IteratorT < IElement > (pElements);
      while (removePortsIter.hasNext())
      {
         IElement elem = removePortsIter.next();
         String elemType = elem.getElementType();
         if (elemType != null && elemType.equals("Port"))
         {
            removePortsIter.remove();
         }
      }
      createPresentationElements(pDiagram, pElements);
   }

   /**
    * If pDiagram is a component diagram then this guy does the CDFS
    *
    * @param pDiagram [in] The diagram that should be created
    * @param pElements [in] The selected elements on the tree
    * @param pFirstSelectedElement [in] The first selected element
    */
   private boolean createComponentDiagram(IDiagram pExistingDiagram, ETList < IElement > pElements)
   {
      boolean handled = false;
      if (pExistingDiagram != null && pElements != null)
      {
         int nKind = IDiagramKind.DK_UNKNOWN;
         nKind = pExistingDiagram.getDiagramKind();
         if (nKind == IDiagramKind.DK_COMPONENT_DIAGRAM)
         {
            IComponentDiagramCreator pCreator = new ComponentDiagramCreator();
            handled = pCreator.generate(pElements, pExistingDiagram);
         }
      }
      return handled;
   }

   /**
    * Creates the specified diagram, and adds the input elements to the diagram.
    *
    * @param diagramKind[in]  Kind of diagram to create
    * @param pNamespace[in]   Namespace for the new diagram.  Allowed to be NULL if the diagram is passed in.
    * @param sDiagramName[in] Name of the new diagram.
    * @param pElements[in]    Elements to be added to the new diagram.
    * @param pCreatedDiagram[in] The created diagram.  If the diagram is passed in
    *
    * @return HRESULT
    *
    * @warning This function does not ensure the elements are valid for the diagram.
    */
   public IDiagram createDiagramForElements(int diagramKind, INamespace pNamespace, String sDiagramName, ETList < IElement > pElements, IDiagram pCreatedDiagram)
   {
      IDiagram cpDia = null;

      ETSmartWaitCursor wait = null;
      try
      {
         wait = new ETSmartWaitCursor();

         // The user is allowed to pass the diagram in through pCreatedDiagram
         if (pCreatedDiagram != null)
         {
            addElementsToDiagram(pCreatedDiagram, pElements, null, CRB_GET_ALL);
            cpDia = pCreatedDiagram;
         }
         else
         {
            cpDia = createDiagram(diagramKind, pNamespace, sDiagramName, pElements, null, null);
         }
      }
      finally
      {
         wait.stop();
      }
      
      return cpDia;
   }

   /**
    * Creates the specified diagram, and adds the input elements to the diagram when the diagram is next opened.
    */
   public IProxyDiagram createStubDiagramForElements(int diagramKind, INamespace pNamespace, String sDiagramName, ETList < IElement > pElements)
   {
      StubDiagramCreator stubCreator = new StubDiagramCreator();
      return stubCreator.createDiagram(diagramKind, pNamespace, sDiagramName, pElements);
   }

   /**
    * Creates the specified diagram,
    * and adds the input XMIIDs as elements to the diagram when the diagram is next opened.
    */
   public IProxyDiagram createStubDiagramForXMIIDs(String sDiagramKind, INamespace pNamespace, String sDiagramName, String sProjectXMIID, IStrings pXMIIDsToCDFS, IStrings pXMIIDsForNavigationOnly)
   {
      StubDiagramCreator stubCreator = new StubDiagramCreator();
      return stubCreator.createDiagram(sDiagramKind, pNamespace, sDiagramName, sProjectXMIID, pXMIIDsToCDFS, pXMIIDsForNavigationOnly);
   }

   /**
    * Adds the input elements to the input diagram.
    * The caller of this diagram has to determine the elements that need to be put on the diagram.
    * The only special case is if the caller knows that all of pParentElement's owned elements
    * are to be used to populate the diagram, then the caller can call this function with pElements=NULL.
    *
    * @param pDiagram [in] The existing diagram
    * @param pElements [in] The elements to add
    * @param pParentElement [in] If pElements is null then this parameter can be used
    * to get the children of pParentElement and add those to the diagram.
    */
   public long addElementsToDiagram(IDiagram pDiagram, ETList < IElement > pElements, IElement pParentElement, int nChildRetrievalBehavior)
   {
      if (pDiagram != null)
      {
         // The input must either have a list of elements, or one element
         if (pElements != null || pParentElement != null)
         {
            // Block all meta data events
            // Fix W7607:  This blocker was causing a problem with SCM, the update will fix this.
            // UPDATE (W7634):  When this fix is in place we should be able to block events again.
            // Fix J514:  The property editor seems to be updating during CDFS,
            //            so we are now using the blocker again.
            boolean origBlock = EventBlocker.startBlocking();

            IGUIBlocker blocker = null;
            ETSmartWaitCursor wait = new ETSmartWaitCursor();
            try
            {
               // Block user input, and containment calculations when dropping
               // the presentation elements on the diagram
               boolean bBlockRefreshDuringLayout = true;
               blocker = new GUIBlocker(GBK.DIAGRAM_KEYBOARD | GBK.DIAGRAM_MOVEMENT | GBK.DIAGRAM_RESIZE | GBK.DIAGRAM_DELETION | GBK.DIAGRAM_CONTAINMENT | GBK.DIAGRAM_TRACK_CARS);
					int diaKind = pDiagram.getDiagramKind();	// IDiagramKind

               // Create a diagram from an interaction.  This will create a sequence diagram
               if (createSequenceDiagram(pDiagram, pElements, pParentElement))
               {
                  wait.restore();
                  
                  bBlockRefreshDuringLayout = false;
               }
               else
               {
                  wait.restore();
                  
                  // Used the owned elements as the input elements
                  if (pElements == null)
                  {
                     pElements = getOwnedElements2(pParentElement);
                  }

                  if (pElements != null)
                  {
                     // See if we should create a component diagram
                     boolean handled = createComponentDiagram(pDiagram, pElements);
                     wait.restore();

                     // Finally handle the diagram creation in the default way
                     if (!handled)
                     {
                        defaultCreateDiagramMechanism(pDiagram, pElements);
                        wait.restore();

                        if (pParentElement instanceof IInteraction)
                        {
                           IInteraction cpInteraction = (IInteraction) pParentElement;
                           String name = cpInteraction.getName();
                           if (name == null || name.length() == 0)
                           {
                              cpInteraction.setName(pDiagram.getName());
                           }
                        }

                        // Don't block state diagrams since they reparent themselves.
                        if (diaKind == IDiagramKind.DK_STATE_DIAGRAM)
                        {
                           bBlockRefreshDuringLayout = false;
                        }
                     }
                  }
               }
                    
//                    printNodeLocations("Pre Layout", pDiagram);
               performLayout(pDiagram, true);
               wait.restore();
               pumpMessages(pDiagram);

               // I am keeping in the printNodeLocations in for debug reasons.
//                printNodeLocations("Post Layout", pDiagram);
                    
               // Now we need to tell all the containers to populate themselves
               populateAllContainers(pDiagram);
               wait.restore();
                    
//                printNodeLocations("Post Populate", pDiagram);
            }
            finally
            {
               wait.stop();
               if (blocker != null)
               {
                  blocker.clearBlockers();
               }
               EventBlocker.stopBlocking(origBlock);
            }
         }
      }
      return 0;
   }

//    protected void printNodeLocations(String title, IDiagram pDiagram)
//    {
//        System.out.println("************************************");
//        System.out.println("** " + title);
//        System.out.println("************************************");
//        
//        IDrawingAreaControl control = ((IUIDiagram) pDiagram).getDrawingArea();
//        if (control != null)
//        {
//            // Get all the container draw engines
//            ETList < IPresentationElement > pPEs = control.getAllItems();
//            if (pPEs != null)
//            {
//                int count = pPEs.size();
//                boolean needRelayout = false;
//                for (int i = 0; i < count; i++)
//                {   
//                    IPresentationElement pPE = pPEs.get(i);
//                    com.tomsawyer.util.TSObject tsObject = TypeConversions.getTSObject(pPE);
//                    if(tsObject instanceof com.tomsawyer.editor.TSENode)
//                    {
//                        System.out.println("====================================");                        
//                        
//                        IDrawEngine drawEngine = TypeConversions.getDrawEngine(pPE);
//                        if (drawEngine instanceof IADContainerDrawEngine)
//                        {                        
//                            System.out.println("== Container                       =");
//                        }
//
//                        com.tomsawyer.editor.TSENode node = (com.tomsawyer.editor.TSENode)tsObject;
//                        System.out.println("Left = " + node.getLeft());
//                        System.out.println("Top = " + node.getTop());
//                        System.out.println("Right = " + node.getRight());
//                        System.out.println("Bottom = " + node.getBottom());
//                        
//                        org.netbeans.modules.uml.core.support.umlsupport.IETRect rectCompartment = TypeConversions.getLogicalBoundingRect(drawEngine);
//
//                        System.out.println("------------------------------------");
//
//                        System.out.println("Left = " + rectCompartment.getLeft());
//                        System.out.println("Top = " + rectCompartment.getTop());
//                        System.out.println("Right = " + rectCompartment.getRight());
//                        System.out.println("Bottom = " + rectCompartment.getBottom());
//                        System.out.println("====================================");
//                    }
//                }
//            }
//        }
//    }
   
   /**
    * Called when the addin is initialized.
    */
   public long initialize(Object context)
   {
      // Get the current message service
      m_MessageService = ProductHelper.getMessageService();

      if (m_EventsSink == null)
      {
         m_EventsSink = new AddInEventSink();
         m_EventsSink.setParent(this);

         m_Helper.registerProjectTreeContextMenuEvents(m_EventsSink);
         m_Helper.registerDrawingAreaEvents(this);
      }
      return 0;
   }

   /**
    * Register for the accelerators that the edit control needs.
    */
   private boolean registerAccelerators()
   {
      boolean bRetVal = false;

      //if( !m_bAcceleratorsRegistered)
      {
         // Don't allow transformations if we're Describe Developer 'cause
         // Component diagrams are not allowed
         IAcceleratorManager cpManager = getAcceleratorManager();
         if (cpManager != null && m_hook != null)
         {
            cpManager.register(m_hook, this, "F6", false);

            bRetVal = true;
            m_bAcceleratorsRegistered = true;
         }
      }
      //else
      {
         m_bAcceleratorsRegistered = true;
      }

      return bRetVal;
   }

   /**
    * Called when the addin is deinitialized.
    */
   public long deInitialize(Object context)
   {
      // Release our message service
      m_MessageService = null;

      // Unregister from the dispatchers
      m_Helper.revokeProjectTreeContextMenuSink(m_EventsSink);

      revokeAccelerators();

      // Delete our sink
      if (m_EventsSink != null)
      {
         m_EventsSink = null;
      }

      return 0;
   }

   /**
    * 
    */
   private void revokeAccelerators()
   {
      // TODO Auto-generated method stub

   }

   /**
    * Called when the addin is unloaded.
    */
   public long unLoad(Object context)
   {
      return 0;
   }

   /**
    * The version of the addin.
    *
    * @param pVersion [out,retval] The version of this addin.
    */
   public String getVersion()
   {
      return m_Version;
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
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getID()
    */
   public String getID()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * Returns the progid of this addin.
    *
    * @param sProgID [out,retval] The progid of this adding (ie "DiagramCreatorAddIn.DiagCreatorAddIn");
    */
   public String getProgID()
   {
      return "org.netbeans.modules.uml.ui.addins.diagramcreator.DiagCreatorAddIn";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getLocation()
    */
   public String getLocation()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * Retrieves the selected elements fromt he project tree
    */
   private ETList < IElement > getSelectedElements(IProjectTreeControl ppProjectTree)
   {
      ETList < IElement > retObj = null;
      if (ppProjectTree == null)
      {
         ppProjectTree = ProductHelper.getProjectTree();
      }

      IProjectTreeItem[] cpItems = ppProjectTree.getSelected();
      if (cpItems != null)
      {
         int count = cpItems.length;
         if (count > 0)
         {
            retObj = new ETArrayList < IElement > ();

            // We go over the list and add to our list of IElements
            for (int i = 0; i < count; i++)
            {
               IProjectTreeItem item = cpItems[i];
               IElement modEle = item.getModelElement();
               if (modEle != null)
               {
                  retObj.add(modEle);
               }
            }
         }
      }
      return retObj;
   }

   /**
    * Retrieves the owned elements of the input element
    */
   private ETList < INamedElement > getOwnedElements(IElement pElement)
   {
      ETList < INamedElement > retObj = null;
      // If the owner is a namespace then get the owned elements.  If it's a state machine or
      // a composite state then the elements are actually owned by the first region
      if (pElement != null)
      {
         if (pElement instanceof IStateMachine)
         {
            IStateMachine cpStateMachine = (IStateMachine) pElement;
            IRegion cpFirstRegion = cpStateMachine.getFirstRegion();
            if (cpFirstRegion != null)
            {
               retObj = cpFirstRegion.getOwnedElements();
            }
         }
         else if (pElement instanceof IState)
         {
            IState cpState = (IState) pElement;
            ETList < IRegion > cpRegions = cpState.getContents();
            if (cpRegions != null)
            {
               int count = cpRegions.size();
               for (int i = 0; i < count; i++)
               {
                  IRegion cpRegion = cpRegions.get(i);
                  ETList < INamedElement > elems = cpRegion.getOwnedElements();
                  if (elems != null)
                  {
                     if (retObj != null)
                     {
                        retObj.addAll(elems);
                     }
                     else
                     {
                        retObj = elems;
                     }
                  }
               }
            }
         }
         else if (pElement instanceof INamespace)
         {
            retObj = ((INamespace) pElement).getOwnedElements();
         }
      }
      return retObj;
   }

   /**
    * Retrieves the owned elements of the input element
    */
   private ETList < IElement > getOwnedElements2(IElement pElement)
   {
      ETList < IElement > retObj = null;
      ETList < INamedElement > cpOwnedElements = getOwnedElements(pElement);
      if (cpOwnedElements != null)
      {
         retObj = new ETArrayList < IElement > ();
         int count = cpOwnedElements.size();
         for (int i = 0; i < count; i++)
         {
             retObj.add(cpOwnedElements.get(i));
         }
      }
      return retObj;
   }

   /**
    * Retrieves the first element in the list of elements
    */
   private IElement getFirstElement(ETList < IElement > pElements)
   {      
      return pElements != null && pElements.size() > 0 ? pElements.get(0) : null;
   }

   /**
    * Ask the user to create a diagram based on the elements
    */
   private void guiCreateDiagram(DiagramHandler pHandler)
   {
       ETSmartWaitCursor wait = new ETSmartWaitCursor();
       try
       {
           IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
           if (diaMgr != null)
           {
               IElement modEle = pHandler.getElement();
               if (modEle != null)
               {
                   INamespace cpNamespace = null;
                   int diaKind = IDiagramKind.DK_CLASS_DIAGRAM;
                   int availableKinds = IDiagramKind.DK_ALL;
                   
                   // TODO:  Update this code to use the ProjectTreeEngine.etc file
                   // dragAndDropDiagrams attribute to determine the available diagram kinds
                   
                   // Determine the questions to ask the user in the new diagram dialog
                   // based on the type of element
                   if (modEle instanceof IInteraction)
                   {
                       cpNamespace = (IInteraction) modEle;
                       diaKind = IDiagramKind.DK_SEQUENCE_DIAGRAM;
                       availableKinds = IDiagramKind.DK_SEQUENCE_DIAGRAM | IDiagramKind.DK_COLLABORATION_DIAGRAM;
                   }
                   else if (modEle instanceof IOperation)
                   {
                       cpNamespace = (IOperation) modEle;
                       diaKind = IDiagramKind.DK_SEQUENCE_DIAGRAM;
                       availableKinds = IDiagramKind.DK_SEQUENCE_DIAGRAM | IDiagramKind.DK_COLLABORATION_DIAGRAM;
                       
                       // Make sure the operation is RE'd by the handler
                       pHandler.setOperationToRE((IOperation) modEle);
                   }
                   else if (modEle instanceof IActivity)
                   {
                       cpNamespace = (IActivity) modEle;
                       diaKind = IDiagramKind.DK_ACTIVITY_DIAGRAM;
                       availableKinds = IDiagramKind.DK_ACTIVITY_DIAGRAM;
                   }
                   else if (modEle instanceof IStateMachine)
                   {
                       cpNamespace = (IStateMachine) modEle;
                       diaKind = IDiagramKind.DK_STATE_DIAGRAM;
                       availableKinds = IDiagramKind.DK_STATE_DIAGRAM;
                   }
                   else if (modEle instanceof ICollaboration)
                   {
                       cpNamespace = (ICollaboration) modEle;
                       diaKind = IDiagramKind.DK_CLASS_DIAGRAM;
                       availableKinds = IDiagramKind.DK_CLASS_DIAGRAM | IDiagramKind.DK_COMPONENT_DIAGRAM;
                   }
                   else
                   {
                       // Make the default namespace for the diagram the same as the input element's
                       // If the owning namespace is a region or activity partition then go one more up
                       if (modEle instanceof INamedElement)
                       {
                           INamedElement cpNamedEle = (INamedElement) modEle;
                           cpNamespace = cpNamedEle.getNamespace();
                           if (cpNamespace != null)
                           {
                               if (cpNamespace instanceof IRegion)
                               {
                                   IRegion pRegion = (IRegion) cpNamespace;
                                   cpNamespace = null;
                                   cpNamespace = pRegion.getNamespace();
                               }
                               else if (cpNamespace instanceof IActivityPartition)
                               {
                                   IActivityPartition pPartition = (IActivityPartition) cpNamespace;
                                   cpNamespace = null;
                                   cpNamespace = pPartition.getNamespace();
                               }
                           }
                       }
                   }
                   
                   // Ask the user if they want to create a diagram
                   IDiagram newDiagram = diaMgr.newDiagramDialog(
                           cpNamespace, diaKind, availableKinds, pHandler);
                   
                   // Fixed issue 95782. When a diagram is 1st created, its dirty state is false.
                   // Set the dirty state to true to have the diagram autosaved.
                   if (newDiagram != null )
                   {
                       newDiagram.setIsDirty(true);
                       newDiagram.save();
                   }
               }
           }
       }
       finally
       {
           wait.stop();
       }
   }

   /**
    * Create the specified diagram, using the call back interface
    */
   private IDiagram createDiagram(int diagramKind, INamespace pNamespace, String sDiagramName, ETList < IElement > pElements, IElement pParentElement, IProjectTreeControl pProjectTree)
   {
      IDiagram retObj = null;
      // Create the diagram, if necessary
      IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
      if (diaMgr != null)
      {
         DiagramHandler handler = createDiagramCallback(pElements, pParentElement, pProjectTree, false);
         retObj = diaMgr.createDiagram(diagramKind, pNamespace, sDiagramName, handler);
      }
      // Fixed issue 96119. 
      // Set the diagram dirty to force autosave.
      retObj.setIsDirty(true);
      retObj.save();
      return retObj;
   }

   /**
    * Add the elements owned by the owner element to the list of elements
    * If the pOwnerElement is an activity, or state, it is removed and all its owned elements are added
    * with out asking the user.
    *
    * @param pDiagram [in] The diagram that got created.
    * @param pOwnerElement [in] The owner of the diagram
    * @param pElements [in,out] The list of elements that'll get created.  More may be added if the user
    * wants to add owned elements.
    */
   public ETList < IElement > guiAddOwnedElements(IDiagram pDiagram, IElement pOwnerElement, ETList < IElement > pElements)
   {
      if (pElements == null)
      {
         pElements = new ETArrayList < IElement > ();
      }
      
      ETList < INamedElement > ownedElements = getOwnedElements(pOwnerElement);
      if (ownedElements != null)
      {
         ETList < IElement > validElems = validateElementsForDiagram(pDiagram, ownedElements);
         if (validElems != null)
         {
            int count = validElems.size();
            if (count > 0)
            {
               if (pOwnerElement instanceof IInteraction || pOwnerElement instanceof IActivity || pOwnerElement instanceof IStateMachine)
               {
                  pElements.remove(pOwnerElement);
               }
               else
               {
                  //ask the user
                  IQuestionDialog dialog = new SwingQuestionDialogImpl();
                  String title = loadString("IDS_DEPTHDIALOGTITLE");
                  String message = loadString("IDS_DEPTHDIALOGMSG");
                  dialog.setDefaultButton(IQuestionDialog.IDOK);
                  QuestionResponse result = dialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONWARNING, message, "", title, MessageResultKindEnum.SQDRK_RESULT_YES, false);
                  
                  if (result.getResult() == MessageResultKindEnum.SQDRK_RESULT_NO)
                  {
                     // Don't include the child elements
                     count = 0;
                  }
                  else
                  {
                     // Remove the owner so we don't get so many nested links, if that owner
                     // is an IPackage
                     if (pOwnerElement instanceof IPackage)
                     {
                        pElements.remove(pOwnerElement);
                     }
                  }
               }

               for (int i = 0; i < count; i++)
               {
                  pElements.add(validElems.get(i));
               }
            }
         }
      }
      return pElements;
   }

   /**
    * Ask the user if the diagram of the specified kind under the input element should be deleted
    */
   private boolean guiRemoveDiagramByKind(INamespace pNamespace, int diaKind)
   {
      boolean proceed = true;
      IProxyDiagramManager proxyDiaMgr = ProxyDiagramManager.instance();
      ETList < IProxyDiagram > proxyDias = proxyDiaMgr.getDiagramsInNamespace(pNamespace);
      if (proxyDias != null)
      {
         int count = proxyDias.size();
         for (int i = 0; i < count; i++)
         {
            IProxyDiagram dia = proxyDias.get(i);
				int otherKind = dia.getDiagramKind();
				
            if (otherKind == diaKind)
            {
               //ask the user
               IQuestionDialog dialog = new SwingQuestionDialogImpl();
               String title = loadString("IDS_REMOVE_DIAGRAM_TITLE");
               String name = dia.getQualifiedName();
               String id = "";
               
               if (IDiagramKind.DK_SEQUENCE_DIAGRAM == diaKind)
               {
                  id = loadString("IDS_REMOVE_SQD");
               }
               else
               {
                  id = loadString("IDS_REMOVE_CoD");
               }
               String message = formatMessage(id, name);

               //QuestionResponse result = MessageResultKindEnum.SQDRK_RESULT_YES;
               dialog.setDefaultButton(IQuestionDialog.IDOK);
               QuestionResponse result = dialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONWARNING, message, "", title, MessageResultKindEnum.SQDRK_RESULT_YES, false);
               if (result.getResult() == MessageResultKindEnum.SQDRK_RESULT_YES)
               {
                  proxyDiaMgr.removeDiagram(dia.getFilename());
                  proceed = true;
               }
            }
         }
      }
      return proceed;
   }

   private String formatMessage(String id, String name)
   {
      //return id + name;
      return StringUtilities.replaceAllSubstrings(id, "\"%1\"", name);
   }

   /**
    * Ask the user how to continue based on the input operation
    */
   public IInteraction continueREOperation(IOperation pOperation, IDiagram pDiagram)
   {
      IInteraction pInteraction = reverseEngineerOperation(pOperation);

      // Fix W6804:  Move the diagram under the REed interaction, and
      //             delete the diagram's interaction
      if (pInteraction instanceof INamespace)
      {
         INamespace cpSpace = (INamespace) pInteraction;
         INamespace diaSpace = pDiagram.getNamespace();
 			boolean isSame = diaSpace.isSame(cpSpace);
         if (!isSame)
         {
            pDiagram.setNamespace(cpSpace);

            if (diaSpace != null)
            {
               diaSpace.delete();
            }
         }
      }
      return pInteraction;
   }

   /**
    * Make sure the diagram can create these elements
    */
   private ETList < IElement > validateElementsForDiagram(IDiagram pDiagram, ETList < INamedElement > pNamedElements)
   {
      ETList < IElement > retObj = null;
      if (pDiagram != null && pNamedElements != null)
      {
         if (pDiagram instanceof IUIDiagram)
         {
            IUIDiagram dia = (IUIDiagram) pDiagram;
            IDrawingAreaControl cpControl = dia.getDrawingArea();
            if (cpControl != null)
            {
               IPresentationTypesMgr presTypesMgr = cpControl.getPresentationTypesMgr();
               if (presTypesMgr != null)
               {
                  // Get the diagram kind
                  int diaKind = pDiagram.getDiagramKind();

                  // Only keep valid elements for this diagram
                  int count = pNamedElements.size();
                  for (int i = 0; i < count; i++)
                  {
                     INamedElement namedEle = pNamedElements.get(i);
                     String initStr = presTypesMgr.getMetaTypeInitString(namedEle, diaKind);
                     if (initStr != null && initStr.length() > 0)
                     {
                        // Don't add links, because
                        // they will get discovered during relationship discovery.
                        PresentationTypeDetails details = presTypesMgr.getInitStringDetails(initStr, diaKind);
								int nKind = details.getObjectKind();

                        if ((TSGraphObjectKind.TSGOK_NODE == nKind) || (TSGraphObjectKind.TSGOK_NODE_DECORATOR == nKind) || (TSGraphObjectKind.TSGOK_NODE_RESIZE == nKind))
                        {
                           if (retObj == null)
                           {
                              retObj = new ETArrayList < IElement > ();
                           }
                           
                           if (retObj != null)
                           {
                              retObj.add(namedEle);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      return retObj;
   }

	protected void postProcessCreatedPresentationElements(IDiagram pDiagram, ETList < IElement > pElements, ETList<IPresentationElement> newPES)
	{
		// We need to stagger the nodes so nested links work, 
		// if we don't layout here the nodes are on top of each other and the
		// Relationship discovery thinks the packages are contained. Bug DT 2533 reported by Sun
					
		performLayout(pDiagram, false);
		
		// process post drop handling
		IDiagramEngine diaEngine = TypeConversions.getDiagramEngine(pDiagram);
		if (diaEngine != null)
		{
			// This discovers relationships.
			diaEngine.postOnDrop(pElements, false);
		}
		
		IPresentationElement lastPresEle = newPES != null && newPES.size() > 0 ? newPES.get(newPES.size() -1) : null;
		if (lastPresEle != null)
		{
			// We are calling fitInWindow so this makes very little sence? (Kevin)
			// bug where presentation elements not centered
			pDiagram.centerPresentationElement(lastPresEle, false, true);	
		}
	}
	
   /**
    * Create the presentation element, and discover the associated presentation relationships
    *
    * @param pDiagram [in] The diagram we're creating
    * @param pElements [in] The elements to place on the diagram
    */
   private void createPresentationElements(IDiagram pDiagram, ETList < IElement > pElements)
   {
      if (pDiagram != null && pElements != null)
      {
         ICoreRelationshipDiscovery relDiscovery = TypeConversions.getRelationshipDiscovery(pDiagram);
         if (relDiscovery != null && relDiscovery instanceof IADRelationshipDiscovery)
         {
            IADRelationshipDiscovery adRelDiscovery = (IADRelationshipDiscovery) relDiscovery;
            int count = pElements.size();
            if (count > 0)
            {
               int diaKind = pDiagram.getDiagramKind();
                // Create this object on the stack to verify that we'll end progress even
               // if we get an exception
               ThermProgress ensureEnd = null;

               try
               {
                  ensureEnd = new ThermProgress();

                  // If we have a message string then begin the progress
                  String message = loadString("IDS_CREATING_PES");
                  ensureEnd.beginProgress(message, 0, count, 0);
                  int i = 0;
                  ETList < IElement > pOrginalElements = new ETArrayList < IElement > ();
                  pOrginalElements.addAll(pElements);
						ETList<IPresentationElement> newPES = new ETArrayList<IPresentationElement>();
						
	                for (Iterator < IElement > iter = pOrginalElements.iterator(); iter.hasNext(); i++)
                  {
                     IElement cpElem = iter.next();

                     IPresentationElement pEle = null;

                     // Fix W776:  In order for the sequence diagram to create presentation elements,
                     //            only the Interface on a non SQD diagram needs special handling.
                     IInterface cpInterface = cpElem instanceof IInterface ? (IInterface) cpElem : null;
                     IPartFacade cpFacade = cpElem instanceof IPartFacade ? (IPartFacade) cpElem : null;

                     if (cpInterface != null && cpFacade == null)
                     {
                        // We have an interface that isn't a partfacade
                        if (diaKind != IDiagramKind.DK_SEQUENCE_DIAGRAM)
                        {
                           pEle = adRelDiscovery.createInterfaceAsClassPresentationElement(cpInterface);
                        }
                        else
                        	pEle = null;
                     }
                     else
                     {
                        pEle = adRelDiscovery.createPresentationElement(cpElem);
                     }
                     
                     if (pEle != null)
                     {
								newPES.add(pEle);
                     }
                      // Fix J543:  Attempting to free more memory by removing elements from the list
                     iter.remove();

                     ensureEnd.setPos(i);
                  }

						postProcessCreatedPresentationElements(pDiagram, pElements, newPES);
               }
               finally
               {
                  // End the progress
                  ensureEnd.endProgress();
               }
            }
         }
      }
   }


   /**
    * Determines if CDFS is able to create a diagram from the element
    */
   private boolean canDiagramBeCreatedFromElement(IElement pElement)
   {
      boolean canCreate = false;
      if (pElement != null)
      {
         // We shouldn't be able to select elements under an interaction and CDFS.  
         // Therefore, we should disable the CDFS menu if you select any children 
         // of the interaction.
         IElement owner = pElement.getOwner();
         if (owner instanceof IInteraction || pElement instanceof IMessage || pElement instanceof ICombinedFragment || pElement instanceof IOperation || pElement instanceof IAttribute)
         {
            // For operations, we have to make sure the operation can be REed
            // in order to support CDFS
            if (pElement instanceof IOperation)
            {
               IUMLParsingIntegrator integrator = getUMLParsingIntegrator();
               boolean canRE = integrator.canOperationBeREed((IOperation) pElement);
               if (canRE)
               {
                  canCreate = true;
               }
            }
         }
         else
         {
            canCreate = true;
         }
      }
      return canCreate;
   }

   /**
    * Access to the UML parsing integrator member variable
    */
   private IUMLParsingIntegrator getUMLParsingIntegrator()
   {
      if (m_UMLParsingIntegrator == null)
      {
         m_UMLParsingIntegrator = new UMLParsingIntegrator();
      }
      return m_UMLParsingIntegrator;
   }

   /**
    * Prepare the diagram for layout, and layout the presentation elements on the diagram
    *
    * @param pDiagram [in] The diagram to perform layout on.
    * @param bIgnoreContainment [in] Should we ignore containment?
    */
   private void performLayout(IDiagram pDiagram, boolean ignoreContainment)
   {
      if (pDiagram != null)
      {
         // Deselect everything
         pDiagram.selectAll(false);

         // Set the default mode to be selection
         pDiagram.enterMode(IDrawingToolKind.DTK_SELECTION);

         // Layout the diagram
         int diaKind = pDiagram.getDiagramKind();
         
         // Let the diagram m decide its layout kind.
         //int layoutKind = getLayoutKind(diaKind);

         // Do a layout using our action which will automatically create
         // a blocker for containment if necessary
         if (pDiagram instanceof IUIDiagram)
         {
            IUIDiagram dia = (IUIDiagram) pDiagram;
            IDrawingAreaControl cpControl = dia.getDrawingArea();
            if (cpControl != null)
            {
               if (diaKind == IDiagramKind.DK_COMPONENT_DIAGRAM)
               {
                  // Component diagram is unique 'cause it does some containment
                  IComponentDiagramCreator diaCreator = new ComponentDiagramCreator();
                  diaCreator.performLayout(pDiagram);
               }
               else
               {
                  // Perform normal layout routines
                  ITopographyChangeAction changeAction = new TopographyChangeAction();
                  if (ignoreContainment)
                  {
                     changeAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE_IGNORECONTAINMENT_SILENT);
                  }
                  else
                  {
                     changeAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE_SILENT);
                  }

                  changeAction.setLayoutStyle(true, true, pDiagram.getLayoutStyle());
						changeAction.execute(cpControl);
                  //dia.postDelayedAction(changeAction);
               }
               cpControl.refresh(false);
            }
         }
      }
   }

   /**
    * Expands the diagram namespace's node in the project tree
    */
   public void expandProjectTree(IDiagram pDiagram, IProjectTreeControl pProjectTree)
   {
      // Fixes 3000 Expand project tree branch when creating diagram from selected
      if (pDiagram != null)
      {
         INamespace space = pDiagram.getNamespace();
         if (space != null)
         {
            expandProjectTree(space, pProjectTree);
         }
      }
   }

   /**
    * Expands the element's namespace's node in the project tree
    */
   public void expandProjectTree(IElement pElement, IProjectTreeControl pProjectTree)
   {
      if (pElement != null && pElement instanceof INamedElement)
      {
         INamespace space = ((INamedElement) pElement).getNamespace();
         if (space != null)
         {
            expandProjectTree(space, pProjectTree);
         }
      }
   }

   /**
    * Expands the namespace's node in the project tree
    */
   private void expandProjectTree(INamespace pNamespace, IProjectTreeControl pProjectTree)
   {
      ETList < IProjectTreeItem > items = pProjectTree.findNode2(pNamespace);
      if (items != null)
      {
         int count = items.size();
         if (count > 0)
         {
            IProjectTreeItem item = items.get(0);
            pProjectTree.setIsExpanded(item, true);
         }
      }
   }
   
    /**
    * expands Element nodes
    */
   public void expandProjectTree(ETList<IElement> pElements, IProjectTreeModel pProjectTreeModel)
   {
      if (pElements != null && pElements.size() > 0)
      {
         ETList <ITreeItem> items = null;
         Iterator<IElement> iter = pElements.iterator();
          while (iter.hasNext()) 
          {
              items = pProjectTreeModel.findNodes(iter.next());
              if (items != null) 
              {
                  int count = items.size();
                  for (int i = 0; i < count; i++) 
                  {
                      ITreeItem item = items.get(i);  
                      pProjectTreeModel.fireItemExpanding(item);
                  }
              }
          }
      }
   }

   /**
    * If the selected item is an interaction, a sequence diagram is generated.
    *
    * @return True if the sequence diagram is created.
    */
   private boolean createSequenceDiagram(IDiagram pExistingDiagram, ETList < IElement > pElements, IElement pElement)
   {
      boolean handled = false;
      int diaKind = pExistingDiagram != null ? pExistingDiagram.getDiagramKind() : IDiagramKind.DK_UNKNOWN;
      
      if (diaKind == IDiagramKind.DK_SEQUENCE_DIAGRAM)
      {
         if (pElement == null)
         {
            pElement = getFirstElement(pElements);
         }

         // If the input element is an interaction then use the sequence diagram generator.
         if (pElement instanceof IInteraction)
         {
            IInteraction cpInteraction = (IInteraction) pElement;

            // Note that we verify that the user used that namespace when creating
            // the diagram 'cause the new dialog allows folks to change the namespace

            // If we have an exising diagram make sure this element and
            // the namespace of the diagram are the same.
            if (pExistingDiagram != null)
            {
               cpInteraction = null;
               INamespace diaSpace = pExistingDiagram.getNamespace();
               if (diaSpace != null)
               {
                  if (pElement.isSame(diaSpace))
                  {
                     cpInteraction = (IInteraction) pElement;
                  }
               }
            }

            // At this point if we have an interaction we've verified that we should
            // use the sequence diagram generator.
            if (cpInteraction != null)
            {
               ISequenceDiagramGenerator diaGenerator = new SequenceDiagramGenerator();
               handled = diaGenerator.generate(cpInteraction, pExistingDiagram);
            }
         }
      }
      return handled;
   }

   /**
    * Reverse Engineer the input operation, and return the interaction created.
    */
   private IInteraction reverseEngineerOperation(IOperation pOperation)
   {
      IInteraction retObj = null;
      // At this point if we have an operation we've verified that we should
      // user RE to create the interaction.
      if (pOperation != null)
      {
         //IProject proj = getCurrentProject();
         IProject proj = pOperation.getProject();
         if (proj != null)
         {
            IUMLParsingIntegrator integrator = getUMLParsingIntegrator();
            integrator.reverseEngineerOperation(proj, pOperation);
            retObj = getOperationsInteraction(pOperation);
         }
      }
      return retObj;
   }

   /**
    * Retrieve's the operation's interaction
    */
   private IInteraction getOperationsInteraction(IOperation pOperation)
   {
      IInteraction retObj = null;
      if (pOperation != null)
      {
         ETList < IElement > elems = pOperation.getElements();
         if (elems != null)
         {
            int count = elems.size();

            // Get the last (bottom) interaction from the operetion's owned elements
            for (int i = count - 1; i >= 0; i--)
            {
               IElement elem = elems.get(i);
               if (elem instanceof IInteraction)
               {
                  retObj = (IInteraction) elem;
                  break;
               }
            }
            if (retObj != null)
            {
               retObj.setOwner(pOperation);
            }
         }
      }
      return retObj;
   }

   /**
    * Creates a new diagram (using CDFS) using the element that is the parent of the input diagram
    */
   private void transformDiagramTo(IDiagram pDiagram, int eKind)
   {
      if (pDiagram != null)
      {
         IPromptDialog dialog = new SwingPromptDialog();
         String prompt = loadString("IDS_Q_DIAGRAM_NAME");
         String title = loadString("IDS_Q_DIAGRAM_NAME_TITLE");
         if (IDiagramKind.DK_COLLABORATION_DIAGRAM == eKind)
         {
            title = loadString("IDS_Q_DIAGRAM_COD_TITLE");
         }
         else
         {
            title = loadString("IDS_Q_DIAGRAM_SQD_TITLE");
         }
         
         String oldDiaName = pDiagram.getName();

         ETPairT < Boolean, String > result = dialog.displayEdit(prompt, oldDiaName, title);
         boolean userHitOK = ((Boolean) result.getParamOne()).booleanValue();
         String diaName = result.getParamTwo();
         if (userHitOK)
         {
            INamespace space = pDiagram.getNamespace();
            if (space != null && guiRemoveDiagramByKind(space, eKind))
            {
               IProjectTreeControl projTree = ProductHelper.getProjectTree();
               createDiagram(eKind, space, diaName, null, space, projTree);
            }
         }
      }
   }

   /**
    * Retrieve the current project
    */
   private IProject getCurrentProject()
   {
      IProductProjectManager projMgr = ProductHelper.getProductProjectManager();
      return projMgr != null ? projMgr.getCurrentProject() : null;
   }

   /**
    * Retrieve the current diagram
    */
   private IDiagram getCurrentDiagram()
   {
      IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
      return diaMgr != null ? diaMgr.getCurrentDiagram() : null;
   }

   /**
    * Get the drawing area control from the diagram, throw if not able
    */
   private void pumpMessages(IDiagram pDiagram)
   {
      if (pDiagram instanceof IUIDiagram)
      {
         IDrawingAreaControl control = ((IUIDiagram) pDiagram).getDrawingArea();
         if (control != null)
         {
            control.pumpMessages(false);
         }
      }
   }

   /**
    * Retrieves the current product's accelerator manager
    */
   private IAcceleratorManager getAcceleratorManager()
   {
      IProduct prod = ProductHelper.getProduct();
      return prod != null ? prod.getAcceleratorManager() : null;
   }

   /**
    * Create the class presentation elements
    */
   private void createClassPresentationElements(IDiagram pDiagram)
   {
      if (pDiagram != null)
      {
         ICoreRelationshipDiscovery relDiscovery = TypeConversions.getRelationshipDiscovery(pDiagram);
         IProject proj = pDiagram.getProject();
         if (proj != null)
         {
            // Gather up all the classes and create presentation elements
            IElementLocator locator = new ElementLocator();

            // '//UML:Class' will find all classes in the project. To do it for just a particular namespace, pass 
            // the namespace element where ver is, and use this as as the query ".//UML:Class". 
            // ( Notice the '.' before the double slash ).
            ETList < IElement > elems = locator.findElementsByQuery(proj, "//UML:Class");
            if (elems != null)
            {
               int count = elems.size();
               for (int i = 0; i < count; i++)
               {
                  IElement elem = elems.get(i);
                  if (elem instanceof IClass)
                  {
                     IPresentationElement pEle = relDiscovery.createPresentationElement(elem);
                  }
               }
            }
         }
      }
   }

   /**
    * Create the package presentation elements
    */
   private void createPackagePresentationElements(IDiagram pDiagram)
   {
      if (pDiagram instanceof IStructuralDiagram)
      {
         ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(pDiagram);
         IProject proj = pDiagram.getProject();
         if (proj != null)
         {
            // Gather up all the classes and create presentation elements
            IElementLocator locator = new ElementLocator();
            ETList < IElement > elems = locator.findElementsByQuery(proj, ".//UML:Element.ownedElement/UML:Package");
            if (elems != null)
            {
               int count = elems.size();
               for (int i = 0; i < count; i++)
               {
                  IElement elem = elems.get(i);
                  if (elem instanceof IPackage)
                  {
                     IPresentationElement pEle = relDisco.createPresentationElement(elem);
                  }
               }
            }
         }
      }
   }

   /**
    * Message from the project tree that a context menu is about to be displayed
    */
   public void onProjectTreeContextMenuPrepare(IProjectTreeControl pControl, IProductContextMenu contextMenu)
   {
      if (pControl != null && m_EventsSink != null)
      {
         // See if we have a model element selected
         IElement elem = pControl.getFirstSelectedModelElement();
         if (canDiagramBeCreatedFromElement(elem))
         {
            ETList < IProductContextMenuItem > menuItems = contextMenu.getSubMenus();
            if (menuItems != null)
            {
               IProductContextMenuItem selMenuItem = new ProductContextMenuItem();

               // Set the sensitivity on the button to false, then back to true if we meet the
               // necessary conditions.
               selMenuItem.setSensitive(true);
               String temp = loadString("IDS_CREATEDIAGRAMFROMSELECTED");
               selMenuItem.setMenuString(temp);

               temp = loadString("IDS_CREATEDIAGRAM_DESC");
               selMenuItem.setDescription(temp);

               selMenuItem.setButtonSource("MBK_CREATEDIAGRAMFROMSELECTED");
               selMenuItem.setSelectionHandler(m_EventsSink);
               menuItems.add(selMenuItem);
            }
         }
      }
   }

   /**
    * Message from the sink that something has been selected
    */
   public void handleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem)
   {
      String temp = loadString("IDS_CREATEDIAGRAM_DESC");
      String desc = pSelectedItem.getDescription();
      if (temp.equals(desc))
      {
         Object pDisp = pContextMenu.getParentControl();
         if (pDisp instanceof IProjectTreeControl)
         {
            guiCreateDiagramFromProjectTreeElements((IProjectTreeControl) pDisp);
         }
      }
   }

   /**
    * Manages the diagram callback objects
    */
   private DiagramHandler createDiagramCallback(ETList < IElement > pElements, IElement pParentElement, IProjectTreeControl pProjectTree, boolean bUsingGUI)
   {
      DiagramHandler handler = new DiagramHandler();
      handler.setDiagCreatorAddIn(this);
      handler.setElements(pElements);
      handler.setElement(pParentElement);
      handler.setProjectTree(pProjectTree);
      handler.setUsingGUI(bUsingGUI);

      // Add this to our list of managed objects
      m_Callbacks.add(handler);
      return handler;
   }
   
   private DiagramHandler createDiagramCallback(ETList <IElement> pElements, IElement pParentElement, IProjectTreeModel projectTreeModel, boolean bUsingGUI)
   {
      DiagramHandler handler = new DiagramHandler();
      handler.setDiagCreatorAddIn(this);
      handler.setElements(pElements);
      handler.setElement(pParentElement);
      handler.setProjectTreeModel(projectTreeModel);
      handler.setUsingGUI(bUsingGUI);

      // Add this to our list of managed objects
      m_Callbacks.add(handler);
      return handler;
   }

   /**
    * Removes the diagram callback from the list of our managed objects
    */
   public void removeDiagramCallback(DiagramHandler pCallback)
   {
      if (pCallback != null)
      {
         m_Callbacks.removeElement(pCallback);
      }
   }

   /**
    * The new dialog uses a context to keep some defaults for building the information
    * presented in the dialog.  If the tree that we are coming from is the design center
    * we need to set up the data a little differently.
    * 
    *
    * @param pProjectTree[in]		The project tree
    *
    * @return HRESULT
    *
    */
   //This has to be uncommented after resolving cyclic dependency issues
//   private void setUpNewDialogContext(IProjectTreeControl pProjectTree)
//   {
//      // Create our context in case the new dialog is brought up and we need to get our
//      // workspace and project
//      INewDialogContext pContext = new NewDialogContext();
//
//      // all of this work is to get the workspace from the design pattern catalog
//      // so that we will have the right projects in our project (namespace) lists
//      ICoreProduct prod = ProductHelper.getCoreProduct();
//      if (prod != null)
//      {
//         IDesignCenterManager pManager = prod.getDesignCenterManager();
//         if (pManager instanceof IADDesignCenterManager)
//         {
//            IADDesignCenterManager adMgr = (IADDesignCenterManager) pManager;
//            IDesignPatternCatalog pCatalog = adMgr.getDesignPatternCatalog();
//            if (pCatalog != null)
//            {
//               IWorkspace pWorkspace = pCatalog.getWorkspace();
//               pContext.setWorkspace(pWorkspace);
//               pContext.setProject(null);
//               pContext.setUseAllProjectExtensions(false);
//               pContext.setProjectTree(pProjectTree);
//            }
//         }
//      }
//   }

   /**
    * Populates all the containers on the diagram
    */
   private void populateAllContainers(IDiagram pDiagram)
   {
      pumpMessages(pDiagram);
      if (pDiagram instanceof IUIDiagram)
      {
         IDrawingAreaControl control = ((IUIDiagram) pDiagram).getDrawingArea();
         if (control != null)
         {
            // Get all the container draw engines
            ETList < IPresentationElement > pPEs = control.getAllItems();
            if (pPEs != null)
            {
               int count = pPEs.size();
               boolean needRelayout = false;
               for (int i = 0; i < count; i++)
               {
                  IPresentationElement pPE = pPEs.get(i);
                  IDrawEngine drawEngine = TypeConversions.getDrawEngine(pPE);
                  if (drawEngine instanceof IADContainerDrawEngine)
                  {
                     // Call populate on them so they create populate their contents
                     boolean didPopulate = ((IADContainerDrawEngine) drawEngine).populate();
                     if (didPopulate)
                     {
                        needRelayout = true;
                     }
                  }
               }
               
               // BUG : If the container grows it may end up containing
               // stuff that shouldn't be contained.  For SP1 we can't fix right now.

               // If the populate actually added elements then we need to relayout the diagram 'cause
               // the containers may have resized, also we'll need to do a relationship discovery.
               if (needRelayout)
               {
                  final ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(pDiagram);
                  final IDiagram diagram = pDiagram;
                  if (relDisco != null)
                  {
                     // Fix J2510:  We need to delay this so that the proper elements exist on the diagram,
                     //             and so that layout will work properly.
                     SwingUtilities.invokeLater( new Runnable()
                     {
                        public void run()
                        {
                           relDisco.discoverCommonRelations(true);
                           performLayout( diagram, false );
                        }
                     } );
                  }
               }
            }
         }
      }
   }

   /**
    * Retrieves a resource string.
    */
   public static String loadString(String key)
   {
      try
      {
         return RESOURCE_BUNDLE.getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }

   /* Initializes the addin.
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IViewActionDelegate#init(org.netbeans.modules.uml.core.addinframework.ui.application.ApplicationView)
    */
   public void init(ApplicationView view)
   {
      m_View = view;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IPlugginAction#run()
    */
   public void run(ActionEvent e)
   {
      if (m_View != null)
      {
         String id = m_View.getId();
         if (id.equals("org.netbeans.modules.uml.view.projecttree"))
         {
            JProjectTree projTree = (JProjectTree) m_View;
            handleProjectTreeItemSelected(projTree);
         }
      }
   }

   private void handleProjectTreeItemSelected(JProjectTree projTree)
   {
      // See if we have a model element selected
      IElement elem = projTree.getFirstSelectedModelElement();
      if (canDiagramBeCreatedFromElement(elem))
      {
         guiCreateDiagramFromProjectTreeElements(projTree);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IPlugginAction#selectionChanged(org.netbeans.modules.uml.core.addinframework.ui.action.PluginAction, org.netbeans.modules.uml.core.addinframework.ui.action.selection.ISelection)
    */
//   public void selectionChanged(PluginAction action, ISelection selection)
//   {
//
//   }

   /**
    * Determines if the "Create Diagram From Selected" menu item should be 
    * enabled.
    */
//   public boolean validate(ApplicationView view, IContributionItem item, IMenuManager pContextMenu)
//   {
//      boolean valid = false;
//      if (view instanceof IProjectTreeControl)
//      {
//         IProjectTreeControl control = (IProjectTreeControl) view;
//
//         boolean isFolder = isFolderSelected(control);
//         if (!isFolder)
//         {
//            // See if we have a model element selected
//            IElement firstSelModEle = control.getFirstSelectedModelElement();
//
//            if (canDiagramBeCreatedFromElement(firstSelModEle))
//            {
//               valid = true;
//            }
//         }
//      }
//      else if (view instanceof IDrawingAreaControl)
//      {
//         // not valid for drawing area.
//      }
//      return valid;
//   }

   /**
    * Determines whether or not a folder node is selected in the tree
    *
    * @param pControl[in]			The tree control
    * @param bSel[out]				Whether or not a folder is selected in the tree
    *
    * return HRESULT
    */
   protected boolean isFolderSelected(IProjectTreeControl pControl)
   {
      boolean bSel = false;

      // get what is selected in the tree
      IProjectTreeItem[] pTreeItems = pControl.getSelected();
      if (pTreeItems != null)
      {
         int count = pTreeItems.length;
         for (int x = 0; x < count; x++)
         {
            // get the selected item
            IProjectTreeItem pTreeItem = pTreeItems[x];
            if (pTreeItem != null)
            {
               ITreeItem pDisp = pTreeItem.getProjectTreeSupportTreeItem();
               if (pDisp instanceof ITreeFolder)
               {
                  bSel = true;
                  break;
               }
            }
         }
      }

      return bSel;
   }

   /* Executes the accelerators for the addin
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorListener#onAcceleratorInvoke(java.lang.String)
    */
   public boolean onAcceleratorInvoke(String keyCode)
   {
      boolean bHandled = false;

      if (keyCode.equals("F6"))
      {
         IDiagram cpDiagram = getCurrentDiagram();
         if (cpDiagram != null)
         {
            int eKind = cpDiagram.getDiagramKind();
            switch (eKind)
            {
               case IDiagramKind.DK_COLLABORATION_DIAGRAM :
                  transformDiagramTo(cpDiagram, IDiagramKind.DK_SEQUENCE_DIAGRAM);
                  bHandled = true;
                  break;

               case IDiagramKind.DK_SEQUENCE_DIAGRAM :
                  transformDiagramTo(cpDiagram, IDiagramKind.DK_COLLABORATION_DIAGRAM);
                  bHandled = true;
                  break;

               default :
                  break;
            }
         }
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
   {
   }

   /* Used to register the accelerators needed for the addin.
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
   {
      IDiagram pParentDiagram = pDiagramControl.getDiagram();
      if (pParentDiagram instanceof IUIDiagram)
      {
         IUIDiagram pUIDiagram = (IUIDiagram) pParentDiagram;
         m_hook = (JComponent) pUIDiagram.getDrawingArea();

         registerAccelerators();
      }
   }

   /* Used to register the accelerators needed for the addin.
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
   {
      if (pParentDiagram instanceof IUIDiagram)
      {
         IUIDiagram pUIDiagram = (IUIDiagram) pParentDiagram;
         m_hook = (JComponent) pUIDiagram.getDrawingArea();

         registerAccelerators();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
   {

   }

    // Implement IAcceleratorListener interface. This method is not used in this class.
    public boolean onCreateNewNodeByKeyboard()
    {
        return false;
    }
}
