/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.profiler.freeform;

import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.modules.profiler.ui.NBHTMLLabel;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.*;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.util.HelpCtx;

/**
 * A class providing basic support for profiling free-form projects.
 *
 * @author Ian Formanek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.ProjectTypeProfiler.class)
public final class FreeFormProjectTypeProfiler extends AbstractProjectTypeProfiler {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------
    private static final class AntTaskSelectPanel extends JPanel implements HelpCtx.Provider {

        private static final String HELP_CTX_KEY = "FreeFormProjectTypeProfiler.AntTaskSelectPanel.HelpCtx"; // NOI18N
        private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
        //~ Instance fields ------------------------------------------------------------------------------------------------------
        final JComboBox targetBox;
        final JLabel label;
        final NBHTMLLabel descriptionLabel;

        //~ Constructors ---------------------------------------------------------------------------------------------------------
        AntTaskSelectPanel(final List /*<String>*/ list, final int type, final JButton okButton) {
            list.add(0, SELECT_TARGET_ITEM_STRING);

            if (type == TARGET_PROFILE) {
                label = new JLabel(SELECT_PROJECT_TASK_LABEL_STRING);
            } else {
                label = new JLabel(SELECT_FILE_TASK_LABEL_STRING);
            }

            descriptionLabel = new NBHTMLLabel(CREATE_NEW_TARGET_MSG);
            targetBox = new JComboBox(list.toArray(new Object[list.size()]));
            targetBox.setSelectedIndex(0);
            targetBox.addItemListener(new ItemListener() {

                public void itemStateChanged(final ItemEvent e) {
                    okButton.setEnabled(targetBox.getSelectedIndex() != 0);
                }
            });

            setLayout(new GridBagLayout());

            label.setLabelFor(targetBox);

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(12, 12, 12, 12);
            add(label, gridBagConstraints);

            targetBox.getAccessibleContext().setAccessibleName(TARGET_BOX_ACCESS_NAME);
            targetBox.getAccessibleContext().setAccessibleDescription(TARGET_BOX_ACCESS_DESCR);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.insets = new Insets(12, 0, 12, 12);
            add(targetBox, gridBagConstraints);

            descriptionLabel.setFocusable(false);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(12, 12, 12, 12);
            add(descriptionLabel, gridBagConstraints);

            okButton.setEnabled(false);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        public String getTargetName() {
            if (targetBox.getSelectedIndex() == 0) {
                return null; //nothing selected
            }

            return (String) targetBox.getSelectedItem();
        }

        public HelpCtx getHelpCtx() {
            return HELP_CTX;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String ERROR_PARSING_BUILDFILE_MSG = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_ErrorParsingBuildFileMsg"); // NOI18N
    private static final String OK_BUTTON_NAME = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_OkButtonName"); // NOI18N
    private static final String SELECT_PROFILING_TASK_DIALOG_CAPTION = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_SelectProfilingTaskDialogCaption"); // NOI18N
    private static final String NO_PROFILER_TASK_MSG = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_NoProfilerTaskMsg"); // NOI18N
    private static final String SELECT_TARGET_ITEM_STRING = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_SelectTargetItemString"); // NOI18N
    private static final String SELECT_PROJECT_TASK_LABEL_STRING = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_SelectProjectTaskLabelString"); // NOI18N
    private static final String SELECT_FILE_TASK_LABEL_STRING = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_SelectFileTaskLabelString"); // NOI18N
    private static final String CREATE_NEW_TARGET_MSG = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_CreateNewTargetMsg"); // NOI18N
    private static final String TARGET_BOX_ACCESS_NAME = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_TargetBoxAccessName"); // NOI18N
    private static final String TARGET_BOX_ACCESS_DESCR = NbBundle.getMessage(FreeFormProjectTypeProfiler.class,
            "FreeFormProjectTypeProfiler_TargetBoxAccessDescr"); // NOI18N
    // -----
    private static final String FREEFORM_PROJECT_NAMESPACE_40 = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    private static final String FREEFORM_PROJECT_NAMESPACE_41 = "http://www.netbeans.org/ns/freeform-project/2"; // NOI18N
    private static final String PROFILE_TARGET_ATTRIBUTE = "profile-target"; // NOI18N
    private static final String PROFILE_SINGLE_TARGET_ATTRIBUTE = "profile-file-target"; // NOI18N
    private static final String PROFILE_VERSION_ATTRIBUTE = "version"; // NOI18N
    private static final String VERSION_NUMBER = "0.4"; // NOI18N

    // --- ProjectTypeProfiler implementation ------------------------------------------------------------------------------
    public String getProfilerTargetName(final Project project, final FileObject buildScript, final int type,
            final FileObject profiledClass) {
        final Element e = ProjectUtils.getAuxiliaryConfiguration(project).getConfigurationFragment("data",
                ProjectUtilities.PROFILER_NAME_SPACE,
                false); // NOI18N
        String profileTarget = e.getAttribute(PROFILE_TARGET_ATTRIBUTE);
        String profileSingleTarget = e.getAttribute(PROFILE_SINGLE_TARGET_ATTRIBUTE);

        switch (type) {
            case TARGET_PROFILE:
                profileTarget = selectProfilingTarget(project, buildScript, TARGET_PROFILE, profileTarget);

                if (profileTarget == null) {
                    return null; // cancelled by the user
                }

                saveProfilerConfig(project, profileTarget, profileSingleTarget);

                return profileTarget;
            case TARGET_PROFILE_SINGLE:
                profileSingleTarget = selectProfilingTarget(project, buildScript, TARGET_PROFILE_SINGLE, profileSingleTarget);

                if (profileSingleTarget == null) {
                    return null; // cancelled by the user
                }

                saveProfilerConfig(project, profileTarget, profileSingleTarget);

                return profileSingleTarget;
            default:
                return null;
        }
    }

    public boolean isProfilingSupported(final Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);

        Element e = aux.getConfigurationFragment("general-data", FREEFORM_PROJECT_NAMESPACE_40, true); // NOI18N

        if (e == null) {
            e = aux.getConfigurationFragment("general-data", FREEFORM_PROJECT_NAMESPACE_41, true); // NOI18N
        }

        return (e != null);
    }

    public FileObject getProjectBuildScript(Project project) {
        return Util.getProjectBuildScript(project);
    }

    public boolean checkProjectCanBeProfiled(final Project project, final FileObject profiledClassFile) {
        return true; // no check performed in freeform project
    }

    public boolean checkProjectIsModifiedForProfiler(final Project project) {
        Element e = ProjectUtils.getAuxiliaryConfiguration(project).getConfigurationFragment("data",
                ProjectUtilities.PROFILER_NAME_SPACE,
                false); // NOI18N

        if (e != null) {
            final String profileTarget = e.getAttribute(PROFILE_TARGET_ATTRIBUTE);
            final String profileSingleTarget = e.getAttribute(PROFILE_SINGLE_TARGET_ATTRIBUTE);

            if (((profileTarget != null) || (profileSingleTarget != null))) {
                return true; // already setup for profiling, nothing more to be done
            }
        } else {
            saveProfilerConfig(project, null, null);
        }

        return true;
    }

    public void configurePropertiesForProfiling(final Properties props, final Project project, final FileObject profiledClassFile) {
        if (profiledClassFile != null) { // In case the class to profile is explicitely selected (profile-single)
            // 1. specify profiled class name

            final String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
            props.setProperty("profile.class", profiledClass); //NOI18N

            // 2. include it in javac.includes so that the compile-single picks it up
            final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(ProjectUtilities.getSourceRoots(project),
                    profiledClassFile), profiledClassFile);
            props.setProperty("javac.includes", clazz); //NOI18N
        }
    }

    private boolean checkTarget(final Element element) {
        final NodeList nl = element.getElementsByTagName("nbprofiledirect"); // NOI18N

        return (nl.getLength() > 0);
    }

    // --- Private methods -------------------------------------------------------------------------------------------------
    private boolean saveProfilerConfig(final Project project, final String profileTarget, final String profileSingleTarget) {
        // not yet modified for profiler => create profiler-build-impl & modify build.xml and project.xml
        final Element profilerFragment = XMLUtil.createDocument("ignore", null, null, null) // NOI18N
                .createElementNS(ProjectUtilities.PROFILER_NAME_SPACE, "data"); // NOI18N

        profilerFragment.setAttribute(PROFILE_VERSION_ATTRIBUTE, VERSION_NUMBER);

        if (profileTarget != null) {
            profilerFragment.setAttribute(PROFILE_TARGET_ATTRIBUTE, profileTarget);
        }

        if (profileSingleTarget != null) {
            profilerFragment.setAttribute(PROFILE_SINGLE_TARGET_ATTRIBUTE, profileSingleTarget);
        }

        ProjectUtils.getAuxiliaryConfiguration(project).putConfigurationFragment(profilerFragment,
                false);

        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e1) {
            Profiler.getDefault().notifyException(Profiler.EXCEPTION, e1);
            ProfilerLogger.log(e1);

            return false;
        }

        return true;
    }

    /**
     * Prompts the user to select one of existing targets for profiling.
     *
     * @param project
     * @param buildScript
     * @param type          Type of profiling target to select (single or project)
     * @param currentTarget
     * @return String name of the selected target or null if cancelled by the user
     */
    private String selectProfilingTarget(final Project project, final FileObject buildScript, final int type,
            final String currentTarget) {
        final List targets = Util.getAntScriptTargets(buildScript);
        final List l;
        try {
            l = AntScriptUtils.getCallableTargetNames(buildScript);
        } catch (IOException x) {
            Profiler.getDefault().displayError(MessageFormat.format(ERROR_PARSING_BUILDFILE_MSG,
                    new Object[]{ProjectUtils.getInformation(project).getName()                    }));

            return null;
        }

        // if currently selected target exists, just return it
        if (!"".equals(currentTarget) && l.contains(currentTarget)) { //NOI18N

            return currentTarget;
        }

        final JButton okButton = new JButton(OK_BUTTON_NAME);
        final AntTaskSelectPanel atsp = new AntTaskSelectPanel(l, type, okButton);

        while (true) {
            final DialogDescriptor dd = new DialogDescriptor(atsp, SELECT_PROFILING_TASK_DIALOG_CAPTION, true,
                    new Object[]{okButton, DialogDescriptor.CANCEL_OPTION                    }, okButton,
                    DialogDescriptor.BOTTOM_ALIGN, null, null);
            final Dialog d = ProfilerDialogs.createDialog(dd);
            d.setVisible(true);

            if (dd.getValue() == okButton) {
                final String targetName = atsp.getTargetName();

                for (Iterator it = targets.iterator(); it.hasNext();) {
                    final TargetLister.Target t = (TargetLister.Target) it.next();

                    if (t.getName().equals(targetName)) {
                        if (checkTarget(t.getElement())) {
                            return targetName;
                        } else if (ProfilerDialogs.notify(new NotifyDescriptor.Confirmation(NO_PROFILER_TASK_MSG,
                                NotifyDescriptor.OK_CANCEL_OPTION,
                                NotifyDescriptor.WARNING_MESSAGE)) == NotifyDescriptor.OK_OPTION) {
                            return targetName;
                        }
                    }
                }
            } else {
                return null; // cancelled by the user
            }
        }
    }
}
