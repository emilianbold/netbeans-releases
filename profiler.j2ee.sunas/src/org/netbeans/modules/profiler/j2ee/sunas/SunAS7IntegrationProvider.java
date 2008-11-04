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

package org.netbeans.modules.profiler.j2ee.sunas;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.AbstractIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.IntegrationCategorizer;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;
import org.netbeans.modules.profiler.attach.wizard.steps.WizardStep;

/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class SunAS7IntegrationProvider extends AbstractIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="Resources">
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.profiler.j2ee.sunas.Bundle"); // NOI18N
    private static final String SUNAS_7_STRING = messages.getString("SunAS7IntegrationProvider_SunAs7String"); // NOI18N
    private static final String WORKING_DIR_HINT_MSG = messages.getString("SunAS7IntegrationProvider_WorkingDirHintMsg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP3_MSG = messages.getString("SunAS7IntegrationProvider_ManualRemoteStep3Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP4_MSG = messages.getString("SunAS7IntegrationProvider_ManualRemoteStep4Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP5_MSG = messages.getString("SunAS7IntegrationProvider_ManualRemoteStep5Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP6_MSG = messages.getString("SunAS7IntegrationProvider_ManualRemoteStep6Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP1_MSG = messages.getString("SunAS7IntegrationProvider_ManualDirectStep1Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP2_MSG = messages.getString("SunAS7IntegrationProvider_ManualDirectStep2Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP3_MSG = messages.getString("SunAS7IntegrationProvider_ManualDirectStep3Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP4_MSG = messages.getString("SunAS7IntegrationProvider_ManualDirectStep4Msg"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP1_MSG = messages.getString("SunAS7IntegrationProvider_ManualDynamicStep1Msg"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP2_MSG = messages.getString("SunAS7IntegrationProvider_ManualDynamicStep2Msg"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP3_MSG = messages.getString("SunAS7IntegrationProvider_ManualDynamicStep3Msg"); // NOI18N
    private static final String DOUBLE_QUOTES_WARNING_MSG = messages.getString("SunAS7IntegrationProvider_DoubleQuotesWarningMsg"); // NOI18N
    private static final String DYNAMIC_WARNING_MESSAGE = messages.getString("SunASIntegrationProvider_DynamicWarningMessage"); // NOI18N  
                                                                                                                                //</editor-fold>
    private static final WizardStep NULL_WIZARD_STEP = new NullWizardStep();

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IntegrationProvider.IntegrationHints getAfterInstallationHints(AttachSettings attachSettings, boolean b) {
        return new IntegrationProvider.IntegrationHints();
    }

    public WizardStep getAttachedWizard() {
        return NULL_WIZARD_STEP;
    }

    public String getDynamicWorkingDirectoryHint(String targetOS, AttachSettings attachSettings) {
        return MessageFormat.format(WORKING_DIR_HINT_MSG,
                                    new Object[] {
                                        IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                        IntegrationUtils.getDirectorySeparator(targetOS)
                                    }); // NOI18N
    }

    // <editor-fold defaultstate="collapsed" desc="WizardIntegrationProvider implementation">
    public IntegrationProvider.IntegrationHints getIntegrationReview(AttachSettings attachSettings) {
        return null;
    }

    public IntegrationProvider.IntegrationHints getModificationHints(AttachSettings attachSettings) {
        String targetOS = attachSettings.getHostOS();

        // Remote attach instructions
        if (attachSettings.isRemote()) {
            return getManualRemoteIntegrationStepsInstructions(targetOS, attachSettings);
        }
        // Local direct attach
        else if (attachSettings.isDirect()) {
            return getManualLocalDirectIntegrationStepsInstructions(targetOS, attachSettings);
        }
        // Local dynamic attach
        else {
            return getManualLocalDynamicIntegrationStepsInstructions(targetOS, attachSettings);
        }
    }

    public String getTitle() {
        return SUNAS_7_STRING;
    }

    public void categorize(IntegrationCategorizer integrationCategorizer) {
        integrationCategorizer.addAppserver(this, getAttachWizardPriority());
    }

    public void modify(AttachSettings attachSettings) {
    }

    public void run(AttachSettings attachSettings) {
    }

    public boolean supportsAutomation() {
        return false;
    }

    protected int getAttachWizardPriority() {
        return 22;
    }

    private final String getDoubleQuotesWarning(String targetOS, AttachSettings attachSettings) {
        return MessageFormat.format(DOUBLE_QUOTES_WARNING_MSG,
                                    new Object[] {
                                        getProfilerAgentCommandLineArgsForDomainScript(targetOS, attachSettings.isRemote(),
                                                                                       attachSettings.getPort())
                                    });
    }

    private IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                  AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP1_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Step 2
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_MSG,
                                                  new Object[] { IntegrationUtils.getJavaPlatformName(getTargetJava()) }));

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP3_MSG,
                                                  new Object[] {
                                                      getProfilerAgentCommandLineArgsForDomainScript(targetOS,
                                                                                                     attachSettings.isRemote(),
                                                                                                     attachSettings.getPort())
                                                  }));

        // Step 4
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP4_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Warning about double-quotes in 'jvm-options'
        instructions.addWarning(getDoubleQuotesWarning(targetOS, attachSettings));

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        instructions.setWarningsFirst(false);

        return instructions;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                   AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP1_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        instructions.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP2_MSG,
                                                  new Object[] { IntegrationUtils.getJavaPlatformName(getTargetJava()) }));
        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP3_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        instructions.addWarning(MessageFormat.format(DYNAMIC_WARNING_MESSAGE,
                                                     new Object[] {
                                                         IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                         IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS,
                                                                                                          getTargetJava(),
                                                                                                          attachSettings.isRemote(),
                                                                                                          attachSettings.getPort())
                                                     }));

        return instructions;
    }

    // </editor-fold>
    private IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                             AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        instructions.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP3_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS),
                                                      IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS)
                                                  })); // NOI18N

        // Step 4
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_MSG,
                                                  new Object[] { IntegrationUtils.getJavaPlatformName(getTargetJava()) }));

        // Step 5
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP5_MSG,
                                                  new Object[] {
                                                      getProfilerAgentCommandLineArgsForDomainScript(targetOS,
                                                                                                     attachSettings.isRemote(),
                                                                                                     attachSettings.getPort()),
                                                      IntegrationUtils.getRemoteAbsolutePathHint()
                                                  }));

        // Step 6
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP6_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Warning about double-quotes in 'jvm-options'
        instructions.addWarning(getDoubleQuotesWarning(targetOS, attachSettings));

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        instructions.setWarningsFirst(false);

        return instructions;
    }

    private final String getProfilerAgentCommandLineArgsForDomainScript(String targetOS, boolean isRemote, int portNumber) {
        if (!IntegrationUtils.isWindowsPlatform(targetOS)
                || (IntegrationUtils.getNativeLibrariesPath(targetOS, getTargetJava(), isRemote).indexOf(' ') == -1)) {
            return IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS, getTargetJava(), isRemote, portNumber);
        }

        return "-agentpath:" + IntegrationUtils.getNativeLibrariesPath(targetOS, getTargetJava(), isRemote)
               + IntegrationUtils.getDirectorySeparator(targetOS) + IntegrationUtils.getProfilerAgentLibraryFile(targetOS) + "=" //NOI18N
               + "\"" + IntegrationUtils.getLibsDir(targetOS, isRemote) + "\"" + "," + portNumber; //NOI18N
    }
}
