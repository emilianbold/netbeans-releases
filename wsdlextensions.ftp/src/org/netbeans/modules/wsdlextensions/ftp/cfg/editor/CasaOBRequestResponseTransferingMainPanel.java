/*
 * InboundRequestResponseMessagingMainPanel.java
 *
 */

package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.ftp.validator.FTPComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

/**
 * @author Sun Microsystems
 */
public class CasaOBRequestResponseTransferingMainPanel extends javax.swing.JPanel implements BindingConfigurationDelegate {

    private FTPSettingsRequestResponseTransferPanel mFTPReqRespTransferPanel = null;
    private InboundTransferPanel mIBTransferPanel = null;
    private OutboundTransferPanel mOBTransferPanel = null;
    private WSDLComponent mComponent = null;
    private QName mQName = null;
    private PropertyChangeSupport mProxy;
    private ChangeListener l;
    private int mCurrentTabIndex;
    
    /** Creates new form CasaOBRequestResponseTransferingMainPanel */
    public CasaOBRequestResponseTransferingMainPanel(WSDLComponent component, PropertyChangeSupport proxy) {
        mComponent = component;
        mProxy = proxy;
        initComponents();
        initCustomComponents();
        populateView(component);
    }

    public void populateView(WSDLComponent component) {
        boolean correlateEnabled = false;
        
        if (mIBTransferPanel != null) {
            mIBTransferPanel.populateView(null, component);                    
            correlateEnabled = correlateEnabled || mIBTransferPanel.getRequestResponseCorrelate();
        }     
        
        if (mOBTransferPanel != null) {
            mOBTransferPanel.populateView(null, component);                    
            correlateEnabled = correlateEnabled || mOBTransferPanel.getRequestResponseCorrelate();
        }         

        if (mFTPReqRespTransferPanel != null) {
            mFTPReqRespTransferPanel.populateView(null, component);
            mFTPReqRespTransferPanel.setCorrelate(correlateEnabled);
        }
    }

    public boolean commit() {
        boolean result = true;
        Map p = null;
        
        if (mFTPReqRespTransferPanel != null) {
            result = result && mFTPReqRespTransferPanel.commit();
            p = mFTPReqRespTransferPanel.exportProperties();
        }
        
        if (mIBTransferPanel != null) {
            if ( p != null )
                mIBTransferPanel.importProperties(p);
            result = result && mIBTransferPanel.commit();
        }     
        
        if (mOBTransferPanel != null) {
            if ( p != null )
                mOBTransferPanel.importProperties(p);
            result = result && mOBTransferPanel.commit();
        }         
        return result;
    }

    public void enablePayloadProcessing(boolean enable) {
        if (mIBTransferPanel != null) {
            mIBTransferPanel.enablePayloadProcessing(enable);
        }     
        
        if (mOBTransferPanel != null) {
            mOBTransferPanel.enablePayloadProcessing(enable);
        }         
    }
    
    public void setProject(Project project) {
        if (mFTPReqRespTransferPanel != null) {
            mFTPReqRespTransferPanel.setProject(project);                    
        }
        
        if (mIBTransferPanel != null) {
            mIBTransferPanel.setProject(project);                    
        }     
        
        if (mOBTransferPanel != null) {
            mOBTransferPanel.setProject(project);                    
        }         
    }    
   
    public FTPSettingsRequestResponseTransferPanel getConnectionPanel() {
        return mFTPReqRespTransferPanel;
    }
    
    public InboundTransferPanel getIBTransferPanel() {
        return mIBTransferPanel;
    }
    
    public OutboundTransferPanel getOBTransferPanel() {
        return mOBTransferPanel;
    }
    
