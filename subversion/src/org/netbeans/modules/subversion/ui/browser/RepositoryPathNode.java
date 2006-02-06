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
import org.netbeans.modules.subversion.SVNRoot;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.beans.BeanInfo;
import java.util.Set;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Represents a path in the repository.
 *
 * @author Tomas Stupka
 *
 */
public class RepositoryPathNode extends AbstractNode {
    
    private final RepositoryPathEntry entry;
    private final SVNClient client;
    
    interface SVNClient {        
        public List listRepositoryPath(RepositoryPathEntry entry) throws SVNClientException;
        public void createFolder(SVNRoot svnRoot, String name, String message) throws SVNClientException;
        public boolean isReadOnly();
    }

    static class RepositoryPathEntry {
        private final SVNNodeKind svnNodeKind;
        private final SVNRoot svnRoot;
        RepositoryPathEntry (SVNRoot svnRoot, SVNNodeKind svnNodeKind) {
            this.svnNodeKind = svnNodeKind;
            this.svnRoot = svnRoot;
        }
        public SVNNodeKind getSvnNodeKind() {
            return svnNodeKind;
        }
        SVNRoot getSvnRoot() {
            return svnRoot;
        }
    }    
    
    public static RepositoryPathNode create(SVNClient client, SVNRoot svnRoot) {
        return create(client, new RepositoryPathEntry(svnRoot, SVNNodeKind.DIR));
    }
    
    private static RepositoryPathNode create(SVNClient client, RepositoryPathEntry entry) {
        RepositoryPathChildren kids = new RepositoryPathChildren(client, entry);
        RepositoryPathNode node = new RepositoryPathNode(kids, client, entry);
        node.setDisplayName(entry.getSvnRoot().getSvnUrl().getLastPathSegment()); // NOI18N
        return node;
    }

    private RepositoryPathNode(Children children, SVNClient client, RepositoryPathEntry entry) {
        super(children);
        this.entry = entry;
        this.client = client;
        
        if(entry.getSvnNodeKind()==SVNNodeKind.DIR){
            setIconBaseWithExtension("org/netbeans/modules/subversion/ui/browser/defaultFolder.gif");       // NOI18N
        }        
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
        if(entry.getSvnNodeKind()==SVNNodeKind.DIR) {
            return new Action[] { new CreateFolderAction() };
        }
        return new Action[0];
    }

    public RepositoryPathEntry getEntry() {
        return entry;
    }

    public SVNClient getClient() {
        return client;
    }
    
    private static class RepositoryPathChildren extends Children.Keys implements Runnable {

        private RequestProcessor.Task task;
        private final RepositoryPathEntry pathEntry;
        private final SVNClient client;
        
        public RepositoryPathChildren(SVNClient client, RepositoryPathEntry pathEntry) {
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
            Node pathNode = RepositoryPathNode.create(client, (RepositoryPathEntry) key);
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
        
        public void setKeys(List list) {
            super.setKeys(list);
        }
        
        public void setKeys(Set set) {
            super.setKeys(set);
        }
        
    }

    private class CreateFolderAction extends AbstractAction {
        public CreateFolderAction() {
           putValue(Action.NAME, org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Action_MakeDir")); // NOI18N
        }
        public void actionPerformed(ActionEvent e) {
            try {
                client.createFolder(entry.getSvnRoot(), 
                                    entry.getSvnRoot().getSvnUrl().getLastPathSegment() + "newdir", // NOI18N // XXX new dir name ???
                                    "message");                                        // NOI18N // XXX message ???   
            } catch (SVNClientException ex) {
                org.openide.ErrorManager.getDefault().notify(ex);                                
                ((RepositoryPathChildren)RepositoryPathNode.this.getChildren()).setKeys(     // XXX this is a mess !!!
                        Collections.singleton(RepositoryPathChildren.errorNode(ex)) );
                return;
           }                                        
        }
        public boolean isEnabled() {
            return !client.isReadOnly() && entry.getSvnNodeKind() == SVNNodeKind.DIR;
        }
    }    
}
