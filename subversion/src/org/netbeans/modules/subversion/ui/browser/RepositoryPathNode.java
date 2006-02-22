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

import org.netbeans.modules.subversion.RepositoryFile;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.util.Collections;
import java.awt.*;
import java.beans.BeanInfo;
import java.util.Collection;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Represents a path in the repository.
 *
 * @author Tomas Stupka
 *
 */
public class RepositoryPathNode extends AbstractNode {
    
    private RepositoryPathEntry entry;
    private final BrowserClient client;
    private String name = null;    
    private final static Node[] EMPTY_NODES = new Node[0];        

    static class RepositoryPathEntry {
        private final SVNNodeKind svnNodeKind;
        private final RepositoryFile file;
        RepositoryPathEntry (RepositoryFile file, SVNNodeKind svnNodeKind) {
            this.svnNodeKind = svnNodeKind;
            this.file = file;
        }
        public SVNNodeKind getSvnNodeKind() {
            return svnNodeKind;
        }
        RepositoryFile getRepositoryFile() {
            return file;
        }        
    }    
    
    static RepositoryPathNode createRepositoryPathNode(BrowserClient client, RepositoryFile file) {
        return createRepositoryPathNode(client, new RepositoryPathEntry(file, SVNNodeKind.DIR));
    }   
    
    static RepositoryPathNode createRepositoryPathNode(BrowserClient client, RepositoryPathEntry entry) {
        RepositoryPathChildren kids = new RepositoryPathChildren(client, entry);
        RepositoryPathNode node = new RepositoryPathNode(kids, client, entry);        
        return node;
    }

    static RepositoryPathNode createBrowserPathNode(BrowserClient client, RepositoryPathEntry entry) {
        RepositoryPathNode node = new RepositoryPathNode(new Children.Array(), client, entry);        
        return node;
    }
            
    private RepositoryPathNode(Children children, BrowserClient client, RepositoryPathEntry entry) {
        super(children);
        this.entry = entry;
        this.client = client;
        
        if(entry.getSvnNodeKind()==SVNNodeKind.DIR){
            setIconBaseWithExtension("org/netbeans/modules/subversion/ui/wizards/browser/defaultFolder.gif");       // NOI18N
        } else {
            // XXX 
        }
    }

    public String getDisplayName() {
        return getName();
    }

    public String getName() {        
        return entry.getRepositoryFile().getName();
    }

    public void setName(String name) {
        entry = new RepositoryPathEntry(
                    entry.getRepositoryFile().replaceLastSegment(name), 
                    entry.getSvnNodeKind()
                );
    }
    
    public Image getIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image) UIManager.get("Nb.Explorer.Folder.icon");                                         // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }       

    public Image getOpenedIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image) UIManager.get("Nb.Explorer.Folder.openedIcon");                                   // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    public Action[] getActions(boolean context) {
        return client.getActions();
    }

    public RepositoryPathEntry getEntry() {
        return entry;
    }

    public BrowserClient getClient() {
        return client;
    }       

    public boolean canRename() {
        return !(getChildren() instanceof RepositoryPathChildren); // XXX implicit logic
    }
    
    private static class RepositoryPathChildren extends Children.Keys implements Runnable {

        private RequestProcessor.Task task;

        private final RepositoryPathEntry pathEntry;
        private final BrowserClient client;

        public RepositoryPathChildren(BrowserClient client, RepositoryPathEntry pathEntry) {
            this.client = client;
            this.pathEntry = pathEntry;
        }

        protected void addNotify() {
            super.addNotify();
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2001")); // NOI18N
            setKeys(Collections.singleton(waitNode));
            RequestProcessor rp = RequestProcessor.getDefault();
            task = rp.post(this);
        }

        protected void removeNotify() {
            task.cancel();
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Node) {
                return new Node[] {(Node)key};
            }
            
            RepositoryPathEntry entry = (RepositoryPathEntry) key;                        
            Node node = this.findChild(entry.getRepositoryFile().getName());
            if(node != null) {
                //return new Node[] {node};
                return null;
            }
            Node pathNode = RepositoryPathNode.createRepositoryPathNode(client, entry);
            return new Node[] {pathNode};
        }

        public void run() {
            try {
                setKeys(client.listRepositoryPath(pathEntry));                
            } catch (SVNClientException ex) {
                org.openide.ErrorManager.getDefault().notify(ex);                
                setKeys(Collections.singleton(errorNode(ex)));
                return;
            }  
        }

        private static Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2002")); // NOI18N
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }    
    }    
}
