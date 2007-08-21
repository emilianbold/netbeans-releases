/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.swing.NbiDirectoryChooser;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class DestinationPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public DestinationPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
        setProperty(ERROR_NULL_PROPERTY,
                DEFAULT_ERROR_NULL);
        setProperty(ERROR_NOT_VALID_PROPERTY,
                DEFAULT_ERROR_NOT_VALID);
        setProperty(ERROR_CONTAINS_EXCLAMATION_PROPERTY,
                DEFAULT_ERROR_CONTAINS_EXCLAMATION);
        setProperty(ERROR_CANNOT_CANONIZE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CANONIZE);
        setProperty(ERROR_NOT_ABSOLUTE_PROPERTY,
                DEFAULT_ERROR_NOT_ABSOLUTE);
        setProperty(ERROR_NOT_DIRECTORY_PROPERTY,
                DEFAULT_ERROR_NOT_DIRECTORY);
        setProperty(ERROR_NOT_READABLE_PROPERTY,
                DEFAULT_ERROR_NOT_READABLE);
        setProperty(ERROR_NOT_WRITABLE_PROPERTY,
                DEFAULT_ERROR_NOT_WRITABLE);
        setProperty(ERROR_NOT_EMPTY_PROPERTY,
                DEFAULT_ERROR_NOT_EMPTY);
        setProperty(ERROR_NOT_ENDS_WITH_APP_PROPERTY,
                DEFAULT_ERROR_NOT_ENDS_WITH_APP);
        setProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY,
                DEFAULT_ERROR_NOT_ENOUGH_SPACE);
        setProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY,
                DEFAULT_ERROR_CANNOT_GET_LOGIC);
        setProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CHECK_SPACE);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new DestinationPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class DestinationPanelUi extends ErrorMessagePanelUi {
        protected DestinationPanel        component;
        
        public DestinationPanelUi(DestinationPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new DestinationPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class DestinationPanelSwingUi extends ErrorMessagePanelSwingUi {
        protected DestinationPanel component;
        
        private NbiLabel destinationLabel;
        private NbiTextField destinationField;
        private NbiButton destinationButton;
        
        private NbiPanel spacerPanel;
        
        private NbiDirectoryChooser fileChooser;
        
        public DestinationPanelSwingUi(
                final DestinationPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        @Override
        public JComponent getDefaultFocusOwner() {
            return destinationField;
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            destinationLabel.setText(
                    component.getProperty(DESTINATION_LABEL_TEXT_PROPERTY));
            destinationButton.setText(
                    component.getProperty(DESTINATION_BUTTON_TEXT_PROPERTY));
            
            final Product product = (Product) component.
                    getWizard().
                    getContext().
                    get(Product.class);
            
            String destination = null;
            
            destination = product.
                    getProperty(Product.INSTALLATION_LOCATION_PROPERTY);
            
            if (destination == null) {
                destination = DEFAULT_DESTINATION;
            }
            
            destination = component.resolvePath(destination).getAbsolutePath();
            
            try {
                if (SystemUtils.isMacOS() && (
                        product.getLogic().wrapForMacOs() ||
                        product.getLogic().requireDotAppForMacOs())) {
                    if (!destination.endsWith(APP_SUFFIX)) {
                        final File parent = new File(destination).getParentFile();
                        final String suffix = product.getDisplayName() + APP_SUFFIX;
                        
                        if (parent != null) {
                            destination = new File(
                                    parent,
                                    suffix).getAbsolutePath();
                        } else {
                            destination = new File(
                                    destination,
                                    suffix).getAbsolutePath();
                        }
                    }
                }
            } catch (InitializationException e) {
                ErrorManager.notifyError(
                        component.getProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY),
                        e);
            }
            
            destinationField.setText(destination);
            
            super.initialize();
        }
        
        @Override
        protected void saveInput() {
            try {
                String value = destinationField.getText().trim();
                value = FileUtils.eliminateRelativity(value).getCanonicalPath();
                
                component.getWizard().setProperty(
                        Product.INSTALLATION_LOCATION_PROPERTY,
                        value);
            } catch (IOException e) {
                ErrorManager.notifyError(
                        component.getProperty(ERROR_CANNOT_CANONIZE_PROPERTY),
                        e);
            }
        }
        
        @Override
        protected String validateInput() {
            final String string = destinationField.getText().trim();
            final Product product = (Product) component.
                    getWizard().
                    getContext().
                    get(Product.class);
            
            try {
                if (string.equals(StringUtils.EMPTY_STRING)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NULL_PROPERTY),
                            string);
                }
                
                File file = FileUtils.eliminateRelativity(string);
                
                String filePath = file.getAbsolutePath();
                if (filePath.length() > 45) {
                    filePath = filePath.substring(0, 45) + "...";
                }
                
                if (!SystemUtils.isPathValid(file.getAbsolutePath())) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_VALID_PROPERTY),
                            filePath);
                }
                
                if (product.getLogic().prohibitExclamation() &&
                        file.getAbsolutePath().contains("!")) {
                    return StringUtils.format(
                            component.getProperty(ERROR_CONTAINS_EXCLAMATION_PROPERTY),
                            filePath);
                }
                
                if (!file.equals(file.getAbsoluteFile())) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_ABSOLUTE_PROPERTY),
                            file.getPath());
                }
                
                try {
                    file = file.getCanonicalFile();
                } catch (IOException e) {
                    return StringUtils.format(
                            component.getProperty(ERROR_CANNOT_CANONIZE_PROPERTY),
                            filePath);
                }
                
                filePath = file.getAbsolutePath();
                if (filePath.length() > 45) {
                    filePath = filePath.substring(0, 45) + "...";
                }
                
                if (file.exists() && !file.isDirectory()) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_DIRECTORY_PROPERTY),
                            filePath);
                }
                
                if (!FileUtils.canRead(file)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_READABLE_PROPERTY),
                            filePath);
                }
                
                if (!FileUtils.canWrite(file)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_WRITABLE_PROPERTY),
                            filePath);
                }
                
                if (!FileUtils.isEmpty(file)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_EMPTY_PROPERTY),
                            filePath);
                }
                
                if (SystemUtils.isMacOS() && (
                        product.getLogic().wrapForMacOs() ||
                        product.getLogic().requireDotAppForMacOs()) &&
                        !file.getAbsolutePath().endsWith(APP_SUFFIX)) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_ENDS_WITH_APP_PROPERTY),
                            filePath);
                }
                
                final long requiredSize =
                        product.getRequiredDiskSpace() + REQUIRED_SPACE_ADDITION;
                final long availableSize =
                        SystemUtils.getFreeSpace(file);
                if (availableSize < requiredSize) {
                    return StringUtils.format(
                            component.getProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY),
                            filePath,
                            StringUtils.formatSize(requiredSize - availableSize));
                }
            } catch (InitializationException e) {
                ErrorManager.notifyError(component.getProperty(
                        ERROR_CANNOT_GET_LOGIC_PROPERTY), e);
            } catch (NativeException e) {
                ErrorManager.notifyError(component.getProperty(
                        ERROR_CANNOT_CHECK_SPACE_PROPERTY), e);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // destinationField /////////////////////////////////////////////////////
            destinationField = new NbiTextField();
            destinationField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
                public void insertUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
                public void removeUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
            });
            
            // destinationLabel /////////////////////////////////////////////////////
            destinationLabel = new NbiLabel();
            destinationLabel.setLabelFor(destinationField);
            
            // destinationButton ////////////////////////////////////////////////////
            destinationButton = new NbiButton();
            destinationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    browseButtonPressed();
                }
            });
            
            // fileChooser //////////////////////////////////////////////////////////
            fileChooser = new NbiDirectoryChooser();
            
            // spacerPanel //////////////////////////////////////////////////////////
            spacerPanel = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(destinationLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(destinationField, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(destinationButton, new GridBagConstraints(
                    1, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(4, 4, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            add(spacerPanel, new GridBagConstraints(
                    1, 50,                            // x, y
                    2, 1,                             // width, height
                    0.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void browseButtonPressed() {
            final Product product = (Product) component.
                    getWizard().
                    getContext().
                    get(Product.class);
            
            final File currentDestination = new File(destinationField.getText());
            
            fileChooser.setSelectedFile(currentDestination);
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String newDestination =
                        fileChooser.getSelectedFile().getAbsolutePath();
                
                try {
                    String suffix = currentDestination.getName();
                    
                    if (SystemUtils.isMacOS() && (
                            product.getLogic().wrapForMacOs() ||
                            product.getLogic().requireDotAppForMacOs())) {
                        if (!newDestination.endsWith(APP_SUFFIX) &&
                                !suffix.endsWith(APP_SUFFIX)) {
                            suffix += APP_SUFFIX;
                        }
                    }
                    newDestination = new File(
                            newDestination,
                            suffix).getAbsolutePath();
                    
                } catch (InitializationException e) {
                    ErrorManager.notifyError(component.getProperty(
                            ERROR_CANNOT_GET_LOGIC_PROPERTY), e);
                }
                
                destinationField.setText(newDestination);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.description"); // NOI18N
    
    public static final String DESTINATION_LABEL_TEXT_PROPERTY
            = "destination.label.text"; // NOI18N
    public static final String DESTINATION_BUTTON_TEXT_PROPERTY
            = "destination.button.text"; // NOI18N
    
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.destination.button.text"); // NOI18N
    
    
    public static final String ERROR_NULL_PROPERTY =
            "error.null"; // NOI18N
    public static final String ERROR_NOT_VALID_PROPERTY =
            "error.not.valid"; // NOI18N
    public static final String ERROR_CONTAINS_EXCLAMATION_PROPERTY =
            "error.contains.exclamation"; // NOI18N
    public static final String ERROR_NOT_ABSOLUTE_PROPERTY =
            "error.not.absolute"; // NOI18N
    public static final String ERROR_CANNOT_CANONIZE_PROPERTY =
            "error.cannot.canonize"; // NOI18N
    public static final String ERROR_NOT_DIRECTORY_PROPERTY =
            "error.not.directory"; // NOI18N
    public static final String ERROR_NOT_READABLE_PROPERTY =
            "error.not.readable"; // NOI18N
    public static final String ERROR_NOT_WRITABLE_PROPERTY =
            "error.not.writable"; // NOI18N
    public static final String ERROR_NOT_EMPTY_PROPERTY =
            "error.not.empty"; // NOI18N
    public static final String ERROR_NOT_ENDS_WITH_APP_PROPERTY =
            "error.not.ends.with.app"; // NOI18N
    public static final String ERROR_NOT_ENOUGH_SPACE_PROPERTY =
            "error.not.enough.space"; // NOI18N
    public static final String ERROR_CANNOT_GET_LOGIC_PROPERTY =
            "error.cannot.get.logic";//NOI18N
    public static final String ERROR_CANNOT_CHECK_SPACE_PROPERTY =
            "error.cannot.check.space"; // NOI18N
    
    public static final String DEFAULT_ERROR_NULL =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.null"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_VALID =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.valid"); // NOI18N
    public static final String DEFAULT_ERROR_CONTAINS_EXCLAMATION =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.contains.exclamation"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_ABSOLUTE =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.absolute"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CANONIZE =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.cannot.canonize"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_DIRECTORY =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.directory"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_READABLE =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.readable"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_WRITABLE =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.writable"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_EMPTY =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.empty"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_ENDS_WITH_APP =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.ends.with.app"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_ENOUGH_SPACE =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.not.enough.space"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_GET_LOGIC =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.error.cannot.get.logic"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "DP.error.cannot.check.space"); // NOI18N
    
    public static final String DEFAULT_DESTINATION =
            ResourceUtils.getString(DestinationPanel.class,
            "DP.default.destination"); // NOI18N
    
    public static final String APP_SUFFIX =
            ".app"; // NOI18N
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
