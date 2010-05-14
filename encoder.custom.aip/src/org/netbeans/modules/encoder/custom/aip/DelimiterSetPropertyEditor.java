/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.custom.aip;

import com.sun.encoder.custom.appinfo.DelimiterSet;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Editor for the delimiter set property
 *
 * @author Jun Xu
 */
public class DelimiterSetPropertyEditor  extends PropertyEditorSupport
    implements ExPropertyEditor {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/Bundle");
    private final EncodingOption mEncodingOption;
    
    /**
     * Creates a new instance of DerivationTypeEditor
     */
    public DelimiterSetPropertyEditor(EncodingOption encodingOption) {
        mEncodingOption = encodingOption;
        initialize(encodingOption);
    }
    
    private void initialize(EncodingOption encodingOption) {
    }

    public String getAsText() {
        DelimiterSet delimSet = (DelimiterSet) getValue();
        if (delimSet == null || delimSet.sizeOfLevelArray() == 0){
            return _bundle.getString("delim_set_pe.lbl.not_specified");
        }
        
        return NbBundle.getMessage(DelimiterSetPropertyEditor.class, "delim_set_pe.lbl.levels", delimSet.sizeOfLevelArray()); //NOI18N
    }
    
    public Component getCustomEditor() {
        DelimiterSet dupDelimSet = mEncodingOption.getDelimiterSet();
        if (dupDelimSet == null) {
            dupDelimSet = DelimiterSet.Factory.newInstance();
        } else {
            dupDelimSet = (DelimiterSet) dupDelimSet.copy();
        }
        final DelimiterSetForm delimSetForm =
                new DelimiterSetForm(dupDelimSet);
        final DialogDescriptor descriptor =
                new DialogDescriptor(delimSetForm,
                    NbBundle.getMessage(DelimiterSetPropertyEditor.class, "LBL_Delimiter_Set_Editor_Title"), //NOI18N
                    true,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                                try {
                                    Node root = delimSetForm.getExplorerManager().getRootContext();
                                    DelimiterSet delimSet = (DelimiterSet) root.getLookup().lookup(DelimiterSet.class);
                                    if (delimSet.sizeOfLevelArray() > 0) {
                                        setValue(delimSet);
                                    } else {
                                        setValue(null);
                                    }
                                } catch (IllegalArgumentException iae) {
                                    ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                            iae.getMessage(), iae.getLocalizedMessage(), 
                                            null, new java.util.Date());
                                    throw iae;
                                }
                            }
                        }
                    }
                );
                
        // enable/disable the dlg ok button depending selection
        delimSetForm.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("valid")) { //NOI18N
                    descriptor.setValid(((Boolean)evt.getNewValue()).booleanValue());
                }
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(
                new java.awt.Dimension(740, 450));
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                delimSetForm.setVisible( true );
            }
        } );
        delimSetForm.setVisible( false );
        return dlg;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }
}