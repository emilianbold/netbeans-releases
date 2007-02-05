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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

public class LicensesPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public LicensesPanel() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        
        setProperty(ACCEPT_CHECKBOX_TEXT_PROPERTY, DEFAULT_ACCEPT_CHECKBOX_TEXT);
    }
    
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size()  > 0;
    }
    
    public boolean canExecuteBackward() {
        return Registry.getInstance().getProductsToInstall().size()  > 0;
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new LicensesPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class LicensesPanelUi extends WizardPanelUi {
        protected LicensesPanel component;
        
        public LicensesPanelUi(LicensesPanel component) {
            super(component);
            
            this.component = component;
        }
        
        // swing ui specific ////////////////////////////////////////////////////////
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new LicensesPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class LicensesPanelSwingUi extends WizardPanelSwingUi {
        protected LicensesPanel component;
        
        private List<Product> products;
        
        private NbiTextPane   licensePane;
        private NbiScrollPane licenseScrollPane;
        
        private NbiCheckBox   acceptCheckBox;
        
        public LicensesPanelSwingUi(
                final LicensesPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            this.products = new LinkedList<Product>();
            
            initComponents();
        }
        
        protected void initialize() {
            acceptCheckBox.setText(
                    component.getProperty(ACCEPT_CHECKBOX_TEXT_PROPERTY));
            
            final List<Product> currentProducts =
                    Registry.getInstance().getProductsToInstall();
            
            if (!products.equals(currentProducts)) {
                final StringBuilder text = new StringBuilder();
                for (Product product: currentProducts) {
                    text.append("-------------------------------------------------");
                    text.append(StringUtils.CRLF);
                    text.append(product.getDisplayName() + ":");
                    text.append(StringUtils.CRLFCRLF);
                    try {
                        text.append(product.getLogic().getLicense().getText());
                    } catch (InitializationException e) {
                        ErrorManager.notifyError(
                                "Could not access configuration logic",
                                e);
                    }
                    text.append(StringUtils.CRLFCRLF);
                }
                
                licensePane.setText(text);
                licensePane.setCaretPosition(0);
                
                acceptCheckBox.setSelected(false);
                
                products = currentProducts;
            }
            
            acceptCheckBoxToggled();
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            licensePane = new NbiTextPane();
            licensePane.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
            
            licenseScrollPane = new NbiScrollPane(licensePane);
            
            acceptCheckBox = new NbiCheckBox();
            acceptCheckBox.setSelected(false);
            acceptCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    acceptCheckBoxToggled();
                }
            });
            
            add(licenseScrollPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(acceptCheckBox, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            
            // L&F-specific tweaks
            if (UIManager.getLookAndFeel().getID().equals("GTK")) {
                licenseScrollPane.setViewportBorder(null);
            }
        }
        
        private void acceptCheckBoxToggled() {
            if (acceptCheckBox.isSelected()) {
                container.getNextButton().setEnabled(true);
            } else {
                container.getNextButton().setEnabled(false);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ACCEPT_CHECKBOX_TEXT_PROPERTY =
            "accept.checkbox.text"; // NOI18N
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.description"); // NOI18N
    
    public static final String DEFAULT_ACCEPT_CHECKBOX_TEXT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.accept.checkbox.text"); // NOI18N
}