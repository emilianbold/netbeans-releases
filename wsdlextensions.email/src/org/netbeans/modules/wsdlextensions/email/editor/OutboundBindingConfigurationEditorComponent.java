package org.netbeans.modules.wsdlextensions.email.editor;

import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.wsdlextensions.email.editor.wizard.OutboundBindingConfigurationEditorForm;
import org.netbeans.modules.wsdlextensions.email.editor.wizard.OutboundWsdlModelAdapter;
import org.netbeans.modules.wsdlextensions.email.editor.wizard.ModelModificationException;
import org.netbeans.modules.wsdlextensions.email.editor.wizard.OutboundBindingConfigurationEditorModel;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class OutboundBindingConfigurationEditorComponent implements ExtensibilityElementConfigurationEditorComponent {

    private static final Logger logger = Logger.getLogger(
            OutboundBindingConfigurationEditorComponent.class.getName());
    
    private final OutboundBindingConfigurationEditorForm form;
    private final OutboundBindingConfigurationEditorForm.Model viewModel;
    private final OutboundWsdlModelAdapter wsdlModelAdapter;
    
    OutboundBindingConfigurationEditorComponent(QName qname, WSDLModel wsdlModel, boolean enablePayloadProcessing) {
        if (wsdlModel == null) {
            throw new NullPointerException(NbBundle.getMessage(OutboundWsdlModelAdapter.class,
	                "OutboundBindingConfigurationEditorComponent.WsdlModelIsNull"));
        }
        viewModel = new OutboundBindingConfigurationEditorModel();
        wsdlModelAdapter = new OutboundWsdlModelAdapter(wsdlModel);
        try {
        	OutboundBindingConfigurationEditorForm.syncToFrom(viewModel, wsdlModelAdapter);
        } catch (ModelModificationException e) {
            throw new RuntimeException(e);
        }
        form = new OutboundBindingConfigurationEditorForm(viewModel);
        form.setEnablePayloadProcessing(enablePayloadProcessing);
    }    
    
    /**
     * Return a editor panel. In general, its better to cache this in
     * implementation, till commit/rollback is called. So that user provided
     * values will be saved in the panel.
     *
     * @return editor view
     */
    public JPanel getEditorPanel() {
        return (JPanel)form;
    }

    /**
     * Return title for the dialog.
     *
     * @return String title
     */
    public String getTitle() {
        return NbBundle.getMessage(OutboundBindingConfigurationEditorComponent.class,
                "OutboundConfigurationEditorPanel.CONFIGURE_TITLE");
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
        boolean success = true;
        form.commit();
        try {
        	OutboundBindingConfigurationEditorForm.syncToFrom(wsdlModelAdapter, form.getModel());
        } catch (ModelModificationException e) {
            logger.log(Level.SEVERE, NbBundle.getMessage(OutboundBindingConfigurationEditorComponent.class,
	                "OutboundBindingConfigurationEditorComponent.WsdlSaveFailed"), e);
            success = false;
        }
        return success;
    }    

    /**
     * Cleanup panel, discard values.
     *
     * @return boolean
     */
    public boolean rollback() {
        // Note: Different from reparse semantics.
        // Here, we tell the form to reload its presentation from its own model
        // (The Form contract requires data held by visual widgets to be
        // effectively distinct from what is in the Form's model.)
        // The underlying WSDL model may have changed, yes, but that's not
        // my problem here.  That's reparse business.
        form.refresh();
        return true;
    }
    
    /**
     * Do validation, and return true if valid, otherwise false.
     *
     * @return boolean
     */
    public boolean isValid() {
        return requiredInformationExists();
    }
    
    public void reparse(WSDLComponent component) {
        // Note: Different from rollback semantics.
        // In rollback(), we discard changes to the form made THRU the form.
        // Here, we want to reload ALL changes regardless of source;
        // essentially, repopulate the form's data model.
        try {
            wsdlModelAdapter.focus(component);
            OutboundBindingConfigurationEditorForm.syncToFrom(form.getModel(), wsdlModelAdapter);
            form.refresh();
        } catch (ModelModificationException e) {
            logger.log(Level.SEVERE, NbBundle.getMessage(OutboundBindingConfigurationEditorComponent.class,
	                "OutboundBindingConfigurationEditorComponent.ConfigReloadFailed"), e);
        }
    }
    
    private boolean requiredInformationExists() {
        boolean success;
        success = !safeString(viewModel.getEmailServer()).equals("");
        return success;
    }

	    public String safeString(String value) {
	        if (value == null) {
	            value = "";
	        }
	        if (value.startsWith(" ") || value.endsWith(" ")) {
	            value = value.trim();
	        }
	        return value;
	    }
    
}
