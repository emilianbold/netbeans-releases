/*
 * CasaIB1WayMessagingMainPanel.java
 *
 */
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.ftp.validator.FTPComponentValidator;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sun Microsystems
 */
public class CasaFTPBindingEditorMainPanel extends javax.swing.JPanel implements ValidationProxy, PropertyChangeSupport {

    private Map editorCache = new HashMap();
    private static final String FTP_BINDING_SCHEME_MESSAGE = NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "FTPBCBindingScheme.message");
    private static final String FTP_BINDING_SCHEME_TRANSFER = NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "FTPBCBindingScheme.transfer");
    private WSDLComponent mComponent = null;
    private ActionListener mActionListener;
    private JPanel mCurrBindingConfigPanel;
    private String mLinkDirection;
    private String mOperationName;
    private String mBindingScheme;

    /** Creates new form InboundRequestResponseMessagingMainPanel */
    public CasaFTPBindingEditorMainPanel(WSDLComponent component, String linkDirection) {
        mLinkDirection = linkDirection;
        mComponent = component;
        initComponents();
        initCustomComponents();
        populateView(component);
    }

    public void populateView(WSDLComponent component) {
    }

    public boolean commit() {
        boolean result = false;
        if (mCurrBindingConfigPanel != null && mCurrBindingConfigPanel instanceof BindingConfigurationDelegate) {
            result = ((BindingConfigurationDelegate) mCurrBindingConfigPanel).commit();
        }
        return result;
    }

    public void enablePayloadProcessing(boolean enable) {
        if (mCurrBindingConfigPanel != null && mCurrBindingConfigPanel instanceof BindingConfigurationDelegate) {
            ((BindingConfigurationDelegate) mCurrBindingConfigPanel).enablePayloadProcessing(enable);
        }
    }

    public void setProject(Project project) {
        if (mCurrBindingConfigPanel != null && mCurrBindingConfigPanel instanceof BindingConfigurationDelegate) {
            ((BindingConfigurationDelegate) mCurrBindingConfigPanel).setProject(project);
        }
    }

    /**
     * Set the operation name to be configured
     * @param opName
     */
    void setOperationName(String opName) {
        mOperationName = opName;
        if (mCurrBindingConfigPanel != null && mCurrBindingConfigPanel instanceof BindingConfigurationDelegate) {
            ((BindingConfigurationDelegate) mCurrBindingConfigPanel).setOperationName(opName);
        }
    }

    /**
     * Validate the model
     * @return boolean true if model validation is successful; otherwise false
     */
    protected boolean validateContent() {
        boolean ok = validateMe();
        if (!ok) {
            return ok;
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

    public boolean validateMe() {
        boolean result = true;
        if ( mCurrBindingConfigPanel != null ) {
            result = ((BindingConfigurationDelegate)mCurrBindingConfigPanel).isValidConfiguration();
        }
        return result;
    }

    /**
     * initialize the binding scheme selection panel
     */
    private void initCustomComponents() {
        // populate view
        bindingSchemeComboBox.removeAllItems();
        bindingSchemeComboBox.addItem("");
        bindingSchemeComboBox.addItem(FTP_BINDING_SCHEME_MESSAGE);
        bindingSchemeComboBox.addItem(FTP_BINDING_SCHEME_TRANSFER);
        bindingSchemeComboBox.addActionListener(mActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == bindingSchemeComboBox) {
                    Object selected = bindingSchemeComboBox.getSelectedItem();
                    mBindingScheme = null;
                    if (selected != null && selected.toString().trim().length() > 0) {
                        mBindingScheme = selected.toString().trim();
                        addVisualPanel();
                    }
                }
            }
        });
    }

    private void addVisualPanel() {
        JPanel comp = null;
        if (mLinkDirection != null && mBindingScheme != null && mBindingScheme.length() > 0) {
//            String editorKey = mLinkDirection + "|" + mBindingScheme + "|" + mOperationName;
            if (mLinkDirection.equals(
                    ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION)) {
                // from direction of link, it is an inbound.  we need to look at
                // model to see if it is one way or request/response
//                editorKey = editorKey + "|" + ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION;
                if ((BindingComponentUtils.getInputBindingOperationCount(mComponent) > 0) &&
                        (BindingComponentUtils.getOutputBindingOperationCount(mComponent) > 0)) {
                    // 2 way - poll request - put response
//                    editorKey = editorKey + "|2WAY";
//                    Object editor = editorCache.get(editorKey);
//                    if (editor == null) {
                        if (mBindingScheme.equals(FTP_BINDING_SCHEME_MESSAGE)) {
                            comp = new CasaIBRequestResponseMessagingMainPanel(mComponent, this);
                        }
                        else if (mBindingScheme.equals(FTP_BINDING_SCHEME_TRANSFER)) {
                            comp = new CasaIBRequestResponseTransferingMainPanel(mComponent, this);
                        }
//                        editorCache.put(editorKey, comp);
//                    } 
//                    else {
//                        comp = (JPanel) editor;
//                    }
                } else if (BindingComponentUtils.getInputBindingOperationCount(mComponent) > 0) {
//                    editorKey = editorKey + "|1WAY";
                    // 1 way inbound - poll request
//                    Object editor = editorCache.get(editorKey);
//                    if (editor == null) {
                        if (mBindingScheme.equals(FTP_BINDING_SCHEME_MESSAGE)) {
                            comp = new CasaIB1WayMessagingMainPanel(mComponent, this);
                        } else {
                            comp = new CasaIB1WayTransferingMainPanel(mComponent, this);
                        }
//                        editorCache.put(editorKey, comp);
//                    } else {
//                        comp = (JPanel) editor;
//                    }
                }
            } else if (mLinkDirection.equals(ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION)) {
//                editorKey = editorKey + "|" + ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION;
                // from direction of link, it is an outbound.  we need now look 
                // model to see if one way, solicited read, or request/response
                if ((BindingComponentUtils.getInputBindingOperationCount(mComponent) > 0) &&
                        (BindingComponentUtils.getOutputBindingOperationCount(mComponent) > 0)) {
                    // 2 way - put request - poll response
                    // need to de-ambiguite - it can be solicit get / receive
                    boolean wantSolicit = promptForSolicitedRead();
//                    editorKey = editorKey + "|2WAY|" + (wantSolicit ? "solicit" : "outbound.req.resp");
//                    // outbound req resp or solicit get
//                    Object editor = editorCache.get(editorKey);
//                    if (editor == null) {
                        if (mBindingScheme.equals(FTP_BINDING_SCHEME_MESSAGE)) {
                            if ( wantSolicit ) {
                                comp = new CasaOBSolicitMessagingMainPanel(mComponent, this);
                            }
                            else {
                                comp = new CasaOBRequestResponseMessagingMainPanel(mComponent, this);
                            }
                        } else {
                            if ( wantSolicit ) {
                                comp = new CasaOBSolicitTransferingMainPanel(mComponent, this);
                            }
                            else {
                                comp = new CasaOBRequestResponseTransferingMainPanel(mComponent, this);
                            }
                        }
//                        editorCache.put(editorKey, comp);
//                    } else {
//                        comp = (JPanel) editor;
//                    }
                } else if (BindingComponentUtils.getInputBindingOperationCount(mComponent) > 0) {
                    // 1 way outbound - put request
//                    editorKey = editorKey + "|1WAY";
//                    // 1 way inbound - poll request
//                    Object editor = editorCache.get(editorKey);
//                    if (editor == null) {
                        if (mBindingScheme.equals(FTP_BINDING_SCHEME_MESSAGE)) {
                            comp = new CasaOB1WayMessagingMainPanel(mComponent, this);
                        } else {
                            comp = new CasaOB1WayTransferingMainPanel(mComponent, this);
                        }
//                        editorCache.put(editorKey, comp);
//                    } else {
//                        comp = (JPanel) editor;
//                    }
                }
            }

            if (mCurrBindingConfigPanel != null) {
                this.remove(mCurrBindingConfigPanel);
            }
            if (comp != null) {
                mCurrBindingConfigPanel = comp;
                ((BindingConfigurationDelegate)mCurrBindingConfigPanel).enablePayloadProcessing(false);
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.weightx = 0.5;
                gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
                this.add(comp, gridBagConstraints);
                revalidate();
                Window windowAncestor = SwingUtilities.getWindowAncestor(this);
                if (windowAncestor != null) {
                    windowAncestor.pack();
                }
                
            }
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

        bindingSchemeLab = new javax.swing.JLabel();
        bindingSchemeComboBox = new javax.swing.JComboBox();

        setName("Form"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        bindingSchemeLab.setLabelFor(bindingSchemeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(bindingSchemeLab, org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.bindingSchemeLab.text")); // NOI18N
        bindingSchemeLab.setToolTipText(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.bindingSchemeComboBox.tooltip")); // NOI18N
        bindingSchemeLab.setName("bindingSchemeLab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(bindingSchemeLab, gridBagConstraints);
        bindingSchemeLab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.bindingSchemeLab.text")); // NOI18N

        bindingSchemeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bindingSchemeComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.bindingSchemeComboBox.tooltip")); // NOI18N
        bindingSchemeComboBox.setName("bindingSchemeComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(bindingSchemeComboBox, gridBagConstraints);
        bindingSchemeComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.bindingSchemeComboBox.AccessibleContext.accessibleName")); // NOI18N
        bindingSchemeComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.bindingSchemeComboBox.tooltip")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class, "CasaFTPBindingEditorMainPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bindingSchemeComboBox;
    private javax.swing.JLabel bindingSchemeLab;
    // End of variables declaration//GEN-END:variables

    public void doFirePropertyChange(String evt, Object oldVal, Object newVal) {
        firePropertyChange(evt, oldVal, newVal);
    }

    public void validatePlugin() {
        validateMe();
    }
    
    private boolean promptForSolicitedRead() {
        boolean isSolicited = false;
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class,
                "CasaFTPBindingEditorMainPanel.IsSolicited"),
                NbBundle.getMessage(CasaFTPBindingEditorMainPanel.class,
                "CasaFTPBindingEditorMainPanel.IsSolicitedTitle"),
                NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        if (result.equals(NotifyDescriptor.YES_OPTION)) {
            isSolicited = true;                   
        }
        return isSolicited;
    }

    public ErrorDescription validateMe(boolean fireEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
