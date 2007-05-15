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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class ErrorMessagePanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ErrorMessagePanel() {
        // does nothing
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new ErrorMessagePanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ErrorMessagePanelUi extends WizardPanelUi {
        protected ErrorMessagePanel        component;
        
        public ErrorMessagePanelUi(ErrorMessagePanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new ErrorMessagePanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class ErrorMessagePanelSwingUi extends WizardPanelSwingUi {
        /////////////////////////////////////////////////////////////////////////////
        // Constants
        public static final String ERROR_ICON =
                "org/netbeans/installer/wizard/components/panels/error.png"; // NOI18N
        public static final String WARNING_ICON =
                "org/netbeans/installer/wizard/components/panels/warning.png"; // NOI18N
        public static final String INFO_ICON =
                "org/netbeans/installer/wizard/components/panels/info.png"; // NOI18N
        public static final String EMPTY_ICON =
                "org/netbeans/installer/wizard/components/panels/empty.png"; // NOI18N
        
        public static final Color ERROR_COLOR = 
                Color.BLACK;
        public static final Color WARNING_COLOR = 
                Color.BLACK;
        public static final Color INFO_COLOR = 
                Color.BLACK;
        public static final Color EMPTY_COLOR = 
                Color.BLACK;
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        protected ErrorMessagePanel component;
        
        private Icon errorIcon;
        private Icon warningIcon;
        private Icon infoIcon;
        private Icon emptyIcon;
        
        private Color errorColor;
        private Color warningColor;
        private Color infoColor;
        private Color emptyColor;
        
        private NbiLabel errorLabel;
        
        private ValidatingThread validatingThread;
        
        public ErrorMessagePanelSwingUi(
                final ErrorMessagePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            errorIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(ERROR_ICON));
            warningIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(WARNING_ICON));
            infoIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(INFO_ICON));
            emptyIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(EMPTY_ICON));
            
            errorColor = 
                    ERROR_COLOR;
            warningColor = 
                    WARNING_COLOR;
            infoColor = 
                    INFO_COLOR;
            emptyColor = 
                    EMPTY_COLOR;
            
            initComponents();
        }
        
        @Override
        public void evaluateBackButtonClick() {
            if (validatingThread != null) {
                validatingThread.pause();
            }
            
            super.evaluateBackButtonClick();
        }
        
        @Override
        public void evaluateNextButtonClick() {
            if (validatingThread != null) {
                validatingThread.pause();
            }
            
            final String errorMessage = validateInput();
            
            if (errorMessage == null) {
                saveInput();
                component.getWizard().next();
            } else {
                ErrorManager.notifyError(errorMessage);
                if (validatingThread != null) {
                    validatingThread.play();
                }
            }
        }
        
        @Override
        public void evaluateCancelButtonClick() {
            if (validatingThread != null) {
                validatingThread.pause();
            }
            
            if (!UiUtils.showYesNoDialog(
                    "Cancel",
                    "Are you sure you want to cancel?")) {
                if (validatingThread != null) {
                    validatingThread.play();
                }
                return;
            }
            
            component.getWizard().getFinishHandler().cancel();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            updateErrorMessage();
            
            if (validatingThread == null) {
                validatingThread = new ValidatingThread(this);
                validatingThread.start();
            } else {
                validatingThread.play();
            }
        }
        
        protected String getWarningMessage() {
            return null;
        }
        
        protected String getInformationalMessage() {
            return null;
        }
        
        protected synchronized final void updateErrorMessage() {
            String message;
            
            try {
                message = validateInput();
                if (message != null) {
                    errorLabel.setIcon(errorIcon);
                    errorLabel.setText(message);
                    errorLabel.setForeground(errorColor);
                    container.getNextButton().setEnabled(false);
                    
                    return;
                }
                
                message = getWarningMessage();
                if (message != null) {
                    errorLabel.setIcon(warningIcon);
                    errorLabel.setText(message);
                    errorLabel.setForeground(warningColor);
                    container.getNextButton().setEnabled(true);
                    
                    return;
                }
                
                message = getInformationalMessage();
                if (message != null) {
                    errorLabel.setIcon(infoIcon);
                    errorLabel.setText(message);
                    errorLabel.setForeground(infoColor);
                    container.getNextButton().setEnabled(true);
                    
                    return;
                }
                
                errorLabel.setIcon(emptyIcon);
                errorLabel.clearText();
                errorLabel.setForeground(emptyColor);
                container.getNextButton().setEnabled(true);
            } catch (Exception e) {
                // we have a good reason to catch Exception here, as most of the
                // code that is called is not under the engine's control
                // (validateInput() is component-specific) and we do not want to
                // propagate unexpected exceptions that could otherwise be handled
                // normally
                
                ErrorManager.notifyError("Failed to verify input", e);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // errorLabel ///////////////////////////////////////////////////////////
            errorLabel = new NbiLabel();
            errorLabel.setFocusable(true);
            
            // this /////////////////////////////////////////////////////////////////
            add(errorLabel, new GridBagConstraints(
                    0, 99,                             // x, y
                    99, 1,                             // width, height
                    1.0, 0.0,                          // weight-x, weight-y
                    GridBagConstraints.CENTER,         // anchor
                    GridBagConstraints.HORIZONTAL,     // fill
                    new Insets(11, 11, 11, 11),        // padding
                    0, 0));                            // ??? (padx, pady)
        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Inner Classes
        public static class ValidatingThread extends NbiThread {
            /////////////////////////////////////////////////////////////////////////
            // Instance
            private ErrorMessagePanelSwingUi swingUi;
            private boolean paused;
            
            public ValidatingThread(final ErrorMessagePanelSwingUi swingUi) {
                super();
                
                this.swingUi = swingUi;
                this.paused = false;
            }
            
            public void run() {
                while (true) {
                    if (!paused) {
                        swingUi.updateErrorMessage();
                    }
                    
                    try {
                        sleep(VALIDATION_DELAY);
                    } catch (InterruptedException e) {
                        ErrorManager.notifyDebug("Interrupted", e);
                    }
                }
            }
            
            public void pause() {
                paused = true;
            }
            
            public void play() {
                paused = false;
            }
            
            /////////////////////////////////////////////////////////////////////////
            // Constants
            public static final long VALIDATION_DELAY = 2000;
        }
        
        public static class ValidatingDocumentListener implements DocumentListener {
            /////////////////////////////////////////////////////////////////////////
            // Instance
            private ErrorMessagePanelSwingUi swingUi;
            
            public ValidatingDocumentListener(ErrorMessagePanelSwingUi swingUi) {
                this.swingUi = swingUi;
            }
            
            public void insertUpdate(DocumentEvent event) {
                swingUi.updateErrorMessage();
            }
            
            public void removeUpdate(DocumentEvent event) {
                swingUi.updateErrorMessage();
            }
            
            public void changedUpdate(DocumentEvent event) {
                swingUi.updateErrorMessage();
            }
        }
    }
}
