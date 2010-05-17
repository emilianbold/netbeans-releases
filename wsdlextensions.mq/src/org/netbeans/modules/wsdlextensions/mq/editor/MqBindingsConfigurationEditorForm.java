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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.mq.editor;

import java.awt.Component;

import org.netbeans.modules.wsdlextensions.mq.editor.event.WSDLWizardPanelChangeOutlet;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Concrete form for the MQ Bindings Configuration Extension.
 *
 * @author Noel.Ang@sun.com
 */
final class MqBindingsConfigurationEditorForm
        extends Form {

    MqBindingsConfigurationEditorForm(FormModel model,
                                      WSDLWizardContext context,
                                      BindingMode inputMode
    ) {
        super(context, model);
        this.inputMode = inputMode;
    }
    
    /**
     * Return the form's view.
     *
     * @return FormView.
     */
    protected FormView getView() {
        if (swingComponent == null) {
            swingComponent = new MqBindingsConfigurationEditorPanel(this);
            ((MqBindingsConfigurationEditorPanel) swingComponent).setInputMode(
                    inputMode
            );
            ((MqBindingsConfigurationEditorPanel) swingComponent).setChangeSupport(
                    new WSDLWizardPanelChangeOutlet(this)
            );
        }
        return swingComponent;
    }

    /**
     * The Swing component that represents the form's visual representation.
     *
     * @return The form's view.
     */
    public Component getComponent() {
        return (Component) getView();
    }

    /**
     * Return name of the panel.
     *
     * @return String
     */
    public String getName() {
        return taskDescription;
    }

    /**
     * Return the help context, to show the help page for this panel.
     *
     * @return HelpCtx
     */
    public HelpCtx getHelp() {
        return null;
    }

    /**
     * Controls the Next and Finish buttons on the wizard. Returning false, will
     * disable both Next and Finish Buttons on the wizard.
     *
     * @return true if Next and Finish needs to be enabled
     */
    public boolean isValid() {
        MqBindingsFormModel aModel = new MqBindingsConfigurationEditorModel();
        swingComponent.write(aModel);
        
        // Criteria for validity:
        // One-Way inbound requires these values specified:
        //     Queue
        //     Queue Manager
        //     Polling Interval
        // One-Way outbound requires these values specified:
        //     Queue
        //     Queue Manager
        // Request-response inbound requires these values specified:
        //     Queue
        //     Queue Manager
        boolean valid = true;
        switch (inputMode) {
            case ONEWAYGET:
                valid &= !"".equals(aModel.getQueue());
                valid &= !"".equals(aModel.getQueueManager());
                valid &= !"".equals(aModel.getPolling());
                break;
            case ONEWAYPUT:
                valid &= !"".equals(aModel.getQueue());
                valid &= !"".equals(aModel.getQueueManager());
                break;
            case TWOWAYGET:
                valid &= !"".equals(aModel.getQueue());
                valid &= !"".equals(aModel.getQueueManager());
                break;
        }

        // Global requirements regardless of mode:
        // If host is specified, so must port and channel.
        String host = aModel.getHost();
        String port = aModel.getPort();
        String channel = aModel.getChannel();
        valid &= ("".equals(host) || (!"".equals(port) && !"".equals(channel)));
        return valid;
    }

    /**
     * If true, then the wizard is finishable at this step.
     *
     * @return true if finishable
     */
    public boolean isFinishPanel() {
        return true;
    }

    private final BindingMode inputMode;
    private FormView swingComponent;
    private final String taskDescription = NbBundle.getMessage(
            MqBindingsConfigurationEditorForm.class,
            "MqBindingsConfigurationEditorForm.TaskDescription"
    );

    public enum BindingMode {
        ONEWAYGET, ONEWAYPUT, TWOWAYGET
    }
}
