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

import java.awt.Component;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.util.Collections;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.openide.ErrorManager;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
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
    private boolean repositoryFolder;

    private boolean isListed = false;
    
    static RepositoryPathNode createRepositoryPathNode(BrowserClient client, RepositoryFile file) {
        return createRepositoryPathNode(client, new RepositoryPathEntry(file, SVNNodeKind.DIR, new SVNRevision(0), null, ""));
    }          

    private static RepositoryPathNode createRepositoryPathNode(BrowserClient client, RepositoryPathEntry entry) {        
        RepositoryPathNode node = new RepositoryPathNode(client, entry, true);
        return node;
    }    

    static RepositoryPathNode createPreselectedPathNode(BrowserClient client, RepositoryFile file) {
        return createDelayedExpandNode(client, file);
    }

    static RepositoryPathNode createRepositoryRootNode(BrowserClient client, RepositoryFile file) {
        return createDelayedExpandNode(client, file);
    }
    
    private static RepositoryPathNode createDelayedExpandNode(BrowserClient client, RepositoryFile file) {
        return new DelayedExpandNode(client, new RepositoryPathEntry(file, SVNNodeKind.DIR, new SVNRevision(0), null, ""), false);
    }
    
    static RepositoryPathNode createNewBrowserNode(BrowserClient client, RepositoryPathEntry entry) {
        return new NewBrowserNode(client, entry, false);
    }            
    
    private RepositoryPathNode(BrowserClient client, RepositoryPathEntry entry, boolean repositoryFolder) {
        super(entry.getSvnNodeKind() == SVNNodeKind.DIR ? new RepositoryPathChildren(client) : Children.LEAF);
        this.entry = entry;
        this.client = client;
        this.repositoryFolder = repositoryFolder;        
        
        if(entry.getSvnNodeKind() == SVNNodeKind.DIR) {
            setIconBaseWithExtension("org/openide/loaders/defaultFolder.gif");       // NOI18N
        } else {
            setIconBaseWithExtension("org/openide/loaders/defaultFile.gif");         // NOI18N    
        }
        initProperties();
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
                
        ps.put(new RevisionProperty());
        ps.put(new DateProperty());
        ps.put(new AuthorProperty());
        ps.put(new HistoryProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }   
    
    public String getDisplayName() {
        return getName();
    }

    public String getName() {        
        if(entry.getRepositoryFile().isRepositoryRoot()) {
            return entry.getRepositoryFile().getRepositoryUrl().toString();
        } else {
            return entry.getRepositoryFile().getName();   
        }        
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
                        node.entry.getSvnNodeKind(),
                        node.entry.getLastChangedRevision(),
                        node.entry.getLastChangedDate(),
                        node.entry.getLastChangedAuthor()
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
    
    /**
     * List the repository path from entry and sets up the Nodes children with the retrieved values
     */ 
    void expand() {         
        if(isListed) {
            return; 
        }
        isListed = true;
        Children ch = getChildren();
        if(ch instanceof RepositoryPathChildren) {
            ((RepositoryPathChildren) getChildren()).listRepositoryPath(entry);                                       
        }        
    }
    
    private static class RepositoryPathChildren extends Children.Keys {

        private RequestProcessor.Task task;

        private final BrowserClient client;

        private Node[] previousNodes = null;
        
        public RepositoryPathChildren(BrowserClient client) {
            this.client = client;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
        }
        
        @Override
        protected void removeNotify() {
            task.cancel();
            setKeys(Collections.EMPTY_LIST);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Node) {
                return new Node[] {(Node) key};
            }
            
            RepositoryPathEntry entry = (RepositoryPathEntry) key;                        
            Node node = this.findChild(entry.getRepositoryFile().getName());
            if(node != null) {
                return null;
            }
            
            // reuse nodes 
            if(previousNodes != null) {
                for(Node n : previousNodes) {
                    if(n instanceof RepositoryPathNode) {
                        if(((RepositoryPathNode)n).entry.getRepositoryFile().getName().equals(entry.getRepositoryFile().getName())) {
                            return new Node[] {n};
                        }
                    }
                }
            }
            
            Node pathNode = RepositoryPathNode.createRepositoryPathNode(client, entry);
            return new Node[] {pathNode};
        }

        public void listRepositoryPath(final RepositoryPathEntry pathEntry) {
            
            previousNodes = getNodes();
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2001")); // NOI18N
            setKeys(Collections.singleton(waitNode));                        
            
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(pathEntry.getRepositoryFile().getRepositoryUrl());
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    try {                                                
                        Collection<RepositoryPathEntry> listedEntries = client.listRepositoryPath(pathEntry, this);
                        if(isCanceled()) {
                            return;
                        }

                        Collection<RepositoryPathEntry> entries = getPreviousNodeEntries();
                        if(listedEntries == null) {
                            // is not a folder in the repository
                            RepositoryPathNode node = (RepositoryPathNode) getNode();
                            node.setRepositoryFolder(false);                                       
                        } else {
                            if(!isCreativeBrowser(client)) {
                                removePreselectedFolders(listedEntries);                                
                            }                               
                           
                            // keep nodes which were created in the browser
                            Collection<RepositoryPathEntry> accepptedEntries = new ArrayList<RepositoryPathEntry>();
                            for(RepositoryPathEntry listedEntry : listedEntries) {
                                boolean found = false;
                                for(RepositoryPathEntry entry : entries) {
                                    if(entry.getRepositoryFile().getName().equals(listedEntry.getRepositoryFile().getName())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if(!found) {
                                    accepptedEntries.add(listedEntry);
                                }
                            }
                            entries.addAll(accepptedEntries);        
                        }
                        setKeys(entries);                            
                        
                    } catch (SVNClientException ex) {
                        Collection entries = getPreviousNodeEntries();
                        if(entries.size() > 0) {                            
                            setKeys(entries);
                        } else {
                            setKeys(Collections.singleton(errorNode(ex)));                            
                        }
                        return;
                    } finally {
                        previousNodes = null;
                    }
                }
            };
            support.start(rp, pathEntry.getRepositoryFile().getRepositoryUrl(), org.openide.util.NbBundle.getMessage(Browser.class, "BK2001")); // NOI18N
        }

        private Collection<RepositoryPathEntry> getPreviousNodeEntries() {
            List<RepositoryPathEntry> l = new ArrayList<RepositoryPathEntry>();
            if(previousNodes != null) {
                for(Node node : previousNodes) {
                    if(node instanceof RepositoryPathNode) {
                        l.add( ((RepositoryPathNode)node).entry);    
                    }                                
                }
            }            
            return l;
        }
        
        private String getLastPathSegment(RepositoryPathEntry entry) {            
            String[] childSegments = entry.getRepositoryFile().getPathSegments();
            return childSegments.length > 0 ? childSegments[childSegments.length-1] : null;            
        }
        
        private static Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2002")); // NOI18N
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }    
        
        private boolean isCreativeBrowser(BrowserClient client) {
            Action[] actions = client.getActions();
            for (int i = 0; i < actions.length; i++) {
                if(actions[i] instanceof CreateFolderAction) {
                    return true;
                }
            }
            return false;
        }
        
        private void removePreselectedFolders(final Collection cl) {
            Node[] childNodes = getNodes();
            for(int i=0; i < childNodes.length; i++) {
                if(childNodes[i] instanceof RepositoryPathNode) {
                    String lastChildSegment = getLastPathSegment( ((RepositoryPathNode) childNodes[i]).getEntry() );                                
                    if(lastChildSegment!=null) {
                        boolean pathExists = false;
                        for(Iterator it = cl.iterator(); it.hasNext(); ) {
                            String lastNewChildSegment = getLastPathSegment((RepositoryPathEntry) it.next());     
                            if(lastNewChildSegment!=null) {
                                if(lastNewChildSegment.equals(lastChildSegment)) {
                                    pathExists = true;
                                    break;
                                }
                            }
                        }
                        if(!pathExists) {                                            
                            remove(new Node[] { childNodes[i] });
                        }
                    }
                }
            }
        }

    }    
        
    static final String PROPERTY_NAME_REVISION = "revision";    // NOI18N    
    static final String PROPERTY_NAME_DATE     = "date";        // NOI18N    
    static final String PROPERTY_NAME_AUTHOR   = "author";      // NOI18N    
    static final String PROPERTY_NAME_HISTORY  = "history";     // NOI18N    

    private final static String HISTORY_DISPLAY_NAME = org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "LBL_BrowserTree_History_Name");
    private final static String HISTORY_SHORT_DESC = org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "LBL_BrowserTree_History_Short_Desc");    
           
    private class RevisionProperty extends NodeProperty {
        public RevisionProperty() {
            super(PROPERTY_NAME_REVISION, String.class, PROPERTY_NAME_REVISION, PROPERTY_NAME_REVISION);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return entry.getLastChangedRevision();
        }
    }

    private class DateProperty extends NodeProperty<String> {

        public DateProperty() {
            super(PROPERTY_NAME_DATE, String.class, PROPERTY_NAME_DATE, PROPERTY_NAME_DATE);
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            Date date = entry.getLastChangedDate();
            return date != null ? DateFormat.getDateTimeInstance().format(date) : "";
        }
    }

    private class AuthorProperty extends NodeProperty<String> {
                
        public AuthorProperty() {
            super(PROPERTY_NAME_AUTHOR, String.class, PROPERTY_NAME_AUTHOR, PROPERTY_NAME_AUTHOR);
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return entry.getLastChangedAuthor();
        }
    }

    private class HistoryProperty extends PropertySupport.ReadOnly<String> {
       
        public HistoryProperty() {
            super(PROPERTY_NAME_HISTORY, String.class, HISTORY_DISPLAY_NAME, HISTORY_SHORT_DESC);
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return "";
        }        
        
        public String toString() {            
            try {
                Object obj = getValue();               
                return obj != null ? obj.toString() : "";
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }           
        
        public PropertyEditor getPropertyEditor() {
            return new HistoryPropertyEditor();
        }                   
    }
                            
    private abstract class NodeProperty<T> extends PropertySupport.ReadOnly<T> {        
        protected NodeProperty(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public String toString() {            
            try {
                Object obj = getValue();               
                return obj != null ? obj.toString() : "";
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }

        public boolean canWrite() {
            return false;                    
        }     

        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport();
        }                     
    }

    private class HistoryPropertyEditor extends PropertyEditorSupport {
       
        public HistoryPropertyEditor() {      
            setValue("");           
        }        
        
        public boolean supportsCustomEditor () {
            return true;
        }
        
        public Component getCustomEditor() {
            SVNRevision revision = entry.getLastChangedRevision();
            SVNUrl repositoryUrl = entry.getRepositoryFile().getRepositoryUrl();
            SVNUrl fileUrl = entry.getRepositoryFile().getFileUrl();
            final SvnSearch svnSearch = new SvnSearch(new RepositoryFile(repositoryUrl, fileUrl, revision));        
            return svnSearch.getSearchPanel();
        }
    }

    static class RepositoryPathEntry {
        private final SVNNodeKind svnNodeKind;
        private final RepositoryFile file;
        private final SVNRevision revision;        
        private Date date;
        private final String author;
        RepositoryPathEntry (RepositoryFile file, SVNNodeKind svnNodeKind, SVNRevision revision, Date date, String author) {
            this.svnNodeKind = svnNodeKind;
            this.file = file;
            this.revision = revision;
            this.date = date;
            this.author = author;
        }
        public SVNNodeKind getSvnNodeKind() {
            return svnNodeKind;
        }
        RepositoryFile getRepositoryFile() {
            return file;
        }        
        SVNRevision getLastChangedRevision() {
            return revision;
        }       
        Date getLastChangedDate() {
            return date;
        }               
        String getLastChangedAuthor() {
            return author;
        }                       
    }        

    /**
     * Created in the Browser. Never lists it's children from the repository as they don't exist
     */ 
    private static class NewBrowserNode extends RepositoryPathNode {
        public NewBrowserNode(BrowserClient client, RepositoryPathEntry entry, boolean repositoryFolder) {
            super(client, entry, repositoryFolder);
        }       
        @Override
        void expand() {
             // do nothing
        }
    }

    /**
     * Lists it's children from the repository after the second expand in the browser
     */ 
    private static class DelayedExpandNode extends RepositoryPathNode {
        private final int IGNORE_EXPANDS = 1;
        private int expanded = 0;    
        public DelayedExpandNode(BrowserClient client, RepositoryPathEntry entry, boolean repositoryFolder) {
            super(client, entry, repositoryFolder);            
        }       
        @Override
        void expand() {
            try {
                if(expanded < IGNORE_EXPANDS) {
                    return; 
                }
                super.expand();
            } finally {
                if(expanded <= IGNORE_EXPANDS) {
                    ++expanded;
                }
            }        
        }
    }
    
}
