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

package org.netbeans.modules.profiler.j2ee.sunas;

import java.text.MessageFormat;
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
    "SunAS7IntegrationProvider_SunAs7String=Sun Java System Application Server 7",
    "SunAS7IntegrationProvider_WorkingDirHintMsg=Working directory is typically {0}{1}domains{1}<YOUR_DOMAIN>{1}<YOUR_SERVER>{1}config.",
    "SunAS7IntegrationProvider_ManualRemoteStep3Msg=Locate the correct <code>server.xml</code> configuration file for your server\\:<br><code>{0}{1}domains{1}&lt;YOUR_DOMAIN&gt;{1}&lt;YOUR_SERVER&gt;{1}config{1}server.xml</code><br>where <code>{2}</code> is the Sun Java System Application Server 7 installation directory on remote host. <code>&lt;YOUR_DOMAIN&gt;</code> stands for the actual domain (typically \"<code>domain</code>\" or \"<code>domain1</code>\") and <code>&lt;YOUR_SERVER&gt;</code> stands for the actual server (typically \"<code>server</code>\" or \"<code>server1</code>\").",
    "SunAS7IntegrationProvider_ManualRemoteStep4Msg=If the Sun Java System Application Server 7 is configured to run on a different JVM, set the server to run on {0}. To do this, find the <code>&lt;java-config&gt;</code> xml element in <code>server.xml</code> and change its \"<code>java-home</code>\" attribute as follows\\:<br><code>&lt;java-config java-home=\"&lt;path to {0} directory&gt;\" ...&gt;</code>",
    "SunAS7IntegrationProvider_ManualRemoteStep5Msg=Place the following <code>&lt;profiler&gt;</code> element right after the <code>&lt;java-config&gt;</code> before the first <code>&lt;jvm-options&gt;</code> element\\:<br><code>&lt;profiler enabled=\"true\" name=\"NetBeansProfiler\"&gt;<br>&nbsp;&nbsp;&lt;jvm-options&gt;{0}&lt;/jvm-options&gt;<br>&lt;/profiler&gt;</code><br>In <code>&lt;jvm-options&gt;</code> element the {1}.",
    "SunAS7IntegrationProvider_ManualRemoteStep6Msg=Start the server from the <code>{0}{1}bin</code> directory using <code>asadmin start-domain --domain &lt;YOUR_DOMAIN&gt;</code><br>The JVM will start, but will not proceed with server instance execution until you connect the profiler.",
    "SunAS7IntegrationProvider_ManualDirectStep1Msg=Locate the correct <code>server.xml</code> configuration file for your server\\:<br><code>{0}{1}domains{1}&lt;YOUR_DOMAIN&gt;{1}&lt;YOUR_SERVER&gt;{1}config{1}server.xml</code><br>where <code>{0}</code> is the Sun Java System Application Server 7 installation directory. <code>&lt;YOUR_DOMAIN&gt;</code> stands for the actual domain (typically \"<code>domain</code>\" or \"<code>domain1</code>\") and <code>&lt;YOUR_SERVER&gt;</code> stands for the actual server (typically \"<code>server</code>\" or \"<code>server1</code>\").",
    "SunAS7IntegrationProvider_ManualDirectStep2Msg=If the Sun Java System Application Server 7 is configured to run on a different JVM, set the server to run on {0}. To do this, find the <code>&lt;java-config&gt;</code> xml element in <code>server.xml</code> and change its \"<code>java-home</code>\" attribute as follows\\:<br><code>&lt;java-config java-home=\"&lt;path to {0} directory&gt;\" ...&gt;</code>",
    "SunAS7IntegrationProvider_ManualDirectStep3Msg=Place the following <code>&lt;profiler&gt;</code> element right after the <code>&lt;java-config&gt;</code> before the first <code>&lt;jvm-options&gt;</code> element\\:<br><code>&lt;profiler enabled=\"true\" name=\"NetBeansProfiler\"&gt;<br>&nbsp;&nbsp;&lt;jvm-options&gt;{0}&lt;/jvm-options&gt;<br>&lt;/profiler&gt;</code>",
    "SunAS7IntegrationProvider_ManualDirectStep4Msg=Start the server from the <code>{0}{1}bin</code> directory using <code>asadmin start-domain --domain &lt;YOUR_DOMAIN&gt;</code><br>The JVM will start, but will not proceed with server instance execution until you connect the profiler.",
    "SunAS7IntegrationProvider_ManualDynamicStep1Msg=Locate the correct <code>server.xml</code> configuration file for your server\\:<br><code>{0}{1}domains{1}&lt;YOUR_DOMAIN&gt;{1}&lt;YOUR_SERVER&gt;{1}config{1}server.xml</code><br>where <code>{0}</code> is the Sun Java System Application Server 7 installation directory. <code>&lt;YOUR_DOMAIN&gt;</code> stands for the actual domain (typically \"<code>domain</code>\" or \"<code>domain1</code>\") and <code>&lt;YOUR_SERVER&gt;</code> stands for a the actual server (typically \"<code>server</code>\" or \"<code>server1</code>\").",
    "SunAS7IntegrationProvider_ManualDynamicStep2Msg=If the Sun Application Server 7 is configured to run on different JVM, setup it to run on {0}. To do this, find the <code>&lt;java-config&gt;</code> xml element in <code>server.xml</code> and change its \"<code>java-home</code>\" attribute as follows\\:<br><code>&lt;java-config java-home=\"&lt;path to {0} directory&gt;\" ...&gt;</code>",
    "SunAS7IntegrationProvider_ManualDynamicStep3Msg=Start the server from the <code>{0}{1}bin</code> directory using <code>asadmin start-domain --domain &lt;YOUR_DOMAIN&gt;</code><br>When the server is running, click Attach to select the server process to attach to.",
    "SunAS7IntegrationProvider_DoubleQuotesWarningMsg=You may need to use double quotes for the <code>&lt;jvm-options&gt;</code> element value on some systems/configurations\\:<br><code>&lt;jvm-options&gt;\"{0}\"&lt;/jvm-options&gt;</code>"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.attach.spi.IntegrationProvider.class)
