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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.wizard.SubWizard;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public class WizardSequence implements WizardComponent {
    private SubWizard wizard;
    private boolean   active = true;
    
    private List<WizardComponent> wizardComponents = new ArrayList<WizardComponent>();
    
    private List<WizardCondition> wizardConditions = new ArrayList<WizardCondition>();
    
    private Properties properties = new Properties();
    
    public void executeComponent(final SubWizard wizard) {
        this.wizard = wizard;
        
        wizard.createSubWizard(wizardComponents).next();
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
    
    public SubWizard getWizard() {
        return wizard;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean isActive) {
        active = isActive;
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
        return getProperty(name, true);
    }
    
    public final String getProperty(String name, boolean parse) {
        String value = properties.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    public final void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }
    
    public final Properties getProperties() {
        return properties;
    }
    
    // helper methods for SystemUtils and ResourceUtils /////////////////////////////
    public String parseString(String string) {
        return systemUtils.parseString(string, getCorrectClassLoader());
    }
    
    public File parsePath(String path) {
        return systemUtils.parsePath(path, getCorrectClassLoader());
    }
    
    public String getString(String baseName, String key) {
        return resourceUtils.getString(baseName, key, getCorrectClassLoader());
    }
    
    public String getString(String baseName, String key, Object... arguments) {
        return resourceUtils.getString(baseName, key, getCorrectClassLoader(), arguments);
    }
    
    public InputStream getResource(String path) {
        return resourceUtils.getResource(path, getCorrectClassLoader());
    }
    
    // private stuff ////////////////////////////////////////////////////////////////
    private ClassLoader getCorrectClassLoader() {
        if (getWizard().getProductComponent() != null) {
            return getWizard().getProductComponent().getClassLoader();
        } else {
            return getClass().getClassLoader();
        }
    }
    
    // protected area ///////////////////////////////////////////////////////////////
    protected static final SystemUtils   systemUtils   = SystemUtils.getInstance();
    protected static final StringUtils   stringUtils   = StringUtils.getInstance();
    protected static final ResourceUtils resourceUtils = ResourceUtils.getInstance();
    protected static final FileUtils     fileUtils     = FileUtils.getInstance();
}