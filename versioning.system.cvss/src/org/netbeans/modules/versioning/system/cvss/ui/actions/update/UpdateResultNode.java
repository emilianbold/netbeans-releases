/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.versioning.system.cvss.ui.actions.update;

import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

/**
 * The node that is rendered in the Update Results view.
 * 
 * @author Maros Sandor
 */
class UpdateResultNode extends AbstractNode {
    
    private DefaultFileInfoContainer info;

    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_PATH        = "path"; // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    
    private final MessageFormat conflictFormat = new MessageFormat("<font color=\"#FF0000\">{0}</font>"); // NOI18N
    private final MessageFormat mergedFormat = new MessageFormat("<font color=\"#0000FF\">{0}</font>"); // NOI18N
    private final MessageFormat removedFormat = new MessageFormat("<font color=\"#999999\">{0}</font>"); // NOI18N
    
    private String statusDisplayName;
    private String htmlDisplayName;
    
    public UpdateResultNode(DefaultFileInfoContainer info) {
        super(Children.LEAF);
        this.info = info;
        initProperties();
        refreshHtmlDisplayName();
    }

    public DefaultFileInfoContainer getInfo() {
        return info;
    }

    public String getName() {
        return info.getFile().getName();
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
        ps.put(new UpdateResultNode.StatusProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }
    
    private void refreshHtmlDisplayName() {
        if (isConflict()) {
            htmlDisplayName = conflictFormat.format(new Object [] { getName() });
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Conflict"); // NOI18N
        } else if (isRemoved()) {
            htmlDisplayName = removedFormat.format(new Object [] { getName() });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Removed"); // NOI18N
        } else if (isMerged()) {
            htmlDisplayName = mergedFormat.format(new Object [] { getName() });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Merged"); // NOI18N
        } else if (isUpdated()) {
            htmlDisplayName = getName();            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Updated"); // NOI18N
        } else {
            throw new IllegalStateException("Unhandled update type: " + info.getType()); // NOI18N
        }
        fireDisplayNameChange(htmlDisplayName, htmlDisplayName);
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    private boolean isUpdated() {
        return "UP".indexOf(info.getType()) != -1; // NOI18N
    }

    private boolean isMerged() {
        return "G".equals(info.getType()); // NOI18N
    }
    
    private boolean isRemoved() {
        return DefaultFileInfoContainer.PERTINENT_STATE.equals(info.getType());
    }

    private boolean isConflict() {
        return "C".equals(info.getType()); // NOI18N
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
            shortPath = Utils.getRelativePath(info.getFile());
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
    
    private class StatusProperty extends UpdateResultNode.SyncFileProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Desc")); // NOI18N
            String shortPath = Utils.getRelativePath(info.getFile());
            String sortable = info.getType();
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + UpdateResultNode.this.getName()); // NOI18N
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return statusDisplayName;
        }
    }
}