public class SunAS7IntegrationProvider extends AbstractIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final WizardStep NULL_WIZARD_STEP = new NullWizardStep();

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IntegrationProvider.IntegrationHints getAfterInstallationHints(AttachSettings attachSettings, boolean b) {
        return new IntegrationProvider.IntegrationHints();
    }

    public WizardStep getAttachedWizard() {
        return NULL_WIZARD_STEP;
    }

    public String getDynamicWorkingDirectoryHint(String targetOS, AttachSettings attachSettings) {
        return Bundle.SunAS7IntegrationProvider_WorkingDirHintMsg(
                    IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS), // NOI18N
                    IntegrationUtils.getDirectorySeparator(targetOS));
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
        return Bundle.SunAS7IntegrationProvider_SunAs7String();
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
        return Bundle.SunAS7IntegrationProvider_DoubleQuotesWarningMsg(
                    getProfilerAgentCommandLineArgsForDomainScript(
                        targetOS, 
                        attachSettings.isRemote(),
                        attachSettings.getPort()));
    }

    private IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                  AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDirectStep1Msg(
                                IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS)));

        // Step 2
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDirectStep2Msg(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())));

        // Step 3
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDirectStep3Msg(
                                getProfilerAgentCommandLineArgsForDomainScript(
                                    targetOS,
                                    attachSettings.isRemote(),
                                    attachSettings.getPort())));

        // Step 4
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDirectStep4Msg(
                                IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS)));

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
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDynamicStep1Msg(
                                IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS)));

        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDynamicStep2Msg(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())));
        // Step 3
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualDynamicStep3Msg(
                                IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS)));

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        instructions.addWarning(Bundle.SunASIntegrationProvider_DynamicWarningMessage(
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
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualRemoteStep3Msg(
                                IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS),
                                IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS))); // NOI18N

        // Step 4
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualRemoteStep4Msg(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())));

        // Step 5
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualRemoteStep5Msg(
                                getProfilerAgentCommandLineArgsForDomainScript(
                                    targetOS,
                                    attachSettings.isRemote(),
                                    attachSettings.getPort()),
                                    IntegrationUtils.getRemoteAbsolutePathHint()));

        // Step 6
        instructions.addStep(Bundle.SunAS7IntegrationProvider_ManualRemoteStep6Msg(
                                IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS)));

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

        return "-agentpath:" + IntegrationUtils.getNativeLibrariesPath(targetOS, getTargetJava(), isRemote) // NOI18N
               + IntegrationUtils.getDirectorySeparator(targetOS) + IntegrationUtils.getProfilerAgentLibraryFile(targetOS) + "=" //NOI18N
               + "\"" + IntegrationUtils.getLibsDir(targetOS, isRemote) + "\"" + "," + portNumber; //NOI18N
    }
}
