/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ResolveConflictsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.DeleteLocalAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.status.StatusAction;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.io.File;

/**
 * The node that is rendered in the SyncTable view. It gets values to display from the
 * CvsFileNode which serves as the 'data' node for this 'visual' node.
 * 
 * @author Maros Sandor
 */
public class SyncFileNode extends AbstractNode {
    
    private CvsFileNode node;

    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_PATH        = "path"; // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    static final String COLUMN_NAME_STICKY      = "sticky"; // NOI18N
    
    private String htmlDisplayName;
    private String sticky;

    public SyncFileNode(CvsFileNode node) {
        this(Children.LEAF, node);
    }

    private SyncFileNode(Children children, CvsFileNode node) {
        super(children, Lookups.fixed(node.getLookupObjects()));
        this.node = node;
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
        if (node.getInformation().getStatus() == FileInformation.STATUS_VERSIONED_CONFLICT) {
            return SystemAction.get(ResolveConflictsAction.class);
        }
        return SystemAction.get(DiffAction.class);
    }

    // XXX see SyncTable getPopup that ignores this
    public Action[] getActions(boolean context) {
        if (context) return new Action[0];
        Action[] actions = Annotator.getActions();

        // customize - replace Show status by Delete
        for(int  i = 0;  i < actions.length; i++) {
            Action action = actions[i];
            if (action instanceof StatusAction) {
                actions[i] = SystemAction.get(DeleteLocalAction.class);
                break;
            }
        }

        return actions;
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
        ps.put(new StickyProperty());
        
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
        htmlDisplayName = CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(node.getFile().getName(), info, null);
        fireDisplayNameChange(node.getName(), node.getName());
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    /**
     * Gets sticky information for the given file. Sticky info does not include any leading specifier (T, D). 
     * 
     * @return String branch,tag,date sticky info or an empty String, never returns null
     */ 
    public String getSticky() {
        if (sticky == null) {
            if ((sticky = Utils.getSticky(node.getFile())) == null) {
                sticky = ""; // NOI18N
            } else {
                sticky = sticky.substring(1);
            }
        }
        return sticky == null || sticky.length() == 0 ? "" : sticky; // NOI18N
    }

    private abstract class SyncFileProperty extends PropertySupport.ReadOnly {

        protected SyncFileProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }
    }
    
    private class StickyProperty extends SyncFileProperty {

        public StickyProperty() {
            super(COLUMN_NAME_STICKY, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2001"), NbBundle.getMessage(SyncFileNode.class, "BK2002"));
        }

        public Object getValue() {
            return getSticky();
        }
    }
    
    private class PathProperty extends SyncFileProperty {

        private String shortPath;

        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2003"), NbBundle.getMessage(SyncFileNode.class, "BK2004"));
            shortPath = Utils.getRelativePath(node.getFile());
            setValue("sortkey", shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return shortPath;
        }
    }
    
    private class NameProperty extends SyncFileProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2005"), NbBundle.getMessage(SyncFileNode.class, "BK2006"));
            setValue("sortkey", SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return SyncFileNode.this.getDisplayName();
        }
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class StatusProperty extends SyncFileProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2007"), NbBundle.getMessage(SyncFileNode.class, "BK2008"));
            String shortPath = Utils.getRelativePath(node.getFile());
            String sortable = Integer.toString(Utils.getComparableStatus(node.getInformation().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return node.getInformation().getStatusText();
        }
    }
}
