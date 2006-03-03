/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.browser;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * XXX same abstract base with createfolder ... 
 * @author Tomas Stupka
 */
public class SelectPathAction extends AbstractAction {
    
    private final SVNUrl selectionUrl;
    private Node[] selectionNodes;
    private final Browser browser;
    private final static Node[] EMPTY_NODES = new Node[0];
    
    public SelectPathAction(Browser browser, SVNUrl selection) {
        this.browser = browser;
        this.selectionUrl = selection;                
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Action_SelectPath")); // NOI18N
        setEnabled(true);
    }       
    
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = getSelectionNodes();
        
        if(nodes == null || nodes == EMPTY_NODES) {
            return;
        }
        try {            
            browser.getExplorerManager().setSelectedNodes(nodes);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace(); // should not happen
        }
    }

    private Node[] getSelectionNodes() {
        if(selectionNodes == null) {
            String[] segments = selectionUrl.getPathSegments();
            Node node = (RepositoryPathNode) browser.getExplorerManager().getRootContext();            
            
            for (int i = 0; i < segments.length; i++) {
                Children children = node.getChildren();    
                node = children.findChild(segments[i]);
                if(node==null) {
                    break;
                }                    
            }            
            if(node == null) {
                selectionNodes = EMPTY_NODES;
            } else {
                selectionNodes = new Node[] {node};    
            }            
        }
        return selectionNodes;
    }    
    
}    