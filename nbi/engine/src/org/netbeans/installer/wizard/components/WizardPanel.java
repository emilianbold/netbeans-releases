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
package org.netbeans.installer.wizard.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.netbeans.installer.wizard.SubWizard;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardPanel extends JPanel implements WizardComponent {
    private SubWizard  wizard;
    private boolean active      = true;
    private boolean initialized = false;
    
    private List<WizardCondition> wizardConditions =
            new ArrayList<WizardCondition>();
    
    private Properties properties = new Properties();
    
    public final void executeComponent(SubWizard aWizard) {
        wizard = aWizard;
        
        if (!initialized) {
            defaultInitComponents();
            initComponents();
            
            initialized = true;
        }
        
        defaultInitialize();
        initialize();
        
        getWizard().getFrame().setWizardPanel(this);
    }
    
    public abstract void initialize();
    
    public abstract void initComponents();
    
    public abstract void defaultInitialize();
    
    public abstract void defaultInitComponents();
    
    public boolean evaluateConditions() {
        for (WizardCondition condition: wizardConditions) {
            if (condition.evaluate() == false) {
                return false;
            }
        }
        
        return true;
    }
    
    public void addCondition(WizardCondition aCondition) {
        wizardConditions.add(aCondition);
    }
    
    public abstract String getDialogTitle();
    
    public final JButton getHelpButton() {
        return wizard.getFrame().getContentPane().getHelpButton();
    }
    
    public abstract void evaluateHelpButtonClick();
    
    public final JButton getBackButton() {
        return wizard.getFrame().getContentPane().getBackButton();
    }
    
    public abstract void evaluateBackButtonClick();
    
    public final JButton getNextButton() {
        return wizard.getFrame().getContentPane().getNextButton();
    }
    
    public abstract void evaluateNextButtonClick();
    
    public final JButton getCancelButton() {
        return wizard.getFrame().getContentPane().getCancelButton();
    }
    
    public abstract void evaluateCancelButtonClick();
    
    public abstract JButton getDefaultButton();
    
    public final SubWizard getWizard() {
        return wizard;
    }
    
    public final boolean isActive() {
        return active;
    }
    
    public final void setActive(boolean isActive) {
        active = isActive;
    }
    
    public void addChildComponent(WizardComponent aWizardComponent) {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public boolean isForwardOnly() {
        return false;
    }
    
    public boolean isBackwardOnly() {
        return false;
    }
    
    public boolean isPointOfNoReturn() {
        return false;
    }
    
    public final String getProperty(String name) {
        return properties.getProperty(name);
    }
    
    public final void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }
    
    public final Properties getProperties() {
        return properties;
    }
}