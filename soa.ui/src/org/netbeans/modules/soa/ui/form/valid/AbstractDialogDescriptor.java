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
package org.netbeans.modules.soa.ui.form.valid;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.netbeans.modules.soa.ui.form.CommonUiBundle;
import org.netbeans.modules.soa.ui.form.FormLifeCycle;
import org.netbeans.modules.soa.ui.form.valid.Validator.Reason;
import org.netbeans.modules.soa.ui.form.valid.Validator.Severity;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 * Base class for DialogDescriptors which support the dialog's validation and life cycle
 *
 * @author nk160297
 */
public abstract class AbstractDialogDescriptor extends DialogDescriptor
        implements ValidStateManager.ValidStateListener {
    
    protected JButton btnOk;
    protected JButton btnCancel;
    
    private ValidStateManager.Provider myVsmProvider;
    
    public AbstractDialogDescriptor(Object innerPane, String title) {
        super(innerPane, title, true, JOptionPane.DEFAULT_OPTION,
                null, DEFAULT_ALIGN, null, null);
        //
        assert innerPane != null;
        assert innerPane instanceof Container;
        assert innerPane instanceof FormLifeCycle;
        //
        btnOk = new JButton(NbBundle.getMessage(CommonUiBundle.class, "BTN_Ok")); // NOI18N
        btnOk.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommonUiBundle.class,"ACSN_BTN_Ok")); // NOI18N
        btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommonUiBundle.class,"ACSD_BTN_Ok")); // NOI18N
        
        btnCancel = new JButton(NbBundle.getMessage(CommonUiBundle.class, "BTN_Cancel")); // NOI18N
        btnCancel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommonUiBundle.class,"ACSN_BTN_Cancel")); // NOI18N
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommonUiBundle.class,"ACSD_BTN_Cancel")); // NOI18N
        
        setOptions(new Object[] {btnOk, btnCancel});
        //
        setClosingOptions(new Object[] {btnCancel});
        //
        ActionListener buttonListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (btnOk.equals(event.getSource())) {
                    processOkButton();
                }
            }
        };
        setButtonListener(buttonListener);
        //
        ValidStateManager validStateManager = getValidStateManager(true);
        if (validStateManager != null) {
            validStateManager.addValidStateListener(this);
            // update to initial value
            updateOkButton(validStateManager);
        }
    }
    
    public synchronized ValidStateManager getValidStateManager(boolean isFast) {
        if (myVsmProvider == null) {
            final Object innerPane = getMessage();
            if (innerPane instanceof ValidStateManager.Provider) {
                myVsmProvider = (ValidStateManager.Provider)innerPane;
            } else if (innerPane instanceof ValidStateManager) {
                myVsmProvider = new ValidStateManager.Provider() {
                    public ValidStateManager getValidStateManager(boolean isFast) {
                        return (ValidStateManager)innerPane;
                    }
                };
            } else {
                myVsmProvider = new ValidStateManager.Provider.Default();
            }
        }
        return myVsmProvider.getValidStateManager(isFast);
    }
    
    @Override
    public Object getDefaultValue() {
        return btnOk;
    }
    
    /**
     * Indicates if the Ok button has pressed.
     * This method is intended to be called after dialog has closed to
     * check if the Ok button was pressed.
     */
    public boolean isOkHasPressed() {
        return getValue() == btnOk;
    }
    
    public abstract void processOkButton();
    
    public abstract void processWindowClose();
    
    @Override
    public void setMessage(Object innerPane) {
        assert innerPane != null;
        assert innerPane instanceof Container;
        assert innerPane instanceof FormLifeCycle;
        //
        ValidStateManager validStateManager = getValidStateManager(true);
        if (validStateManager != null) {
            validStateManager.removeValidStateListener(this);
        }
        //
        myVsmProvider = null;
        super.setMessage(innerPane);
        //
        validStateManager = getValidStateManager(true);
        if (validStateManager != null) {
            validStateManager.addValidStateListener(this);
        }
    }
    
    public void stateChanged(ValidStateManager source, boolean isValid) {
        updateOkButton(source);
    }
    
    protected void setOptionClosable(Object option, boolean flag) {
        List<Object> cOptions =
                new ArrayList<Object>(Arrays.asList(getClosingOptions()));
        //
        if (flag) {
            if (!cOptions.contains(option)) {
                cOptions.add(option);
            }
        } else {
            if (cOptions.contains(option)) {
                cOptions.remove(option);
            }
        }
        //
        setClosingOptions(cOptions.toArray());
    }
    
    protected void updateOkButton(ValidStateManager validStateManager) {
        if (validStateManager.isValid()) {
            btnOk.setEnabled(true);
            btnOk.setToolTipText(null);
        } else {
            btnOk.setEnabled(false);
            Reason reason = validStateManager.getFistReason(Severity.ERROR);
            if (reason != null) {
                btnOk.setToolTipText(reason.getText());
            }
        }
    }
    
}
