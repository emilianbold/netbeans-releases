package org.netbeans.modules.wsdlextensions.ldap;

	import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;

	/**
	 * 	LDAPError
	 *     Defines an instance of an error containing the error mode and the 
	 *     error message
	 * 
	 * 
	 */
	public class LDAPError {
	    private String mErrMode = 
	            ExtensibilityElementConfigurationEditorComponent.
	            PROPERTY_CLEAR_MESSAGES_EVT;
	    private String mErrMessage= "";
	    
	    public LDAPError(String errMode, String errMessage) {
	        mErrMode = errMode;
	        mErrMessage = errMessage;
	    }
	    
	    public LDAPError() {        
	    }
	    
	    public void setErrorMessage(String errMessage) {
	        mErrMessage = errMessage;
	    }
	    
	    public String getErrorMessage() {
	        return mErrMessage;
	    }
	    
	    public void setErrorMode(String errMode) {
	        mErrMode = errMode;
	    }
	    
	    public String getErrorMode() {
	        return mErrMode;
	    }   
	    
}
