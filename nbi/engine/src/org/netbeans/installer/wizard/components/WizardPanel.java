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
import javax.swing.JPanel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;

import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardPanel extends JPanel implements WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Wizard  wizard;
    private boolean    initialized = false;
    
    private List<WizardCondition> conditions = new ArrayList<WizardCondition>();
    private Properties properties = new Properties();
    
    // WizardComponent implementation ///////////////////////////////////////////////
    public final void executeForward(final Wizard wizard) {
        executeComponent(wizard);
    }
    
    public final void executeBackward(final Wizard wizard) {
        executeComponent(wizard);
    }
    
    public final void executeBlocking(final Wizard wizard) {
        executeForward(wizard);
    }
    
    public final void executeSilently(final Wizard wizard) {
        wizard.next();
    }
    
    public final void addChild(WizardComponent component) {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final void removeChild(WizardComponent component) {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final List<WizardComponent> getChildren() {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final boolean evaluateConditions() {
        for (WizardCondition condition: conditions) {
            if (condition.evaluate() == false) {
                return false;
            }
        }
        
        return true;
    }
    
    public final void addCondition(final WizardCondition condition) {
        conditions.add(condition);
    }
    
    public final void removeCondition(final WizardCondition condition) {
        conditions.remove(condition);
    }
    
    public final List<WizardCondition> getConditions() {
        return conditions;
    }
    
    public boolean canExecuteForward() {
        return true;
    }
    
    public boolean canExecuteBackward() {
        return true;
    }
    
    public boolean isPointOfNoReturn() {
        return false;
    }
    
    public final String getProperty(final String name) {
        return getProperty(name, true);
    }
    
    public final void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }
    
    public final Properties getProperties() {
        return properties;
    }
    
    // abstract methods - to be overridden by subclasses ////////////////////////////
    protected abstract void initialize();
    
    protected abstract void initComponents();
    
    protected abstract void defaultInitialize();
    
    protected abstract void defaultInitComponents();
    
    public abstract String getDialogTitle();
    
    public abstract void evaluateHelpButtonClick();
    
    public abstract void evaluateBackButtonClick();
    
    public abstract void evaluateNextButtonClick();
    
    public abstract void evaluateCancelButtonClick();
    
    public abstract NbiButton getDefaultButton();
    
    /////////////////////////////////////////////////////////////////////////////////
    private final void executeComponent(final Wizard wizard) {
        this.wizard = wizard;
        
        if (!initialized) {
            defaultInitComponents();
            initComponents();
            
            initialized = true;
        }
        
        defaultInitialize();
        initialize();
        
        getWizard().getFrame().setWizardPanel(this);
    }
    
    protected final Wizard getWizard() {
        return wizard;
    }
    
    protected final NbiButton getHelpButton() {
        return getWizard().getFrame().getContentPane().getHelpButton();
    }
    
    protected final NbiButton getBackButton() {
        return wizard.getFrame().getContentPane().getBackButton();
    }
    
    protected final NbiButton getNextButton() {
        return wizard.getFrame().getContentPane().getNextButton();
    }
    
    protected final NbiButton getCancelButton() {
        return wizard.getFrame().getContentPane().getCancelButton();
    }
    
    // helper methods for working with properties ///////////////////////////////////
    protected final String getProperty(final String name, final boolean parse) {
        String value = properties.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    // helper methods for SystemUtils and ResourceUtils /////////////////////////////
    protected final String parseString(final String string) {
        return systemUtils.parseString(string, getCorrectClassLoader());
    }
    
    protected final File parsePath(final String path) {
        return systemUtils.parsePath(path, getCorrectClassLoader());
    }
    
    protected final String getString(final String baseName, final String key) {
        return resourceUtils.getString(baseName, key, getCorrectClassLoader());
    }
    
    protected final String getString(final String baseName, final String key, final Object... arguments) {
        return resourceUtils.getString(baseName, key, getCorrectClassLoader(), arguments);
    }
    
    protected final InputStream getResource(final String path) {
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