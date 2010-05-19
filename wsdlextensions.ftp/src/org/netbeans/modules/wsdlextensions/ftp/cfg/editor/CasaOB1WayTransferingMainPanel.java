/*
 * InboundRequestResponseMessagingMainPanel.java
 *
 */
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.Collection;
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
 *
 * @author  Sun Microsystems
 */
public class CasaOB1WayTransferingMainPanel extends javax.swing.JPanel implements BindingConfigurationDelegate {

    private FTPSettingsOneWayPanel mFTP1WayPanel = null;
    private OutboundTransferPanel mOBTransferPanel = null;
    private WSDLComponent mComponent = null;
    private QName mQName = null;
    private PropertyChangeSupport mProxy;

    /** Creates new form InboundRequestResponseMessagingMainPanel */
    public CasaOB1WayTransferingMainPanel(WSDLComponent component, PropertyChangeSupport proxy) {
        mComponent = component;
        mProxy = proxy;
        initComponents();
        initCustomComponents();
        populateView(component);
    }

    public void populateView(WSDLComponent component) {
        if (mFTP1WayPanel != null) {
            mFTP1WayPanel.populateView(null, component);
        }

        if (mOBTransferPanel != null) {
            mOBTransferPanel.populateView(null, component);
        }
    }

    public boolean commit() {
        boolean result = true;
        if (mFTP1WayPanel != null) {
            result = result && mFTP1WayPanel.commit();
        }

        if (mOBTransferPanel != null) {
            result = result && mOBTransferPanel.commit();
        }
        return result;
    }

    public void enablePayloadProcessing(boolean enable) {
        if (mOBTransferPanel != null) {
            mOBTransferPanel.enablePayloadProcessing(enable);
        }
    }

    public void setProject(Project project) {
        if (mFTP1WayPanel != null) {
            mFTP1WayPanel.setProject(project);
        }

        if (mOBTransferPanel != null) {
            mOBTransferPanel.setProject(project);
        }
    }

    public FTPSettingsOneWayPanel getConnectionPanel() {
        return mFTP1WayPanel;
    }

    public OutboundTransferPanel getOBTransferPanel() {
        return mOBTransferPanel;
    }

    /**
     * Set the operation name to be configured
     * @param opName
     */
    public void setOperationName(String opName) {
        if (mFTP1WayPanel != null) {
            mFTP1WayPanel.setOperationName(opName);
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
        if (desc != null || desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
            return false;
        }

        ValidationResult results = new FTPComponentValidator().validate(mComponent.getModel(), null, ValidationType.COMPLETE);
        Collection<ResultItem> resultItems = results.getValidationResult();
        ResultItem firstResult = null;
        String type = ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT;
        boolean result = true;
        if (resultItems != null && !resultItems.isEmpty()) {
            for (ResultItem item : resultItems) {
                if (item.getType() == ResultType.ERROR) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT;
                    result = false;
                    break;
                } else if (firstResult == null) {
                    firstResult = item;
                    type = ExtensibilityElementConfigurationEditorComponent.PROPERTY_WARNING_EVT;
                }
            }
        }
        if (firstResult != null) {
            firePropertyChange(type, null, firstResult.getDescription());
            return result;
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, null);
            return true;
        }

    }

    public ErrorDescription validateMe() {
        // validate each pane in turn
        int tabCnt = jTabbedPane1.getTabCount();
        ErrorDescription desc = null;
        Component comp = jTabbedPane1.getSelectedComponent();

        // validate curr first
        if (comp != null) {
            desc = ((BindingConfigurationDelegate) comp).validateMe();
            if (desc != null && desc.getErrorMode() != null) {
                // pre-pend title of the tab
                desc.setErrorMessage(comp.getName() + ":" + desc.getErrorMessage());
            }
        }

        if (desc == null || desc.getErrorMode() == null) {
            // if curr ok, further validate rest
            // but skip current
            for (int i = 0; i < tabCnt; i++) {
                comp = jTabbedPane1.getComponentAt(i);
                desc = ((BindingConfigurationDelegate) comp).validateMe();
                if (desc != null && desc.getErrorMode() != null && desc.getErrorMode().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
                    desc.setErrorMessage(comp.getName() + ":" + desc.getErrorMessage());
                    break;
                }
            }
        }

        return desc;
    }

    private void initCustomComponents() {
        mFTP1WayPanel = new FTPSettingsOneWayPanel(mQName, mComponent, mProxy);
        mOBTransferPanel = new OutboundTransferPanel(mQName, mComponent, true, true, mProxy);

        jTabbedPane1.addTab("1." + mFTP1WayPanel.getName(), mFTP1WayPanel);
        jTabbedPane1.addTab("2." + mOBTransferPanel.getName(), mOBTransferPanel);
        jTabbedPane1.setMnemonicAt(0, KeyEvent.VK_1);
        jTabbedPane1.setMnemonicAt(1, KeyEvent.VK_2);
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
        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CasaOB1WayTransferingMainPanel.class, "CasaOB1WayTransferingMainPanel.jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CasaOB1WayTransferingMainPanel.class, "CasaOB1WayTransferingMainPanel.jTabbedPane1.AccessibleContext.accessibleDescription")); // NOI18N

        add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel1.getAccessibleContext().setAccessibleName("CASA Outbound Oneway Transferring Main Panel");
        jPanel1.getAccessibleContext().setAccessibleDescription("CASA Outbound Oneway Transferring Main Panel");
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
