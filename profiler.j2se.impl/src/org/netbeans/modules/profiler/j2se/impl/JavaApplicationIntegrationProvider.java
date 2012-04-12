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

package org.netbeans.modules.profiler.j2se.impl;

import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import java.text.MessageFormat;
import org.netbeans.modules.profiler.attach.providers.AbstractIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.IntegrationCategorizer;
import org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "JavaApplicationIntegrationProvider_Title=Java Application",
    "JavaApplicationIntegrationProvider_ManualRemoteStep3Message=Run the application using {0}. When starting the application, provide the extra startup option to the <code>java</code> command:<br><code>{1}</code><br>The {2}.",
    "JavaApplicationIntegrationProvider_ManualRemoteStep4Message=The JVM will start, but will not proceed with application execution until you connect the profiler.",
    "JavaApplicationIntegrationProvider_ManualRemoteHintMessage=You can use <code>{0}</code> command instead of <code>java</code> command to run your application in profiling mode. Check the script for any additional modifications according to your system configuration.",
    "JavaApplicationIntegrationProvider_ManualDirectStep1Message=Run the application using {0}. When starting the application, provide the extra startup option to the <code>java</code> command:<br><code>{1}</code>",
    "JavaApplicationIntegrationProvider_ManualDirectStep2Message=The JVM will start, but will not proceed with application execution until you connect the profiler.",
    "JavaApplicationIntegrationProvider_DynamicWarningMessage=Make sure your IDE is using {0}.",
    "JavaApplicationIntegrationProvider_ManualDynamicStep1Message=Start the Application using {0}.",
    "JavaApplicationIntegrationProvider_ManualDynamicStep2Message=When the application is running, click Attach to select the application process to attach to."
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class JavaApplicationIntegrationProvider extends AbstractIntegrationProvider {
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
        return Bundle.JavaApplicationIntegrationProvider_Title();
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
        hints.addStep(Bundle.JavaApplicationIntegrationProvider_ManualDirectStep1Message(
                        IntegrationUtils.getJavaPlatformName(getTargetJava()),
                        IntegrationUtils.getProfilerAgentCommandLineArgs(
                            targetOS, 
                            getTargetJava(),
                            attachSettings.isRemote(),
                            attachSettings.getPort(),
                            false)));

        // Step 2
        hints.addStep(Bundle.JavaApplicationIntegrationProvider_ManualDirectStep2Message());

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
        hints.addStep(Bundle.JavaApplicationIntegrationProvider_ManualDynamicStep1Message(
                        IntegrationUtils.getJavaPlatformName(getTargetJava())));
        // Step 2
        hints.addStep(Bundle.JavaApplicationIntegrationProvider_ManualDynamicStep2Message());

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        // Put here a warning that the IDE must be run under JDK6/7/8
        hints.addWarning(Bundle.JavaApplicationIntegrationProvider_DynamicWarningMessage(
                            IntegrationUtils.getJavaPlatformName(getTargetJava())));

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
        hints.addStep(Bundle.JavaApplicationIntegrationProvider_ManualRemoteStep3Message(
                        IntegrationUtils.getJavaPlatformName(getTargetJava()),
                        IntegrationUtils.getProfilerAgentCommandLineArgs(
                            targetOS, 
                            getTargetJava(),
                            attachSettings.isRemote(),
                            attachSettings.getPort()),
                        REMOTE_ABSOLUTE_PATH_HINT));

        // Step 4
        hints.addStep(Bundle.JavaApplicationIntegrationProvider_ManualRemoteStep4Message());

        // Note about spaces in path when starting Profiler agent
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(SPACES_IN_PATH_WARNING_MSG);
        }

        // Note about export vs. setenv on UNIXes
        hints.addHint(Bundle.JavaApplicationIntegrationProvider_ManualRemoteHintMessage(
                        IntegrationUtils.getRemoteProfileCommandString(targetOS, getTargetJava())));

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        return hints;
    }
}
