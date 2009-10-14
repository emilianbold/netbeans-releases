/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.graph.view.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorManager;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoCategory;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * Tool bar for use in transformation, data filter and data validation editors.
 *
 * @author Ritesh Adval
 * @author Girish Patil
 * @author Jonathan Giron
 * @version $Revision$
 */

public class SQLToolBar extends BasicToolBar {

    class ToolBarItemActionListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e ActionEvent to handle
         */
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            SQLToolBarMenuItem toolItem = (SQLToolBarMenuItem) src;
            IGraphView view = SQLToolBar.this.manager.getGraphView();
            if (view != null) {
                IGraphController controller = view.getGraphController();
                if (controller != null) {
                    controller.handleNodeAdded(toolItem.getItemObject(), new Point(50, 50));
                }
            }
        }
    }

    class ToolBarPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (IOperatorXmlInfo.OPERATOR_CHECKED.equals(propName)) {
                Boolean val = (Boolean) evt.getNewValue();
                IOperatorXmlInfo node = (IOperatorXmlInfo) evt.getSource();

                SQLToolBarMenuItem item = SQLToolBar.this.findToolBarItem(node);
                if (item != null) {
                    item.setVisible(val.booleanValue());
                }
            } else if (IOperatorXmlInfo.OPERATOR_DROPPED.equals(propName)) {
                IOperatorXmlInfo node = (IOperatorXmlInfo) evt.getSource();
                IGraphView view = SQLToolBar.this.manager.getGraphView();

                if (view != null) {
                    IGraphController controller = view.getGraphController();
                    if (controller != null) {
                        controller.handleNodeAdded(node, new Point(50, 50));
                    }
                }
            }
        }
    }

    private ToolBarItemActionListener aListener;
    private ToolBarPropertyChangeListener pListener;
    private SQLToolBarMenu mActiveMenu;
    private List mMenus = new Vector();
    private IOperatorXmlInfoModel model;
    private IOperatorManager manager;

    public SQLToolBar() {
        pListener = new ToolBarPropertyChangeListener();
        aListener = new ToolBarItemActionListener();

        setFloatable(false);
        setBorder(null);
        this.setMargin(new Insets(0, 0, 0, 0));
    }

    public SQLToolBar(IOperatorManager mgr) {
        this();
        manager = mgr;
        model = mgr.getOperatorXmlInfoModel();
    }

    public SQLToolBarMenu getActiveMenu() {
        synchronized (getTreeLock()) {
            return mActiveMenu;
        }
    }

    /**
     * @see java.swing.JToolBar#getMinimumSize
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    public SQLToolBarMenu getNextMenu(SQLToolBarMenu menu) {
        synchronized (getTreeLock()) {
            for (int i = 0; i < mMenus.size(); i++) {
                Object comp = mMenus.get(i);
                if (comp == menu) {
                    SQLToolBarMenu nxtMenu = null;
                    if (i == mMenus.size() - 1) {
                        nxtMenu = (SQLToolBarMenu) mMenus.get(0);
                    } else {
                        nxtMenu = (SQLToolBarMenu) mMenus.get(i + 1);
                    }

                    if (!nxtMenu.isVisible()) {
                        nxtMenu = menu;
                    }
                    return nxtMenu;
                }
            }
        }
        throw new IllegalArgumentException("The menu specified is not in this group.");
    }

    public SQLToolBarMenu getPreviousMenu(SQLToolBarMenu menu) {
        synchronized (getTreeLock()) {
            for (int i = 0; i < mMenus.size(); i++) {
                Object comp = mMenus.get(i);
                if (comp == menu) {
                    if (i == 0) {
                        return (SQLToolBarMenu) mMenus.get(mMenus.size() - 1);
                    } else {
                        return (SQLToolBarMenu) mMenus.get(i - 1);
                    }
                }
            }
        }
        throw new IllegalArgumentException("The menu specified is not in this group.");
    }

    /**
     * Initializes contents of the toolbar.
     */
    @Override
    public void initializeToolBar() {
        // Remove all old toolbar components
        this.removeAll();
        this.mMenus.clear();

        // Add any custom components first
        super.initializeToolBar();
    }
    
  public void initializeSQLToolBar() {
        // Add SQL operators
        Node node = model.getRootNode();
        Children children = node.getChildren();
        Node[] nodes = children.getNodes();

        for (int i = 0; i < nodes.length; i++) {
            IOperatorXmlInfoCategory catNode = (IOperatorXmlInfoCategory) nodes[i];           
            if (shouldDisplay(catNode.getToolbarType())) {
                createOperatorCategories(catNode);
            }
        }
     }
    
    
    public void setActiveMenu(SQLToolBarMenu menu) {
        synchronized (getTreeLock()) {
            if (mActiveMenu != menu) {
                if (mActiveMenu != null) {
                    // if menu is null the active menu already
                    // closed
                    if (menu != null) {
                        mActiveMenu.hideButtonMenu();
                    }
                }
            }
            mActiveMenu = menu;
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        if (this.isEnabled() == enable) {
            return;
        }
        super.setEnabled(enable);
        synchronized (getTreeLock()) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component comp = getComponent(i);
                comp.setEnabled(enable);
            }
        }
    }

    private boolean shouldDisplay(int nodeType) {
        return (IOperatorXmlInfoModel.CATEGORY_ALL == nodeType || ((manager.getToolbarType() & nodeType) != 0));
    }
    
    private void createOperatorCategories(IOperatorXmlInfoCategory catNode) {
        SQLToolBarMenu menu = new SQLToolBarMenu(catNode, manager, this);
        mMenus.add(menu);

        // Now add each one of the operator in this category.
        Children children = ((Node)catNode).getChildren();
        Node[] nodes = children.getNodes();

        for (int i = 0; i < nodes.length; i++) {
            IOperatorXmlInfo opNode = (IOperatorXmlInfo) nodes[i];
            if (shouldDisplay(opNode.getToolbarType())) {
                SQLToolBarMenuItem bvi = menu.addMenuItem(opNode);

                if (bvi != null) {
                    opNode.addPropertyChangeListener(pListener);
                    bvi.addActionListener(aListener);
                }
            }
        }
        
        // Always append a separator before appending the category menu.
        this.addSeparator(new Dimension(1, 0));
        this.addSeparator(new Dimension(2, 30));
        this.addSeparator(new Dimension(1, 0));
        this.add(menu);        
    }
    
    private SQLToolBarMenuItem findToolBarItem(IOperatorXmlInfo node) {
        SQLToolBarMenuItem bvi = null;

        for (int i = 0; i < this.getComponentCount(); i++) {
            Component comp = this.getComponentAtIndex(i);

            if (comp instanceof SQLToolBarMenu) {
                SQLToolBarMenu menu = (SQLToolBarMenu) this.getComponentAtIndex(i);

                bvi = menu.findMenuItem(node);

                if (bvi != null) {
                    break;
                }
            }
        }

        return bvi;
    }
}
