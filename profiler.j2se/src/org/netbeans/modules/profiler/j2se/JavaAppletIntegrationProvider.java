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

package org.netbeans.modules.profiler.j2se;

import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.netbeans.modules.profiler.attach.providers.AbstractIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.IntegrationCategorizer;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
public class JavaAppletIntegrationProvider extends AbstractIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.profiler.j2se.Bundle"); // NOI18N
    private static final String APPLET_WORKDIR_WINDOWS_MESSAGE = messages.getString("JavaAppletIntegrationProvider_AppletWorkDirWindowsMessage"); // NOI18N
    private static final String APPLET_WORKDIR_UNIXES_MESSAGE = messages.getString("JavaAppletIntegrationProvider_AppletWorkDirUnixesMessage"); // NOI18N
    private static final String OPERA_REMOTE_ATTACH_WARNING_MESSAGE = messages.getString("JavaAppletIntegrationProvider_OperaRemoteAttachWarningMessage"); // NOI18N
    private static final String MANUAL_REMOTE_STEP3_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualRemoteStep3Message"); // NOI18N
    private static final String MANUAL_REMOTE_STEP4_WINDOWS_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualRemoteStep4WindowsMessage"); // NOI18N
    private static final String MANUAL_REMOTE_STEP4_UNIXES_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualRemoteStep4UnixesMessage"); // NOI18N
    private static final String MANUAL_REMOTE_STEP5_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualRemoteStep5Message"); // NOI18N
    private static final String MANUAL_REMOTE_STOP_APPLET_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualRemoteStopAppletMessage"); // NOI18N
    private static final String MANUAL_REMOTE_RESTORE_SETTINGS_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualRemoteRestoreSettingsMessage"); // NOI18N
    private static final String OPERA_DIRECT_ATTACH_WARNING_MESSAGE = messages.getString("JavaAppletIntegrationProvider_OperaDirectAttachWarningMessage"); // NOI18N
    private static final String MANUAL_LOCAL_STEP1_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualLocalStep1Message"); // NOI18N
    private static final String MANUAL_DIRECT_STEP2_WINDOWS_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDirectStep2WindowsMessage"); // NOI18N
    private static final String MANUAL_DIRECT_STEP2_UNIXES_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDirectStep2UnixesMessage"); // NOI18N
    private static final String MANUAL_DIRECT_STEP2_MAC_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDirectStep2MacMessage"); // NOI18N
    private static final String MANUAL_DIRECT_STEP3_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDirectStep3Message"); // NOI18N
    private static final String MANUAL_DIRECT_STOP_APPLET_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDirectStopAppletMessage"); // NOI18N
    private static final String MANUAL_DIRECT_RESTORE_SETTINGS_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDirectRestoreSettingsMessage"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP2_WINDOWS_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDynamicStep2WindowsMessage"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP2_UNIXES_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDynamicStep2UnixesMessage"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP3_MESSAGE = messages.getString("JavaAppletIntegrationProvider_ManualDynamicStep3Message"); // NOI18N
    private static final String APPLET_TITLE = messages.getString("JavaAppletIntegrationProvider_Title");
    private static final String DYNAMIC_WARNING_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_DynamicWarningMessage"); // NOI18N  

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of JavaAppletIntegrationProvider
     */
    public JavaAppletIntegrationProvider() {
        this.attachedWizard = new NullWizardStep();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IntegrationProvider.IntegrationHints getAfterInstallationHints(AttachSettings attachSettings, boolean automation) {
        return null;
    }

    public IntegrationProvider.IntegrationHints getIntegrationReview(AttachSettings attachSettings) {
        return null;
    }

    public IntegrationProvider.IntegrationHints getModificationHints(AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = null;

        if (attachSettings.isRemote()) {
            hints = getManualRemoteIntegrationStepsInstructions(attachSettings.getHostOS(), attachSettings);
        } else {
            if (attachSettings.isDirect()) {
                hints = getManualLocalDirectIntegrationStepsInstructions(attachSettings.getHostOS(), attachSettings);
            } else {
                hints = getManualLocalDynamicIntegrationStepsInstructions(attachSettings.getHostOS(), attachSettings);
            }
        }

        return hints;
    }

    public String getTitle() {
        return APPLET_TITLE;
    }

    public void categorize(IntegrationCategorizer categorizer) {
        categorizer.addApplet(this, getAttachWizardPriority());
    }

    public void modify(AttachSettings attachSettings) {
    }

    // <editor-fold defaultstate="collapsed" desc="WizardIntegrationProvider implementation">
    public void run(AttachSettings attachSettings) {
    }

    public boolean supportsAutomation() {
        return false;
    }

    protected int getAttachWizardPriority() {
        return 1;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                  AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Windows & Opera & remote attach warning
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(OPERA_DIRECT_ATTACH_WARNING_MESSAGE);
        }

        // Step 1
        hints.addStep(MANUAL_LOCAL_STEP1_MESSAGE);

        // Step 2
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_WINDOWS_MESSAGE,
                                               new Object[] {
                                                   IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS,
                                                                                                                 getTargetJava(),
                                                                                                                 attachSettings
                                                                                                                                                                     .isRemote(),
                                                                                                                 attachSettings
                                                                                                                                                                       .getPort())
                                               }));
        } else if (IntegrationUtils.PLATFORM_MAC_OS.equals(targetOS)) {
            hints.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_MAC_MESSAGE,
                                               new Object[] {
                                                   IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS,
                                                                                                                 getTargetJava(),
                                                                                                                 attachSettings
                                                                                                                                                                               .isRemote(),
                                                                                                                 attachSettings
                                                                                                                                                                                 .getPort())
                                               }));
        } else {
            hints.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_UNIXES_MESSAGE,
                                               new Object[] {
                                                   IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS,
                                                                                                                 getTargetJava(),
                                                                                                                 attachSettings
                                                                                                                                                                               .isRemote(),
                                                                                                                 attachSettings
                                                                                                                                                                                 .getPort())
                                               }));
        }

        // Step 3
        hints.addStep(MANUAL_DIRECT_STEP3_MESSAGE);

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        // Note about stopping Applet JVM
        hints.addHint(MANUAL_DIRECT_STOP_APPLET_MESSAGE);

        // Note about stopping Applet JVM
        hints.addHint(MANUAL_DIRECT_RESTORE_SETTINGS_MESSAGE);

        return hints;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                   AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Step 1
        hints.addStep(MANUAL_LOCAL_STEP1_MESSAGE);

        // Step 2
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP2_WINDOWS_MESSAGE,
                                               new Object[] { IntegrationUtils.getJavaPlatformName(getTargetJava()) }));
        } else {
            hints.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP2_UNIXES_MESSAGE,
                                               new Object[] { IntegrationUtils.getJavaPlatformName(getTargetJava()) }));
        }

        // Step 3
        hints.addStep(MANUAL_DYNAMIC_STEP3_MESSAGE);

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        // Put here a warning that the IDE must be run under JDK6/7
        hints.addWarning(MessageFormat.format(DYNAMIC_WARNING_MESSAGE,
                                              new Object[] {
                                                  IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                  IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS, getTargetJava(),
                                                                                                   attachSettings.isRemote(),
                                                                                                   attachSettings.getPort())
                                              }));

        return hints;
    }

    // </editor-fold>
    private IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                             AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Windows & Opera & remote attach warning
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(OPERA_REMOTE_ATTACH_WARNING_MESSAGE);
        }

        // Step 1
        hints.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        hints.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        hints.addStep(MANUAL_REMOTE_STEP3_MESSAGE);

        // Step 4
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_WINDOWS_MESSAGE,
                                               new Object[] {
                                                   IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS,
                                                                                                                 getTargetJava(),
                                                                                                                 attachSettings
                                                                                                                                                                                                                                                                .isRemote(),
                                                                                                                 attachSettings
                                                                                                                                                                                                                                                                  .getPort())
                                               }));
        } else {
            hints.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_UNIXES_MESSAGE,
                                               new Object[] {
                                                   IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS,
                                                                                                                 getTargetJava(),
                                                                                                                 attachSettings
                                                                                                                                                                                                                                                                          .isRemote(),
                                                                                                                 attachSettings
                                                                                                                                                                                                                                                                            .getPort())
                                               }));
        }

        // Step 5
        hints.addStep(MANUAL_REMOTE_STEP5_MESSAGE);

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        // Note about stopping Applet JVM
        hints.addHint(MANUAL_REMOTE_STOP_APPLET_MESSAGE);

        // Note about stopping Applet JVM
        hints.addHint(MANUAL_REMOTE_RESTORE_SETTINGS_MESSAGE);

        return hints;
    }
}
