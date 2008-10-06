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
import org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
public class JavaApplicationIntegrationProvider extends AbstractIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.profiler.j2se.Bundle"); // NOI18N
    private static final String MANUAL_REMOTE_STEP3_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualRemoteStep3Message"); // NOI18N
    private static final String MANUAL_REMOTE_STEP4_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualRemoteStep4Message"); // NOI18N
    private static final String MANUAL_REMOTE_HINT_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualRemoteHintMessage"); // NOI18N
    private static final String MANUAL_DIRECT_STEP1_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualDirectStep1Message"); // NOI18N
    private static final String MANUAL_DIRECT_STEP2_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualDirectStep2Message"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP1_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualDynamicStep1Message"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP2_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_ManualDynamicStep2Message"); // NOI18N  
    private static final String DYNAMIC_WARNING_MESSAGE = messages.getString("JavaApplicationIntegrationProvider_DynamicWarningMessage"); // NOI18N  
    private static final String APPLICATION_TITLE = messages.getString("JavaApplicationIntegrationProvider_Title");

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // -----
    public JavaApplicationIntegrationProvider() {
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
        return APPLICATION_TITLE;
    }

    public void categorize(IntegrationCategorizer categorizer) {
        categorizer.addApplication(this, getAttachWizardPriority());
    }

    public void modify(AttachSettings attachSettings) {
    }

    public boolean supportsJVM(TargetPlatformEnum jvm, AttachSettings attachSettings) {
        if (attachSettings.isRemote() && jvm.equals(TargetPlatformEnum.JDK_CVM) && 
           (attachSettings.getHostOS() == IntegrationUtils.PLATFORM_LINUX_OS ||  attachSettings.getHostOS() == IntegrationUtils.PLATFORM_WINDOWS_OS)) {
            return true;
        }
        return super.supportsJVM(jvm,attachSettings);
    }

    // <editor-fold defaultstate="collapsed" desc="WizardIntegrationProvider implementation">
    public void run(AttachSettings attachSettings) {
    }

    public boolean supportsAutomation() {
        return false;
    }

    protected int getAttachWizardPriority() {
        return 10;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                  AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Step 1
        hints.addStep(MessageFormat.format(MANUAL_DIRECT_STEP1_MESSAGE,
                                           new Object[] {
                                               IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                               IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS, getTargetJava(),
                                                                                                attachSettings.isRemote(),
                                                                                                attachSettings.getPort())
                                           }));

        // Step 2
        hints.addStep(MANUAL_DIRECT_STEP2_MESSAGE);

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        return hints;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                   AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Step 1
        hints.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP1_MESSAGE,
                                           new Object[] { IntegrationUtils.getJavaPlatformName(getTargetJava()) }));
        // Step 2
        hints.addStep(MANUAL_DYNAMIC_STEP2_MESSAGE);

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

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
        //        StringBuffer text = new StringBuffer();

        // Step 1
        hints.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        hints.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        hints.addStep(MessageFormat.format(MANUAL_REMOTE_STEP3_MESSAGE,
                                           new Object[] {
                                               IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                               IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS, getTargetJava(),
                                                                                                attachSettings.isRemote(),
                                                                                                attachSettings.getPort()),
                                               REMOTE_ABSOLUTE_PATH_HINT
                                           }));

        // Step 4
        hints.addStep(MANUAL_REMOTE_STEP4_MESSAGE);

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about export vs. setenv on UNIXes
        hints.addHint(MessageFormat.format(MANUAL_REMOTE_HINT_MESSAGE,
                                           new Object[] { IntegrationUtils.getRemoteProfileCommandString(targetOS) }));

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        return hints;
    }
}
