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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.mq.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;

/**
 * Wizard extension iterator for the WebSphere MQ WSDL Extension.
 *
 * @author Noel.Ang@sun.com
 */
public final class MqWsdlEditorExtensionIterator
        extends WSDLWizardExtensionIterator {

    public MqWsdlEditorExtensionIterator(WSDLWizardContext context) {
        super(context);
        steps = new ArrayList<String>();
    }

    private void initializePanels(String templateName) {
        assert templateName != null;

        synchronized (lock) {

            if (panels == null) {
                panels = new ArrayList<Form>();
            } else {
                panels.clear();
            }

            if (templateName.equals("OneWayInbound")) {
                WSDLWizardContext context = getWSDLWizardContext();
                Form.FormModel model = globalModel = defaultFormModel(context);
                panels.add(new MqBindingsConfigurationEditorForm(model,
                        context,
                        MqBindingsConfigurationEditorForm.BindingMode.ONEWAYGET
                )
                );
            }

            if (templateName.equals("OneWayOutbound")) {
                WSDLWizardContext context = getWSDLWizardContext();
                Form.FormModel model = defaultFormModel(context);
                panels.add(new MqBindingsConfigurationEditorForm(model,
                        context,
                        MqBindingsConfigurationEditorForm.BindingMode.ONEWAYPUT
                )
                );
            }

            if (templateName.equals("TwoWayInbound")) {
                WSDLWizardContext context = getWSDLWizardContext();
                Form.FormModel model = globalModel = defaultFormModel(context);
                panels.add(new MqBindingsConfigurationEditorForm(model,
                        context,
                        MqBindingsConfigurationEditorForm.BindingMode.TWOWAYGET
                )
                );
            }
        }
    }

    private MqBindingsFormModel defaultFormModel(WSDLWizardContext context) {
        WsdlConfigModelAdapter model = new WsdlConfigModelAdapter(context);
        model.setPort("1414");
        model.setPolling("5000");
        model.setIsSyncpoint(true);
        model.setIsTransactional(false);
        model.setIsDefaultBindingOption(true);
        model.setIsDefaultReadOption(true);
        return model;
    }

    private void initializeSteps() {
        synchronized (lock) {
            steps.clear();
            for (Form panel : panels) {
                steps.add(panel.getName());
            }
            activePanelIndex = -1;
        }
    }

    /**
     * Called when the template is changed in the wizard name and location
     * panel. Use this method to initialize panels, and steps.
     *
     * @param templateName One of the predefined template names in the
     * Extension's template.xml.
     */
    public void setTemplateName(String templateName) {
        if (templateName == null) {
            throw new NullPointerException("templateName");
        }
        initializePanels(templateName);
        initializeSteps();
    }

    /**
     * Called when Finish is clicked on the wsdl wizard. Commit all values
     * collected into the WSDLModel.
     *
     * @return true if successful.
     */
    public boolean commit() {
        boolean success = true;
        if (globalModel != null) {
            if (globalModel instanceof WsdlConfigModelAdapter) {
                try {
                    ((WsdlConfigModelAdapter) globalModel).updateModel();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    success = false;
                }
            }
        }
        return success;
    }

    public WSDLWizardDescriptorPanel current() {
        synchronized (lock) {
            return (activePanelIndex != -1
                    ? panels.get(activePanelIndex)
                    : null);
        }
    }

    public String[] getSteps() {
        synchronized (lock) {
            return steps.toArray(new String[steps.size()]);
        }
    }

    public boolean hasNext() {
        int idx = activePanelIndex;
        return idx < panels.size() - 1;
    }

    public boolean hasPrevious() {
        return activePanelIndex > 0;
    }

    /**
     * Callback so that the extension iterator know that next button is clicked.
     * Can be used to increment the counter, or some other logic to calculate
     * the next step.
     */
    public void nextPanel() {
        synchronized (lock) {
            activePanelIndex = Math.min(activePanelIndex + 1, panels.size() - 1);
        }
    }

    /**
     * Callback so that extension iterator knows that previous button was
     * clicked.
     */
    public void previousPanel() {
        synchronized (lock) {
            activePanelIndex = Math.max(activePanelIndex - 1, -1);
        }
    }

    private static final Logger logger = Logger.getLogger(
            MqBindingsConfigurationEditor.class.getName());
    private final Object lock = new Object();
    private final List<String> steps;
    private List<Form> panels;
    private volatile int activePanelIndex = -1;
    private volatile MqBindingsFormModel globalModel = null;
}
