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

package org.netbeans.modules.uml.designpattern;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTree;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterManager;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterSupport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.project.ui.nodes.UMLDiagramNode;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelElementNode;
import org.netbeans.modules.uml.project.ui.nodes.UMLRequirementNode;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.projecttree.ISwingProjectTreeModel;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;

/**
 * The DesignCenterComponent is a component that can be embedded into NetBeans
 * (FFJ) window system.  DesignCenterComponent will display a Describe system
 * tree control inside NetBeans (FFJ).
 * @author  Trey Spiva
 * @version 1.0
 */
public class DesignCenterComponent extends TopComponent {
    private static final String TREE_TYPE_ID = "DesignCenter";
    private static ResourceBundle mBundle = ResourceBundle.getBundle("org.netbeans.modules.uml.designpattern.Bundle");
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8400374912390440402L;

    private static int instanceCount = 0;
    private static    JProjectTree mDesignCenter = null;
    private static    ADDesignCenterEngine dsEngine;
    private ISwingProjectTreeModel m_Model = null;
    transient private boolean added = false;

    /**
     *  The Set of Forte workspaces in which a project tree has been opened.
     */
    private static HashSet workspaces = new HashSet();

    private static DesignCenterComponent lastComponent = null;

    /**
     * Creates  a DesignCenterComponent component.
     */
    public DesignCenterComponent() {
        super();
        setName(mBundle.getString("Pane.DesignCenter.Title"));
        initializeTopComponent();
    }

    public void initializeTopComponent() {
        if (mDesignCenter == null) {
            mDesignCenter = new DesignCenterTree();
            initializeDesignCenter(mDesignCenter);
            
            setLayout(new BorderLayout());
            add(mDesignCenter.getView(), BorderLayout.CENTER);
            mDesignCenter.addTreeFocusListener(new DCFocusListerner());
            //setVisible(true);
            //doLayout();
            
            InputMap inputMap = mDesignCenter.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put(KeyStroke.getKeyStroke("shift F10"),
                    "SHOW_CONTEXT_MENU");
            
            inputMap.put(KeyStroke.getKeyStroke("DELETE"),
                    "DELETE_SELECTED_ELEMENT");
            
            mDesignCenter.getActionMap().put("SHOW_CONTEXT_MENU", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Component c = (Component)e.getSource() ;
                    
                    //need to use the popup thing inthe ApplicaitonView
                    mDesignCenter.getPopupMenuManager(mDesignCenter).showPopupMenu(c) ;  //.showAccessiblePopupMenu();
                }
            });
            
