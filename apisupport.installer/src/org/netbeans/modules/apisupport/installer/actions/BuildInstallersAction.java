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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.apisupport.installer.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.ErrorManager;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

public final class BuildInstallersAction extends AbstractAction implements ContextAwareAction {

    private static BuildInstallersAction inst = null;

    private BuildInstallersAction() {
    }

    public static synchronized BuildInstallersAction getDefault() {
        if (inst == null) {
            inst = new BuildInstallersAction();
        }
        return inst;
    }

    public static void actionPerformed(Node[] e) {
        ContextBuildInstaller.actionPerformed(e);
    }

    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextBuildInstaller();
    }

    static class ContextBuildInstaller extends AbstractAction implements Presenter.Popup {

        public ContextBuildInstaller() {
            putValue(NAME, NbBundle.getMessage(BuildInstallersAction.class, "CTL_BuildInstallers"));
        }

        public void actionPerformed(ActionEvent e) {
            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length > 0) {
                ContextBuildInstaller.actionPerformed(n);
            } else {
                ContextBuildInstaller.actionPerformed((Node[]) null);
            }
        }

        public static void actionPerformed(Node[] e) {
            if (e != null) {
                for (Node node : e) {
                    final Project prj = node.getLookup().lookup(Project.class);
                    if (prj != null) {
                        File suiteLocation = FileUtil.toFile(prj.getProjectDirectory());
                        FileObject propertiesFile = prj.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        Properties ps = new Properties();
                        String appName = "";
                        try {
                            InputStream is = propertiesFile.getInputStream();
                            ps.load(is);
                            appName = ps.getProperty("app.name");
                        } catch (IOException ex) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "Can`t store properties", ex);
                        }
                        //Logger.getLogger(BuildInstallersAction.class.getName()).warning("actionPerformed for " + suiteLocation);
                        Properties props = new Properties();
                        props.put("suite.location", suiteLocation.getAbsolutePath().replace("\\", "/"));
                        props.put("suite.nbi.product.uid",
                                appName.replaceAll("[0-9]+", "").toLowerCase(Locale.ENGLISH));


                        props.put("nbi.stub.location", InstalledFileLocator.getDefault().locate(
                                "nbi/stub",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        props.put(
                                "nbi.stub.common.location", InstalledFileLocator.getDefault().locate(
                                "nbi/.common",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));

                        props.put(
                                "nbi.ant.tasks.jar", InstalledFileLocator.getDefault().locate(
                                "modules/ext/nbi-ant-tasks.jar",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        props.put(
                                "nbi.registries.management.jar", InstalledFileLocator.getDefault().locate(
                                "modules/ext/nbi-registries-management.jar",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));
                        props.put(
                                "nbi.engine.jar", InstalledFileLocator.getDefault().locate(
                                "modules/ext/nbi-engine.jar",
                                "org.netbeans.modules.apisupport.installer", false).getAbsolutePath().replace("\\", "/"));

                        List<String> platforms = new ArrayList<String>();
                        for (Object s : ps.keySet()) {
                            String key = (String) s;
                            String prefix = "installer.os.";
                            if (key.startsWith(prefix) && ps.get(key).equals("true")) {
                                platforms.add(key.substring(prefix.length()));
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < platforms.size(); i++) {
                            if (i != 0) {
                                sb.append(" ");
                            }
                            sb.append(platforms.get(i));
                        }

                        props.put("generate.installer.for.platforms",
                                sb.toString());

                        File javaHome = new File(System.getProperty("java.home"));
                        if (new File(javaHome,
                                "lib/rt.jar").exists() && javaHome.getName().equals("jre")) {
                            javaHome = javaHome.getParentFile();
                        }
                        props.put(
                                "generator-jdk-location-forward-slashes", javaHome.getAbsolutePath().replace("\\", "/"));
                        /*
                        props.put(
                                "generated-installers-location-forward-slashes",
                                new File(suiteLocation, "dist").getAbsolutePath().replace("\\", "/"));
                        */
                        props.put(
                                "pack200.enabled", "false");

                        /*
                        for (Object s : props.keySet()) {
                        Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.INFO,
                        "[" + s + "] = " + props.get(s));
                        }
                         */
                        /*
                        File tmpProps = null;
                        try {
                            tmpProps = File.createTempFile("nbi-properties-", ".properties");
                            FileOutputStream fos = new FileOutputStream(tmpProps);
                            props.store(fos, null);
                            fos.close();
                        } catch (IOException ex) {
                            Logger.getLogger(BuildInstallersAction.class.getName()).log(Level.WARNING, "Can`t store properties", ex);
                        }*/
                        try {
                            final ExecutorTask executorTask = ActionUtils.runTarget(findGenXml(prj), new String[]{"build"}, props);
                            /*
                            executorTask.addTaskListener(new TaskListener() {

                            public void taskFinished(Task task) {
                            if (executorTask.result() == 0) {
                            try {
                            ActionUtils.runTarget(findInstXml(prj), new String[]{"build"}, new Properties());
                            } catch (FileStateInvalidException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                            } catch (IOException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N

                            }
                            }
                            }
                            });*/
                        } catch (FileStateInvalidException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                        } catch (IOException ex) {
                            ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project").notify(ex); // NOI18N
                        }

                        /*
                        if (tmpProps != null && !tmpProps.delete() && tmpProps.exists()) {
                            tmpProps.deleteOnExit();
                        }*/
                    }
                }

            }



        }

        private static FileObject findBuildXml(Project project) {
            return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        }

        private static FileObject findGenXml(Project project) {
            return FileUtil.toFileObject(InstalledFileLocator.getDefault().locate(
                    "nbi/stub/template.xml",
                    "org.netbeans.modules.apisupport.installer", false));
        }

        private static FileObject findInstXml(Project project) throws FileStateInvalidException {
            return project.getProjectDirectory().getFileObject("build/installer/build.xml");
        }

        public JMenuItem getPopupPresenter() {
            Node[] n = WindowManager.getDefault().getRegistry().getActivatedNodes();
            if (n.length == 1) {
                Project prj = n[0].getLookup().lookup(Project.class);
                if (prj != null && prj.getClass().getSimpleName().equals("SuiteProject")) {
                    return new JMenuItem(this);
                }
            }

            JMenuItem dummy = new JMenuItem();
            dummy.setVisible(false);
            return dummy;

        }
    }
}


