/*
 * WizardPanel.java
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardPanel extends JPanel implements WizardComponent {
    private Wizard  wizard;
    private boolean active      = true;
    private boolean initialized = false;
    
    private List<WizardCondition> wizardConditions =
            new ArrayList<WizardCondition>();
    
    private Properties properties = new Properties();
    
    public final void executeComponent(Wizard aWizard) {
        wizard = aWizard;
        
        if (!initialized) {
            defaultInitComponents();
            initComponents();
            
            initialized = true;
        }
        
        defaultInitialize();
        initialize();
        
        getWizard().getWizardFrame().setWizardPanel(this);
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
        return wizard.getWizardFrame().getContentPane().getHelpButton();
    }
    
    public abstract void evaluateHelpButtonClick();
    
    public final JButton getBackButton() {
        return wizard.getWizardFrame().getContentPane().getBackButton();
    }
    
    public abstract void evaluateBackButtonClick();
    
    public final JButton getNextButton() {
        return wizard.getWizardFrame().getContentPane().getNextButton();
    }
    
    public abstract void evaluateNextButtonClick();
    
    public final JButton getCancelButton() {
        return wizard.getWizardFrame().getContentPane().getCancelButton();
    }
    
    public abstract void evaluateCancelButtonClick();
    
    public abstract JButton getDefaultButton();
    
    public final Wizard getWizard() {
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