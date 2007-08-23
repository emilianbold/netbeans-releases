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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.status;

import java.io.IOException;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.util.logging.Level;

/**
 * The node that is rendered in the SyncTable view. It gets values to display from the
 * CvsFileNode which serves as the 'data' node for this 'visual' node.
 * 
 * @author Maros Sandor
 */
public class SyncFileNode extends AbstractNode {
    
    private HgFileNode node;

    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_PATH        = "path"; // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    static final String COLUMN_NAME_BRANCH      = "branch"; // NOI18N
    
    private String htmlDisplayName;

    private RequestProcessor.Task repoload;

    private final VersioningPanel panel;

    public SyncFileNode(HgFileNode node, VersioningPanel _panel) {
        this(Children.LEAF, node, _panel);
        
    }

    private SyncFileNode(Children children, HgFileNode node, VersioningPanel _panel) {
        super(children, Lookups.fixed(node.getLookupObjects()));
        this.node = node;
        this.panel = _panel;
        initProperties();
        refreshHtmlDisplayName();
    }
    
    public File getFile() {
        return node.getFile();
    }

    public FileInformation getFileInformation() {
        return node.getInformation();
    }
    
    public String getName() {
        return node.getName();
    }

    public Action getPreferredAction() {
        // TODO: getPreferedAction
        if (node.getInformation().getStatus() == FileInformation.STATUS_VERSIONED_CONFLICT) {
            return null; //SystemAction.get(ResolveConflictsAction.class);
        }
        return new DiffAction(null, HgUtils.getCurrentContext(null));
    }

    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    public Cookie getCookie(Class klass) {
        FileObject fo = FileUtil.toFileObject(getFile());
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                if (fo.equals(dobj.getPrimaryFile())) {
                    return dobj.getCookie(klass);
                }
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
        return super.getCookie(klass);
    }

    private void initProperties() {
        if (node.getFile().isDirectory()) setIconBaseWithExtension("org/openide/loaders/defaultFolder.gif"); // NOI18N

        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new NameProperty());
        ps.put(new PathProperty());
        ps.put(new StatusProperty());
        ps.put(new BranchProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = node.getInformation(); 
        int status = info.getStatus();
        // Special treatment: Mergeable status should be annotated as Conflict in Versioning view according to UI spec
        if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            status = FileInformation.STATUS_VERSIONED_CONFLICT;
        }
        htmlDisplayName = Mercurial.getInstance().getMercurialAnnotator().annotateNameHtml(node.getFile().getName(), info, null);
        fireDisplayNameChange(node.getName(), node.getName());
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    private abstract class SyncFileProperty extends org.openide.nodes.PropertySupport.ReadOnly {

        protected SyncFileProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                Mercurial.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
    }
    
    private class BranchProperty extends SyncFileProperty {

        public BranchProperty() {
            super(COLUMN_NAME_BRANCH, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2001"), NbBundle.getMessage(SyncFileNode.class, "BK2002")); // NOI18N
        }

        public Object getValue() {
            String branchInfo = panel.getDisplayBranchInfo();
            return branchInfo == null ? "" : branchInfo; // NOI18N
        }
    }
    
    private class PathProperty extends SyncFileProperty {

        private String shortPath;

        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2003"), NbBundle.getMessage(SyncFileNode.class, "BK2004")); // NOI18N
            setValue("sortkey", "\u65000\t" + SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return shortPath = HgUtils.getRelativePath(node.getFile());
        }
    }

    // XXX it's not probably called, are there another Node lifecycle events
    public void destroy() throws IOException {
        super.destroy();
        if (repoload != null) {
            repoload.cancel();
        }
    }
    
    private class NameProperty extends SyncFileProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2005"), NbBundle.getMessage(SyncFileNode.class, "BK2006")); // NOI18N
            setValue("sortkey", SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return SyncFileNode.this.getDisplayName();
        }
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class StatusProperty extends SyncFileProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2007"), NbBundle.getMessage(SyncFileNode.class, "BK2008")); // NOI18N
            String shortPath = HgUtils.getRelativePath(node.getFile()); // NOI18N
            String sortable = Integer.toString(HgUtils.getComparableStatus(node.getInformation().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            FileInformation finfo =  node.getInformation();
            //TODO: finfo.getEntry(node.getFile());  // XXX not interested in return value, side effect loads ISVNStatus structure
            int mask = panel.getDisplayStatuses();
            return finfo.getStatusText(mask);
        }
    }
}
