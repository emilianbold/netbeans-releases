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
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.soa.ui.SoaConstants;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;

/**
 *
 * @author nk160297
 */
public class DefaultValidStateManager implements ValidStateManager {
    
    private static String NBSP = "&nbsp;"; // NOI18N
    private static String HTML = "<html>"; // NOI18N
    
    private HashMap<Validator, List<String>> myProblems;
    private List<ValidStateListener> myListeners;
    private List<Validator> validatorsToIgnor;
    
    public DefaultValidStateManager() {
        myProblems = new HashMap<Validator, List<String>>();
        myListeners = new ArrayList<ValidStateListener>();
        validatorsToIgnor = new ArrayList<Validator>();
    }
    
    public void clearReasons() {
        boolean wasInvalid = !isValid();
        myProblems.clear();
        if (wasInvalid) {
            fireStateChanged();
        }
    }
    
    public void setValid(Validator validator, boolean flag, List<String> reasons) {
        if (isIgnorValidator(validator)) {
            return;
        }
        if (flag) {
            if (myProblems.containsKey(validator)) {
                myProblems.remove(validator);
                fireStateChanged();
            }
        } else {
            boolean needUpdateReasons = false;
            //
            if (myProblems.containsKey(validator)) {
                List<String> currReasons = myProblems.get(validator);
                if (!equals(currReasons, reasons)) {
                    needUpdateReasons = true;
                }
            } else {
                needUpdateReasons = true;
            }
            //
            if (needUpdateReasons) {
                // It's necessary to copy reasons' list here!!!
                myProblems.put(validator, new ArrayList<String>(reasons));
                fireStateChanged();
            }
        }
    }
    
    public void setValid(Validator validator, boolean flag, String reason) {
        setValid(validator, flag, Collections.singletonList(reason));
    }
    
    public void setValid(Validator validator, boolean flag) {
        if (isIgnorValidator(validator)) {
            return;
        }
        List<String> reasons = validator.getReasons();
        if (reasons == null || reasons.size() == 0) {
            setValid(validator, flag, SoaConstants.NOT_ASSIGNED);
        } else {
            setValid(validator, flag, validator.getReasons());
        }
    }
    
    public void ignoreValidator(Validator validator, boolean flag) {
        if (validator == null) {
            return;
        }
        //
        if (flag) {
            // add validator to ignor
            if (!validatorsToIgnor.contains(validator)) {
                validatorsToIgnor.add(validator);
            }
        } else {
            // remove validator to ignor
            validatorsToIgnor.remove(validator);
        }
    }
    
    public boolean isIgnorValidator(Validator validator) {
        return validatorsToIgnor.contains(validator);
    }
    
    public void removeValidStateListener(ValidStateListener listener) {
        myListeners.remove(listener);
    }
    
    public void addValidStateListener(ValidStateListener listener) {
        myListeners.add(listener);
    }
    
    public boolean isValid() {
        return myProblems.size() == 0;
    }
    
    public String getReason() {
        List<String> reasons = getReasons();
        if (reasons.size() > 0) {
            return reasons.get(0);
        }
        return null;
    }
    
    public List<String> getReasons() {
        ArrayList<String> result = new ArrayList<String>();
        for (List<String> reasonsList : myProblems.values()) {
            if (reasonsList != null && reasonsList.size() > 0) {
                result.addAll(reasonsList);
            }
        }
        return result;
    }
    
    /**
     * Shows only first reason.
     */
    public String getHtmlReasons() {
        StringBuffer sb = new StringBuffer();
        //
        // The following line is necessary to provide text wrapping in JLable controls
        sb.append(HTML);
        List<String> reasons = getReasons();
        if (!reasons.isEmpty()) {
            String firstReason = reasons.get(0);
            if (firstReason.startsWith(HTML)) {
                // cut out the <html> tag from the reason text if it present
                firstReason = firstReason.substring(HTML.length());
            }
            sb.append(firstReason);
        }
        //
        return sb.toString();
    }
    
//    /**
//     * Provides multiline text with full list of problems
//     */
//    public String getHtmlReasons() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("<html>"); // NOI18N
//        //
//        String prefix = NbBundle.getMessage(
//                DefaultValidStateManager.class, "ERR_COMMON_PREFIX"); // NOI18N
//        sb.append(prefix).append("<p>"); // NOI18N
//        //
//        boolean firstLine = true;
//        for (String reason : getReasons()) {
//            if (firstLine) {
//                firstLine = false;
//            } else {
//                sb.append("<br>"); // NOI18N
//            }
//            //
//            sb.append(NBSP).append("-").append(NBSP).append(reason). // NOI18N
//                    append(" ").append(NBSP); // NOI18N
//        }
//        //
//        return sb.toString();
//    }
    
    private void fireStateChanged() {
        boolean isValid = isValid();
        for (ValidStateListener listener : myListeners) {
            listener.stateChanged(this, isValid);
        }
    }
    
    private boolean equals(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) {
            return true;
        } else if (list1 == null && list2 != null) {
            return false;
        } else if (list1 != null && list2 == null) {
            return false;
        }
        //
        // Both lists is not null
        if (list1.size() != list2.size()) {
            return false;
        } else {
            //
            // Both lists has the same size
            for (int index = 0; index < list1.size(); index ++) {
                String str1 = list1.get(index);
                String str2 = list2.get(index);
                //
                if (str1 != null) {
                    if (!str1.equals(str2)) {
                        return false;
                    }
                } else if (str2 != null) {
                    if (!str2.equals(str1)) {
                        return false;
                    }
                } else {
                    // Both str equals null --> continue
                }
            }
        }
        //
        return true;
    }
    
    public void validateChildrenControls(Container parent, boolean fast) {
        validateChildrenControls(this, parent, fast);
    }
    
    /**
     * This method do recursive validation of the component's tree by
     * looking validator providers.
     */
    public static void validateChildrenControls(
            ValidStateManager vsm, Container parent, boolean fast) {
        for (Component comp : parent.getComponents()) {
            Validator validator = null;
            if (comp instanceof Validator) {
                validator = ((Validator)comp);
            } else if (comp instanceof Validator.Provider) {
                validator = ((Validator.Provider)comp).getValidator();
            }
            if (validator != null && !vsm.isIgnorValidator(validator)) {
                validator.clearReasons();
                boolean isValid = fast ?
                    validator.doFastValidation() :
                    validator.doDetailedValidation();
                if (!isValid) {
                    vsm.setValid(validator, false, validator.getReason());
                }
            }
            if (comp instanceof Container) {
                validateChildrenControls(vsm, (Container)comp, fast);
            }
        }
    }
    
    public static ValidStateManager findVSM(Component comp, boolean isFast) {
        while (comp != null) {
            if (comp instanceof ValidStateManager.Provider) {
                return ((ValidStateManager.Provider)comp).getValidStateManager(isFast);
            } else {
                comp = comp.getParent();
            }
        }
        return null;
    }
    
    public static class DefaultVsmProvider implements ValidStateManager.Provider {
        
        private Component myComponent;
        
        public DefaultVsmProvider(Component comp) {
            myComponent = comp;
        }
        
        public ValidStateManager getValidStateManager(boolean isFast) {
            return DefaultValidStateManager.findVSM(myComponent, isFast);
        }
        
    }
}
