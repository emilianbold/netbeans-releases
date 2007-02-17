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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class SourcesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of SourcesNodeFactory */
    public SourcesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        NbModuleProject prj = p.getLookup().lookup(NbModuleProject.class);
        return new SourceNL(prj);
    }

    
    private static class SourceNL implements NodeList<SourceGroup>, ChangeListener {
        private List<ChangeListener> list = new ArrayList<ChangeListener>();
        private static final String[] SOURCE_GROUP_TYPES = {
            JavaProjectConstants.SOURCES_TYPE_JAVA,
            NbModuleProject.SOURCES_TYPE_JAVAHELP,
        };
        
        private final NbModuleProject project;
        
        SourceNL(NbModuleProject prj) {
            project = prj;
        }
        public void addChangeListener(ChangeListener l) {
            list.add(l);
        }

        public void removeChangeListener(ChangeListener l) {
            list.remove(l);
        }

        public void addNotify() {
            ProjectUtils.getSources(project).addChangeListener(this);
        }

        public void removeNotify() {
            ProjectUtils.getSources(project).removeChangeListener(this);
        }

        public Node node(SourceGroup key) {
            return PackageView.createPackageView(key);
        }

        public List<SourceGroup> keys() {
            List<SourceGroup> l = new ArrayList<SourceGroup>();
            Sources s = ProjectUtils.getSources(project);
            for (int i = 0; i < SOURCE_GROUP_TYPES.length; i++) {
                SourceGroup[] groups = s.getSourceGroups(SOURCE_GROUP_TYPES[i]);
                l.addAll(Arrays.asList(groups));
            }
            SourceGroup javadocDocfiles = makeJavadocDocfilesSourceGroup();
            if (javadocDocfiles != null) {
                l.add(javadocDocfiles);
            }
            return l;
        }
        
        private SourceGroup makeJavadocDocfilesSourceGroup() {
            String propname = "javadoc.docfiles"; // NOI18N
            FileObject root = resolveFileObjectFromProperty(propname);
            if(root == null) {
                return null;
            }
            return GenericSources.group(project, root, propname, NbBundle.getMessage(ModuleLogicalView.class, "LBL_extra_javadoc_files"), null, null);
        }
        
        private FileObject resolveFileObjectFromProperty(String property){
            String filename = project.evaluator().getProperty(property);
            if (filename == null) {
                return null;
            }
            return project.getHelper().resolveFileObject(filename);
        }

        public void stateChanged(ChangeEvent arg0) {
            for (ChangeListener lst : list) {
                lst.stateChanged(new ChangeEvent(this));
            }
        }
}
}
