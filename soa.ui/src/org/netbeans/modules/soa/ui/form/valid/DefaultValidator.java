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
