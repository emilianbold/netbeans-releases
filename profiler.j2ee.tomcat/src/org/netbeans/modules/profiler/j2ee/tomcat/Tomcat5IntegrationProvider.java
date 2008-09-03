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

package org.netbeans.modules.profiler.j2ee.tomcat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.attach.providers.ValidationResult;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.spi.ModificationException;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
public class Tomcat5IntegrationProvider extends AbstractTomcatIntegrationProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final String INTEGR_REVIEW_STEP1_WINEXE_MSG = NbBundle.getMessage(this.getClass(),
                                                                              "TomcatIntegrationProvider_IntegrReviewStep1WinExeMsg"); // NOI18N
    private final String MANUAL_WINEXE_HINT_MSG = NbBundle.getMessage(this.getClass(),
                                                                      "TomcatIntegrationProvider_ManualWinExeHint"); // NOI18N
    private final String PATH_TO_CATALINA_BASE_MSG = NbBundle.getMessage(this.getClass(),
                                                                         "TomcatIntegrationProvider_Catalina_Base_Hint"); // NOI18N
    private final String TOMCAT_50_TITLE = NbBundle.getMessage(this.getClass(), "TomcatIntegrationProvider_Tomcat50String"); // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IntegrationProvider.IntegrationHints getIntegrationReview(AttachSettings attachSettings) {
        IntegrationHints retValue;
        String targetOS = attachSettings.getHostOS();

        if (isTomcatExeUsed(this.getInstallationPath())) {
            retValue = new IntegrationProvider.IntegrationHints();
            retValue.addStep(MessageFormat.format(INTEGR_REVIEW_STEP1_WINEXE_MSG,
                                                  new Object[] { getModifiedScriptPath(targetOS, true) }));
            retValue.addStep(MessageFormat.format(INTEGR_REVIEW_STEP2_MSG,
                                                  new Object[] { getChangedLines(targetOS, attachSettings, getCatalinaBase(),
                                                                                 false) }));
        } else {
            retValue = super.getIntegrationReview(attachSettings);
        }

        return retValue;
    }

    public String getTitle() {
        return TOMCAT_50_TITLE;
    }

    public void modify(AttachSettings attachSettings) throws ModificationException {
        String targetOS = attachSettings.getHostOS();

        if (isTomcatExeUsed(this.getInstallationPath())) {
            StringBuffer catalinaScript = getChangedLines(targetOS, attachSettings, getCatalinaBase(), false);

            File catalinaScriptFile = new File(getModifiedScriptPath(targetOS, false));
            OutputStream os = null;

            try {
                os = new FileOutputStream(catalinaScriptFile);
                os.write(catalinaScript.toString().getBytes());
                os.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                }
            }
        } else {
            super.modify(attachSettings);
        }
    }

    public ValidationResult validateInstallation(final String targetOS, final String path) {
        ValidationResult retValue = super.validateInstallation(targetOS, path);

        if (!retValue.isValid()) {
            if (isTomcatExeUsed(path) && IntegrationUtils.isWindowsPlatform(targetOS)) { // Tomcat installed by the windows installer has no catalina.bat; check for tomcat*.exe instead
                retValue = new ValidationResult(true, "No CATALINA script found. Using " + getTomcatExe() + " instead"); // NOI18N
            }
        }

        return retValue;
    }

    protected int getAttachWizardPriority() {
        return 12;
    }

    protected String getCatalinaScriptName() {
        return "catalina.50"; // NOI18N
    }

    protected String getTomcatName() {
        return "Tomcat5"; // NOI18N
    }

    private String getTomcatExe() {
        return getTomcatName() + ".exe"; // NOI18N
    }

    protected int getMagicNumber() {
        return 20;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                    AttachSettings attachSettings) {
        IntegrationHints retValue;

        retValue = super.getManualLocalDirectIntegrationStepsInstructions(targetOS, attachSettings);
        addWinExeHint(retValue, targetOS, attachSettings);

        return retValue;
    }

    protected IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                     AttachSettings attachSettings) {
        IntegrationHints retValue;

        retValue = super.getManualLocalDynamicIntegrationStepsInstructions(targetOS, attachSettings);
        addWinExeHint(retValue, targetOS, attachSettings);

        return retValue;
    }

    protected IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                               AttachSettings attachSettings) {
        IntegrationHints retValue;

        retValue = super.getManualRemoteIntegrationStepsInstructions(targetOS, attachSettings);
        addWinExeHint(retValue, targetOS, attachSettings);

        return retValue;
    }

    private StringBuffer getChangedLines(final String targetOS, final AttachSettings attachSettings, final String catalinaBase,
                                         final boolean inHtml) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        String catalinaBaseOption = ""; // NOI18N

        StringBuffer catalinaScript = new StringBuffer();
        catalinaScript.append(IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_OPTS",
                                                                               IntegrationUtils.getProfilerAgentCommandLineArgs(targetOS,
                                                                                                                                getTargetJava(),
                                                                                                                                false,
                                                                                                                                attachSettings
                                                                                                                                .getPort()))); // NOI18N
        appendNewLine(inHtml, catalinaScript);

        if ((catalinaBase != null) && (catalinaBase.trim().length() > 0)) {
            catalinaScript.append(IntegrationUtils.getAssignEnvVariableValueString(targetOS, "CATALINA_BASE",
                                                                                   getQuotedPath(catalinaBase))); // NOI18N
            appendNewLine(inHtml, catalinaScript);
            catalinaBaseOption = ";-Dcatalina.base=" + IntegrationUtils.getEnvVariableReference("CATALINA_BASE", targetOS); // NOI18N
        }

        catalinaScript.append(getQuotedPath(this.getInstallationPath() + separator + "bin" + separator + getTomcatExe()))
                      .append(" //TS//" + getTomcatName()); // NOI18N
        catalinaScript.append(" ").append("--JavaHome=").append(getQuotedPath(getTargetJavaHome())).append(' '); // NOI18N
        catalinaScript.append("--JvmOptions %CATALINA_OPTS%").append(catalinaBaseOption); // NOI18N

        return catalinaScript;
    }

    private String getQuotedPath(final String path) {
        if (path.indexOf(' ') > -1) { // NOI18N

            return "\"" + path + "\""; // NOI18N
        } else {
            return path;
        }
    }

    private boolean isTomcatExeUsed(final String path) {
        final String separator = System.getProperty("file.separator"); // NOI18N
        final File exeFile = new File(path + separator + "bin" + separator + getTomcatExe()); // NOI18N
        final File scriptFile = new File(getScriptPath(IntegrationUtils.PLATFORM_WINDOWS_OS, false)); // this method is called only for Windows tomcat installation

        return exeFile.exists() && !scriptFile.exists();
    }

    private void addWinExeHint(final IntegrationProvider.IntegrationHints hints, final String targetOS,
                               final AttachSettings attachSettings) {
        if (isTomcatExeUsed(this.getInstallationPath())) {
            hints.addHint(MessageFormat.format(MANUAL_WINEXE_HINT_MSG,
                                               new Object[] {
                                                   getScriptPath(targetOS, true), getModifiedScriptPath(targetOS, true),
                                                   getChangedLines(targetOS, attachSettings, PATH_TO_CATALINA_BASE_MSG, true)
                                               }));
        }
    }

    private void appendNewLine(final boolean inHtml, final StringBuffer buffer) {
        if (inHtml) {
            buffer.append("<br>"); // NOI18N
        } else {
            buffer.append('\n'); // NOI18N
        }
    }
}
