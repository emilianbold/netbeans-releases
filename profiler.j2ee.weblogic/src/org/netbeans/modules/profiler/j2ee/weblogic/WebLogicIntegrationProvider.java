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

package org.netbeans.modules.profiler.j2ee.weblogic;

import java.util.ResourceBundle;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.modules.profiler.attach.providers.AbstractIntegrationProvider;
import org.netbeans.modules.profiler.attach.providers.IntegrationCategorizer;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;
import org.netbeans.modules.profiler.attach.spi.ModificationException;
import org.netbeans.modules.profiler.attach.spi.RunException;
import org.netbeans.modules.profiler.attach.wizard.steps.NullWizardStep;

/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class WebLogicIntegrationProvider extends AbstractIntegrationProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="Resources">
    protected final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.profiler.j2ee.weblogic.Bundle"); // NOI18N
    protected final String DYNAMIC_WARNING_MESSAGE = messages.getString("WebLogicIntegrationProvider_DynamicWarningMessage"); // NOI18N  
                                                                                                                              // </editor-fold>
    protected final String MANUAL_DIRECT_DYNAMIC_STEP1_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectDynamicStep1Msg"); // NOI18N
    protected final String MANUAL_DIRECT_STEP2_MSG = messages.getString("WebLogicIntegrationProvider_ManualDirectStep2Msg"); // NOI18N
    protected final String MANUAL_DYNAMIC_STEP2_MSG = messages.getString("WebLogicIntegrationProvider_ManualDynamicStep2Msg"); // NOI18N
    protected final String MANUAL_REMOTE_STEP3_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep3Msg"); // NOI18N
    protected final String MANUAL_REMOTE_STEP4_MSG = messages.getString("WebLogicIntegrationProvider_ManualRemoteStep4Msg"); // NOI18N
    protected final String PATH_TO_JVM_DIR_TEXT = messages.getString("WebLogicIntegrationProvider_PathToJvmDirText"); // NOI18N
    protected final String WINDOWS_ANCHOR_TEXT = messages.getString("WebLogicIntegrationProvider_WindowsAnchorText"); // NOI18N

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
