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
package org.netbeans.modules.subversion.ui.search;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Handles the UI for repository browsing.
 *
 * @author Tomas Stupka
 */
public class SvnSearch {
        
    private final SvnSearchPanel panel;    
    
    private RepositoryFile repositoryRoot;

    private SvnProgressSupport support;
    
    public SvnSearch(RepositoryFile repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
        panel = new SvnSearchPanel();
        listLogEntries();
    }       

    /**
     * Cancels all running tasks
     */
    public void cancel() {
        Node rootNode = getExplorerManager().getRootContext();
        if(rootNode != null) {
            getExplorerManager().setRootContext(Node.EMPTY);
            try {                                
                rootNode.destroy();
                if(support != null) {
                    support.cancel();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
            }            
        }
    }
    
    private void listLogEntries() {
        final Node root = new AbstractNode(new Children.Array());
        panel.getExplorerManager().setRootContext(root);
        final Node[] waitNodes = new Node[] { new WaitNode("Loading..." )};
        root.getChildren().add(waitNodes);
        
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(this.repositoryRoot.getRepositoryUrl());
        try { 
            support = new SvnProgressSupport() {
                protected void perform() {
                    SvnClient client;
                    ISVNLogMessage[] lm;
                    try {
                        client = Subversion.getInstance().getClient(SvnSearch.this.repositoryRoot.getRepositoryUrl(), this);
                        lm = client.getLogMessages(repositoryRoot.getRepositoryUrl(), new SVNRevision.Number(1), SVNRevision.HEAD);
                    } catch (SVNClientException ex) {
                        AbstractNode errorNode = new AbstractNode(Children.LEAF);
                        errorNode.setDisplayName("Error"); 
                        errorNode.setShortDescription(ex.getLocalizedMessage());
                        root.getChildren().remove(waitNodes);
                        root.getChildren().add(new Node[] {errorNode});
                        return;
                    }

                    if(isCanceled()) {
                        return;
                    }    

                    root.getChildren().remove(waitNodes);
                    Node[] nodes = new Node[lm.length];
                    for (int i = 0; i < lm.length; i++) {
                        nodes[i] = new LogMessageNode(lm[i]);
                    }
                    root.getChildren().add(nodes);
                }                        
            };
            support.start(rp, "Searching revisions");
        } finally {
            support = null;
        }
    }
   
    public JPanel getSearchPanel() {
        return panel;
    }

    private ExplorerManager getExplorerManager() {
        return panel.getExplorerManager();
    }
    
    public SVNRevision getSelectedRevision() {
        Node[] nodes = getExplorerManager().getSelectedNodes();
        if(nodes == null || nodes.length == 0) {
            return null;
        }
        return ((LogMessageNode) nodes[0]).getLogMessage().getRevision();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().removePropertyChangeListener(listener);
    }
    
}
