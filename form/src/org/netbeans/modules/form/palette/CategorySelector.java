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

package org.netbeans.modules.form.palette;

import javax.swing.*;

import org.openide.nodes.Node;
import org.openide.explorer.view.ListView;
import org.openide.explorer.*;
import org.openide.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;

/**
 * A simple panel allowing the user to choose one of the palette categories.
 * Used by ChooseCategoryWizardPanel in the "Add to Palette" wizard for
 * choosing the target category for added components.
 */

class CategorySelector extends JPanel implements ExplorerManager.Provider {

    private ExplorerManager explorerManager;

    CategorySelector() {
        explorerManager = new ExplorerManager();	
        explorerManager.setRootContext(getCategoryRootNode());	
	
        ListView listView = new ListView();
        // Issue 50703 - restore the default scroll pane's border
        JScrollPane scrollPane = new JScrollPane();
        listView.setBorder(scrollPane.getBorder());
        listView.getAccessibleContext().setAccessibleDescription(
            PaletteUtils.getBundleString("ACSD_CTL_PaletteCategories")); // NOI18N
        listView.setPopupAllowed(false);
        listView.setTraversalAllowed(false);
	
        JLabel categoryLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, 
                PaletteUtils.getBundleString("CTL_PaletteCategories")); // NOI18N
        categoryLabel.setLabelFor(listView);

        getAccessibleContext().setAccessibleDescription(
            PaletteUtils.getBundleString("ACSD_PaletteCategoriesSelector")); // NOI18N

        setLayout(new java.awt.BorderLayout(0, 5));
        add(categoryLabel, java.awt.BorderLayout.NORTH);
        add(listView, java.awt.BorderLayout.CENTER);
    }

    private Node getCategoryRootNode() {
	Node root = new AbstractNode(new Children.Array());	
	
	Node[] paleteCategories = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), false);
	Node[] categoryNodes = new Node[paleteCategories.length];
	
	for (int i = 0; i < paleteCategories.length; i++) {
	    categoryNodes[i] = new FilterNode(paleteCategories[i], Children.LEAF);
	}		
	
	root.getChildren().add(categoryNodes);
	getExplorerManager().setRootContext(root);	

	return root;    
    }
    
    public static String selectCategory() {
        CategorySelector selector = new CategorySelector();
        selector.setBorder(new javax.swing.border.EmptyBorder(12, 12, 0, 11));
        DialogDescriptor dd = new DialogDescriptor(
            selector,
            PaletteUtils.getBundleString("CTL_SelectCategory_Title"), // NOI18N
            true,
            null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);

        return dd.getValue() == DialogDescriptor.OK_OPTION ?
            selector.getSelectedCategory() : null;
    }

    String getSelectedCategory() {
        Node[] selected = explorerManager.getSelectedNodes();
        return selected.length == 1 ? selected[0].getName() : null;
    }

    // ExplorerManager.Provider
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(400, 300);
    }
}
