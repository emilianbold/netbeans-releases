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

package org.netbeans.modules.iep.editor.tcg.ps;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.UIManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;


/**
 * Helper for showing custom property editor.
 *
 *
 * @author Bing Lu
 */
public class TcgComponentNodePropertyCustomizerDialogManager {
    /**
     * Custom property mCustomizer.
     */
    private Component mCustomizer;
    
    private TcgComponentNodePropertyCustomizerState mCustomizerState;
    
    /** Dialog instance. */
    private Window mDialog;
    
    /** Ok button */
    private JButton mOkButton;
    
    private String mTitle;
    private Object mDefaultOption;
    private Object[] mOptions;
    
    private ActionListener mActionListener;
    
    private TcgComponentNodePropertyCustomizerDialogManager(TcgComponentNodeProperty prop) {
        OperatorComponent comp = prop.getModelComponent();
        TcgComponentNodePropertyEditor editor = (TcgComponentNodePropertyEditor)prop.getPropertyEditor();
        mCustomizerState = new TcgComponentNodePropertyCustomizerState();
        editor.attachCustomizerState(mCustomizerState);
        mCustomizer = editor.getCustomEditor();
        
        String componentDisplayName = TcgPsI18n.getDisplayName(comp.getComponentType());
        
        mTitle = NbBundle.getMessage(TcgComponentNodePropertyCustomizerDialogManager.class,
                "TcgComponentNodePropertyCustomizerDialogManager.Property_Editor_Title",
                componentDisplayName);
        
        mActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object source = evt.getSource();
                if (source == mOkButton) {
                    if ((mCustomizerState != null) && (mCustomizerState.getState() == PropertyEnv.STATE_NEEDS_VALIDATION)) {
                        mCustomizerState.setState(PropertyEnv.STATE_VALID);
                        
                        if (mCustomizerState.getState() != PropertyEnv.STATE_VALID) {
                            // if the change was vetoed do nothing and return
                            return;
                        }
                    }
                }
                mDialog.dispose();
            }
        };
    }
    
    /**
     * Creates proper DialogDescriptor and obtain mDialog instance
     * via DialogDisplayer.getDialog() call.
     */
    private Window getDialog(TcgComponentNodeProperty prop) {
        mOkButton = new JButton(getString("CustomizerDialogManager.OK"));
        
        JButton cancelButton = new JButton(getString("CustomizerDialogManager.Cancel"));
        cancelButton.setVerifyInputWhenFocusTarget(false);
        cancelButton.setDefaultCapable(false);
        
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
            mOptions = new Object[] { cancelButton, mOkButton };
        } else {
            mOptions = new Object[] { mOkButton, cancelButton };
        }
        
        mDefaultOption = mOkButton;
        
        boolean isModal = true;
        DialogDescriptor descriptor = new DialogDescriptor(
                mCustomizer, mTitle, isModal, mOptions, mDefaultOption,
                DialogDescriptor.DEFAULT_ALIGN, null, mActionListener);
        
        String helpID = prop.getModelComponent().getHelpID();
//        if(helpID == null) {
//            helpID = "iep_work_iepops"; //NO I18N
//        }
        
        descriptor.setHelpCtx(new HelpCtx(helpID));
        //descriptor.setHelpCtx(new HelpCtx("iep_work_iepops"));
        mDialog = org.openide.DialogDisplayer.getDefault().createDialog(descriptor);
        // mDialog closing reactions
        mDialog.addWindowListener(new WindowAdapter() {
            /** Ensure that values are reverted when user cancelles dialog
             * by clicking on x image */
            public void windowClosing(WindowEvent e) {
                mDialog.dispose();
            }
            
            /** Remove property listener on window close */
            public void windowClosed(WindowEvent e) {
                mDialog.removeWindowListener(this);
            }
        });
        return mDialog;
    }
    
    
    private static String getString(String key) {
        return NbBundle.getBundle(TcgComponentNodePropertyCustomizerDialogManager.class).getString(key);
    }
    
    public static void showDialog(TcgComponentNodeProperty prop) {
        TcgComponentNodePropertyCustomizerDialogManager manager = new TcgComponentNodePropertyCustomizerDialogManager(prop);
        manager.getDialog(prop).setVisible(true);
    }
}
