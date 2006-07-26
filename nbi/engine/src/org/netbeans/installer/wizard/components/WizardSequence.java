/*
 * WizardSequence.java
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public class WizardSequence implements WizardComponent {
    private boolean active = true;
    
    private List<WizardComponent> wizardComponents = 
            new ArrayList<WizardComponent>();
    
    private List<WizardCondition> wizardConditions = 
            new ArrayList<WizardCondition>();
    
    private Properties properties = new Properties();
    
    public void executeComponent(Wizard wizard) {
        wizard.createSubWizard(wizardComponents).start();
    }
    
    public void addChildComponent(WizardComponent aWizardComponent) {
        wizardComponents.add(aWizardComponent);
    }
    
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
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean isActive) {
        active = isActive;
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