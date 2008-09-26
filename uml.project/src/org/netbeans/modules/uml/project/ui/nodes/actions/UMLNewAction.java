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


package org.netbeans.modules.uml.project.ui.nodes.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.datatransfer.NewType;
import org.openide.windows.WindowManager;


public class UMLNewAction extends NodeAction
{
   private static ActSubMenuModel model = new ActSubMenuModel(null);
   
   protected void performAction(Node[] activatedNodes)
   {
      performAction(activatedNodes, 0);
   }
   
   protected boolean asynchronous()
   {
      return false;
   }
   
   /** Performs action on index and nodes.
    */
   private static void performAction(Node[] activatedNodes, int indx)
   {
      NewType[] types = getNewTypes(activatedNodes);
      
      if (types.length <= indx)
      {
         return;
      }
      NewType type = types[indx];
      try
      {
         type.create();
      }
      catch (java.io.IOException e)
      {
         Exceptions.printStackTrace(e);
      }
   }
   
   private static NewType[] getNewTypes()
   {
      return getNewTypes(WindowManager.getDefault().getRegistry().getCurrentNodes());
   }
   
   /** Getter for array of activated new types.
    */
   private static NewType[] getNewTypes(Node[] activatedNodes)
   {
      if ((activatedNodes == null) || (activatedNodes.length != 1))
      {
         return new NewType[0];
      }
      else
      {
         return activatedNodes[0].getNewTypes();
      }
   }
   
   protected boolean enable(Node[] activatedNodes)
   {
      NewType[] types = getNewTypes();
      model.cs.fireChange();
      return (types.length > 0);
   }
   
   public String getName()
   {
      return createName(getNewTypes());
   }
   
   public HelpCtx getHelpCtx()
   {
      return new HelpCtx(UMLNewAction.class);
   }
   
   public javax.swing.JMenuItem getPopupPresenter()
   {
      return new Actions.SubMenu(this, model, true);
   }
   
   public javax.swing.Action createContextAwareInstance(Lookup actionContext)
   {
      return new DelegateAction(this, actionContext);
   }
   
   /** Utility method, creates name for action depending on specified new types. */
   private static String createName(NewType[] newTypes)
   {
//      if ((newTypes != null) && (newTypes.length == 1))
//      {
//         return NbBundle.getMessage(UMLNewAction.class, 
//               "LBL_New_Menu",  // NOI18N
//               newTypes[0].getName());
//      }
//      else
//      {
         return NbBundle.getMessage(UMLNewAction.class, 
               "LBL_New_Menu", "");  // NOI18N
//      }
   }
   

   private static class ActSubMenuModel implements Actions.SubMenuModel
   {
      static final long serialVersionUID = -1L;
      
      final ChangeSupport cs = new ChangeSupport(this);
      
      /** lookup to read the new types from or null if they whould be taken
       * directly from top component's selected nodes
       */
      private Lookup lookup;
      private Node prevNode;
      private NewType[] prevTypes;
      
      ActSubMenuModel(Lookup lookup)
      {
         this.lookup = lookup;
      }
      
      private NewType[] newTypes()
      {
         if (lookup != null)
         {
            java.util.Collection lkupResult = lookup.lookupResult(Node.class).allItems();
            
            if (lkupResult != null && lkupResult.size() == 1)
            {
               java.util.Iterator it = lkupResult.iterator();
               
               while (it.hasNext())
               {
                  Lookup.Item item = (Lookup.Item) it.next();
                  Node node = (Node) item.getInstance();
                  
                  if (node != null)
                  {
                     if (node == prevNode && prevTypes != null)
                     {
                        return prevTypes;
                     }
                     prevNode = node;
                     prevTypes = node.getNewTypes();
                     return prevTypes;
                  }
               }
            }
         }
         
         return getNewTypes();
      }
      
      public int getCount()
      {
         return newTypes().length;
      }
      
      public String getLabel(int index)
      {
         NewType[] newTypes = newTypes();
         
         if (newTypes == null || newTypes.length <= index)
         {
            return null;
         }
         else
         {
            return newTypes[index].getName();
         }
      }
      
      public String getIconResource(int index)
      {
         String iconRes = null;
         NewType[] newTypes = newTypes();
         if (newTypes != null && newTypes.length > index)
         {
            NewType type = newTypes[index];
            iconRes = (type instanceof INewTypeExt ? ((INewTypeExt)type).getIconResource() : null);
         }
         return iconRes;
      }
      
