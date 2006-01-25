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

import java.net.MalformedURLException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.beans.BeanInfo;
import java.util.ArrayList;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Represents a path in repository, its children
 * subfolders.
 *
 * <p>Lookup contains a string identifing represented url.
 *
 * @author Tomas Stupka
 *
 */
public class RepositoryPathNode extends AbstractNode {

    private final static Node[] EMPTY = new Node[0];
    private final SVNUrl svnURL;

    private RequestProcessor requestProcessor;
    
    public static RepositoryPathNode create(ISVNClientAdapter svnClient, SVNUrl svnURL) {
        RepositoryPathChildren kids = new RepositoryPathChildren(svnClient, svnURL);
        Lookup lookup = Lookups.singleton(svnURL); 
        RepositoryPathNode node = new RepositoryPathNode(kids, lookup, svnURL);

        node.setDisplayName(svnURL.getLastPathSegment() + " ...");
        annotate(node, svnClient, svnURL);
        return node;
    }

    private static void annotate(final Node node, final ISVNClientAdapter svnClient, final SVNUrl svnURL) {
        RequestProcessor requestProcessor = new RequestProcessor("BrowserPanel", 1, true);
        Runnable r = new Runnable() {
            public void run() {
                ISVNLogMessage[] messages = null;
                try {                
                    messages = svnClient.getLogMessages(svnURL, SVNRevision.HEAD, SVNRevision.HEAD);
                } catch (SVNClientException ex) {
                    ex.printStackTrace();
                    // XXX message error node ???
                }
                if(messages==null || messages.length == 0) { 
                    // XXX message error node ???
                } else {
                    StringBuffer displaName = new StringBuffer();
                    displaName.append(svnURL.getLastPathSegment());
                    displaName.append(" [");
                    displaName.append(messages[0].getRevision());
                    displaName.append(" ");
                    displaName.append(messages[0].getAuthor());
                    displaName.append("]");
                    node.setDisplayName(displaName.toString());
                }
            }
        };
        requestProcessor.post(r);
    }
    
    private RepositoryPathNode(Children children, Lookup lookup, SVNUrl svnURL) {
        super(children, lookup);
        this.svnURL = svnURL;
        setIconBaseWithExtension("org/netbeans/modules/subversion/ui/browser/defaultFolder.gif"); // NOI18N
    }

    public Image getIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image) UIManager.get("Nb.Explorer.Folder.icon");  // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    public Image getOpenedIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image) UIManager.get("Nb.Explorer.Folder.openedIcon");  // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    SVNUrl getSVNUrl() {
        return svnURL;
    }
    
    private static class RepositoryPathChildren extends Children.Keys implements Runnable {

        private final ISVNClientAdapter svnClient;
        private final SVNUrl svnURL;
        private RequestProcessor.Task task;

        public RepositoryPathChildren(ISVNClientAdapter svnClient, SVNUrl svnURL) {
            this.svnClient = svnClient;
            this.svnURL = svnURL;
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
            SVNUrl newSVNURL;
            try {                
                newSVNURL = new SVNUrl(svnURL.toString() + // XXX HACK ???
                                       "/" + // NOI18N
                                       (String) key); 
            } catch (MalformedURLException ex) {
                ex.printStackTrace(); // should not happen
                return EMPTY;
            }
            
            Node pathNode = RepositoryPathNode.create(svnClient, newSVNURL);
            return new Node[] {pathNode};
        }

        public void run() {
            List keys;
            try {
                keys = listRepositoryPath(svnClient, svnURL);
            } catch (SVNClientException ex) {
                ex.printStackTrace(); // XXX notify ???
                setKeys(Collections.singleton(errorNode(ex)));
                return;
            }
            setKeys(keys);
        }

        private Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2002")); // NOI18N
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }
        
        private List listRepositoryPath(ISVNClientAdapter svnClient, SVNUrl root) throws SVNClientException {
            List ret = new ArrayList();
            ISVNDirEntry[] dirEntry = null;
            dirEntry = svnClient.getList(root, SVNRevision.HEAD, false);
            
            if(dirEntry==null) {
                return null;
            }
            
            for (int i = 0; i < dirEntry.length; i++) { 
                if(dirEntry[i].getNodeKind()==SVNNodeKind.DIR) {
                    ret.add(dirEntry[i].getPath());
                }                
            }
            return ret;
        }       
    }
}
