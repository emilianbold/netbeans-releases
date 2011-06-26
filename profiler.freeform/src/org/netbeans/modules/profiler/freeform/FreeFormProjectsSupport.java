/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.freeform;

import java.awt.Dialog;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jiri Sedlacek
 */
public final class FreeFormProjectsSupport {
    
    private static final String ERROR_PARSING_BUILDFILE_MSG = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_ErrorParsingBuildFileMsg"); // NOI18N
    private static final String OK_BUTTON_NAME = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_OkButtonName"); // NOI18N
    private static final String SELECT_PROFILING_TASK_DIALOG_CAPTION = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_SelectProfilingTaskDialogCaption"); // NOI18N
    private static final String NO_PROFILER_TASK_MSG = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_NoProfilerTaskMsg"); // NOI18N
    
    public static final String PROFILE_TARGET_ATTRIBUTE = "profile-target"; // NOI18N
    public static final String PROFILE_SINGLE_TARGET_ATTRIBUTE = "profile-file-target"; // NOI18N
    public static final String PROFILE_VERSION_ATTRIBUTE = "version"; // NOI18N
    public static final String VERSION_NUMBER = "0.4"; // NOI18N
    
    
    public static boolean saveProfilerConfig(final Project project, final String profileTarget, final String profileSingleTarget) {
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
    public static String selectProfilingTarget(final Project project, final FileObject buildScript, final int type,
            final String currentTarget) {
        final List targets = Util.getAntScriptTargets(buildScript);
        final List l;
        try {
            l = AntScriptUtils.getCallableTargetNames(buildScript);
        } catch (IOException x) {
            ProfilerDialogs.displayError(MessageFormat.format(ERROR_PARSING_BUILDFILE_MSG,
                    new Object[]{ ProjectUtils.getInformation(project).getName() }));

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
            final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
            d.setVisible(true);

            if (dd.getValue() == okButton) {
                final String targetName = atsp.getTargetName();

                for (Iterator it = targets.iterator(); it.hasNext();) {
                    final TargetLister.Target t = (TargetLister.Target) it.next();

                    if (t.getName().equals(targetName)) {
                        if (checkTarget(t.getElement())) {
                            return targetName;
                        } else if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(NO_PROFILER_TASK_MSG,
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
    
    private static boolean checkTarget(final Element element) {
        final NodeList nl = element.getElementsByTagName("nbprofiledirect"); // NOI18N

        return (nl.getLength() > 0);
    }
    
}