      public HelpCtx getHelpCtx(int index)
      {
         NewType[] newTypes = newTypes();
         
         if (newTypes == null || newTypes.length <= index)
         {
            return null;
         }
         else
         {
            return newTypes[index].getHelpCtx();
         }
      }
      
      public void performActionAt(int index)
      {
         NewType[] nt = newTypes();
         
         if (nt == null || nt.length <= index)
         {
            return;
         }
         
         Node[] nodeArr;
         
         if (lookup != null)
         {
            nodeArr = (Node[]) lookup.lookupAll(Node.class).toArray(new Node[0]);
         }
         else
         {
            nodeArr = WindowManager.getDefault().getRegistry().getCurrentNodes();
         }
         performAction(nodeArr, index);
      }
      
      /** Adds change listener for changes of the model.
       */
      public void addChangeListener(ChangeListener l)
      {
         cs.addChangeListener(l);
      }
      
      /** Removes change listener for changes of the model.
       */
      public void removeChangeListener(ChangeListener l)
      {
         cs.removeChangeListener(l);
      }
   }
   // end of ActSubMenuModel
   
   /** A delegate action that is usually associated with a specific lookup and
    * extract the nodes it operates on from it. Otherwise it delegates to the
    * regular NodeAction.
    */
   private static final class DelegateAction extends javax.swing.AbstractAction
         implements Presenter.Popup
   {
      /** Action to delegate to. */
      private final NodeAction delegate;
      
      /** Associated model to use. */
      private final ActSubMenuModel model;
      
      public DelegateAction(NodeAction a, Lookup actionContext)
      {
         this.delegate = a;
         this.model = new ActSubMenuModel(actionContext);
         this.putValue(NAME, createName(model.newTypes()));
      }
      
      /** Overrides superclass method, adds delegate description. */
      public String toString()
      {
         return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
      }
      
      /** Invoked when an action occurs.
       */
      public void actionPerformed(java.awt.event.ActionEvent e)
      {
         model.performActionAt(0);
      }
      
      public void addPropertyChangeListener(PropertyChangeListener listener)
      {
      }
      
      public void removePropertyChangeListener(PropertyChangeListener listener)
      {
      }
      
      public Object getValue(String key)
      {
         if (javax.swing.Action.NAME.equals(key))
         {
            return createName(model.newTypes());
         }
         else
         {
            return delegate.getValue(key);
         }
      }
      
      public boolean isEnabled()
      {
         return model.getCount() > 0;
      }
      
      public JMenuItem getPopupPresenter()
      {
         Actions.SubMenu popupPresenter  = new CustomedMenuItem(this, model, true);
         return popupPresenter;
      }
   }
   // end of DelegateAction
   
   private static class CustomedMenuItem extends Actions.SubMenu
   {
      private Actions.SubMenuModel model;
      private List<JMenuItem> menuList = new ArrayList<JMenuItem>();
      private JMenu aMenu;
      Action action;
      
      public CustomedMenuItem(Action aAction, Actions.SubMenuModel model, boolean popup)
      {
         super(aAction, model, popup);
         this.model = model;
         this.action = aAction;
      }
      
      public JComponent[] getMenuPresenters()
      {
         return synchMenuPresenters(null);
      }
      
      public JComponent[] synchMenuPresenters(JComponent[] items)
      {
         menuList.clear();
         if (model != null)
         {
            int count = model.getCount();
            if (count > 0 )
            {
               JMenuItem item = null;
               aMenu = new JMenu();
               Mnemonics.setLocalizedText(aMenu, (String)action.getValue(Action.NAME));
               
               // create sub menu items
               for (int i = 0; i < count; i++)
               {
                  String label = model.getLabel(i);
                  String iconRes = ((ActSubMenuModel)model).getIconResource(i);
                  if (label != null)
                  {
                     item = new JMenuItem();
                     Mnemonics.setLocalizedText(item, label);
                     if (iconRes != null)
                     {
                        ImageIcon icon = new ImageIcon(ImageUtilities.loadImage(iconRes));
                        item.setIcon(icon);
                     }
                     item.addActionListener(new SubActionListener(i, model));
                     aMenu.add(item);
                  }
               }
               aMenu.setEnabled(true);
               menuList.add(aMenu);
            }
         }
         return menuList.toArray(new JMenuItem[menuList.size()]);
      }
      
      private static class SubActionListener implements java.awt.event.ActionListener
      {
         int index;
         Actions.SubMenuModel model;
         
         public SubActionListener(int index, Actions.SubMenuModel support)
         {
            this.index = index;
            this.model = support;
         }
         
         public void actionPerformed(ActionEvent e)
         {
            model.performActionAt(index);
         }
      }
   }
}
