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
 * CatalogEntryNode.java
 *
 * Created on 11/13/03 4:02 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.operators.JTreeOperator;

/** CatalogEntryNode Class
 * @author ms113234 */
public class CatalogEntryNode extends Node {
    
    private static class RemoveAction extends ActionNoBlock{
        RemoveAction(){
            super(null, "Delete Delete");
        }
    }
    
    private static class EditAction extends ActionNoBlock{
        EditAction(){
            super(null, "Edit");
        }
    }

    private static final Action editAction = new EditAction();
    private static final Action viewAction = new ViewAction();
    private static final Action propertiesAction = new PropertiesAction();
    private static final DeleteAction removeAction = new DeleteAction();
    
    /** creates new CatalogEntryNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public CatalogEntryNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }
    
    /** creates new CatalogEntryNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public CatalogEntryNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }
    
    /** creates new CatalogEntryNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public CatalogEntryNode(Node parent, String treePath) {
        super(parent, treePath);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            viewAction,
            propertiesAction
        });
    }
    
    /** performs ViewAction with this node */
    public void view() {
        viewAction.perform(this);
    }
    
    public void edit(){
        editAction.perform(this);
    }
    
    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    public void remove() {
        removeAction.perform(this);
    }
}

