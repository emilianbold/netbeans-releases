/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.profiler.j2ee.generic;

import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.AbstractIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.IntegrationCategorizer;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;
import org.netbeans.modules.profiler.attach.wizard.steps.WizardStep;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "GenericWebAppServerIntegrationProvider_GenericWebappServerString=Generic Web/App Server",
    "GenericWebAppServerIntegrationProvider_PathToJdkDirectory=path to {0} directory",
    "GenericWebAppServerIntegrationProvider_ManualRemoteStep3Message=If the server is configured to run on a different JVM, set the server to run on {0}. This typically means changing the system/environment variable <code>JAVA_HOME</code> as follows:<br><code>{1}</code>",
    "GenericWebAppServerIntegrationProvider_ManualRemoteStep4Message=When starting the server, provide extra startup option to the <code>java</code> command:<br><code>{0}</code><br>The {1}.",
    "GenericWebAppServerIntegrationProvider_ManualRemoteStep5Message=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "GenericWebAppServerIntegrationProvider_ManualDirectStep1Message=If the server is configured to run on a different JVM, set the server to run on {0}. This typically means the changing system/environment variable <code>JAVA_HOME</code> as follows:<br><code>{1}</code>",
    "GenericWebAppServerIntegrationProvider_ManualDirectStep2Message=When starting the server, provide extra startup option to the <code>java</code> command:<br><code>{0}</code>",
    "GenericWebAppServerIntegrationProvider_ManualDirectStep3Message=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "GenericWebAppServerIntegrationProvider_DynamicWarningMessage=Make sure your IDE is using {0}.",
    "GenericWebAppServerIntegrationProvider_ManualDynamicStep1Message=Start the server using {0}.",
    "GenericWebAppServerIntegrationProvider_ManualDynamicStep2Message=When the server is running, click Attach to select the server process to attach to."
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class GenericWebAppServerIntegrationProvider extends AbstractIntegrationProvider {
    private static final WizardStep NULL_WIZARD_STEP = new NullWizardStep();
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IntegrationProvider.IntegrationHints getAfterInstallationHints(AttachSettings attachSettings, boolean b) {
        return new IntegrationProvider.IntegrationHints();
    }

    public WizardStep getAttachedWizard() {
        return NULL_WIZARD_STEP;
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
        return Bundle.GenericWebAppServerIntegrationProvider_GenericWebappServerString();
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

    public boolean supportsDirect() {
        return true;
    }

    public boolean supportsDynamic() {
        return true;
    }

    public boolean supportsDynamicPid() {
        return true;
    }

    public boolean supportsLocal() {
        return true;
    }

    public boolean supportsManual() {
        return true;
    }

    public boolean supportsRemote() {
        return true;
    }

    protected int getAttachWizardPriority() {
        return 1;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                  AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        String pathToJDKDirectory = "&lt;" // NOI18N
                                    + Bundle.GenericWebAppServerIntegrationProvider_PathToJdkDirectory(
                                        IntegrationUtils.getJavaPlatformName(getTargetJava()))
                                    + "&gt;"; // NOI18N
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualDirectStep1Message(
                                IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                IntegrationUtils.getExportEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_HOME", // NOI18N
                                    pathToJDKDirectory, 
                                    true))); // NOI18N

        // Step 2
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualDirectStep2Message(
                                IntegrationUtils.getProfilerAgentCommandLineArgs(
                                    targetOS, 
                                    getTargetJava(),
                                    attachSettings.isRemote(),
                                    attachSettings.getPort(),
                                    false)));

        // Step 3
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualDirectStep3Message());

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        // Note about export vs. setenv on UNIXes
        if (!IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addHint(EXPORT_SETENV_MSG);
        }

        return instructions;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                   AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualDynamicStep1Message(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())));
        // Step 2
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualDynamicStep2Message());

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        // Note about export vs. setenv on UNIXes
        if (!IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addHint(EXPORT_SETENV_MSG);
        }

        instructions.addWarning(Bundle.GenericWebAppServerIntegrationProvider_DynamicWarningMessage(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava())));

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
        String pathToJDKDirectory = "&lt;" // NOI18N
                                    + Bundle.GenericWebAppServerIntegrationProvider_PathToJdkDirectory(
                                        IntegrationUtils.getJavaPlatformName(getTargetJava()))
                                    + "&gt;"; // NOI18N
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualRemoteStep3Message(
                                IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                IntegrationUtils.getExportEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_HOME", // NOI18N
                                    pathToJDKDirectory, 
                                    true)));

        // Step 4
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualRemoteStep4Message(
                                IntegrationUtils.getProfilerAgentCommandLineArgs(
                                    targetOS, 
                                    getTargetJava(),
                                    attachSettings.isRemote(),
                                    attachSettings.getPort(),
                                    false),
                                REMOTE_ABSOLUTE_PATH_HINT));

        // Step 5
        instructions.addStep(Bundle.GenericWebAppServerIntegrationProvider_ManualRemoteStep5Message());

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        // Note about export vs. setenv on UNIXes
        if (!IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addHint(EXPORT_SETENV_MSG);
        }

        return instructions;
    }
}
