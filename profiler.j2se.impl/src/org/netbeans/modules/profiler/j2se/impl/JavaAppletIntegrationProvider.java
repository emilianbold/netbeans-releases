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
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "JavaAppletIntegrationProvider_Title=Java Applet",
    "JavaAppletIntegrationProvider_AppletWorkDirWindowsMessage=Applet working directory depends on your web browser type. For Internet Explorer use <%USERPROFILE%>\\Desktop, for Mozilla and Firefox use the browser installation directory, for Opera use the path where the html page launching the applet is located.",
    "JavaAppletIntegrationProvider_AppletWorkDirUnixesMessage=Applet working directory is typically the location from where the web browser running the applet was started.",
    "JavaAppletIntegrationProvider_OperaRemoteAttachWarningMessage=Because the Opera web browser uses bundled or standard JRE for running applets rather than the Java Plug-in and no arguments can be passed to it, it cannot be used for profiling applets remotely.",
    "JavaAppletIntegrationProvider_ManualRemoteStep3Message=Close all windows and tabs of the remote web browser.",
    "JavaAppletIntegrationProvider_ManualRemoteStep4WindowsMessage=Open the Java Control Panel from the Windows Control Panel. Open the Java tab and click View in Java Applet Runtime Settings area. The Java Runtime Settings dialog opens. Double-click the Java Runtime Parameters cell and insert following string:<br><code>{0}</code><br>If <code>&lt;remote&gt;</code> contains spaces the entire string must be double-quoted. Close the dialog using the OK button and then click OK or Apply in the Java Control Panel.",
    "JavaAppletIntegrationProvider_ManualRemoteStep4UnixesMessage=Open the Java Control Panel of the JRE that will run profiled applet using <code>&lt;$JAVA_HOME&gt;/jre/bin/ControlPanel</code> command. Open the Java tab and click View in the Java Applet Runtime Settings area. The Java Runtime Settings dialog opens. Double-click the Java Runtime Parameters cell and insert the following string:<br><code>{0}</code><br>Close the dialog using the OK button and then click OK or Apply in the Java Control Panel.",
    "JavaAppletIntegrationProvider_ManualRemoteStep5Message=Start the applet in the remote web browser. The browser window opens but the applet will not start until you connect the profiler.",
    "JavaAppletIntegrationProvider_ManualRemoteStopAppletMessage=The applet JVM will keep running until you stop it using the Detach Profiler action or until all windows and tabs of the remote web browser are closed.",
    "JavaAppletIntegrationProvider_ManualRemoteRestoreSettingsMessage=You will need to restore the original Java Runtime Parameters cell value to be able to run applets without profiling on the remote system.",
    "JavaAppletIntegrationProvider_OperaDirectAttachWarningMessage=Because the Opera web browser uses bundled or standard JRE for running applets rather than the Java Plug-in and no arguments can be passed to it, it cannot be used for profiling applets using direct attachment.",
    "JavaAppletIntegrationProvider_ManualLocalStep1Message=Close all windows and tabs of your web browser.",
    "JavaAppletIntegrationProvider_ManualDirectStep2WindowsMessage=Open the Java Control Panel from the Windows Control Panel. Open the Java tab and click View in the Java Applet Runtime Settings area. The Java Runtime Settings dialog opens. Double-click the Java Runtime Parameters cell and insert following string:<br><code>{0}</code><br>Close the dialog using the OK button and then click OK or Apply in the Java Control Panel.",
    "JavaAppletIntegrationProvider_ManualDirectStep2UnixesMessage=Open the Java Control Panel of the JRE that will run the profiled applet using <code>&lt;$JAVA_HOME&gt;/jre/bin/ControlPanel</code> command. Open the Java tab and click View in the Java Applet Runtime Settings area. The Java Runtime Settings dialog opens. Double-click the Java Runtime Parameters cell and insert following string:<br><code>{0}</code><br>Close the dialog using the OK button and then click OK or Apply in the Java Control Panel.",
    "JavaAppletIntegrationProvider_ManualDirectStep2MacMessage=Open the Java Preferences.app located in <code>/Applications/Utilities/Java</code> directory. Open the General tab and click in the Java Applet Runtime Parameters area and insert following string:<br><code>{0}</code><br>Click the Save button and quit the Java Preferences application.",
    "JavaAppletIntegrationProvider_ManualDirectStep3Message=Start the applet in the web browser. The browser window opens but the applet will not start until you connect the profiler.",
    "JavaAppletIntegrationProvider_ManualDirectStopAppletMessage=The applet JVM will keep running until you stop it using the Detach Profiler action or until all windows and tabs of the web browser are closed.",
    "JavaAppletIntegrationProvider_ManualDirectRestoreSettingsMessage=You will need to restore the original Java Runtime Parameters cell value to be able to run applets without profiling.",
    "JavaAppletIntegrationProvider_ManualDynamicStep2WindowsMessage=Open the Java Control Panel from the Windows Control Panel and make sure that {0} will be used to run the profiled applet.<br><br>Note: The Opera web browser does not use Java Plug-in to run applets. Please see Opera online documentation for instructions about how to change the JRE used for running applets.",
    "JavaAppletIntegrationProvider_ManualDynamicStep2UnixesMessage=Make sure that your browser has {0} set to run the profiled applet.",
    "JavaAppletIntegrationProvider_ManualDynamicStep3Message=Start the applet in the web browser. When the applet is running, click Attach to select the applet process to attach to."
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class JavaAppletIntegrationProvider extends AbstractIntegrationProvider {
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
        return Bundle.JavaAppletIntegrationProvider_Title();
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
            hints.addWarning(Bundle.JavaAppletIntegrationProvider_OperaDirectAttachWarningMessage());
        }

        // Step 1
        hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualLocalStep1Message());

        // Step 2
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            String args = IntegrationUtils.getProfilerAgentCommandLineArgs(
                    targetOS, getTargetJava(), attachSettings.isRemote(), attachSettings.getPort(), false);
