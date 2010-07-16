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

import java.awt.event.ActionListener;
import javax.swing.JPanel;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * MQ bindings editor for the NetBeans Composite Application Service Assembly
 * (CASA) editor.
 *
 * @author Noel.Ang@sun.com
 */
final class MqBindingsConfigurationEditor
        implements ExtensibilityElementConfigurationEditorComponent {

    // Identifies the various configurable data points.
    enum FIELD {
        HOST, PORT, QUEUEMANAGER, QUEUE, CHANNEL
    }

    private final Form form;
    private final WsdlConfigModelAdapter wsdlModelAdapter;
    private volatile MqBindingsConfigurationEditorPanel editorPanel;

    MqBindingsConfigurationEditor(WSDLModel wsdlModel,
                                  MqBindingsConfigurationEditorForm.BindingMode mode
    ) {
        if (wsdlModel == null) {
            throw new NullPointerException("wsdlModel");
        }
        MqBindingsFormModel viewModel = new MqBindingsConfigurationEditorModel();
        wsdlModelAdapter = new WsdlConfigModelAdapter(wsdlModel);
        initializeDefaults(wsdlModelAdapter);
        viewModel.adopt(wsdlModelAdapter);
        form = new MqBindingsConfigurationEditorForm(viewModel, null, mode);
    }

    private void initializeDefaults(MqBindingsFormModel model) {
        model.setQueueManager("qm_localhost");
        model.setQueue("default");
        model.setPort("1414");
        model.setPolling("5000");
        model.setIsSyncpoint(true);
        model.setIsTransactional(false);
        model.setIsDefaultBindingOption(true);
        model.setIsDefaultReadOption(true);
    }
    
    /**
     * Return a editor panel. In general, its better to cache this in
     * implementation, till commit/rollback is called. So that user provided
     * values will be saved in the panel.
     *
     * @return editor view
     */
    public JPanel getEditorPanel() {
        if (editorPanel == null) {
            form.revert();
            editorPanel = (MqBindingsConfigurationEditorPanel) form.getComponent();
        }
        return editorPanel;
    }

    /**
     * Return title for the dialog.
     *
     * @return String title
     */
    public String getTitle() {
        return NbBundle.getMessage(MqBindingsConfigurationEditor.class,
                "MqBindingsConfigurationEditor.CONFIGURE_TITLE"); //NOI18N
    }

    /**
     * Return the helpctx to be shown in dialog/wizards.
     *
     * @return HelpCtx.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Generally is not needed to be used. Implement if you have special cases.
     * listener on OK/Cancel buttons.
     *
     * @return ActionListener
     */
    public ActionListener getActionListener() {
        return null;
    }

    /**
     * Commit all values from the panel and commit it to the wsdl model. Return
     * true, if successfully committed, otherwise false.
     *
     * @return boolean
     */
    public boolean commit() {
        form.commit();
        wsdlModelAdapter.adopt(form.getModel());
        wsdlModelAdapter.updateModel();
        return true;
    }

    /**
     * Cleanup panel, discard values.
     *
     * @return boolean
     */
    public boolean rollback() {
        // Note: Different from reparse semantics.
        // Here, we tell the form to reload its presentation from its own model
        // The underlying WSDL model may have changed, yes, but that's not
        // my problem here.  That's reparse business.
        form.revert();
        return true;
    }

    /**
     * Do validation, and return true if valid, otherwise false.
     *
     * @return boolean
     */
    public boolean isValid() {
        return form.isValid();
    }

    public void reparse() {
        // Note: Different from rollback semantics.
        // In rollback(), we discard changes to the form made THRU the form.
        // Here, we want to reload ALL changes regardless of source;
        // essentially, repopulate the form's data model.
        form.getModel().adopt(wsdlModelAdapter);
        form.revert();
    }
    
    public void focus(Operation operation) {
        if (operation == null) {
            throw new NullPointerException("operation");
        }
        wsdlModelAdapter.focus(operation);
        form.getModel().adopt(wsdlModelAdapter);
        form.revert();
    }
    
    public void focus(Port port) {
        if (port == null) {
            throw new NullPointerException("port");
        }
        wsdlModelAdapter.focus(port);
        form.getModel().adopt(wsdlModelAdapter);
        form.revert();
    }
}
