/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.beans.editor.VersionEditor;
import org.netbeans.modules.xml.tax.beans.editor.EncodingEditor;
import org.netbeans.modules.xml.tax.beans.editor.StandaloneEditor;
import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocumentCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = 8592875472261625357L;
    
    
    //
    // init
    //
    
    /** */
    public TreeDocumentCustomizer () {
        super ();
        
        initComponents ();
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeDocumentCustomizer::init"); // NOI18N
        
        versionLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_document_version")); // NOI18N
        encodingLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_document_encoding")); // NOI18N
        standaloneLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_document_standalone")); // NOI18N
        
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeDocument getDocument () {
        return (TreeDocument)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeDocument.PROP_VERSION)) {
            updateVersionComponent ();
        } else if (pche.getPropertyName ().equals (TreeDocument.PROP_ENCODING)) {
            updateEncodingComponent ();
        } else if (pche.getPropertyName ().equals (TreeDocument.PROP_STANDALONE)) {
            updateStandaloneComponent ();
        }
    }
    
    
    /**
     */
    protected final void updateDocumentVersion () {
        if ( cbVersion.getSelectedItem () == null ) {
            return;
        }
        
        try {
            getDocument ().setVersion (text2null ((String) cbVersion.getSelectedItem ()));
        } catch (TreeException exc) {
            updateVersionComponent ();
            TAXUtil.notifyTreeException (exc);
        }
        
    }
    
    /**
     */
    protected final void updateVersionComponent () {
        cbVersion.setSelectedItem (null2text (getDocument ().getVersion ()));
    }
    
    /**
     */
    protected final void updateDocumentEncoding () {
        if ( cbEncoding.getSelectedItem () == null ) {
            return;
        }
        
        try {
            getDocument ().setEncoding (text2null ((String) cbEncoding.getSelectedItem ()));
        } catch (TreeException exc) {
            updateEncodingComponent ();
            TAXUtil.notifyTreeException (exc);
        }
        
    }
    
    /**
     */
    protected final void updateEncodingComponent () {
        cbEncoding.setSelectedItem (null2text (getDocument ().getEncoding ()));
    }
    
    /**
     */
    protected final void updateDocumentStandalone () {
        if ( cbStandalone.getSelectedItem () == null ) {
            return;
        }
        
        try {
            getDocument ().setStandalone (text2null ((String) cbStandalone.getSelectedItem ()));
        } catch (TreeException exc) {
            updateStandaloneComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateStandaloneComponent () {
        cbStandalone.setSelectedItem (null2text (getDocument ().getStandalone ()));
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateVersionComponent ();
        updateEncodingComponent ();
        updateStandaloneComponent ();
    }
    
    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        
        cbVersion.setEnabled (editable);
        cbEncoding.setEnabled (editable);
        cbStandalone.setEnabled (editable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        versionLabel = new javax.swing.JLabel();
        cbVersion = new javax.swing.JComboBox();
        encodingLabel = new javax.swing.JLabel();
        cbEncoding = new javax.swing.JComboBox();
        standaloneLabel = new javax.swing.JLabel();
        cbStandalone = new javax.swing.JComboBox();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        versionLabel.setText(Util.THIS.getString ("PROP_document_version"));
        versionLabel.setLabelFor(cbVersion);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(versionLabel, gridBagConstraints);

        cbVersion.setModel((new javax.swing.DefaultComboBoxModel(VersionEditor.getItems())));
        cbVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbVersionActionPerformed(evt);
            }
        });

        cbVersion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cbVersionFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(cbVersion, gridBagConstraints);

        encodingLabel.setText(Util.THIS.getString ("PROP_document_encoding"));
        encodingLabel.setLabelFor(cbEncoding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(encodingLabel, gridBagConstraints);

        cbEncoding.setModel(new javax.swing.DefaultComboBoxModel(EncodingEditor.getItems())
        );
        cbEncoding.setEditable(true);
        cbEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEncodingActionPerformed(evt);
            }
        });

        cbEncoding.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cbEncodingFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(cbEncoding, gridBagConstraints);

        standaloneLabel.setText(Util.THIS.getString ("PROP_document_standalone"));
        standaloneLabel.setLabelFor(cbStandalone);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(standaloneLabel, gridBagConstraints);

        cbStandalone.setModel(new javax.swing.DefaultComboBoxModel(StandaloneEditor.getItems()));
        cbStandalone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStandaloneActionPerformed(evt);
            }
        });

        cbStandalone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cbStandaloneFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(cbStandalone, gridBagConstraints);

        fillPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void cbStandaloneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cbStandaloneFocusLost
        // Add your handling code here:
        updateDocumentStandalone ();
    }//GEN-LAST:event_cbStandaloneFocusLost
    
    private void cbStandaloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStandaloneActionPerformed
        // Add your handling code here:
        updateDocumentStandalone ();
    }//GEN-LAST:event_cbStandaloneActionPerformed
    
    private void cbEncodingFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cbEncodingFocusLost
        // Add your handling code here:
        updateDocumentEncoding ();
    }//GEN-LAST:event_cbEncodingFocusLost
    
    private void cbEncodingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEncodingActionPerformed
        // Add your handling code here:
        updateDocumentEncoding ();
    }//GEN-LAST:event_cbEncodingActionPerformed
    
    private void cbVersionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cbVersionFocusLost
        // Add your handling code here:
        updateDocumentVersion ();
    }//GEN-LAST:event_cbVersionFocusLost
    
    private void cbVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbVersionActionPerformed
        // Add your handling code here:
        updateDocumentVersion ();
    }//GEN-LAST:event_cbVersionActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel standaloneLabel;
    private javax.swing.JComboBox cbVersion;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JComboBox cbEncoding;
    private javax.swing.JComboBox cbStandalone;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel fillPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeDocumentCustomizer"));
        
        cbVersion.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_cbVersion"));
        cbEncoding.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_cbEncoding1"));
        cbStandalone.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_cbStandalone"));
    }
}
