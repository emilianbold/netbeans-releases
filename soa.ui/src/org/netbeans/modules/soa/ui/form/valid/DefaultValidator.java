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
    private List<Reason> myReasons;
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
            doValidation(fast);
            vsm.processValidationResults(this);
        }
    }
    
    public void doValidation(boolean fast) {
        if (fast) {
            doFastValidation();
        } else {
            doDetailedValidation();
        }
    }
    
    public void clearReasons() {
        if (!(myReasons == null || myReasons.isEmpty())) {
            myReasons.clear();
        }
    }
    
    public Reason getReason() {
        if (!(myReasons == null || myReasons.isEmpty())) {
            return myReasons.get(0);
        }
        return null;
    }
    
    public List<Reason> getReasons() {
        return myReasons;
    }
    
    public List<Reason> getReasons(Severity severity) {
        if (myReasons == null) {
            return null;
        }
        //
        ArrayList<Reason> result = new ArrayList<Reason>();
        //
        for (Reason reason : myReasons) {
            if (reason.getSeverity() == severity) {
                result.add(reason);
            }
        }
        //
        return result;
    }
    
    public boolean hasReasons(Severity severity) {
        if (severity == null) {
            return !(myReasons == null || myReasons.isEmpty());
        }
        //
        if (myReasons != null) {
            for (Reason reason : myReasons) {
                if (reason.getSeverity() == severity) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void addReason(Reason newReason) {
        if (myReasons == null) {
            myReasons = new ArrayList<Reason>();
        }
        //
        myReasons.add(newReason);
    }
    
    public void addReasons(List<Reason> reasons) {
        myReasons.addAll(reasons);
    }
    
    public void setReason(Reason newReason) {
        myReasons = new ArrayList<Reason>();
        myReasons.add(newReason);
    }
    
    /**
     * Takes the text resource by the specified key and set it as an error reason.
     */
    public void setReasonKey(Severity severity, String key) {
        String text = NbBundle.getMessage(myBundleLocator, key);
        Reason reason = new Reason(severity, text);
        setReason(reason);
    }
    
    /**
     * Takes the text resource by the specified key and add it as an error reason.
     */
    public void addReasonKey(Severity severity, String key) {
        String text = NbBundle.getMessage(myBundleLocator, key);
        Reason reason = new Reason(severity, text);
        addReason(reason);
    }
    
    /**
     * Takes the text resource by the specified key and add it as an error reason.
     */
    public void addReasonKey(Severity severity, String key, String... params) {
        String text = NbBundle.getMessage(myBundleLocator, key, params);
        Reason reason = new Reason(severity, text);
        addReason(reason);
    }
    
    /**
     * The default implementation which calls the fast validation.
     */
    public void doDetailedValidation() {
        doFastValidation();
    }
}
