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

package org.netbeans.modules.visualweb.project.jsf;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

import org.openide.modules.ModuleInstall;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author Po-Ting Wu
 */
public class Install extends ModuleInstall {
    private static final Logger LOGGER = Logger.getLogger(Install.class.getName());

    public void restored() {
        OpenProjects.getDefault().addPropertyChangeListener(new OpenProjectsListener());
    }

    public static class OpenProjectsListener implements PropertyChangeListener {
        public OpenProjectsListener() {
            // Don't do anything during IDE startup.
        }
        
        public void propertyChange(PropertyChangeEvent event) {
            // The list of open projects has changed.
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {
                List<Project> oldOpenProjectsList = Arrays.asList((Project[]) event.getOldValue());
                List<Project> newOpenProjectsList = Arrays.asList((Project[]) event.getNewValue());
                
                Set<Project> jsfProjectsSet = new LinkedHashSet<Project>();
                Set<Project> openedProjectsSet = new LinkedHashSet<Project>(newOpenProjectsList);
                openedProjectsSet.removeAll(oldOpenProjectsList);
                for (Project project : openedProjectsSet) {
                    if (JsfProjectUtils.isJsfProject(project)) {
                        jsfProjectsSet.add(project);
                    }
                }
                   
                if (jsfProjectsSet.size() == 0) {
                    return;
                }

                // Visual Web project found in the just opened project list
                Library libJSF = LibraryManager.getDefault().getLibrary("jsf-designtime"); // NOI18N
                Library libRowset = LibraryManager.getDefault().getLibrary("rowset-ri"); // NOI18N
                String projs = ""; // NOI18N
                boolean needJSF = false;
                boolean needRowset = false;

                for (Project project : jsfProjectsSet) {
                    boolean badProj = false;
                    if (libJSF == null && !JsfProjectUtils.isJavaEE5Project(project)) {
                        // It's a VisualWeb/Creator J2EE 1.4 project
                        ClassPath cp = ClassPath.getClassPath(JsfProjectUtils.getDocumentRoot(project), ClassPath.COMPILE);
                        if (cp.findResource("javax/faces/FacesException.class") == null && //NOI18N
                            cp.findResource("org/apache/myfaces/webapp/StartupServletContextListener.class") == null) { //NOI18N
                            // Server doesn't have the JSF RI support
                            projs = appendProject(project, projs);
                            badProj = true;
                            needJSF = true;
                        }
                    }

                    String srcLevel = JsfProjectUtils.getSourceLevel(project);
                    if ("1.3".equals(srcLevel) || "1.4".equals(srcLevel)) { // NOI18N
                        // It's a VisualWeb/Creator J2SE 1.3/1.4 project
                        ClassPath cp = ClassPath.getClassPath(JsfProjectUtils.getDocumentRoot(project), ClassPath.COMPILE);
                        if (cp.findResource("javax/sql/rowset/BaseRowSet.class") == null) { //NOI18N
                            // IDE doesn't have the Rowset RI support
                            if (libRowset != null) {
                                try {
                                    JsfProjectUtils.addLibraryReferences(project, new Library[] { libRowset });
                                } catch (IOException ex) {
                                    LOGGER.log(Level.WARNING, "Exception during adding Rowset RI library", ex); //NOI18N
                                }
                            } else {
                                if (!badProj) {
                                    projs = appendProject(project, projs);
                                }
                                needRowset = true;
                            }
                        }
                    }
                }

                if (projs.length() > 0) {
                    String RI = (needJSF && needRowset) ? NbBundle.getMessage(Install.class, "LBL_MissingTwo")
                                                        : NbBundle.getMessage(Install.class, "LBL_MissingOne");
                    String nbms = "";
                    if (needJSF) {
                        nbms = NbBundle.getMessage(Install.class, "LBL_MissingJSF");
                    }
                    if (needRowset) {
                        nbms += NbBundle.getMessage(Install.class, "LBL_MissingRowset");
                    }

                    final String mesg = NbBundle.getMessage(Install.class, "LBL_MissingNBM", projs, RI, nbms);
                    SwingUtilities.invokeLater(new Runnable () {
                        public void run() {
                            NotifyDescriptor d = new NotifyDescriptor.Message(mesg, NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);
                        }
                    });
                }
            }
        }

        public String appendProject(Project project, String projs) {
            ProjectInformation info = ProjectUtils.getInformation(project);
            if (projs.length() > 0) {
                projs += ", "; // NOI18N
            }
            projs += info.getDisplayName();

            return projs;
        }
    }
}
