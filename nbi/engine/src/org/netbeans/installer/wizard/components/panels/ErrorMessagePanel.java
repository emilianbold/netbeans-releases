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
import javax.swing.SwingUtilities;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.swing.NbiLabel;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class ErrorMessagePanel extends DefaultWizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ERROR_ICON = "org/netbeans/installer/wizard/components/panels/error.png";
    public static final String EMPTY_ICON = "org/netbeans/installer/wizard/components/panels/empty.png";
    
    public static final long VALIDATION_DELAY = 1000;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Icon errorIcon = new ImageIcon(getClass().getClassLoader().getResource(ERROR_ICON));
    private Icon emptyIcon = new ImageIcon(getClass().getClassLoader().getResource(EMPTY_ICON));
    
    private ValidatingThread validatingThread = new ValidatingThread();
    private NbiLabel         errorLabel = null;
    
    public void defaultInitialize() {
        super.defaultInitialize();
        
        if (validatingThread.isAlive()) {
            validatingThread.play();
        } else {
            validatingThread.start();
        }
    }
    
    public void defaultInitComponents() {
        super.defaultInitComponents();
        
        errorLabel = new NbiLabel();
        
        add(errorLabel, new GridBagConstraints(0, 99, 99, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 11, 11), 0, 0));
    }
    
    public void evaluateNextButtonClick() {
        super.evaluateNextButtonClick();
        
        validatingThread.pause();
    }
    
    public void evaluateHelpButtonClick() {
        super.evaluateHelpButtonClick();
        
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
    
    public void updateErrorMessage() {
        String errorMessage = validateInput();
        
        if (errorMessage != null) {
            errorLabel.setIcon(errorIcon);
            errorLabel.setText(errorMessage);
            getNextButton().setEnabled(false);
        } else {
            errorLabel.setIcon(emptyIcon);
            errorLabel.setText(" ");
            getNextButton().setEnabled(true);
        }
    }
    
    public abstract String validateInput();
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class ValidatingThread extends Thread {
        private boolean paused = false;
        
        public void run() {
            while (true) {
                if (!paused) {
                    updateErrorMessage();
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
