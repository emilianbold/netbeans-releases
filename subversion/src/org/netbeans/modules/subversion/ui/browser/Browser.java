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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.SVNRoot;
import org.netbeans.modules.subversion.ui.checkout.Executor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class Browser implements VetoableChangeListener, RepositoryPathNode.SVNClient {
        
    private final BrowserPanel panel;
    
    private static final SVNRoot[] EMPTY_ROOT = new SVNRoot[0];
    private static final List EMPTY_LIST = new ArrayList(0);
    
    private boolean showFiles = false;
    private boolean writeable = false;
    private boolean singleSelectionOnly = false;
    
    private ProgressHandle progressHandle;
    
    private SVNRoot svnRoot;
    
    /** Creates a new instance of BrowserSelector */
    public Browser(String title) 
    {                                
        panel = new BrowserPanel(title,           
                                 org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSN_RepositoryTree"),         // NOI18N
                                 org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSD_RepositoryTree"),         // NOI18N
                                 singleSelectionOnly);
        
        panel.getExplorerManager().addVetoableChangeListener(this);        
    }   

    public void setup(SVNRoot svnRoot) 
    {
        this.svnRoot = svnRoot;
        panel.getExplorerManager().setRootContext(RepositoryPathNode.create(this, svnRoot));       
    }

    public void reset() {
        Node rootNode = panel.getExplorerManager().getRootContext();
        if(rootNode != null) {
            try {                
                // XXX is this enough ???
                rootNode.destroy(); 
                if(progressHandle!=null) {
                    progressHandle.finish();
                }
            } catch (IOException ex) {
                ex.printStackTrace(); // should not happen
            }
            panel.getExplorerManager().setRootContext(Node.EMPTY);
        }
    }
    
    public List listRepositoryPath(final RepositoryPathNode.RepositoryPathEntry entry) throws SVNClientException {
        
        if(entry.getSvnNodeKind().equals(SVNNodeKind.FILE)) {
            return EMPTY_LIST; // nothing to do...
        }
        
        progressHandle = 
            ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2001"));       // NOI18N        
        progressHandle.start();
            
        List ret = new ArrayList();
        try {
            final List dirEntryList = new ArrayList();

            Executor.Command cmd = new Executor.Command() {
                protected void executeCommand(ISVNClientAdapter client) throws SVNClientException {
                    ISVNDirEntry[] dirEntry = client.getList(entry.getSvnRoot().getSvnUrl(), entry.getSvnRoot().getSVNRevision(), false);
                    for (int i = 0; i < dirEntry.length; i++) {
                       dirEntryList.add(dirEntry[i]);
                    }
                }            
            };
            Executor.getInstance().execute(cmd);

            if(dirEntryList==null) {
                progressHandle.finish();
                return null;
            }
            
            for (Iterator it = dirEntryList.iterator(); it.hasNext();) {
                ISVNDirEntry dirEntry = (ISVNDirEntry) it.next();
                if( dirEntry.getNodeKind()==SVNNodeKind.DIR || 
                    (dirEntry.getNodeKind()==SVNNodeKind.FILE && showFiles) ) 
                {
                    SVNRoot root = entry.getSvnRoot();
                    SVNUrl svnUrl = root.getSvnUrl().appendPath(dirEntry.getPath());                                        
                    ret.add(new RepositoryPathNode.RepositoryPathEntry(new SVNRoot(svnUrl, root.getSVNRevision()), dirEntry.getNodeKind()));
                }                
            }        
        } finally {
            if(progressHandle!=null) {
                progressHandle.finish();
                progressHandle = null;    
            }            
        }

        return ret;
    }
    
    public JPanel getBrowserPanel() {
        return panel;
    }
    
    public Node[] getSelectedNodes() {
        return panel.getExplorerManager().getSelectedNodes();
    }

    public SVNRoot[] getSelectedRoots() {
        Node[] nodes = (Node[]) panel.getExplorerManager().getSelectedNodes();
        
        if(nodes.length == 0) {
            return EMPTY_ROOT;
        }
        
        SVNRoot[] ret = new SVNRoot[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ret[i] = ((RepositoryPathNode) nodes[i]).getEntry().getSvnRoot();
        }
        return ret;
    }
    
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {

            Node[] newSelection = (Node[]) evt.getNewValue();
                        
            // RULE: don't select the repository node
            if(containsRootNode(newSelection)) {                
                throw new PropertyVetoException("", evt); // NOI18N
            }
            
            Node[] oldSelection =  (Node[]) evt.getOldValue();                                    
            
            // RULE: don't select nodes on a different level as the already selected 
            if(oldSelection.length == 0) {
                // it is first node selected ->
                // -> there is nothig to check
                return;
            }   
                                    
            if(areDisjunct(oldSelection, newSelection)) {
                // as if the first node would be selected ->
                // -> there is nothig to check
                return;
            }
            
            // we anticipate that nothing went wrong and            
            // all nodes in the old selection are at the same level
            Node selectedNode = oldSelection[0];   
                                                   
            if(!selectionIsAtLevel(newSelection, getNodeLevel(selectedNode))) {
                throw new PropertyVetoException("", evt); // NOI18N
            }
            
        }
    }
    
    private boolean containsRootNode(Node[] selection) {
        for (int i = 0; i < selection.length; i++) {
            if(selection[i] == panel.getExplorerManager().getRootContext()) {
                return true; 
            }
        }        
        return false;
    }

    private boolean nodesOnDifferentLevelsAreSelected(Node[] selection) {
        for (int i = 0; i < selection.length; i++) {
            if(selection[i] == panel.getExplorerManager().getRootContext()) {
                return true; 
            }
        }        
        return false;
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
        
    public void createFolder(final SVNRoot svnRoot, final String name, final String message) {
        Executor.Command cmd = new Executor.Command () {
            protected void executeCommand(ISVNClientAdapter client) throws SVNClientException {                
                client.mkdir(svnRoot.getSvnUrl().appendPath(name), message); 
            }
        };
        try {
            Executor.getInstance().execute(cmd);
        } catch (SVNClientException ex) {
            org.openide.ErrorManager.getDefault().notify(ex);                
        }        
    }

    public boolean isReadOnly() {
        return !writeable;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        panel.getExplorerManager().addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        panel.getExplorerManager().removePropertyChangeListener(listener);
    }

    public void setShowFiles(boolean showFiles) {
        this.showFiles = showFiles;
    }

    public void setWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    public void setSingleSelectionOnly(boolean singleSelectionOnly) {
        this.singleSelectionOnly = singleSelectionOnly;
    }

    
}
