/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.utils.helper.ErrorLevel;
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
                "org/netbeans/installer/wizard/components/panels/error.png";
        public static final String EMPTY_ICON =
                "org/netbeans/installer/wizard/components/panels/empty.png";
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        protected ErrorMessagePanel component;
        
        private Icon errorIcon;
        private Icon emptyIcon;
        
        private NbiLabel errorLabel;
        
        private ValidatingThread validatingThread;
        
        public ErrorMessagePanelSwingUi(
                final ErrorMessagePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            errorIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(ERROR_ICON));
            emptyIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(EMPTY_ICON));
            
            initComponents();
        }
        
        public void evaluateBackButtonClick() {
            if (validatingThread != null) {
                validatingThread.pause();
            }
            
            super.evaluateBackButtonClick();
        }
        
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
        
        protected void initialize() {
            updateErrorMessage();
            
            if (validatingThread == null) {
                validatingThread = new ValidatingThread(this);
                validatingThread.start();
            } else {
                validatingThread.play();
            }
        }
        
        protected synchronized void updateErrorMessage() {
            String errorMessage;
            
            try {
                errorMessage = validateInput();
                
                if (errorMessage == null) {
                    errorLabel.setIcon(emptyIcon);
                    errorLabel.setText(" ");
                    container.getNextButton().setEnabled(true);
                    return;
                }
            } catch (Exception e) {
                ErrorManager.notifyDebug("Failed to verify input", e);
                errorMessage = "Unknown error: " + e.getMessage();
            }
            
            errorLabel.setIcon(errorIcon);
            errorLabel.setText(errorMessage);
            container.getNextButton().setEnabled(false);
        }
        
        private void initComponents() {
            errorLabel = new NbiLabel();
            
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
        protected static class ValidatingThread extends NbiThread {
            /////////////////////////////////////////////////////////////////////////
            // Constants
            public static final long VALIDATION_DELAY = 10000;
            
            /////////////////////////////////////////////////////////////////////////
            // Instance
            private ErrorMessagePanelSwingUi swingUi;
            private boolean paused = false;
            
            public ValidatingThread(ErrorMessagePanelSwingUi swingUi) {
                super();
                
                this.swingUi = swingUi;
            }
            
            public void run() {
                while (true) {
                    if (!paused) {
                        swingUi.updateErrorMessage();
                    }
                    
                    try {
                        sleep(VALIDATION_DELAY);
                    } catch (InterruptedException e) {
                        ErrorManager.notify(ErrorLevel.DEBUG, e);
                    }
                }
            }
            
            public void pause() {
                paused = true;
            }
            
            public void play() {
                paused = false;
            }
        }
        
        protected static class ValidatingDocumentListener implements DocumentListener {
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
