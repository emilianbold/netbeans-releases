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
package org.netbeans.modules.websvc.axis2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Utils;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.netbeans.modules.websvc.axis2.services.model.ServicesUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service = ProjectOpenedHook.class,
projectType = "org-netbeans-modules-java-j2seproject")
public class Axis2OpenHook extends ProjectOpenedHook {

    private String AXIS2_EXTENSION = "axis2"; //NOI18N
    private Project prj;
    private FileChangeListener axis2CreationListener;
    private PropertyChangeListener axis2Listener;

    /** Creates a new instance of JaxWSLookupProvider */
    public Axis2OpenHook(Project prj) {
        this.prj = prj;
    }

    protected void projectOpened() {
        final Axis2ModelProvider axis2ModelProvider = prj.getLookup().lookup(Axis2ModelProvider.class);
        if (axis2ModelProvider != null) {
            FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
            axis2CreationListener = new FileChangeAdapter() {

                public void fileDataCreated(FileEvent fe) {
                    if ("axis2.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                        FileObject axis2_fo = getAxisConfigFileObject(prj);
                        final Axis2Model axis2Model = Axis2Utils.getAxis2Model(axis2_fo, true);
                        if (axis2Model != null) {
                            axis2Listener = new PropertyChangeListener() {

                                public void propertyChange(PropertyChangeEvent evt) {
                                    handleAxis2BuildScript(axis2Model);
                                }
                            };
                            axis2Model.getRootComponent().addPropertyChangeListener(axis2Listener);
                            axis2ModelProvider.setAxis2Model(axis2Model);
                        }
                    }
                }

                public void fileDeleted(FileEvent fe) {
                    if ("axis2.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                        Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
                        if (axis2Model != null) {
                            axis2Model.getRootComponent().removePropertyChangeListener(axis2Listener);
                        }
                        axis2Listener = null;
                        axis2ModelProvider.setAxis2Model(null);

                        AntBuildExtender antExtender = prj.getLookup().lookup(AntBuildExtender.class);
                        FileObject axis2_build = prj.getProjectDirectory().getFileObject(TransformerUtils.AXIS2_BUILD_XML_PATH);
                        try {
                            removeAxis2Extension(prj, axis2_build, antExtender);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            };
            nbprojectDir.addFileChangeListener(axis2CreationListener);

            FileObject axis2Fo = getAxisConfigFileObject(prj);
            if (axis2Fo != null) {
                final Axis2Model axis2Model = Axis2Utils.getAxis2Model(axis2Fo, true);
                if (axis2Model != null) {
                    FileObject axis2_build = prj.getProjectDirectory().getFileObject(TransformerUtils.AXIS2_BUILD_XML_PATH);
                    if (axis2_build == null) {
                        try {
                            AntBuildExtender antExtender = prj.getLookup().lookup(AntBuildExtender.class);
                            addAxis2Extension(prj, antExtender);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    axis2Listener = new PropertyChangeListener() {

                        public void propertyChange(PropertyChangeEvent evt) {
                            handleAxis2BuildScript(axis2Model);
                        }
                    };
                    axis2Model.getRootComponent().addPropertyChangeListener(axis2Listener);
                    axis2ModelProvider.setAxis2Model(axis2Model);
                }
            }
            FileObject servicesFo = getServicesFileObject(prj);
            if (servicesFo != null) {
                ServicesModel servicesModel = ServicesUtils.getServicesModel(servicesFo, true);
                if (servicesModel != null) {
                    axis2ModelProvider.setServicesModel(servicesModel);
                }
            }
            // setting axis properties
            final Preferences preferences = AxisUtils.getPreferences();
            try {
                String axisDeploy = preferences.get("AXIS_DEPLOY", null); //NOI18N
                if (axisDeploy != null) {
                    AxisUtils.updateAxisDeployProperty(prj, axisDeploy);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void projectClosed() {
        Axis2ModelProvider axis2ModelProvider = prj.getLookup().lookup(Axis2ModelProvider.class);
        if (axis2ModelProvider != null) {
            FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
            if (nbprojectDir != null) {
                nbprojectDir.removeFileChangeListener(axis2CreationListener);
                Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
                if (axis2Model != null) {
                    axis2Model.getRootComponent().removePropertyChangeListener(axis2Listener);
                }
            }
        }
    }

    private void handleAxis2BuildScript(Axis2Model axis2Model) {
        final AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
        if (ext != null) {
            if (axis2Model.getRootComponent().getServices().size() == 0) {
                // remove nbproject/axis2.xml
                // remove the axis2 extension
                final FileObject axis2_build = prj.getProjectDirectory().getFileObject(TransformerUtils.AXIS2_BUILD_XML_PATH);
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        try {
                            removeAxis2Extension(prj, axis2_build, ext);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, 500);
            } else {
                // re-generate nbproject/axis2-build.xml
                // add axis2 extension, if needed
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        try {
                            addAxis2Extension(prj, ext);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, 500);
            }
        }
    }

    private void updateAxisProperties(String axisHome, String axisDeploy) throws IOException {
        EditableProperties ep = AxisUtils.getEditableProperties(prj, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        boolean needToStore = false;
        if (ep != null) {
            String oldAxisDeploy = ep.getProperty("axis2.deploy.war"); //NOI18N
            if (oldAxisDeploy == null) {
                oldAxisDeploy = ep.getProperty("axis2.deploy.dir"); //NOI18N
            }
            if (axisDeploy != null && !axisDeploy.equals(oldAxisDeploy)) {
                if (axisDeploy.endsWith(".war")) { //NOI18N
                    ep.setProperty("axis2.deploy.war", axisDeploy); //NOI18N
                    ep.remove("axis2.deploy.dir"); // NOI18N
                } else {
                    ep.setProperty("axis2.deploy.dir", axisDeploy); //NOI18N
                    ep.remove("axis2.deploy.war"); //NOI18N
                }
            }
        }
        AxisUtils.storeEditableProperties(prj, AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
    }

    private FileObject getAxisConfigFileObject(Project prj) {
        return prj.getProjectDirectory().getFileObject(TransformerUtils.AXIS2_XML_PATH);
    }

    private FileObject getServicesFileObject(Project prj) {
        FileObject servicesFolder = AxisUtils.getServicesFolder(prj.getProjectDirectory(), false);
        if (servicesFolder != null) {
            return servicesFolder.getFileObject("services", "xml"); // NOI18N
        } else {
            return null;
        }
    }

    private void addAxis2Extension(
            Project prj,
            AntBuildExtender antExtender) throws IOException {

        assert antExtender != null;
        TransformerUtils.transform(prj.getProjectDirectory());
        FileObject axis2_build = prj.getProjectDirectory().getFileObject(TransformerUtils.AXIS2_BUILD_XML_PATH);
        assert axis2_build != null;
        AntBuildExtender.Extension extension = antExtender.getExtension(AXIS2_EXTENSION);
        if (extension == null) {
            extension = antExtender.addExtension(AXIS2_EXTENSION, axis2_build);
            //adding dependencies
            extension.addDependency("jar", "axis2-aar"); //NOI18N
            ProjectManager.getDefault().saveProject(prj);
        }
    }

    private void removeAxis2Extension(
            Project prj,
            FileObject axis2_build,
            AntBuildExtender antExtender) throws IOException {
        assert antExtender != null;
        AntBuildExtender.Extension extension = antExtender.getExtension(AXIS2_EXTENSION);
        if (extension != null) {
            antExtender.removeExtension(AXIS2_EXTENSION);
            ProjectManager.getDefault().saveProject(prj);
        }
        if (axis2_build != null) {
            FileLock fileLock = axis2_build.lock();
            if (fileLock != null) {
                try {
                    axis2_build.delete(fileLock);
                } finally {
                    fileLock.releaseLock();
                }
            }
        }

    }
}
