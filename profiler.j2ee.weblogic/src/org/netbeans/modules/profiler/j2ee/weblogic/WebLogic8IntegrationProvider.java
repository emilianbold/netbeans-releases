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
//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class WebLogic8IntegrationProvider extends WebLogicIntegrationProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final String COPY_FILE_81_MSG = messages.getString("WebLogicIntegrationProvider_CopyFile81Msg"); // NOI18N
    private final String MANUAL_DIRECT_DYNAMIC_STEP3_WL_81_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectDynamicStep3Wl81Msg"); // NOI18N
    private final String MANUAL_DIRECT_STEP4_WL_81_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectStep4Wl81Msg"); // NOI18N
    private final String MANUAL_DYNAMIC_STEP4_WL_81_MSG = messages.getString("WebLogicIntegrationProvider_ManualDynamicStep4Wl81Msg"); // NOI18N
    private final String MANUAL_REMOTE_STEP5_WL_81_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep5Wl81Msg"); // NOI18N
    private final String MANUAL_REMOTE_STEP6_WL_81_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep6Wl81Msg"); // NOI18N
    private final String WEBLOGIC_81_STRING = messages.getString("WebLogicIntegrationProvider_WebLogic81String"); // NOI18N
    private final String WL_81_ANCHOR_TEXT = messages.getString("WebLogicIntegrationProvider_Wl81AnchorText"); // NOI18N
    private final String WL_81_CLEANUP_TEXT = messages.getString("WebLogicIntegrationProvider_Wl81CleanupText"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getTitle() {
        return WEBLOGIC_81_STRING;
    }

    protected int getAttachWizardPriority() {
        return 41;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                    AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("BEA_HOME", targetOS), // NOI18N
        IntegrationUtils.getDirectorySeparator(targetOS), "", // NOI18N
        "mydomain", // NOI18N
        MessageFormat.format(COPY_FILE_81_MSG, new Object[] { getStartScriptExtension(targetOS) })
                                                  }));

        // Step 2
        String wl81Settings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                              + "<br>" // NOI18N
                                       //      IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", IntegrationUtils.getVMDir(targetOS, attachSettings.isRemote())) + // NOI18N
                                       //      "<br>" + // NOI18N
                              + IntegrationUtils.getAddProfilerLibrariesToPathString(targetOS, getTargetJava(),
                                                                                     attachSettings.isRemote(), true);

        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS) ? WINDOWS_ANCHOR_TEXT
                                                                                                   : WL_81_ANCHOR_TEXT,
                                                      wl81Settings,
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
                                                      WL_81_CLEANUP_TEXT
                                                  })); // NOI18N

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP3_WL_81_MSG,
                                                  new Object[] { getStartScriptExtension(targetOS) }));

        // Step 4
        instructions.addStep(MANUAL_DIRECT_STEP4_WL_81_MSG);

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
        IntegrationUtils.getDirectorySeparator(targetOS), "", // NOI18N
        "mydomain", // NOI18N
        MessageFormat.format(COPY_FILE_81_MSG, new Object[] { getStartScriptExtension(targetOS) })
                                                  }));

        // Step 2
        String wl81Settings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                              + "<br>" // NOI18N
                              + ""; // IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", IntegrationUtils.getVMDir(targetOS, attachSettings.isRemote())); // NOI18N

        instructions.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP2_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS) ? WINDOWS_ANCHOR_TEXT
                                                                                                   : WL_81_ANCHOR_TEXT,
                                                      wl81Settings, WL_81_CLEANUP_TEXT
                                                  }));

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP3_WL_81_MSG,
                                                  new Object[] { getStartScriptExtension(targetOS) }));

        // Step 4
        instructions.addStep(MANUAL_DYNAMIC_STEP4_WL_81_MSG); // NOI18N

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
        IntegrationUtils.getDirectorySeparator(targetOS), "", // NOI18N
        "mydomain", // NOI18N
        MessageFormat.format(COPY_FILE_81_MSG, new Object[] { getStartScriptExtension(targetOS) })
                                                  }));

        // Step 4
        String wl81Settings = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_VENDOR", "Sun") // NOI18N
                              + "<br>" // NOI18N
                                       //      IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", IntegrationUtils.getVMDir(targetOS, attachSettings.isRemote())) + // NOI18N
                                       //      "<br>" + // NOI18N
                              + IntegrationUtils.getAddProfilerLibrariesToPathString(targetOS, getTargetJava(),
                                                                                     attachSettings.isRemote(), true);

        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_MSG,
                                                  new Object[] {
                                                      getStartScriptExtension(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS) ? WINDOWS_ANCHOR_TEXT
                                                                                                   : WL_81_ANCHOR_TEXT,
                                                      wl81Settings,
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
                                                      IntegrationUtils.getRemoteAbsolutePathHint(), WL_81_CLEANUP_TEXT
                                                  })); // NOI18N

        // Step 5
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP5_WL_81_MSG,
                                                  new Object[] { getStartScriptExtension(targetOS) }));

        // Step 6
        instructions.addStep(MANUAL_REMOTE_STEP6_WL_81_MSG);

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        return instructions;
    }

    private String getStartScriptExtension(String targetOS) {
        return (IntegrationUtils.isWindowsPlatform(targetOS) ? "cmd" : "sh"); // NOI18N
    }
}
