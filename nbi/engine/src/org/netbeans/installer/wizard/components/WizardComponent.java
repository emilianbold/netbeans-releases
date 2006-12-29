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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.border.LineBorder;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.wizard.SwingUi;
import org.netbeans.installer.wizard.containers.WizardContainerSwing;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.WizardUi;
import org.netbeans.installer.wizard.conditions.TrueCondition;
import org.netbeans.installer.wizard.conditions.WizardCondition;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DIALOG_TITLE_PROPERTY       = "dialog.title";
    public static final String HELP_BUTTON_TEXT_PROPERTY   = "help.button.text";
    public static final String BACK_BUTTON_TEXT_PROPERTY   = "back.button.text";
    public static final String NEXT_BUTTON_TEXT_PROPERTY   = "next.button.text";
    public static final String CANCEL_BUTTON_TEXT_PROPERTY = "cancel.button.text";
    public static final String FINISH_BUTTON_TEXT_PROPERTY = "finish.button.text";
    
    public static final String DEFAULT_DIALOG_TITLE =
            ResourceUtils.getString(WizardComponent.class, "WC.dialog.title");
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
    protected Wizard wizard;
    
    protected List<WizardComponent> components = new ArrayList<WizardComponent>();
    protected WizardCondition       condition  = new TrueCondition();
    protected Properties            properties = new Properties();
    
    protected WizardComponent() {
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
        
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
    
    protected final void removeAllChildren() {
        components.clear();
    }
    
    public final void setCondition(final WizardCondition condition) {
        this.condition = condition;
    }
    
    public final WizardCondition getCondition() {
        return condition;
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
    
    public final Wizard getWizard() {
        return wizard;
    }
    
    public final void setWizard(final Wizard wizard) {
        this.wizard = wizard;
    }
    
    // some helper methods //////////////////////////////////////////////////////////
    public final String getProperty(final String name, final boolean parse) {
        String value = properties.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    public final String parseString(final String string) {
        return SystemUtils.parseString(string, getCorrectClassLoader());
    }
    
    public final File parsePath(final String path) {
        return SystemUtils.parsePath(path, getCorrectClassLoader());
    }
    
    public final String getString(final String baseName, final String key) {
        return ResourceUtils.getString(baseName, key, getCorrectClassLoader());
    }
    
    public final String getString(final String baseName, final String key, final Object... arguments) {
        return ResourceUtils.getString(baseName, key, getCorrectClassLoader(), arguments);
    }
    
    public final InputStream getResource(final String path) {
        return ResourceUtils.getResource(path, getCorrectClassLoader());
    }
    
    public final ClassLoader getCorrectClassLoader() {
        if (getWizard().getProductComponent() != null) {
            return getWizard().getProductComponent().getClassLoader();
        } else {
            return getClass().getClassLoader();
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
        
        public SwingUi getSwingUi(final WizardContainerSwing container) {
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
        protected WizardContainerSwing container;
        
        public WizardComponentSwingUi(
                final WizardComponent component,
                final WizardContainerSwing container) {
            this.component = component;
            this.container = container;
            
            initComponents();
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
        
        public String getDialogTitle() {
            return component.getProperty(DIALOG_TITLE_PROPERTY);
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
                ErrorManager.notify(ErrorLevel.ERROR, errorMessage);
            }
        }
        
        public void evaluateCancelButtonClick() {
            if (!UiUtils.showYesNoDialog("Are you sure you want to cancel?")) {
                return;
            }
            
            Installer.getInstance().cancel();
        }
        
        public NbiButton getDefaultButton() {
            return container.getNextButton();
        }
        
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            Graphics2D graphics2d = (Graphics2D) graphics;
            
            Composite oldComposite = graphics2d.getComposite();
            
            graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            graphics2d.setColor(Color.WHITE);
            graphics2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            
            graphics2d.setComposite(oldComposite);
        }
        
        private void initComponents() {
            setBorder(new LineBorder(Color.BLACK, 1));
        }
    }
}