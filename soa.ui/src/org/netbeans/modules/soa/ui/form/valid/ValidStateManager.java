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

import java.util.List;
import org.netbeans.modules.soa.ui.form.valid.Validator.Reason;
import org.netbeans.modules.soa.ui.form.valid.Validator.Severity;

/**
 *
 * @author nk150297
 */
public interface ValidStateManager {

    boolean isValid();

    void processValidationResults(Validator validator);
    
    /**
     * Allow to include/exclude the validator to ignore list. 
     * The validators in the ignore list are not taken into consideration.
     */
    void ignoreValidator(Validator validator, boolean flag);
    
    boolean isIgnorValidator(Validator validator);
    
    void clearReasons();

    /**
     * If severity isn't specified (null value) then the reason with 
     * any severity is returned.
     * @param severity
     * @return
     */
    Reason getFistReason(Severity severity);
    
    /**
     * If severity isn't specified (null value) then the reasons with 
     * any severity are returned.
     * @param severity
     * @return
     */
    List<Reason> getReasons(Severity severity);

    public String getHtmlReasons();

    void addValidStateListener(ValidStateListener listener);
    void removeValidStateListener(ValidStateListener listener);
    
    interface ValidStateListener {
        void stateChanged(ValidStateManager source, boolean isValid);
    }
    
    interface Provider {
        /**
         * It's implied that the Provider can provide 2 valid state managers:
         * - One is intended to track fast validation state.
         * - Another is intended to track detailed validation state.
         * The parameter isFast specifies what kind of VSM is required. 
         * A provider can ignore the parameter if it doesn't necessary to have 
         * more then one VSM.
         */
        ValidStateManager getValidStateManager(boolean isFast);
        
        public static class Default implements Provider {
            private ValidStateManager myFastProvider = new DefaultValidStateManager();
            private ValidStateManager myDetailedProvider = new DefaultValidStateManager();
            
            public ValidStateManager getValidStateManager(boolean isFast) {
                if (isFast) {
                    return myFastProvider;
                } else {
                    return myDetailedProvider;
                }
            }
        }
    }
}
