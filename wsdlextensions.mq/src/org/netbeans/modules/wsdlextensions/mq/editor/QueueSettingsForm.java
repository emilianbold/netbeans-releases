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

import javax.swing.JComponent;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Concrete form for the Queue Settings dialog.
 *
 * @author Noel.Ang@sun.com
 */
final class QueueSettingsForm
        extends Form {

    QueueSettingsForm(WSDLWizardContext context, FormModel model) {
        super(context, model);
    }

    /**
     * Return the form's view.
     *
     * @return FormView.
     */
    protected FormView getView() {
        if (swingComponent == null) {
            swingComponent = new QueueSettingsPanel();
        }
        return swingComponent;
    }

    JComponent getDefaultFocusComponent() {
        getView();
        return ((QueueSettingsPanel) swingComponent).getDefaultFocusComponent();
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

        return !"".equals(aModel.getQueue());
    }

    /**
     * If true, then the wizard is finishable at this step.
     *
     * @return true if finishable
     */
    public boolean isFinishPanel() {
        return false;
    }

    private FormView swingComponent;

    private final String taskDescription =
            NbBundle.getMessage(QueueSettingsForm.class,
                    "QueueSettingsForm.TaskDescription"
            );
}
