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

/*
 * CatalogNode.java
 *
 * Created on 11/13/03 4:01 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.operators.JTreeOperator;

/** CatalogNode Class
 * @author ms113234 */
public class CatalogNode extends AbstractNode {
    
    /* ACTIONS */
    private static class RefreshAction extends ActionNoBlock {
        public RefreshAction() {
            super(null, "Refresh");
        }
    }
    
    private static class AddLocalAction extends ActionNoBlock {
        public AddLocalAction() {
            super(null, "Add Local DTD or XML Schema...");
        }
    }
    
    private static class RemoveCatalogAction extends Action {
        public RemoveCatalogAction() {
            super(null, "Remove");
        }
    }
    
    
    private static final Action refreshAction = new RefreshAction();
    private static final Action removeCatalogAction = new RemoveCatalogAction();
    private static final Action customizeAction = new CustomizeAction();
    private static final Action propertiesAction = new PropertiesAction();
    private static final Action addLocalAction = new AddLocalAction();

    
    /** creates new CatalogNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public CatalogNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }
    
    /** creates new CatalogNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public CatalogNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }
    
    /** creates new CatalogNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public CatalogNode(Node parent, String treePath) {
        super(parent, treePath);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            refreshAction,
            removeCatalogAction,
            customizeAction,
            propertiesAction
        });
    }
    
    public void addLocal(){
        addLocalAction.perform(this);
    }
    
    /** performs RefreshAction with this node */
    public void refresh() {
        refreshAction.perform(this);
    }
    
    /**
     * performs RemoveCatalogAction with this node
     */
    public void removeCatalog() {
        removeCatalogAction.perform(this);
    }
    
    /** performs CustomizeAction with this node */
    public void customize() {
        customizeAction.perform(this);
    }
    
    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
    
    /** returns catalog entry node with given name or <code>null</code> */
    public CatalogEntryNode getCatalogEntry(String displayName) {
        return (CatalogEntryNode) getChild(displayName, CatalogEntryNode.class);
    }

}

