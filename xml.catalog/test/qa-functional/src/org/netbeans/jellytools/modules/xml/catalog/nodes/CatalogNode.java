package org.netbeans.jellytools.modules.xml.catalog.nodes;

/*
 * CatalogNode.java
 *
 * Created on 11/13/03 4:01 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.jndi.actions.RefreshAction;
import org.netbeans.jellytools.modules.xml.catalog.actions.UnmountCatalogAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.Bundle;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.JTreeOperator;

/** CatalogNode Class
 * @author ms113234 */
public class CatalogNode extends AbstractNode {
    
    private static final Action refreshAction = new RefreshAction();
    private static final Action unmountCatalogAction = new UnmountCatalogAction();
    private static final Action customizeAction = new CustomizeAction();
    private static final Action propertiesAction = new PropertiesAction();

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
            unmountCatalogAction,
            customizeAction,
            propertiesAction
        });
    }

    /** performs RefreshAction with this node */
    public void refresh() {
        refreshAction.perform(this);
    }

    /** performs UnmountCatalogAction with this node */
    public void unmountCatalog() {
        unmountCatalogAction.perform(this);
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

