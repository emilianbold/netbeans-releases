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

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.openide.util.HelpCtx;

/**
 * General control interface for the various GUI "forms" (panels, dialogs, etc.)
 * that comprise the MQ implementation of the extensibility element
 * configuration editor.
 *
 * @author Noel.Ang@sun.com
 */
abstract class Form
        extends WSDLWizardDescriptorPanel {

    protected Form(WSDLWizardContext context, FormModel model) {
        super(context);
        this.model = model;
    }

    private void storeSettings(org.openide.loaders.TemplateWizard outlet) {
        FormModel model = getModel();
        outlet.putProperty(FormModel.class.getName(), model);
    }

    private void readSettings(org.openide.loaders.TemplateWizard inlet) {
        FormModel model = getModel();
        Object importModel = inlet.getProperty(FormModel.class.getName());
        if (importModel != null) {
            model.adopt((FormModel) importModel);
        }
    }

    /**
     * Signal for the form to reread its data model into its view, in effect
     * discarding uncommitted changes made thru the view.
     */
    protected void revert() {
        FormView view = getView();
        view.load(model);
    }

    /**
     * Signal for the form to update its data model with uncommitted changes
     * made thru its view.
     */
    protected void commit() {
        FormView view = getView();
        view.write(model);
    }

    /**
     * Return the form's view.
     *
     * @return FormView.
     */
    protected abstract FormView getView();

    /**
     * Returns the form's own data model.
     *
     * @return FormModel.
     */
    protected final FormModel getModel() {
        return model;
    }

    /**
     * Return name of the panel.
     *
     * @return String
     */
    public abstract String getName();

    /**
     * Return the gui component needed to show in the wizard. This method will
     * be called multiple times, so cache the component.
     *
     * @return Component
     */
    public abstract Component getComponent();

    /**
     * Return the help context, to show the help page for this panel.
     *
     * @return HelpCtx
     */
    public abstract HelpCtx getHelp();

    /**
     * Controls the Next and Finish buttons on the wizard. Returning false, will
     * disable both Next and Finish Buttons on the wizard.
     *
     * @return true if Next and Finish needs to be enabled
     */
    public abstract boolean isValid();

    /**
     * If true, then the wizard is finishable at this step.
     *
     * @return true if finishable
     */
    public abstract boolean isFinishPanel();
    
    public void importModel(FormModel model) {
        this.model.adopt(model);
        getView().load(this.model);
    }

    /**
     * Passes the settings Object, in this case, its TemplateWizard object. It
     * can be used to retrieve values from the settings. Called before the panel
     * is about to be shown on the wizard.
     *
     * @param settings A supported data holder.
     */
    public void readSettings(Object settings) {
        if (settings instanceof org.openide.loaders.TemplateWizard) {
            readSettings((org.openide.loaders.TemplateWizard) settings);
        }
        revert();
    }

    /**
     * Called when Previous, Next or Finish is pressed, to store information in
     * the TemplateWizard object.
     *
     * @param settings A supported data holder.
     */
    public void storeSettings(Object settings) {
        commit();
        if (settings instanceof org.openide.loaders.TemplateWizard) {
            storeSettings((org.openide.loaders.TemplateWizard) settings);
        }
    }

    private final FormModel model;

    public static interface FormView {

        /**
         * Populate the form's view with the information provided. If the supplied
         * model is not a type that is recognized or meaningful, it can be
         * disregarded.
         *
         * @param model A supported FormModel instance.
         */
        void load(Form.FormModel model);

        /**
         * Populate a model object with the data in the view. If the supplied model
         * is not a type that is recognized or meaningful, it can be disregarded.
         *
         * @param model A supported FormModel instance
         */
        void write(Form.FormModel model);
    }
    
    public static interface FormModel {
        void adopt(FormModel model);
    }
}
