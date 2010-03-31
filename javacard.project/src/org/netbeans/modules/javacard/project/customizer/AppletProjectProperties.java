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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import org.netbeans.modules.javacard.project.*;
import com.sun.javacard.AID;
import com.sun.javacard.filemodels.AppletXmlModel;
import com.sun.javacard.filemodels.DeploymentXmlModel;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.openide.filesystems.FileObject;

/**
 * Project properties for EAP and CAP projects.
 *
 * @author Tim Boudreau
 */
public class AppletProjectProperties extends JCProjectProperties {

    private final ProjectKind kind;

    public AppletProjectProperties(JCProject project) {
        super(project);
        kind = project.kind();
        init(project);
    }
    private AID defaultApplet;

    public void setDefaultApplet(AID defaultApplet) {
        this.defaultApplet = defaultApplet;
    }

    public AID getDefaultApplet() {
        return defaultApplet;
    }

    private void init(JCProject project) {
        group = new StoreGroup();
        PropertyEvaluator eval = project.evaluator();
        onInit(eval);
        SEND_SCRIPT = group.createToggleButtonModel(eval, ProjectPropertyNames.PROJECT_PROP_RUN_APDUTOOL);

        String scriptsPath = kind.isApplet() ? JCConstants.SCRIPTS_DIR_PATH : kind == ProjectKind.WEB ? JCConstants.HTML_FILE_PATH : null;
        FileObject scriptsDir = scriptsPath == null ? null : project.getProjectDirectory().getFileObject(scriptsPath);
        if (scriptsDir != null) {
            Vector<String> scripts = new Vector<String>();
            for (FileObject fo : scriptsDir.getChildren()) {
                if ("text/x-apduscr".equals(fo.getMIMEType())) { //NOI18N
                    scripts.add(FileUtil.getRelativePath(project.getProjectDirectory(), fo));
                }
            }
            SCRIPTS = new DefaultComboBoxModel(scripts);
        } else {
            SCRIPTS = new DefaultComboBoxModel(new Object[]{
                        NbBundle.getMessage(AppletProjectProperties.class,
                        "SCRIPTS_DIR_BAD", scriptsDir) //NOI18N
                    });
            scriptsPathBad = true;
        }
        String mainScript = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_MAIN_SCRIPT_FILE);
        SCRIPTS.setSelectedItem(mainScript);
    }
    private AppletXmlModel appletXmlFileModel;
    private AppletXmlModel appletXmlUiModel;
    private DeploymentXmlModel deploymentXmlFileModel;
    private DeploymentXmlModel deploymentXmlUIModel;
    private StoreGroup group;
    public ButtonModel SEND_SCRIPT;
    public ComboBoxModel SCRIPTS = new DefaultComboBoxModel();
    private boolean scriptsPathBad;

    public DeploymentXmlModel getDeploymentXmlFileModel() {
        return deploymentXmlFileModel;
    }

    public void setDeploymentXmlFileModel(DeploymentXmlModel deploymentXmlFileModel) {
        this.deploymentXmlFileModel = deploymentXmlFileModel;
    }

    public DeploymentXmlModel getDeploymentXmlUIModel() {
        return deploymentXmlUIModel;
    }

    public void setDeploymentXmlUIModel(DeploymentXmlModel deploymentXmlUIModel) {
        this.deploymentXmlUIModel = deploymentXmlUIModel;
    }

    public void setAppletXmlFromFile(AppletXmlModel aidFromFile) {
        this.appletXmlFileModel = aidFromFile;
    }

    public void setAppletXmlFromUI(AppletXmlModel aidFromUI) {
        this.appletXmlUiModel = aidFromUI;
    }

    @Override
    protected final Boolean onStoreProperties(EditableProperties props) throws IOException {
        super.onStoreProperties(props);
        boolean result = doStoreProperties(props);
        if (result) {
            group.store(props);
            if (!scriptsPathBad) {
                if (SEND_SCRIPT.isSelected() && SCRIPTS.getSelectedItem() != null) {
                    props.setProperty(ProjectPropertyNames.PROJECT_PROP_MAIN_SCRIPT_FILE,
                            (String) SCRIPTS.getSelectedItem());
                } else {
                    props.remove(ProjectPropertyNames.PROJECT_PROP_MAIN_SCRIPT_FILE);
                }
            }
            if (kind.isApplet()) { //Also used by Clslibproject, to store classic package AID
                storeDeploymentXml();
                storeAppletXML();
                AppletXmlModel xmlModel = appletXmlUiModel;
                if (xmlModel == null) {
                    //Make a new one - we need to write the applet_aid properties
                    FileObject fo = project.getProjectDirectory().getFileObject(
                            JCConstants.APPLET_DESCRIPTOR_PATH); //NOI18N
                    if (fo == null) {
                        fo = FileUtil.createData(project.getProjectDirectory(),
                                JCConstants.APPLET_DESCRIPTOR_PATH);
                    }
                    if (fo != null && System.getProperty("JCProject.test") == null) {
                        InputStream in = fo.getInputStream();
                        try {
                            PEH peh = new PEH();
                            xmlModel = new AppletXmlModel(in, peh);
                        } finally {
                            in.close();
                        }
                    }
                }
            }
        }
        return result;
    }

    private static class PEH implements com.sun.javacard.filemodels.ParseErrorHandler {

        public void handleError(IOException ioe) throws IOException {
            throw ioe;
        }

        public void handleBadAIDError(IllegalArgumentException aidParseError, String aidString) {
            //do nothing
        }

        public void unrecognizedElementEncountered(String elementName) throws IOException {
            //do nothing
        }
    }

    protected boolean doStoreProperties(EditableProperties props) throws IOException {
        //do nothing, for subclasses
        return true;
    }

    protected void onInit(PropertyEvaluator eval) {
        //do nothing, for subclasses
    }

    private void storeAppletXML() throws IOException {
        boolean shouldStore = appletXmlUiModel != null &&
                !appletXmlUiModel.equals(appletXmlFileModel);

        if (shouldStore) {
            FileObject fo = project.getProjectDirectory().getFileObject(
                    JCConstants.APPLET_DESCRIPTOR_PATH); //NOI18N
            FileLock lock = fo.lock();
            OutputStream out = fo.getOutputStream(lock);
            PrintWriter writer = new PrintWriter(out);
            try {
                writer.println(appletXmlUiModel.toXml());
            } finally {
                writer.close();
                out.close();
                lock.releaseLock();
            }
            appletXmlFileModel = appletXmlUiModel;
        }
    }

    private void storeDeploymentXml() throws IOException {
        boolean shouldStore = deploymentXmlUIModel != null &&
                !deploymentXmlUIModel.equals(deploymentXmlFileModel);
        if (shouldStore) {
            FileObject fo = project.getProjectDirectory().getFileObject(
                    JCConstants.DEPLOYMENT_XML_PATH);
            if (fo == null) {
                fo = FileUtil.createData(project.getProjectDirectory(),
                        JCConstants.DEPLOYMENT_XML_PATH);
            }
            FileLock lock = fo.lock();
            OutputStream out = fo.getOutputStream(lock);
            PrintWriter writer = new PrintWriter(out);
            try {
                writer.println(deploymentXmlUIModel.toXml());
            } finally {
                writer.close();
                out.close();
                lock.releaseLock();
            }
            deploymentXmlUIModel = deploymentXmlFileModel;
        }
    }
}
