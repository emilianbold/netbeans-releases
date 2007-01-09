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
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class TextPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String TEXT_PROPERTY         = "text";
    public static final String CONTENT_TYPE_PROPERTY = "content.type";
    
    public static final String DEFAULT_TEXT =
            ResourceUtils.getString(TextPanel.class, "TP.text");
    public static final String DEFAULT_CONTENT_TYPE =
            ResourceUtils.getString(TextPanel.class, "TP.content.type");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public TextPanel() {
        setProperty(TEXT_PROPERTY, DEFAULT_TEXT);
        setProperty(CONTENT_TYPE_PROPERTY, DEFAULT_CONTENT_TYPE);
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new TextPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class TextPanelUi extends WizardPanelUi {
        protected TextPanel        component;
        
        public TextPanelUi(TextPanel component) {
            super(component);
            
            this.component = component;
        }
        
        // swing ui specific ////////////////////////////////////////////////////////
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new TextPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class TextPanelSwingUi extends WizardPanelSwingUi {
        protected TextPanel component;
        
        private NbiTextPane textPane;
        
        public TextPanelSwingUi(
                final TextPanel component, 
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        public boolean hasTitle() {
            return false;
        }
        
        protected void initialize() {
            textPane.setContentType(component.getProperty(CONTENT_TYPE_PROPERTY));
            textPane.setText(component.getProperty(TEXT_PROPERTY));
        }
        
        private void initComponents() {
            textPane = new NbiTextPane();
                    
            add(textPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // ??? (padx, pady)
        }
    }
}
