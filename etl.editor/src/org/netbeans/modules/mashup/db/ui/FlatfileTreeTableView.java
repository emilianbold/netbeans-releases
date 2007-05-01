/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.netbeans.modules.mashup.db.ui.model.FlatfileTreeTableModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileTreeTableView extends JPanel implements ExplorerManager.Provider {

    private FlatfileTreeView treeView;
    private PropertySheetView propertyView;
    private Node selectedNode;
    private JSplitPane splitPane;
    private ExplorerManager etlExplorerManager = new ExplorerManager();

    /**
     * Creates a new instance of FlatfileTableView
     * 
     * @param model is the model to create this object with.
     */
    public FlatfileTreeTableView() {
        treeView = new FlatfileTreeView();
        treeView.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Flat File Database Definition"),
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
        rightPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Properties"), BorderFactory.createEmptyBorder(0, 0,
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

    /**
     * Sets model for this view to the given instance.
     * 
     * @param model FlatfileTreeTableModel providing content for this view.
     */
    public void setModel(FlatfileTreeTableModel model) {
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

    class FlatfileTreeView extends BeanTreeView {
        public FlatfileTreeView() {
            super();
        }

        protected void selectionChanged(Node[] nodes, ExplorerManager em) throws PropertyVetoException {
            super.selectionChanged(nodes, em);
            if (nodes != null && nodes.length != 0) {
                selectedNode = nodes[0];
                propertyView.setNodes(new Node[] { selectedNode});
                firePropertyChange(FlatfileTreeTableView.this.getName(), selectedNode, selectedNode);
            }
        }
    }
}

