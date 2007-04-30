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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
public class J2seJaxWsLookupProvider implements LookupProvider {
    
    private String JAX_WS_XML_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"; //NOI18N
    private String JAX_WS_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jaxws-j2se.xsl"; //NOI18N
    private String JAXWS_EXTENSION = "jaxws"; //NOI18N
    
    /** Creates a new instance of JaxWSLookupProvider */
    public J2seJaxWsLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        if (prj==null) return null;
        final JaxWsModel jaxWsModel = getJaxWsModel(prj);
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            private FileChangeListener jaxWsListener;
            private FileChangeListener jaxWsCreationListener;
            
            protected void projectOpened() {
                if (jaxWsModel!=null) { 
                    AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                    if (ext != null) {
                        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
                        try {
                            if (jaxws_build==null && jaxWsModel.getClients().length>0) {
                                // generate nbproject/jaxws-build.xml
                                try {
                                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                                        public Boolean run() throws IOException {
                                            TransformerUtils.transformClients(prj.getProjectDirectory(),JAX_WS_STYLESHEET_RESOURCE);
                                            return Boolean.TRUE;
                                        }
                                    });
                                } catch (MutexException e) {
                                    throw (IOException)e.getException();
                                }
                                TransformerUtils.transformClients(prj.getProjectDirectory(), JAX_WS_STYLESHEET_RESOURCE);
                                jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                                assert jaxws_build!=null;
                                if (extension==null) {
                                    extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
                                    //adding dependencies
                                    extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
                                    extension.addDependency("-do-compile", "wsimport-client-compile"); //NOI18N
                                    extension.addDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
                                    ProjectManager.getDefault().saveProject(prj);
                                }
                            } else if (jaxws_build!=null && jaxWsModel.getClients().length==0) {
                                if (extension!=null) {
                                    ext.removeExtension(JAXWS_EXTENSION);
                                    ProjectManager.getDefault().saveProject(prj);
                                }
                                FileLock fileLock = jaxws_build.lock();
                                if (fileLock!=null) 
                                    try {
                                        jaxws_build.delete(fileLock);
                                    } finally {
                                        fileLock.releaseLock();
                                    }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        FileObject jaxws_fo = getJaxWsFileObject(prj);
                        if (jaxws_fo!=null) {                     
                            jaxWsListener = new FileChangeAdapter() {
                                public void fileChanged(FileEvent fe) {
                                    handleJaxsClientBuildScript();
                                }
                            };  
                            jaxws_fo.addFileChangeListener(jaxWsListener);
                        } else {
                            FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
                            if (nbprojectDir!=null) {
                                jaxWsCreationListener = new FileChangeAdapter() {
                                    public void fileDataCreated(FileEvent fe) {
                                        if ("jax-ws.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                                            FileObject jaxws_fo = getJaxWsFileObject(prj);
                                            if (jaxws_fo!=null) {
                                                jaxWsListener = new FileChangeAdapter() {
                                                    public void fileChanged(FileEvent fe) {
                                                        handleJaxsClientBuildScript();
                                                    }
                                                };  
                                                jaxws_fo.addFileChangeListener(jaxWsListener);                                           
                                            }
                                        }
                                    }
                                };
                                nbprojectDir.addFileChangeListener(jaxWsCreationListener);
                            }
                        }
                    }
                }
            }
            protected void projectClosed() {
                FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
                if (nbprojectDir!=null) {
                    nbprojectDir.removeFileChangeListener(jaxWsCreationListener);
                    FileObject jaxws_fo = getJaxWsFileObject(prj);
                    if (jaxws_fo!=null)
                        jaxws_fo.removeFileChangeListener(jaxWsListener);
                }
                
            }
            
            private void handleJaxsClientBuildScript() {
                AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                if (ext != null) {
                    FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                    AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
                    try {
                        if (jaxWsModel.getClients().length==0) {
                            if (extension!=null) {
                                ext.removeExtension(JAXWS_EXTENSION);
                                ProjectManager.getDefault().saveProject(prj);
                            }
                            if (jaxws_build!=null) {
                                FileLock fileLock = jaxws_build.lock();
                                if (fileLock!=null) {
                                    try {
                                        jaxws_build.delete(fileLock);
                                    } finally {
                                        fileLock.releaseLock();
                                    }
                                }
                            }
                        } else {
                            try {
                                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                                    public Boolean run() throws IOException {
                                        TransformerUtils.transformClients(prj.getProjectDirectory(), JAX_WS_STYLESHEET_RESOURCE);
                                        return Boolean.TRUE;
                                    }
                                });
                            } catch (MutexException e) {
                                throw (IOException)e.getException();
                            }
                            TransformerUtils.transformClients(prj.getProjectDirectory(), JAX_WS_STYLESHEET_RESOURCE);
                            if (jaxws_build==null) {
                                jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                            }
                            assert jaxws_build!=null;
                            if (extension==null) {
                                extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
                                //adding dependencies
                                extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
                                extension.addDependency("-do-compile", "wsimport-client-compile"); //NOI18N
                                extension.addDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
                                ProjectManager.getDefault().saveProject(prj);
                            }
                            
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        return Lookups.fixed(new Object[] {
            openhook,
            jaxWsModel 
        });
    }
    
    private JaxWsModel getJaxWsModel(Project prj) {
        try {
            FileObject fo = getJaxWsFileObject(prj);
            if (fo==null)
                return JaxWsModelProvider.getDefault().getJaxWsModel(
                        WSUtils.class.getResourceAsStream(JAX_WS_XML_RESOURCE));
            else {
                JaxWsModel jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(fo);
                return jaxWsModel;
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
    
    private FileObject getJaxWsFileObject(Project prj) {
        FileObject jaxWsFo = findJaxWsFileObject(prj);
        return jaxWsFo;
    }
    
    public FileObject findJaxWsFileObject(Project prj) {
        return prj.getProjectDirectory().getFileObject(TransformerUtils.JAX_WS_XML_PATH);
    }
}
