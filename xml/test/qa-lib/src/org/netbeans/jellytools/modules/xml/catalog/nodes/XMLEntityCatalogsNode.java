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
package org.netbeans.jellytools.modules.xml.catalog.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.xml.catalog.actions.MountCatalogAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/** XMLEntityCatalogsNode Class
 * @author ms113234 */
public class XMLEntityCatalogsNode extends AbstractNode {
    private static final String MY_PATH = Bundle
    .getString("org.netbeans.modules.xml.catalog.Bundle", "TEXT_catalog_root");  // NOI18N
    
    private static final Action mountCatalogAction = new MountCatalogAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new XMLEntityCatalogsNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public XMLEntityCatalogsNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new XMLEntityCatalogsNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public XMLEntityCatalogsNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }

    /** creates new XMLEntityCatalogsNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public XMLEntityCatalogsNode(Node parent, String treePath) {
        super(parent, treePath);
    }

    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            mountCatalogAction,
            propertiesAction
        });
    }

    /** performs MountCatalogAction with this node */
    public void mountCatalog() {
        mountCatalogAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
    
    /** returns default XML Entity Catalogs node instance */
    public static XMLEntityCatalogsNode getInstance() {
        return new XMLEntityCatalogsNode(AbstractNode.getRuntimeTab().tree(), MY_PATH);
    }
    
   /** returns catalog node with given name or <code>null</code> */
    public CatalogNode getCatalog(String displayName) {
        return (CatalogNode) getChild(displayName, CatalogNode.class);
    }
}

