/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.update;

import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.lookup.Lookups;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * The node that is rendered in the Update Results view.
 * 
 * @author Maros Sandor
 */
class UpdateResultNode extends AbstractNode {
    
    private FileUpdateInfo info;

    static final String COLUMN_NAME_NAME        = "name";   // NOI18N
    static final String COLUMN_NAME_PATH        = "path";   // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    
    private final MessageFormat conflictFormat  = new MessageFormat("<font color=\"#FF0000\">{0}</font>");  // NOI18N
    private final MessageFormat mergedFormat    = new MessageFormat("<font color=\"#0000FF\">{0}</font>");  // NOI18N
    private final MessageFormat removedFormat   = new MessageFormat("<font color=\"#999999\">{0}</font>");  // NOI18N
    private final MessageFormat addedFormat     = new MessageFormat("<font color=\"#008000\">{0}</font>");    // NOI18N   
    
    private String statusDisplayName;
    
    private String htmlDisplayName;
    
    public UpdateResultNode(FileUpdateInfo info) {
        super(Children.LEAF, Lookups.fixed(new Object [] { info }));
        this.info = info;
        initProperties();
        refreshHtmlDisplayName();
    }

    public FileUpdateInfo getInfo() {
        return info;
    }

    public String getName() {
        String name = info.getFile().getName() + ( (info.getAction() & FileUpdateInfo.ACTION_TYPE_PROPERTY) != 0 ? " - Property" : "" );        
        return name;
    }
    
    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    public <T extends Node.Cookie> T getCookie(Class<T> klass) {
        FileObject fo = FileUtil.toFileObject(info.getFile());
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
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new UpdateResultNode.NameProperty());
        ps.put(new UpdateResultNode.PathProperty());
        ps.put(new UpdateResultNode.FileStatusProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }
    
    private void refreshHtmlDisplayName() {
        String name = getName();        
        if ( (info.getAction() & FileUpdateInfo.ACTION_ADDED) != 0 ) { 
            htmlDisplayName = addedFormat.format(new Object [] {  name } );     
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Added"); // NOI18N 
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_CONFLICTED) != 0 ) {
            htmlDisplayName = conflictFormat.format(new Object [] { name });
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Conflict"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_DELETED) != 0 ) {
            htmlDisplayName = removedFormat.format(new Object [] { name });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Removed"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_MERGED) != 0 ) {
            htmlDisplayName = mergedFormat.format(new Object [] { name });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Merged"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_UPDATED) != 0 ) {
            htmlDisplayName = name;            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Updated"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_CONFLICTED_RESOLVED) != 0 ) {
            htmlDisplayName = name;            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Conflict_Resolved"); // NOI18N
        } else {
            throw new IllegalStateException("Unhandled update type: " + info.getAction()); // NOI18N
        }
        fireDisplayNameChange(htmlDisplayName, htmlDisplayName);
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    private abstract class SyncFileProperty extends PropertySupport.ReadOnly<String> {
        protected SyncFileProperty(String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        public String toString() {
            try {
                return getValue();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }
    }
    
    private class PathProperty extends UpdateResultNode.SyncFileProperty {
        private String shortPath;
        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Path_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Path_Desc")); // NOI18N
            try {                
                shortPath = SvnUtils.getRelativePath(info.getFile());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
                shortPath = "";
            }                
            setValue("sortkey", shortPath + "\t" + UpdateResultNode.this.getName()); // NOI18N
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return shortPath;
        }
    } 
    
    private class NameProperty extends UpdateResultNode.SyncFileProperty {
        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Name_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Name_Desc")); // NOI18N
            setValue("sortkey", UpdateResultNode.this.getName()); // NOI18N
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return UpdateResultNode.this.getDisplayName();
        }
    }

    private final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class FileStatusProperty extends UpdateResultNode.SyncFileProperty {        
        private String shortPath;        
        public FileStatusProperty() {            
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Desc"));            
            try {
                shortPath = SvnUtils.getRelativePath(info.getFile());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
                shortPath = "";
            }                
            String sortable = Integer.toString(info.getAction());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + UpdateResultNode.this.getName());
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return statusDisplayName;
        }
    }
}
