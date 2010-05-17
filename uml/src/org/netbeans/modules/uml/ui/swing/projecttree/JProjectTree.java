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


package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManagerEventsAdapter;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlsupport.PreventReEntrance;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.filter.ProjectTreeFilterDialogEventsAdapter;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogContext;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModelListener;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeUpdateLocker;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeDragVerifyImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeExpandingContextImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeModelEvent;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeUpdateLocker;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.DefaultEngineResource;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.INameCollisionListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.applicationmanager.NameCollisionListener;
import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogDisplayEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import javax.swing.JToolBar;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author treys
 *
 */
public class JProjectTree extends ApplicationView implements IProjectTreeControl
{
   public final static int PTIK_WORKSPACE    = 2;
   public final static int PTIK_PROJECT      = 4;
   public final static int PTIK_MODELELEMENT = 8;
   public final static int PTIK_DIAGRAM      = 16;

   public final static int SA_OPEN = 2;
   public final static int SA_CLOSE = 4;
   public final static int SA_SAVE = 8;

   //private DispatchHelper m_DispatchHelper   = new DispatchHelper();
   private boolean        m_InDragProcess    = false;
   private JTree          m_Tree             = null;//new JTree();
   private DispatchHelper m_DispatcherHelper = new DispatchHelper();
   private INameCollisionListener m_NameCollisionListener = null;
   private IProjectTreeCollisionHandler m_CollisionHandler = null;

   private PreferenceManagerEventHandler m_PrefHandler = new PreferenceManagerEventHandler();

//   private StructuredSelectionProvider m_SelectionProvider = new StructuredSelectionProvider();
   private String m_ConfigMgrName = "ProjectTree"; //$NON-NLS-1$

   private TreePath[] m_DragPaths = null;

   public JProjectTree()
   {
      this(null);
   }

   /**
    * @param newModel
    */
   public JProjectTree(ISwingProjectTreeModel newModel)
   {
      super("org.netbeans.modules.uml.view.projecttree"); //$NON-NLS-1$

      try
      {
         setModel(newModel);

         m_Tree = new JTree()
         {
            public void paint(java.awt.Graphics g)
            {
               // I understand that I am putting a bandaide on an issue.  For
               // some reason the UI for JTree is trowing a NullPointerException.
               // This is only occuring in side of Sun One Studio after an RE.
               // Since I can not step through the Debugger and I can not add
               // debug statements (since it is a JDK class). I have no choice
               // but to add a bandaide.
               try
               {
                  super.paint(g);
               }
               catch(NullPointerException e)
               {
               }
            }
         };

         m_Tree.setCellRenderer(new ProjectTreeRender());
         m_Tree.setSelectionModel(new DefaultTreeSelectionModel());
         m_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         //setEditable(true);
         m_Tree.setEnabled(true);
         m_Tree.setDragEnabled(true);
         m_Tree.setTransferHandler( new ProjectTreeTransferHandler()  );
         m_Tree.setAutoscrolls(true);

         m_Tree.addTreeSelectionListener(new TreeSelectionListener()
         {
            public void valueChanged(TreeSelectionEvent e)
            {
               //ETSystem.out.println("I am here.");
               fireSelectionChange();
            }
         });

         m_Tree.addMouseListener(new ProjectTreeMouseHandler());
         m_Tree.addKeyListener(new ProjectTreeKeyboardHandler());
         m_Tree.addTreeWillExpandListener(new ProjectTreeExpandHandler());

         m_DispatcherHelper.registerForPreferenceManagerEvents(m_PrefHandler);
         m_DispatcherHelper.registerProjectTreeFilterDialogEvents(new FilterListener());

         // Initialize based on perferences.
         //         String perfValue = ProductHelper.getPreferenceValue("Workspace",
         //                                                             "ShowWorkspaceNode");
         //         m_Tree.setRootVisible(perfValue.equals("PSK_YES") );
         //         m_Tree.setShowsRootHandles(perfValue.equals("PSK_YES") == false);

         m_Tree.setInvokesStopCellEditing(true);
         m_Tree.setCellEditor(new ProjectTreeCellEditor(m_Tree));
         m_Tree.setEditable(true);

         registerContextMenu(true);
         if (newModel != null)
         {
            newModel.setProjectTree(m_Tree);
         }
         // Create the name collision listener
         m_NameCollisionListener = new NameCollisionListener();
         m_CollisionHandler = new ProjectTreeCollisionHandler();

         m_NameCollisionListener.setHandler(m_CollisionHandler);
         m_CollisionHandler.setProjectTree(this);

      }
      catch(Exception e)
      {
      }
   }

