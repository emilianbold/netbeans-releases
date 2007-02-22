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

package org.netbeans.modules.xml.schema.ui.basic.search;

import java.awt.Component;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategoryNode;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * Provides utility methods for the search provider implementations.
 *
 * @author Nathan Fiedler
 */
public class Providers {

    /**
     * Creates a new instance of Providers.
     */
    private Providers() {
    }

    /**
     * If a CategoryNode is selected, return the child type of that node.
     *
     * @param  category  Category for which to find selected component.
     * @return  schema component class, or null if none selected.
     */
    public static Class<? extends SchemaComponent> getSelectedChildType(
            Category category) {
        Component parent = category.getComponent().getParent();
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, parent);
        if (tc != null) {
            Node[] nodes = tc.getActivatedNodes();
            if (nodes != null && nodes.length > 0) {
                for (Node node : nodes) {
                    CategoryNode cn = (CategoryNode) node.getCookie(
                            CategoryNode.class);
                    if (cn != null) {
                        return cn.getChildType();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieve the selected schema component for the given Category.
     *
     * @param  category  Category for which to find selected component.
     * @return  selected schema component, or null if none.
     */
    public static SchemaComponent getSelectedComponent(Category category) {
        Component parent = category.getComponent().getParent();
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, parent);
        if (tc != null) {
            Node[] nodes = tc.getActivatedNodes();
            if (nodes != null && nodes.length > 0) {
                for (Node node : nodes) {
                    SchemaComponentNode scn = (SchemaComponentNode) node.
                            getCookie(SchemaComponentNode.class);
                    if (scn != null) {
                        return scn.getReference().get();
                    }
                }
            }
        }
        return null;
    }
}
