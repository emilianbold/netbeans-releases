/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.tmap.ui.editors;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;

/**
 * @author  Vitaly Bychkov
 */
public class TransformPanel extends EditorLifeCycleAdapter
        implements Validator.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<Transform> myEditor;
    private QName myFaultName;
    
    private DefaultValidator myValidator;
    
    /**
     * If true then the normal output is implied.
     * Otherwize the fault output is implied.
     */
    private boolean useNormalOutput;
    
    public TransformPanel(CustomNodeEditor<Transform> anEditor) {
        this.myEditor = anEditor;
        createContent();
    }
    
    @Override
    public void createContent() {
        //
        initComponents();
        bindControls2PropertyNames();
        
        //
        myEditor.getValidStateManager(true).addValidStateListener(
                new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
    }
    
    /**
     * Binds simple controls to names of properties.
     * This is necessary for automatic value inicialization and value inquiry.
     */
    private void bindControls2PropertyNames() {
        fldName.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, PropertyType.NAME);
    }
    
    @Override
    public boolean initControls() {
        Transform transform = myEditor.getEditedObject();
        if (transform != null) {
        }
        //
//        updateEnabledState();
        //
        getValidator().revalidate(true);
        //
        return true;
    }
    
    @Override
    public boolean applyNewValues() throws VetoException {
        //
        Transform transform = myEditor.getEditedObject();
        if (transform != null) {
            //
            // if xsl file doesn't exist - create it
        }
        return true;
    }
    
    private Collection<QName> getAllowedFaultTypesQName() {
        Collection<QName> faultTypes = new HashSet<QName>();
        Object item = cbxResultVar.getSelectedItem();
        
        if (item == null || myFaultName == null) {
            return faultTypes;
        }
        Operation operation = (Operation) item;
        Collection<Fault> faults = operation.getFaults();
        
        WSDLModel model = operation.getModel();
        String namespace = model.getDefinitions().getTargetNamespace();
//System.out.println();
//System.out.println("my  namespace: " + myFaultName.getNamespaceURI());
//System.out.println("see namespace: " + namespace);
        
        if ( !namespace.equals(myFaultName.getNamespaceURI())) {
            return faultTypes;
        }
        String location = myFaultName.getLocalPart();
//System.out.println("my   location: " + location);
        
        for (Fault fault : faults) {
//System.out.println("see  location: " + fault.getName());
            if ( !fault.getName().equals(location)) {
                continue;
            }
            NamedComponentReference<Message> faultMsgRef = fault.getMessage();
            
            if (faultMsgRef == null) {
                continue;
            }
            QName faultMsgQName = faultMsgRef.getQName();
            
            if (faultMsgQName != null) {
                faultTypes.add(faultMsgQName);
            }
        }
        return faultTypes;
    }
        
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, TransformPanel.class) {
                
                public void doFastValidation() {
//                    addReasonKey(Severity.ERROR, 
//                            "ERR_OPERATION_NO_OUTPUT",
//                            ((Operation) item).getName()); // NOI18N
                }
                
                @Override
                public void doDetailedValidation() {
                }                
            };
        }
        return myValidator;
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        fldName = new javax.swing.JTextField();
        lblSourceVar = new javax.swing.JLabel();
        cbxSourceVar = new javax.swing.JComboBox();
        lblResultVar = new javax.swing.JLabel();
        cbxResultVar = new javax.swing.JComboBox();
        lblXslFile = new javax.swing.JLabel();
        fldXslFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        lblErrorMessage = new javax.swing.JLabel();

        lblName.setLabelFor(fldName);
        lblName.setText("null");

        fldName.setColumns(40);
        fldName.setName(""); // NOI18N

        lblSourceVar.setLabelFor(cbxSourceVar);
        lblSourceVar.setText("null");

        lblResultVar.setLabelFor(cbxResultVar);
        lblResultVar.setText("null");

        lblXslFile.setLabelFor(fldXslFile);
        lblXslFile.setText("null");

        fldXslFile.setColumns(30);
        fldXslFile.setEditable(false);

        btnBrowse.setText("null");
        btnBrowse.setMargin(new java.awt.Insets(0, 2, 0, 2));

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setAlignmentX(0.5F);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblSourceVar)
                            .add(lblResultVar)
                            .add(lblName)
                            .add(lblXslFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fldXslFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(btnBrowse))
                            .add(cbxResultVar, 0, 503, Short.MAX_VALUE)
                            .add(cbxSourceVar, 0, 503, Short.MAX_VALUE)
                            .add(fldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(fldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblSourceVar)
                    .add(cbxSourceVar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblResultVar)
                    .add(cbxResultVar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnBrowse)
                    .add(lblXslFile)
                    .add(fldXslFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(112, 112, 112)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblName.getAccessibleContext().setAccessibleName("null");
        lblName.getAccessibleContext().setAccessibleDescription("null");
        fldName.getAccessibleContext().setAccessibleName("null");
        fldName.getAccessibleContext().setAccessibleDescription("null");
        lblSourceVar.getAccessibleContext().setAccessibleName("null");
        lblSourceVar.getAccessibleContext().setAccessibleDescription("null");
        cbxSourceVar.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransformPanel.class, "ACSN_CBX_PartnerLink")); // NOI18N
        cbxSourceVar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransformPanel.class, "ACSD_CBX_PartnerLink")); // NOI18N
        lblResultVar.getAccessibleContext().setAccessibleName("null");
        lblResultVar.getAccessibleContext().setAccessibleDescription("null");
        cbxResultVar.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransformPanel.class, "ACSN_CBX_Operation")); // NOI18N
        cbxResultVar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransformPanel.class, "ACSD_CBX_Operation")); // NOI18N
        lblXslFile.getAccessibleContext().setAccessibleName("null");
        lblXslFile.getAccessibleContext().setAccessibleDescription("null");
        fldXslFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransformPanel.class, "ACSN_TXTFLD_OutputVariable")); // NOI18N
        fldXslFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransformPanel.class, "ACSD_TXTFLD_OutputVariable")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TransformPanel.class,"ACSN_BTN_BrowseOutputVarible")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TransformPanel.class,"ACSD_BTN_BrowseOutputVarible")); // NOI18N

        getAccessibleContext().setAccessibleName("null");
        getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbxResultVar;
    private javax.swing.JComboBox cbxSourceVar;
    private javax.swing.JTextField fldName;
    private javax.swing.JTextField fldXslFile;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblResultVar;
    private javax.swing.JLabel lblSourceVar;
    private javax.swing.JLabel lblXslFile;
    // End of variables declaration//GEN-END:variables
}
