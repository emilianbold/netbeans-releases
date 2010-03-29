/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
@LookupProvider.Registration(projectType="org-netbeans-modules-java-j2seproject")
public class J2seJaxWsLookupProvider implements LookupProvider {

    private static final String JAX_WS_XML_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"; //NOI18N
    private FileChangeListener jaxWsListener;
    private ChangeListener jaxWsCreationListener;
    
    /** Creates a new instance of JaxWSLookupProvider */
    public J2seJaxWsLookupProvider() {
    }
    
    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        if (prj==null) return null;
        final JaxWsModel jaxWsModel = getJaxWsModel(prj);

        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            
            @Override
            protected void projectOpened() {
                if (jaxWsModel!=null) { 
                    AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                    final JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
                    if (ext != null && extProvider != null) {
                        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                        try {
                            AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
                            if (jaxws_build==null || extension == null) {
                                if (jaxWsModel.getClients().length > 0) {
                                    // generate nbproject/jaxws-build.xml
                                    // add jaxws extension
                                    extProvider.addJaxWsExtension(ext);
                                    addJaxWsApiEndorsed(prj);
                                }
                            } else if (jaxWsModel.getClients().length==0) {
                                // remove nbproject/jaxws-build.xml
                                // remove the jaxws extension
                                extProvider.removeJaxWsExtension(ext);
                            } else {
                                // remove compile dependencies, and re-generate build-script if needed
                                FileObject project_xml = prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                                if (project_xml != null) {
                                    removeCompileDependencies(prj, project_xml, ext);
                                }
                                addJaxWsApiEndorsed(prj);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        FileObject jaxws_fo = jaxWsModel.getJaxWsFile();
                        if (jaxws_fo!=null) {                     
                            jaxWsListener = new FileChangeAdapter() {
                                @Override
                                public void fileChanged(FileEvent fe) {
                                    handleJaxsClientBuildScript(prj, jaxWsModel);
                                }
                            };  
                            jaxws_fo.addFileChangeListener(jaxWsListener);
                        }
                    }
                }
            }
            
            @Override
            protected void projectClosed() {
                if (jaxWsModel != null) {
                    if (jaxWsCreationListener != null) {
                        jaxWsModel.removeChangeListener(jaxWsCreationListener);
                    }
                    FileObject jaxws_fo = jaxWsModel.getJaxWsFile();
                    if (jaxws_fo!=null && jaxWsListener != null) {
                        jaxws_fo.removeFileChangeListener(jaxWsListener);
                    }
                }                
            }
        };
        return Lookups.fixed(new Object[] {
            openhook,
            jaxWsModel 
        });
    }
    
    private JaxWsModel getJaxWsModel(final Project prj) {
        try {
            FileObject fo = findJaxWsFileObject(prj);
            if (fo == null) {
                final JaxWsModel model = JaxWsModelProvider.getDefault().getJaxWsModel(
                        WSUtils.class.getResourceAsStream(JAX_WS_XML_RESOURCE));
                if (model != null) {
                    jaxWsCreationListener = new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            FileObject jaxws_fo = model.getJaxWsFile();                            
                            if (jaxws_fo!=null) {
                                jaxWsListener = new FileChangeAdapter() {
                                    @Override
                                    public void fileChanged(FileEvent fe) {
                                        handleJaxsClientBuildScript(prj, model);
                                    }
                                };
                                jaxws_fo.addFileChangeListener(jaxWsListener);
                            }
                        }

                    };
                    model.addChangeListener(jaxWsCreationListener);
                }
                return model;
            } else {
                return JaxWsModelProvider.getDefault().getJaxWsModel(fo);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }

    private void handleJaxsClientBuildScript(Project prj, JaxWsModel jaxWsModel) {
        AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
        JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
        if (ext != null && extProvider != null) {
            FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
            try {
                if (jaxWsModel.getClients().length==0) {
                    // remove nbproject/jaxws-build.xml
                    // remove the jaxws extension
                    extProvider.removeJaxWsExtension(ext);
                } else {
                    // re-generate nbproject/jaxws-build.xml
                    // add jaxws extension
                    extProvider.addJaxWsExtension(ext);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private FileObject findJaxWsFileObject(Project prj) {
        return prj.getProjectDirectory().getFileObject(TransformerUtils.JAX_WS_XML_PATH);
    }
    
    /** make old project backward compatible with new projects
     *
     */
    private void removeCompileDependencies (
                        Project prj,
                        FileObject project_xml,
                        final AntBuildExtender ext) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(FileUtil.toFile(project_xml)));
        String line = null;
        boolean isOldVersion = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("wsimport-client-compile")) { //NOI18N
                isOldVersion = true;
                break;
            }
        }
        br.close();
        if (isOldVersion) {
            TransformerUtils.transformClients(prj.getProjectDirectory(), J2seBuildScriptExtensionProvider.JAX_WS_STYLESHEET_RESOURCE);
            AntBuildExtender.Extension extension = ext.getExtension(JaxWsBuildScriptExtensionProvider.JAXWS_EXTENSION);
            if (extension!=null) {
                extension.removeDependency("-do-compile", "wsimport-client-compile"); //NOI18N
                extension.removeDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
                ProjectManager.getDefault().saveProject(prj);
            }
        }

    }

    private void addJaxWsApiEndorsed(Project prj) throws IOException {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(
        JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups!=null && sourceGroups.length>0) {
            WSUtils.addJaxWsApiEndorsed(prj, sourceGroups[0].getRootFolder());
        }
    }
}
