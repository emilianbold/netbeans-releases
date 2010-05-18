/*
 * InboundOutboundMessagePanel.java
 *
 * Created on August 10, 2008, 12:21 PM
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.util.Collection;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.jms.validator.JMSComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 *
 * @author  jalmero
 */
public class InboundRequestResponseMainPanel extends javax.swing.JPanel {

    private InboundOneWayMessagePanel mInboundPanel = null;
    private InboundResponseMessagePanel mResponseMsgPanel = null;
    private InboundResponseMessageConsumerPanel mResponseMsgConsumerPanel = null;
    private JMSAdvancedPanel mAdvPanel = null;
    private WSDLComponent mComponent = null;
    private QName mQName = null;
    
    /** Creates new form InboundOutboundMessagePanel */
    public InboundRequestResponseMainPanel(WSDLComponent component) {
        mComponent = component;
        initComponents();
        initCustomComponents();
        populateView(component);
    }

    public void populateView(WSDLComponent component) {
        if (mInboundPanel != null) {
            mInboundPanel.populateView(null, component);                    
        }
        
        if (mResponseMsgPanel != null) {
            mResponseMsgPanel.populateView(null, component);                    
        }     
        
        if (mResponseMsgConsumerPanel != null) {
            mResponseMsgConsumerPanel.populateView(null, component);                    
        }         
    }
    
    void setProject(Project project) {
        if (mInboundPanel != null) {
            mInboundPanel.setProject(project);
        }
        
        if (mResponseMsgPanel != null) {
            mResponseMsgPanel.setProject(project);                    
        }
        
    }    
   
    public InboundOneWayMessagePanel getInboundPanel() {
        return mInboundPanel;
    }
    
    public InboundResponseMessagePanel getResponseMessagePanel() {
        return mResponseMsgPanel;
    }
    
    public InboundResponseMessageConsumerPanel getResponseMessageConsumerPanel() {
        return mResponseMsgConsumerPanel;
    }
    
    public JMSAdvancedPanel getAdvancedPanel() {
        return mAdvPanel;
    }    
    
    /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        if (mInboundPanel != null) {
            mInboundPanel.setOperationName(opName);
        }
        
        if (mResponseMsgPanel != null) {
            mResponseMsgPanel.setOperationName(opName);
        }  
        
        if (mResponseMsgConsumerPanel != null) {
            mResponseMsgConsumerPanel.setOperationName(opName);
        }           
    }
    
    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
        if (mInboundPanel != null) {
            mInboundPanel.enablePayloadProcessing(enable);
        }
        
        if (mResponseMsgPanel != null) {
            mResponseMsgPanel.enablePayloadProcessing(enable);
        }        
    }      
    
    public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {    
        FileError fileError = mInboundPanel.validateMe(fireEvent);    
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {
            fileError = mResponseMsgPanel.validateMe(fireEvent); 
            if (ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {  
                if (mResponseMsgConsumerPanel != null) {
                    mResponseMsgConsumerPanel.validateMe(fireEvent);                   
                }                 
            }
        }
        return fileError;                 
    }   
    
    /**
     * Route the property change event to this panel
     */
    public void doFirePropertyChange(String name, Object oldValue, Object newValue) {           
        firePropertyChange(name, oldValue, 
                newValue);         
    }   
    
    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        // do FileBC-specific validation first
        FileError fileError = validateMe(true);
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(fileError.getErrorMode())) {
            return false;
        }

        ValidationResult results = new JMSComponentValidator().
                validate(mComponent.getModel(), null, ValidationType.COMPLETE);
        Collection<ResultItem> resultItems = results.getValidationResult();
        ResultItem firstResult = null;
        String type = ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT;
        boolean result = true;
        if (resultItems != null && !resultItems.isEmpty()) {
            for (ResultItem item : resultItems) {
                if (item.getType() == ResultType.ERROR) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_ERROR_EVT;
                    result = false;
                    break;
                } else if (firstResult == null) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.
                            PROPERTY_WARNING_EVT;
                }
            }
        }
        if (firstResult != null) {
            firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.
                    PROPERTY_CLEAR_MESSAGES_EVT, null, null);
            return true;
        }

    }
    
    private void initCustomComponents() {
        mInboundPanel = new InboundOneWayMessagePanel(mQName, mComponent);
        mResponseMsgPanel = new InboundResponseMessagePanel(mQName, mComponent);
        mResponseMsgConsumerPanel = new InboundResponseMessageConsumerPanel(mQName, mComponent);
        mAdvPanel = new JMSAdvancedPanel(mQName, mComponent);
        
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "InboundMessageStepWizardPanel.TitleLabel"), mInboundPanel);
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "InboundResponseMessageStepWizardPanel.TitleLabel"), mResponseMsgPanel);
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "InboundResponseMessageConsumerStepWizardPanel.TitleLabel"), mResponseMsgConsumerPanel);
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "JMSAdvancedPanel.TitleLabel"), mAdvPanel);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        jPanel1.add(jTabbedPane1, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
