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

package org.netbeans.modules.uml.documentation.ui;

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/**
 *  TopComponent for the Describe 6.0 documentation (JavaDoc) editor.
 *
 * @author  Darshan
 * @version 1.0
 */
public class DocumentationTopComponnet extends TopComponent implements PropertyChangeListener
{
   private PropertyChangeListener mActivatedNodeListener = null;;
   /**
    *  Serialization ID used by NetBeans. Note that this is faked, not
    * generated.
    */
   static final long serialVersionUID = 17754071377356384L;
   private static DocumentationTopComponnet mTopComponent = null;
   
   /**
    *  The Describe documentation editor ActiveX control wrapper class.
    */
   private DocumentationPane pane;
   
   /**
    *  Creates a documentation editor top component; the document editor
    * control is instantiated on addNotify() and destroyed on removeNotify().
    */
   public DocumentationTopComponnet() {
       setLayout(new BorderLayout());
       setName(NbBundle.getMessage(DocumentationTopComponnet.class, "Pane.Documentation.Title"));
       String desc = NbBundle.getMessage(DocumentationTopComponnet.class, "ACDS_DOCUMENTATION");
       getAccessibleContext().setAccessibleDescription(desc);
        
       initializeTopComponent();
       
       mActivatedNodeListener = this;
       TopComponent.getRegistry().addPropertyChangeListener(
                org.openide.util.WeakListeners.propertyChange(mActivatedNodeListener, TopComponent.getRegistry())
                );
   }
   
   public void initializeTopComponent()
   {
      if (pane == null)
      {
         pane = new DocumentationPane(getName());
         
         pane.addKeyListener(
         new KeyAdapter()
         {
            public void keyPressed(KeyEvent e)
            {
               if(e.getKeyCode() == KeyEvent.VK_TAB ||
               e.getKeyCode() == KeyEvent.VK_DELETE)
               {
                  e.consume();
               }
               else
               {
                  WindowManager.getDefault().getMainWindow().dispatchEvent(e);
               }
            }
         }
         );
      
         setLayout(new BorderLayout());
         add(pane, BorderLayout.CENTER);
         doLayout();
      }
   }
   
   protected void componentActivated() {
       super.componentActivated();
       pane.startEdit();
   }
   
   public boolean canClose()
   {
       pane.setElementDescription();
       return true;
   }

   
   /**
    *  Called when NetBeans places this TopComponent in the UI, not necessarily
    * making it visible. This is where we must create and display the property
    * editor - creating it before display is not suitable since Describe's
    * ActiveX controls don't like not having a window handle to paint to.
    */
   public void addNotify()
   {
      super.addNotify();
      //postNotify();
   }
   
   public void postNotify()
   {
//      if(ProjectController.isConnected())
//      {
//         try
//         {
            if (pane == null)
            {
               pane = new DocumentationPane()
               {
                  public void addNotify()
                  {
                     super.addNotify();
                  }
               };
               
               pane.addKeyListener(new KeyAdapter()
               {
                  public void keyPressed(KeyEvent e)
                  {
                     if(e.getKeyCode() == KeyEvent.VK_TAB ||
                        e.getKeyCode() == KeyEvent.VK_DELETE)
                     {
                        e.consume();
                     }
                     else
                     {
                        WindowManager.getDefault().getMainWindow().dispatchEvent(e);
                     }
                  }
               }
               );
            }
            
            add(pane, BorderLayout.CENTER);
            doLayout();
//         }
//         catch (Exception e)
//         {
//            e.printStackTrace();
//         }
//      }
   }
   
   public void releaseControl()
   {
      removeAll();
      pane = null;
   }
   
   /**
    *  Called when NetBeans pulls this TopComponent out of the UI. This is a
    * good place to remove the Describe property editor control.
    */
   public void removeNotify()
   {
      super.removeNotify();
   }
   
   public static synchronized DocumentationTopComponnet getDefault()
   {
      if (mTopComponent == null)
      {
         mTopComponent = new DocumentationTopComponnet();
      }
      return mTopComponent;
   }
   
   public static synchronized DocumentationTopComponnet getInstance()
   {
      if(mTopComponent == null)
      {
         TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
         if (tc != null)
         {
            mTopComponent = (DocumentationTopComponnet)tc;
         }
         else
         {
            mTopComponent = new DocumentationTopComponnet();
         }
      }
      
      return mTopComponent;
   }
   
   public int getPersistenceType()
   {
      return TopComponent.PERSISTENCE_ALWAYS;
   }
   
   public String preferredID()
   {
      return getClass().getName();
   }
   
   public Image getIcon()
   {
		return Utilities.loadImage(
		"org/netbeans/modules/uml/documentation/ui/resources/DocPane.gif"); // NOI18N
   }
   
   public void startEdit() {
       pane.startEdit();
   }
   
   public HelpCtx getHelpCtx() {
       return new HelpCtx("DDEToolsDocumentation2_htm_wp1342319");
   }
   
   /**
    * Listen for activated nodes property change events.
    */
   public void propertyChange(PropertyChangeEvent evt)
   {
       if (evt.getPropertyName().equals( TopComponent.Registry.PROP_ACTIVATED_NODES ))
       {
           boolean enable = false;
           org.openide.nodes.Node[] arr = TopComponent.getRegistry().getActivatedNodes();
           // save element doc before switching to another component, 79828
           if(arr.length == 1)
           {
               IProjectTreeItem item = (IProjectTreeItem)arr[0].getCookie(IProjectTreeItem.class);
               if(item != null)
               {
                   pane.onProjectTreeSelChanged(new IProjectTreeItem[] {item});
                   enable = true;
               }
               else
                   pane.setElementDescription();
           }
           else
           {
               pane.setElementDescription();
           }
           pane.enableDocControl(enable);
       }
           
   }
}
