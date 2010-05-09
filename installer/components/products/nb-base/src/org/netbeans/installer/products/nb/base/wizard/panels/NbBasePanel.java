/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.installer.products.nb.base.wizard.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.BrowserUtils;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.swing.NbiComboBox;
import org.netbeans.installer.utils.helper.swing.NbiDirectoryChooser;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationValidator;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxEditor;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxModel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelUi;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class NbBasePanel extends DestinationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JdkLocationPanel jdkLocationPanel;
    
    public NbBasePanel() {
        jdkLocationPanel = new JdkLocationPanel();
        
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
        
        setProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY,
                DEFAULT_JDK_LOCATION_LABEL_TEXT);
        setProperty(BROWSE_BUTTON_TEXT_PROPERTY,
                DEFAULT_BROWSE_BUTTON_TEXT);
        setProperty(WARNING_INSTALL_INTO_USERDIR_PROPERTY,
                DEFAULT_WARNING_INSTALL_INTO_USERDIR);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbBaseDestinationPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        jdkLocationPanel.setWizard(getWizard());
        
        //first, initialize the min and max values with the panel`s default
        //second, check if nbProduct has the properties set
        //third, check other nb- products if they have these properties set        
        String minVersionNbBase = getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY);
        String maxVersionNbBase = getProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY);
        String preferredVersion = getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY);
        String jreAllowedStr = getProperty(JdkLocationPanel.JRE_ALLOWED_PROPERTY);

        Version min = (minVersionNbBase != null) ? Version.getVersion(minVersionNbBase) : null;
        Version max = (maxVersionNbBase != null) ? Version.getVersion(maxVersionNbBase) : null;
        Version preferred = (preferredVersion != null) ? Version.getVersion(preferredVersion) : null;
        boolean jreAllowed = !"false".equals(jreAllowedStr); // if nothing defined - then true

        if (getWizard().getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY) != null) {
            min = Version.getVersion(getWizard().getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
        }
        if (getWizard().getProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY) != null) {
            max = Version.getVersion(getWizard().getProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY));
        }
        if (getWizard().getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY) != null) {
            preferred = Version.getVersion(getWizard().getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY));
        }
        if (getWizard().getProperty(JdkLocationPanel.JRE_ALLOWED_PROPERTY) != null) {
            jreAllowed = !"false".equals(getWizard().getProperty(JdkLocationPanel.JRE_ALLOWED_PROPERTY));
        }
        
        for (Product product : Registry.getInstance().getProductsToInstall()) {
            if (product.getUid().startsWith("nb-")) {
                jreAllowed &= !"false".equals(product.getProperty(JdkLocationPanel.JRE_ALLOWED_PROPERTY));

                String minVersionString = product.getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY);
                if (minVersionString != null) {
                    Version depMinVersion = Version.getVersion(minVersionString);
                    if (min == null || depMinVersion.newerThan(min)) {
                        min = depMinVersion;
                    }
                }
                String maxVersionString = product.getProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY);
                if (maxVersionString != null) {
                    Version depMaxVersion = Version.getVersion(maxVersionString);
                    if (min == null || depMaxVersion.olderThan(max)) {
                        max = depMaxVersion;
                    }
                }
            // do not check preferred version of the dependent nb product :
            // it is not clear how to handle that
            }
        }


        String finalMinVersion = (min == null) ? null : min.toString();
        String finalMaxVersion = (max == null) ? null : max.toString();
        String preferedVersion = (preferred == null) ? null : preferred.toString();
        String jreAllowedString = Boolean.toString(jreAllowed);

        if (finalMinVersion != null) {
            jdkLocationPanel.setProperty(
                    JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY,
                    finalMinVersion);
        }
        if (finalMaxVersion != null) {
            jdkLocationPanel.setProperty(
                    JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY,
                    finalMaxVersion);
        }
        if (preferedVersion != null) {
            jdkLocationPanel.setProperty(
                    JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY,
                    preferedVersion);
        }
        jdkLocationPanel.setProperty(
                JdkLocationPanel.JRE_ALLOWED_PROPERTY,
                jreAllowedString);
        
        jdkLocationPanel.initialize();
        
        //reinitialize labels which are different for cases of jdk and jre allowance
        setProperty(DESCRIPTION_PROPERTY, 
                jreAllowed ? DEFAULT_DESCRIPTION_JAVA : DEFAULT_DESCRIPTION);
        setProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY, 
                jreAllowed ? DEFAULT_JAVA_LOCATION_LABEL_TEXT : DEFAULT_JDK_LOCATION_LABEL_TEXT);
                
        //This makes it possible to perform silent installation with emptry state files 
        //that means that JDK_LOCATION_PROPERTY property is explicitely set to the first location
        //that fits the requirements
        //TODO: Investigate the prons&cons and side affects of moving
        //this code to the end of JdkLocationPanel.initialize() method        
        File jdkLocation = jdkLocationPanel.getSelectedLocation();        
        if(jdkLocation!=null && !jdkLocation.getPath().equals(StringUtils.EMPTY_STRING)) {
            jdkLocationPanel.setLocation(jdkLocation);
        }        
    }
    
    public JdkLocationPanel getJdkLocationPanel() {
        return jdkLocationPanel;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbBaseDestinationPanelUi extends DestinationPanelUi {
        protected NbBasePanel panel;
        
        public NbBaseDestinationPanelUi(NbBasePanel panel) {
            super(panel);
            
            
            this.panel = panel;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbBaseDestinationPanelSwingUi(panel, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbBaseDestinationPanelSwingUi extends DestinationPanelSwingUi {
        protected NbBasePanel panel;
        
        private NbiLabel jdkLocationLabel;
        private NbiComboBox jdkLocationComboBox;
        private NbiButton browseButton;
        private NbiTextPane statusLabel;
        
        private NbiTextField jdkLocationField;
        
        private NbiDirectoryChooser fileChooser;
        
        public NbBaseDestinationPanelSwingUi(
                final NbBasePanel panel,
                final SwingContainer container) {
            super(panel, container);
            
            this.panel = panel;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            jdkLocationLabel.setText(
                    panel.getProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY));
            
            final JdkLocationPanel jdkLocationPanel = panel.getJdkLocationPanel();
            
            if (jdkLocationPanel.getLocations().size() == 0) {
                final Version minVersion = Version.getVersion(jdkLocationPanel.getProperty(
                        JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
                final Version maxVersion = Version.getVersion(jdkLocationPanel.getProperty(
                        JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY));

                statusLabel.setContentType("text/html");
                statusLabel.setText(StringUtils.format(
                        jdkLocationPanel.getProperty(JdkLocationPanel.ERROR_NOTHING_FOUND_PROPERTY),
                        minVersion.toJdkStyle(),
                        maxVersion.toJdkStyle(),
                        jdkLocationPanel.getProperty(JdkLocationPanel.JAVA_DOWNLOAD_PAGE_PROPERTY)));

                statusLabel.addHyperlinkListener(BrowserUtils.createHyperlinkListener());
            } else {
                statusLabel.clearText();
                statusLabel.setVisible(false);
            }
            
            final List<File> jdkLocations = jdkLocationPanel.getLocations();                        
            final List<String> jdkLabels = jdkLocationPanel.getLabels();
            
            final LocationsComboBoxModel model = new LocationsComboBoxModel(
                    jdkLocations,
                    jdkLabels);            
            
            ((LocationsComboBoxEditor) jdkLocationComboBox.getEditor()).setModel(
                    model);
            jdkLocationComboBox.setModel(
                    model);
            
            final File selectedLocation = jdkLocationPanel.getSelectedLocation();
            final int index = jdkLocations.indexOf(selectedLocation);
            String selectedItem;
            if(index != -1) {
                  selectedItem = jdkLabels.get(index);  
            } else {
                  selectedItem = selectedLocation.toString();
            }  
            model.setSelectedItem(selectedItem);                        
            browseButton.setText(
                    panel.getProperty(BROWSE_BUTTON_TEXT_PROPERTY));
            
            super.initialize();
        }
        
        @Override
        protected void saveInput() {
            super.saveInput();
            
            panel.getJdkLocationPanel().setLocation(
                    new File(jdkLocationField.getText().trim()));
        }
        
        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();
            if (errorMessage == null) {
                errorMessage = panel.getJdkLocationPanel().validateLocation(
                        jdkLocationField.getText().trim());
            }
            
            return errorMessage;
        }

        @Override
        protected String getWarningMessage() {
            String warning = super.getWarningMessage();
            if (warning == null) {
                final String location = getDestinationField().getText().trim();
                final File f = FileUtils.eliminateRelativity(location);
                final File nbUserDirRoot = new File(SystemUtils.getUserHomeDirectory(), ".netbeans");
                if (f.equals(nbUserDirRoot) || FileUtils.isParent(nbUserDirRoot, f)) {
                    warning = StringUtils.format(panel.getProperty(
                            WARNING_INSTALL_INTO_USERDIR_PROPERTY),
                            nbUserDirRoot.getAbsolutePath());
                }
            }
            return warning;
        }


        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // selectedLocationField ////////////////////////////////////////////////
            jdkLocationField = new NbiTextField();
            jdkLocationField.getDocument().addDocumentListener(
                    new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
                
                public void removeUpdate(DocumentEvent e) {
                 //   updateErrorMessage();  
                }
                
                public void changedUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
            });
            
            // jdkLocationComboBox //////////////////////////////////////////////////
            final LocationValidator validator = new LocationValidator() {
                public void validate(String location) {
                    jdkLocationField.setText(location);
                }
            };
            
            jdkLocationComboBox = new NbiComboBox();
            jdkLocationComboBox.setEditable(true);
            jdkLocationComboBox.setEditor(new LocationsComboBoxEditor(validator));
            jdkLocationComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    final ComboBoxModel model = jdkLocationComboBox.getModel();
                    
                    if (model instanceof LocationsComboBoxModel) {
                        jdkLocationField.setText(
                                ((LocationsComboBoxModel) model).getLocation());
                    }
                }
            });
            
            // jdkLocationLabel /////////////////////////////////////////////////////
            jdkLocationLabel = new NbiLabel();
            jdkLocationLabel.setLabelFor(jdkLocationComboBox);
            
            // browseButton /////////////////////////////////////////////////////////
            browseButton = new NbiButton();
            browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    browseButtonPressed();
                }
            });
            
            // statusLabel //////////////////////////////////////////////////////////
            statusLabel = new NbiTextPane();            
            
            // fileChooser //////////////////////////////////////////////////////////
            fileChooser = new NbiDirectoryChooser();
            
            // this /////////////////////////////////////////////////////////////////
            add(jdkLocationLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(jdkLocationComboBox, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(browseButton, new GridBagConstraints(
                    1, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 4, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            add(statusLabel, new GridBagConstraints(
                    0, 4,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void browseButtonPressed() {
            fileChooser.setSelectedFile(new File(jdkLocationField.getText().trim()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jdkLocationComboBox.getModel().setSelectedItem(
                        fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String JDK_LOCATION_LABEL_TEXT_PROPERTY =
            "jdk.location.label.text"; // NOI18N
    public static final String BROWSE_BUTTON_TEXT_PROPERTY =
            "browse.button.text"; // NOI18N
    public static final String WARNING_INSTALL_INTO_USERDIR_PROPERTY =
            "install.into.userdir.storage";
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.description"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_JAVA =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.description.java"); // NOI18N
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.destination.button.text"); // NOI18N
    
    public static final String DEFAULT_JDK_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.jdk.location.label.text"); // NOI18N
    public static final String DEFAULT_JAVA_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.java.location.label.text"); // NOI18N
    public static final String DEFAULT_BROWSE_BUTTON_TEXT =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.browse.button.text"); // NOI18N
    
    public static final String DEFAULT_WARNING_INSTALL_INTO_USERDIR =
            ResourceUtils.getString(NbBasePanel.class,
            "NBP.warning.install.into.userdir"); // NOI18N
}
