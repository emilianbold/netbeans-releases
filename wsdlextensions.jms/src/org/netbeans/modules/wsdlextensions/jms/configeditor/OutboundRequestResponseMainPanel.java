/*
 * InboundOutboundMessagePanel.java
 *
 * Created on August 10, 2008, 12:21 PM
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.util.Collection;
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
public class OutboundRequestResponseMainPanel extends javax.swing.JPanel {

    private OutboundOneWayConnectionPanel mConnectionPanel = null;
    private OutboundResponseReplyPanel mResponseReplyPanel = null;
    private OutboundResponsePublisherPanel mResponsePublisherPanel = null;    
    private JMSAdvancedPanel mAdvPanel = null;
    private WSDLComponent mComponent = null;
    private QName mQName = null;
    
    /** Creates new form InboundOutboundMessagePanel */
    public OutboundRequestResponseMainPanel(WSDLComponent component) {
        mComponent = component;
        initComponents();
        initCustomComponents();
        populateView(component);
    }

    public void populateView(WSDLComponent component) {
        if (mConnectionPanel != null) {
            mConnectionPanel.populateView(null, component);                    
        }
        
        if (mResponseReplyPanel != null) {
            mResponseReplyPanel.populateView(null, component);                    
        }     
        
        if (mResponsePublisherPanel != null) {
            mResponsePublisherPanel.populateView(null, component);                    
        }         
    }
    
    public OutboundOneWayConnectionPanel getConnectionPanel() {
        return mConnectionPanel;
    }
    
    public OutboundResponseReplyPanel getResponseReplyePanel() {
        return mResponseReplyPanel;
    }
    
    public OutboundResponsePublisherPanel getResponseMessagePublisherPanel() {
        return mResponsePublisherPanel;
    }
    
    public JMSAdvancedPanel getAdvancedPanel() {
        return mAdvPanel;
    }    
    
    /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        if (mConnectionPanel != null) {
            mConnectionPanel.setOperationName(opName);
        }
        
        if (mResponseReplyPanel != null) {
            mResponseReplyPanel.setOperationName(opName);
        }        
    }
    
    /**
     * Enable the Processing Payload section accordingly
     * @param enable
     */
    public void enablePayloadProcessing(boolean enable) {
        if (mConnectionPanel != null) {
            mConnectionPanel.enablePayloadProcessing(enable);
        }
        
        if (mResponseReplyPanel != null) {
            mResponseReplyPanel.enablePayloadProcessing(enable);
        }        
    }
    
    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        // do FileBC-specific validation first

        boolean ok = validateMe();
        if (!ok) {
            return ok;
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

    public boolean validateMe() {
        boolean ok = true;
        // TODO validate required parameters and fire error if any failures
        return ok;
    }    
    
    private void initCustomComponents() {
        mConnectionPanel = new OutboundOneWayConnectionPanel(mQName, mComponent);
        mResponseReplyPanel = new OutboundResponseReplyPanel(mQName, mComponent);
        mResponsePublisherPanel = new OutboundResponsePublisherPanel(mQName, mComponent);
        mAdvPanel = new JMSAdvancedPanel(mQName, mComponent);
        
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "OutboundOneWayConnectionStepWizardPanel.TitleLabel"), mConnectionPanel);
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "OutboundResponseReplyStepWizardPanel.TitleLabel"), mResponseReplyPanel);
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "OutboundResponsePublisherStepWizardPanel.TitleLabel"), mResponsePublisherPanel);
        jTabbedPane1.addTab(NbBundle.getMessage(InboundOneWayMainPanel.class,
                "JMSAdvancedPanel.TitleLabel"), mAdvPanel);
    }
    
    void setProject(Project project) {
        if (mConnectionPanel != null) {
            mConnectionPanel.setProject(project);                    
        }         
        if (mResponseReplyPanel != null) {
            mResponseReplyPanel.setProject(project);                    
        }              
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
