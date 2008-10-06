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

package org.netbeans.modules.profiler.j2ee.weblogic;

import java.text.MessageFormat;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
public class WebLogic9IntegrationProvider extends WebLogicIntegrationProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final String COPY_FILES_9_MSG = messages.getString("WebLogicIntegrationProvider_CopyFiles9Msg"); // NOI18N
    private final String MANUAL_DIRECT_DYNAMIC_STEP3_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectDynamicStep3Wl9Msg"); // NOI18N
    private final String MANUAL_DIRECT_DYNAMIC_STEP4_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectDynamicStep4Wl9Msg"); // NOI18N
    private final String MANUAL_DIRECT_STEP5_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectStep5Wl9Msg"); // NOI18N
    private final String MANUAL_DYNAMIC_STEP5_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualDynamicStep5Wl9Msg"); // NOI18N
    private final String MANUAL_REMOTE_STEP5_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep5Wl9Msg"); // NOI18N
    private final String MANUAL_REMOTE_STEP6_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep6Wl9Msg"); // NOI18N
    private final String MANUAL_REMOTE_STEP7_WL_9_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep7Wl9Msg"); // NOI18N
    private final String WEBLOGIC_9_STRING = messages.getString("WebLogicIntegrationProvider_WebLogic9String"); // NOI18N
    private final String WL_9_ANCHOR_TEXT = messages.getString("WebLogicIntegrationProvider_Wl9AnchorText"); // NOI18N
    private final String WL_9_CLEANUP_TEXT = messages.getString("WebLogicIntegrationProvider_Wl9CleanupText"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getTitle() {
        return WEBLOGIC_9_STRING;
    }

    public boolean supportsDynamic() {
        return true;
    }

    protected int getAttachWizardPriority() {
        return 40;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                    AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
        IntegrationUtils.getDirectorySeparator(targetOS), "bin", // NOI18N
        "base_domain", // NOI18N
        MessageFormat.format(COPY_FILES_9_MSG, new Object[] { getStartScriptExtension(targetOS) })
                                                  }));

        // Step 2
        String wlSettings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                               MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                                                    new Object[] {
                                                                                                        IntegrationUtils
                                                                                                           .getJavaPlatformName(getTargetJava())
                                                                                                    })); // NOI18N

        final String cleanupText = MessageFormat.format(WL_9_CLEANUP_TEXT,
                                                        new Object[] { IntegrationUtils.getBatchExtensionString(targetOS) });
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS) ? WINDOWS_ANCHOR_TEXT
                                                                                                   : WL_9_ANCHOR_TEXT, wlSettings,
                                                      IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_OPTIONS",
                                                                                                       IntegrationUtils
                                                                                                                       .getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                                                        getTargetJava(),
                                                                                                                                                        attachSettings
                                                                                                                                                        .isRemote(),
                                                                                                                                                        attachSettings
                                                                                                                                                        .getPort())
                                                                                                       + " "
                                                                                                       + IntegrationUtils
                                                                                                                               .getEnvVariableReference("JAVA_OPTIONS",
                                                                                                                                                        targetOS)),
                                                      cleanupText
                                                  })); // NOI18N

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP3_WL_9_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                           new Object[] {
                                                                               IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                           })
                                                  }));

        // Step 4
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP4_WL_9_MSG,
                                                  new Object[] { getStartScriptExtension(targetOS) }));

        // Step 5
        instructions.addStep(MANUAL_DIRECT_STEP5_WL_9_MSG);

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        return instructions;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                     AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
        IntegrationUtils.getDirectorySeparator(targetOS), "bin", // NOI18N
        "base_domain", // NOI18N
        MessageFormat.format(COPY_FILES_9_MSG, new Object[] { getStartScriptExtension(targetOS) })
                                                  }));

        // Step 2
        String wlSettings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                               MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                                                    new Object[] {
                                                                                                        IntegrationUtils
                                                                                                                                                                               .getJavaPlatformName(getTargetJava())
                                                                                                    })); // NOI18N

        final String cleanupText = MessageFormat.format(WL_9_CLEANUP_TEXT,
                                                        new Object[] { IntegrationUtils.getBatchExtensionString(targetOS) });
        instructions.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP2_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS) ? WINDOWS_ANCHOR_TEXT
                                                                                                   : WL_9_ANCHOR_TEXT, wlSettings,
                                                      cleanupText
                                                  }));

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP3_WL_9_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                           new Object[] {
                                                                               IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                           })
                                                  }));

        // Step 4
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP4_WL_9_MSG,
                                                  new Object[] { getStartScriptExtension(targetOS) }));

        // Step 5
        instructions.addStep(MANUAL_DYNAMIC_STEP5_WL_9_MSG); // NOI18N

        // Put here a warning that the IDE must be run under JDK6/7
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

    protected IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                               AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        instructions.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP3_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
        IntegrationUtils.getDirectorySeparator(targetOS), "bin", // NOI18N
        "base_domain", // NOI18N
        MessageFormat.format(COPY_FILES_9_MSG, new Object[] { getStartScriptExtension(targetOS) })
                                                  }));

        // Step 4
        String wlSettings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                               MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                                                    new Object[] {
                                                                                                        IntegrationUtils
                                                                                                                                                                                                                                                    .getJavaPlatformName(getTargetJava())
                                                                                                    })); // NOI18N

        final String cleanupText = MessageFormat.format(WL_9_CLEANUP_TEXT,
                                                        new Object[] { IntegrationUtils.getBatchExtensionString(targetOS) });
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS) ? WINDOWS_ANCHOR_TEXT
                                                                                                   : WL_9_ANCHOR_TEXT, wlSettings,
                                                      IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_OPTIONS",
                                                                                                       IntegrationUtils
                                                                                                                                                                                                                                                                .getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                                                                                                                                                                                                 getTargetJava(),
                                                                                                                                                                                                                                                                                                 attachSettings
                                                                                                                                                                                                                                                                                                 .isRemote(),
                                                                                                                                                                                                                                                                                                 attachSettings
                                                                                                                                                                                                                                                                                                 .getPort())
                                                                                                       + " "
                                                                                                       + IntegrationUtils
                                                                                                                                                                                                                                                                        .getEnvVariableReference("JAVA_OPTIONS",
                                                                                                                                                                                                                                                                                                 targetOS)),
                                                      IntegrationUtils.getRemoteAbsolutePathHint(), cleanupText
                                                  })); // NOI18N

        // Step 5
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP5_WL_9_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                           new Object[] {
                                                                               IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                           })
                                                  }));

        // Step 6
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP6_WL_9_MSG, new Object[] { getStartScriptExtension(targetOS) }));

        // Step 7
        instructions.addStep(MANUAL_REMOTE_STEP7_WL_9_MSG);

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        return instructions;
    }

    private String getStartScriptExtension(String targetOS) {
        return (IntegrationUtils.isWindowsPlatform(targetOS) ? "cmd" : "sh"); // NOI18N
    }
}
