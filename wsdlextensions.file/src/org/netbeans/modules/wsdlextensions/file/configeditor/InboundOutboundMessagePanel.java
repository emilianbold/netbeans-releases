/*
 * InboundOutboundMessagePanel.java
 *
 * Created on August 10, 2008, 12:21 PM
 */

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.util.Collection;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.file.validator.FileComponentValidator;
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
public class InboundOutboundMessagePanel extends javax.swing.JPanel {

    private InboundMessagePanel mInboundPanel = null;
    private OutboundMessagePanel mOutboundPanel = null;
    private WSDLComponent mComponent = null;
    private QName mQName = null;
    private Project mProject = null;
    
    /** style document for description area **/
    private StyledDocument mDoc = null;
    private StyledDocument mDocAdv = null;
    private StyledDocument mDocAdvOut = null;
    private String[] mStyles = null;    
    
    /** Creates new form InboundOutboundMessagePanel */
    public InboundOutboundMessagePanel(WSDLComponent component) {
        mComponent = component;
        initComponents();
        populateView(component);
        setAccessibility();
    }

    public void populateView(WSDLComponent component) {
        if (mInboundPanel != null) {
            mInboundPanel.populateView(null, component);  
            mInboundPanel.setProject(mProject);
        }
        
        if (mOutboundPanel != null) {
            mOutboundPanel.populateView(null, component); 
            mOutboundPanel.setProject(mProject);
        }        
    }
    
    /**
     * Return the inbound panel
     * @return
     */
    public InboundMessagePanel getInboundPanel() {
        return mInboundPanel;
    }
    
    /**
     * Return the outbound panel
     * @return
     */
    public OutboundMessagePanel getOutboundPanel() {
        return mOutboundPanel;
    }
    
    /**
     * Set the Project associated with the wsdl for this panel
     * @param project
     */      
    void setProject(Project project) {
        mProject = project;
        if (mInboundPanel != null) {
            mInboundPanel.setProject(project);
        }
        
        if (mOutboundPanel != null) {
            mOutboundPanel.setProject(project);
        }        
    }    
    
    /**
     * Set the operation name to be configured for both inbound and outbound
     * @param opName
     */
    void setOperationName(String opName) {
        if (opName != null) {
            mInboundPanel.setOperationName(opName);
            mOutboundPanel.setOperationName(opName);
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
        
        if (mOutboundPanel != null) {
            mOutboundPanel.enablePayloadProcessing(enable);
        }        
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
        FileError fileError = validateMe(true);
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(fileError.getErrorMode())) {
            return false;
        }

        ValidationResult results = new FileComponentValidator().
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
                    PROPERTY_CLEAR_MESSAGES_EVT, null, "");
            return true;
        }

    }

    
    public FileError validateMe() {
        return validateMe(false);
    }
    
    public FileError validateMe(boolean fireEvent) {    
        FileError fileError = mInboundPanel.validateMe(fireEvent);    
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_CLEAR_MESSAGES_EVT.equals(fileError.getErrorMode())) {
            fileError = mOutboundPanel.validateMe(fireEvent); 
        }

        return fileError;
    }       
    
    private void setAccessibility() {
        String name = NbBundle.getMessage(InboundOutboundMessagePanel.class,
                "InboundMessagePanel.StepLabel") +   // NOI18N
                NbBundle.getMessage(InboundOutboundMessagePanel.class,
                "OutboundMessagePanel.StepLabel");  // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleName(name);
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(name);
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
        mInboundPanel = new InboundMessagePanel(mQName, mComponent);
        mOutboundPanel = new OutboundMessagePanel(mQName, mComponent);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "InboundOutboundMessagePanel.inboundTabPanel.TabConstraints.tabTitle"), mInboundPanel); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FileBindingConfigurationPanel.class, "InboundOutboundMessagePanel.outboundTabPanel.TabConstraints.tabTitle"), mOutboundPanel); // NOI18N

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
