/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.runjar;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.MavenSourcesImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class RunJarPrereqChecker implements PrerequisitesChecker {

    private String mainClass;

    public boolean checkRunConfig(RunConfig config) {
        String actionName = config.getActionName();
        Set<Map.Entry<Object, Object>> entries = config.getProperties().entrySet();
        for (Map.Entry<Object, Object> str : entries) {
            if ("exec.executable".equals(str.getKey())) { //NOI18N
                // check for "java" and replace it with absolute path to
                // project j2seplaform's java.exe
                String val = (String) str.getValue();
                if ("java".equals(val)) { //NOI18N
                    //TODO somehow use the config.getMavenProject() call rather than looking up the
                    // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
                    ActiveJ2SEPlatformProvider plat = config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                    assert plat != null;
                    FileObject fo = plat.getJavaPlatform().findTool(val);
                    if (fo != null) {
                        File fl = FileUtil.toFile(fo);
                        config.setProperty("exec.executable", fl.getAbsolutePath()); //NOI18N
                    }
                }
            }
        }

        if ((ActionProvider.COMMAND_RUN.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                "profile".equals(actionName)) &&
                NbMavenProject.TYPE_JAR.equals(
                config.getProject().getLookup().lookup(NbMavenProject.class).getPackagingType())) {
            String mc = null;
            for (Map.Entry<Object, Object> str : entries) {
                String val = (String) str.getValue();
                if (val.contains("${packageClassName}")) { //NOI18N
                    //show dialog to choose main class.
                    if (mc == null) {
                        mc = eventuallyShowDialog(config.getProject(), actionName);
                    }
                    if (mc == null) {
                        return false;
                    }
                    val = val.replace("${packageClassName}", mc); //NOI18N
                    config.setProperty((String) str.getKey(), val);
                }
            }
        }
        return true;
    }


    private String eventuallyShowDialog(Project project, String actionName) {
        if (mainClass != null) {
            return mainClass;
        }
        List<FileObject> roots = new ArrayList<FileObject>();
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            SourceGroup sourceGroup = grps[i];
            if (MavenSourcesImpl.NAME_SOURCE.equals(sourceGroup.getName())) {
                roots.add(sourceGroup.getRootFolder());
            }
        }
        grps = srcs.getSourceGroups(MavenSourcesImpl.TYPE_GEN_SOURCES);
        for (int i = 0; i < grps.length; i++) {
            SourceGroup sourceGroup = grps[i];
            roots.add(sourceGroup.getRootFolder());
        }
        final JButton okButton = new JButton(NbBundle.getMessage(RunJarPrereqChecker.class, "LBL_ChooseMainClass_OK"));
//        JButton okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (RunJarPanel.class, "AD_ChooseMainClass_OK"));


        final MainClassChooser panel = new MainClassChooser(roots.toArray(new FileObject[0]));
        Object[] options = new Object[]{
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        panel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof MouseEvent && MouseUtils.isDoubleClick(((MouseEvent) e.getSource()))) {
                    // click button and finish the dialog with selected class
                    okButton.doClick();
                } else {
                    okButton.setEnabled(panel.getSelectedMainClass() != null);
                }
            }
        });
        panel.rbSession.setSelected(true);
        okButton.setEnabled(false);
        DialogDescriptor desc = new DialogDescriptor(
                panel,
                NbBundle.getMessage(RunJarPrereqChecker.class, "LBL_ChooseMainClass_Title"),
                true,
                options,
                options[0],
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (okButton == desc.getValue()) {
            if (panel.rbSession.isSelected()) {
                mainClass = panel.getSelectedMainClass();
            } else if (panel.rbPermanent.isSelected()) {
                writeMapping(actionName, project, panel.getSelectedMainClass());
            }
            return panel.getSelectedMainClass();
        }
        return null;
    }

    private void writeMapping(String actionName, Project project, String clazz) {
        try {
            M2ConfigProvider usr = project.getLookup().lookup(M2ConfigProvider.class);
            ActionToGoalMapping mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(usr.getDefaultConfig().getRawMappingsAsString()));
            NetbeansActionMapping mapp = ActionToGoalUtils.getDefaultMapping(actionName, project);
            mapping.addAction(mapp);
            Set<Map.Entry<Object, Object>> entries = mapp.getProperties().entrySet();
            for (Map.Entry<Object, Object> str : entries) {
                String val = (String) str.getValue();
                if (val.contains("${packageClassName}")) { //NOI18N
                    //show dialog to choose main class.
                    val = val.replace("${packageClassName}", clazz); //NOI18N
                    str.setValue(val);
                }
            }
            //TODO we should definitely write to the mappings of active configuration here..
            CustomizerProviderImpl.writeNbActionsModel(project, mapping, M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
        } catch (Exception e) {
            Exceptions.attachMessage(e, "Cannot persist action configuration.");
            Exceptions.printStackTrace(e);
        }
    }
}
