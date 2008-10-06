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

package org.netbeans.modules.profiler.j2ee.sunas;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.profiler.attach.panels.AttachWizardPanel;
import org.netbeans.modules.profiler.attach.providers.TargetPlatform;
import org.netbeans.modules.profiler.attach.providers.ValidationResult;
import org.netbeans.modules.profiler.attach.wizard.AttachWizardContext;


/**
 *
 * @author Jaroslav Bachorik
 */
public class SunASIntegrationPanel extends AttachWizardPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------


    /*default*/ class Model {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private List domains = null;
        private String domain = ""; // NOI18N
        private String installPath = ""; // NOI18N
        private TargetPlatform targetPlatform;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Model() {
            this.domains = new Vector();
            targetPlatform = null;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setDomain(String domain) {
            this.domain = domain;
            publishUpdate(new ChangeEvent(this));
        }

        public String getDomain() {
            return domain;
        }

        public String getDomainHint() {
            return domainHint;
        }

        public List getDomains() {
            return domains;
        }

        public void setInstallPath(String installPath) {
            SunASAutoIntegrationProvider provider = (SunASAutoIntegrationProvider) getContext()
                                                                                       .getIntegrationProvider();
            String targetOS = getContext().getAttachSettings().getHostOS();

            ValidationResult validation = provider.validateInstallation(targetOS, installPath);

            if (!validation.isValid()) {
                hint = validation.getMessage();
                domainHint = NbBundle.getMessage(this.getClass(), "SunAS8IntegrationProvider_InstallDirPendingMsg"); // NOI18N
            } else {
                hint = ""; // NOI18N
                domainHint = ""; // NOI18N
            }

            this.installPath = installPath;
            updateDomains();
            publishUpdate(new ChangeEvent(this));
        }

        public String getInstallPath() {
            return installPath;
        }

        public String getInstallPathHint() {
            return hint;
        }

        public TargetPlatform getSelectedPlatform() {
            return targetPlatform;
        }

        public void setTargetJava(TargetPlatform java) {
            targetPlatform = java;
            publishUpdate(new ChangeEvent(this));
        }

        private void updateDomains() {
            SunASAutoIntegrationProvider provider = (SunASAutoIntegrationProvider) getContext().getIntegrationProvider();

            domains.clear();
            domains.addAll(provider.getAvailableDomains(this.installPath));

            if (((this.domain == null) || (this.domain.length() == 0)) && (this.domains.size() > 0)) {
                this.domain = (String) this.domains.get(0);
            } else if (this.domains.size() == 0) {
                this.domain = null;
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "SunASIntegrationPanel.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Model model = null;
    private String domainHint = ""; // NOI18N
    private String hint = ""; // NOI18N
    private SunASIntegrationPanelUI panel = null;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of SunASIntegrationPanel */
    public SunASIntegrationPanel() {
        this.model = new Model();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelp() {
        return HELP_CTX;
    }

    public boolean isValid() {
        return (this.model.getInstallPath() != null) && (this.model.getInstallPath().length() > 0)
               && (this.model.getDomain() != null) && (this.model.getDomain().length() > 0)
               && (this.model.getSelectedPlatform() != null);
    }

    public boolean canBack(AttachWizardContext context) {
        return true;
    }

    public boolean canFinish(AttachWizardContext context) {
        return false;
    }

    public boolean canNext(AttachWizardContext context) {
        return isValid();
    }

    public boolean onCancel(AttachWizardContext context) {
        return true;
    }

    public void onEnter(AttachWizardContext context) {
        if ((model.getDomain() == null) || (model.getDomain().length() == 0)) {
            model.setDomain(((SunASAutoIntegrationProvider) context.getIntegrationProvider()).getDomain());
        }

        if ((model.getInstallPath() == null) || (model.getInstallPath().length() == 0)) {
            model.setInstallPath(((SunASAutoIntegrationProvider) context.getIntegrationProvider()).getInstallationPath());
        }

        final String selectedJavaHome = ((SunASAutoIntegrationProvider) context.getIntegrationProvider()).getTargetJava();
        final List platformList = TargetPlatform.getPlatformList(false);

        for (Iterator it = platformList.iterator(); it.hasNext();) {
            TargetPlatform platform = (TargetPlatform) it.next();

            if (platform.equals(selectedJavaHome)) {
                model.setTargetJava(platform);

                break;
            }
        }

        panel.loadModel();
    }

    public void onExit(AttachWizardContext context) {
        SunASAutoIntegrationProvider provider = (SunASAutoIntegrationProvider) getContext().getIntegrationProvider();
        boolean isModified = false;
        if (provider.getInstallationPath() == null || provider.getDomain() == null || provider.getTargetJavaHome() == null) {
            return;
        }

        if (!provider.getInstallationPath().equals(this.model.getInstallPath())) {
            isModified = true;
        } else if (!provider.getDomain().equals(this.model.getDomain())) {
            isModified = true;
        } else if ((provider.getTargetJavaHome() != null) && (model.getSelectedPlatform() != null)
                       && !provider.getTargetJavaHome().equals(this.model.getSelectedPlatform().getHomePath())) {
            isModified = true;
        }

        if (isModified) {
            context.setConfigChanged();
        }

        provider.setInstallationPath(this.model.getInstallPath());
        provider.setDomain(this.model.getDomain());
        provider.setTargetPlatform(this.model.getSelectedPlatform());
    }

    public void onFinish(AttachWizardContext context) {
    }

    protected JPanel getRenderPanel() {
        if (panel == null) {
            panel = new SunASIntegrationPanelUI(this.model);
        }

        return panel;
    }

    protected void onPanelShow() {
        this.panel.refreshJvmList(model.getSelectedPlatform());
    }
}
