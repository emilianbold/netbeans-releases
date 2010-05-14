package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.util.NbBundle;
import org.netbeans.modules.wsdlextensions.email.editor.EmailError;


	/**
	 * Controller to allow for persisting of the model from
	 * from the visual components as well as persistence of the model
	 * 
	 * 
	 */
	public class OutboundPersistenceController {
	    private OutboundBindingConfigurationEditorForm outboundEditorForm = null;
	    private WSDLComponent mWSDLComponent = null;
	    
	    private static final Logger logger = Logger.getLogger(
	    		OutboundPersistenceController.class.getName());

	    
	    public OutboundPersistenceController(WSDLComponent modelComponent,
	    		OutboundBindingConfigurationEditorForm visualComponent) {
	    	outboundEditorForm = visualComponent;
	        mWSDLComponent = modelComponent;
	    }
	    
	     /**
	     * Commit all changes
	     * @return
	     */
	    public boolean commit() {
	        boolean success = true;

	        EmailError emailError = outboundEditorForm.validateMe();
	        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT.equals(emailError.getErrorMode())) {
	           return false;
	        }       
	        
	        outboundEditorForm.commit();
	        OutboundBindingConfigurationEditorForm.Model model = (OutboundBindingConfigurationEditorForm.Model)outboundEditorForm.getModel();
	        OutboundWsdlModelAdapter modelAdapter = new OutboundWsdlModelAdapter(mWSDLComponent.getModel());
	        modelAdapter.focus(mWSDLComponent);
	        try {
	        	outboundEditorForm.syncToFrom(modelAdapter, model);
	        } catch (ModelModificationException ex) {
	            logger.log(Level.SEVERE, NbBundle.getMessage(OutboundPersistenceController.class,
	                "OutboundPersistenceController.ConfigSaveFailed"), ex);
	            success = false;
	        }
	        return success;
	    }

}