   protected void registerTreeAccelerator(final String accelerator)
   {
		ActionListener packageAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onAcceleratorAction(accelerator);
			}
		};
		registerKeyboardAction(packageAction, KeyStroke.getKeyStroke(accelerator), JComponent.WHEN_IN_FOCUSED_WINDOW);
   }

   //   /**
   //    *
   //    */
   //   protected void addControls()
   //   {
   //      setLayout(new BorderLayout());
   //      add(m_Tree, BorderLayout.CENTER);
   //   }

   public void setModel(TreeModel model)
   {
      try
      {
         if(model instanceof ISwingProjectTreeModel)
         {
            ProjectTreeModelListener listener = new ProjectTreeModelListener();
            model.addTreeModelListener(listener);

            if(model instanceof ISwingProjectTreeModel)
            {
               ISwingProjectTreeModel swingModel = (ISwingProjectTreeModel)model;
               swingModel.addProjectTreeModelListener(listener);
               setConfigMgrName(swingModel.getModelName());
            }

            m_Tree.setModel(model);

				// Add accelerators
				ISwingProjectTreeModel newModel = (ISwingProjectTreeModel)model;
				if (newModel.getProjectTreeName().equals(ProjectTreeResources.getString("ProjectTreeSwingModel.ProjectTree_Name")))
				{
					registerTreeAccelerator(ProjectTreeResources.getString("IDS_CTRLD"));
					registerTreeAccelerator(ProjectTreeResources.getString("IDS_CTRLK"));
					registerTreeAccelerator(ProjectTreeResources.getString("IDS_CTRLE"));
					registerTreeAccelerator(ProjectTreeResources.getString("IDS_CTRLB"));
					registerTreeAccelerator(ProjectTreeResources.getString("IDS_CTRLT"));
				}
         }
      }
      catch (Exception e)
      {
         //ignore - sometimes setModel throws null pointer.
      }
      finally
      {
         if(model instanceof ISwingProjectTreeModel)
         {
            ((ISwingProjectTreeModel)model).setProjectTree(m_Tree);
         }
      }
   }

   public ISwingProjectTreeModel getProjectModel()
   {
      return (ISwingProjectTreeModel)m_Tree.getModel();
   }



   //**************************************************
   // ApplicationView Methods
   //**************************************************

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.application.ApplicationView#createViewControl(javax.swing.JPanel)
    */
   public void createViewControl(JPanel parent)
   {
      parent.setLayout(new BorderLayout());
      JScrollPane pane = new JScrollPane(m_Tree);
      parent.add(pane, BorderLayout.CENTER);
   }

   protected void contributeActionBars(JToolBar bars)
   {
      if(bars != null)
      {
         JToolBar tManager = bars; //.getToolBarManager();
         tManager.add(new FilterAction(this));
         tManager.add(new RefreshAction(this));
         tManager.setRollover(true);
      }
   }

   public void menuAboutToShow(IMenuManager manager)
   {
      manager.setContextObject(this);
      createOpenPullRight(manager);
      createNewPullRight(manager);

      manager.add(new SaveAction(this));
      manager.add(new CloseAction(this));
      manager.add(new Separator());
      //manager.add(new Separator("Element Actions"));

      manager.add(new DeleteAction(this));
      manager.add(new RenameAction(this));
      //manager.add(new Separator());

      createInsertPullRight(manager);
      createRemovePullRight(manager);
      manager.add(new Separator());

      getSelectedItemActions(manager);
      
      //manager.add(new DivideProjectAction(this))
      getModuleMenuItems(manager);
      
      manager.add(new Separator());

      if(packageExistInTree() == true)
      {
         manager.add(new ExpandAction(this));
      }

      //while showing the menu, I need to setup the New Dialog context, in case we show new dialog.
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         IWorkspace space = model.getWorkspace();

         INewDialogContext pContext = new NewDialogContext();
         pContext.setWorkspace(space);
         pContext.setProject(null);
         pContext.setUseAllProjectExtensions(true);
         pContext.setProjectTree(this);
      }
   }

   protected void getModuleMenuItems(IMenuManager manager)
   {
       manager.add(new Separator(), "additions"); //$NON-NLS-1$
   }
   
   protected void getSelectedItemActions(IMenuManager manager)
   {
       
   }
   
   /**
    * Do package icons exist in the tree
    *
    * @return true if packages are in the tree
    */
   private boolean packageExistInTree()
   {
      boolean foundPackage = false;
      ETList<IProjectTreeItem> projItems = getProjects();
      if (projItems != null)
      {
         int numProjs = projItems.size();
         for (int i=0; i<numProjs; i++)
         {
            IProjectTreeItem item = projItems.get(i);
            ETList<IProjectTreeItem> foundItems = findNodesRepresentingMetaType(item, "Package"); //$NON-NLS-1$
            if (foundItems != null)
            {
               int count = foundItems.size();
               if (count > 0)
               {
                  foundPackage = true;
                  break;
               }
            }
         }
      }
      return foundPackage;
   }

   /**
    * @param manager
    */
   private void createRemovePullRight(IMenuManager manager)
   {
      IMenuManager removeMenu = manager.createSubMenu(ProjectTreeResources.getString("JProjectTree.Remove_Menu"),  //$NON-NLS-1$
      "org.netbeans.modules.uml.view.projecttree.remove.popup"); //$NON-NLS-1$

      if(removeMenu != null)
      {
         removeMenu.add(new RemoveProjectAction(this));
      }

      manager.add(removeMenu);
   }

   /**
    * @param manager
    */
   private void createInsertPullRight(IMenuManager manager)
   {
      IMenuManager insertMenu = manager.createSubMenu(ProjectTreeResources.getString("JProjectTree.Insert_Menu"),  //$NON-NLS-1$
      "org.netbeans.modules.uml.view.projecttree.insert.popup"); //$NON-NLS-1$

      if(insertMenu != null)
      {
         insertMenu.add(new InsertProjectAction(this));
      }
      manager.add(insertMenu);
   }

   /**
    * @param manager
    */
   private void createNewPullRight(IMenuManager manager)
   {
      IMenuManager newMenu = manager.createSubMenu(ProjectTreeResources.getString("JProjectTree.New_Menu"),  //$NON-NLS-1$
      "org.netbeans.modules.uml.view.projecttree.insert.new"); //$NON-NLS-1$

      if(newMenu != null)
      {
         newMenu.add(new NewWorkspaceAction(this));
         newMenu.add(new NewProjectAction(this));
         newMenu.add(new NewDiagramAction(this));
         newMenu.add(new NewPackageAction(this));
         newMenu.add(new NewElementAction(this));
//         newMenu.add(new Separator("NewElement")); //$NON-NLS-1$

         newMenu.add(new NewAttributeAction(this));
         newMenu.add(new NewOperationAction(this));
      }

      manager.add(newMenu);
   }

   /**
    * @param manager
    */
   protected void createOpenPullRight(IMenuManager manager)
   {
      IMenuManager openMenu = manager.createSubMenu(ProjectTreeResources.getString("JProjectTree.Open_Menu"),  //$NON-NLS-1$
      "org.netbeans.modules.uml.view.projecttree.insert.open"); //$NON-NLS-1$

      if(openMenu != null)
      {
         openMenu.add(new OpenWorkspaceAction(this));
         openMenu.add(new OpenProjectAction(this));
         openMenu.add(new OpenDiagramAction(this));
      }

      manager.add(openMenu);
   }

   public void setPopupMenuListener(MouseListener listener)
   {
      m_Tree.addMouseListener(listener);
   }


   //**************************************************
   // Fire Events
   //**************************************************

   public void fireSelectionChange()
   {
      if(m_InDragProcess == false)
      {
         //         TreePath[] paths = getSelectionPaths();
         //         IProjectTreeItem[] items = new IProjectTreeItem[paths.length];
         //
         //         for (int index = 0; index < paths.length; index++)
         //         {
         //            ITreeItem curItem = getProjectModel().getTreeItem(paths[index]);
         //            items[index] = curItem.getData();
         //         }

         IProjectTreeItem[] items = getSelected();

         IProjectTreeEventDispatcher disp = m_DispatcherHelper.getProjectTreeDispatcher();
         if(disp != null)
         {
            IEventPayload payload = disp.createPayload("ProjectTreeSelChanged"); //$NON-NLS-1$
            disp.fireSelChanged(this, items, payload);

         }
      }
   }

   public boolean fireItemExpanding(ITreeItem item)
   {
      boolean retVal = true;

      IProjectTreeEventDispatcher disp = m_DispatcherHelper.getProjectTreeDispatcher();
		ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();

      if(disp != null)
      {
         try
         {
            IEventPayload payload = disp.createPayload("ProjectTreeItemExpanding"); //$NON-NLS-1$
            IProjectTreeExpandingContext context = new ProjectTreeExpandingContextImpl(item);
            disp.fireItemExpanding(this, context, payload);

            retVal = !context.isCancel();
         }
         finally
         {
				waitCursor.stop();
         }
      }

      //	   recursive--;
      return retVal;
   }

   public boolean fireItemExpanding(TreePath path)
   {
      boolean retVal = true;

      //m_Tree.addSelectionPath(path);

      ITreeItem item = getProjectModel().getTreeItem(path);
      if((item != null) && ((item.getChildCount() <= 0) || (item.isInitalized() == false)))
      {
         retVal =  fireItemExpanding(item);
      }

      return retVal;
   }

   /**
    * @param selPath
    */
   public void fireDoubleClick(ITreeItem item,
   boolean   isControl,
   boolean   isShift,
   boolean   isAlt,
   boolean   isMeta)
   {
      boolean retVal = true;

      IProjectTreeEventDispatcher disp = m_DispatcherHelper.getProjectTreeDispatcher();

      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeDoubleClick"); //$NON-NLS-1$
         disp.fireDoubleClick(this, item.getData(),
         isControl,
         isShift,
         isAlt,
         isMeta,
         payload);
      }
   }

   /**
    * @param selPath
    */
   public void fireDoubleClick(TreePath path,
   boolean isControl,
   boolean isShift,
   boolean isAlt,
   boolean isMeta)
   {
      ITreeItem item = getProjectModel().getTreeItem(path);
      if(item != null)
      {
         fireDoubleClick(item,
         isControl,
         isShift,
         isAlt,
         isMeta);
      }
   }

   /**
    * Notifies listeners that project tree item is being draged.
    *
    * @param items The project tree items that are being draged.
    * @return <code>true</code> if the drag can continu.
    */
   public boolean fireBeginDrag(IProjectTreeItem[] items)
   {
      boolean retVal = true;

      IProjectTreeEventDispatcher disp = m_DispatcherHelper.getProjectTreeDispatcher();

      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeBeginDrag"); //$NON-NLS-1$
         IProjectTreeDragVerify context = new ProjectTreeDragVerifyImpl();

         disp.fireBeginDrag(this, items, context, payload);

         retVal = !context.isCancel();

         m_InDragProcess = retVal;

      }
      return retVal;
   }

   /**
    * Notifies listeners that project tree item is being draged.
    *
    * @param paths The path to the tree items.
    * @return <code>true</code> if the drag can continu.
    */
   public boolean fireBeginDrag(TreePath[] paths)
   {
      boolean retVal = true;

      IProjectTreeItem[] items = new IProjectTreeItem[paths.length];

      for (int index = 0; index < paths.length; index++)
      {
         ITreeItem curItem = getProjectModel().getTreeItem(paths[index]);

         if(curItem != null)
         {
            items[index] = curItem.getData();
         }
      }

      m_DragPaths = paths;
      if((items != null) && (items.length > 0))
      {
         fireBeginDrag(items);
      }

      return retVal;
   }

   /**
    * Notifies listeners that the drag process has been completed.  This event
    * is only fired if project tree is the drop target.
    *
    * @param data The data that was draged onto the project tree.
    * @param action The action to occur.
    * @return <code>true</code> if the drop can continue.
    */
   public boolean fireEndDrag(Transferable data, int action)
   {
      boolean retVal = true;
      m_InDragProcess = false;

      IProjectTreeEventDispatcher disp = m_DispatcherHelper.getProjectTreeDispatcher();

      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeEndDrag"); //$NON-NLS-1$
         IProjectTreeDragVerify context = new ProjectTreeDragVerifyImpl();
         context.setTargetNode(getDropTargetItem());
         disp.fireEndDrag(this, data, action, context, payload);

         retVal = !context.isCancel();
         m_DragPaths = null;
         //m_InDragProcess = retVal;

      }
      return retVal;
   }

   /**
    * Notifies listeners that the drag operation is moving.  This event will
    * only be sent if the drag operation is on top of the project tree.
    *
    * @param data The data that was draged onto the project tree.
    * @param context The draw context.
    * @return <code>true</code> if the item can be dropped on the project tree
    *         item.
    */
   public boolean fireMoveDrag(Transferable           data,
   IProjectTreeDragVerify context)
   {
      boolean retVal = true;
      m_InDragProcess = false;

      IProjectTreeEventDispatcher disp = m_DispatcherHelper.getProjectTreeDispatcher();

      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeMoveDrag"); //$NON-NLS-1$
         disp.fireMoveDrag(this, data, context, payload);

         retVal = !context.isCancel();

         m_InDragProcess = retVal;

      }
      return retVal;


   }

   //**************************************************
   // JTree Override Methods
   //**************************************************


        /* (non-Javadoc)
         * @see javax.swing.JTree#fireTreeWillExpand(javax.swing.tree.TreePath)
         */
   //	public void fireTreeWillExpand(TreePath path)
   //	  throws ExpandVetoException
   //	{
   //		if(fireItemExpanding(path) == false)
   //		{
   //			throw new ExpandVetoException(null);
   //		}
   //		else
   //		{
   //         m_Tree.fireTreeWillExpand(path);
   //		}
   //	}

   //	**************************************************
   // IProjectTreeControl methods
   // **************************************************
   public void filterTree()
   {
      IFilterDialog dialog = UIFactory.createProjectTreeFilterDialog(m_Tree, getProjectModel());
      if(dialog != null)
      {
         dialog.show();
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getRootNodes()
         */
   public ETList<IProjectTreeItem> getRootNodes()
   {
      // TODO Auto-generated method stub
      return null;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getStandardDescription(int)
         */
   public String getStandardDescription(int nKind)
   {
      // TODO Auto-generated method stub
      return null;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getUserCalledShowWorkspaceNode()
         */
   public boolean getUserCalledShowWorkspaceNode()
   {
      // TODO Auto-generated method stub
      return false;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getShowWorkspaceNode()
         */
   public boolean getShowWorkspaceNode()
   {
      // TODO Auto-generated method stub
      return false;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setShowWorkspaceNode(boolean)
         */
   public void setShowWorkspaceNode(boolean value)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addWorkspace()
         */
   public long addWorkspace()
   {
      // TODO Auto-generated method stub
      return 0;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getUnfilteredProjects()
         */
   public IStrings getUnfilteredProjects()
   {
      // TODO Auto-generated method stub
      return null;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setUnfilteredProjects(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
         */
   public void setUnfilteredProjects(IStrings value)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#refresh(boolean)
         */
   public void refresh(boolean bPostEvent)
   {
      PreventReEntrance prev = new PreventReEntrance();
      prev.startBlocking(0);

      try
      {
         if (!prev.isBlocking())
         {
            ISwingProjectTreeModel model = getProjectModel();

            if(bPostEvent == true)
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        refresh(false);
                     }
                     catch (Exception ex)
                     {
                        //if it fails in refresh - ignore.
                     }
                  }
               });
            }
            else
            {
               PreserveTreeState preserveState = new PreserveTreeState(m_Tree);

               try
               {
                  preserveState.perserveState();

                  model.clear();
//                  ITreeItem rootItem = model.getRootItem();
//                  fireItemExpanding(rootItem);

                  m_Tree.updateUI();

               }
               catch(Exception e)
               {
                  //just ignore it - looks like updateUI is failing with Null Pointer some times.
                  e.printStackTrace();
               }
               finally
               {
                  m_Tree.treeDidChange();
                  preserveState.restoreState();
               }
            }
         }
      }
      catch (Exception exc)
      {
         UMLMessagingHelper helper = new UMLMessagingHelper();
         helper.sendExceptionMessage(exc);
      }
      finally
      {
         prev.releaseBlock();
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#close()
         */
   public void close()
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#sortChildNodes(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public void sortChildNodes(IProjectTreeItem pParent)
   {
      ISwingProjectTreeModel model = getProjectModel();
      if(model != null)
      {
         ITreeItem item = pParent.getProjectTreeSupportTreeItem();
         if(item == null)
         {
            ITreeItem[] items = pParent.getPath();

            if((items != null) && (items.length > 0))
            {
               item = items[items.length - 1];
            }

         }

         if(item != null)
         {
            model.sortChildren(item);
         }
      }

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#sortThisNode(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public void sortThisNode(IProjectTreeItem pNode)
   {
      // TODO Auto-generated method stub

   }


   /**
    * Sets the name of the node that is associated with the project tree item.
    * The name of the node can be used to determine the icon that is displayed.
    *
    * @param pItem The tree item to update.
    * @param name The name of the node.
    */
   public void setNodeName(IProjectTreeItem pItem, String name)
   {
      if(pItem != null)
      {
         TreePath path = new TreePath(pItem.getPath());
         ITreeItem curItem = getProjectModel().getTreeItem(path);
         curItem.setName(name);
      }
   }

  /* (non-Javadoc)
   * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setDescription(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
   */
   public void setDescription(IProjectTreeItem pItem, String sDesc)
   {
      if(pItem != null)
      {
         pItem.setDescription(sDesc);
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setSecondaryDescription(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
         */
   public void setSecondaryDescription(IProjectTreeItem pItem, String sDesc)
   {
      if (pItem != null)
      {
         pItem.setSecondaryDescription(sDesc);
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setDispatch(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.Object)
         */
   public void setDispatch(IProjectTreeItem pItem, Object pDisp)
   {
      if(pItem != null)
      {
         pItem.setData(pDisp);
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setModelElement(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
   public void setModelElement(IProjectTreeItem pItem, IElement pEle)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public IProjectTreeItem addItem( IProjectTreeItem pParent,
                                    String programName,
                                    String sText,
                                    int nSortPriority,
                                    IElement pElement)
   {
      IProjectTreeItem pCreatedItem  = null;
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         pCreatedItem = model.addItem(pParent, programName, sText, (long)nSortPriority, pElement, null, sText);
      }
      return pCreatedItem;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addItem2(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public IProjectTreeItem addItem( IProjectTreeItem pParent,
                                    String programName,
                                    String sText,
                                    int nSortPriority,
                                    IElement pElement,
                                    String sDescription)
   {
      IProjectTreeItem pCreatedItem = null;
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         pCreatedItem = model.addItem(pParent, programName, sText, (long)nSortPriority, pElement, null, sDescription);
      }
      return pCreatedItem;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addItem3(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.Object, java.lang.String, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public IProjectTreeItem addItem( IProjectTreeItem pParent,
                                    String programName,
                                    String sText,
                                    int nSortPriority,
                                    IElement pElement,
                                    Object pProjectTreeSupportTreeItem,
                                    String sDescription)
   {
      IProjectTreeItem pCreatedItem = null;
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         pCreatedItem = model.addItem(pParent, programName, sText, (long)nSortPriority, pElement,
         pProjectTreeSupportTreeItem, sDescription);
      }
      return pCreatedItem;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#beginEditFirstSelected()
         */
   public void beginEditFirstSelected()
   {
      try
      {
         beginEditContext();
         TreePath[] paths = m_Tree.getSelectionPaths();
         if (paths != null && paths.length > 0)
         {
            TreePath firstOne = paths[0];
            m_Tree.startEditingAtPath(firstOne);
         }
      }
      catch (Exception e)
      {
          //Log.out("Exception in EditFirstSelected " + e.getMessage());
      }
   }

   /**
    * Tells the listener we've got as a member that it was us that began editing
    */
   public void beginEditContext()
   {
      try
      {
         if (m_NameCollisionListener != null)
         {
            m_NameCollisionListener.setEnabled(true);
         }
      }
      catch (Exception e)
      {
      }
   }


        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#beginEditThisItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public void beginEditThisItem(final IProjectTreeItem pItem)
   {

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               ITreeItem[] path = pItem.getPath();

               if((path != null) && (path.length > 0))
               {
                  TreePath treePath = new TreePath(path);

                  m_Tree.clearSelection();

                  m_Tree.expandPath(treePath.getParentPath());
                  m_Tree.addSelectionPath(treePath);
                  beginEditFirstSelected();
               }
            }
            catch (Exception e)
            {
            }
         }
      });
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getSelected()
         */
   public IProjectTreeItem[] getSelected()
   {
      IProjectTreeItem[] retVal = null;

      TreePath[] paths = m_Tree.getSelectionPaths();
      if((paths != null) && (paths.length > 0))
      {
         retVal = new IProjectTreeItem[paths.length];

         for (int index = 0; index < paths.length; index++)
         {
            ITreeItem curItem = getProjectModel().getTreeItem(paths[index]);
            retVal[index] = curItem.getData();
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#deselectAll()
         */
   public void deselectAll()
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#endEditing(boolean)
         */
   public void endEditing(boolean bSaveChanges)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Deletes the selected tree items.  Right now it only knows how to delete model elements and
    * diagrams.
    */
   public void deleteSelectedItems()
   {
      boolean foundOnlyTheseSelected = areJustTheseSelected(PTIK_MODELELEMENT | PTIK_DIAGRAM);
      if (foundOnlyTheseSelected)
      {
         IProjectTreeItem[] items = getSelected();
         if (items != null)
         {
            int count = items.length;

            // Gather up all the diagrams and model elements in preparation for deleting.
            ETList<IElement> pModelElements = new ETArrayList<IElement>();
            Vector<String> diagrams = new Vector<String>();
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = items[i];
               IElement modEle = item.getModelElement();
               boolean isDiagram = item.isDiagram();
               boolean isImportedPackage = item.isImportedPackage();
               boolean isImportedModEle = item.isImportedModelElement();

               // If this item is a model element then add to the list of model elements,
               // if it's a diagram then add it to the list of names associated with the
               // diagrams.
               if (isImportedPackage)
               {
                  IPackageImport pImport = item.getImportedPackage();
                  if (pImport != null)
                  {
                     pModelElements.add(pImport);
                  }
               }
               else if (isImportedModEle)
               {
                  IElementImport eImport = item.getImportedModelElement();
                  if (eImport != null)
                  {
                     pModelElements.add(eImport);
                  }
               }
               else if (modEle != null)
               {
                  pModelElements.add(modEle);
               }
               else if (isDiagram)
               {
                  String diaName = item.getDescription();
                  if (diaName != null && diaName.length() > 0)
                  {
                     diagrams.add(diaName);
                  }
               }
            }

            // Now actually do the delete of the model elements
            int numModEle = pModelElements.size();
            for (int j=0; j<numModEle; j++)
            {
               IElement modEle = pModelElements.get(j);
               modEle.delete();
               //					How to take care of user cancel action?
               //					if (hr == EFR_S_EVENT_CANCELLED)
               //					{
               //					   // The user hit 'no' to a cancel, so stop whacking model elements and diagrams
               //					   break;
               //					}
            }

            // If the user canceled a delete then don't continue to delete diagrams.
            //				if (hr != EFR_S_EVENT_CANCELLED)
            //				{
            // Now whack the diagrams
            IWorkspace space = ProductHelper.getWorkspace();
            IProxyDiagramManager proxyMgr = ProxyDiagramManager.instance();
            int numDias = diagrams.size();
            for (int k=0; k<numDias; k++)
            {
               String location = diagrams.get(k);
               proxyMgr.removeDiagram(location);
            }
         }
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setText(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
         */
   public void setText(IProjectTreeItem pItem, String sText)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getParent(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public IProjectTreeItem getParent(IProjectTreeItem pItem)
   {
      IProjectTreeItem retVal = null;

      if(getProjectModel() != null)
      {
         ISwingProjectTreeModel model = getProjectModel();
         ITreeItem treeItem = model.getTreeItem(new TreePath(pItem.getPath()));

         if(treeItem != null)
         {
            ITreeItem parent = treeItem.getParentItem();
            if (parent != null)
            {
               retVal = parent.getData();
            }
         }
      }
      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#retrieveProjectFromItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public IProject retrieveProjectFromItem(IProjectTreeItem pProjItem)
   {
      IProject retVal = null;

      if(pProjItem != null)
      {
         IProjectTreeItem curItem = pProjItem;

         retVal = curItem.getProject();
         while((retVal == null) && (curItem != null))
         {
            curItem = getParent(curItem);
            if(curItem != null)
            {
               retVal = curItem.getProject();
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getProjects()
         */
   public ETList<IProjectTreeItem> getProjects()
   {
      ETList<IProjectTreeItem> retVal = new ETArrayList<IProjectTreeItem>();
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         ITreeItem item = model.getRootItem();
         if (item != null)
         {
            int count = item.getChildCount();
            if (count > 0)
            {
               for (int i=0; i<count; i++)
               {
                  ITreeItem child = item.getChild(i);
                  IProjectTreeItem projItem = child.getData();
                  if (projItem.isProject())
                  {
                     retVal.add(projItem);
                  }
                  else if (projItem.isWorkspace())
                  {
                     //get its children and add to the retVal
                  }
               }
            }
         }
      }
      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getWorkspaceTreeItem()
         */
   public IProjectTreeItem getWorkspaceTreeItem()
   {
      IProjectTreeItem retVal = null;

      if(getProjectModel() != null)
      {
         ITreeItem item = getProjectModel().getRootItem();

         if(item != null)
         {
            IProjectTreeItem data = item.getData();
            if(data != null)
            {
               if(data.isWorkspace() == true)
               {
                  retVal = data;
               }
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getChildren(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public ETList<IProjectTreeItem> getChildren(IProjectTreeItem pItem)
   {
      ETList<IProjectTreeItem> retObj = null;
      if (pItem != null)
      {
         ITreeItem item = pItem.getProjectTreeSupportTreeItem();
         if (item != null)
         {
            Enumeration<ITreeItem> iter = item.getNodeChildren();
            if (iter != null)
            {
               retObj = new ETArrayList<IProjectTreeItem>();
               while (iter.hasMoreElements())
               {
                  ITreeItem retItem = iter.nextElement();
                  IProjectTreeItem itemToAdd = retItem.getData();
                  if (itemToAdd != null)
                  {
                     retObj.add(itemToAdd);
                  }
               }
            }
         }
      }
      return retObj;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getChildDiagrams(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public IProjectTreeItem[] getChildDiagrams(IProjectTreeItem pItem)
   {
      // TODO Auto-generated method stub
      return null;
   }

        /*
         * finds the node with given topLevelId and xmiid in the tree
         */
   public ETList<IProjectTreeItem> findNode(ITreeItem parent, String sTopLevelXMIID, String itemXMIID)
   {
      ETList<IProjectTreeItem> retObj = new ETArrayList<IProjectTreeItem>();
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         ITreeItem root = parent;
         if (parent == null)
         {
            root = model.getRootItem();
         }
         if (root != null)
         {
            Enumeration<ITreeItem> children = root.getNodeChildren();
            if (children != null)
            {
               while (children.hasMoreElements())
               {
                  ITreeItem item = children.nextElement();
                  IProjectTreeItem projItem = item.getData();
                  if (projItem != null)
                  {
                     IElement elem = projItem.getModelElement();
                     if (elem != null)
                     {
                        String topId = elem.getTopLevelId();
                        String xmiid = elem.getXMIID();
                        if (topId != null && xmiid != null &&
                        topId.equals(sTopLevelXMIID) && xmiid.equals(itemXMIID))
                        {
                           retObj.add(projItem);
                        }
                     }
                  }

                  //See if we need to recurse?
                  ETList<IProjectTreeItem> childItems = findNode(item, sTopLevelXMIID, itemXMIID);
                  if (childItems != null && childItems.size() > 0)
                  {
                     retObj.addAll(childItems);
                  }
               }
            }
         }
      }
      return retObj;
   }

   /**
    * Finds a node given an element.
    *
    * @param pElement [in] The element to search for
    * @param pFoundItems [out,retval] The found tree items that represent this element
    */
   public ETList<IProjectTreeItem> findNode2(IElement pElement)
   {
      ETList<IProjectTreeItem> retObj = null;
      if (pElement != null)
      {
         String topLevelId = pElement.getTopLevelId();
         String modelXMIID = pElement.getXMIID();
         retObj = findNode(null, topLevelId, modelXMIID);
      }
      return retObj;
   }

   /**
    * Returns the direct children of pParent that have the description.
    *
    * @param pParent [in] The parent where we'll begin our search
    * @param sDescription [in] The description to look for
    * @param bCallRecursively [in] Should we call recursively
    * @param pFoundItems [out,retval] All the items that have sDescription as a description
    */
   public ETList<IProjectTreeItem> findNodeWithDescription(IProjectTreeItem pParent,
   String sDescription,
   boolean bCallRecursively)
   {
      ETList<IProjectTreeItem> retObj = new ETArrayList<IProjectTreeItem>();
      if (sDescription != null && sDescription.length() > 0)
      {
         ETList<IProjectTreeItem> allChildren = null;
         if (pParent == null)
         {
            allChildren = getRootNodes();
         }
         else
         {
            allChildren = getChildren(pParent);
         }
         if (allChildren != null)
         {
            int count = allChildren.size();
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = allChildren.get(i);
               String desc = item.getDescription();
               if (desc != null && desc.equals(sDescription))
               {
                  retObj.add(item);
               }

               if (bCallRecursively)
               {
                  // Now recursively call this routine to find more items
                  ETList<IProjectTreeItem> pChildItems = findNodeWithDescription(item, sDescription, true);
                  if (pChildItems != null && pChildItems.size() > 0)
                  {
                     retObj.addAll(pChildItems);
                  }
               }
            }
         }
      }
      return retObj;
   }

   /**
    * Returns the direct children of pParent that have the metatype sMetaType.
    *
    * @param pParent [in] The parent where we'll begin our search
    * @param sMetaType [in] The metatype we're looking for in the tree
    * @param pFoundItems [out,retval] All the items that representation a metatype
    */
   public ETList<IProjectTreeItem> findNodesRepresentingMetaType(IProjectTreeItem pParent, String sMetaType)
   {
      ETList<IProjectTreeItem> retVal = new ETArrayList<IProjectTreeItem>();
      ETList<IProjectTreeItem> allChildren = getChildren(pParent);
      if (allChildren != null)
      {
         int count = allChildren.size();
         for (int i=0; i<count; i++)
         {
            IProjectTreeItem item = allChildren.get(i);
            String metaType = item.getModelElementMetaType();
            if (metaType != null && metaType.equals(sMetaType))
            {
               retVal.add(item);
            }

            // Now recursively call this routine to find more items
            ETList<IProjectTreeItem> childItems = findNodesRepresentingMetaType(item, sMetaType);
            if (childItems != null)
            {
               int subCount = childItems.size();
               if (subCount > 0)
               {
                  retVal.addAll(childItems);
               }
            }
         }
      }
      return retVal;
   }

   /**
    * Find a particular diagram node in the tree.  May exist in several places.
    *
    * @param sTOMFilename [in] The etl file that represents the diagram
    * @param pFoundItems [out,retval] The found diagram items
    */
   public ETList<IProjectTreeItem> findDiagramNode(ITreeItem parent, String sTOMFilename)
   {
      ETList<IProjectTreeItem> retObj = new ETArrayList<IProjectTreeItem>();
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         ITreeItem root = parent;
         if (parent == null)
         {
            root = model.getRootItem();
         }
         if (root != null)
         {
            Enumeration<ITreeItem> children = root.getNodeChildren();
            if (children != null)
            {
               while (children.hasMoreElements())
               {
                  ITreeItem item = children.nextElement();
                  IProjectTreeItem projItem = item.getData();
                  if (projItem != null)
                  {
                     boolean isDiagram = projItem.isDiagram();
                     if (isDiagram)
                     {
                        String name = projItem.getDescription();
                        if (name != null && name.equals(sTOMFilename))
                        {
                           retObj.add(projItem);
                        }
                     }
                  }

                  //Since diagram can exist in multiple places, need to find all.
                  ETList<IProjectTreeItem> childItems = findDiagramNode(item, sTOMFilename);
                  if (childItems != null && childItems.size() > 0)
                  {
                     retObj.addAll(childItems);
                  }
               }
            }
         }
      }
      return retObj;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#removeDiagramNode(java.lang.String, boolean)
         */
   public void removeDiagramNode(String sTOMFilename, boolean bPostEvent)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#removeFromTree(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[])
         */
   public void removeFromTree(IProjectTreeItem[] pRemovedItems)
   {
      ISwingProjectTreeModel model = getProjectModel();
      if(pRemovedItems != null)
      {
         for(int index = 0; index < pRemovedItems.length; index++)
         {
            IProjectTreeItem curItem = pRemovedItems[index];
            ITreeItem curTreeItem = model.getTreeItem(new TreePath(curItem.getPath()));

            if(curTreeItem != null)
            {
               model.removeNodeFromParent(curTreeItem);
            }
         }
      }

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#removeFromTree2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
         */
   public void removeFromTree2(IElement pElementToRemove)
   {
      ISwingProjectTreeModel model = getProjectModel();
      if(pElementToRemove != null)
      {
         ETList < ITreeItem > treeItems = model.findNodes(pElementToRemove);

         if(treeItems != null)
         {
            Iterator < ITreeItem > iter = treeItems.iterator();
            while(iter.hasNext() == true)
            {
               model.removeNodeFromParent(iter.next());
            }
         }
      }

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#selectProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
         */
   public void selectProject(IProject pProject)
   {
      ISwingProjectTreeModel model = getProjectModel();
      if(pProject != null)
      {
         ETList < ITreeItem > treeItems = model.findNodes(pProject);

         if((treeItems != null) && (treeItems.size() > 0))
         {

            ITreeItem item = treeItems.get(0);
            m_Tree.addSelectionPath(new TreePath(item.getPath()));
         }
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#lockWindowUpdate(boolean)
         */
   public void lockWindowUpdate(boolean bLock)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getDropTargetItem()
         */
   public IProjectTreeItem getDropTargetItem()
   {
      IProjectTreeItem retVal = null;
      IProjectTreeItem[] curSelected = getSelected();

      // During the drag the tree selects the items as they are passed over.
      // Therefore, there should only be one selected item.  The item that
      // is the drop target.
      if((curSelected != null) && (curSelected.length > 0))
      {
         retVal = curSelected[0];
      }

      return retVal;
   }

   /**
    * Fired when a project is closed.  This removes the project item from the tree control.
    *
    * @param sName [in] The name of the project to close.
    */
   public void closeProject(IProject pProject)
   {
      if (pProject != null)
      {
         //to do implement
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#openProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
         */
   public void openProject(IProject pProject)
   {
      try
      {
         ITreeItem item = getProjectModel().projectOpened(pProject);
         if (item != null)
         {
            m_Tree.expandPath(new TreePath(item.getPath()));
         }
      }
      catch (Exception e)
      {
      }
   }

   /* Returns true if only these kinds of tree items are selected.
    * The input is a long made up of ProjectTreeItemKind's.
    *
    * @param items [in] OR'ed ProjectTreeItemKind indicating the kind if items you want to query
    * @param pFoundOneOfTheseItems [out,retval] true if one of these items were found
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#areJustTheseSelected(int)
    */
   public boolean areJustTheseSelected(int itemsTypes)
   {
      boolean retVal = false;

      IProjectTreeItem[] items = getSelected();
      if(items != null)
      {
         if(items.length > 0)
         {
            retVal = true;
         }

         for (int index = 0; (index < items.length) && (retVal == true); index++)
         {
            boolean curTest = false;

            if(((itemsTypes & PTIK_WORKSPACE) == PTIK_WORKSPACE)&&
            (items[index].isWorkspace() == true))
            {
               curTest = true;
            }

            if(((itemsTypes & PTIK_PROJECT) == PTIK_PROJECT) &&
            (items[index].isProject() == true))
            {
               curTest = true;
            }

            if(((itemsTypes & PTIK_MODELELEMENT) == PTIK_MODELELEMENT) &&
            (items[index].isModelElement() == true))
            {
               curTest = true;
            }

            if(((itemsTypes & PTIK_DIAGRAM) == PTIK_DIAGRAM) &&
            (items[index].isDiagram() == true))
            {
               curTest = true;
            }

            // So basically if any of the above test passed then we found a
            // one of the desiried item types.  If all of the above test failed
            // then the current item is not one of the desired types.  Therefore,
            // we have just failed the test and the "for" loop will kick out.
            retVal = curTest;
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#atLeastOneOfTheseSelected(int)
         */
   public boolean atLeastOneOfTheseSelected(int itemsTypes)
   {
      boolean retVal = false;

      IProjectTreeItem[] items = getSelected();
      if(items != null)
      {
         for (int index = 0; (index < items.length); index++)
         {
            boolean curTest = false;

            if(((itemsTypes & PTIK_WORKSPACE) == PTIK_WORKSPACE)&&
            (items[index].isWorkspace() == true))
            {
               retVal = true;
            }

            if(((itemsTypes & PTIK_PROJECT) == PTIK_PROJECT) &&
            (items[index].isProject() == true))
            {
               retVal = true;
            }

            if(((itemsTypes & PTIK_MODELELEMENT) == PTIK_MODELELEMENT) &&
            (items[index].isModelElement() == true))
            {
               retVal = true;
            }

            if(((itemsTypes & PTIK_DIAGRAM) == PTIK_DIAGRAM) &&
            (items[index].isDiagram() == true))
            {
               retVal = true;
            }

            if (retVal)
            {
               break;
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, boolean)
         */
   public IElement getFirstSelectedModelElement()
   {
      IElement retVal = null;

      IProjectTreeItem item = getFirstSelectedModelElementItem();
      if(item != null)
      {
         retVal = item.getModelElement();
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedModelElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean)
         */
   public IProjectTreeItem getFirstSelectedModelElementItem()
   {
      IProjectTreeItem retVal = null;

      IProjectTreeItem[] selItems = getSelected();

      if((selItems != null) && (selItems.length > 0))
      {
         for (int index = 0; (index < selItems.length); index++)
         {
            if(selItems[index] != null)
            {
               if(selItems[index].isModelElement() == true)
               {
                  retVal = selItems[index];
                  break;
               }
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedDiagram(java.lang.StringBuffer, boolean)
         */
   public String getFirstSelectedDiagram()
   {
      String retVal = ""; //$NON-NLS-1$

      IProjectTreeItem[] selItems = getSelected();

      if((selItems != null) && (selItems.length > 0))
      {
         for (int index = 0; (index < selItems.length); index++)
         {
            if(selItems[index] != null)
            {
               if(selItems[index].isDiagram() == true)
               {
                  retVal = selItems[index].getDescription();
                  break;
               }
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedOpenDiagram(java.lang.StringBuffer, boolean)
         */
   public String getFirstSelectedOpenDiagram()
   {
      String retVal = ""; //$NON-NLS-1$

      IProjectTreeItem[] selItems = getSelected();

      if((selItems != null) && (selItems.length > 0))
      {
         for (int index = 0; (index < selItems.length); index++)
         {
            if(selItems[index] != null)
            {
               if(selItems[index].isDiagram() == true)
               {
                  IProxyDiagram proxy = selItems[index].getDiagram();
                  if((proxy != null) && (proxy.isOpen() == true))
                  {
                     retVal = selItems[index].getDescription();
                     break;
                  }
               }
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedClosedDiagram(java.lang.StringBuffer, boolean)
         */
   public String getFirstSelectedClosedDiagram()
   {
      String retVal = ""; //$NON-NLS-1$

      IProjectTreeItem[] selItems = getSelected();

      if((selItems != null) && (selItems.length > 0))
      {
         for (int index = 0; (index < selItems.length); index++)
         {
            if(selItems[index] != null)
            {
               if(selItems[index].isDiagram() == true )
               {
                  IProxyDiagram proxy = selItems[index].getDiagram();
                  if((proxy != null) && (proxy.isOpen() == false))
                  {
                     retVal = selItems[index].getDescription();
                     break;
                  }
               }
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedOpenProject(java.lang.StringBuffer, boolean)
         */
   public String getFirstSelectedOpenProject()
   {
      String retVal = ""; //$NON-NLS-1$

      IProjectTreeItem[] selItems = getSelected();

      if((selItems != null) && (selItems.length > 0))
      {
         for (int index = 0; (index < selItems.length); index++)
         {
            if(selItems[index] != null)
            {
               IProject project = selItems[index].getProject();

               if(project != null )
               {
                  retVal = selItems[index].getItemText();
                  break;
               }
            }
         }
      }

      return retVal;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedClosedProject()
         */
   public String getFirstSelectedClosedProject()
   {
      String retVal = ""; //$NON-NLS-1$

      IProjectTreeItem[] selItems = getSelected();

      if((selItems != null) && (selItems.length > 0))
      {
         for (int index = 0; (index < selItems.length); index++)
         {
            if(selItems[index] != null)
            {
               if((selItems[index].isProject() == true) &&
               (selItems[index].getProject() == null))
               {
                  retVal = selItems[index].getItemText();
                  break;
               }
            }
         }
      }

      return retVal;

   }


        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getWindowHandle()
         */
   public int getWindowHandle()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Searches for this element in the tree, selects it and makes it visible.
    *
    * @param pModelElement [in] The model element to find and select
    */
   public void findAndSelectInTree(IElement pModelElement)
   {
      ETList<IElement> pModelElements = new ETArrayList<IElement>();
      if (pModelElements != null)
      {
         // populate the model elements list with all the namespaces leading up to this
         // pModelElement.
         pModelElements.add(pModelElement);
         IElement pOwner = pModelElement.getOwner();
         while (pOwner != null)
         {
            IElement pThisModelElement = pOwner;
            pOwner = null;
            if (pThisModelElement != null)
            {
               pModelElements.add(pThisModelElement);
               pOwner = pThisModelElement.getOwner();
            }
         }
         int numModelElements = pModelElements.size();
         // Now that we have a list of model elements, expand every one.
         for (int i = numModelElements - 1 ; i >=0 ; i--)
         {
            IElement pThisModelElement = pModelElements.get(i);
            if (pThisModelElement != null)
            {
               ETList<IProjectTreeItem> pItems = findNode2(pThisModelElement);
               int numItems = 0;
               if (pItems != null)
               {
                  numItems = pItems.size();
               }
               if (numItems > 0)
               {
                  IProjectTreeItem pItem = pItems.get(0);
                  if (pItem != null)
                  {
                     ITreeItem treeItem = pItem.getProjectTreeSupportTreeItem();
                     if (treeItem != null)
                     {
                        TreePath path = new TreePath(treeItem.getPath());
                        m_Tree.expandPath(path);
                     }
                  }
               }
            }
         }

         // Find the target model element in the list and make it visible
         ETList<IProjectTreeItem> pItems = findNode2(pModelElement);
         int numItems = 0;
         if (pItems != null)
         {
            numItems = pItems.size();
         }
         if (numItems > 0)
         {
            IProjectTreeItem pItem = pItems.get(0);
            if (pItem != null)
            {
               ITreeItem treeItem = pItem.getProjectTreeSupportTreeItem();
               if (treeItem != null)
               {
                  TreePath path = new TreePath(treeItem.getPath());
                  m_Tree.setSelectionPath(path);
               }
            }
         }
      }
   }

   public void selectInTree(ITreeItem treeItem)
   {
        if (treeItem != null) {
            TreePath path = new TreePath(treeItem.getPath());
            m_Tree.setSelectionPath(path);
        }
   }
   
   public void clearSelectionInTree()
   {
       if (m_Tree != null)
           m_Tree.clearSelection();
   }
   
   public boolean requestTreeFocus()
   {
      return (m_Tree != null ? m_Tree.requestFocusInWindow() : false);
   }
   
   public void addTreeFocusListener(FocusListener listener)
   {
      m_Tree.addFocusListener(listener);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getHasBeenExpanded(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
     */
   public boolean getHasBeenExpanded(IProjectTreeItem pItem)
   {
      // TODO Auto-generated method stub
      return false;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getIsExpanded(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public boolean getIsExpanded(IProjectTreeItem pItem)
   {
      boolean bExpand = false;
      if (pItem != null)
      {
         ITreeItem treeItem = pItem.getProjectTreeSupportTreeItem();
         if (treeItem != null)
         {
            TreePath path = new TreePath(treeItem.getPath());
            bExpand = m_Tree.isExpanded(path);
         }
      }
      return bExpand;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setIsExpanded(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean)
         */
   public void setIsExpanded(IProjectTreeItem pItem, boolean value)
   {
      if (m_Tree != null && pItem != null)
      {
         ITreeItem[] path = pItem.getPath();

         if((path != null) && (path.length > 0))
         {
            TreePath treePath = new TreePath(path);

            if (value)
            {
               m_Tree.expandPath(treePath);
            }
            else
            {
               m_Tree.collapsePath(treePath);
            }
         }
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getIsSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
         */
   public boolean getIsSelected(IProjectTreeItem pItem)
   {
      // TODO Auto-generated method stub
      return false;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setIsSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean)
         */
   public void setIsSelected(IProjectTreeItem pItem, boolean value)
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#rememberTreeState()
         */
   public void rememberTreeState()
   {
      // TODO Auto-generated method stub

   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#restoreTreeState()
         */
   public void restoreTreeState()
   {
      // TODO Auto-generated method stub

   }

   /**
    * Opens the selected diagrams.
    *
    * @param pDiagramsActedUpon [out,retval] The opened diagrams
    */
   public ETList<IProxyDiagram> openSelectedDiagrams()
   {
      return actOnSelectedDiagrams(SA_OPEN);
   }

   /**
    * Closes the selected diagrams.
    *
    * @param pDiagramsActedUpon [out,retval] The closed diagrams
    */
   public ETList<IProxyDiagram> closeSelectedDiagrams()
   {
      return actOnSelectedDiagrams(SA_CLOSE);
   }

   /**
    * Saves the selected diagrams.
    *
    * @param pDiagramsActedUpon [out,retval] The saved diagrams
    */
   public ETList<IProxyDiagram> saveSelectedDiagrams()
   {
      return actOnSelectedDiagrams(SA_SAVE);
   }

   /**
    * Performs a specific action on the selected diagrams based on the flag.
    *
    * @param nAction [in] The action to perform
    * @param pDiagramsActedUpon [out,retval] The diagrams that were acted upon
    */
   private ETList<IProxyDiagram> actOnSelectedDiagrams(int nAction)
   {
      ETList<IProxyDiagram> retObj = new ETArrayList<IProxyDiagram>();
      IProductDiagramManager pDiaMgr = ProductHelper.getProductDiagramManager();
      IProxyDiagramManager proxyMgr = ProxyDiagramManager.instance();
      if (pDiaMgr != null)
      {
         IProjectTreeItem[] items = getSelected();
         if (items != null)
         {
            int count = items.length;
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = items[i];
               if (nAction == SA_OPEN)
               {
                  boolean isDiagram = item.isDiagram();
                  if (isDiagram)
                  {
                     String diaFilename = item.getDescription();
                     if (diaFilename != null && diaFilename.length() > 0)
                     {
                        IDiagram opnDia = pDiaMgr.openDiagram(diaFilename, true, null);
                        IProxyDiagram proxyDia = proxyMgr.getDiagram(diaFilename);
                        retObj.add(proxyDia);
                     }
                  }
               }
               else if (nAction == SA_CLOSE)
               {
                  IProxyDiagram proxyDia = item.getDiagram();
                  if (proxyDia != null)
                  {
                     boolean isOpen = proxyDia.isOpen();
                     String diaFilename = item.getDescription();
                     if (isOpen && diaFilename != null && diaFilename.length() > 0)
                     {
                        // Close the diagram
                        pDiaMgr.closeDiagram(diaFilename);

                        // See if it's still open - user could have hit cancel.
                        IDiagram dia = pDiaMgr.getOpenDiagram(diaFilename);
                        if (dia == null)
                        {
                           proxyDia = null;
                           proxyDia = proxyMgr.getDiagram(diaFilename);
                           retObj.add(proxyDia);
                        }
                     }
                     else if (diaFilename != null && diaFilename.length() > 0)
                     {
                        // Already closed.  This is ok, add it to our list
                        retObj.add(proxyDia);
                     }
                  }
               }
               else if (nAction == SA_SAVE)
               {
                  IProxyDiagram proxyDia = item.getDiagram();
                  if (proxyDia != null)
                  {
                     IDiagram dia = proxyDia.getDiagram();
                     if (dia != null)
                     {
                         try
                         {
                             dia.save();
                         } catch (IOException e)
                         {
                             Exceptions.printStackTrace(e);
                         }
                        retObj.add(proxyDia);
                     }
                  }
               }
            }
         }
      }
      return retObj;
   }

   /**
    * Closes the selected projects.
    */
   public void closeSelectedProjects(IWorkspace space)
   {
      actOnSelectedProjects(SA_CLOSE, space);
   }

   /**
    * Saves the selected projects.
    */
   public void saveSelectedProjects(IWorkspace space)
   {
      actOnSelectedProjects(SA_SAVE, space);
   }

   /**
    * Saves the selected projects in the workspace.
    */
   public void saveSelectedProjectsInWorkspace()
   {
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         IWorkspace space = getWorkspace();
         if (space != null)
         {
            IProjectTreeItem[] items = getSelected();
            if (items != null)
            {
               int count = items.length;
               for (int i=0; i<count; i++)
               {
                  IProjectTreeItem item = items[i];
                  boolean isProject = item.isProject();
                  IProject proj = item.getProject();
                  if (isProject && proj != null)
                  {
                     String name = item.getItemText();
                     if (name != null && name.length() > 0)
                     {
                        IWSProject wsProj = space.getWSProjectByName(name);
                        if (wsProj != null)
                        {
                           try
                           {
                              wsProj.save();
                           }
                           catch (WorkspaceManagementException e)
                           {
                              Log.stackTrace(e);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#handleLostProject(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
         */
   public void handleLostProject(IWSProject wsProject)
   {
      // TODO Auto-generated method stub

   }

   /**
    * Ask the user what to do about a name collision
    *
    * @param pElement [in] The element being renamed
    * @param sProposedName [in] The new name
    * @param pCollidingElements [in] A list of elements this name collides with
    * @param pCell [in] The result cell.  Used to cancel the rename.
    */
   public void questionUserAboutNameCollision(INamedElement pElement,
   String sProposedName,
   ETList<INamedElement> pCollidingElements,
   IResultCell pCell)
   {
      // Get the first colliding element
      INamedElement pFirstCollidingElement = null;
      if (pCollidingElements != null)
      {
         int count = pCollidingElements.getCount();
         if (count > 0)
         {
            pFirstCollidingElement = pCollidingElements.get(0);
         }
      }
      if (pFirstCollidingElement != null && pElement != null && pCell != null)
      {
		  DialogDisplayer.getDefault().notify(
						new NotifyDescriptor.Message(NbBundle.getMessage(
								JProjectTree.class, "JProjectTree.NameCollision")));
		  // Cancel the editing to abort the name collision
		   pCell.setContinue(false);
		   ProjectTreeCellEditor editor = (ProjectTreeCellEditor)m_Tree.getCellEditor();
		   if (editor != null)
		   {
			  editor.cancelCellEditing();
		   }
		   m_Tree.updateUI();
						
//         // Ask the user if he wants to reconnect the presentation element to a different model element
//         IQuestionDialog pDiag = new SwingQuestionDialogImpl();
//         if ( pDiag != null )
//         {
//            String title = ProjectTreeResources.getString("JProjectTree.NameCollisionTitle");
//            String msg = ProjectTreeResources.getString("JProjectTree.NameCollision");
//            QuestionResponse result = pDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONWARNING, msg, 0, null, title);
//            if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
//            {
//               // User wants to allow the name collision.
//            }
//            else if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
//            {
//               // Cancel the editing to abort the name collision
//               pCell.setContinue(false);
//               ProjectTreeCellEditor editor = (ProjectTreeCellEditor)m_Tree.getCellEditor();
//               if (editor != null)
//               {
//                  editor.cancelCellEditing();
//               }
//               m_Tree.updateUI();
//            }
//         }
      }
      else
      {
         pCell.setContinue(true);
      }
   }

   /**
    * Returns the number of open projects
    */
   public int getNumOpenProjects()
   {
      int numOpen = 0;
      IApplication app = ProductHelper.getApplication();
      if (app != null)
      {
         if (m_ConfigMgrName != null && m_ConfigMgrName.equals("ProjectTree")) //$NON-NLS-1$
         {
            ETList<IProject> projs = app.getProjects();
            if (projs != null)
            {
               numOpen = projs.size();
            }
         }
         else
         {
            ETList<IProject> projs = app.getProjects("etpat"); //$NON-NLS-1$
            if (projs != null)
            {
               numOpen = projs.size();
            }
         }
      }
      return numOpen;
   }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#postDeleteModelElement(java.lang.String, java.lang.String)
         */
   public void postDeleteModelElement(String sTopLevelXMIID, String sXMIID)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#isEditing()
    */
   public boolean isEditing()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /**
    * etrieves the first project selected in the tree. If more than one is selected,
    * the first is retrieved
    * @return If a project is found then the project is returned.  Otherwise,
    *         <code>null</code> is returned.
    */
   public IProject getSelectedProject()
   {
      IProject retVal = null;

      String projectName = getFirstSelectedOpenProject();

      if((projectName != null) && (projectName.length() > 0))
      {
         IApplication app = ProductHelper.getApplication();

         if(app != null)
         {
            retVal = app.getProjectByName(projectName);
         }
      }

      return retVal;
   }

   //**************************************************
   // Support Inner Classes
   //**************************************************
   public class ProjectTreeKeyboardHandler extends java.awt.event.KeyAdapter
   {
      
      public void keyPressed(java.awt.event.KeyEvent e)
      {
         String key = e.getKeyText(e.getKeyCode());
         if( (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) )
         {
            //fix for #6184543 and 6184550
            if(skipDelete())
               return;
            
            deleteSelectedElements();
         }
      }
      
      private boolean skipDelete()
      {
         //DO NOT allow delete for:-
         //1. project node in project tree
         //2. nodes in design center, except diagrams
         ISwingProjectTreeModel model = getProjectModel();
         IProjectTreeItem[] items = getSelected();
         if (items != null)
         {
            for (int i=0; i<items.length; i++)
            {
               //if project node, ignore delete
               IProjectTreeItem item = items[i];
               if(item != null && item.isProject())
               {
                  return true;
               }
               
               if(model != null)
               {
                  //do not allow delete on design center tree.
                  //well atleast for the non-diagram nodes
                  String modelName = model.getModelName();      
                  if ( (modelName.equals("DesignCenter") == true) &&
                       (item != null) &&
                       (!item.isDiagram()) )
                  {
                     return true;
                  }
               }
            }
         }
         return false;
      }
   }


   public class ProjectTreeMouseHandler extends MouseInputAdapter
   {
      public void mousePressed(MouseEvent e)
      {
         int selRow = m_Tree.getRowForLocation(e.getX(), e.getY());
         TreePath selPath = m_Tree.getPathForRow(selRow);
         //in any case I want to select this item.
         if (selRow != -1)
         {
            //if shift or control is down, user might be doing multi-select
            if (!e.isShiftDown() && !e.isControlDown())
            {
               int count = m_Tree.getSelectionCount();
               //if count is more than 1 I do not want to change the selection
               if (count <= 1)
               {
                  m_Tree.setSelectionPath(selPath);
               }
            }
         }
         if(e.getClickCount() == 2)
         {
            if(selRow != -1)
            {
               fireDoubleClick(selPath, e.isControlDown(),
               e.isShiftDown(),
               e.isShiftDown(),
               e.isMetaDown());
            }
         }
      }
   }


   public class ProjectTreeExpandHandler implements TreeWillExpandListener
   {

      /* (non-Javadoc)
       * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
       */
      public void treeWillExpand(TreeExpansionEvent e)
      throws ExpandVetoException
      {
         if(fireItemExpanding(e.getPath()) == false)
         {
            throw new ExpandVetoException(e);
         }
      }

      /* (non-Javadoc)
       * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
       */
      public void treeWillCollapse(TreeExpansionEvent event)
      throws ExpandVetoException
      {
      }
   }

   protected class PreferenceManagerEventHandler extends PreferenceManagerEventsAdapter
   {
       //kris richards - this will probably never get called since the pref no 
       //ShowWorkspaceNode pref expunged. set to false.
      public void onPreferenceChange(String name, IPropertyElement pElement,
      IResultCell cell)
      { }

      public void onPreferencesChange(IPropertyElement[] pElements,
      IResultCell cell)
      {
          //kris richards - these prefs are no longer valid so this code should never execute.
         for (int index = 0; index < pElements.length; index++)
         {
            String name = pElements[index].getName();
            if((name.equals("ShowAliasedNames") == true) ||  //$NON-NLS-1$
            (name.equals("DisplayTVs") == true)) //$NON-NLS-1$
            {
               refresh(true);
               break;
            }
         }
      }
   }

   protected class ProjectTreeModelListener implements TreeModelListener, IProjectTreeModelListener
   {

      public void treeNodesChanged(final TreeModelEvent e)
      {
         final TreePath path   = e.getTreePath();
         final boolean  expand = m_Tree.isExpanded(path);

         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               try
               {
                  m_Tree.updateUI();
                  if(expand == true)
                  {
                     m_Tree.expandPath(path);
                  }
               }
               catch(Exception ex)
               {
                  //just ignore the exception
               }
            }
         });
      }

      /**
       * Listens for new projects being inserted into the model.  When a project
       * is inserted into the model and it the project is open then the node
       * must also be expaneded.
       *
       * @param e The tree model event.
       */
      public void treeNodesInserted(TreeModelEvent e)
      {
         try
         {
            Object[] children = e.getChildren();
            for (int index = 0; index < children.length; index++)
            {
               if (children[index] instanceof ITreeItem)
               {
                  ITreeItem curItem = (ITreeItem)children[index];
                  IProjectTreeItem data = curItem.getData();

                  if((data != null) && (data.isProject() == true))
                  {
                     // The IProject model element will only be present if
                     // the project is opened.  When a project is closed the
                     // IProject element is removed from the ITreeItem.
                     //
                     // So, if the model element is present expand the node.
                     if(data.getModelElement() instanceof IProject)
                     {
                        m_Tree.expandPath(e.getTreePath().pathByAddingChild(curItem));
                     }
                  }
               }
            }

            m_Tree.treeDidChange();
         }
         catch (Exception ex)
         {
         }
         //
         //         final TreePath path = e.getTreePath();
         //
         //         ITreeItem parent = (ITreeItem)path.getLastPathComponent();
         //         int childrenMax = parent.getChildCount();
         //
         //         if(m_Tree.isExpanded(path) == true)
         //         {
         //            SwingUtilities.invokeLater(new Runnable()
         //            {
         //               public void run()
         //               {
         //                  try
         //                  {
         //                     m_Tree.treeDidChange();
         ////					      m_Tree.expandPath(path.getParentPath());
         //                     try
         //                     {
         //                        m_Tree.fireTreeWillExpand(path.getParentPath());
         //                     }
         //                     catch (ExpandVetoException eve)
         //                     {
         //                     }
         //                     m_Tree.fireTreeExpanded(path.getParentPath());
         //
         ////                     m_Tree.updateUI();
         //                  }
         //                  catch(Exception ex)
         //                  {
         //                     ex.printStackTrace();
         //                  }
         //               }
         //            });
         //         }
      }

      public void treeNodesRemoved(TreeModelEvent e)
      {
      }

      public void treeStructureChanged(TreeModelEvent e)
      {
      }

      /**
       * After the project has been closed the node must be collapsed in the
       * tree.  The model is not able to collapse the node because the model
       * does not know about the UI details.
       */
      public void projectClosed(ProjectTreeModelEvent e)
      {
         try
         {
            ITreeElement node = e.getTreeElement();

            TreePath path = new TreePath(node.getPath());
            if(m_Tree.isExpanded(path) == true)
            {
               m_Tree.collapsePath(path);
            }
         }
         catch (Exception ex)
         {
         }
      }

      /**
       * After the project has been opened it must be expanded.  The model is
       * not able to expand the node because the model does not know about
       * the UI details.
       */
      public void projectOpened(ProjectTreeModelEvent e)
      {
         try
         {
            ITreeElement node = e.getTreeElement();

            TreePath path = new TreePath(node.getPath());
            if(m_Tree.isExpanded(path) == false)
            {
               m_Tree.expandPath(path);
            }
         }
         catch (Exception ex)
         {
         }
      }
   }

   public class FilterListener extends ProjectTreeFilterDialogEventsAdapter
   {
      public void onProjectTreeFilterDialogOKActivated(IFilterDialog dialog,
      org.netbeans.modules.uml.core.support.umlsupport.IResultCell cell)
      {
         refresh(true);
      }
   }

   public class ProjectTreeAction extends BaseAction
   {
      private JProjectTree m_Tree = null;

      public ProjectTreeAction(String id, JProjectTree tree, String text)
      {
         this(tree, text, ""); //$NON-NLS-1$
         setId(id);
         setLabel(text);
      }

      public ProjectTreeAction(JProjectTree tree, String text, String iconPath)
      {
         setText(text);
         if((iconPath != null) && (iconPath.length() > 0))
         {
            setSmallIcon(new ImageIcon(this.getClass().getResource(iconPath)));
         }

         m_Tree = tree;
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IViewActionDelegate#init(org.netbeans.modules.uml.ui.products.ad.application.ApplicationView)
       */
      public void init(ApplicationView view)
      {
         if (view instanceof JProjectTree)
         {
            setProjectTree((JProjectTree)view);
         }
      }

      public JProjectTree getProjectTree()
      {
         return m_Tree;
      }

      public void setProjectTree(JProjectTree tree)
      {
         m_Tree = tree;
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#selectionChanged(org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction, org.netbeans.modules.uml.ui.products.ad.application.selection.ISelection)
       */
//      public void selectionChanged(PluginAction action, ISelection selection)
//      {
//
//      }
   }

   public class RefreshAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public RefreshAction(JProjectTree tree)
      {
         super(tree, ProjectTreeResources.getString("JProjectTree.Refresh_Action_Name"), ProjectTreeResources.getString("JProjectTree.Refresh_Icon_Path")); //$NON-NLS-1$ //$NON-NLS-2$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         refresh(true);
      }
   }

   public class FilterAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public FilterAction(JProjectTree tree)
      {
         super(tree, ProjectTreeResources.getString("JProjectTree.Filter_Action_Name"), ProjectTreeResources.getString("JProjectTree.Filter_Icon_Path")); //$NON-NLS-1$ //$NON-NLS-2$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         filterTree();
      }

      public boolean isEnabled()
      {
         //always enabled
         return true;
      }
   }

   public class DeleteAction extends ProjectTreeAction
   {
      public DeleteAction(JProjectTree tree)
      {
         super("MBK_DELETE", tree, ProjectTreeResources.getString("JProjectTree.Delete_Action_Name")); //$NON-NLS-1$

         if(tree != null)
         {
            boolean isEnabled = true;

            IProjectTreeItem[] items = tree.getSelected();
            if(items != null)
            {
               for (int index = 0; index < items.length; index++)
               {
                  if(getProjectModel().canDelete(items[index]) == false)
                  {
                     // We can always delete a diagram.
                     if(items[index].isDiagram() == false)
                     {
                        isEnabled = false;
                     }
                  }
               }
            }
            else
            {
               isEnabled = false;
            }

            if(isEnabled == true)
            {
               isEnabled = tree.areJustTheseSelected(PTIK_MODELELEMENT | PTIK_DIAGRAM);
            }

            setEnabled(isEnabled);
         }
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
            deleteSelectedElements();
      }

      public boolean isEnabled()
      {
         //It should be enabled only if I have an Element selected and this is not
         //project or workspace.
         boolean retVal = true;
         IProjectTreeItem[] items = getSelected();

         if (items != null)
         {
            boolean allowDelete = true;
            int count = items.length;
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = items[i];
               if (!getProjectModel().canDelete(item))
               {
                  // See if we have a diagram
                  boolean isDiagram = item.isDiagram();
                  if (!isDiagram)
                  {
                     retVal = false;
                     break;
                  }
               }
               else
               {
                  // so far you can delete the item, so now check
                  IProject proj = retrieveProjectFromItem(item);
                  if (proj != null)
                  {
                     boolean bMember = isMemberOfDesignCenterProject(proj);
                     if (bMember)
                     {
                        retVal = false;
                        break;
                     }
                  }
               }
            }

            if (retVal)
            {
               retVal = areJustTheseSelected(PTIK_MODELELEMENT | PTIK_DIAGRAM);
            }
         }
         else
         {
            // do not allow the delete to be enabled if nothing is selected in the project tree
            retVal = false;
         }

         return retVal;
      }
   }

      private void deleteSelectedElements() {
          ISwingProjectTreeModel model = getProjectModel();
          if (model == null)
              return;

          String title = loadString("IDS_DELETE_QUESTIONDIALOGTITLE"); //$NON-NLS-1$
          String message = loadString("IDS_DELETE_QUESTION"); //$NON-NLS-1$
          IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
          java.awt.Component component = this;
          if (ui != null) {
            component = ui.getWindowHandle();
          }
          JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
          JDialog dialog = pane.createDialog(component, title);
          dialog.setVisible(true);
          Object selectedValue = pane.getValue();
          if(selectedValue instanceof Integer) {
              int ans = ((Integer)selectedValue).intValue();
              if (ans == JOptionPane.YES_OPTION) {
                  deleteSelectedItems();
              }
          }
      }


   public class RenameAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public RenameAction(JProjectTree tree)
      {
         super("MBK_RENAME", tree, ProjectTreeResources.getString("JProjectTree.Rename_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            beginEditFirstSelected();
         }
      }

      public boolean isEnabled()
      {
         boolean retVal = false;
         //enable if it has model element and its not workspace node.
         IElement modEle = getFirstSelectedModelElement();
         IProjectTreeItem projItem = getFirstSelectedModelElementItem();
         if (getProjectModel().canEdit(projItem))
         {
            boolean bMember = isMemberOfDesignCenterProject(modEle);
            if (!bMember)
            {
               retVal = true;
            }
         }
         else
         {
            // See if we have a diagram
            String location = getFirstSelectedDiagram();
            if (location != null && location.length() > 0)
            {
               retVal = true;
            }
            else
            {
               // See if we have a project
               String projName = getFirstSelectedOpenProject();
               if (projName != null && projName.length() > 0)
               {
                  retVal = true;
               }
            }
         }
         return retVal;
      }
   }

   public class SaveAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public SaveAction(JProjectTree tree)
      {
         super("MBK_SAVE", tree, ProjectTreeResources.getString("JProjectTree.Save_Acion_Name")); //$NON-NLS-1$
      }

          /* (non-Javadoc)
           * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
           */
      public void actionPerformed(ActionEvent e)
      {
			ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
         try
         {
            ISwingProjectTreeModel model = getProjectModel();
            if (model != null)
            {
               // See if we have a workspace open, if so then just save that and be done with it!
               boolean spaceSel = atLeastOneOfTheseSelected(PTIK_WORKSPACE);
               if (spaceSel)
               {
                  IWorkspace space = getWorkspace();
                  if (space != null)
                  {
                     try
                     {
                        space.save();
                     }
                     catch (WorkspaceManagementException e1)
                     {
                        Log.stackTrace(e1);
                     }
                  }
               }
               else
               {
                  // Save the diagrams
                  saveSelectedDiagrams();

                  // Save the projects
                  saveSelectedProjectsInWorkspace();
               }
            }
         }
         finally
         {
				waitCursor.stop();
         }
      }

      public boolean isEnabled()
      {
         boolean retVal = false;
         //Enable only if the model element under it is dirty.
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            // Sensitive if we have diagrams or projects selected and open
            String diaName = getFirstSelectedOpenDiagram();
            String projName = getFirstSelectedOpenProject();
            if ( (diaName != null && diaName.length() > 0) ||
            (projName != null && projName.length() > 0 ) )
            {
               retVal = true;
            }
            else
            {
               // See if we have a workspace open
               retVal = atLeastOneOfTheseSelected(PTIK_WORKSPACE);
            }
         }
         return retVal;
      }
   }

   public class CloseAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public CloseAction(JProjectTree tree)
      {
         super("MBK_CLOSE", tree, ProjectTreeResources.getString("JProjectTree.Close_Acion_Name")); //$NON-NLS-1$
      }

          /* (non-Javadoc)
           * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
           */
      public void actionPerformed(ActionEvent e)
      {
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            // See if we have the workspace selected
            boolean spaceSel = atLeastOneOfTheseSelected(PTIK_WORKSPACE);
            if (spaceSel)
            {
               IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
               if (pUI != null)
               {				  
                  pUI.closeWorkspace();
               }
            }
            else
            {
               // Close the diagrams
               closeSelectedDiagrams();

               // Close the projects
               IWorkspace space = getWorkspace();
               if (space != null)
               {
				  closeSelectedProjects(space);				  
               }
            }
         }
      }

      public boolean isEnabled()
      {
         boolean retVal = false;
         //enable for open diagram, open project or open workspace.
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            String diaName = getFirstSelectedOpenDiagram();
            String projName = getFirstSelectedOpenProject();
            if ((diaName != null && diaName.length() > 0) ||
            (projName != null && projName.length() > 0))
            {
               retVal = true;
            }
            else
            {
               // See if we have the workspace selected
               boolean foundOne = atLeastOneOfTheseSelected(PTIK_WORKSPACE);
               if (foundOne)
               {
                  String mgrName = getConfigMgrName();
                  if (mgrName != null && mgrName.equals("DesignCenter")) //$NON-NLS-1$
                  {
                     // do not want to enable the close button for the workspace if the
                     // workspace is in the design center because the processing was
                     // actually closing the project workspace (current workspace)
                  }
                  else
                  {
                     retVal = true;
                  }
               }
            }
         }
         return retVal;
      }
   }

   public String getConfigMgrName()
   {
      return m_ConfigMgrName;
   }

   public void setConfigMgrName(String newVal)
   {
      m_ConfigMgrName = newVal;
   }

   /**
    * Gets the workspace from the product if we are in the project tree
    * or asks the user defined addin for its workspace if we are in the design center.
    *
    *
    * @param pWorkspace[out]		The workspace that the tree knows about
    *
    * @return HRESULT
    */
   private IWorkspace getWorkspace()
   {
      IWorkspace retSpace = null;
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         retSpace = model.getWorkspace();
      }
      return retSpace;
   }

   public class DivideProjectAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public DivideProjectAction(JProjectTree tree)
      {
         super("MBK_DIVIDE_PROJECT", tree, ProjectTreeResources.getString("JProjectTree.Divide_Project_Acion_Name")); //$NON-NLS-1$
      }

      public boolean isEnabled()
      {
         boolean retVal = false;
         IProject proj = getSelectedProject();
         if (proj != null)
         {
            boolean bMember = isMemberOfDesignCenterProject(proj);
            if (!bMember)
            {
               retVal = true;
            }
         }
         return retVal;
      }

        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
         */
      public void actionPerformed(ActionEvent e)
      {
         manageSelectedProject();
      }
   }

   /**
    *
    * Manages the selected Project in the tree. This looks for all Packages in the
    * selected Project, extracts them into their own .etx files, and saves the Project
    *
    * @return HRESULT
    *
    */
   private void manageSelectedProject()
   {
      ISwingProjectTreeModel model = getProjectModel();
      if (model != null)
      {
         IProject proj = getSelectedProject();
         if (proj != null)
         {
            ETList<IElement> elemsToExtract = retrieveExtractElements(proj);

            if (elemsToExtract != null)
            {
               extractElements(proj, elemsToExtract);
            }
         }
      }
   }

   /**
    *
    * Retrieves the elements that will be extracted when the user is managing the project
    *
    * @param pProj[in]              The project that the elements belong to.
    * @param elementsToExtract[out] The found elements
    *
    * @return HRESULT
    *
    */
   private ETList<IElement> retrieveExtractElements(IProject proj)
   {
      ETList<IElement> retObj = null;

      // We are only looking for Packages at this point.
      String query = "//UML:Package"; //$NON-NLS-1$
      retObj = (new ElementCollector<IElement>()).retrieveElementCollection((IElement)proj, query, IElement.class);

      return retObj;
   }

   /**
    *
    * Extracts the elements passed in, asking the user if they want to continue and showing progress
    * if needed.
    *
    * @param pProj[in]              The Project being managed
    * @param elementsToExtract[in]  The elements being extracted
    *
    * @return HRESULT
    *
    */
   private void extractElements(IProject proj, ETList<IElement> elems)
   {
      if (elems != null)
      {
         int count = elems.size();
         if (count > 0 && proceedWithExtract())
         {
            IProgressDialog progDialog = ProductHelper.getProgressDialog();
            progDialog.setCollapse(true);
            progDialog.setLimits(new ETPairT < Integer, Integer > (new Integer(0), new Integer(count+1)));

            String title = loadString("IDS_MANAGE_PROJECT_TITLE"); //$NON-NLS-1$
            if (title != null && title.length() > 0)
            {
               progDialog.setTitle(title);
               boolean status = progDialog.display(MessageDialogDisplayEnum.MMK_MODELESS);
               if (status)
               {
                  String saveProj = loadString("IDS_SAVING_PROJECT"); //$NON-NLS-1$
                  progDialog.setFieldOne(saveProj);

                  // Save the Project before going on, so we can rollback to the current state before proceeding.
                  proj.save(null, true);

                  // Now make sure the Project is dirty so that all gets saved appropriately
                  if (dirtyTheProject(proj))
                  {
                     int pos = progDialog.increment();
                     ITypeManager typeMan = proj.getTypeManager();
                     if (typeMan != null)
                     {
                        boolean isCancelled = false;

                        // Now block all events. This is really only needed to prevent
                        // the type manager from getting all the InitialExtracted events
                        try
                        {
                           EventBlocker.startBlocking();
                           String tempMessage = loadString("IDS_MANAGE_PROJECT_GROUP_MESSAGE"); //$NON-NLS-1$
                           String notNamed = loadString("IDS_NOT_NAMED"); //$NON-NLS-1$
                           String addingTypes = loadString("IDS_ADDING_TO_TYPE_FILE"); //$NON-NLS-1$
                           String numFileBuffer = String.valueOf(count);
                           boolean loadExternals = true;
                           for (int i=0; i<count && !isCancelled; i++)
                           {
                              isCancelled = progDialog.getIsCancelled();
                              if (!isCancelled)
                              {
                                 String groupMessage = tempMessage;
                                 String buffer = String.valueOf(i+1);
                                 groupMessage = StringUtilities.replaceSubString(groupMessage, "%1", buffer); //$NON-NLS-1$
                                 groupMessage = StringUtilities.replaceSubString(groupMessage, "%2", numFileBuffer); //$NON-NLS-1$
                                 progDialog.setGroupingTitle(groupMessage);

                                 IElement elem = elems.get(i);
                                 String extractMessage = loadString("IDS_MANAGE_PROJECT_EXTRACT_MESSAGE"); //$NON-NLS-1$
                                 String elemType = elem.getElementType();
                                 String name = null;
                                 if (elem instanceof INamedElement)
                                 {
                                    name = ((INamedElement)elem).getName();
                                 }

                                 if (elemType != null && name != null && elemType.length() > 0 && name.length() > 0)
                                 {
                                    extractMessage = StringUtilities.replaceSubString(extractMessage, "%1", elemType); //$NON-NLS-1$
                                    extractMessage = StringUtilities.replaceSubString(extractMessage, "%2", name); //$NON-NLS-1$

                                    progDialog.setFieldOne(extractMessage);

                                    // All that work, to just do this....
                                    proj.extractElement(elem);

                                    // And then add the type to the .ettm file
                                    progDialog.setFieldTwo(addingTypes);
                                    typeMan.addType(elem, false, loadExternals);

                                    // We only want to load external elements once
                                    loadExternals = false;
                                 }
                              }
                              progDialog.clearFields();
                              progDialog.increment();
                           }
                        }
                        finally
                        {
                           EventBlocker.stopBlocking(false);
                        }

                        progDialog.increment();
                        if (isCancelled)
                        {
                           // If we cancelled, close the project and reopen
                           IWSProject wsProj = proj.getWSProject();
                           if (wsProj != null)
                           {
                              try
                              {
                                 wsProj.close(false);
                                 wsProj.open();
                              }
                              catch (WorkspaceManagementException e)
                              {
                                 Log.stackTrace(e);
                              }
                           }
                        }
                     }
                  }
                  else
                  {
                     String unableToModify = loadString("IDS_UNABLE_TO_MODIFY_THE_PROJECT"); //$NON-NLS-1$
                     progDialog.setFieldOne(unableToModify);
                  }

                  String closeLabel = loadString("IDS_CLOSE_LABEL"); //$NON-NLS-1$
                  progDialog.setCollapse(false);
                  progDialog.promptForClosure(closeLabel, true);
               }
            }
         }
      }
   }

   private String loadString(String key)
   {
      return DefaultEngineResource.getString(key);
   }

   /**
    *
    * Queries the user to make sure that they want to proceed with the Management of
    * the project
    *
    *
    * @return true if they want to do it, else false
    *
    */
   private boolean proceedWithExtract()
   {
      boolean proceed = false;
      IQuestionDialog qDialog = new SwingQuestionDialogImpl();
      String message = loadString("IDS_MANAGE_PROJECT_MESSAGE"); //$NON-NLS-1$
      String title = loadString("IDS_MANAGE_PROJECT_TITLE"); //$NON-NLS-1$
      QuestionResponse result =
      qDialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO,
      MessageIconKindEnum.EDIK_ICONQUESTION,
      message,"", title,  //$NON-NLS-1$
      MessageResultKindEnum.SQDRK_RESULT_YES,
      false);
      if (result.getResult() == MessageResultKindEnum.SQDRK_RESULT_YES)
      {
         proceed = true;
      }
      return proceed;
   }

   /**
    *
    * Dirties the Project in a save way, making sure element modifies are
    * going out
    *
    * @param pProject[in]  The Project to dirty
    *
    * @return true if processing should proceed, else false
    *
    */
   private boolean dirtyTheProject(IProject proj)
   {
      boolean proceed = true;
      IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
      proceed = UMLXMLManip.fireElementPreModified(proj, helper);
      if (proceed)
      {
         helper.dispatchElementModified(proj);
      }
      return proceed;
   }

   private boolean isMemberOfDesignCenterProject(IElement elem)
   {
      boolean member = false;
      if (elem != null)
      {
         IProject proj = null;
         if (elem instanceof IProject)
         {
            proj = (IProject)elem;
         }
         else
         {
            proj = elem.getProject();
         }
         if (proj != null)
         {
            String fileName = proj.getFileName();
            if (fileName != null && fileName.length() > 0)
            {
               String ext = FileSysManip.getExtension(fileName);
               if (ext != null && ext.equals(FileExtensions.PATTERN_EXT_NODOT))
               {
                  member = true;
               }
            }
         }
      }
      return member;
   }

   public class ExpandAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public ExpandAction(JProjectTree tree)
      {
         super("MBK_EXPAND_ITEM", tree, ProjectTreeResources.getString("JProjectTree.Expand_All_Packages_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         performExpandAction();
      }

      public boolean isEnabled()
      {
         //enable only when package node is selected
         return true;
      }
   }

   private void performExpandAction()
   {
      IProjectTreeUpdateLocker locker = new ProjectTreeUpdateLocker();
      try
      {
         locker.lockTree(this);

         // get the projects that the tree knows about
         ETList<IProjectTreeItem> projItems = getProjects();
         if (projItems != null)
         {
            int count = projItems.size();
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem projItem = projItems.get(i);
               IElement pEle = projItem.getModelElement();
               if (pEle != null && pEle instanceof IProject)
               {
                  IProject pProj = (IProject)pEle;
                  String xmiid = pProj.getTopLevelId();
                  if (xmiid != null && xmiid.length() > 0)
                  {
                     ETList<IProjectTreeItem> projects = findNode(null, xmiid, xmiid);
                     if (projects != null)
                     {
                        int num = projects.size();
                        if (num > 0)
                        {
                           IProjectTreeItem item = projects.get(0);
                           int numCycles = 0;
                           boolean done = false;
                           while (!done)
                           {
                              // Get all the packages
                              ETList<IProjectTreeItem> childrenToExpand = addChildPackages(item, null);

                              //Now expand all the packages
                              if (childrenToExpand != null && childrenToExpand.size() > 0)
                              {
                                 // Go through all the packages and expand
                                 for (int j=0; j<childrenToExpand.size(); j++)
                                 {
                                    IProjectTreeItem pItem = childrenToExpand.get(j);
                                    ITreeItem treeItem = pItem.getProjectTreeSupportTreeItem();
                                    treeItem.setExpanded(true);
                                    TreePath path = new TreePath(treeItem.getPath());
                                    m_Tree.expandPath(path);
                                 }
                              }
                              else
                              {
                                 done = true;
                              }

                              numCycles++;
                              if (numCycles >= 100)
                              {
                                 // Something bad happened.  I doubt that we'd have 100 packages deep
                                 // projects.  If we get here we probably have a package that just won't
                                 // register as opened for some reason.
                                 done = true;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      finally
      {
         locker.unlockTree(this);
      }
   }

   /**
    * Adds child packages to the list that are not expanded.
    *
    * @param pProjectTree [in] The project tree which is a parent of this button handler
    * @param pParentItem [in] The parent to query
    * @param pChildrenToExpand [in] A returned list of all the package project tree items which are not yet
    * expanded.
    */
   private ETList<IProjectTreeItem> addChildPackages(IProjectTreeItem parent,
   ETList<IProjectTreeItem> childrenToExpand)
   {
      if (childrenToExpand == null)
      {
         childrenToExpand = new ETArrayList<IProjectTreeItem>();
      }
      ETList<IProjectTreeItem> children = null;
      children = getChildren(parent);

      // Get all the packages
      if (children != null)
      {
         int count = children.size();
         for (int i=0; i<count; i++)
         {
            IProjectTreeItem item = children.get(i);
            IElement elem = item.getModelElement();
            if (elem != null)
            {
               String elemType = elem.getElementType();
               if (elemType != null && elemType.equals("Package")) //$NON-NLS-1$
               {
                  boolean isExpanded = getIsExpanded(item);
                  if (!isExpanded)
                  {
                     childrenToExpand.add(item);
                  }
                  else
                  {
                     // To into the children
                     childrenToExpand = addChildPackages(item, childrenToExpand);
                  }
               }
            }
         }
      }
      return childrenToExpand;
   }

   public class RemoveProjectAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public RemoveProjectAction(JProjectTree tree)
      {
         super("MBK_REMOVE_PROJECT", tree, ProjectTreeResources.getString("JProjectTree.Remove_Project_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         removeSelectedProjectsFromWorkspace();
         IWorkspace space = getWorkspace();
         if (space != null)
         {
            space.setIsDirty(true);
         }
      }

      public boolean isEnabled()
      {
         // See if we have a diagram selected
         boolean found = false;
         found = atLeastOneOfTheseSelected(PTIK_PROJECT);
         return found;
      }
   }

   /**
    * Removes the selected projects from the workspace.
    */
   private void removeSelectedProjectsFromWorkspace()
   {
      IWorkspace space = getWorkspace();
      if (space != null)
      {
         // Close the diagrams
         closeSelectedProjects(space);
         IProjectTreeItem[] items = getSelected();
         if (items != null)
         {
            int count = items.length;
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = items[i];
               boolean isProject = item.isProject();
               IProject proj = item.getProject();

               // Make sure it's closed.  If pProject != 0 then the user hit cancel.
               if (isProject && proj == null)
               {
                  String name = item.getItemText();
                  if (name != null && name.length() > 0)
                  {
                     try
                     {
                        space.removeWSProjectByName(name);
                        space.setIsDirty(true);
                     }
                     catch (WorkspaceManagementException e)
                     {
                        Log.stackTrace(e);
                     }
                  }
               }
            }
         }
      }
   }

   public class InsertProjectAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public InsertProjectAction(JProjectTree tree)
      {
         super("MBK_INSERT_PROJECT", tree, ProjectTreeResources.getString("JProjectTree.Insert_Project_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         IProductProjectManager projMgr = ProductHelper.getProductProjectManager();
         if (projMgr != null)
         {
            IWorkspace space = getWorkspace();
            if (space != null)
            {
               projMgr.displayInsertProjectDialog(space);
               space.setIsDirty(true);			  
               refresh(true);
            }
         }
      }

      public boolean isEnabled()
      {
         // Enabled if a workspace is opened
         return getWorkspaceIsOpened();
      }

   }

   /**
    * Returns true if a workspace is opened.
    *
    * @return true if a workspace is open.
    */
   private boolean getWorkspaceIsOpened()
   {
      boolean retVal = false;
      IWorkspace space = getWorkspace();
      if (space != null)
      {
         retVal = space.isOpen();
      }
      return retVal;
   }

   public class OpenWorkspaceAction extends ProjectTreeAction
   {
      JProjectTree m_Tree = null;

      public OpenWorkspaceAction(JProjectTree tree)
      {
         super("MBK_OPEN_WORKSPACE", tree, ProjectTreeResources.getString("JProjectTree.OpenWorkspace_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
         if (pUI != null)
         {
            pUI.openWorkspaceDialog();
         }
      }

      public boolean isEnabled()
      {
         boolean bEnable = true;
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            String modelName = model.getModelName();
            
            // do not enable if in design center
            //if (model instanceof DesignCenterSwingModel)
            if(modelName.equals("DesignCenter") == true)
            {
               bEnable = false;
            }
         }
         return bEnable;
      }
   }

   public class OpenProjectAction extends ProjectTreeAction
   {
      public OpenProjectAction(JProjectTree tree)
      {
         super("MBK_OPEN_PROJECT", tree, ProjectTreeResources.getString("JProjectTree.OpenProject_Acion_Name")); //$NON-NLS-1$

         setEnabled(getIsClosedProjectSelected());
      }

      public boolean getIsClosedProjectSelected()
      {
         boolean retVal = false;

         JProjectTree tree = getProjectTree();

         if(tree != null)
         {
            String location = tree.getFirstSelectedClosedProject();
            if(location.length() > 0)
            {
               retVal = true;
            }
         }

         return retVal;
      }

      public boolean isEnabled()
      {
         // Enable if we have a closed project selected
         return getIsClosedProjectSelected();
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            IWorkspace space = getWorkspace();
            if (space != null)
            {
               openSelectedProjects(space);
            }
         }
      }

   }

   /**
    * Opens the selected projects.
    */
   public void openSelectedProjects(IWorkspace space)
   {
      actOnSelectedProjects(SA_OPEN, space);
   }

   /**
    * Opens or closes the selected projects based on the flag.
    *
    * @param nAction [in] The action to perform
    */
   private void actOnSelectedProjects(int nAction, IWorkspace pWorkspace)
   {
      IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
      IProductDiagramManager pDiaMgr = ProductHelper.getProductDiagramManager();
      if (pUI != null && pDiaMgr != null)
      {
         IApplication app = ProductHelper.getApplication();
         IProjectTreeItem[] items = getSelected();
         if (items != null)
         {
            int count = items.length;
            for (int i=0; i<count; i++)
            {
               IProjectTreeItem item = items[i];
               boolean isProject = item.isProject();
               IProject proj = item.getProject();
               if (nAction == SA_OPEN)
               {
                  if (isProject && proj == null)
                  {
                     String projName = item.getItemText();
                     if (app != null)
                     {
                        // Open this project
                        if (pWorkspace == null)
                        {
                           pWorkspace = ProductHelper.getWorkspace();
                        }
                        app.openProject(pWorkspace, projName);
                     }
                  }
               }
               else if (nAction == SA_CLOSE)
               {
                  if (proj != null)
                  {
                     getProjectModel().closeProject(proj);
                  }
               }
               else if (nAction == SA_SAVE)
               {
                  if (proj != null)
                  {
                     IWSProject wsProj = proj.getWSProject();
                     if (wsProj != null)
                     {
                        try
                        {
                           wsProj.save();
                        }
                        catch (WorkspaceManagementException e)
                        {
                           Log.stackTrace(e);
                        }
                     }
                  }
               }

               if(item.isProject() == true)
               {
                  TreePath path = new TreePath(item.getPath());
                  ITreeItem treeItem = getProjectModel().getTreeItem(path);
                  if(treeItem != null)
                  {
                     treeItem.setIsInitalized(false);
                     treeItem.removeAllChildren();

                     ETList < ITreeItem > changedItems = new ETArrayList();
                     changedItems.add(treeItem);
                     getProjectModel().notifyOfStructureChange(changedItems);
                  }
               }
               if (nAction == SA_OPEN)
               {
                   m_Tree.expandPath(new TreePath(item.getPath()));
               }
            }
         }
      }
   }

   public class OpenDiagramAction extends ProjectTreeAction
   {
      public OpenDiagramAction(JProjectTree tree)
      {
         super("MBK_OPEN_DIAGRAM", tree, ProjectTreeResources.getString("JProjectTree.OpenDiagram_Acion_Name")); //$NON-NLS-1$


      }

      public boolean isEnabled()
      {
         boolean isEnabled = false;

         JProjectTree tree = getProjectTree();
         if(tree != null)
         {
            String selDiagram = tree.getFirstSelectedClosedDiagram();
            if (selDiagram != null && selDiagram.length() > 0)
            {
               isEnabled = true;
            }
         }
         return isEnabled;
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            openSelectedDiagrams();
         }
      }
   }

   public class NewWorkspaceAction extends ProjectTreeAction
   {
      public NewWorkspaceAction(JProjectTree tree)
      {
         super("MBK_NEW_WORKSPACE", tree, ProjectTreeResources.getString("JProjectTree.NewWorkspace_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
         if (pUI != null)
         {
            pUI.newWorkspaceDialog();
         }
      }

      public boolean isEnabled()
      {
         boolean bEnable = true;
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            String modelName = model.getModelName();
            
            // do not enable if in design center
            //if (model instanceof DesignCenterSwingModel)
            if(modelName.equals("DesignCenter") == true)
            {
               bEnable = false;
            }
         }
         return bEnable;
      }
   }

   public class NewProjectAction extends ProjectTreeAction
   {
      public NewProjectAction(JProjectTree tree)
      {
         super("MBK_NEW_PROJECT", tree, ProjectTreeResources.getString("JProjectTree.NewProject_Acion_Name")); //$NON-NLS-1$
      }

      public boolean isEnabled()
      {
         // Enabled if a workspace is opened
         boolean bEnable = true;
         ISwingProjectTreeModel model = getProjectModel();
         if (model != null)
         {
            String modelName = model.getModelName();            
            
            // only enable in design center if on a workspace
            //if (model instanceof DesignCenterSwingModel)
            if(modelName.equals("DesignCenter") == true)
            {
               bEnable = atLeastOneOfTheseSelected(PTIK_WORKSPACE);
            }
            else
            {
               bEnable = getWorkspaceIsOpened();
            }
         }
         return bEnable;
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         IProductProjectManager projMgr = ProductHelper.getProductProjectManager();
         if (projMgr != null)
         {
            projMgr.displayNewProjectDialog();
            IWorkspace space = getWorkspace();
            if (space != null)
            {
               space.setIsDirty(true);
			   //Smitha - Fix for bug # 6289779 
			   space.save();			   			   
            }
         }
      }
   }

   public class NewDiagramAction extends ProjectTreeAction
   {
      public NewDiagramAction(JProjectTree tree)
      {
         super("MBK_NEW_DIAGRAM",tree, ProjectTreeResources.getString("JProjectTree.NewDiagram_Acion_Name")); //$NON-NLS-1$
      }

      public boolean isEnabled()
      {
         // Valid only if a workspace and a project is opened
         boolean retVal = false;

         JProjectTree tree = getProjectTree();
         IElement modEle = getFirstSelectedModelElement();
         if(tree != null && modEle instanceof INamespace)
         {
            IProjectTreeItem  item = tree.getFirstSelectedModelElementItem();

            if(item != null)
            {
               IProject project = retrieveProjectFromItem(item);
               if(project != null)
               {
                  boolean bMember = isMemberOfDesignCenterProject(project);
                  if (!bMember)
                  {
                     retVal = true;
                  }
               }
            }
         }
         return retVal;
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
          IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
          if (diaMgr != null)
          {
              IElement modEle = getFirstSelectedModelElement();
              if (modEle != null && modEle instanceof INamespace)
              {
                  IDiagram newDiagram = diaMgr.newDiagramDialog((INamespace)modEle,
                          IDiagramKind.DK_UNKNOWN,
                          IDiagramKind.DK_ALL, null);
                  
                  // Fixed issue 95782. When a diagram is 1st created, its dirty state is false.
                  // Set the dirty state to true to have the diagram autosaved.
                  if (newDiagram != null )
                  {
                      newDiagram.setDirty(true);
                      try
                      {
                          newDiagram.save();
                      } catch (IOException ioe)
                      {
                          Exceptions.printStackTrace(ioe);
                      }
                  }
              }
          }
      }
   }

   public class NewElementAction extends ProjectTreeAction
   {
      public NewElementAction(JProjectTree tree)
      {
         this(tree, ProjectTreeResources.getString("JProjectTree.NewElement_Acion_Name")); //$NON-NLS-1$
      }

      public NewElementAction(JProjectTree tree, String name)
      {
         super("MBK_NEW_ELEMENT", tree, name);
      }

      public NewElementAction(String id, JProjectTree tree, String name)
      {
         super(id, tree, name);
      }

      public boolean isEnabled()
      {
         return enableElementOrPackage();
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         INamespace space = getNamespaceForElementOrPackage();
         IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
         if (pUI != null && space != null)
         {
            pUI.newElementDialog(space);			
         }
      }
   }

   public class NewPackageAction extends NewElementAction
   {
      public NewPackageAction(JProjectTree tree)
      {
         super("MBK_NEW_PACKAGE", tree, ProjectTreeResources.getString("JProjectTree.NewPackage_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         INamespace space = getNamespaceForElementOrPackage();
         IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
         if (pUI != null && space != null)
         {
            pUI.newPackageDialog(space);
         }
      }

      public boolean isEnabled()
      {
         return enableElementOrPackage();
      }
   }

   private INamespace getNamespaceForElementOrPackage()
   {
      INamespace space = null;
      IElement modEle = getFirstSelectedModelElement();
      if (modEle != null)
      {
         if (modEle instanceof INamespace)
         {
            space = (INamespace)modEle;
         }
         else if (modEle instanceof INamedElement)
         {
            //get the parent namespace
            space = ((INamedElement)modEle).getNamespace();
         }
      }
      else
      {
         String diaName = getFirstSelectedDiagram();
         if (diaName != null && diaName.length() > 0)
         {
            // See if we have a diagram with a namespace
            IProxyDiagramManager pMgr = ProxyDiagramManager.instance();
            IProxyDiagram pDia = pMgr.getDiagram(diaName);
            if (pDia != null)
            {
               space = pDia.getNamespace();
            }
         }
      }
      return space;
   }

   private boolean enableElementOrPackage()
   {
      boolean retVal = false;
      // Valid only if we have a namespace
      IElement modEle = getFirstSelectedModelElement();
      String firstDia = getFirstSelectedDiagram();
      INamespace space = null;
      if (modEle != null)
      {
         if (modEle instanceof INamespace)
         {
            space = (INamespace)modEle;
         }
         else if (modEle instanceof INamedElement)
         {
            // Get the parent namespace
            space = ((INamedElement)modEle).getNamespace();
         }
      }
      else if (firstDia != null && firstDia.length() > 0)
      {
         // See if we have a diagram with a namespace
         IProxyDiagramManager pMgr = ProxyDiagramManager.instance();
         IProxyDiagram proxyDia = pMgr.getDiagram(firstDia);
         if (proxyDia != null)
         {
            space = proxyDia.getNamespace();
         }
      }

      if (space != null)
      {
         boolean bMember = isMemberOfDesignCenterProject(space);
         if (!bMember)
         {
            retVal = true;
         }
      }
      return retVal;
   }

   public class NewAttributeAction extends ProjectTreeAction
   {
      public NewAttributeAction(JProjectTree tree)
      {
         super("MBK_NEW_ATTRIBUTE", tree, ProjectTreeResources.getString("JProjectTree.NewAttribute_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         try
         {
            IElement modEle = getFirstSelectedModelElement();
            if (modEle != null && modEle instanceof IClassifier)
            {
               IClassifier pClass = (IClassifier)modEle;
               IAttribute pAttr = pClass.createAttribute3();
               pClass.addAttribute(pAttr);

               // Now expand the element if it's in the tree
               ISwingProjectTreeModel model = getProjectModel();
               if (model != null)
               {
                  IProjectTreeItem item = getFirstSelectedModelElementItem();
                  if (item != null)
                  {
                     ITreeItem treeItem = item.getProjectTreeSupportTreeItem();
                     TreePath path = new TreePath(treeItem.getPath());
                     m_Tree.expandPath(path);
                     //treeItem.setExpanded(true);
                  }
                  ETList<IProjectTreeItem> pTreeItems = findNode2(pAttr);
                  if (pTreeItems != null)
                  {
                     int count = pTreeItems.size();
                     if (count > 0)
                     {
                        IProjectTreeItem pAttrItem = pTreeItems.get(0);						
                        beginEditThisItem(pAttrItem);
                     }
                  }
               }
            }
         }
         catch (Exception ex)
         {
         }
      }

      public boolean isEnabled()
      {
         return enableAttributeOrOperation();
      }
   }

   public class NewOperationAction extends ProjectTreeAction
   {
      public NewOperationAction(JProjectTree tree)
      {
         super("MBK_NEW_OPERATION", tree, ProjectTreeResources.getString("JProjectTree.NewOperation_Acion_Name")); //$NON-NLS-1$
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
       */
      public void actionPerformed(ActionEvent e)
      {
         try
         {
            IElement modEle = getFirstSelectedModelElement();
            if (modEle != null && modEle instanceof IClassifier)
            {
               IClassifier pClass = (IClassifier)modEle;
               IOperation pOper = pClass.createOperation3();
               pClass.addOperation(pOper);

               // Now expand the element if it's in the tree
               ISwingProjectTreeModel model = getProjectModel();
               if (model != null)
               {
                  IProjectTreeItem item = getFirstSelectedModelElementItem();
                  if (item != null)
                  {
                     ITreeItem treeItem = item.getProjectTreeSupportTreeItem();
                     if (treeItem != null)
                     {
                        TreePath path = new TreePath(treeItem.getPath());
                        m_Tree.expandPath(path);
                        //treeItem.setExpanded(true);
                     }
                  }
                  ETList<IProjectTreeItem> pTreeItems = findNode2(pOper);
                  if (pTreeItems != null)
                  {
                     int count = pTreeItems.size();
                     if (count > 0)
                     {
                        IProjectTreeItem pOperItem = pTreeItems.get(0);
                        beginEditThisItem(pOperItem);
                     }
                  }
               }
            }
         }
         catch (Exception ex)
         {
         }
      }

      public boolean isEnabled()
      {
         return enableAttributeOrOperation();
      }
   }

   private boolean enableAttributeOrOperation()
   {
      boolean retVal = false;
      // If > 1 classifier is selected then disable the menu
      IProjectTreeItem[] items = getSelected();
      if (items != null)
      {
         int count = items.length;
         int numClassifiers = 0;
         for (int i=0; i<count; i++)
         {
            IProjectTreeItem item = items[i];
            IElement modEle = item.getModelElement();
            if (modEle != null && modEle instanceof IClassifier)
            {
               boolean bMember = isMemberOfDesignCenterProject((IClassifier)modEle);
               if (!bMember)
               {
                  numClassifiers++;
                  if (numClassifiers == 2)
                  {
                     break;
                  }
               }
            }
         }
         if (numClassifiers == 1)
         {
            retVal = true;
         }
      }
      //enable if its INamespace (not project or workspace).
      return retVal;
   }

   /**
    * Resets the state of the drag operation.  (Package Projected).
    */
   void resetDragState()
   {
      m_InDragProcess = false;
   }

   // Accelerator handler
	protected void onAcceleratorAction(String accelerator)
	{
		if (accelerator.equals(ProjectTreeResources.getString("IDS_CTRLD")))
		{
			NewDiagramAction action = new NewDiagramAction(this);
			if (action.isEnabled())
			{
				action.actionPerformed(null);
			}
		}
		else if (accelerator.equals(ProjectTreeResources.getString("IDS_CTRLK")))
		{
			NewPackageAction action = new NewPackageAction(this);
			if (action.isEnabled())
			{
				action.actionPerformed(null);
			}
		}
		else if (accelerator.equals(ProjectTreeResources.getString("IDS_CTRLE")))
		{
			NewElementAction action = new NewElementAction(this);
			if (action.isEnabled())
			{
				action.actionPerformed(null);
			}
		}
//		else if (accelerator.equals(ProjectTreeResources.getString("IDS_CTRLB")))
//		{
//			NewAttributeAction action = new NewAttributeAction(this);
//			if (action.isEnabled())
//			{
//				action.actionPerformed(null);
//			}
//		}
//		else if (accelerator.equals(ProjectTreeResources.getString("IDS_CTRLT")))
//		{
//			NewOperationAction action = new NewOperationAction(this);
//			if (action.isEnabled())
//			{
//				action.actionPerformed(null);
//			}
//		}
	};
}



