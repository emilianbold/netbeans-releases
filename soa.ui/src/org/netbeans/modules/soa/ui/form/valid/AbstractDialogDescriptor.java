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
            btnOk.setToolTipText(validStateManager.getReason());
        }
    }
    
}
