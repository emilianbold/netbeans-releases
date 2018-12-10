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

import com.sun.javacard.AID;
import java.awt.EventQueue;
import java.io.File;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class ClassicAppletProjectProperties extends AppletProjectProperties {

    private AID originalPackageAID;
    private AID packageAID;
    private boolean useMyProxies;
    private boolean wasUseMyProxies;

    public ClassicAppletProjectProperties(JCProject project) {
        super(project);
    }

    public AID getPackageAID() {
        return packageAID;
    }

    public void setPackageAID(AID aid) {
        packageAID = aid;
    }

    public boolean isUseMyProxies() {
        return useMyProxies;
    }

    public void setUseMyProxies(boolean useMyProxies) {
        this.useMyProxies = useMyProxies;
    }

    private void rewriteManifest() throws IOException {
        FileObject manifestFo = project.getProjectDirectory().getFileObject(JCConstants.MANIFEST_PATH); //NOI18N
        Manifest manifest = null;
        if (manifestFo == null) {
            Logger.getLogger(ClassicAppletProjectProperties.class.getName()).log(
                    Level.INFO, "Manifest missing for project {0}.  Recreating.",  //NOI18N
                    project.getProjectDirectory().getPath());
            manifestFo = project.getProjectDirectory().createData(JCConstants.MANIFEST_PATH); //NOI18N
            manifest = new Manifest();
            Attributes a = manifest.getMainAttributes();
            a.putValue (JCConstants.MANIFEST_ENTRY_CLASSIC_RUNTIME_DESCRIPTOR_VERSION, "3.0"); //NOI18N
            a.putValue (JCConstants.MANIFEST_APPLICATION_TYPE, project.kind().getManifestApplicationType());
        } else {
            InputStream in = manifestFo.getInputStream();
            try {
                manifest = new Manifest (in);
            } finally {
                in.close();
            }
        }
        manifest.getMainAttributes().putValue(
                JCConstants.MANIFEST_ENTRY_CLASSIC_PACKAGE_AID, packageAID.toString());
        FileLock lock = manifestFo.lock();
        OutputStream out = manifestFo.getOutputStream(lock);
        try {
            manifest.write(out);
        } finally {
            out.close();
            lock.releaseLock();
        }
    }

    public static final String PROXY_SOURCE_DIR = "proxies";  //NOI18N
    @Override
    protected void onBeforeStoreProperties() throws IOException {
        super.onBeforeStoreProperties();
        if ((wasUseMyProxies != useMyProxies) || (useMyProxies && project.getProjectDirectory().getFileObject(PROXY_SOURCE_DIR) == null)) {
            updateProxies(this, useMyProxies, SOURCE_ROOTS_MODEL);
        }
    }

    static void updateProxies (JCProjectProperties props, boolean useMyProxies, DefaultTableModel srcRootsModel) throws IOException {
        FileObject root = props.getProject().getProjectDirectory();
        FileObject proxies = root.getFileObject (PROXY_SOURCE_DIR);
        boolean created = false;
        if (useMyProxies) {
            if (proxies == null) {
                proxies = root.createFolder(PROXY_SOURCE_DIR);
                created = true;
            }
            int max = props.SOURCE_ROOTS_MODEL.getRowCount();
            boolean found = false;
            File proxiesFile = FileUtil.toFile(proxies);
            String proxiesPath = proxiesFile.getAbsolutePath();
            for (int i=0; i < max; i++) {
                File f = (File) srcRootsModel.getValueAt(i, 0);
                if (f.getAbsolutePath().equals(proxiesPath)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                String label = NbBundle.getMessage(ClassicAppletProjectProperties.class, "LBL_PROXY_SOURCES");  //NOI18N
                srcRootsModel.addRow(new Object[] { proxiesFile, label });
            }
        } else {
            if (proxies != null) {
                File f = FileUtil.toFile(proxies);
                int max = srcRootsModel.getRowCount();
                Vector<?> v = srcRootsModel.getDataVector();
                int toRemove = -1;
                for (int i= 0; i < max; i++) {
                    File test = (File) ((Vector<?>)(v.elementAt(i))).elementAt(0);
                    boolean match = test.getAbsolutePath().equals(f.getAbsolutePath());
                    if (match) {
                        toRemove = i;
                        break;
                    }
                }
                if (toRemove != -1) {
                    srcRootsModel.removeRow(toRemove);
                }
            }
        }
        if (created || (proxies != null && isEmpty(proxies))) {
            offerToGenerateProxies(props.getProject());
        }
    }

    private static boolean isEmpty (FileObject proxies) {
        //XXX scan for classes that implement import javacard.framework.Shareable
        //and map them back to classes in the src dirs.
        return false;
    }

    private static void offerToGenerateProxies(JCProject project) {
        Parameters.notNull("project", project);  //NOI18N
        //unit tests
        if (Boolean.getBoolean("JCProject.test")) return; //NOI18N
        assert project.kind().isClassic();
        //Do this after everything has been written to disk and with no
        //mutexes held - currently we're inside ProjectManager.mutex().
        //Not a good idea to show a modal dialog while holding locks.
        EventQueue.invokeLater (new ProxyGenerator(project));
    }

    private static final class ProxyGenerator implements Runnable {
        private final JCProject project;

        public ProxyGenerator(JCProject project) {
            this.project = project;
        }

        public void run() {
            if (EventQueue.isDispatchThread()) {
                String msg = NbBundle.getMessage (ClassicAppletProjectProperties.class, "ASK_GENERATE_PROXIES"); //NOI18N
                String title= NbBundle.getMessage(ClassicAppletProjectProperties.class, "TITLE_ASK_GENERATE_PROXIES"); //NOI18N
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.YES_NO_OPTION);
                if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(nd))) {
                    RequestProcessor.getDefault().post (this);
                }
            } else {
                FileObject buildFo = project.getProjectDirectory().getFileObject("build.xml"); //NOI18N
                if (buildFo != null) {
                    try {
                        ExecutorTask task = ActionUtils.runTarget(buildFo,
                                new String[]{"generate-sio-proxies"}, //NOI18N
                                new Properties());
                        task.getInputOutput().select();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    @Override
    protected boolean doStoreProperties(EditableProperties props) throws IOException {
        if (wasUseMyProxies != useMyProxies) {
            props.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES, String.valueOf(useMyProxies));
            if (useMyProxies) {
                props.setProperty (ProjectPropertyNames.PROJECT_PROP_PROXY_SRC_DIR,
                        PROXY_SOURCE_DIR);
            } else if (PROXY_SOURCE_DIR.equals(props.getProperty(ProjectPropertyNames.PROJECT_PROP_PROXY_SRC_DIR))) {
                props.remove (ProjectPropertyNames.PROJECT_PROP_PROXY_SRC_DIR);
                props.remove (ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES);
            }
        }
        if (packageAID != null && !packageAID.equals(originalPackageAID)) {
            props.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_PACKAGE_AID, packageAID.toString());
            rewriteManifest();
        }
        // #198931
        return true;
    }

    @Override
    protected void onInit(PropertyEvaluator eval) {
        useMyProxies = Boolean.parseBoolean(project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES));
        wasUseMyProxies = useMyProxies;
        String aidString = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_PACKAGE_AID);
        if (aidString != null) {
            try {
                packageAID = AID.parse(aidString);
                originalPackageAID = packageAID;
            } catch (IllegalArgumentException e) {
                Logger.getLogger(ClassicAppletProjectProperties.class.getName()).log(Level.INFO,
                        "Bad classic package aid in " + //NOI18N
                        project.getProjectDirectory().getPath() + ": " + aidString, e); //NOI18N
            }
        }
    }
}
