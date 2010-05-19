package org.netbeans.modules.wsdlextensions.email.editor.wizard;


import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.wsdlextensions.email.EmailConstants;
import org.netbeans.modules.wsdlextensions.email.editor.panels.OutboundMessagePanel;
import org.netbeans.modules.wsdlextensions.email.editor.EmailError;
import org.netbeans.modules.wsdlextensions.email.editor.Form;
import org.netbeans.modules.wsdlextensions.email.editor.Form.FormModel;

	/**
	* SMTP Binding 
	* 
	*/

	public class OutboundBindingConfigurationEditorForm extends OutboundMessagePanel  {
    
		    private final Model model;

            private boolean mEnablePayloadProcessing = true;

            private Project mProject;
           
		    public OutboundBindingConfigurationEditorForm(Model model){
		        super();
		        this.model = model;
		        init();

		        SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		                mLocationTextField.requestFocusInWindow();
                        if(!mEnablePayloadProcessing){
                            mXsdElementTypeTextField.setEnabled(mEnablePayloadProcessing);
                            mXsdEleTypeButton.setEnabled(mEnablePayloadProcessing);
                            mMessageTypeComboBox.setEnabled(mEnablePayloadProcessing);
                            mCharEncodingComboBox.setEnabled(mEnablePayloadProcessing);
                            mEncodingStyleTextField.setEnabled(mEnablePayloadProcessing);
                        }
		            }
		        });
		        
		    }
		    
		    private void init() {
		    }
		    
		    /**
		     * Signal for the form to reread its data model into its view, in effect
		     * discarding uncommitted changes made thru the view.
		     */
		    public void refresh() {
		        if (!SwingUtilities.isEventDispatchThread()) {
		            Utils.dispatchToSwingThread("refresh()", new Runnable() {
		                public void run() {
		                    refresh();
		                }
		            });
		            return;
		        }
				// Email Address
		        mLocationTextField.setText(model.getLocation());
                mSMTPServerTextField.setText(model.getEmailServer());
				mPortTextField.setText(model.getPort());
                mUserNameTextField.setText(model.getUserName());
				mPasswordField.setText(model.getPassword());
                mUseSSLCheckBox.setSelected(model.getUseSSL());

				// Email Message
				mCharEncodingComboBox.setSelectedItem(model.getCharset());
                mMessageTypeComboBox.setSelectedItem(model.getMessageType());
				mEncodingStyleTextField.setText(model.getEncodingStyle());
		        mSendOptionComboBox.setSelectedItem(model.getSendOption());
                mEmbedImgCheckBox.setSelected(model.getEmbedImagesInHtml());
                mHandleNMAttCheckBox.setSelected(model.getHandleNMAttachments());
                mXsdElementTypeTextField.setText(model.getXsdElementOrType());
		    }


		    /**
		     * Signal for the form to update its data model with uncommitted changes
		     * made thru its view.
		     */
		    public void commit() {
		        if (!SwingUtilities.isEventDispatchThread()) {
		            Utils.dispatchToSwingThread("commit()", new Runnable() {
		                public void run() {
		                    commit();
		                }
		            });
		            return;
		        }

				// Email Address
		        model.setLocation(mLocationTextField.getText());
		        model.setEmailServer(mSMTPServerTextField.getText());
		        model.setPort(mPortTextField.getText());
		        model.setUserName(mUserNameTextField.getText());
		        model.setPassword(new StringBuilder().append(mPasswordField.getPassword()).toString());
                model.setUseSSL(mUseSSLCheckBox.isSelected());

				// Email Message
				model.setCharset(mCharEncodingComboBox.getSelectedItem().toString());
				model.setEncodingStyle(mEncodingStyleTextField.getText());
                model.setUse(getUseType());
                model.setSendOption(mSendOptionComboBox.getSelectedItem().toString());
                model.setEmbedImagesInHtml(mEmbedImgCheckBox.isSelected());
                model.setHandleNMAttachments(mHandleNMAttCheckBox.isSelected());
                model.setMessageType(mMessageTypeComboBox.getSelectedItem().toString());
                model.setElementType(getSelectedElementType());
                model.setPartType(getSelectedPartType());
                model.setXsdElementOrType(mXsdElementTypeTextField.getText());

		    }
		    
		    /**
		     * Validate the changes before the form update its data model
		     * 
		     * 
		     * @return boolean.
		     */		    
		    
		    private boolean requiredInformationExists() {
		        boolean success;
		        success = !Utils.safeString(mLocationTextField.getText()).equals("");
		        return success;
		    }		    

		    /**
		     * Populate the form's internal data model with the information provided. If
		     * the supplied model is not a type that is recognized or meaningful, it is
		     * disregarded.
		     *
		     * @param model A supported FormModel instance.
		     */
		    public void loadModel(FormModel model) {

		    }

		    /**
		     * Returns the form's own data model.
		     *
		     * @return Form data model
		     */
		    public FormModel getModel() {
		        return model;
		    }  

		    /**
		     * The Swing component that represents the form's visual representation.
		     *
		     * @return The form's view.
		     */
		    public JComponent getComponent() {
		       return this;
		    }

		    public void setEnablePayloadProcessing(boolean enable) {
		        this.mEnablePayloadProcessing = enable;
		    }

		    public void setProject(Project project) {
		        this.mProject = project;
		    }

		    /**
		     * Validate the changes before commiting the data
		     * 
		     * 
		     * @return EmailError.
		     */			    
		    public EmailError validateMe() {
		    	EmailError emailError = new EmailError();
		    	return emailError;
		    }	    
		    
		    public static void syncToFrom(Form.FormModel destModel, Form.FormModel srcModel)
		    throws ModelModificationException {
				if (!(destModel instanceof OutboundBindingConfigurationEditorForm.Model)) {
				    return;
				}
				if (!(srcModel instanceof OutboundBindingConfigurationEditorForm.Model)) {
				    return;
				}
				OutboundBindingConfigurationEditorForm.Model dest =
				        (OutboundBindingConfigurationEditorForm.Model) destModel;
				OutboundBindingConfigurationEditorForm.Model src =
				        (OutboundBindingConfigurationEditorForm.Model) srcModel;

				// Email Address
		        dest.setLocation(src.getLocation());
		        dest.setEmailServer(src.getEmailServer());
		        dest.setPort(src.getPort());
		        dest.setUserName(src.getUserName());
		        dest.setPassword(src.getPassword());
                dest.setUseSSL(src.getUseSSL());

				// Email Message
				dest.setCharset(src.getCharset());
				dest.setEncodingStyle(src.getEncodingStyle());
                dest.setUse(src.getUse());
                dest.setSendOption(src.getSendOption());
                dest.setEmbedImagesInHtml(src.getEmbedImagesInHtml());
                dest.setHandleNMAttachments(src.getHandleNMAttachments());
                dest.setMessageType(src.getMessageType());
                dest.setElementType(src.getElementType());
                dest.setPartType(src.getPartType());
                dest.setXsdElementOrType(src.getXsdElementOrType());
		    } 		    

            /**
             * Return the use type for input message
             * @return String use type
             */
            public String getUseType() {
                Object typeObj = mMessageTypeComboBox.getSelectedItem();
                if (typeObj != null && typeObj.equals(EmailConstants.ENCODED_DATA)) {
                    return EmailConstants.ENCODED;
                } else {
                    return EmailConstants.LITERAL;
                }
            }
		    /**
		     * Data model that this view/panel can understand. Implement this interface
		     * to supply this panel with content.
		     */
		    public interface Model extends FormModel {
		        
		    	public String getLocation();
		        public void setLocation(String location);
				
				public String getEmailServer();
				public void setEmailServer(String smtpServer);

				public String getPort();
				public void setPort(String port);

				public String getUserName(); 
				public void setUserName(String userName); 

				public String getPassword(); 
				public void setPassword(String password); 

				public boolean getUseSSL();
				public void setUseSSL(boolean useSSL);

				public String getCharset();
				public void setCharset(String charEncoding);

				public String getEncodingStyle(); 
				public void setEncodingStyle(String encodingStyle);

				public String getMessageType();
				public void setMessageType(String messageType);

				public String getUse(); 
				public void setUse(String use); 

				public String getSendOption(); 
				public void setSendOption(String sendOption); 

				public boolean getEmbedImagesInHtml();
				public void setEmbedImagesInHtml(boolean embedImgHTML);

				public boolean getHandleNMAttachments();
				public void setHandleNMAttachments(boolean handleNMAtt);

                public String getXsdElementOrType();
				public void setXsdElementOrType(String xsdEOT);
				
                public GlobalType getPartType();
				public void setPartType(GlobalType partType);
				
                public GlobalElement getElementType();
				public void setElementType(GlobalElement elementType);
		        
		    }

		}
