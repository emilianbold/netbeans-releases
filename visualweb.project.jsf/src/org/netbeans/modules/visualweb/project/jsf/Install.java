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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
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
                if (LibraryManager.getDefault().getLibrary("jsf-designtime") != null) { // NOI18N
                    return;
                }

                List<Project> oldOpenProjectsList = Arrays.asList((Project[]) event.getOldValue());
                List<Project> newOpenProjectsList = Arrays.asList((Project[]) event.getNewValue());
                
                Set<Project> openedProjectsSet = new LinkedHashSet<Project>(newOpenProjectsList);
                openedProjectsSet.removeAll(oldOpenProjectsList);
                String projs = ""; // NOI18N
                for (Project project : openedProjectsSet) {
                    if (JsfProjectUtils.isJsfProject(project) && !JsfProjectUtils.isJavaEE5Project(project)) {
                        // It's a VisualWeb/Creator J2EE 1.4 project
                        ClassPath cp = ClassPath.getClassPath(JsfProjectUtils.getDocumentRoot(project), ClassPath.COMPILE);
                        if (cp.findResource("javax/faces/FacesException.class") == null && //NOI18N
                            cp.findResource("org/apache/myfaces/webapp/StartupServletContextListener.class") == null) { //NOI18N
                            // Server doesn't have the JSF RI support
                            ProjectInformation info = ProjectUtils.getInformation(project);
                            if (projs.length() > 0) {
                                projs += ", "; // NOI18N
                            }
                            projs += info.getDisplayName();
                        }
                    }
                }

                if (projs.length() > 0) {
                    final String projsList = projs;
                    SwingUtilities.invokeLater(new Runnable () {
                        public void run() {
                            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(Install.class, "LBL_MissingJSF", projsList), NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);
                        }
                    });
                }
            }
        }
    }
}
