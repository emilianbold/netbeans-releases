/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.iep.editor.tcg.ps;

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
        TcgComponentNode node = prop.getNode();
        TcgComponentNodePropertyEditor editor = (TcgComponentNodePropertyEditor)prop.getPropertyEditor();
        mCustomizerState = new TcgComponentNodePropertyCustomizerState();
        editor.attachCustomizerState(mCustomizerState);
        mCustomizer = editor.getCustomEditor();
        
        mTitle = NbBundle.getMessage(TcgComponentNodePropertyCustomizerDialogManager.class,
                "TcgComponentNodePropertyCustomizerDialogManager.Property_Editor_Title",
                node.getTypeDisplayName());
        
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
    private Window getDialog() {
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
        descriptor.setHelpCtx(new HelpCtx("iep_work_iepops"));
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
        manager.getDialog().setVisible(true);
    }
}
