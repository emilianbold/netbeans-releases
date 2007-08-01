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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ClientOptionsPanel.java
 *
 * Created on July 25, 2005, 10:40 AM
 */
package org.netbeans.modules.mobility.end2end.multiview;

import java.util.Properties;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;

/**
 *
 * @author  Michal Skvor
 */
public class ClientOptionsPanel extends SectionInnerPanel {
    
    final private transient E2EDataObject dataObject;
    private transient Properties properties;
    
    final private static String TRUE = "true";
    final private static String FALSE = "false";
    
    
    /** Creates new form ClientOptionsPanel */
    public ClientOptionsPanel( SectionView sectionView, E2EDataObject dataObject ) {
        super( sectionView );
        
        this.dataObject = dataObject;
        
        dataObject.addSaveCallback( new SaveCallbackImpl());
        
        initComponents();
        
        initValues();
    }
    
    private void initValues() {
        final Configuration config = dataObject.getConfiguration();
        
        final ClientConfiguration cc = config.getClientConfiguration();
        properties = cc.getProperties();
        if( TRUE.equals( properties.getProperty( ClientConfiguration.PROP_DATABINDING ))) {
            clientDataBinding.setSelected( true );
        } else {
            clientDataBinding.setSelected( false );
        }
        if( TRUE.equals( properties.getProperty( ClientConfiguration.PROP_CREATE_STUBS ))) {
            clientCreateStubs.setSelected( true );
        } else {
            clientCreateStubs.setSelected( false );
        }
        if( TRUE.equals( properties.getProperty( ClientConfiguration.PROP_FLOATING_POINT ))) {
            clientFloatingPoint.setSelected( true );
        } else {
            clientFloatingPoint.setSelected( false );
        }
    }
    
    public JComponent getErrorComponent( @SuppressWarnings("unused")
	final String errorId ) {
        return null;
    }
    
    public void linkButtonPressed( @SuppressWarnings("unused")
	final Object ddBean, @SuppressWarnings("unused")
	final String ddProperty ) {
    }
    
    public void setValue( @SuppressWarnings("unused")
	final JComponent source, @SuppressWarnings("unused")
	final Object value ) {
    }
    
    private class SaveCallbackImpl implements E2EDataObject.SaveCallback {
        
        SaveCallbackImpl() {
            //to avoid creation of accessor class
        } 
        
        private final static String ID = "ClientOptionsPanelSaveCallback"; // NOI18N
        
        @SuppressWarnings("synthetic-access")
		public void save() {
            //System.err.println(" - Saving Option Panel");
            if( clientDataBinding.getSelectedObjects() == null ) {
                properties.setProperty( ClientConfiguration.PROP_DATABINDING, FALSE ); // NOI18N
            } else {
                properties.setProperty( ClientConfiguration.PROP_DATABINDING, TRUE ); // NOI18N
            }
            
            if( clientCreateStubs.getSelectedObjects() == null ) {
                properties.setProperty( ClientConfiguration.PROP_CREATE_STUBS, FALSE ); // NOI18N
            } else {
                properties.setProperty( ClientConfiguration.PROP_CREATE_STUBS, TRUE ); // NOI18N
            }
            
            if( clientFloatingPoint.getSelectedObjects() == null ) {
                properties.setProperty( ClientConfiguration.PROP_FLOATING_POINT, FALSE ); // NOI18N
            } else {
                properties.setProperty( ClientConfiguration.PROP_FLOATING_POINT, TRUE ); // NOI18N
            }
            
        }
        
        public int hashCode() {
            return ID.hashCode();
        }
        
        public boolean equals( final Object obj ) {
            if( obj instanceof SaveCallbackImpl ) {
                return true;
            }
            return false;
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        clientCreateStubs = new javax.swing.JCheckBox();
        clientFloatingPoint = new javax.swing.JCheckBox();
        clientDataBinding = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        clientCreateStubs.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(clientCreateStubs, org.openide.util.NbBundle.getMessage(ClientOptionsPanel.class, "LBL_Generate_Stubs")); // NOI18N
        clientCreateStubs.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientCreateStubs.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientCreateStubs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientCreateStubsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(clientCreateStubs, gridBagConstraints);

        clientFloatingPoint.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(clientFloatingPoint, org.openide.util.NbBundle.getMessage(ClientOptionsPanel.class, "LABEL_Floating_Point")); // NOI18N
        clientFloatingPoint.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientFloatingPoint.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientFloatingPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientFloatingPointActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(clientFloatingPoint, gridBagConstraints);

        clientDataBinding.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(clientDataBinding, org.openide.util.NbBundle.getMessage(ClientOptionsPanel.class, "LABEL_Client_DataBinding")); // NOI18N
        clientDataBinding.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientDataBinding.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientDataBinding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientDataBindingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(clientDataBinding, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void clientDataBindingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientDataBindingActionPerformed
        dataObject.setModified( true );
}//GEN-LAST:event_clientDataBindingActionPerformed
    
    private void clientCreateStubsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientCreateStubsActionPerformed
        dataObject.setModified( true );
    }//GEN-LAST:event_clientCreateStubsActionPerformed
    
    private void clientFloatingPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientFloatingPointActionPerformed
        dataObject.setModified( true );
    }//GEN-LAST:event_clientFloatingPointActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox clientCreateStubs;
    private javax.swing.JCheckBox clientDataBinding;
    private javax.swing.JCheckBox clientFloatingPoint;
    // End of variables declaration//GEN-END:variables
    
}
