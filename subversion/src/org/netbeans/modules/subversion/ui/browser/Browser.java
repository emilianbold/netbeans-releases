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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.wizards.CheckoutWizard;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class Browser implements VetoableChangeListener, BrowserClient {
        
    private final BrowserPanel panel;
    
    private String defaultFolderName;
    
    private static final RepositoryFile[] EMPTY_ROOT = new RepositoryFile[0];
    private static final List EMPTY_LIST = new ArrayList(0);
    private static final Action[] EMPTY_ACTIONS = new Action[0];
    
    private final boolean showFiles;    
    
    private RepositoryFile repositoryRoot;        
    
    private Action[] nodeActions;
    
    private ArrayList progressHandlers;

    private boolean fileSelectionOnly;
    
    public Browser(String title) {                                
        this(title, false, false, false);
    }   
    
    public Browser(String title, boolean showFiles, boolean singleSelectionOnly, boolean fileSelectionOnly) {
        this.showFiles = showFiles;
        this.fileSelectionOnly = fileSelectionOnly;

        panel = new BrowserPanel(title,           
                                 org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSN_RepositoryTree"),         // NOI18N
                                 org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSD_RepositoryTree"),         // NOI18N
                                 singleSelectionOnly);
        
        getExplorerManager().addVetoableChangeListener(this);                
    }       

    public void setup(RepositoryFile repositoryRoot, RepositoryFile[] select, BrowserAction[] nodeActions) 
    {        
        if(nodeActions!=null) {
            this.nodeActions = nodeActions;
            panel.setActions(nodeActions);    
            for (int i = 0; i < nodeActions.length; i++) {
                nodeActions[i].setBrowser(this);
            }
        } else {
            this.nodeActions = EMPTY_ACTIONS;
        }        
        this.repositoryRoot = repositoryRoot;
        
        RepositoryPathNode rootNode = RepositoryPathNode.createRepositoryPathNode(this, repositoryRoot);                        
        Node[] selectedNodes = getSelectedNodes(rootNode, repositoryRoot, select);   
        getExplorerManager().setRootContext(rootNode);
        
        if(selectedNodes!=null) {
            try {
                getExplorerManager().setSelectedNodes(selectedNodes);    
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }    
        }
    }

    private Node[] getSelectedNodes(RepositoryPathNode rootNode, RepositoryFile repositoryRoot, RepositoryFile[] select) {        
        if(select==null || select.length <= 0) {
            return null;
        }
        Node segmentParentNode = null;        
        List nodesToSelect = new ArrayList(select.length);                                                           

        for (int i = 0; i < select.length; i++) {                
            String[] segments = select[i].getPathSegments();                
            segmentParentNode = rootNode;
            RepositoryFile segmentFile = repositoryRoot;
            for (int j = 0; j < segments.length; j++) {
                segmentFile = segmentFile.appendPath(segments[j]);
                RepositoryPathNode segmentNode = RepositoryPathNode.createRepositoryPathNode(this, segmentFile);
                segmentParentNode.getChildren().add(new Node[] {segmentNode});                
                segmentParentNode = segmentNode;
            }   
            nodesToSelect.add(segmentParentNode);                    
        }                
        return (Node[])nodesToSelect.toArray(new Node[nodesToSelect.size()]);                        
    }
    
    public void reset() {
        Node rootNode = getExplorerManager().getRootContext();
        if(rootNode != null) {
            getExplorerManager().setRootContext(Node.EMPTY);
            try {                
                // XXX is this enough ??? 
                rootNode.destroy(); 
                if(progressHandlers!=null) {
                    for (Iterator it = progressHandlers.iterator(); it.hasNext();) {
                        ProgressHandle ph = (ProgressHandle) it.next();
                        ph.finish();    
                    }
                    progressHandlers = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace(); // should not happen
            }            
        }
    }
    
    public List listRepositoryPath(final RepositoryPathNode.RepositoryPathEntry entry) throws SVNClientException {
        
        if(entry.getSvnNodeKind().equals(SVNNodeKind.FILE)) {
            return EMPTY_LIST; // nothing to do...
        }
        
        ProgressHandle ph = 
            ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(Browser.class, "BK2001"));       // NOI18N        
        ph.start();
        getProgressHandlers().add(ph);
        
        SvnClient client = Subversion.getInstance().getClient(this.repositoryRoot.getRepositoryUrl());
        
        List ret = new ArrayList();        
        try {            

            ISVNDirEntry[] dirEntries = client.getList(
                                        entry.getRepositoryFile().getFileUrl(), 
                                        entry.getRepositoryFile().getRevision(), 
                                        false);             

            if(dirEntries == null || dirEntries.length == 0) {
                ph.finish();
                return EMPTY_LIST; // nothing to do...
            }
            
            for (int i = 0; i < dirEntries.length; i++) {                            
                ISVNDirEntry dirEntry = dirEntries[i];
                if( dirEntry.getNodeKind()==SVNNodeKind.DIR || 
                    (dirEntry.getNodeKind()==SVNNodeKind.FILE && showFiles) ) 
                {
                    RepositoryFile repositoryFile = entry.getRepositoryFile();
                    ret.add(
                        new RepositoryPathNode.RepositoryPathEntry(
                            repositoryFile.appendPath(dirEntry.getPath()), 
                            dirEntry.getNodeKind())
                    );
                }                
            }        
        } finally {
            if(ph!=null) {
                ph.finish();
                getProgressHandlers().remove(ph);
            }            
        }

        return ret;
    }
    
    public JPanel getBrowserPanel() {
        return panel;
    }
    
    public Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    public RepositoryFile[] getSelectedFiles() {
        Node[] nodes = (Node[]) getExplorerManager().getSelectedNodes();
        
        if(nodes.length == 0) {
            return EMPTY_ROOT;
        }
        
        RepositoryFile[] ret = new RepositoryFile[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ret[i] = ((RepositoryPathNode) nodes[i]).getEntry().getRepositoryFile();
        }
        return ret;
    }
    
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {

            Node[] newSelection = (Node[]) evt.getNewValue();
            if(newSelection == null || newSelection.length == 0) {
                return;
            }

            // RULE: don't select the repository node
//            if(containsRootNode(newSelection)) {                
//                throw new PropertyVetoException("", evt); // NOI18N
//            }
            
            Node[] oldSelection = (Node[]) evt.getOldValue();                                    
            
            // RULE: don't select nodes on a different level as the already selected 
            if(oldSelection.length == 0 && newSelection.length == 1) {
                // it is first node selected ->
                // -> there is nothig to check
                return;
            }   
                                    
            if(oldSelection.length != 0 && areDisjunct(oldSelection, newSelection)) {
                // as if the first node would be selected ->
                // -> there is nothig to check
                return;
            }

            Node selectedNode = null;
            if(oldSelection.length > 0) {
                // we anticipate that nothing went wrong and
                // all nodes in the old selection are at the same level
                selectedNode = oldSelection[0];
            } else {
                selectedNode = newSelection[0];
            }
            if(!selectionIsAtLevel(newSelection, getNodeLevel(selectedNode))) {
                throw new PropertyVetoException("", evt); // NOI18N
            }

            if(fileSelectionOnly) {
                for (int i = 0; i < newSelection.length; i++) {
                    if(newSelection[i] instanceof RepositoryPathNode) {
                        RepositoryPathNode node = (RepositoryPathNode) newSelection[i];
                        if(node.getEntry().getSvnNodeKind() == SVNNodeKind.DIR) {
                            throw new PropertyVetoException("", evt); // NOI18N
                        }
                    }
                }
            }            
        }
    }    
    
    private boolean selectionIsAtLevel(Node[] newSelection, int level) {
        for (int i = 0; i < newSelection.length; i++) {
             if (getNodeLevel(newSelection[i]) != level)  {
                return false;
             }
        }        
        return true;
    }
    
    private boolean areDisjunct(Node[] oldSelection, Node[] newSelection) { 
        for (int i = 0; i < oldSelection.length; i++) {
            if(isInArray(oldSelection[i], newSelection)) {                
                return false;
            }
        }
        return true;
    }
    
    private int getNodeLevel(Node node) {
        int level = 0;
        while(node!=null) {
            node = node.getParentNode();
            level++;
        }
        return level;
    }
    
    private boolean isInArray(Node node, Node[] nodeArray) {
        for (int i = 0; i < nodeArray.length; i++) {
            if(node==nodeArray[i]) {
                return true;
            }
        }
        return false;
    }    
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().removePropertyChangeListener(listener);
    }
    
    ExplorerManager getExplorerManager() {        
        return panel.getExplorerManager();
    }

    public Action[] getActions() {
        return nodeActions;        
    }

    private ArrayList getProgressHandlers() {
        if(progressHandlers == null) {
            progressHandlers = new ArrayList(5);
        }
        return progressHandlers;
    }

    void setSelectedNodes(Node[] selection) throws PropertyVetoException {
        getExplorerManager().setSelectedNodes(selection);
    }
}
