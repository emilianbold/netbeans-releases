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

import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.modules.profiler.attach.panels.AttachWizardPanel;
import org.netbeans.modules.profiler.attach.providers.TargetPlatform;
import org.netbeans.modules.profiler.attach.providers.TargetPlatformEnum;
import org.netbeans.modules.profiler.attach.providers.ValidationResult;
import org.netbeans.modules.profiler.attach.wizard.AttachWizardContext;
import org.openide.util.HelpCtx;


/**
 *
 * @author Tomas Hurka
 * @author Jaroslav Bachorik
 */
public class TomcatIntegrationPanel extends AttachWizardPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------


    /* default */ class Model {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private TargetPlatform.TargetPlatformFilter platformFilter = new TargetPlatform.TargetPlatformFilter() {
            public boolean isSupported(TargetPlatform javaPlatform) {
                AttachSettings settings = getContext().getAttachSettings();
                TargetPlatformEnum jvm = javaPlatform.getAsEnum();

                if (!settings.isDirect()) {
                    if (settings.isDynamic16()) {
                        if (!jvm.equals(TargetPlatformEnum.JDK6) && !jvm.equals(TargetPlatformEnum.JDK7)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return getContext().getIntegrationProvider().supportsJVM(javaPlatform.getAsEnum(), settings);
            }
        };

        private String catalinaBase = ""; // NOI18N
        private String catalinaHint = ""; // NOI18N
        private String tomcatHint = ""; // NOI18N
        private String tomcatInstall = ""; // NOI18N
        private TargetPlatform selectedPlatform = null;
        private boolean catalinaValid = true;
        private boolean tomcatValid = false;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setCatalinaBase(String path) {
            if (getContext() == null) {
                return;
            }

            ValidationResult result = ((AbstractTomcatIntegrationProvider) getContext().getIntegrationProvider())
                                                                                                              .validateCatalinaBase(path);

            if (result.isValid()) {
                this.catalinaValid = true;
                this.catalinaHint = ""; // NOI18N
            } else {
                this.catalinaValid = false;
                this.catalinaHint = result.getMessage();
            }

            this.catalinaBase = path;
            publishUpdate(new ChangeEvent(this));
        }

        public String getCatalinaBase() {
            return this.catalinaBase;
        }

        public String getCatalinaBaseHint() {
            return this.catalinaHint;
        }

        public boolean isCatalinaValid() {
            return this.catalinaValid;
        }

        public TargetPlatform.TargetPlatformFilter getPlatformFilter() {
            return platformFilter;
        }

        public TargetPlatformEnum getSelectedJVM() {
            return this.selectedPlatform.getAsEnum();
        }

        public void setSelectedPlatform(TargetPlatform platform) {
            this.selectedPlatform = platform;
            publishUpdate(new ChangeEvent(this));
        }

        public TargetPlatform getSelectedPlatform() {
            return this.selectedPlatform;
        }

        public void setTomcatInstall(String path) {
            if (getContext() == null) {
                return;
            }

            String targetOS = getContext().getAttachSettings().getHostOS();

            ValidationResult result = ((AbstractTomcatIntegrationProvider) getContext().getIntegrationProvider()).validateInstallation(targetOS,                                                                                                                                                                                       path);

            if (result.isValid()) {
                this.tomcatValid = true;
                this.tomcatHint = ""; // NOI18N
            } else {
                this.tomcatValid = false;
                this.tomcatHint = result.getMessage();
            }

            this.tomcatInstall = path;
            publishUpdate(new ChangeEvent(this));
        }

        public String getTomcatInstall() {
            return this.tomcatInstall;
        }

        public String getTomcatInstallHint() {
            return this.tomcatHint;
        }

        public boolean isTomcatValid() {
            return this.tomcatValid;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "TomcatIntegrationPanel.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Model model = null;
    private TomcatIntegrationPanelUI panel = null;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TomcatIntegrationPanel() {
        this.model = new Model();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelp() {
        return HELP_CTX;
    }

    public boolean isValid() {
        return this.model.isTomcatValid() && this.model.isCatalinaValid() && (this.model.getSelectedPlatform() != null);
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
        model.setCatalinaBase(((AbstractTomcatIntegrationProvider) context.getIntegrationProvider()).getCatalinaBase());
        model.setTomcatInstall(((AbstractTomcatIntegrationProvider) context.getIntegrationProvider()).getInstallationPath());

        final String selectedJavaHome = ((AbstractTomcatIntegrationProvider) context.getIntegrationProvider()).getTargetJava();
        final List platformList = TargetPlatform.getPlatformList(false);

        for (Iterator it = platformList.iterator(); it.hasNext();) {
            TargetPlatform platform = (TargetPlatform) it.next();

            if (platform.getHomePath().equals(selectedJavaHome)) {
                model.setSelectedPlatform(platform);
            }
        }

        this.panel.loadModel();
    }

    public void onExit(AttachWizardContext context) {
        //    setTrackUpdates(false);
        //    this.panel.setModelDefaults();
        //    setTrackUpdates(true);
        AbstractTomcatIntegrationProvider provider = (AbstractTomcatIntegrationProvider) context.getIntegrationProvider();
        boolean isModified = false;

        if ((provider.getTargetJavaHome() != null) && (this.model.getSelectedPlatform() != null)
                && !provider.getTargetJavaHome().equals(this.model.getSelectedPlatform().getHomePath())) {
            isModified = true;
        } else if (!provider.getCatalinaBase().equals(this.model.getCatalinaBase())) {
            isModified = true;
        } else if (!provider.getInstallationPath().equals(this.model.getTomcatInstall())) {
            isModified = true;
        }

        if (isModified) {
            context.setConfigChanged();
        }

        provider.setTargetPlatform(this.model.getSelectedPlatform());
        provider.setCatalinaBase(this.model.getCatalinaBase());
        provider.setInstallationPath(this.model.getTomcatInstall());
    }

    public void onFinish(AttachWizardContext context) {
    }

    protected JPanel getRenderPanel() {
        if (this.panel == null) {
            this.panel = new TomcatIntegrationPanelUI(this.model);
        }

        return this.panel;
    }

    protected void onPanelShow() {
        panel.refreshJvmList(model.getSelectedPlatform());
    }
}
