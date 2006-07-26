/*
 * WizardComponent.java
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components;

import java.util.Properties;
import org.netbeans.installer.wizard.*;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public interface WizardComponent {
    public abstract void executeComponent(Wizard aWizard);
    
    public abstract void addChildComponent(WizardComponent aWizardComponent);
    
    public abstract boolean evaluateConditions();
    
    public abstract void addCondition(WizardCondition condition);
    
    public abstract boolean isActive();
    
    public abstract void setActive(boolean isActive); 
    
    public abstract String getProperty(String name);
    
    public abstract void setProperty(String name, String value);
    
    public abstract Properties getProperties();
}