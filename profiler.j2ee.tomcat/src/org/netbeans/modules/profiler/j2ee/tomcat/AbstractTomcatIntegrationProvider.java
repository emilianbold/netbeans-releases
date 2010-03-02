/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
public abstract class AbstractTomcatIntegrationProvider extends AbstractScriptIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.profiler.j2ee.tomcat.Bundle"); // NOI18N
    private static final String PROFILED_TOMCAT_CONSOLE_STRING = messages.getString("TomcatIntegrationProvider_ProfiledTomcatConsoleString"); // NOI18N
    private static final String MANUAL_REMOTE_STEP3_MSG = messages.getString("TomcatIntegrationProvider_ManualRemoteStep3Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP4_MSG = messages.getString("TomcatIntegrationProvider_ManualRemoteStep4Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP5_MSG = messages.getString("TomcatIntegrationProvider_ManualRemoteStep5Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP6_MSG = messages.getString("TomcatIntegrationProvider_ManualRemoteStep6Msg"); // NOI18N
    private static final String PATH_TO_JVM_DIR_TEXT = messages.getString("TomcatIntegrationProvider_PathToJvmDirText"); // NOI18N
    private static final String MANUAL_DIRECT_DYNAMIC_STEP1_MSG = messages.getString("TomcatIntegrationProvider_ManualDirectDynamicStep1Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP2_MSG = messages.getString("TomcatIntegrationProvider_ManualDirectStep2Msg"); // NOI18N
    private static final String MANUAL_DIRECT_DYNAMIC_STEP3_MSG = messages.getString("TomcatIntegrationProvider_ManualDirectDynamicStep3Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP4_MSG = messages.getString("TomcatIntegrationProvider_ManualDirectStep4Msg"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP2_MSG = messages.getString("TomcatIntegrationProvider_ManualDynamicStep2Msg"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP4_MSG = messages.getString("TomcatIntegrationProvider_ManualDynamicStep4Msg"); // NOI18N
    private static final String DYNAMIC_WARNING_MESSAGE = messages.getString("TomcatIntegrationProvider_DynamicWarningMessage"); // NOI18N  
    private static final String ADDITIONAL_STEPS_STEP1_DIRECT_MSG = messages.getString("TomcatIntegrationProvider_AdditionalStepsStep1DirectMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP1_DYNAMIC_MSG = messages.getString("TomcatIntegrationProvider_AdditionalStepsStep1DynamicMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP2_MSG = messages.getString("TomcatIntegrationProvider_AdditionalStepsStep2Msg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP3_DIRECT_MSG = messages.getString("TomcatIntegrationProvider_AdditionalStepsStep3DirectMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP3_DYNAMIC_PID_MSG = messages.getString("TomcatIntegrationProvider_AdditionalStepsStep3DynamicPidMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_AUTO_START_MSG = messages.getString("TomcatIntegrationProvider_AdditionalStepsAutoStartMsg"); // NOI18N
    private static final String INTEGR_REVIEW_STEP1_MSG = messages.getString("TomcatIntegrationProvider_IntegrReviewStep1Msg"); // NOI18N
    protected static final String INTEGR_REVIEW_STEP2_MSG = messages.getString("TomcatIntegrationProvider_IntegrReviewStep2Msg"); // NOI18N
    private static final String VALIDATION_DIRNOEXIST_MSG = messages.getString("TomcatIntegrationProvider_PathNotExistsMsg"); // NOI18N
    private static final String VALIDATION_HOME_INVALID_MSG = messages.getString("TomcatIntegrationProvider_InvalidCatalinaHomeMsg"); // NOI18N
    private static final String VALIDATION_BASE_INVALID_MSG = messages.getString("TomcatIntegrationProvider_InvalidCatalinaBaseMsg"); // NOI18N
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
        this.attachedWizard = new SimpleWizardStep(NbBundle.getMessage(AbstractTomcatIntegrationProvider.class,
                                                                       "AttachWizard_LocateRequiredFilesString"),
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
            hints.addStep(MessageFormat.format(ADDITIONAL_STEPS_STEP1_DIRECT_MSG,
                                               new Object[] { getModifiedScriptPath(targetOS, true) }));
        } else {
            hints.addStep(MessageFormat.format(ADDITIONAL_STEPS_STEP1_DYNAMIC_MSG,
                                               new Object[] { getModifiedScriptPath(targetOS, true), "" })); // NOI18N
        }

        // Step 2
        hints.addStep(ADDITIONAL_STEPS_STEP2_MSG);

        // Step 3
        if (attachSettings.isDirect()) {
            hints.addStep(ADDITIONAL_STEPS_STEP3_DIRECT_MSG);
        } else {
            hints.addStep(ADDITIONAL_STEPS_STEP3_DYNAMIC_PID_MSG);
            hints.addWarning(MessageFormat.format(DYNAMIC_WARNING_MESSAGE,
                                                  new Object[] {
                                                      IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                      IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS, getTargetJava(),
                                                                                                       attachSettings.isRemote(),
                                                                                                       attachSettings.getPort())
                                                  }));
        }

        // automatic server startup note
        hints.addHint(ADDITIONAL_STEPS_AUTO_START_MSG);

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
        hints.addStep(MessageFormat.format(INTEGR_REVIEW_STEP1_MSG,
                                           new Object[] {
                                               getScriptPath(targetOS, true), getModifiedScriptPath(targetOS, true), targetOS
                                           }));

        // Step 2
        hints.addStep(MessageFormat.format(INTEGR_REVIEW_STEP2_MSG,
                                           new Object[] {
                                               IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                                                this.getTargetJavaHome()) // NOI18N
                                               + "<br>"
                                               + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_HOME",
                                                                                                  this.getInstallationPath()) // NOI18N
                                               + "<br>"
                                               + (((this.catalinaBase != null) && (this.catalinaBase.length() > 0))
                                                  ? (IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_BASE",
                                                                                                      this.catalinaBase) // NOI18N
                                                  + "<br>") : "")
                                               + (attachSettings.isDirect()
                                                  ? (IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS",
                                                                                                      IntegrationUtils
                                                                                                                                                                                                                                       .getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                                                                                                                                                                        getTargetJava(),
                                                                                                                                                                                                                                                                        false,
                                                                                                                                                                                                                                                                        attachSettings
                                                                                                                                                                                                                                                                        .getPort())) // NOI18N
                                                  + "<br>") : "")
                                           })); // NOI18N

        return hints;
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

    public SettingsPersistor getSettingsPersistor() {
        return this.persistor;
    }

    public ValidationResult validateCatalinaBase(final String path) {
        if ((path == null) || (path.length() == 0)) {
            return new ValidationResult(true);
        }

        if (!new File(path).exists()) {
            return new ValidationResult(false, VALIDATION_DIRNOEXIST_MSG);
        } else if (!validateCatalinaBasePath(path)) {
            return new ValidationResult(false, VALIDATION_BASE_INVALID_MSG);
        }

        return new ValidationResult(true);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Validation of filepaths">
    public ValidationResult validateInstallation(final String targetOS, final String path) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        String ext = IntegrationUtils.getBatchExtensionString(targetOS);

        if (!new File(path).exists()) {
            return new ValidationResult(false, VALIDATION_DIRNOEXIST_MSG);
        }

        if (!new File(path + separator + "bin" + separator + getCatalinaScriptName() + ext).exists()) { // NOI18N

            return new ValidationResult(false, VALIDATION_HOME_INVALID_MSG);
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
        hints.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                           new Object[] {
                                               IntegrationUtils.getEnvVariableReference("CATALINA_HOME", targetOS),
                                               IntegrationUtils.getDirectorySeparator(targetOS), "catalina",
                                               IntegrationUtils.getBatchExtensionString(targetOS)
                                           })); // NOI18N

        // Step 2
        hints.addStep(MessageFormat.format(MANUAL_DIRECT_STEP2_MSG,
                                           new Object[] {
                                               getCatalinaScriptName(), IntegrationUtils.getBatchExtensionString(targetOS),
                                               (IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                                                 MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                                                                      new Object[] {
                                                                                                                          IntegrationUtils
                                                                                                                          .getJavaPlatformName(getTargetJava())
                                                                                                                      })))
                                               + "<br>"
                                               + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS",
                                                                                                  IntegrationUtils
                                                                                                                                                                                                                                                                                                                                             .getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                                                                                                                                                                                                                                                                              getTargetJava(),
                                                                                                                                                                                                                                                                                                                                                                              attachSettings
                                                                                                                                                                                                                                                                                                                                                                              .isRemote(),
                                                                                                                                                                                                                                                                                                                                                                              attachSettings
                                                                                                                                                                                                                                                                                                                                                                              .getPort()))
                                           })); // NOI18N

        // Step 3
        hints.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP3_MSG,
                                           new Object[] {
                                               getCatalinaScriptName(), IntegrationUtils.getBatchExtensionString(targetOS)
                                           }));

        // Step 4
        hints.addStep(MANUAL_DIRECT_STEP4_MSG);

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

        return hints;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                     AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();
        // Step 1
        hints.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                           new Object[] {
                                               IntegrationUtils.getEnvVariableReference("CATALINA_HOME", targetOS),
                                               IntegrationUtils.getDirectorySeparator(targetOS), getCatalinaScriptName(),
                                               IntegrationUtils.getBatchExtensionString(targetOS)
                                           })); // NOI18N

        // Step 2
        hints.addStep(MessageFormat.format(MANUAL_DYNAMIC_STEP2_MSG,
                                           new Object[] {
                                               getCatalinaScriptName(), IntegrationUtils.getBatchExtensionString(targetOS),
                                               IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                                                MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                                                                     new Object[] {
                                                                                                                         IntegrationUtils
                                                                                                                         .getJavaPlatformName(getTargetJava())
                                                                                                                     }))
                                           })); // NOI18N

        // Step 3
        hints.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP3_MSG,
                                           new Object[] {
                                               getCatalinaScriptName(), IntegrationUtils.getBatchExtensionString(targetOS)
                                           }));

        // Step 4
        hints.addStep(MANUAL_DYNAMIC_STEP4_MSG);

        // Note about decreasing CPU profiling overhead
        hints.addHint(REDUCE_OVERHEAD_MSG);

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

    // <editor-fold defaultstate="collapsed" desc="Manual integration steps generation methods">
    protected IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                               AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints hints = new IntegrationProvider.IntegrationHints();

        // Step 1
        hints.addStep(getManualRemoteStep1(targetOS));

        // Step 2
        hints.addStep(getManualRemoteStep2(targetOS));

        // Step 3
        hints.addStep(MessageFormat.format(MANUAL_REMOTE_STEP3_MSG,
                                           new Object[] {
                                               IntegrationUtils.getEnvVariableReference("REMOTE_CATALINA_HOME", targetOS),
                                               IntegrationUtils.getDirectorySeparator(targetOS), getCatalinaScriptName(),
                                               IntegrationUtils.getBatchExtensionString(targetOS)
                                           })); // NOI18N

        // Step 4
        hints.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_MSG,
                                           new Object[] {
                                               getCatalinaScriptName(), IntegrationUtils.getBatchExtensionString(targetOS),
                                               (IntegrationUtils.getAssignEnvVariableValueString(targetOS, "JAVA_HOME",
                                                                                                 MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                                                                      new Object[] {
                                                                                                                          IntegrationUtils
                                                                                                                          .getJavaPlatformName(getTargetJava())
                                                                                                                      })))
                                               + "<br>"
                                               + IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS",
                                                                                                  IntegrationUtils
                                                                                                                                                                                                                                                                                                                                                                                                                                                    .getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     getTargetJava(),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     attachSettings
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     .isRemote(),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     attachSettings
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     .getPort())),
                                               REMOTE_ABSOLUTE_PATH_HINT
                                           })); // NOI18N

        // Step 5
        hints.addStep(MessageFormat.format(MANUAL_REMOTE_STEP5_MSG,
                                           new Object[] {
                                               getCatalinaScriptName(), IntegrationUtils.getBatchExtensionString(targetOS)
                                           }));

        // Step 6
        hints.addStep(MANUAL_REMOTE_STEP6_MSG);

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
        return PROFILED_TOMCAT_CONSOLE_STRING;
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

        StringBuffer path = new StringBuffer();
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
        StringBuffer javaOpts = new StringBuffer();
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
        profilerOpts.replace("\\s-", delimiter + "-");

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
