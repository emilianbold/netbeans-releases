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
	public class InboundPersistenceController {
	    private InboundBindingConfigurationEditorForm inboundEditorForm = null;
	    private WSDLComponent mWSDLComponent = null;
	    
	    private static final Logger logger = Logger.getLogger(
	    		InboundPersistenceController.class.getName());
        public String templateType = "";
	    
	    public InboundPersistenceController(WSDLComponent modelComponent,
	    		InboundBindingConfigurationEditorForm visualComponent) {
	    	inboundEditorForm = visualComponent;
	        mWSDLComponent = modelComponent;
	    }
	    
	     /**
	     * Commit all changes
	     * @return
	     */
	    public boolean commit() {
	        boolean success = true;

	        EmailError emailError = inboundEditorForm.validateMe();
	        if (ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT.equals(emailError.getErrorMode())) {
	           return false;
	        }       
	        
	        inboundEditorForm.commit();
	        InboundBindingConfigurationEditorForm.Model model = (InboundBindingConfigurationEditorForm.Model)inboundEditorForm.getModel();
	        InboundWsdlModelAdapter modelAdapter = new InboundWsdlModelAdapter(mWSDLComponent.getModel(), templateType);
	        modelAdapter.focus(mWSDLComponent);
	        try {
	        	inboundEditorForm.syncToFrom(modelAdapter, model);
	        } catch (ModelModificationException ex) {
	            logger.log(Level.SEVERE, NbBundle.getMessage(InboundPersistenceController.class,
	                "InboundPersistenceController.ConfigSaveFailed"), ex);
	            success = false;
	        }
	        return success;
	    }

        public String getTemplateType(){
            return templateType;
        }

        public void setTemplateType(String val){
            this.templateType = val;
        }
}
