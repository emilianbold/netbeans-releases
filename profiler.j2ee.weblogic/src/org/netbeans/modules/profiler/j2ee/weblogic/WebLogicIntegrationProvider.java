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

package org.netbeans.modules.profiler.j2ee.weblogic;

import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.modules.profiler.attach.providers.AbstractIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.IntegrationCategorizer;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.spi.ModificationException;
import org.netbeans.modules.profiler.attach.spi.RunException;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({  
    "WebLogicIntegrationProvider_ManualRemoteStep3Msg=Go to your remote domain directory <code>{0}{1}&lt;PATH_TO_YOUR_DOMAIN&gt;{1}{2}</code>, where <code>{0}</code> is the installation directory of your Bea products on remote host and <code>&lt;PATH_TO_YOUR_DOMAIN&gt;</code> is the actual path to your domain, for example \"<code>user_projects{1}domains{1}{3}</code>\".<br><br>{4}",
    "WebLogicIntegrationProvider_CopyFile81Msg=Copy file <code>startWebLogic.{0}</code> in this directory to <code>startWebLogic_nbprofiler.{0}</code>",
    "WebLogicIntegrationProvider_CopyFiles9Msg=Copy files <code>startWebLogic.{0}</code> and <code>setDomainEnv.{0}</code> in this directory to <code>startWebLogic_nbprofiler.{0}</code> and <code>setDomainEnv_nbprofiler.{0}</code>",
    "WebLogicIntegrationProvider_Wl81CleanupText=Then remove any other <code>JAVA_VENDOR</code> and <code>JAVA_HOME</code> definitions later in the script.",
    "WebLogicIntegrationProvider_Wl9CleanupText=Then replace any occurences of <code>setDomainEnv{0}</code> in the script with <code>setDomainEnv_nbprofiler{0}</code>",
    "WebLogicIntegrationProvider_PathToJvmDirText=&lt;path to {0} directory&gt;",
    "WebLogicIntegrationProvider_ManualRemoteStep4WinMsg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just after the <code>SETLOCAL</code> command:<br><code>{1}<br>{2}</code><br>For setting <code>JAVA_OPTIONS</code> the {3}.<br><br>{4}",
    "WebLogicIntegrationProvider_ManualRemoteStep4Wl9Msg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just before the line containing <code># --- Start Functions ---</code>:<br><code>{1}<br>{2}</code><br>For setting <code>JAVA_OPTIONS</code> the {3}.<br><br>{4}",
    "WebLogicIntegrationProvider_ManualRemoteStep4Wl81Msg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just after the line containing <code># Initialize the common environment.</code>:<br><code>{1}<br>{2}</code><br>For setting <code>JAVA_OPTIONS</code> the {3}.<br><br>{4}",
    "WebLogicIntegrationProvider_ManualRemoteStep5Wl81Msg=Start the remote server using <code>startWebLogic_nbprofiler{0}</code>",
    "WebLogicIntegrationProvider_ManualRemoteStep5Wl9Msg=In <code>setDomainEnv_nbprofiler{0}</code> set the <code>SUN_JAVA_HOME</code> variable to point to <code>{1}</code><br>",
    "WebLogicIntegrationProvider_ManualRemoteStep6Wl81Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "WebLogicIntegrationProvider_ManualRemoteStep6Wl9Msg=Start the remote server using <code>startWebLogic_nbprofiler.{0}</code>",
    "WebLogicIntegrationProvider_ManualRemoteStep7Wl9Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "WebLogicIntegrationProvider_ManualDirectDynamicStep1Msg=Go to your domain directory <code>{0}{1}&lt;PATH_TO_YOUR_DOMAIN&gt;{1}{2}</code>, where <code>{0}</code> is the installation directory of your Bea products and <code>&lt;PATH_TO_YOUR_DOMAIN&gt;</code> is the actual path to your domain, for example \"<code>user_projects{1}domains{1}{3}</code>\".<br><br>{4}",
    "WebLogicIntegrationProvider_ManualDirectStep2WinMsg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just after the <code>SETLOCAL</code> command:<br><code>{1}<br>{2}</code><br><br>{3}",
    "WebLogicIntegrationProvider_ManualDirectStep2Wl9Msg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just before the line containing <code># --- Start Functions ---</code>:<br><code>{1}<br>{2}</code><br><br>{3}",
    "WebLogicIntegrationProvider_ManualDirectStep2Wl81Msg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just after the line containing <code># Initialize the common environment.</code>:<br><code>{1}<br>{2}</code><br><br>{3}",
    "WebLogicIntegrationProvider_ManualDirectDynamicStep3Wl81Msg=Start the server using <code>startWebLogic_nbprofiler.{0}</code>",
    "WebLogicIntegrationProvider_ManualDirectDynamicStep3Wl9Msg=In <code>setDomainEnv_nbprofiler.{0}</code> set the <code>SUN_JAVA_HOME</code> variable to point to <code>{1}</code>",
    "WebLogicIntegrationProvider_ManualDirectStep4Wl81Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "WebLogicIntegrationProvider_ManualDirectDynamicStep4Wl9Msg=Start the server using <code>startWebLogic_nbprofiler.{0}</code>",
    "WebLogicIntegrationProvider_ManualDirectStep5Wl9Msg=The JVM will start, but will not proceed with server execution until you connect the profiler.",
    "WebLogicIntegrationProvider_ManualDynamicStep2WinMsg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just after the <code>SETLOCAL</code> command:<br><code>{1}</code><br><br>{2}",
    "WebLogicIntegrationProvider_ManualDynamicStep2Wl9Msg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just before the line containing <code># --- Start Functions ---</code>:<br><code>{1}</code><br><br>{2}",
    "WebLogicIntegrationProvider_ManualDynamicStep2Wl81Msg=Put the following settings at the beginning of <code>startWebLogic_nbprofiler.{0}</code> just after the line containing <code># Initialize the common environment.</code>:<br><code>{1}</code><br><br>{2}",
    "WebLogicIntegrationProvider_ManualDynamicStep4Wl81Msg=When the server is running, click Attach to select the server process to attach to.",
    "WebLogicIntegrationProvider_ManualDynamicStep5Wl9Msg=When the server is running, click Attach to select the server process to attach to.",
    "WebLogicIntegrationProvider_ManualDynamicUnsupportedConfigMsg=This configuration is not supported for {0}.",
    "WebLogicIntegrationProvider_DynamicWarningMessage=Make sure your IDE is using {0}."
})
public abstract class WebLogicIntegrationProvider extends AbstractIntegrationProvider {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public WebLogicIntegrationProvider() {
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

    public void categorize(IntegrationCategorizer categorizer) {
        categorizer.addAppserver(this, getAttachWizardPriority());
    }

    public void modify(AttachSettings attachSettings) throws ModificationException {
    }

    public void run(AttachSettings settings) throws RunException {
    }

    public boolean supportsAutomation() {
        return false;
    }

    protected abstract IntegrationProvider.IntegrationHints getManualLocalDirectIntegrationStepsInstructions(String targetOS,
                                                                                                             AttachSettings attachSettings);

    protected abstract IntegrationProvider.IntegrationHints getManualLocalDynamicIntegrationStepsInstructions(String targetOS,
                                                                                                              AttachSettings attachSettings);

    protected abstract IntegrationProvider.IntegrationHints getManualRemoteIntegrationStepsInstructions(String targetOS,
                                                                                                        AttachSettings attachSettings);
}