//            if (args.indexOf(' ') != -1) args = "\"" + args + "\""; // NOI18N  Bugfix #173041
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDirectStep2WindowsMessage(args));
        } else if (IntegrationUtils.PLATFORM_MAC_OS.equals(targetOS)) {
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDirectStep2MacMessage(
                            IntegrationUtils.getProfilerAgentCommandLineArgs(
                                targetOS,
                                getTargetJava(),
                                attachSettings.isRemote(),
                                attachSettings.getPort(),
                                false)));
        } else {
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDirectStep2UnixesMessage(
                            IntegrationUtils.getProfilerAgentCommandLineArgs(
                                targetOS,
                                getTargetJava(),
                                attachSettings.isRemote(),
                                attachSettings.getPort(),
                                false)));
        }

        // Step 3
        hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDirectStep3Message());

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        // Note about stopping Applet JVM
        hints.addHint(Bundle.JavaAppletIntegrationProvider_ManualDirectStopAppletMessage());

        // Note about stopping Applet JVM
        hints.addHint(Bundle.JavaAppletIntegrationProvider_ManualDirectRestoreSettingsMessage());

        return hints;
    }

    private IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                   AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Step 1
        hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualLocalStep1Message());

        // Step 2
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDynamicStep2WindowsMessage(
                            IntegrationUtils.getJavaPlatformName(getTargetJava())));
        } else {
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDynamicStep2UnixesMessage(
                            IntegrationUtils.getJavaPlatformName(getTargetJava())));
        }

        // Step 3
        hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualDynamicStep3Message());

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

        // Windows & Opera & remote attach warning
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addWarning(Bundle.JavaAppletIntegrationProvider_OperaRemoteAttachWarningMessage());
        }

        // Step 1
        hints.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        hints.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualRemoteStep3Message());

        // Step 4
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualRemoteStep4WindowsMessage(
                            IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(
                                targetOS,
                                getTargetJava(),
                                attachSettings.isRemote(),
                                attachSettings.getPort())));
        } else {
            hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualRemoteStep4UnixesMessage(
                            IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(
                                targetOS,
                                getTargetJava(),
                                attachSettings.isRemote(),
                                attachSettings.getPort())));
        }

        // Step 5
        hints.addStep(Bundle.JavaAppletIntegrationProvider_ManualRemoteStep5Message());

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        // Note about stopping Applet JVM
        hints.addHint(Bundle.JavaAppletIntegrationProvider_ManualRemoteStopAppletMessage());

        // Note about stopping Applet JVM
        hints.addHint(Bundle.JavaAppletIntegrationProvider_ManualRemoteRestoreSettingsMessage());

        return hints;
    }
}
