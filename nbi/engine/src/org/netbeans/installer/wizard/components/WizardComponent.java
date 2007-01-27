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
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String TITLE_PROPERTY              = "title";
    public static final String DESCRIPTION_PROPERTY        = "description";
    public static final String HELP_BUTTON_TEXT_PROPERTY   = "help.button.text";
    public static final String BACK_BUTTON_TEXT_PROPERTY   = "back.button.text";
    public static final String NEXT_BUTTON_TEXT_PROPERTY   = "next.button.text";
    public static final String CANCEL_BUTTON_TEXT_PROPERTY = "cancel.button.text";
    public static final String FINISH_BUTTON_TEXT_PROPERTY = "finish.button.text";
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(WizardComponent.class, "WC.title");
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(WizardComponent.class, "WC.description");
    public static final String DEFAULT_HELP_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class, "WC.help.button.text");
    public static final String DEFAULT_BACK_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class, "WC.back.button.text");
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class, "WC.next.button.text");
    public static final String DEFAULT_CANCEL_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class, "WC.cancel.button.text");
    public static final String DEFAULT_FINISH_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class, "WC.finish.button.text");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Wizard wizard;
    
    private List<WizardComponent> components;
    private Properties            properties;
    
    protected WizardComponent() {
        components = new ArrayList<WizardComponent>();
        properties = new Properties();
        
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        
        setProperty(HELP_BUTTON_TEXT_PROPERTY, DEFAULT_HELP_BUTTON_TEXT);
        setProperty(BACK_BUTTON_TEXT_PROPERTY, DEFAULT_BACK_BUTTON_TEXT);
        setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT);
        setProperty(CANCEL_BUTTON_TEXT_PROPERTY, DEFAULT_CANCEL_BUTTON_TEXT);
        setProperty(FINISH_BUTTON_TEXT_PROPERTY, DEFAULT_FINISH_BUTTON_TEXT);
    }
    
    public abstract void executeForward();
    
    public abstract void executeBackward();
    
    public abstract void initialize();
    
    public abstract WizardUi getWizardUi();
    
    public boolean canExecuteForward() {
        return true;
    }
    
    public boolean canExecuteBackward() {
        return true;
    }
    
    public boolean isPointOfNoReturn() {
        return false;
    }
    
    // wizard ///////////////////////////////////////////////////////////////////////
    public final Wizard getWizard() {
        return wizard;
    }
    
    public final void setWizard(final Wizard wizard) {
        this.wizard = wizard;
    }
    
    // children /////////////////////////////////////////////////////////////////////
    public final void addChild(final WizardComponent component) {
        components.add(component);
    }
    
    public final void removeChild(final WizardComponent component) {
        components.remove(component);
    }
    
    public final void addChildren(final List<WizardComponent> components) {
        this.components.addAll(components);
    }
    
    public final List<WizardComponent> getChildren() {
        return components;
    }
    
    // properties ///////////////////////////////////////////////////////////////////
    public final String getProperty(final String name) {
        return getProperty(name, true);
    }
    
    public final String getRawProperty(final String name) {
        return getProperty(name, false);
    }
    
    public final void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }
    
    public final Properties getProperties() {
        return properties;
    }
    
    // helpers //////////////////////////////////////////////////////////////////////
    protected final String parseString(final String string) {
        return SystemUtils.parseString(string, wizard.getClassLoader());
    }
    
    protected final File parsePath(final String path) {
        return SystemUtils.parsePath(path, wizard.getClassLoader());
    }
    
    protected final String getString(final String baseName, final String key) {
        return ResourceUtils.getString(baseName, key, wizard.getClassLoader());
    }
    
    protected final String getString(final String baseName, final String key, final Object... arguments) {
        return ResourceUtils.getString(baseName, key, wizard.getClassLoader(), arguments);
    }
    
    protected final InputStream getResource(final String path) {
        return ResourceUtils.getResource(path, wizard.getClassLoader());
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private final String getProperty(final String name, final boolean parse) {
        String value = properties.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WizardComponentUi implements WizardUi {
        protected WizardComponent        component;
        protected WizardComponentSwingUi swingUi;
        
        public WizardComponentUi(final WizardComponent component) {
            this.component = component;
        }
        
        public SwingUi getSwingUi(final SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardComponentSwingUi(component, container);
            }
            
            swingUi.initializeContainer();
            swingUi.initialize();
            
            return swingUi;
        }
    }
    
    public static class WizardComponentSwingUi extends SwingUi {
        protected WizardComponent      component;
        protected SwingContainer container;
        
        public WizardComponentSwingUi(
                final WizardComponent component,
                final SwingContainer container) {
            this.component = component;
            this.container = container;
        }
        
        public boolean hasTitle() {
            return true;
        }
        
        public String getTitle() {
            return component.getProperty(TITLE_PROPERTY);
        }
        
        public String getDescription() {
            return component.getProperty(DESCRIPTION_PROPERTY);
        }
        
        public void evaluateHelpButtonClick() {
            // does nothing
        }
        
        public void evaluateBackButtonClick() {
            component.getWizard().previous();
        }
        
        public void evaluateNextButtonClick() {
            String errorMessage = validateInput();
            
            if (errorMessage == null) {
                saveInput();
                component.getWizard().next();
            } else {
                ErrorManager.notifyError(errorMessage);
            }
        }
        
        public void evaluateCancelButtonClick() {
            if (!UiUtils.showYesNoDialog("Cancel", "Are you sure you want to cancel?")) {
                return;
            }
            
            Installer.getInstance().cancel();
        }
        
        public NbiButton getDefaultButton() {
            return container.getNextButton();
        }
        
        protected void initializeContainer() {
            // set up the help button
            container.getHelpButton().setVisible(false);
            container.getHelpButton().setEnabled(false);
            
            container.getHelpButton().setText(
                    component.getProperty(HELP_BUTTON_TEXT_PROPERTY));
            
            // set up the back button
            container.getBackButton().setVisible(true);
            if (component.getWizard().hasPrevious()) {
                container.getBackButton().setEnabled(true);
            } else {
                container.getBackButton().setEnabled(false);
            }
            
            container.getBackButton().setText(
                    component.getProperty(BACK_BUTTON_TEXT_PROPERTY));
            
            // set up the next (or finish) button
            container.getNextButton().setVisible(true);
            container.getNextButton().setEnabled(true);
            
            if (component.getWizard().hasNext()) {
                container.getNextButton().setText(
                        component.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
            } else {
                container.getNextButton().setText(
                        component.getProperty(FINISH_BUTTON_TEXT_PROPERTY));
            }
            
            // set up the cancel button
            container.getCancelButton().setVisible(true);
            container.getCancelButton().setEnabled(true);
            
            container.getCancelButton().setText(
                    component.getProperty(CANCEL_BUTTON_TEXT_PROPERTY));
        }
        
        protected void initialize() {
            // does nothing
        }
        
        protected void saveInput() {
            // does nothing
        }
        
        protected String validateInput() {
            return null; // null means that everything is OK
        }
    }
}