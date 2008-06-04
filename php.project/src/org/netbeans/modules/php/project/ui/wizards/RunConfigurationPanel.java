/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.ui.SourcesFolderNameProvider;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * @author Tomas Mysik
 */
public class RunConfigurationPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    static final String[] CFG_PROPS = new String[] {
        PhpProjectProperties.RUN_AS,
        PhpProjectProperties.URL,
        PhpProjectProperties.REMOTE_CONNECTION,
        PhpProjectProperties.REMOTE_DIRECTORY,
        PhpProjectProperties.REMOTE_UPLOAD,
    };

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final SourcesFolderNameProvider sourcesFolderNameProvider;
    private RunConfigurationPanelVisual runConfigurationPanelVisual = null;
    private WizardDescriptor descriptor = null;
    private final ConfigManager.ConfigProvider configProvider = new WizardConfigProvider();

    public RunConfigurationPanel(String[] steps, SourcesFolderNameProvider sourcesFolderNameProvider) {
        this.sourcesFolderNameProvider = sourcesFolderNameProvider;
        this.steps = steps;
    }

    String[] getSteps() {
        return steps;
    }

    public Component getComponent() {
        if (runConfigurationPanelVisual == null) {
            ConfigManager configManager = new ConfigManager(configProvider);
            // create exactly one configuration
            configManager.createNew(WizardConfigProvider.CONFIG_NAME, WizardConfigProvider.CONFIG_NAME);
            runConfigurationPanelVisual = new RunConfigurationPanelVisual(this, configManager, sourcesFolderNameProvider);
        }
        return runConfigurationPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(RunConfigurationPanel.class.getName());
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;
        runConfigurationPanelVisual.removeRunConfigurationListener(this);
        // XXX
        runConfigurationPanelVisual.addRunConfigurationListener(this);
        fireChangeEvent();
    }

    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        // XXX
    }

    public boolean isValid() {
        getComponent();
        descriptor.putProperty("WizardPanel_errorMessage", " "); // NOI18N
        descriptor.putProperty("WizardPanel_errorMessage", "AAAAAAAXXXXXXXXXX"); // NOI18N
        return false;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private class WizardConfigProvider implements ConfigManager.ConfigProvider {
        static final String CONFIG_NAME = "wizard"; // NOI18N

        final Map<String, Map<String, String>> configs;

        public WizardConfigProvider() {
            configs = ConfigManager.createEmptyConfigs();
        }

        public String[] getConfigProperties() {
            return CFG_PROPS;
        }

        public Map<String, Map<String, String>> getConfigs() {
            return configs;
        }

        public String getActiveConfig() {
            return CONFIG_NAME;
        }

        public void setActiveConfig(String configName) {
        }
    }
}
