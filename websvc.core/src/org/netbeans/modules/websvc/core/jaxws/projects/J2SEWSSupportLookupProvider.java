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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
public class J2SEWSSupportLookupProvider implements LookupProvider {
    
    /** Creates a new instance of JaxWSLookupProvider */
    public J2SEWSSupportLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project project = baseContext.lookup(Project.class);
        JAXWSClientSupportImpl j2seJAXWSClientSupport = new J2SEProjectJAXWSClientSupport(project);
        JAXWSClientSupport jaxWsClientSupportApi = JAXWSClientSupportFactory.createJAXWSClientSupport(j2seJAXWSClientSupport);
        
        WebServicesClientSupportImpl jaxrpcClientSupport = new J2SEProjectJaxRpcClientSupport(project);
        WebServicesClientSupport jaxRpcClientSupportApi = WebServicesClientSupportFactory.createWebServicesClientSupport(jaxrpcClientSupport);
        
        // Implement Code Completion for jax-ws java artifacts
        // (add an instance of ProjectOpenHook project lookup)
        
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            FileChangeListener fcl;
            PropertyChangeListener pcl;
            protected void projectOpened() {
                fcl = new FileChangeListener() {

                    public void fileFolderCreated(FileEvent fe) {
                        final FileObject fo = fe.getFile();
                        // add wsimport artifacts folder to classpath roots
                        if (fo.isFolder() && fo.getName().equals("build")) { //NOI18N
                            RequestProcessor.getDefault().post(new Runnable() {
                                public void run() {
                                    FileObject wsimportClientFolder = fo.getFileObject("generated/wsimport/client"); //NOI18N
                                    if (wsimportClientFolder!=null) {
                                        changeRoots(fo.getParent(),true);                                     
                                    }
                                }
                            });
                        }
                    }

                    public void fileDataCreated(FileEvent fe) {}

                    public void fileChanged(FileEvent fe) {}

                    public void fileDeleted(FileEvent fe) {
                        FileObject fo = fe.getFile();
                        if ("build".equals(fo.getName())) { //NOI18N
                            // remove wsimport artifacts folder from classpath roots
                            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
                            if (jaxWsModel!=null && jaxWsModel.getClients().length>0) {
                                changeRoots(fo.getParent(),false);
                            }                            
                        }

                    }

                    public void fileRenamed(FileRenameEvent fe) {}

                    public void fileAttributeChanged(FileAttributeEvent fe) {}
                    
                };
                project.getProjectDirectory().addFileChangeListener(fcl);
                final JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
                // listen on changes to jax-ws.xml to add/remove "build/generated/wsimport/client" folder
                // to/from project claspath roots
                if (jaxWsModel!=null) {
                    pcl = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            String propName = evt.getPropertyName();
                            Object oldValue = evt.getOldValue();
                            Object newValue = evt.getNewValue();
                            if (propName.startsWith("/JaxWs/Clients/Client")) { //NOI18N
                                String postFix = propName.substring(21);
                                if (!postFix.contains(":") && !postFix.contains("/")) {
                                    if (oldValue == null) { //client added
                                        if (project.getProjectDirectory().getFileObject("build/generated/wsimport/client") !=null) {
                                            changeRoots(project.getProjectDirectory(),true);
                                        }
                                    } else if (newValue == null && 
                                               jaxWsModel.getClients().length == 0) { // client removed
                                        changeRoots(project.getProjectDirectory(),false);
                                    }
                                }
                            }
                        }
                    };
                    jaxWsModel.addPropertyChangeListener(pcl);
                }
            }

            protected void projectClosed() {
                project.getProjectDirectory().removeFileChangeListener(fcl);
                JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
                if (jaxWsModel!=null) {
                    jaxWsModel.removePropertyChangeListener(pcl);
                }
            }
            
            private void changeRoots(FileObject projectDirectory, boolean add) {
                assert projectDirectory != null;
                Sources sources = project.getLookup().lookup(Sources.class);
                SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (srcGroups!=null && srcGroups.length>0) {
                    File buildDir = FileUtil.toFile(projectDirectory); 
                    File file = new File(buildDir,"build/generated/wsimport/client"); //NOI18N
                    try {
                        URL url = file.toURI().toURL();
                        if (!file.exists()) {  //NOI18N
                            assert !url.toExternalForm().endsWith("/");  //NOI18N
                            url = new URL (url.toExternalForm()+'/');   //NOI18N
                        }
                        if (add) { // adding build/generated/wsimport/client as a root
                            ProjectClassPathModifier.addRoots(new URL[]{url}, srcGroups[0].getRootFolder(), ClassPath.COMPILE);
                        } else { // removing build/generated/wsimport/client from roots
                            ProjectClassPathModifier.removeRoots(new URL[]{url}, srcGroups[0].getRootFolder(), ClassPath.COMPILE);                            
                        }
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                    }
                }                 
            }
        };
        
                
        return Lookups.fixed(new Object[] {openhook, jaxWsClientSupportApi,jaxRpcClientSupportApi,new J2SEProjectWSClientSupportProvider()});
    }
}