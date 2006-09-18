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

package org.netbeans.modules.java.j2seproject.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.netbeans.modules.java.j2seproject.UpdateHelper;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerLibraries;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public final class LibrariesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public LibrariesNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        J2SEProject project = (J2SEProject)p.getLookup().lookup(J2SEProject.class);
        assert project != null;
        return new LibrariesNodeList(project);
    }

    private static class LibrariesNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String LIBRARIES = "Libs"; //NOI18N
        private static final String TEST_LIBRARIES = "TestLibs"; //NOI18N

        private SourceRoots testSources;
        private J2SEProject project;
        private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

        private PropertyEvaluator evaluator;
        private UpdateHelper helper;
        private ReferenceHelper resolver;
        
        LibrariesNodeList(J2SEProject proj) {
            project = proj;
            testSources = project.getTestSourceRoots();
            J2SELogicalViewProvider logView = (J2SELogicalViewProvider)project.getLookup().lookup(J2SELogicalViewProvider.class);
            assert logView != null;
            evaluator = logView.getEvaluator();
            helper = logView.getUpdateHelper();
            resolver = logView.getRefHelper();
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(LIBRARIES);
            URL[] testRoots = testSources.getRootURLs();
            boolean addTestSources = false;
            for (int i = 0; i < testRoots.length; i++) {
                File f = new File(URI.create(testRoots[i].toExternalForm()));
                if (f.exists()) {
                    addTestSources = true;
                    break;
                }
            }
            if (addTestSources) {
                result.add(TEST_LIBRARIES);
            }
            return result;
        }

        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }

        public Node node(String key) {
            if (key == LIBRARIES) {
                //Libraries Node
                return  
                    new LibrariesNode(NbBundle.getMessage(J2SELogicalViewProvider.class,"CTL_LibrariesNode"),
                        project, evaluator, helper, resolver, J2SEProjectProperties.RUN_CLASSPATH,
                        new String[] {J2SEProjectProperties.BUILD_CLASSES_DIR},
                        "platform.active", // NOI18N
                        new Action[] {
                            LibrariesNode.createAddProjectAction(project, J2SEProjectProperties.JAVAC_CLASSPATH),
                            LibrariesNode.createAddLibraryAction(project, J2SEProjectProperties.JAVAC_CLASSPATH),
                            LibrariesNode.createAddFolderAction(project, J2SEProjectProperties.JAVAC_CLASSPATH),
                            null,
                            new SourceNodeFactory.PreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE), // NOI18N
                        }
                    );
            } else if (key == TEST_LIBRARIES) {
                return  
                    new LibrariesNode(NbBundle.getMessage(J2SELogicalViewProvider.class,"CTL_TestLibrariesNode"),
                        project, evaluator, helper, resolver, J2SEProjectProperties.RUN_TEST_CLASSPATH,
                        new String[] {
                            J2SEProjectProperties.BUILD_TEST_CLASSES_DIR,
                            J2SEProjectProperties.JAVAC_CLASSPATH,
                            J2SEProjectProperties.BUILD_CLASSES_DIR,
                        },
                        null,
                        new Action[] {
                            LibrariesNode.createAddProjectAction(project, J2SEProjectProperties.JAVAC_TEST_CLASSPATH),
                            LibrariesNode.createAddLibraryAction(project, J2SEProjectProperties.JAVAC_TEST_CLASSPATH),
                            LibrariesNode.createAddFolderAction(project, J2SEProjectProperties.JAVAC_TEST_CLASSPATH),
                            null,
                            new SourceNodeFactory.PreselectPropertiesAction(project, "Libraries", CustomizerLibraries.COMPILE_TESTS), // NOI18N
                        }
                    );
            }
            assert false: "No node for key: " + key;
            return null;
            
        }

        public void addNotify() {
            testSources.addPropertyChangeListener(this);
        }

        public void removeNotify() {
            testSources.removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        }
        
    }
    
}
