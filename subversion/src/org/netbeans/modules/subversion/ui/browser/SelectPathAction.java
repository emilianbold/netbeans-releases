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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
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