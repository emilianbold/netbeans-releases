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
package org.netbeans.modules.soa.ui.form;

import java.beans.PropertyEditor;
import javax.swing.JPanel;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.form.valid.Validator.Severity;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * This class is intended to be used as a base class for different
 * property customizer dialogs, which want to provide a validation facility.
 *
 * @author nk160297
 */
public abstract class ValidablePropertyCustomizer extends JPanel
        implements Validator.Provider, ValidStateManager.Provider,
        ReusablePropertyCustomizer {
    
    private DefaultValidStateManager validationManager;
    protected Validator myValidator;
    protected PropertyEnv myPropertyEnv;
    
    public ValidablePropertyCustomizer() {
        validationManager = new DefaultValidStateManager();
        validationManager.addValidStateListener(new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (myPropertyEnv != null) {
                    if (isValid) {
                        myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                    } else {
                        myPropertyEnv.setState(PropertyEnv.STATE_INVALID);
                    }
                }
            }
        });
    }
    
    public synchronized void init(
            PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        myPropertyEnv = propertyEnv;
    }
    
    public void revalidate(boolean isFast) {
        ValidStateManager vsm = getValidStateManager(isFast);
        Validator validator = getValidator();
        // vsm.clearReasons();
        validator.clearReasons();
        validator.doValidation(isFast);
        vsm.processValidationResults(validator);
    }
    
    public ValidStateManager getValidStateManager(boolean isFast) {
        return validationManager;
    }
    
    public synchronized Validator getValidator() {
        if (myValidator == null) {
            myValidator = createValidator();
        }
        return myValidator;
    }
    
    public abstract Validator createValidator();
    
}
