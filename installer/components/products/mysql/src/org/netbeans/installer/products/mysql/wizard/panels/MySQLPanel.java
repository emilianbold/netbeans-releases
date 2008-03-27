/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.products.mysql.wizard.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiComboBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelSwingUi;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelUi;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Dmitry Lipin
 */
public class MySQLPanel extends DestinationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public MySQLPanel() {

        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);

        setProperty(SYSTEM_TYPE_LABEL_TEXT_PROPERTY,
                DEFAULT_SYSTEM_TYPE_LABEL_TEXT);


    }

    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new MySQLPanelUi(this);
        }

        return wizardUi;
    }

    @Override
    public void initialize() {
        super.initialize();
        getWizard().setProperty(SYSTEM_TYPE_PROPERTY, 
                SystemType.SMALL.getShortName());
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class MySQLPanelUi extends DestinationPanelUi {

        protected MySQLPanel component;

        public MySQLPanelUi(MySQLPanel component) {
            super(component);

            this.component = component;
        }
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new MySQLPanelSwingUi(component, container);
            }

            return super.getSwingUi(container);
        }
    }

    public static class MySQLPanelSwingUi extends DestinationPanelSwingUi {

        protected MySQLPanel panel;
        private NbiComboBox systemTypeComboBox;
        private NbiPanel containerPanel;
        private NbiLabel systemTypeLabel;

        public MySQLPanelSwingUi(
                final MySQLPanel panel,
                final SwingContainer container) {
            super(panel, container);

            this.panel = panel;

            initComponents();
        }

        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            super.initialize();
            systemTypeLabel.setText(
                    panel.getProperty(SYSTEM_TYPE_LABEL_TEXT_PROPERTY));
            systemTypeComboBox.setEditable(false);

            SystemType[] values = SystemType.values();

            systemTypeComboBox.setModel(new DefaultComboBoxModel(values));
        }

        @Override
        protected void saveInput() {
            super.saveInput();
            Object obj = systemTypeComboBox.getSelectedItem();
            LogManager.log("... selected item in MySQL panel: " + obj);
            if (obj != null && obj instanceof SystemType) {
                SystemType t = (SystemType) obj;
                LogManager.log("... setting product property");
                panel.getWizard().setProperty(SYSTEM_TYPE_PROPERTY,
                        t.getShortName());
            }
        }

        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();


            return errorMessage;
        }

        @Override
        protected String getWarningMessage() {
            return null;
        }

        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            containerPanel = new NbiPanel();
            systemTypeLabel = new NbiLabel();
            systemTypeComboBox = new NbiComboBox();
            systemTypeLabel.setLabelFor(systemTypeComboBox);
            // containerPanel ///////////////////////////////////////////////////////
            containerPanel.add(systemTypeLabel, new GridBagConstraints(
                    0, 0, // x, y
                    1, 1, // width, height
                    0.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 11, 0, 0), // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(systemTypeComboBox, new GridBagConstraints(
                    1, 0, // x, y
                    2, 1, // width, height
                    0.5, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(11, 6, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 0, // x, y
                    1, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.CENTER, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???

            add(containerPanel, new GridBagConstraints(
                    0, 3, // x, y
                    2, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.BOTH, // fill
                    new Insets(0, 0, 0, 0), // padding
                    0, 0));                           // padx, pady - ???

        }
    }
    
    public static enum SystemType {

        SMALL("small"),//NOI18N
        MEDIUM("medium"), //NOI18N
        LARGE("large"),//NOI18N
        HUGE("huge"),//NOI18N
        HEAVY("heavy");//NOI18N
        private String name;
        private final static String ext = SystemUtils.isWindows()? ".ini" : ".cnf"; //NOI18N
        private final static String dir = SystemUtils.isWindows()? "" : "support-files/"; //NOI18N
        SystemType(String s) {
            name = s;
        }

        public static SystemType getSystemType(String s) {
            for (SystemType type : values()) {
                if (type.getShortName().equals(s)) {
                    return type;
                }
            }
            return null;
        }
        
        public String getShortName() {
            return name;
        }

        public String getDisplayName() {
            switch (this) {
                case SMALL:
                    return SYSTEM_NAME_SMALL;
                case MEDIUM:
                    return SYSTEM_NAME_MEDIUM;
                case LARGE:
                    return SYSTEM_NAME_LARGE;
                case HUGE:
                    return SYSTEM_NAME_HUGE;
                case HEAVY:
                    return SYSTEM_NAME_HEAVY;
                default:
                    return "";
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        public String getTemplateFile() {
            switch (this) {
                case SMALL:
                    return dir + "my-small" + ext; //NOI18N
                case MEDIUM:
                    return dir + "my-medium" + ext; //NOI18N
                case LARGE:
                    return dir + "my-large" + ext; //NOI18N
                case HUGE:
                    return dir + "my-huge" + ext; //NOI18N
                case HEAVY:
                    return dir + "my-innodb-heavy-4G" + ext; //NOI18N
                default:
                    return "";
            }

        }
        }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.description"); // NOI18N
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.destination.button.text"); // NOI18N
    public static final String SYSTEM_TYPE_PROPERTY =
            "mysql.system.type";
    public static final String SYSTEM_NAME_SMALL =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.system.small");
    public static final String SYSTEM_NAME_MEDIUM =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.system.medium");
    public static final String SYSTEM_NAME_LARGE =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.system.large");
    public static final String SYSTEM_NAME_HUGE =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.system.huge");
    public static final String SYSTEM_NAME_HEAVY =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.system.heavy");
    public static final String SYSTEM_TYPE_LABEL_TEXT_PROPERTY =
            "system.type.label.text";
    public static final String DEFAULT_SYSTEM_TYPE_LABEL_TEXT =
            ResourceUtils.getString(MySQLPanel.class,
            "MSP.system.type.label.text");
    }
