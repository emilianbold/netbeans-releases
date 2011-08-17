/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.runjar;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.classpath.MavenSourcesImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import static org.netbeans.modules.maven.runjar.Bundle.*;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * @author mkleint
 */
@ProjectServiceProvider(service=PrerequisitesChecker.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR)
public class RunJarPrereqChecker implements PrerequisitesChecker {

    private String mainClass;

    @Override public boolean checkRunConfig(RunConfig config) {
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

        assert NbMavenProject.TYPE_JAR.equals(config.getProject().getLookup().lookup(NbMavenProject.class).getPackagingType());
        if ((ActionProvider.COMMAND_RUN.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                "profile".equals(actionName))) {
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

    @Messages({
        "LBL_ChooseMainClass_Title=Select Main Class for Execution",
        "LBL_ChooseMainClass_OK=Select Main Class"
    })
    private String eventuallyShowDialog(Project project, String actionName) {
        if (mainClass != null) {
            return mainClass;
        }
        List<FileObject> roots = new ArrayList<FileObject>();
        Sources srcs = ProjectUtils.getSources(project);
        for (SourceGroup sourceGroup : srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (MavenSourcesImpl.NAME_SOURCE.equals(sourceGroup.getName())) {
                roots.add(sourceGroup.getRootFolder());
            }
        }
        for (SourceGroup sourceGroup : srcs.getSourceGroups(MavenSourcesImpl.TYPE_GEN_SOURCES)) {
            roots.add(sourceGroup.getRootFolder());
        }
        final JButton okButton = new JButton(LBL_ChooseMainClass_OK());
        final MainClassChooser panel = new MainClassChooser(roots.toArray(new FileObject[0]));
        Object[] options = new Object[]{
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        panel.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
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
                LBL_ChooseMainClass_Title(),
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

    static void writeMapping(String actionName, Project project, String clazz) {
        try {
            M2ConfigProvider usr = project.getLookup().lookup(M2ConfigProvider.class);
            NetbeansActionMapping mapp = ModelHandle.getMapping(actionName, project, usr.getDefaultConfig());
            if (mapp == null) {
                mapp = ModelHandle.getDefaultMapping(actionName, project);
            }
            // XXX should this rather run on _all_ actions that reference ${packageClassName}?
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
            ModelHandle.putMapping(mapp, project, usr.getDefaultConfig());
        } catch (Exception e) {
            Exceptions.attachMessage(e, "Cannot persist action configuration.");
            Exceptions.printStackTrace(e);
        }
    }
}
