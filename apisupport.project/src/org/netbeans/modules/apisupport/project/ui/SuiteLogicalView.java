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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans suite project.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class SuiteLogicalView implements LogicalViewProvider {
    
    private final SuiteProject suite;
    
    public SuiteLogicalView(final SuiteProject suite) {
        this.suite = suite;
    }
    
    public Node createLogicalView() {
        return new SuiteRootNode(suite);
    }
    
    public Node findPath(Node root, Object target) {
        if (root.getLookup().lookup(SuiteProject.class) != suite) {
            // Not intended for this project. Should not normally happen anyway.
            return null;
        }
        
        DataObject file;
        if (target instanceof FileObject) {
            try {
                file = DataObject.find((FileObject) target);
            } catch (DataObjectNotFoundException e) {
                return null; // OK
            }
        } else if (target instanceof DataObject) {
            file = (DataObject) target;
        } else {
            // What is it?
            return null;
        }
        
        Node impFilesNode = root.getChildren().findChild("important.files"); // NOI18N
        if (impFilesNode != null) {
            Node[] impFiles = impFilesNode.getChildren().getNodes(true);
            for (int i = 0; i < impFiles.length; i++) {
                if (impFiles[i].getCookie(DataObject.class) == file) {
                    return impFiles[i];
                }
            }
        }
        
        return null;
    }
    
    /** Package private for unit test only. */
    static final class SuiteRootNode extends AnnotatedNode
            implements PropertyChangeListener {
        
        private static final Image ICON = Utilities.loadImage(SuiteProject.SUITE_ICON_PATH, true);
        
        private final SuiteProject suite;
        private final ProjectInformation info;
        
        SuiteRootNode(final SuiteProject suite) {
            super(NodeFactorySupport.createCompositeChildren(suite, "Projects/org-netbeans-modules-apisupport-project-suite/Nodes"), 
                  Lookups.fixed(new Object[] {suite}));
            this.suite = suite;
            info = ProjectUtils.getInformation(suite);
            info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
            setFiles(getProjectFiles());
        }
        
        /** Package private for unit test only. */
        Set<FileObject> getProjectFiles() {
            Set<FileObject> files = new HashSet<FileObject>();
            Enumeration en = suite.getProjectDirectory().getChildren(false);
            while (en.hasMoreElements()) {
                FileObject child = (FileObject) en.nextElement();
                if (FileOwnerQuery.getOwner(child) == suite) {
                    files.add(child);
                }
            }
            return files;
        }
        
        public String getName() {
            return info.getDisplayName();
        }
        
        public String getDisplayName() {
            return info.getDisplayName();
        }
        
        public String getShortDescription() {
            return NbBundle.getMessage(SuiteLogicalView.class, "HINT_suite_project_root_node",
                    FileUtil.getFileDisplayName(suite.getProjectDirectory()));
        }
        
        public Action[] getActions(boolean context) {
            return SuiteActions.getProjectActions(suite);
        }
        
        public Image getIcon(int type) {
            return annotateIcon(ICON, type);
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type); // the same in the meantime
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ProjectInformation.PROP_NAME)) {
                fireNameChange(null, getName());
            } else if (evt.getPropertyName().equals(ProjectInformation.PROP_DISPLAY_NAME)) {
                fireDisplayNameChange(null, getDisplayName());
            }
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(suite, name);
        }
        
    }
}
