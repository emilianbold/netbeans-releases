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
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.wizard.SwingUi;
import org.netbeans.installer.wizard.WizardUi;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.WizardContainerSwing;

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
        
        public SwingUi getSwingUi(WizardContainerSwing container) {
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
                final WizardContainerSwing container) {
            super(component, container);
            
            this.component = component;
            
            errorIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(ERROR_ICON));
            emptyIcon = new ImageIcon(
                    getClass().getClassLoader().getResource(EMPTY_ICON));
            
            initComponents();
        }
        
        protected void initialize() {
            if (validatingThread == null) {
                validatingThread = new ValidatingThread(this);
                validatingThread.start();
            } else {
                validatingThread.play();
            }
        }
        
        public void evaluateNextButtonClick() {
            super.evaluateNextButtonClick();
            
            validatingThread.pause();
        }
        
        public void evaluateCancelButtonClick() {
            super.evaluateCancelButtonClick();
            
            validatingThread.pause();
        }
        
        public void evaluateBackButtonClick() {
            super.evaluateBackButtonClick();
            
            validatingThread.pause();
        }
        
        protected void updateErrorMessage() {
            String errorMessage = validateInput();
            
            if (errorMessage != null) {
                errorLabel.setIcon(errorIcon);
                errorLabel.setText(errorMessage);
                container.getNextButton().setEnabled(false);
            } else {
                errorLabel.setIcon(emptyIcon);
                errorLabel.setText(" ");
                container.getNextButton().setEnabled(true);
            }
        }
        
        private void initComponents() {
            errorLabel = new NbiLabel();
            
            add(errorLabel, new GridBagConstraints(
                    0, 99,                             // x, y
                    99, 1,                             // width, height
                    1.0, 0.0,                          // weight-x, weight-y
                    GridBagConstraints.CENTER,         // anchor
                    GridBagConstraints.HORIZONTAL,     // fill
                    new Insets(7, 11, 11, 11),         // padding
                    0, 0));                            // ??? (padx, pady)
        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Inner Classes
        private static class ValidatingThread extends Thread {
            /////////////////////////////////////////////////////////////////////////
            // Constants
            public static final long VALIDATION_DELAY = 1000;
            
            /////////////////////////////////////////////////////////////////////////
            // Instance
            private ErrorMessagePanelSwingUi swingUi;
            private boolean paused = false;
            
            public ValidatingThread(ErrorMessagePanelSwingUi swingUi) {
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
    }
}
