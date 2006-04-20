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
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.util.Collections;
import java.awt.*;
import java.beans.BeanInfo;
import java.util.Collection;
import org.netbeans.modules.subversion.Subversion;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;

/**
 * Represents a path in the repository.
 *
 * @author Tomas Stupka
 *
 */
public class RepositoryPathNode extends AbstractNode {
    
    private RepositoryPathEntry entry;
    private final BrowserClient client;    
    private boolean repositoryFolder;
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
        RepositoryPathNode node = new RepositoryPathNode(kids, client, entry, true);
        return node;
    }

    static RepositoryPathNode createBrowserPathNode(BrowserClient client, RepositoryPathEntry entry) {
        RepositoryPathNode node = new RepositoryPathNode(new Children.Array(), client, entry, false);
        return node;
    }
            
    private RepositoryPathNode(Children children, BrowserClient client, RepositoryPathEntry entry, boolean repositoryFolder) {
        super(children);
        this.entry = entry;
        this.client = client;
        this.repositoryFolder = repositoryFolder;

        if(entry.getSvnNodeKind()==SVNNodeKind.DIR){
            setIconBaseWithExtension("org/netbeans/modules/subversion/ui/browser/defaultFolder.gif");       // NOI18N
        } else {
            setIconBaseWithExtension("org/netbeans/modules/subversion/ui/browser/defaultFile.gif");         // NOI18N    
        }
    }

    public String getDisplayName() {
        return getName();
    }

    public String getName() {        
        return entry.getRepositoryFile().getName();
    }

    public void setName(String name) {
        String oldName = getName();
        if(!oldName.equals(name)) {
            renameNode (this, name, 0);
            this.fireNameChange(oldName, name);
        }                
    }

    private void renameNode (RepositoryPathNode node, String newParentsName, int level) {        
        node.entry = new RepositoryPathEntry(
                        node.entry.getRepositoryFile().replaceLastSegment(newParentsName, level),
                        node.entry.getSvnNodeKind()
                    );
        Children childern = node.getChildren();
        Node[] childernNodes = childern.getNodes();
        level++;
        for (int i = 0; i < childernNodes.length; i++) {
            if(childernNodes[i] instanceof RepositoryPathNode) {
                renameNode((RepositoryPathNode) childernNodes[i], newParentsName, level);
            }            
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
        return client.getActions();
    }

    public RepositoryPathEntry getEntry() {
        return entry;
    }

    public BrowserClient getClient() {
        return client;
    }       

    public boolean canRename() {
        return !repositoryFolder;
    }

    private void setRepositoryFolder(boolean bl) {
        repositoryFolder = bl;
    }
    
    private static class RepositoryPathChildren extends Children.Keys {

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
            listRepositoryPath();
        }

        protected void removeNotify() {
            task.cancel();
            setKeys(Collections.EMPTY_LIST);
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

        public void listRepositoryPath() {
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(pathEntry.getRepositoryFile().getRepositoryUrl());
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {
                    try {
                        Collection cl = client.listRepositoryPath(pathEntry, this);
                        if(cl == null) {
                            // is not a folder in the repository
                            setKeys(Collections.EMPTY_LIST);
                            RepositoryPathNode node = (RepositoryPathNode) getNode();
                            node.setRepositoryFolder(false);
                        } else {
                            setKeys(cl);
                        }
                    } catch (SVNClientException ex) {
                        setKeys(Collections.singleton(errorNode(ex)));
                        return;
                    }
                }
            };
            support.start(rp, org.openide.util.NbBundle.getMessage(Browser.class, "BK2001"));
        }

        private static Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2002")); // NOI18N
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }    
    }    
}
