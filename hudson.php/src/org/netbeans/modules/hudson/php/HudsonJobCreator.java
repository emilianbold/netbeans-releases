/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hudson.php;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.php.commands.PpwScript;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.options.HudsonOptionsValidator;
import org.netbeans.modules.hudson.php.support.Target;
import org.netbeans.modules.hudson.php.ui.options.HudsonOptionsPanelController;
import org.netbeans.modules.hudson.php.xml.XmlUtils;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.ConfigurationStatus;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.Helper;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class HudsonJobCreator extends JPanel implements ProjectHudsonJobCreator, ChangeListener {

    private static final long serialVersionUID = -668435132135465L;

    private static final Logger LOGGER = Logger.getLogger(HudsonJobCreator.class.getName());

    final List<Target> targets;

    private final PhpModule phpModule;
    private final HudsonSCM.Configuration scm;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private HudsonJobCreator(PhpModule phpModule) {
        this.phpModule = phpModule;
        scm = Helper.prepareSCM(FileUtil.toFile(phpModule.getProjectDirectory()));
        targets = initComponents();
    }

    private static HudsonJobCreator forPhpModule(PhpModule phpModule) {
        HudsonJobCreator hudsonJobCreator = new HudsonJobCreator(phpModule);
        // listeners
        HudsonOptions options = HudsonOptions.getInstance();
        options.addChangeListener(WeakListeners.change(hudsonJobCreator, options));
        return hudsonJobCreator;
    }

    @Override
    public String jobName() {
        return phpModule.getDisplayName();
    }

    @Override
    public JComponent customizer() {
        return this;
    }

    @NbBundle.Messages({
        "HudsonJobCreator.error.noTests=The project does not have any tests.",
        "HudsonJobCreator.error.invalidHudsonOptions=PHP Hudson options are invalid.",
        "HudsonJobCreator.error.buildXmlExists=The project already has build.xml file.",
        "HudsonJobCreator.error.phpUnitConfigExists=The project already has phpunit.xml.dist file."
    })
    @Override
    public ConfigurationStatus status() {
        if (phpModule.getTestDirectory() == null) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_noTests());
        }
        if (scm == null) {
            return Helper.noSCMError();
        }
        // ppw script
        try {
            PpwScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_invalidHudsonOptions()).withExtraButton(getOpenHudsonOptionsButton());
        }
        // job config
        if (HudsonOptionsValidator.validateJobConfig(getJobConfig()) != null) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_invalidHudsonOptions()).withExtraButton(getOpenHudsonOptionsButton());
        }
        // build.xml
        FileObject buildXml = phpModule.getProjectDirectory().getFileObject(PpwScript.BUILD_XML);
        if (buildXml != null && buildXml.isData()) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_buildXmlExists());
        }
        // phpunit.xml
        FileObject phpUnitConfig = phpModule.getProjectDirectory().getFileObject(PpwScript.PHPUNIT_XML);
        if (phpUnitConfig != null && phpUnitConfig.isData()) {
            return ConfigurationStatus.withError(Bundle.HudsonJobCreator_error_phpUnitConfigExists());
        }
        // scm
        ConfigurationStatus scmStatus = scm.problems();
        if (scmStatus != null) {
            return scmStatus;
        }
        return ConfigurationStatus.valid();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public Document configure() throws IOException {
        setupProject();
        return createJobXml();
    }

    private String getJobConfig() {
        return HudsonOptions.getInstance().getJobConfig();
    }

    @NbBundle.Messages({
        "HudsonJobCreator.button.labelWithMnemonics=&Hudson Options...",
        "HudsonJobCreator.button.a11y=Open Hudson PHP options."
    })
    private JButton getOpenHudsonOptionsButton() {
        JButton button = new JButton();
        Mnemonics.setLocalizedText(button, Bundle.HudsonJobCreator_button_labelWithMnemonics());
        button.getAccessibleContext().setAccessibleDescription(Bundle.HudsonJobCreator_button_a11y());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UiUtils.showOptions(HudsonOptionsPanelController.OPTIONS_SUBPATH);
            }
        });
        return button;
    }

    @NbBundle.Messages("HudsonJobCreator.error.ppw=The project files were not generated by PPW script.")
    private void setupProject() throws IOException {
        try {
            Map<String, String> targetParams = new LinkedHashMap<String, String>();
            for (Target target : targets) {
                target.apply(targetParams);
            }
            if (PpwScript.getDefault().createProjectFiles(phpModule, targetParams)) {
                processBuildXml();
            } else {
                errorOccured(Bundle.HudsonJobCreator_error_ppw(), "The project files were not generated by PPW script", getOpenHudsonOptionsButton());
            }
        } catch (InvalidPhpExecutableException ex) {
            // cannot happen here since we just validated it
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private Document createJobXml() throws IOException {
        Document document;
        try {
            document = XmlUtils.parse(new File(getJobConfig()));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        // remove scm, triggers & logRotator if present
        removeNodes(document, "/project/scm", "/project/triggers", "/project/logRotator"); // NOI18N
        // configure
        scm.configure(document);
        Helper.addLogRotator(document);
        // enable
        Node disabled = XmlUtils.query(document, "/project/disabled"); // NOI18N
        if (disabled != null) {
            XmlUtils.setNodeValue(document, disabled, "false"); // NOI18N
        }
        return document;
    }

    private void removeNodes(Document document, String... xpathExpressions) {
        for (String xpathExpression : xpathExpressions) {
            Node node = XmlUtils.query(document, xpathExpression);
            if (node != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }

    @NbBundle.Messages("HudsonJobCreator.error.config=Job configuration failed.")
    private void processBuildXml() throws IOException {
        boolean success = true;
        final File buildXml = new File(FileUtil.toFile(phpModule.getProjectDirectory()), PpwScript.BUILD_XML);
        try {
            Document document = XmlUtils.parse(buildXml);
            for (Target target : targets) {
                if (!target.apply(document)) {
                    success = false;
                }
            }
            if (success) {
                XmlUtils.save(document, buildXml);
            }
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        if (!success) {
            warningOccured(Bundle.HudsonJobCreator_error_config());
        }
    }

    private void warningOccured(String warning) {
        informUser(warning, false, null);
    }

    private void errorOccured(String error, String logMessage, JButton extraButton) throws IOException {
        informUser(error, true, extraButton);
        throw new SilentIOException(logMessage);
    }

    private void informUser(String message, boolean error, JButton extraButton) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, error ? NotifyDescriptor.ERROR_MESSAGE : NotifyDescriptor.WARNING_MESSAGE);
        if (extraButton != null) {
            descriptor.setAdditionalOptions(new Object[] {extraButton});
        }
        DialogDisplayer.getDefault().notify(descriptor);
    }

    private List<Target> initComponents() {
        setLayout(new GridBagLayout());

        List<Target> allTargets = Target.all();
        int i = 0;
        for (final Target target : allTargets) {
            initTargetComponent(i++, target);
        }
        finishLayout(i);
        return allTargets;
    }

    @NbBundle.Messages({
        "# {0} - Ant target name",
        "HudsonJobCreator.checkbox.a11y=Run Ant target: {0}"
    })
    private void initTargetComponent(final int row, final Target target) {
        // checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(target.isSelected());
        checkBox.setEnabled(target.isEnabled());
        Mnemonics.setLocalizedText(checkBox, target.getTitleWithMnemonic());
        checkBox.getAccessibleContext().setAccessibleDescription(Bundle.HudsonJobCreator_checkbox_a11y(target.getName()));
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                target.setSelected(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        // combo?
        List<String> options = target.getOptions();
        final JComboBox combo = options != null ? new JComboBox() : null;
        if (combo != null) {
            combo.setModel(new DefaultComboBoxModel(options.toArray(new String[options.size()])));
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    combo.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            combo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    target.setSelectedOption((String) combo.getSelectedItem());
                }
            });
            // preselect the 1st option
            combo.setSelectedIndex(0);
        }
        // placement
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (combo != null) {
            gridBagConstraints.insets = new Insets(3, 0, 0, 0);
        }
        add(checkBox, gridBagConstraints);
        if (combo != null) {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = row;
            gridBagConstraints.insets = new Insets(2, 2, 0, 5);
            add(combo, gridBagConstraints);
        }
    }

    private void finishLayout(int lastRow) {
        JLabel spaceHolder = new JLabel();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = lastRow + 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spaceHolder, gridBagConstraints);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // change in PHP Hudson Options
        changeSupport.fireChange();
    }

    //~ Inner classes

    @ServiceProvider(service=ProjectHudsonJobCreatorFactory.class, position=300)
    public static class Factory implements ProjectHudsonJobCreatorFactory {

        @Override
        public ProjectHudsonJobCreator forProject(Project project) {
            PhpModule phpModule = project.getLookup().lookup(PhpModule.class);
            if (phpModule == null) {
                // not a php project
                return null;
            }
            return HudsonJobCreator.forPhpModule(phpModule);
        }

    }

}
