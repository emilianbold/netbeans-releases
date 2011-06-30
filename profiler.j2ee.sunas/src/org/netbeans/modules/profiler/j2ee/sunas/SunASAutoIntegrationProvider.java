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

import org.netbeans.lib.profiler.common.AttachSettings;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.IDESettingsPersistor;
import org.netbeans.modules.profiler.attach.providers.SettingsPersistor;
import org.netbeans.modules.profiler.attach.providers.ValidationResult;
import org.netbeans.modules.profiler.attach.providers.scripted.AbstractScriptIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.scripted.ProfilerScriptModifier;
import org.netbeans.modules.profiler.attach.providers.scripted.ScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.providers.scripted.ScriptModificationException;
import org.netbeans.modules.profiler.attach.providers.scripted.TextScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.providers.scripted.XmlScriptHeaderModifier;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.spi.ModificationException;
import org.netbeans.modules.profiler.attach.wizard.steps.SimpleWizardStep;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
public abstract class SunASAutoIntegrationProvider extends AbstractScriptIntegrationProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="Resources">
    private static final String PATH_TO_JVM_DIR_TEXT = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_PathToJvmDirText"); // NOI18N
    private static final String MANUAL_REMOTE_STEP3_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualRemoteStep3Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP4_50_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualRemoteStep450Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP5_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualRemoteStep5Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP6_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualRemoteStep6Msg"); // NOI18N
    private static final String MANUAL_REMOTE_STEP7_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualRemoteStep7Msg"); // NOI18N
    private static final String MANUAL_DIRECT_DYNAMIC_STEP1_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualDirectDynamicStep1Msg"); // NOI18N
    private static final String MANUAL_DIRECT_DYNAMIC_STEP2_50_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualDirectDynamicStep250Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP3_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualDirectStep3Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP4_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualDirectStep4Msg"); // NOI18N
    private static final String MANUAL_DIRECT_STEP5_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualDirectStep5Msg"); // NOI18N
    private static final String MANUAL_DYNAMIC_STEP3_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_ManualDynamicStep3Msg"); // NOI18N
    private static final String INTEGR_REVIEW_STEP1_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_IntegrReviewStep1Msg"); // NOI18N
    private static final String INTEGR_REVIEW_STEP2_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_IntegrReviewStep2Msg"); // NOI18N
    private static final String INTEGR_REVIEW_STEP3_DIRECT_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_IntegrReviewStep3DirectMsg"); // NOI18N
    private static final String INTEGR_REVIEW_STEP3_DYNAMIC_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_IntegrReviewStep3DynamicMsg"); // NOI18N
    private static final String RESTORE_SETTINGS_WARNING_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_RestoreSettingsWarningMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP1_DIRECT_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_AdditionalStepsStep1DirectMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP1_DYNAMIC_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_AdditionalStepsStep1DynamicMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP2_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_AdditionalStepsStep2Msg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP3_DIRECT_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_AdditionalStepsStep3DirectMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_STEP3_DYNAMIC_PID_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_AdditionalStepsStep3DynamicPidMsg"); // NOI18N
    private static final String ADDITIONAL_STEPS_AUTO_START_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_AdditionalStepsAutoStartMsg"); // NOI18N
    private static final String ENTER_INSTALL_DIR_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_EnterInstallDirMsg"); // NOI18N
    private static final String INVALID_INSTALL_DIR_MSG = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunAS8IntegrationProvider_InvalidInstallDirMsg"); // NOI18N
    private static final String DYNAMIC_WARNING_MESSAGE = NbBundle.getMessage(SunASAutoIntegrationProvider.class, "SunASIntegrationProvider_DynamicWarningMessage"); // NOI18N  
                                                                                                                                // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final String SUNAS_INSTALL_VAR_STRING = "AS_INSTALL"; // NOI18N
    private static final String SUNAS_HOME_VAR_STRING = "AS_HOME"; // NOI18N
    private static final String SUNAS_8PE_BINDIR_NAME = "bin"; // NOI18N
    private static final String SUNAS_8PE_ASADMINSCRIPT_NAME = "asadmin"; // NOI18N
    private static final String SUNAS_8PE_CONFIGDIR_NAME = "config"; // NOI18N
    private static final String SUNAS_8PE_ASENVSCRIPT_NAME = "asenv"; // NOI18N
    private static final String SUNAS_8PE_DOMAINSDIR_NAME = "domains"; // NOI18N
    private static final String SUNAS_8PE_DOMAINCONFIGDIR_NAME = "config"; // NOI18N
    private static final String SUNAS_8PE_DOMAINSCRIPT_NAME = "domain.xml"; // NOI18N
    private static final String ASENV_INSERTION_POINT_WIN_0_STRING = "set AS_JAVA"; // NOI18N
    private static final String ASENV_INSERTION_POINT_NOWIN_0_STRING = "AS_JAVA"; // NOI18N
                                                                                  // </editor-fold>

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private SettingsPersistor persistor;
    private String appserverDomain = ""; // NOI18N
    private String scriptPublicId = ""; // NOI18N
    private String scriptSystemId = ""; // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SunASAutoIntegrationProvider() {
        super();
        this.attachedWizard = new SimpleWizardStep(NbBundle.getMessage(SunASAutoIntegrationProvider.class,
                                                                       "AttachWizard_LocateRequiredFilesString"),
                                                   new SunASIntegrationPanel()); // NOI18N
        this.persistor = new IDESettingsPersistor() {
                protected String getSettingsFileName() {
                    return "SunAS8IntegrationProvider.properties"; // NOI18N
                }

                protected void parsePersistableSettings(Properties settings) {
                    setTargetJava(settings.getProperty("SunAS8IntegrationProvider_" + getMagicNumber() + "_JavaPlatform", "")); // NOI18N
                    setInstallationPath(settings.getProperty("SunAS8IntegrationProvider_" + getMagicNumber() + "_InstallDir", "")); // NOI18N
                    setDomain(settings.getProperty("SunAS8IntegrationProvider_" + getMagicNumber() + "_DomainName", "")); // NOI18N

                    if ((getInstallationPath() == null) || (getInstallationPath().length() == 0)) {
                        setInstallationPath(getDefaultInstallationPath());
                    }
                }

                protected Properties preparePersistableSettings() {
                    Properties settings = new Properties();
                    settings.setProperty("SunAS8IntegrationProvider_" + getMagicNumber() + "_JavaPlatform", getTargetJava()); // NOI18N
                    settings.setProperty("SunAS8IntegrationProvider_" + getMagicNumber() + "_InstallDir", getInstallationPath()); // NOI18N
                    settings.setProperty("SunAS8IntegrationProvider_" + getMagicNumber() + "_DomainName", getDomain()); // NOI18N

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
            instructions.addStep(MessageFormat.format(ADDITIONAL_STEPS_STEP1_DIRECT_MSG,
                                                      new Object[] {
                                                          getAsAdminScriptFilePath(targetOS) + " start-domain --domaindir "
                                                          + getInstallationPath()
                                                          + IntegrationUtils.getDirectorySeparator(targetOS)
                                                          + SUNAS_8PE_DOMAINSDIR_NAME + " " + getDomain()
                                                      })); // NOI18N
        } else {
            instructions.addStep(MessageFormat.format(ADDITIONAL_STEPS_STEP1_DYNAMIC_MSG,
                                                      new Object[] {
                                                          getAsAdminScriptFilePath(targetOS) + " start-domain --domaindir "
                                                          + getInstallationPath()
                                                          + IntegrationUtils.getDirectorySeparator(targetOS)
                                                          + SUNAS_8PE_DOMAINSDIR_NAME + " " + getDomain(), ""
                                                      })); // NOI18N
        }

        // Step 2
        instructions.addStep(ADDITIONAL_STEPS_STEP2_MSG);

        if (attachSettings.isDirect()) {
            instructions.addStep(ADDITIONAL_STEPS_STEP3_DIRECT_MSG);
        } else {
            instructions.addStep(ADDITIONAL_STEPS_STEP3_DYNAMIC_PID_MSG);
            instructions.addWarning(MessageFormat.format(DYNAMIC_WARNING_MESSAGE,
                                                         new Object[] {
                                                             IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                             IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS,
                                                                                                              getTargetJava(),
                                                                                                              attachSettings
                                                                                                                                                                                                                      .isRemote(),
                                                                                                              attachSettings
                                                                                                                                                                                                                        .getPort())
                                                         }));
        }

        // modified files warning
        instructions.addWarning(MessageFormat.format(RESTORE_SETTINGS_WARNING_MSG,
                                                     new Object[] { getAsEnvScriptFileName(targetOS), SUNAS_8PE_DOMAINSCRIPT_NAME }));

        instructions.addHint(ADDITIONAL_STEPS_AUTO_START_MSG);

        return instructions;
    }

    public final Collection getAvailableDomains(String installPath) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        final StringBuilder path = new StringBuilder();
        final Collection availableDomains = new ArrayList();

        path.append(installPath);

        if (!installPath.endsWith(separator)) {
            path.append(separator);
        }

        path.append(getDomainsDirPath(separator));

        File domainsDir = new File(path.toString());

        // invalid domains directory
        if (domainsDir.exists()) {
            File[] domains = domainsDir.listFiles();

            // no valid domains found in domains directory
            if (!((domains == null) || (domains.length == 0))) {
                // searching for available domains with domain.xml script
                for (int i = 0; i < domains.length; i++) {
                    StringBuilder configPath = new StringBuilder(domains[i].getAbsolutePath());
                    configPath.append(separator).append(SUNAS_8PE_DOMAINCONFIGDIR_NAME).append(separator)
                              .append(SUNAS_8PE_DOMAINSCRIPT_NAME);

                    if (domains[i].isDirectory() && new File(configPath.toString()).exists()) {
                        availableDomains.add(domains[i].getName());
                    }
                }
            }
        }

        return availableDomains;
    }

    public void setDomain(String domain) {
        appserverDomain = domain;
    }

    // </editor-fold>
    public String getDomain() {
        return appserverDomain;
    }

    public String getDomainScriptDirPath(final AttachSettings attachSettings) {
        String targetOS = attachSettings.getHostOS();

        return getDomainScriptDirPath(targetOS);
    }

    public IntegrationProvider.IntegrationHints getIntegrationReview(AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        String targetOS = attachSettings.getHostOS();

        // Step 1
        instructions.addStep(MessageFormat.format(INTEGR_REVIEW_STEP1_MSG,
                                                  new Object[] {
                                                      getAsEnvScriptFilePath(targetOS), getDomainScriptFilePath(IntegrationUtils.getDirectorySeparator(targetOS)),
                                                      getAsEnvScriptFileName(targetOS), SUNAS_8PE_DOMAINSCRIPT_NAME
                                                  }));

        // Step 2
        instructions.addStep(MessageFormat.format(INTEGR_REVIEW_STEP2_MSG,
                                                  new Object[] {
                                                      getAsEnvScriptFileName(targetOS),
                                                      (IntegrationUtils.isWindowsPlatform(targetOS)
                                                       ? ASENV_INSERTION_POINT_WIN_0_STRING : ASENV_INSERTION_POINT_NOWIN_0_STRING)
                                                      + "=\"" + getTargetJavaHome() + "\""
                                                  })); // NOI18N

        // Step 3
        if (attachSettings.isDirect()) {
            instructions.addStep(MessageFormat.format(INTEGR_REVIEW_STEP3_DIRECT_MSG,
                                                      new Object[] {
                                                          SUNAS_8PE_DOMAINSCRIPT_NAME, "",
                                                          getJvmOptionsElementText(
                                                              getProfilerAgentCommandLineArgsForDomainScript(targetOS,
                                                              attachSettings.isRemote(),
                                                              attachSettings.getPort())
                                                          )
                                                      })); // NOI18N
        } else {
            instructions.addStep(MessageFormat.format(INTEGR_REVIEW_STEP3_DYNAMIC_MSG,
                                                      new Object[] { SUNAS_8PE_DOMAINSCRIPT_NAME }));
        }

        // modified files warning
        instructions.addWarning(MessageFormat.format(RESTORE_SETTINGS_WARNING_MSG,
                                                     new Object[] { getAsEnvScriptFileName(targetOS), SUNAS_8PE_DOMAINSCRIPT_NAME }));

        return instructions;
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

    public SettingsPersistor getSettingsPersistor() {
        return this.persistor;
    }

    /**
     * Overrides the modify method of the AbstractScriptIntegrationProvider in order to be able to modify Env script
     */
    public void modify(AttachSettings attachSettings) throws ModificationException {
        String targetOS = attachSettings.getHostOS();

        try {
            modifyAsEnvScriptFile(targetOS, attachSettings);
            super.modify(attachSettings);
        } catch (ScriptModificationException e) {
            throw new ModificationException(e);
        }
    }

    public ValidationResult validateInstallation(final String targetOS, final String path) {
        if ((path == null) || (path.length() == 0) || !(new File(path).exists())) {
            return new ValidationResult(false, MessageFormat.format(ENTER_INSTALL_DIR_MSG, new Object[] { this.getTitle() }));
        }

        String asenv = getAsScriptFilePath(path, getConfigDir(targetOS), getAsEnvScriptFileName(targetOS), targetOS);

        if (!(new File(asenv).exists())) {
            return new ValidationResult(false,
                                        MessageFormat.format(INVALID_INSTALL_DIR_MSG,
                                                             new Object[] { getTitle(), getAsEnvScriptFileName(targetOS) }));
        }

        return new ValidationResult(true);
    }

    protected String getConfigDir(String targetOS) {
        return SUNAS_8PE_CONFIGDIR_NAME;
    }

    protected boolean isBackupRequired() {
        return true;
    }

    protected String getDefaultScriptEncoding() {
        return "UTF-8"; // NOI18N // domain.xml must be written in UTF-8 encoding
    }

    protected ScriptHeaderModifier getHeaderModifier(final String targetOS) {
        ScriptHeaderModifier modifier = new XmlScriptHeaderModifier() {
            private static final String PROFILER_LINE = "<profiler enabled=\"true\" name=\"NetBeansProfiler\">";
            private boolean needsProfilerLine = true;

            public void lineRead(final StringBuffer line) {
                super.lineRead(line);

                if (needsProfilerLine && (line.indexOf(PROFILER_LINE) != -1)) {
                    needsProfilerLine = false;
                }
            }

            protected boolean needsWritingHeaders() {
                return super.needsWritingHeaders() || needsProfilerLine;
            }
        };

        modifier.setOptionalHeaders(new String[] { IntegrationUtils.ORIGINAL_BACKUP_LOCATION_STRING, "" }); // NOI18N

        return modifier;
    }

    protected abstract int getMagicNumber();

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                    AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                                  new Object[] {
                                                      getAsEnvScriptFileName(targetOS),
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS), this.getTitle()
                                                  })); // NOI18N

        // Step 2
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP2_50_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                      getAsEnvScriptFileName(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS)
                                                      ? (IntegrationUtils.getExportCommandString(targetOS) + " AS_JAVA="
                                                      + MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                             new Object[] {
                                                                                 IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                             }))
                                                      : ("AS_JAVA=\""
                                                      + MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                             new Object[] {
                                                                                 IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                             }) + "\""), this.getTitle()
                                                  })); // NOI18N

        // Step 3
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP3_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Step 4
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP4_MSG,
                                                  new Object[] {
                                                      "",
                                                      getJvmOptionsElementText(
                                                          getProfilerAgentCommandLineArgsForDomainScript(targetOS, attachSettings.isRemote(), attachSettings.getPort())
                                                      )
                                                  })); // NOI18N

        // Step 5
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_STEP5_MSG,
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

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                     AttachSettings attachSettings) {
        IntegrationProvider.IntegrationHints instructions = new IntegrationProvider.IntegrationHints();

        // Step 1
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP1_MSG,
                                                  new Object[] {
                                                      getAsEnvScriptFileName(targetOS),
                                                      IntegrationUtils.getEnvVariableReference("AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS), this.getTitle()
                                                  })); // NOI18N

        // Step 2
        instructions.addStep(MessageFormat.format(MANUAL_DIRECT_DYNAMIC_STEP2_50_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                      getAsEnvScriptFileName(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS)
                                                      ? (IntegrationUtils.getExportCommandString(targetOS) + " AS_JAVA="
                                                      + MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                             new Object[] {
                                                                                 IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                             }))
                                                      : ("AS_JAVA=\""
                                                      + MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                             new Object[] {
                                                                                 IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                             }) + "\""), this.getTitle()
                                                  })); // NOI18N
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

    // <editor-fold defaultstate="collapsed" desc="Manual integration">
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
                                                      getAsEnvScriptFileName(targetOS),
                                                      IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS), this.getTitle()
                                                  })); // NOI18N

        // Step 4
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP4_50_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getJavaPlatformName(getTargetJava()),
                                                      getAsEnvScriptFileName(targetOS),
                                                      IntegrationUtils.isWindowsPlatform(targetOS)
                                                      ? (IntegrationUtils.getExportCommandString(targetOS) + " AS_JAVA="
                                                      + MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                             new Object[] {
                                                                                 IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                             }))
                                                      : ("AS_JAVA=\""
                                                      + MessageFormat.format(PATH_TO_JVM_DIR_TEXT,
                                                                             new Object[] {
                                                                                 IntegrationUtils.getJavaPlatformName(getTargetJava())
                                                                             }) + "\""), this.getTitle()
                                                  })); // NOI18N

        // Step 5
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP5_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Step 6
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP6_MSG,
                                                  new Object[] {
                                                      "",
                                                      getJvmOptionsElementText(
                                                          getProfilerAgentCommandLineArgsForDomainScript(targetOS, attachSettings.isRemote(), attachSettings.getPort())
                                                      ),
                                                      IntegrationUtils.getRemoteAbsolutePathHint()
                                                  })); // NOI18N

        // Step 7
        instructions.addStep(MessageFormat.format(MANUAL_REMOTE_STEP7_MSG,
                                                  new Object[] {
                                                      IntegrationUtils.getEnvVariableReference("REMOTE_AS_INSTALL", targetOS),
                                                      IntegrationUtils.getDirectorySeparator(targetOS)
                                                  })); // NOI18N

        // Note about decreasing CPU profiling overhead
        instructions.addHint(REDUCE_OVERHEAD_MSG);

        return instructions;
    }

    protected String getModifiedScriptPath(final String targetOS, final boolean quoted) {
        return getScriptPath(targetOS, quoted);
    }

    protected String getScriptPath(final String targetOS, final boolean quoted) {
        StringBuilder path = new StringBuilder();
        path.append(getDomainScriptFilePath(IntegrationUtils.getDirectorySeparator(targetOS)));

        if (quoted) {
            path.insert(0, "\"");
            path.append("\"");
        }

        return path.toString();
    }

    protected abstract String getWinSpecificCommandLineArgs(String targetPlatform, boolean isRemote, int portNumber);

    protected void generateCommands(String targetOS, Collection commandsArray) {
        final String separator = IntegrationUtils.getDirectorySeparator(targetOS);
        final String asadminScript = getAsAdminScriptFilePath(targetOS);
        final String domainsDir = getInstallationPath() + separator + getDomainsDirPath(separator); // NOI18N
        final String domainName = getDomain();

        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            commandsArray.add("call"); // NOI18N
            commandsArray.add(asadminScript);
            commandsArray.add("start-domain"); // NOI18N
            commandsArray.add("--verbose"); // NOI18N
            commandsArray.add("--domaindir"); // NOI18N
            commandsArray.add(domainsDir);
            commandsArray.add(domainName);
        } else {
            commandsArray.add(asadminScript);
            commandsArray.add("start-domain"); // NOI18N
            commandsArray.add("--verbose"); // NOI18N
            commandsArray.add("--domaindir"); // NOI18N
            commandsArray.add(domainsDir);
            commandsArray.add(domainName);
        }
    }

    protected void modifyScriptFileForDirectAttach(final String targetOS, final int commPort, final boolean isReplaceFile,
                                                   final StringBuffer buffer) {
        Document domainScriptDocument = loadDomainScriptFile(buffer);

        if (domainScriptDocument == null) {
            return;
        }

        // Remove any previously defined profiler element(s)
        NodeList profilerElementNodeList = domainScriptDocument.getElementsByTagName("profiler"); // NOI18N

        if ((profilerElementNodeList != null) && (profilerElementNodeList.getLength() > 0)) {
            while (profilerElementNodeList.getLength() > 0) {
                profilerElementNodeList.item(0).getParentNode().removeChild(profilerElementNodeList.item(0));
            }
        }

        //    // Add a comment with Profiler modified file header if modifying fresh file
        //    if (isModified) {
        //      domainScriptDocument.insertBefore(domainScriptDocument.createComment(IntegrationUtils.ORIGINAL_BACKUP_LOCATION_STRING), domainScriptDocument.getFirstChild());
        //      domainScriptDocument.insertBefore(domainScriptDocument.createComment(IntegrationUtils.MODIFIED_FOR_PROFILER_STRING), domainScriptDocument.getFirstChild());
        //    }

        // Create "profiler" element
        Element profilerElement = domainScriptDocument.createElement("profiler"); // NOI18N
        profilerElement.setAttribute("enabled", "true"); // NOI18N
        profilerElement.setAttribute("name", "NetBeansProfiler"); // NOI18N

        // Create "jvm-options" element
        String jvmOptionsElementTextContent = getProfilerAgentCommandLineArgsForDomainScript(targetOS, false, commPort);

        // debugging property for agent side - wire I/O
        if (System.getProperty("org.netbeans.lib.profiler.wireprotocol.WireIO.agent") != null) { // NOI18N
            jvmOptionsElementTextContent += " -Dorg.netbeans.lib.profiler.wireprotocol.WireIO=true"; // NOI18N
                                                                                                     // debugging property for agent side - Class loader hook
        }

        if (System.getProperty("org.netbeans.lib.profiler.server.ProfilerInterface.classLoadHook") != null) { // NOI18N
            jvmOptionsElementTextContent += " -Dorg.netbeans.lib.profiler.server.ProfilerInterface.classLoadHook=true"; // NOI18N
        }

        insertJvmOptions(domainScriptDocument, profilerElement, jvmOptionsElementTextContent);
        
        // Find the "java-config" element
        NodeList javaConfigNodeList = domainScriptDocument.getElementsByTagName("java-config"); // NOI18N

        if ((javaConfigNodeList == null) || (javaConfigNodeList.getLength() == 0)) {
            //            lastErrorMessage = MessageFormat.format(CANNOT_FIND_JAVACONFIG_MSG, new Object[] {domainScriptFilePath});
            //            return false;
            return;
        }

        // Insert the "profiler" element as a first child of "java-config" element
        Node javaConfigNode = javaConfigNodeList.item(0);

        if (javaConfigNode.getFirstChild() != null) {
            javaConfigNode.insertBefore(profilerElement, javaConfigNode.getFirstChild());
        } else {
            javaConfigNode.appendChild(profilerElement);
        }

        // Save domain.xml
        saveDomainScriptFile(domainScriptDocument, buffer);
    }

    protected void modifyScriptFileForDynamicAttach(final String hostOS, final int port, final boolean isReplaceFile,
                                                    final StringBuffer buffer) {
        Document domainScriptDocument = loadDomainScriptFile(buffer);

        if (domainScriptDocument == null) {
            return;
        }

        // Remove any previously defined profiler element(s)
        NodeList profilerElementNodeList = domainScriptDocument.getElementsByTagName("profiler"); // NOI18N

        if ((profilerElementNodeList != null) && (profilerElementNodeList.getLength() > 0)) {
            while (profilerElementNodeList.getLength() > 0) {
                profilerElementNodeList.item(0).getParentNode().removeChild(profilerElementNodeList.item(0));
            }
        }

        // Save domain.xml
        saveDomainScriptFile(domainScriptDocument, buffer);
    }
    
    protected void insertJvmOptions(Document domainScriptDocument, Element profilerElement, String optionsString) {
        insertJvmOptionsElement(domainScriptDocument, profilerElement, optionsString);
    }

    protected String getJvmOptionsElementText(String options) {
        return createJvmOptionsElementText(options);
    }
    
    final protected String createJvmOptionsElementText(String options) {
        return "&nbsp;&nbsp;&lt;jvm-options&gt;" + options.trim() + "&lt;/jvm-options&gt;<br>"; // NOI18N
    }
    
    final protected void insertJvmOptionsElement(Document domainScriptDocument, Element profilerElement, String option) {
        Element jvmOptionsElement = domainScriptDocument.createElement("jvm-options"); // NOI18N
        jvmOptionsElement.setTextContent(option);
        profilerElement.appendChild(jvmOptionsElement);
    }
    
    private static String getAsScriptFilePath(final String installDir, final String specDir, final String scriptName,
                                              final String targetOS) {
        final String separator = IntegrationUtils.getDirectorySeparator(targetOS); // NOI18N
        StringBuilder path = new StringBuilder();

        path.append(installDir);

        if (!installDir.endsWith(separator)) {
            path.append(separator);
        }

        path.append(specDir).append(separator).append(scriptName);

        return path.toString();
    }

    private String getAsAdminScriptFileName(String targetOS) {
        return getAsScriptFile(SUNAS_8PE_ASADMINSCRIPT_NAME, targetOS);
    }

    private String getAsAdminScriptFilePath(String targetOS) {
        return getAsScriptFilePath(SUNAS_8PE_BINDIR_NAME, getAsAdminScriptFileName(targetOS), targetOS);
    }

    private String getAsEnvScriptFileName(String targetOS) {
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            return SUNAS_8PE_ASENVSCRIPT_NAME + ".bat"; // NOI18N
        } else {
            return SUNAS_8PE_ASENVSCRIPT_NAME + ".conf"; // NOI18N
        }
    }

    private String getAsEnvScriptFilePath(String targetOS) {
        return getAsScriptFilePath(getConfigDir(targetOS), getAsEnvScriptFileName(targetOS), targetOS);
    }

    private String getAsScriptFile(final String scriptName, final String targetOS) {
        if (IntegrationUtils.isWindowsPlatform(targetOS)) {
            return scriptName + ".bat"; // NOI18N
        }

        return scriptName + ""; // NOI18N
    }

    private String getAsScriptFilePath(final String specDir, final String scriptName, final String targetOS) {
        return getAsScriptFilePath(getInstallationPath(), specDir, scriptName, targetOS);
    }

    private String getDefaultInstallationPath() {
        String home = ""; // NOI18N

        try {
            String homeEnv = System.getenv(SUNAS_INSTALL_VAR_STRING); // java.lang.Error: getenv no longer supported exception is thrown on 1.4.2

            if ((homeEnv != null) && (homeEnv.length() > 1)) {
                File homeDir = new File(homeEnv);

                if (homeDir.exists() && homeDir.isDirectory()) {
                    home = homeEnv;
                }
            } else {
                homeEnv = System.getenv(SUNAS_HOME_VAR_STRING); // java.lang.Error: getenv no longer supported exception is thrown on 1.4.2

                if ((homeEnv != null) && (homeEnv.length() > 1)) {
                    File homeDir = new File(homeEnv);

                    if (homeDir.exists() && homeDir.isDirectory()) {
                        home = homeEnv;
                    }
                }
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) { /* IGNORE */
        }

        return home;
    }

    protected String getDomainsDirPath(String separator) {
        return SUNAS_8PE_DOMAINSDIR_NAME;
    }
    
    protected boolean usesXMLDeclaration() {
        return true;
    }
    
    private String getDomainScriptDirPath(String separator) {
        StringBuilder path = new StringBuilder();

        path.append(getInstallationPath());

        if (!getInstallationPath().endsWith(separator)) {
            path.append(separator);
        }

        path.append(getDomainsDirPath(separator)).append(separator).append(getDomain());
        path.append(separator).append(SUNAS_8PE_DOMAINCONFIGDIR_NAME);

        return path.toString();
    }

    private String getDomainScriptFilePath(String separator) {
        StringBuilder path = new StringBuilder();

        path.append(getDomainScriptDirPath(separator)).append(separator).append(SUNAS_8PE_DOMAINSCRIPT_NAME);

        return path.toString();
    }

    private String getProfilerAgentCommandLineArgsForDomainScript(String targetOS, boolean isRemote, int portNumber) {
        if (!IntegrationUtils.isWindowsPlatform(targetOS)
                || (IntegrationUtils.getNativeLibrariesPath(targetOS, getTargetJava(), isRemote).indexOf(' ') == -1)) {
            return IntegrationUtils.getProfilerAgentCommandLineArgsWithoutQuotes(targetOS, getTargetJava(), isRemote, portNumber); // NOI18N
        }

        return getWinSpecificCommandLineArgs(targetOS, isRemote, portNumber);
    }

    private Document loadDomainScriptFile(StringBuffer scriptBuffer) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            dBuilder.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(String publicId, String systemId)
                                              throws SAXException, IOException {
                        StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                        InputSource source = new InputSource(reader);
                        scriptPublicId = publicId;
                        scriptSystemId = systemId;
                        source.setPublicId(publicId);
                        source.setSystemId(systemId);

                        return source;
                    }
                });

            StringReader scriptReader = new StringReader(scriptBuffer.toString());
            InputSource scriptSource = new InputSource(scriptReader);

            return dBuilder.parse(scriptSource);
        } catch (Exception e) {
            return null;
        }
    }

    private void modifyAsEnvScriptFile(final String targetOS, final AttachSettings attachSettings)
                                throws ScriptModificationException {
        final String asJavaString = (IntegrationUtils.isWindowsPlatform(targetOS) ? ASENV_INSERTION_POINT_WIN_0_STRING
                                                                                  : ASENV_INSERTION_POINT_NOWIN_0_STRING);

        this.modifyScript(getAsEnvScriptFilePath(targetOS),
                          new ProfilerScriptModifier(new TextScriptHeaderModifier(IntegrationUtils.getSilentScriptCommentSign(targetOS))) {
                boolean insertionFound = false;
                int insertionIndex = -1;

                public void onModification(final AttachSettings attachSettings, final String lineBreak, StringBuffer scriptBuffer)
                                    throws ScriptModificationException {
                }

                public void onLineRead(final StringBuffer line)
                                throws ScriptModificationException {
                    if (line.toString().trim().startsWith(asJavaString)) {
                        line.delete(0, line.length());
                        //          line.append(asJavaString).append("=\"").append(targetJavaHomePath).append("\""); // NOI18N
                        line.append(asJavaString).append("=").append(getTargetJavaHome()); // NOI18N
                    }
                }
            }, attachSettings);
    }

    private void saveDomainScriptFile(final Document domainScriptDocument, final StringBuffer scriptBuffer) {
        try {
            StringWriter stringWriter = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
            if (usesXMLDeclaration()) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, scriptPublicId);
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, scriptSystemId);
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, usesXMLDeclaration() ? "no" : "yes"); // NOI18N

            DOMSource domSource = new DOMSource(domainScriptDocument);
            StreamResult streamResult = new StreamResult(stringWriter);

            transformer.transform(domSource, streamResult);

            if (stringWriter.getBuffer().length() > 0) {
                scriptBuffer.delete(0, scriptBuffer.length());
                scriptBuffer.append(stringWriter.getBuffer());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
