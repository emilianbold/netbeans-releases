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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public abstract class DefaultValidator implements Validator {

    private ValidStateManager.Provider myVSMProvider;
    private List<String> myReasons;
    private Class myBundleLocator;
    
    public DefaultValidator(Component comp, Class bundleLocator) {
        myVSMProvider = new DefaultValidStateManager.DefaultVsmProvider(comp);
        myBundleLocator = bundleLocator;
    }
    
    public DefaultValidator(ValidStateManager.Provider vsmProvider, Class bundleLocator) {
        myVSMProvider = vsmProvider;
        myBundleLocator = bundleLocator;
    }

    public void revalidate(boolean fast) {
        ValidStateManager vsm = myVSMProvider.getValidStateManager(fast);
        if (vsm != null) {
            clearReasons();
            boolean isValid = fast ? doFastValidation() : doDetailedValidation();
            vsm.setValid(this, isValid, myReasons);
        }
    }
    
    public void clearReasons() {
        if (myReasons != null) {
            myReasons.clear();
        }
    }
    
    public String getReason() {
        if (myReasons != null && myReasons.size() > 0) {
            return myReasons.get(0);
        }
        return null;
    }
    
    public List<String> getReasons() {
        return myReasons;
    }
    
    public void setReasons(List<String> newReasons) {
        myReasons = newReasons;
    }
    
    public void addReason(String newReason) {
        if (myReasons == null) {
            myReasons = new ArrayList<String>();
        }
        //
        myReasons.add(newReason);
    }
    
    public void addReasons(List<String> reasons) {
        myReasons.addAll(reasons);
    }
    
    public void setReason(String newReason) {
        myReasons = new ArrayList<String>();
        myReasons.add(newReason);
    }
    
    /**
     * It's a helpful method to understand if the validator has the valid status or not. 
     */
    public boolean isReasonsListEmpty() {
        return myReasons == null || myReasons.isEmpty();
    }
    
    /**
     * Takes the text resource by the specified key and set it as an error reason.
     */
    public void setReasonKey(String key) {
        String reason = NbBundle.getMessage(myBundleLocator, key);
        setReason(reason);
    }
    
    /**
     * Takes the text resource by the specified key and add it as an error reason.
     */
    public void addReasonKey(String key) {
        String reason = NbBundle.getMessage(myBundleLocator, key);
        addReason(reason);
    }
    
    /**
     * Takes the text resource by the specified key and add it as an error reason.
     */
    public void addReasonKey(String key, String... params) {
        String reason = NbBundle.getMessage(myBundleLocator, key, params);
        addReason(reason);
    }
    
    /**
     * Takes the text resources by the specified keys and set them as error reasons.
     */
    public void setReasonKeys(String... keys) {
        for (String key : keys) {
            String reason = NbBundle.getMessage(myBundleLocator, key);
            addReason(reason);
        }
    }
    
    /**
     * The default implementation which calls the fast validation.
     */
    public boolean doDetailedValidation() {
        return doFastValidation();
    }
}
