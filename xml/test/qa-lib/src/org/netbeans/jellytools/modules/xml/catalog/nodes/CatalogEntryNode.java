package org.netbeans.jellytools.modules.xml.catalog.nodes;

/*
 * CatalogEntryNode.java
 *
 * Created on 11/13/03 4:02 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.Bundle;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.JTreeOperator;

/** CatalogEntryNode Class
 * @author ms113234 */
public class CatalogEntryNode extends Node {
    
    private static final Action viewAction = new ViewAction();
    private static final Action propertiesAction = new PropertiesAction();

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

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
}

