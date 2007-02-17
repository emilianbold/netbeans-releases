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
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans module project.
 * @author Jesse Glick
 */
public final class ModuleLogicalView implements LogicalViewProvider {
    
    
    private final NbModuleProject project;
    
    public ModuleLogicalView(NbModuleProject project) {
        this.project = project;
    }
    
    public Node createLogicalView() {
        return new RootNode(project);
    }
    
    /** cf. #45952 */
    public Node findPath(Node root, Object target) {
        if (root.getLookup().lookup(NbModuleProject.class) != project) {
            // Not intended for this project. Should not normally happen anyway.
            return null;
        }
        
        Node[] rootChildren = root.getChildren().getNodes(true);
        DataObject file;
        
        if (target instanceof FileObject) {
            try {
                file = DataObject.find((FileObject) target);
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
        } else if (target instanceof DataObject) {
            file = (DataObject) target;
        } else {
            // What is it?
            return null;
        }
        
        for (int i = 0; i < rootChildren.length; i++) {
            Node found = PackageView.findPath(rootChildren[i], target);
            //System.err.println("found " + found + " for " + target + " in " + rootChildren[i]);
            if (found != null) {
                return found;
            }
            // For Important Files node:
            if (rootChildren[i].getName().equals(ImportantFilesNodeFactory.IMPORTANT_FILES_NAME)) {
                Node[] ifChildren = rootChildren[i].getChildren().getNodes(true);
                for (int j = 0; j < ifChildren.length; j++) {
                    if (ifChildren[j].getCookie(DataObject.class) == file) {
                        return ifChildren[j];
                    }
                }
            }
        }
        
        return null;
    }
    
    private static final class RootNode extends AnnotatedNode {
        
        private final NbModuleProject project;
        
        public RootNode(NbModuleProject project) {
            
            // XXX add a NodePathResolver impl to lookup
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-apisupport-project/Nodes"), 
                  Lookups.fixed(new Object[] {project}));
            this.project = project;
            setForceAnnotation(true);
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            ProjectInformation pi = ProjectUtils.getInformation(project);
            setDisplayName(pi.getDisplayName());
            setShortDescription(NbBundle.getMessage(ModuleLogicalView.class, "HINT_project_root_node", FileUtil.getFileDisplayName(project.getProjectDirectory())));
            setFiles(getProjectFiles());
            pi.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == ProjectInformation.PROP_DISPLAY_NAME) {
                        RootNode.this.setDisplayName((String) evt.getNewValue());
                    } else if (evt.getPropertyName() == ProjectInformation.PROP_NAME) {
                        RootNode.this.setName((String) evt.getNewValue());
                    }
                }
            });
        }
        
        private Set getProjectFiles() {
            Set roots = new HashSet();
            Sources sources = ProjectUtils.getSources(project);
            
            // TODO add Sources.addChangeListener(this)
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int i = 0; i<groups.length; i++) {
                SourceGroup group = groups[i];
                FileObject fo = group.getRootFolder();
                if (fo != null) {
                    FileObject [] files = fo.getChildren();
                    for (int j = 0; j < files.length; j++) {
                        FileObject file = files[j];
                        if (group.contains(file)) {
                            roots.add(file);
                        }
                    }
                }
            }
            return roots;
        }
        
        public Action[] getActions(boolean ignore) {
            return ModuleActions.getProjectActions(project);
        }
        
        public Image getIcon(int type) {
            return annotateIcon(super.getIcon(type), type);
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type); // the same in the meantime
        }
        
        public boolean canRename() {
            return true;
        }
        
        public String getName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(project, name);
        }
        
    }
}
