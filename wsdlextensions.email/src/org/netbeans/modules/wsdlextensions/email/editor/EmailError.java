package org.netbeans.modules.wsdlextensions.email.editor;

	import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;

	/**
	 * 	EmailError
	 *     Defines an instance of an error containing the error mode and the 
	 *     error message
	 * 
	 * 
	 */
	public class EmailError {
	    private String mErrMode = 
	            ExtensibilityElementConfigurationEditorComponent.
	            PROPERTY_CLEAR_MESSAGES_EVT;
	    private String mErrMessage = null;
	    
	    public EmailError(String errMode, String errMessage) {
	        mErrMode = errMode;
	        mErrMessage = errMessage;
	    }
	    
	    public EmailError() {        
	    }
	    
	    public void setErrorMessage(String errMessage) {
	        mErrMessage = errMessage;
	    }
	    
	    public String getErrorMessage() {
            if (mErrMessage == null) return "";
	        return mErrMessage;
	    }
	    
	    public void setErrorMode(String errMode) {
	        mErrMode = errMode;
	    }
	    
	    public String getErrorMode() {
	        return mErrMode;
	    }

        public boolean isValid() {
            return !mErrMode.equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT);
        }
	    
}
