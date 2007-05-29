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

package org.netbeans.modules.groovy.grailsproject.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Martin Adamek
 */
public class SourceNodeFactory implements NodeFactory {
    
    public SourceNodeFactory() {
    }

    public NodeList<?> createNodes(Project p) {
        
        GrailsProject project = p.getLookup().lookup(GrailsProject.class);
        assert project != null;
        return new SourcesNodeList(project);
        
    }
    
    private static class SourcesNodeList implements NodeList<SourceGroupKey>, ChangeListener {

        private GrailsProject project;
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        
        public SourcesNodeList(GrailsProject proj) {
            this.project = proj;
        }

        public List<SourceGroupKey> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.<SourceGroupKey>emptyList();
            }
            Sources sources = getSources();
            
            List<SourceGroupKey> result =  new ArrayList<SourceGroupKey>();

            FileObject grailsAppFO = project.getProjectDirectory().getFileObject("grails-app");
            for (FileObject fileObject : grailsAppFO.getChildren()) {
                SourceGroup[] groups = sources.getSourceGroups(fileObject.getName());
                for(SourceGroup sourceGroup : groups) {
                    result.add(new SourceGroupKey(sourceGroup));
                }
            }

            return result;
        }
        
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        public Node node(SourceGroupKey key) {
            return new TreeRootNode(key.group);
        }
        
        public void addNotify() {
            getSources().addChangeListener(this);
        }
        
        public void removeNotify() {
            getSources().removeChangeListener(this);
        }
        
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }
        
    }

    private static class SourceGroupKey {
        
        public final SourceGroup group;
        public final FileObject fileObject;
        
        SourceGroupKey(SourceGroup group) {
            this.group = group;
            this.fileObject = group.getRootFolder();
        }
        
        public int hashCode() {
            return fileObject.hashCode();
        }
        
        public boolean equals(Object obj) {
            
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                // XXX what is the operator binding order supposed to be here??
                return fileObject.equals(otherKey.fileObject) &&
                        thisDisplayName == null ? otherDisplayName == null : thisDisplayName.equals(otherDisplayName);
            }
            
        }
        
    }

}
