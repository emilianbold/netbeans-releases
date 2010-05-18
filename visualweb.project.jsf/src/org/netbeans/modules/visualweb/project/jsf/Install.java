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
                LibraryManager libManager = LibraryManager.getDefault();
                Library libJSF = libManager.getLibrary("jsf1102"); // NOI18N
                Library libJAXRPC = libManager.getLibrary("jaxrpc16"); // NOI18N
                Library libRowset = libManager.getLibrary("rowset-ri"); // NOI18N
                String projs = ""; // NOI18N
                boolean needJSF = false;
                boolean needJAXRPC = false;
                boolean needRowset = false;

                for (Project project : jsfProjectsSet) {
                    boolean badProj = false;
                    boolean JavaEE5Project = JsfProjectUtils.isJavaEE5Project(project);
                    ClassPath cp = ClassPath.getClassPath(JsfProjectUtils.getDocumentRoot(project), ClassPath.COMPILE);

                    // It's a VisualWeb/Creator J2EE 1.4 project
                    if (!JavaEE5Project) {
                        if ((libJSF == null) &&
                            (cp.findResource("javax/faces/FacesException.class") == null) && //NOI18N
                            (cp.findResource("org/apache/myfaces/webapp/StartupServletContextListener.class") == null)) { //NOI18N
                            // Both the IDE and Server do not have the JSF 1.1 RI support
                            projs = appendProject(project, projs);
                            badProj = true;
                            needJSF = true;
                        }

                        if ((libJAXRPC == null) &&
                            (cp.findResource("javax/xml/rpc/Service.class") == null)) { //NOI18N
                            // Both the IDE and Server do not have the JAXRPC support
                            if (!badProj) {
                                projs = appendProject(project, projs);
                            }
                            badProj = true;
                            needJAXRPC = true;
                        }
                    }

                    // It's a VisualWeb/Creator J2SE 1.3/1.4 project
                    String srcLevel = JsfProjectUtils.getSourceLevel(project);
                    if ("1.3".equals(srcLevel) || "1.4".equals(srcLevel)) { // NOI18N
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
                    int count = 0;
                    String nbms = "";
                    if (needJSF) {
                        count++;
                        nbms = NbBundle.getMessage(Install.class, "LBL_MissingJSF");
                    }
                    if (needJAXRPC) {
                        count++;
                        nbms += NbBundle.getMessage(Install.class, "LBL_MissingJAXRPC");
                    }
                    if (needRowset) {
                        count++;
                        nbms += NbBundle.getMessage(Install.class, "LBL_MissingRowset");
                    }

                    String RI = NbBundle.getMessage(Install.class, (count > 1) ? "LBL_MissingMany" : "LBL_MissingOne");

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
