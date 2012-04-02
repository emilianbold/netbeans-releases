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
package org.netbeans.modules.profiler.j2ee.tomcat;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.IDESettingsPersistor;
import org.netbeans.modules.profiler.attach.providers.SettingsPersistor;
import org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum;
import org.netbeans.modules.profiler.attach.providers.ValidationResult;
import org.netbeans.modules.profiler.attach.providers.scripted.AbstractScriptIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.scripted.ScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.providers.scripted.TextScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.wizard.steps.SimpleWizardStep;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "AttachWizard_LocateRequiredFilesString=Locate Required Files",
    "TomcatIntegrationProvider_RemoteAutomaticIntegrUnsupportedMsg=Automatic integration not supported for remote server",
    "TomcatIntegrationProvider_StartTargetUnsupportedMsg=TomcatIntegrationProvider: start target not supported for these settings:\n{0}",
    "TomcatIntegrationProvider_ProfiledTomcatConsoleString=Profiled Tomcat Console",
    "TomcatIntegrationProvider_ErrorStartingTomcatMsg=TomcatIntegrationProvider: Error starting integrated Tomcat:\\n{0}",
    "TomcatIntegrationProvider_PathNotExistsMsg=The selected path doesn not exist",
    "TomcatIntegrationProvider_InvalidCatalinaHomeMsg=The installation path does not exist",
    "TomcatIntegrationProvider_InvalidCatalinaBaseMsg=Invalid Catalina Base directory",
    "TomcatIntegrationProvider_Catalina_Base_Hint=&lt;Path to your CATALINA_BASE&gt;",
    "TomcatIntegrationProvider_PathToJvmDirText=&lt;path to {0} directory&gt;",
    "TomcatIntegrationProvider_ManualWinExeHint=If you are using Tomcat on Windows OS you might not be able to find <code>{0}</code> file. In this case create a new file <code>{1}</code>, copy the following lines, and use the newly created file to start the server:<br><code>{2}</code>",
    "TomcatIntegrationProvider_ManualRemoteStep3Msg=Create a copy of <code>{0}{1}bin{1}{2}{3}</code> and rename it to <code>{2}_nbprofiler{3}</code><br><code>{0}</code> stands for the Tomcat installation directory on remote host.",
    "TomcatIntegrationProvider_ManualRemoteStep4Msg=Add the following lines at the beginning of <code>{0}_nbprofiler{1}</code>, just after the help text:<br><code>{2}</code><br>For setting <code>CATALINA_OPTS</code> the {3}.",
    "TomcatIntegrationProvider_ManualRemoteStep5Msg=Start the server using <code>{0}_nbprofiler{1} run</code><br>If you use <code>startup{1}</code> to start Tomcat, modify it to launch <code>{0}_nbprofiler{1}</code>",
    "TomcatIntegrationProvider_ManualRemoteStep6Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "TomcatIntegrationProvider_ManualDirectDynamicStep1Msg=Create a copy of <code>{0}{1}bin{1}{2}{3}</code> and rename it to <code>{2}_nbprofiler{3}</code><br><code>{0}</code> stands for the Tomcat installation directory.",
    "TomcatIntegrationProvider_ManualDirectStep2Msg=Add the following lines at the beginning of <code>{0}_nbprofiler{1}</code>, just after the help text:<br><code>{2}</code>",
    "TomcatIntegrationProvider_ManualDirectDynamicStep3Msg=Start the server using <code>{0}_nbprofiler{1} run</code><br>If you use <code>startup{1}</code> to start Tomcat, modify it to launch <code>{0}_nbprofiler{1}</code>",
    "TomcatIntegrationProvider_ManualDirectStep4Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "TomcatIntegrationProvider_ManualDynamicStep2Msg=Add the following line at the beginning of <code>{0}_nbprofiler{1}</code>, just after the help text:<br><code>{2}</code>",
    "TomcatIntegrationProvider_ManualDynamicStep4Msg=When the server is running click Attach to select the server process to attach to.",
    "TomcatIntegrationProvider_IntegrReviewStep1Msg=Original file <code>{0}</code> will be copied to <code>{1}</code>",
    "TomcatIntegrationProvider_IntegrReviewStep2Msg=The following lines will be added to the new file:<br><code>{0}</code>",
    "TomcatIntegrationProvider_IntegrReviewStep1WinExeMsg=A new file <code>{0}</code> will be created",
    "TomcatIntegrationProvider_AdditionalStepsStep1DirectMsg=Use \"<code>{0} run\"</code> command to start Tomcat. Tomcat JVM will start, but will not proceed with server execution until the profiler is connected.",
    "TomcatIntegrationProvider_AdditionalStepsStep1DynamicMsg=Use \"<code>{0} run\"</code> command to start Tomcat.",
    "TomcatIntegrationProvider_AdditionalStepsStep2Msg=After this wizard finishes, choose a profiling task and click Attach. For profiling CPU, you should set a meaningful instrumentation filter and/or profile only Part of Application to decrease profiling overhead.",
    "TomcatIntegrationProvider_AdditionalStepsStep3DirectMsg=The profiler connects to Tomcat JVM and the server will start in profiling mode.",
    "TomcatIntegrationProvider_AdditionalStepsStep3DynamicPidMsg=When want to connect the profiler to Tomcat, select the correct server process in the \"Select Process\" dialog and click OK.",
    "TomcatIntegrationProvider_AdditionalStepsAutoStartMsg=If you check the \"Automatically start the server\" checkbox, Tomcat will be started automatically after this wizard finishes.",
    "TomcatIntegrationProvider_DynamicWarningMessage=Make sure your IDE is using {0}."
})
public abstract class AbstractTomcatIntegrationProvider extends AbstractScriptIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String INSERTION_POINT_NOWIN_0_STRING = "#!/bin/sh"; // NOI18N
    private static final String INSERTION_POINT_WIN_1_STRING = "rem Guess CATALINA_HOME"; // NOI18N
    private static final String INSERTION_POINT_NOWIN_1_STRING = "# OS specific support."; // NOI18N
    private static final String INSERTION_POINT_2_STRING = "Get remaining unshifted"; // NOI18N
    private static final String CATALINA_MOD_EXT_STRING = "_nbprofiler"; // NOI18N
    private static final String CATALINA_HOME_VAR_STRING = "CATALINA_HOME"; // NOI18N
    private static final String CATALINA_BASE_VAR_STRING = "CATALINA_BASE"; // NOI18N
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private SettingsPersistor persistor;
    private String catalinaBase = ""; // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of AbstractTomcatIntegrationProvider
     */
    public AbstractTomcatIntegrationProvider() {
        super();
        this.attachedWizard = new SimpleWizardStep(Bundle.AttachWizard_LocateRequiredFilesString(),
                new TomcatIntegrationPanel()); // NOI18N
        this.persistor = new IDESettingsPersistor() {

            protected String getSettingsFileName() {
                return "TomcatIntegrationProvider.properties"; // NOI18N
            }

            protected void parsePersistableSettings(Properties settings) {
                final String javaPlatform = settings.getProperty("TomcatIntegrationProvider_" + getMagicNumber()
                        + "_JavaPlatform", ""); // NOI18N

                if ((javaPlatform != null) && (javaPlatform.length() > 0)) {
                    setTargetJava(javaPlatform);
                } else {
                    setTargetJava(TargetPlatformEnum.JDK5.toString());
                }

                setInstallationPath(settings.getProperty("TomcatIntegrationProvider_" + getMagicNumber() + "_InstallDir", "")); // NOI18N
                setCatalinaBase(settings.getProperty("TomcatIntegrationProvider_" + getMagicNumber() + "_BaseDir", "")); // NOI18N

                if ((getInstallationPath() == null) || (getInstallationPath().length() == 0)) {
                    setInstallationPath(getDefaultInstallationPath());
                }

                if ((getCatalinaBase() == null) || (getCatalinaBase().length() == 0)) {
                    setCatalinaBase(getDefaultCatalinaBase());
                }
            }

            protected Properties preparePersistableSettings() {
                Properties settings = new Properties();
                settings.setProperty("TomcatIntegrationProvider_" + getMagicNumber() + "_JavaPlatform", getTargetJava()); // NOI18N
                settings.setProperty("TomcatIntegrationProvider_" + getMagicNumber() + "_InstallDir", getInstallationPath()); // NOI18N
                settings.setProperty("TomcatIntegrationProvider_" + getMagicNumber() + "_BaseDir", getCatalinaBase()); // NOI18N

                return settings;
            }
        };
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public IntegrationProvider.IntegrationHints getAfterInstallationHints(AttachSettings attachSettings, boolean automation) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();
        String targetOS = attachSettings.getHostOS();

        // Step 1
        if (attachSettings.isDirect()) {
            hints.addStep(Bundle.TomcatIntegrationProvider_AdditionalStepsStep1DirectMsg(getModifiedScriptPath(targetOS, true)));
        } else {
            hints.addStep(Bundle.TomcatIntegrationProvider_AdditionalStepsStep1DynamicMsg(getModifiedScriptPath(targetOS, true))); // NOI18N
        }

        // Step 2
        hints.addStep(Bundle.TomcatIntegrationProvider_AdditionalStepsStep2Msg());

        // Step 3
        if (attachSettings.isDirect()) {
            hints.addStep(Bundle.TomcatIntegrationProvider_AdditionalStepsStep3DirectMsg());
        } else {
            hints.addStep(Bundle.TomcatIntegrationProvider_AdditionalStepsStep3DynamicPidMsg());
            hints.addWarning(Bundle.TomcatIntegrationProvider_DynamicWarningMessage(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())));
        }

        // automatic server startup note
        hints.addHint(Bundle.TomcatIntegrationProvider_AdditionalStepsAutoStartMsg());

        return hints;
    }

    // </editor-fold>
    public void setCatalinaBase(final String path) {
        this.catalinaBase = path;
    }

    public String getCatalinaBase() {
        return this.catalinaBase;
    }

    // <editor-fold defaultstate="collapsed" desc="WizardIntegrationProvider implementation">
    public IntegrationProvider.IntegrationHints getIntegrationReview(AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();
        String targetOS = attachSettings.getHostOS();

        // Step 1
        hints.addStep(Bundle.TomcatIntegrationProvider_IntegrReviewStep1Msg(
                        getScriptPath(targetOS, true), 
                        getModifiedScriptPath(targetOS, true)));

        // Step 2
        hints.addStep(Bundle.TomcatIntegrationProvider_IntegrReviewStep2Msg(
                    IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", // NOI18N
                        this.getTargetJavaHome()) // NOI18N
                        + "<br>" // NOI18N
                        + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_HOME", // NOI18N
                        this.getInstallationPath()) // NOI18N
                        + "<br>" // NOI18N
                        + (((this.catalinaBase != null) && (this.catalinaBase.length() > 0))
                        ? (IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_BASE", // NOI18N
                        this.catalinaBase) // NOI18N
                        + "<br>") : "") // NOI18N
                        + (attachSettings.isDirect()
                        ? (IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS", // NOI18N
                        IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS,
                        getTargetJava(),
                        false,
                        attachSettings.getPort()))
                        + "<br>") : ""))); // NOI18N

        addLinkWarning(hints, "CATALINA_OPTS", attachSettings);
        
        return hints;
    }

    public IntegrationProvider.IntegrationHints getModificationHints(AttachSettings attachSettings) {
        String targetOS = attachSettings.getHostOS();

        IntegrationHints h;
        // Remote attach instructions
        if (attachSettings.isRemote()) {
            h = getManualRemoteIntegrationStepsInstructions(targetOS, attachSettings);
        } // Local direct attach
        else if (attachSettings.isDirect()) {
            h = getManualLocalDirectIntegrationStepsInstructions(targetOS, attachSettings);
        } // Local dynamic attach
        else {
            h = getManualLocalDynamicIntegrationStepsInstructions(targetOS, attachSettings);
        }
        
        addLinkWarning(h, "CATALINA_OPTS", attachSettings);
        
        return h;
    }

    public SettingsPersistor getSettingsPersistor() {
        return this.persistor;
    }

    public ValidationResult validateCatalinaBase(final String path) {
        if ((path == null) || (path.length() == 0)) {
            return new ValidationResult(true);
        }

        if (!new File(path).exists()) {
            return new ValidationResult(false, Bundle.TomcatIntegrationProvider_PathNotExistsMsg());
        } else if (!validateCatalinaBasePath(path)) {
            return new ValidationResult(false, Bundle.TomcatIntegrationProvider_InvalidCatalinaBaseMsg());
        }

        return new ValidationResult(true);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Validation of filepaths">
    public ValidationResult validateInstallation(final String targetOS, final String path) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        String ext = IntegrationUtils.getBatchExtensionString(targetOS);

        if (!new File(path).exists()) {
            return new ValidationResult(false, Bundle.TomcatIntegrationProvider_PathNotExistsMsg());
        }

        if (!new File(path + separator + "bin" + separator + getCatalinaScriptName() + ext).exists()) { // NOI18N

            return new ValidationResult(false, Bundle.TomcatIntegrationProvider_InvalidCatalinaHomeMsg());
        }

        return new ValidationResult(true);
    }

    protected boolean isBackupRequired() {
        return false;
    }

    protected abstract String getCatalinaScriptName();

    // <editor-fold defaultstate="collapsed" desc="Script modification procedures">
    protected ScriptHeaderModifier getHeaderModifier(final String targetOS) {
        return new TextScriptHeaderModifier(IntegrationUtils.getSilentScriptCommentSign(targetOS));
    }

    protected abstract int getMagicNumber();

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
            AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();
        // Step 1
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDirectDynamicStep1Msg(
                        IntegrationUtils.getEnvVariableReference("CATALINA_HOME", targetOS), // NOI18N
                        IntegrationUtils.getDirectorySeparator(targetOS), 
                        "catalina", // NOI18N
                        IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 2
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDirectStep2Msg(
                        getCatalinaScriptName(), 
                        IntegrationUtils.getBatchExtensionString(targetOS),
                        (IntegrationUtils.getAssignEnvVariableValueString(
                            targetOS, 
                            "JAVA_HOME",  // NOI18N
                            Bundle.TomcatIntegrationProvider_PathToJvmDirText(
                                IntegrationUtils.getJavaPlatformName(getTargetJava()))))
                            + "<br>"  // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS",  // NOI18N
                        IntegrationUtils.getProfilerAgentCommandLineArgs(
                            targetOS,
                            getTargetJava(),
                            attachSettings.isRemote(),
                            attachSettings.getPort()))));

        // Step 3
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDirectDynamicStep3Msg(
                        getCatalinaScriptName(), 
                        IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 4
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDirectStep4Msg());

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        return hints;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
            AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();
        // Step 1
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDirectDynamicStep1Msg(
                        IntegrationUtils.getEnvVariableReference("CATALINA_HOME", targetOS), // NOI18N
                        IntegrationUtils.getDirectorySeparator(targetOS), getCatalinaScriptName(),
                        IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 2
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDynamicStep2Msg(
                        getCatalinaScriptName(), 
                        IntegrationUtils.getBatchExtensionString(targetOS),
                        IntegrationUtils.getAssignEnvVariableValueString(
                            targetOS, 
                            "JAVA_HOME",  // NOI18N
                            Bundle.TomcatIntegrationProvider_PathToJvmDirText(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())))));

        // Step 3
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDirectDynamicStep3Msg(
                        getCatalinaScriptName(), 
                        IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 4
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualDynamicStep4Msg());

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        hints.addWarning(Bundle.TomcatIntegrationProvider_DynamicWarningMessage(
                    IntegrationUtils.getJavaPlatformName(getTargetJava())));

        return hints;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Manual integration steps generation methods">
    protected IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
            AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Step 1
        hints.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        hints.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualRemoteStep3Msg(
                        IntegrationUtils.getEnvVariableReference("REMOTE_CATALINA_HOME", targetOS), // NOI18N
                        IntegrationUtils.getDirectorySeparator(targetOS), getCatalinaScriptName(),
                        IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 4
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualRemoteStep4Msg(
                        getCatalinaScriptName(), 
                        IntegrationUtils.getBatchExtensionString(targetOS),
                        (IntegrationUtils.getAssignEnvVariableValueString(
                            targetOS, 
                            "JAVA_HOME", // NOI18N
                            Bundle.TomcatIntegrationProvider_PathToJvmDirText(
                                IntegrationUtils.getJavaPlatformName(getTargetJava())
                            )))
                            + "<br>" // NOI18N
                            + IntegrationUtils.getAssignEnvVariableValueString(
                                targetOS, 
                                "CATALINA_OPTS", // NOI18N
                                IntegrationUtils.getProfilerAgentCommandLineArgs(
                                    targetOS,
                                    getTargetJava(),
                                    attachSettings.isRemote(),
                                    attachSettings.getPort(),
                                    false)),
                                    REMOTE_ABSOLUTE_PATH_HINT)); // NOI18N

        // Step 5
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualRemoteStep5Msg(
                        getCatalinaScriptName(), 
                        IntegrationUtils.getBatchExtensionString(targetOS)));

        // Step 6
        hints.addStep(Bundle.TomcatIntegrationProvider_ManualRemoteStep6Msg());

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        return hints;
    }

    protected String getModifiedScriptPath(final String targetOS, final boolean useQuotas) {
        return getCatalinaScriptPath(targetOS, CATALINA_MOD_EXT_STRING, useQuotas);
    }

    protected String getScriptPath(final String targetOS, final boolean useQuotas) {
        return getCatalinaScriptPath(targetOS, "", useQuotas); // NOI18N
    }

    // </editor-fold>
    protected String getWinConsoleString() {
        return Bundle.TomcatIntegrationProvider_ProfiledTomcatConsoleString();
    }

    protected void generateCommands(String targetOS, Collection commandsArray) {
        commandsArray.add(getModifiedScriptPath(targetOS, false));
        commandsArray.add("run"); // NOI18N

        if (!IntegrationUtils.isWindowsPlatform(targetOS) && !IntegrationUtils.PLATFORM_MAC_OS.equals(targetOS)) {
            // Workaround for Issue 60398 (Attach to automatically started integrated Tomcat using PID doesn't work)
            // Tomcat likely gets Ctrl+C signal when attaching using PID and correctly shuts down without any error message
            // The ">&1" somehow filters out the signal to shut down
            // TODO: check on UNIXes (currently checked on linux only)
            commandsArray.add(">&1"); // NOI18N
        }
    }

    protected void modifyScriptFileForDirectAttach(final String targetOS, final int commPort, final boolean isReplaceFile,
            StringBuffer buffer) {
        String lineBreak = IntegrationUtils.getLineBreak(targetOS);

        //    // init insertion points
        //    int insertionPoint0;
        //    
        //    if (IntegrationUtils.isWindowsPlatform(targetOS)) insertionPoint0 = 0;
        //    else insertionPoint0 = buffer.indexOf(INSERTION_POINT_NOWIN_0_STRING) + INSERTION_POINT_NOWIN_0_STRING.length() + 1;
        int insertionPoint1;

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            insertionPoint1 = buffer.indexOf(INSERTION_POINT_WIN_1_STRING);
        } else {
            insertionPoint1 = buffer.indexOf(INSERTION_POINT_NOWIN_1_STRING);
        }

        int insertionPoint2 = buffer.indexOf(IntegrationUtils.getScriptCommentSign(targetOS) + " " + INSERTION_POINT_2_STRING); // NOI18N

        // create new lines
        //    String header = (isReplaceFile ? IntegrationUtils.getProfilerModifiedReplaceFileHeader(targetOS) : IntegrationUtils.getProfilerModifiedFileHeader(targetOS)) + lineBreak;
        String exportJavaHome = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", this.getTargetJavaHome())
                + lineBreak; // NOI18N
        String exportCatalinaHome = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_HOME",
                this.getInstallationPath()) + lineBreak; // NOI18N
        String exportCatalinaBase = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_BASE", this.catalinaBase)
                + lineBreak; // NOI18N
        String exportNativeLibraries = IntegrationUtils.getAddProfilerLibrariesToPathString(targetOS, this.getTargetJava(),
                false, false) + lineBreak;
        String exportCatalinaOpts = getJavaOptions(targetOS, this.getTargetJava(), commPort, ' '); // NOI18N

        // init counter for insertionPoints offset
        int currentOffset = 0;

        //    // write header
        //    buffer.insert(insertionPoint0 + currentOffset, header);
        //    currentOffset += header.length();

        // write JAVA_HOME
        buffer.insert(insertionPoint1 + currentOffset, exportJavaHome);
        currentOffset += exportJavaHome.length();

        // write CATALINA_HOME
        buffer.insert(insertionPoint1 + currentOffset, exportCatalinaHome);
        currentOffset += exportCatalinaHome.length();

        // write CATALINA_BASE
        if ((this.catalinaBase != null) && !"".equals(this.catalinaBase.trim())) { // NOI18N
            buffer.insert(insertionPoint1 + currentOffset, exportCatalinaBase);
            currentOffset += exportCatalinaBase.length();
        }

        // write CATALINA_OPTS to appropriate place
        int catalinaOptsInsertionPoint = (IntegrationUtils.isWindowsPlatform(targetOS) ? insertionPoint2 : insertionPoint1);
        buffer.insert(catalinaOptsInsertionPoint + currentOffset, exportCatalinaOpts);

        //    currentOffset += exportCatalinaOpts.length();
    }

    protected void modifyScriptFileForDynamicAttach(final String targetOS, final int commPort, final boolean isReplaceFile,
            StringBuffer buffer) {
        String lineBreak = IntegrationUtils.getLineBreak(targetOS);

        // init insertion points
        int insertionPoint0;

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            insertionPoint0 = 0;
        } else {
            insertionPoint0 = buffer.indexOf(INSERTION_POINT_NOWIN_0_STRING) + INSERTION_POINT_NOWIN_0_STRING.length() + 1;
        }

        int insertionPoint1;

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            insertionPoint1 = buffer.indexOf(INSERTION_POINT_WIN_1_STRING);
        } else {
            insertionPoint1 = buffer.indexOf(INSERTION_POINT_NOWIN_1_STRING);
        }

        // create new lines
        String header = IntegrationUtils.getProfilerModifiedFileHeader(targetOS) + lineBreak;
        String exportJavaHome = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME", this.getTargetJavaHome())
                + lineBreak; // NOI18N
        String exportCatalinaHome = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_HOME",
                this.getInstallationPath()) + lineBreak; // NOI18N
        String exportCatalinaBase = IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_BASE", this.catalinaBase)
                + lineBreak; // NOI18N

        // init counter for insertionPoints offset
        int currentOffset = 0;

        // write header
        buffer.insert(insertionPoint0 + currentOffset, header);
        currentOffset += header.length();

        // write JAVA_HOME
        buffer.insert(insertionPoint1 + currentOffset, exportJavaHome);
        currentOffset += exportJavaHome.length();

        // write CATALINA_HOME
        buffer.insert(insertionPoint1 + currentOffset, exportCatalinaHome);
        currentOffset += exportCatalinaHome.length();

        // write CATALINA_BASE
        if ((this.catalinaBase != null) && !"".equals(this.catalinaBase.trim())) { // NOI18N
            buffer.insert(insertionPoint1 + currentOffset, exportCatalinaBase);

            //      currentOffset += exportCatalinaBase.length();
        }
    }

    private String getCatalinaScriptPath(final String targetOS, final String scriptPostfix, boolean useQuotas) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        String ext = IntegrationUtils.getBatchExtensionString(targetOS);
        String quotas = ""; // NOI18N

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            quotas = "\""; // NOI18N
        }

        StringBuilder path = new StringBuilder();
        path.append(this.getInstallationPath());

        if (!this.getInstallationPath().endsWith(separator)) {
            path.append(separator);
        }

        path.append("bin").append(separator).append(getCatalinaScriptName()).append(scriptPostfix).append(ext); // NOI18N

        if (useQuotas && (path.indexOf(" ") > -1)) { // NOI18N
            path.insert(0, quotas);
            path.append(quotas);
        }

        return path.toString();
    }

    private String getDefaultCatalinaBase() {
        String catalinaBase = ""; // NOI18N

        try {
            String catalinaBaseEnv = System.getenv(CATALINA_BASE_VAR_STRING); // java.lang.Error: getenv no longer supported exception is thrown on 1.4.2

            if ((catalinaBaseEnv != null) && (catalinaBaseEnv.length() > 1)) {
                File catalinaBaseDir = new File(catalinaBaseEnv);

                if (catalinaBaseDir.exists() && catalinaBaseDir.isDirectory()) {
                    catalinaBase = catalinaBaseEnv;
                }
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) { /* IGNORE */

        }

        return catalinaBase;
    }

    private String getDefaultInstallationPath() {
        String catalinaHome = ""; // NOI18N

        try {
            String catalinaHomeEnv = System.getenv(CATALINA_HOME_VAR_STRING); // java.lang.Error: getenv no longer supported exception is thrown on 1.4.2

            if ((catalinaHomeEnv != null) && (catalinaHomeEnv.length() > 1)) {
                File catalinaHomeDir = new File(catalinaHomeEnv);

                if (catalinaHomeDir.exists() && catalinaHomeDir.isDirectory()) {
                    catalinaHome = catalinaHomeEnv;
                }
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) { /* IGNORE */

        }

        return catalinaHome;
    }

    private String getJavaOptions(final String targetOS, final String targetJVM, final int commPort, final char delimiter) {
        StringBuilder javaOpts = new StringBuilder();
        boolean nonEmptyOpts = false;

        // java opts for debugging
        // debugging property for agent side - wire I/O
        if (System.getProperty("org.netbeans.lib.profiler.wireprotocol.WireIO.agent") != null) { // NOI18N
            nonEmptyOpts = true;
            javaOpts.append("-Dorg.netbeans.lib.profiler.wireprotocol.WireIO=true"); // NOI18N
        }

        // debugging property for agent side - Class loader hook
        if (System.getProperty("org.netbeans.lib.profiler.server.ProfilerInterface.classLoadHook") != null) { // NOI18N

            if (nonEmptyOpts) {
                javaOpts.append(delimiter);
            }

            nonEmptyOpts = true;
            javaOpts.append("-Dorg.netbeans.lib.profiler.server.ProfilerInterface.classLoadHook=true"); // NOI18N
        }

        String profilerOpts = IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS, targetJVM, false, commPort);
        profilerOpts = profilerOpts.replace("\\s-", delimiter + "-");

        if (nonEmptyOpts) {
            javaOpts.append(delimiter);
        }

        javaOpts.append(profilerOpts);

        return IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS", javaOpts.toString())
                + IntegrationUtils.getLineBreak(targetOS); // NOI18N
    }

    private boolean validateCatalinaBasePath(final String path) {
        final String dirSeparator = System.getProperty("file.separator"); // NOI18N

        return new File(path + dirSeparator + "conf" + dirSeparator + "server.xml").exists(); // NOI18N
    }
}