    /**
     * Set the operation name to be configured
     * @param opName
     */
    public void setOperationName(String opName) {
        if (mFTPReqRespTransferPanel != null) {
            mFTPReqRespTransferPanel.setOperationName(opName);
        }
        
        if (mIBTransferPanel != null) {
            mIBTransferPanel.setOperationName(opName);
        }  
        
        if (mOBTransferPanel != null) {
            mOBTransferPanel.setOperationName(opName);
        }           
    }
    
    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        ErrorDescription desc = validateMe();
        if ( desc != null || desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT) ) {
            return false;
        }

        ValidationResult results = new FTPComponentValidator().
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

    public ErrorDescription validateMe() {
        // validate each pane in turn
        int tabCnt = jTabbedPane1.getTabCount();
        ErrorDescription desc = null;
        Component comp = jTabbedPane1.getSelectedComponent();
        
        // validate curr first
        if ( comp != null ) {
            desc = ((BindingConfigurationDelegate)comp).validateMe();
            if ( desc != null && desc.getErrorMode() != null ) {
                // pre-pend title of the tab
                desc.setErrorMessage(comp.getName() + ":" + desc.getErrorMessage());
            }
        }
        
        if ( desc == null 
                    || desc.getErrorMode() == null ) {
            // if curr ok, further validate rest
            // but skip current
            for ( int i = 0; i < tabCnt; i++ ) {
                comp = jTabbedPane1.getComponentAt(i);
                desc = ((BindingConfigurationDelegate)comp).validateMe();
                if ( desc != null 
                        && desc.getErrorMode() != null 
                        && desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT) ) {
                    desc.setErrorMessage(comp.getName() + ":" + desc.getErrorMessage());
                    break;
                }
            }
        }
        
        return desc;
    }    
    
    private void initCustomComponents() {
        mFTPReqRespTransferPanel = new FTPSettingsRequestResponseTransferPanel(mQName, mComponent, mProxy);
        mIBTransferPanel = new InboundTransferPanel(mQName, mComponent, false, false, false, mProxy);
        mOBTransferPanel = new OutboundTransferPanel(mQName, mComponent, false, true, mProxy);
        
        jTabbedPane1.addTab("1." + mFTPReqRespTransferPanel.getName(), mFTPReqRespTransferPanel);
        jTabbedPane1.addTab("2." + mOBTransferPanel.getName(), mOBTransferPanel); // put req
        jTabbedPane1.addTab("3." + mIBTransferPanel.getName(), mIBTransferPanel); // poll resp

        jTabbedPane1.setMnemonicAt(0, KeyEvent.VK_1);
        jTabbedPane1.setMnemonicAt(1, KeyEvent.VK_2);
        jTabbedPane1.setMnemonicAt(2, KeyEvent.VK_3);
        mCurrentTabIndex = 0;
        jTabbedPane1.addChangeListener(l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if ( e.getSource() == jTabbedPane1 ) {
                    int si = jTabbedPane1.getSelectedIndex();
                    if ( si == 0 || si == 1 || si == 2) {
                        if ( si != mCurrentTabIndex ) {
                            // tab switched
                            Component compTo = jTabbedPane1.getSelectedComponent();
                            Component compFrom = jTabbedPane1.getComponentAt(mCurrentTabIndex);
                            if ( compTo instanceof PropertyAccessible 
                                    && compFrom instanceof PropertyAccessible ) {
                                Map p = ((PropertyAccessible)compFrom).exportProperties();
                                if ( p != null && p.size() > 0 ) {
                                    ((PropertyAccessible)compTo).importProperties(p);
                                }
                            }
                            else {
                                // report error
                            }
                        }
                    }
                }
            }
        });
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
        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CasaOBRequestResponseTransferingMainPanel.class, "CasaOBRequestResponseTransferingMainPanel.jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CasaOBRequestResponseTransferingMainPanel.class, "CasaOBRequestResponseTransferingMainPanel.jTabbedPane1.AccessibleContext.accessibleDescription")); // NOI18N

        add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel1.getAccessibleContext().setAccessibleName("CASA Outbound Request Response Transferring Main Panel");
        jPanel1.getAccessibleContext().setAccessibleDescription("CASA Outbound Request Response Transferring Main Panel");
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    public boolean isValidConfiguration() {
        // validate each pane in turn
        boolean result = true;
        ErrorDescription desc = validateMe();
        if ( desc != null 
                && desc.getErrorMode() != null 
                && desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT) ) {
                ((PropertyChangeSupport)mProxy).doFirePropertyChange(desc.getErrorMode(), null, desc.getErrorMessage());
        }
        else {
            ((PropertyChangeSupport)mProxy).doFirePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
        return result;
    }

}