            mDesignCenter.getActionMap().put("DELETE_SELECTED_ELEMENT", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    
                    mDesignCenter.deleteSelectedItems() ;
                }
            });
            
        }
    }

    public void requestActive() {
        super.requestActive();
        if ( mDesignCenter != null ) {
          mDesignCenter.requestTreeFocus();
       }
    }
    
   public void processIDERemoves(IMenuManager manager)
   {
      //remove the insert and remove project menu items.
      //manager.remove("org.netbeans.modules.uml.view.projecttree.remove.popup");
      //manager.remove("org.netbeans.modules.uml.view.projecttree.insert.popup");

      //remove new project and new workspace menu item
      Object item = manager.find("org.netbeans.modules.uml.view.projecttree.insert.new");
      if (item != null && item instanceof IMenuManager)
      {
         ((IMenuManager)item).remove("MBK_NEW_WORKSPACE");
      }

      //remove open project and open workspace menu item
      Object openItem = manager.find("org.netbeans.modules.uml.view.projecttree.insert.open");
      if (openItem != null && openItem instanceof IMenuManager)
      {
         ((IMenuManager)openItem).remove("MBK_OPEN_WORKSPACE");
      }
   }


    public JProjectTree getControl() {
        return mDesignCenter;
    }

    public void setHotwired(boolean hotwired) {
    }

    public static DesignCenterComponent getLastComponent() {
        return lastComponent;
    }

    /**
     *  Enables or disables this component, depending on the value of the
     * parameter b. An enabled component can respond to user input and generate
     * events.  Components are enabled initially by default.  The
     * DesignCenterComponent can only be enabled if the system tree component
     * has been initialized.
     */
    public void setEnabled(boolean enabled) {
        if(mDesignCenter != null)
            mDesignCenter.setEnabled(enabled);
    }

    /**
     * Determines whether this component is enabled. An enabled component can
     * respond to user input and generate events. The component is only enabled
     * if the system tree has been initialized.
     */
    public boolean isEnabled() {

        boolean retVal = false;
        if(mDesignCenter != null) {
            retVal = true;
        }

        return retVal;
    }

    /**
     * Refreshes the system tree.  The system tree will read Describe and update
     * its contents.
     */
    public void refresh()
    {
       try {
           if(mDesignCenter != null)
           {
               mDesignCenter.refresh(false);
           }
       } catch(Exception E) {
           String msg = "An error occured while trying refresh the " +
                        "design center.";
           javax.swing.JOptionPane.showMessageDialog(null, "Exception: " + E.getMessage());
//           ExceptionDialog.showExceptionError(msg, E);
       }
    }

    public void killControl() {
    }

    public static DesignCenterComponent createDesignCenter() {
        return new DesignCenterComponent();
    }

    public boolean isAdded() {
        return added;
    }

    /**
     * Makes this Component displayable by connecting it to a native screen
     * resource. This method is called internally by the toolkit and should not
     * be called directly by programs.
     */
    public void addNotify() {
        try {
            super.addNotify();
        } catch (Exception e) {
        }
    }
    private void initializeDesignCenter(JProjectTree dc)
    {
       if (m_Model == null)
       {
          IADProduct prod = (IADProduct) ProductHelper.getProduct();

          if (prod != null)
          {
            prod.setDesignCenterTree(dc);
            m_Model = new DesignCenterSwingModel(prod);
            dc.setModel(m_Model);
          }
       }
    }

    /**
     * Makes this Component undisplayable by destroying it native screen
     * resource.  This method is called by the toolkit internally and should not
     * be called directly by programs.
     */
    public void removeNotify() {
        super.removeNotify();
    }

    public static int getInstanceCount() {
        return instanceCount;
    }

   public static synchronized DesignCenterComponent getDefault()
   {
      if (lastComponent == null)
      {
         lastComponent = new DesignCenterComponent(); 
      }
      return lastComponent;
   }

   public static synchronized DesignCenterComponent getInstance()
   {
      if(lastComponent == null)
      {
          TopComponent tc = null;
          try {
              tc = WindowManager.getDefault().findTopComponent("designpattern");
          } catch(Exception ex) {
              //ignore this
          }

         if (tc != null)
         {
            lastComponent = (DesignCenterComponent)tc;            
         }
         else
         {
            lastComponent = new DesignCenterComponent();
         }
      }
      return lastComponent;
   }
   
   public int getPersistenceType()
   {
      return TopComponent.PERSISTENCE_ALWAYS;
   }

   public String preferredID()
   {
      return getClass().getName();
   }

   public HelpCtx getHelpCtx()
   {
       return new HelpCtx("DEToolsDesignCenter2_htm_wp1737211");
   }

    protected void componentClosed()
    {
        super.componentClosed();
        
        
    }

    public void writeExternal(java.io.ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        
        ICoreProduct product = ProductHelper.getCoreProduct();
        IDesignCenterManager manager = product.getDesignCenterManager();
        if(manager != null)
        {
            IDesignCenterSupport[] addins = manager.getAddIns();
            if(addins != null)
            {
                for(IDesignCenterSupport curAddin : addins)
                {
                    if(curAddin instanceof IDesignCenterSupport)
                    {
                        IDesignCenterSupport support = (IDesignCenterSupport)curAddin;
                        support.save();
                    }
                }
            }
        }
    }
   
    class DCFocusListerner implements FocusListener 
   {
        public void focusGained(FocusEvent e) 
        {
            Component comp = e.getComponent();
            if (comp instanceof JTree) 
            {
                JTree tree = (JTree) comp;
                int[] selectedRows = tree.getSelectionRows();
                if (selectedRows == null || selectedRows.length == 0) {
                    // if no row selected previously, select the 1st row.
                    selectedRows = new int[1];
                    selectedRows[0] = 0;
                    tree.setSelectionRows(selectedRows);
                }
            }
        }

        public void focusLost(FocusEvent e) 
        {
        }
   }
    
   public class DesignCenterTree extends JProjectTree
   {
       public void refresh(boolean bPostEvent){
           if(m_Model.getRootItem().getChildCount() > 0){
               super.refresh(bPostEvent);
           }
       }
       
      public void fireSelectionChange()
      {
          super.fireSelectionChange();
          
          IProjectTreeItem[] items = getSelected();
          
          //UMLModelElementNode[] nodes = new UMLModelElementNode[items.length];
          ArrayList < Node > nodeList = new ArrayList < Node >();
          if (items != null) {
              for(IProjectTreeItem item : items)
              {
                  IElement element = item.getModelElement();
                  String desc = item.getDescription();
                  if(element != null)
                  {
                      UMLModelElementNode node = new UMLModelElementNode();
                      node.setElement(element);
                      String nodeName = element instanceof INamedElement ? ((INamedElement) element).getName() : element.getElementType();
                      node.setName(nodeName);
                      nodeList.add(node);
                  }
                  else if(item.getDiagram() != null)
                  {
                      UMLDiagramNode node = new UMLDiagramNode(item.getDiagram());
                      nodeList.add(node);
                  }
		  else if((item.getData() != null) && (item.getData() instanceof IRequirement))
                  {		      
                      UMLRequirementNode node = new UMLRequirementNode(item);
		      node.setRequirement((IRequirement)item.getData());
                      nodeList.add(node);
		  }
              }
          }
          
          Node[] nodes = new Node[nodeList.size()];
          nodeList.toArray(nodes);
          setActivatedNodes(nodes);
      }
      
      protected void getModuleMenuItems(IMenuManager mgr)
      {
          mgr.add(new Separator());
          
          Action[] actions = getActionsFromRegistry("contextmenu/uml/designcenter");
          
          for(Action curAction : actions)
          {
              if (curAction == null)
              {
                  // Make Sure the Seperators are kept.
                  mgr.add(new Separator());
              }              
              else if (curAction.isEnabled())
              {
                  mgr.add(new BaseActionWrapper(curAction));
              }
          }
      }
      
      protected void getSelectedItemActions(IMenuManager mgr)
      {
          IProjectTreeItem[] items = getSelected();
          if((items != null) && (items.length == 1))
          {
              Action[] actions = items[0].getActions();
              if(actions != null)
              {
                  for(Action curAction : actions)
                  {
                      if (curAction == null)
                      {
                          // Make Sure the Seperators are kept.
                          mgr.add(new Separator());
                      }                      
                      else if (curAction.isEnabled())
                      {
                          mgr.add(new BaseActionWrapper(curAction));
                      }
                  }
              }
          }
      }
      
      
      /**
       * The registry information that is retrieved from layer files to build
       * the list of actions supported by this node.
       *
       * @param path The registry path that is used for the lookup.
       * @return The list of actions in the path.  null will be used if when
       *         seperators can be placed.
       */
      protected Action[] getActionsFromRegistry(String path)
      {
          ArrayList<Action> actions = new ArrayList<Action>();
          FileSystem system = Repository.getDefault().getDefaultFileSystem();
          
          try
          {
              if (system != null)
              {
                  FileObject lookupDir = system.findResource(path);
                  
                  if (lookupDir != null)
                  {
                      FileObject[] children = lookupDir.getChildren();
                      
                      for (FileObject curObj : children)
                      {
                          try
                          {
                              DataObject dObj = DataObject.find(curObj);
                              
                              if (dObj != null)
                              {
                                  InstanceCookie cookie = (InstanceCookie)dObj
                                          .getCookie(InstanceCookie.class);
                                  
                                  if (cookie != null)
                                  {
                                      Object obj = cookie.instanceCreate();
                                      
                                      if (obj instanceof Action)
                                      {
                                          actions.add((Action)obj);
                                      }
                                      else if (obj instanceof JSeparator)
                                      {
                                          actions.add(null);
                                      }
                                  }
                              } // dObj != null
                          }
                          
                          catch(ClassNotFoundException e)
                          {
                              // Unable to create the instance for some reason.  So the
                              // do not worry about adding the instance to the list.
                          }
                      } // for-each FileObject
                  } // if lookupDir != null
              } // if system != null
          }
          
          catch(DataObjectNotFoundException e)
          {
              // Basically Bail at this time.
          }
          
          catch(IOException ioE)
          {
              
          }
          
          Action[] retVal = new Action[actions.size()];
          actions.toArray(retVal);
          return retVal;
      }
   }
   
   public Image getIcon()
	{
		return Utilities.loadImage(
			"org/netbeans/modules/uml/resources/designcenter.gif"); // NOI18N
	}
   
//   public class StandardDesignCenterNode extends AbstractNode
//   {
//       public StandardDesignCenterNode(String desc)
//       {
//           
//       }
//   }
//   
//   public class DesignCenterNodeCookie implements Node.Cookie
//   {
//       private String mDescription = "";
//       public DesignCenterNodeCookie(String desc)
//       {
//           mDescription = desc;
//       }
//       
//       public String getDescription()
//   }
}
