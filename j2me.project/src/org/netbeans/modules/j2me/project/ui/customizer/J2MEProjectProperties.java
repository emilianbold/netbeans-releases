/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui.customizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class J2MEProjectProperties {
    
    public static final String JAVAME_ENABLED = "javame.enabled"; // NOI18N

    /**
     * Keeps singleton instance of JFXProjectProperties for any fx project for
     * which property customizer is opened at once
     */
    private static Map<String, J2MEProjectProperties> propInstance = new HashMap<String, J2MEProjectProperties>();

    // Project related references
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project project;

    public Project getProject() {
        return project;
    }

    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }

    static J2MEProjectProperties getInstance(Lookup lookup) {
        Project proj = lookup.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        J2MEProjectProperties prop = propInstance.get(projDir);
        if (prop == null) {
            prop = new J2MEProjectProperties(lookup);
            propInstance.put(projDir, prop);
        }
        return prop;
    }

    /**
     * Creates a new instance of J2MEProjectProperties
     */
    private J2MEProjectProperties(Lookup context) {

        //defaultInstance = provider.getJFXProjectProperties();
        project = context.lookup(Project.class);

        if (project != null) {
            j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            evaluator = j2sePropEval.evaluator();

//            // Packaging
//            binaryEncodeCSS = fxPropGroup.createToggleButtonModel(evaluator, JAVAFX_BINARY_ENCODE_CSS); // set true by default in JFXProjectGenerator
//
//            // Deployment
//            allowOfflineModel = fxPropGroup.createToggleButtonModel(evaluator, ALLOW_OFFLINE); // set true by default in JFXProjectGenerator            
//            backgroundUpdateCheck = fxPropGroup.createToggleButtonModel(evaluator, UPDATE_MODE_BACKGROUND); // set true by default in JFXProjectGenerator
//            installPermanently = fxPropGroup.createToggleButtonModel(evaluator, INSTALL_PERMANENTLY);
//            addDesktopShortcut = fxPropGroup.createToggleButtonModel(evaluator, ADD_DESKTOP_SHORTCUT);
//            addStartMenuShortcut = fxPropGroup.createToggleButtonModel(evaluator, ADD_STARTMENU_SHORTCUT);
//            disableProxy = fxPropGroup.createToggleButtonModel(evaluator, DISABLE_PROXY);
//
//            // CustomizerRun
//            CONFIGS = new JFXConfigs();
//            CONFIGS.read();
//            initPreloaderArtifacts(project, CONFIGS);
//            CONFIGS.setActive(evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG));
//            preloaderClassModel = new PreloaderClassComboBoxModel();
//
//            initVersion(evaluator);
//            initIcons(evaluator);
//            initSigning(evaluator);
//            initNativeBundling(evaluator);
//            initResources(evaluator, project, CONFIGS);
//            initJSCallbacks(evaluator);
//            initRest(evaluator);
        }
    }
    
    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }
    
    /** Getter method */
    public static J2MEProjectProperties getInstanceIfExists(Project proj) {
        assert proj != null;
        String projDir = proj.getProjectDirectory().getPath();
        J2MEProjectProperties prop = propInstance.get(projDir);
        if(prop != null) {
            return prop;
        }
        return null;
    }

    /** Getter method */
    public static J2MEProjectProperties getInstanceIfExists(Lookup context) {
        Project proj = context.lookup(Project.class);
        return getInstanceIfExists(proj);
    }

    public static void cleanup(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        propInstance.remove(projDir);
    }
        
    public void store() throws IOException {
        String meEnabled = evaluator.getProperty(JAVAME_ENABLED);
        if(isTrue(meEnabled)) {
            storeME();
        }
    }
    
    private void storeME() throws IOException {
//        updatePreloaderDependencies(CONFIGS);
//        CONFIGS.storeActive();
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final EditableProperties pep = new EditableProperties(true);
        final FileObject privPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    final InputStream is = projPropsFO.getInputStream();
                    final InputStream pis = privPropsFO.getInputStream();
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    try {
                        pep.load(pis);
                    } finally {
                        if (pis != null) {
                            pis.close();
                        }
                    }
                    
//                    fxPropGroup.store(ep);
//                    storeRest(ep, pep);
//                    CONFIGS.store(ep, pep);
//                    updatePreloaderComment(ep);
//                    logProps(ep);

                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    try {
                        lock = privPropsFO.lock();
                        os = privPropsFO.getOutputStream(lock);
                        pep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }
    
    private J2MECompilingPanel compilingPanel = null;
    public J2MECompilingPanel getCompilingPanel() {
        if (compilingPanel == null) {
            compilingPanel = new J2MECompilingPanel(this);
        }
        return compilingPanel;
    }
    
    private J2MEPackagingPanel packagingPanel = null;
    public J2MEPackagingPanel getPackagingPanel() {
        if (packagingPanel == null) {
            packagingPanel = new J2MEPackagingPanel(this);
        }
        return packagingPanel;
    }
    
    private J2MERunPanel runPanel = null;
    public J2MERunPanel getRunPanel() {
        if (runPanel == null) {
            runPanel = new J2MERunPanel(this);
        }
        return runPanel;
    }
    
    private J2MEApplicationPanel applicationPanel = null;
    public J2MEApplicationPanel getApplicationPanel() {
        if (applicationPanel == null) {
            applicationPanel = new J2MEApplicationPanel(this);
        }
        return applicationPanel;
    }
    
    private J2MEDeploymentPanel deploymentPanel = null;
    public J2MEDeploymentPanel getDeploymentPanel() {
        if (deploymentPanel == null) {
            deploymentPanel = new J2MEDeploymentPanel(this);
        }
        return deploymentPanel;
    }
}
