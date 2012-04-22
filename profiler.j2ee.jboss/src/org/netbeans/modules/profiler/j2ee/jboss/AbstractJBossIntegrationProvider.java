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

package org.netbeans.modules.profiler.j2ee.jboss;

import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;import org.netbeans.modules.profiler.attach.providers.IDESettingsPersistor;
import org.netbeans.modules.profiler.attach.providers.SettingsPersistor;
import org.netbeans.modules.profiler.attach.providers.TargetPlatform;
import org.netbeans.modules.profiler.attach.providers.ValidationResult;
import org.netbeans.modules.profiler.attach.providers.scripted.AbstractScriptIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.scripted.ScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.providers.scripted.TextScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.wizard.steps.SimpleWizardStep;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "JBossIntegrationProvider_ProfiledJbossConsoleString=Profiled JBoss Console",
    "JBossIntegrationProvider_ManualRemoteStep3Msg=Create a copy of <code>{0}{1}bin{1}run{2}</code> and rename it to <code>run_nbprofiler{2}</code><br><code>{0}</code> stands for the JBoss installation directory on remote host.",
    "JBossIntegrationProvider_PathToJvmDirText=&lt;path to {0} directory&gt;",
    "JBossIntegrationProvider_ManualRemoteStep4Msg=Add the following lines at the beginning of <code>run_nbprofiler{0}</code>:<br><code>{1}<br>{2}</code><br>For setting <code>JAVA_OPTS</code> the {3}.",
    "JBossIntegrationProvider_ManualRemoteStep5Msg=Start the server using <code>run_nbprofiler{0}</code>",
    "JBossIntegrationProvider_ManualRemoteStep6Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "JBossIntegrationProvider_ManualDirectDynamicStep1Msg=Create a copy of <code>{0}{1}bin{1}run{2}</code> and rename it to <code>run_nbprofiler{2}</code><br><code>{0}</code> stands for the JBoss installation directory.",
    "JBossIntegrationProvider_ManualDirectStep2Msg=Add the following lines at the beginning of <code>run_nbprofiler{0}</code>:<br><code>{1}<br>{2}</code>",
    "JBossIntegrationProvider_ManualDirectDynamicStep3Msg=Start the server using <code>run_nbprofiler{0}</code>",
    "JBossIntegrationProvider_ManualDirectStep4Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "JBossIntegrationProvider_ManualDynamicStep2Msg=Add the following line at the beginning of <code>run_nbprofiler{0}</code>:<br><code>{1}</code>",
    "JBossIntegrationProvider_ManualDynamicStep4Msg=When the server is running click Attach to select the server process to attach to.",
    "JBossIntegrationProvider_IntegrReviewStep1Msg=Original file <code>{0}</code> will be copied to <code>{1}</code>",
    "JBossIntegrationProvider_IntegrReviewStep2Msg=The following line(s) will be added to the new file:<br><code>{0}</code>",
    "JBossIntegrationProvider_AdditionalStepsStep1DirectMsg=Use \"<code>{0}\"</code> command to start JBoss. JBoss JVM will start, but will not proceed with server execution until the profiler is connected.",
    "JBossIntegrationProvider_AdditionalStepsStep1DynamicMsg=Use \"<code>{0}\"</code> command to start JBoss.",
    "JBossIntegrationProvider_AdditionalStepsStep2Msg=After the wizard finishes, choose a profiling task and click Attach. For profiling CPU, you should set a meaningful instrumentation filter and/or profile only Part of Application to decrease profiling overhead.",
    "JBossIntegrationProvider_AdditionalStepsStep3DirectMsg=The profiler connects to the JBoss JVM and the server will start in profiling mode.",
    "JBossIntegrationProvider_AdditionalStepsStep3DynamicPidMsg=When the server is running, click Attach to select the server process to attach to.",
    "JBossIntegrationProvider_AdditionalStepsAutoStartMsg=If you check the \"Automatically start the server\" checkbox below, JBoss will be started automatically after this wizard finishes.",
    "JBossIntegrationProvider_DynamicWarningMessage=Make sure your IDE is using {0}."
})
public abstract class AbstractJBossIntegrationProvider extends AbstractScriptIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final String JBOSS_RUN_SCRIPT = "run"; // NOI18N
    private static final String JBOSS_HOME_VAR_STRING = "JBOSS_HOME"; // NOI18N
    private static final String INSERTION_POINT_NOWIN_0_STRING = "#!/bin/sh"; // NOI18N
    private static final String INSERTION_POINT_WIN_1_STRING = "@if not \"%ECHO%\" == \"\""; // NOI18N
    private static final String INSERTION_POINT_NONWIN_1_STRING = "DIRNAME=`dirname $0`"; // NOI18N
    private static final String RUN_MOD_EXT_STRING = "_nbprofiler"; // NOI18N
                                                                    // </editor-fold>

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private SettingsPersistor persistor;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public AbstractJBossIntegrationProvider() {
        super();
        this.attachedWizard = new SimpleWizardStep("JBoss provider settings", new JBossIntegrationPanel()); // NOI18N
        this.persistor = new IDESettingsPersistor() {
                protected String getSettingsFileName() {
                    return "JBossIntegrationProvider.properties"; // NOI18N
                }

                protected void parsePersistableSettings(Properties settings) {
                    setTargetJava(settings.getProperty("JBossIntegrationProvider_" + getMagicNumber() + "_JavaPlatform", "")); // NOI18N
                    setInstallationPath(settings.getProperty("JBossIntegrationProvider_" + getMagicNumber() + "_InstallDir", "")); // NOI18N

                    if ((getInstallationPath() == null) || (getInstallationPath().length() == 0)) {
                        setInstallationPath(getDefaultInstallationPath());
                    }
                }

                protected Properties preparePersistableSettings() {
                    Properties settings = new Properties();
                    settings.setProperty("JBossIntegrationProvider_" + getMagicNumber() + "_JavaPlatform", getTargetJava()); // NOI18N
                    settings.setProperty("JBossIntegrationProvider_" + getMagicNumber() + "_InstallDir", getInstallationPath()); // NOI18N

                    return settings;
                }
            };
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IntegrationProvider.IntegrationHints getAfterInstallationHints(AttachSettings attachSettings, boolean automation) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();
        String targetOS = attachSettings.getHostOS();

        // Step 1
        if (attachSettings.isDirect()) {
            instructions.addStep(Bundle.JBossIntegrationProvider_AdditionalStepsStep1DirectMsg(getModifiedScriptPath(targetOS, false)));
        } else {
            instructions.addStep(Bundle.JBossIntegrationProvider_AdditionalStepsStep1DynamicMsg(getModifiedScriptPath(targetOS, false)));
        }

        // Step 2
        instructions.addStep(Bundle.JBossIntegrationProvider_AdditionalStepsStep2Msg());

        // Step 3
        if (attachSettings.isDirect()) {
            instructions.addStep(Bundle.JBossIntegrationProvider_AdditionalStepsStep3DirectMsg());
        } else {
            instructions.addStep(Bundle.JBossIntegrationProvider_AdditionalStepsStep3DynamicPidMsg());
            instructions.addWarning(Bundle.JBossIntegrationProvider_DynamicWarningMessage(IntegrationUtils.getJavaPlatformName(getTargetJava())));
        }

        // automatic server startup note
        instructions.addHint(Bundle.JBossIntegrationProvider_AdditionalStepsAutoStartMsg());

        return instructions;
    }

    public IntegrationProvider.IntegrationHints getIntegrationReview(AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();
        String targetOS = attachSettings.getHostOS();

        String agentArgs = IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS,
                                getTargetJava(),
                                false,
                                attachSettings.getPort()
        );
        
        // Step 1
        instructions.addStep(Bundle.JBossIntegrationProvider_IntegrReviewStep1Msg(
                                new File(getScriptPath(targetOS, true)),
                                getModifiedScriptPath(targetOS, true)));

        // Step 2
        instructions.addStep(Bundle.JBossIntegrationProvider_IntegrReviewStep2Msg(
                                IntegrationUtils.getAssignEnvVariableValueString(
                                    targetOS, "JAVA_HOME",
                                    getTargetJavaHome())
                                + "<br>"
                                + (attachSettings.isDirect()
                                    ? (""
                                    + IntegrationUtils.getAssignEnvVariableValueString(
                                        targetOS,
                                        "JAVA_OPTS",
                                        agentArgs)
                                    + "<br>") : "")));

        addLinkWarning(instructions, "JAVA_OPTS", attachSettings);
        
        return instructions;
    }

    public IntegrationProvider.IntegrationHints getModificationHints(AttachSettings attachSettings) {
        String targetOS = attachSettings.getHostOS();

        
        IntegrationHints h;
        // Remote attach instructions
        if (attachSettings.isRemote()) {
            h = getManualRemoteIntegrationStepsInstructions(targetOS, attachSettings);
        }
        // Local direct attach
        else if (attachSettings.isDirect()) {
            h = getManualLocalDirectIntegrationStepsInstructions(targetOS, attachSettings);
        }
        // Local dynamic attach
        else {
            h = getManualLocalDynamicIntegrationStepsInstructions(targetOS, attachSettings);
        }
        
        addLinkWarning(h, "JAVA_OPTS", attachSettings); // NOI18N
        
        return h;
    }

    public SettingsPersistor getSettingsPersistor() {
        return this.persistor;
    }

    public void setTargetJvm(TargetPlatform javaPlatform) {
        if (javaPlatform == null) {
            return;
        }

        this.setTargetJavaHome(javaPlatform.getHomePath());
        this.setTargetJava(javaPlatform.toString());
    }

    // <editor-fold defaultstate="collapsed" desc="WizardIntegrationProvider implementation">
    public boolean supportsRemote() {
        return true;
    }

    // </editor-fold>
    // </editor-fold>
    public ValidationResult validateInstallation(final String targetOS, final String path) {
        if (!new File(path).exists()) {
            return new ValidationResult(false, "The installation path doesn't exist"); // NOI18N
        }

        if (!new File(getJbossScriptPath(path, targetOS, "", false)).exists()) { // NOI18N

            return new ValidationResult(false, "Invalid installation directory"); // NOI18N
        }

        return new ValidationResult(true);
    }

    protected boolean isBackupRequired() {
        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Modification scripts">
    protected ScriptHeaderModifier getHeaderModifier(final String targetOS) {
        return new TextScriptHeaderModifier(IntegrationUtils.getSilentScriptCommentSign(targetOS));
    }

    protected abstract int getMagicNumber();

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                    AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();
        
        String agentArgs = IntegrationUtils.getProfilerAgentCommandLineArgs(
                            targetOS,
                            getTargetJava(),
                            attachSettings
                            .isRemote(),
                            attachSettings.getPort()
        );
        
        // Step 1
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDirectDynamicStep1Msg(
                                IntegrationUtils.getEnvVariableReference("JBOSS_HOME", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS),
                                IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 2
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDirectStep2Msg(
                                                      IntegrationUtils.getBatchExtensionString(targetOS),
                                                      IntegrationUtils.getAssignEnvVariableValueString(
                                                            targetOS, 
                                                            "JAVA_HOME", // NOI18N
                                                            Bundle.JBossIntegrationProvider_PathToJvmDirText(
                                                                IntegrationUtils.getJavaPlatformName(getTargetJava()))),
                                                      IntegrationUtils.getAssignEnvVariableValueString(
                                                            targetOS, 
                                                            "JAVA_OPTS", // NOI18N
                                                            agentArgs)));

        // Step 3
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDirectDynamicStep3Msg(
                                IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 4
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDirectStep4Msg());
        
        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        // Note about export vs. setenv on UNIXes
        if (!IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addHint(EXPORT_SETENV_MSG);
        }

        return instructions;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                     AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDirectDynamicStep1Msg(
                                IntegrationUtils.getEnvVariableReference("JBOSS_HOME", targetOS), // NOi18N
                                IntegrationUtils.getDirectorySeparator(targetOS),
                                IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 2
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDynamicStep2Msg(
                                IntegrationUtils.getBatchExtensionString(targetOS),
                                IntegrationUtils.getAssignEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_HOME", // NOI18N
                                    Bundle.JBossIntegrationProvider_PathToJvmDirText(
                                        IntegrationUtils.getJavaPlatformName(getTargetJava())))));

        // Step 3
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDirectDynamicStep3Msg(
                                IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 4
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualDynamicStep4Msg());

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        // Note about export vs. setenv on UNIXes
        if (!IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addHint(EXPORT_SETENV_MSG);
        }

        instructions.addWarning(Bundle.JBossIntegrationProvider_DynamicWarningMessage(
                                    IntegrationUtils.getJavaPlatformName(getTargetJava())));

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
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualRemoteStep3Msg(
                                IntegrationUtils.getEnvVariableReference("REMOTE_JBOSS_HOME", targetOS), // NOI18N
                                IntegrationUtils.getDirectorySeparator(targetOS),
                                IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 4
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualRemoteStep4Msg(
                                IntegrationUtils.getBatchExtensionString(targetOS),
                                IntegrationUtils.getAssignEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_HOME", // NOI18N
                                    Bundle.JBossIntegrationProvider_PathToJvmDirText(
                                        IntegrationUtils.getJavaPlatformName(getTargetJava()))),
                                IntegrationUtils.getAssignEnvVariableValueString(
                                    targetOS, 
                                    "JAVA_OPTS", // NOI18N
                                    IntegrationUtils.getProfilerAgentCommandLineArgs(
                                        targetOS,
                                        getTargetJava(),
                                        attachSettings.isRemote(),
                                        attachSettings.getPort())),
                                        IntegrationUtils.getRemoteAbsolutePathHint()));

        // Step 5
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualRemoteStep5Msg(IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 6
        instructions.addStep(Bundle.JBossIntegrationProvider_ManualRemoteStep6Msg());

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        // Note about export vs. setenv on UNIXes
        if (!IntegrationUtils.isWindowsPlatform(targetOS)) {
            instructions.addHint(EXPORT_SETENV_MSG);
        }

        return instructions;
    }

    protected String getModifiedScriptPath(final String targetOS, final boolean quoted) {
        return getJbossScriptPath(targetOS, RUN_MOD_EXT_STRING, quoted);
    }

    protected String getScriptPath(final String targetOS, final boolean quoted) {
        return getJbossScriptPath(targetOS, "", quoted); // NOI18N
    }

    protected String getWinConsoleString() {
        return Bundle.JBossIntegrationProvider_ProfiledJbossConsoleString();
    }

    protected void generateCommands(String targetOS, Collection commandsArray) {
        commandsArray.add(getModifiedScriptPath(targetOS, false));
    }

    protected void modifyScriptFileForDirectAttach(final String targetOS, final int commPort, final boolean isReplaceFile,
                                                   final StringBuffer buffer) {
        String lineBreak = IntegrationUtils.getLineBreak(targetOS);

        // init insertion points
        int insertionPoint0;

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            insertionPoint0 = 0;
        } else {
            insertionPoint0 = buffer.indexOf(INSERTION_POINT_NOWIN_0_STRING) + INSERTION_POINT_NOWIN_0_STRING.length() + 1;
        }

        int insertionPoint1 = (IntegrationUtils.isWindowsPlatform(targetOS) ? buffer.indexOf(INSERTION_POINT_WIN_1_STRING)
                                                                            : buffer.indexOf(INSERTION_POINT_NONWIN_1_STRING));

        // java opts for debugging
        String debugJavaOpts = ""; // NOI18N
                                   // debugging property for agent side - wire I/O

        if (System.getProperty("org.netbeans.lib.profiler.wireprotocol.WireIO.agent") != null) { // NOI18N
            debugJavaOpts += " -Dorg.netbeans.lib.profiler.wireprotocol.WireIO=true"; // NOI18N
                                                                                      // debugging property for agent side - Class loader hook
        }

        if (System.getProperty("org.netbeans.lib.profiler.server.ProfilerInterface.classLoadHook") != null) { // NOI18N
            debugJavaOpts += " -Dorg.netbeans.lib.profiler.server.ProfilerInterface.classLoadHook=true"; // NOI18N
        }

        // create new lines
        String header = (isReplaceFile ? IntegrationUtils.getProfilerModifiedReplaceFileHeader(targetOS)
                                       : IntegrationUtils.getProfilerModifiedFileHeader(targetOS)) + lineBreak;
        String exportJavaHome = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", this.getTargetJavaHome())
                                + lineBreak; // NOI18N
        String exportNativeLibraries = IntegrationUtils.getAddProfilerLibrariesToPathString(targetOS, this.getTargetJava(),
                                                                                            false, false) + lineBreak;
        String javaOpts = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_OPTS",
                                                                           IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                            this.getTargetJava(),
                                                                                                                            false,
                                                                                                                            commPort)
                                                                           + debugJavaOpts) + lineBreak; // NOI18N

        // init counter for insertionPoints offset
        int currentOffset = 0;

        // write header
        buffer.insert(insertionPoint0 + currentOffset, header);
        currentOffset += header.length();

        // write JAVA_HOME
        buffer.insert(insertionPoint1 + currentOffset, exportJavaHome);
        currentOffset += exportJavaHome.length();

        // write JAVA_OPTS to appropriate place
        buffer.insert(insertionPoint1 + currentOffset, javaOpts);
        currentOffset += javaOpts.length();
    }

    protected void modifyScriptFileForDynamicAttach(final String targetOS, final int port, final boolean isReplaceFile,
                                                    final StringBuffer buffer) {
        String lineBreak = IntegrationUtils.getLineBreak(targetOS);

        // init insertion points
        int insertionPoint0;

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            insertionPoint0 = 0;
        } else {
            insertionPoint0 = buffer.indexOf(INSERTION_POINT_NOWIN_0_STRING) + INSERTION_POINT_NOWIN_0_STRING.length() + 1;
        }

        int insertionPoint1 = (IntegrationUtils.isWindowsPlatform(targetOS) ? buffer.indexOf(INSERTION_POINT_WIN_1_STRING)
                                                                            : buffer.indexOf(INSERTION_POINT_NONWIN_1_STRING));

        // create new lines
        String header = IntegrationUtils.getProfilerModifiedFileHeader(targetOS) + lineBreak;
        String exportJavaHome = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", this.getTargetJavaHome())
                                + lineBreak + lineBreak; // NOI18N

        // init counter for insertionPoints offset
        int currentOffset = 0;

        // write header
        buffer.insert(insertionPoint0 + currentOffset, header);
        currentOffset += header.length();

        // write JAVA_HOME
        buffer.insert(insertionPoint1 + currentOffset, exportJavaHome);
        currentOffset += exportJavaHome.length();
    }
    
    private String getDefaultInstallationPath() {
        String home = ""; // NOI18N

        try {
            String homeEnv = System.getenv(JBOSS_HOME_VAR_STRING); // java.lang.Error: getenv no longer supported exception is thrown on 1.4.2

            if ((homeEnv != null) && (homeEnv.length() > 1)) {
                File homeDir = new File(homeEnv);

                if (homeDir.exists() && homeDir.isDirectory()) {
                    home = homeEnv;
                }
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) { /* IGNORE */
        }

        return home;
    }

    private String getJbossScriptPath(final String targetOS, final String scriptPostfix, boolean useQuotas) {
        return getJbossScriptPath(this.getInstallationPath(), targetOS, scriptPostfix, useQuotas);
    }

    private String getJbossScriptPath(final String dirPath, final String targetOS, final String scriptPostfix, boolean useQuotas) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        String ext = IntegrationUtils.getBatchExtensionString(targetOS);
        String quotas = ""; // NOI18N

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            quotas = "\""; // NOI18N
        }

        StringBuilder path = new StringBuilder();
        path.append(dirPath);

        if (!dirPath.endsWith(separator)) {
            path.append(separator);
        }

        path.append("bin").append(separator).append(JBOSS_RUN_SCRIPT).append(scriptPostfix).append(ext); // NOI18N

        if (useQuotas) {
            path.insert(0, quotas);
            path.append(quotas);
        }

        return path.toString();
    }
}
