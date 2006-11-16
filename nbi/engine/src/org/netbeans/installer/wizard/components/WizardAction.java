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
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.conditions.TrueCondition;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardAction implements WizardComponent {
    private Wizard          wizard     = null;
    
    private WizardCondition condition  = new TrueCondition();
    private Properties      properties = new Properties();
    
    protected boolean       finished   = false;
    protected boolean       canceled   = false;
    
    public final void executeForward(final Wizard wizard) {
        Thread worker = new Thread() {
            public void run() {
                executeComponent(wizard, true);
                if (!canceled) {
                    wizard.next();
                }
            }
        };
        worker.start();
    }
    
    public final void executeBackward(final Wizard wizard) {
        Thread worker = new Thread() {
            public void run() {
                executeComponent(wizard, true);
                if (!canceled) {
                    wizard.previous();
                }
            }
        };
        worker.start();
    }
    
    public final void executeBlocking(final Wizard wizard) {
        executeComponent(wizard, true);
    }
    
    public final void executeSilently(final Wizard wizard) {
        executeComponent(wizard, false);
        wizard.next();
    }
    
    public final void executeSilentlyBlocking(final Wizard wizard) {
        executeComponent(wizard, false);
    }
    
    public final void addChild(WizardComponent component) {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final void removeChild(WizardComponent component) {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final void addChildren(List<WizardComponent> component) {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final List<WizardComponent> getChildren() {
        throw new UnsupportedOperationException(
                "This component does not support child components");
    }
    
    public final void setCondition(final WizardCondition condition) {
        this.condition = condition;
    }
    
    public final WizardCondition getCondition() {
        return condition;
    }
    
    public boolean canExecuteForward() {
        return true;
    }
    
    public boolean canExecuteBackward() {
        return false;
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
    public abstract void execute();
    
    public abstract WizardPanel getUI();
    
    public void cancel() {
        canceled = true;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    private void executeComponent(final Wizard wizard, final boolean showUi) {
        this.wizard = wizard;
        
        finished = false;
        
        // first initialize and show the UI
        if (showUi) {
            WizardPanel ui = getUI();
            if (ui != null) {
                for (Object key: getProperties().keySet()) {
                    ui.getProperties().put(key, getProperties().get(key));
                }
                
                ui.executeForward(wizard);
            }
        }
        execute();
        
        finished = true;
    }
    
    protected final Wizard getWizard() {
        return wizard;
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
        return SystemUtils.parseString(string, getCorrectClassLoader());
    }
    
    protected final File parsePath(final String path) {
        return SystemUtils.parsePath(path, getCorrectClassLoader());
    }
    
    protected final String getString(final String baseName, final String key) {
        return ResourceUtils.getString(baseName, key, getCorrectClassLoader());
    }
    
    protected final String getString(final String baseName, final String key, final Object... arguments) {
        return ResourceUtils.getString(baseName, key, getCorrectClassLoader(), arguments);
    }
    
    protected final InputStream getResource(final String path) {
        return ResourceUtils.getResource(path, getCorrectClassLoader());
    }
    
    // private stuff ////////////////////////////////////////////////////////////////
    private final ClassLoader getCorrectClassLoader() {
        if (wizard.getProductComponent() != null) {
            return wizard.getProductComponent().getClassLoader();
        } else {
            return getClass().getClassLoader();
        }
    }
}