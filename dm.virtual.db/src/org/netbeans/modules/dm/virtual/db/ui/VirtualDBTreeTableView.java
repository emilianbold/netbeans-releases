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
package org.netbeans.modules.dm.virtual.db.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.netbeans.modules.dm.virtual.db.ui.model.VirtualDBTreeTableModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * @author Ahimanikya Satapathy
 */
public class VirtualDBTreeTableView extends JPanel implements ExplorerManager.Provider {

    private VirtualDBTreeView treeView;
    private PropertySheetView propertyView;
    private Node selectedNode;
    private JSplitPane splitPane;
    private ExplorerManager etlExplorerManager = new ExplorerManager();

    public VirtualDBTreeTableView() {
        treeView = new VirtualDBTreeView();
        treeView.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JLabel treeVLable = new JLabel(); // For avoiding A11Y Warnings
        treeVLable.setText("");
        treeVLable.setVisible(false);
        treeVLable.setLabelFor(treeView);
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(VirtualDBTreeTableView.class, "ACSD_VirtualDBViewer")); // NO I18N
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(VirtualDBTreeTableView.class, "ACSD_VirtualDBViewer")); // NO I18N

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(VirtualDBTreeTableView.class, "LBL_FFDBdef")),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        leftPanel.add(treeView);
        leftPanel.setPreferredSize(new Dimension(75, 200));
        propertyView = new PropertySheetView();
        propertyView.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        try {
            propertyView.setSortingMode(PropertySheet.UNSORTED);
        } catch (PropertyVetoException ignore) {
            // ignore
        }

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(VirtualDBTreeTableView.class, "LBL_Properties")), BorderFactory.createEmptyBorder(0, 0,
                0, 0)));
        rightPanel.add(propertyView);

        splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        Frame f = WindowManager.getDefault().getMainWindow();
        Dimension d = f.getSize();
        int divLocation = d.width / 3;
        splitPane.setDividerLocation(divLocation);
        this.add(splitPane);
    }

    public void setModel(VirtualDBTreeTableModel model) {
        this.getExplorerManager().setRootContext(model.getRootNode());
        if (selectedNode == null) { // set root default selected node
            selectedNode = model.getRootNode();
        }
    }

    public void setDividerLocation(int size) {
        splitPane.setDividerLocation(size);
    }

    public Node getCurrentNode() {
        return selectedNode;
    }

    public ExplorerManager getExplorerManager() {
        return etlExplorerManager;
    }

    class VirtualDBTreeView extends BeanTreeView {

        public VirtualDBTreeView() {
            super();
        }

        @Override
        protected void selectionChanged(Node[] nodes, ExplorerManager em) throws PropertyVetoException {
            super.selectionChanged(nodes, em);
            if (nodes != null && nodes.length != 0) {
                selectedNode = nodes[0];
                propertyView.setNodes(new Node[]{selectedNode});
                firePropertyChange(VirtualDBTreeTableView.this.getName(), selectedNode, selectedNode);
            }
        }
    }
}

