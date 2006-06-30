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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.*;
import java.awt.*;

/**
 * TreePanelView.java
 *
 * Created on May 26, 2005
 * @author mkuchtiak
 */
public class TreePanelView extends PanelView {

    java.util.HashMap map;
    JPanel cardPanel;
    CardLayout cardLayout;
    public TreePanelView() {
        super();
    }

    public void initComponents() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        map = new java.util.HashMap();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(cardPanel);
        add (scrollPane, BorderLayout.CENTER);
    }

    public void showSelection(org.openide.nodes.Node[] nodes) {
        if (nodes.length>0 && nodes[0] instanceof TreeNode) {
            TreeNode node = (TreeNode)nodes[0];
            showPanel(node);
        }
    }
    
    protected void showPanel(TreeNode node) {
        String panelId = node.getPanelId();
        TreePanel treePanel = (TreePanel)map.get(panelId);
        if (treePanel==null) {
            treePanel = node.getPanel();
            map.put(panelId,treePanel);
            cardPanel.add((JPanel)treePanel,panelId);
        } 
        cardLayout.show(cardPanel, panelId);
        treePanel.setModel(node);
    }

    protected org.netbeans.modules.xml.multiview.Error validateView() {
        return null;
    }

}
