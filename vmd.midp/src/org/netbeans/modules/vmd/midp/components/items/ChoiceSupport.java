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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Karol Harezlak
 */

public final class ChoiceSupport {

    private static Map<String, PropertyValue> listTypes;
    private static Map<String, PropertyValue> fitPolicyValues;
    private static Map<String, PropertyValue> choiceGroupTypes;
    
    public static final int VALUE_EXCLUSIVE = 1;
    public static final int VALUE_MULTIPLE = 2;
    public static final int VALUE_IMPLICIT = 3;
    public static final int VALUE_POPUP = 4;
    
    public static final int VALUE_TEXT_WRAP_DEFAULT = 0;
    public static final int VALUE_TEXT_WRAP_ON = 1;
    public static final int VALUE_TEXT_WRAP_OFF = 2;
    
    public static Map<String, PropertyValue> getListTypes() {
        if (listTypes == null) {
            listTypes = new TreeMap<String, PropertyValue>();
            listTypes.put("EXCLUSIVE", MidpTypes.createIntegerValue(VALUE_EXCLUSIVE)); // NOI18N
            listTypes.put("IMPLICIT", MidpTypes.createIntegerValue(VALUE_IMPLICIT));   // NOI18N
            listTypes.put("MULTIPLE", MidpTypes.createIntegerValue(VALUE_MULTIPLE));   // NOI18N
        }        
        return listTypes;
    }
    
    public static Map<String, PropertyValue>  getChoiceGroupTypes() {
        if (choiceGroupTypes == null) {
            choiceGroupTypes = new TreeMap<String, PropertyValue>();
            choiceGroupTypes.put("EXCLUSIVE", MidpTypes.createIntegerValue(VALUE_EXCLUSIVE));  // NOI18N
            choiceGroupTypes.put("POPUP", MidpTypes.createIntegerValue(VALUE_POPUP));          // NOI18N
            choiceGroupTypes.put("MULTIPLE", MidpTypes.createIntegerValue(VALUE_MULTIPLE));    // NOI18N
        }        
        return choiceGroupTypes;
    }
    
    public static Map<String, PropertyValue> getFitPolicyValues() {
        if (fitPolicyValues == null) {
            fitPolicyValues = new TreeMap<String, PropertyValue>();
            fitPolicyValues.put("TEXT_WRAP_DEFAULT", MidpTypes.createIntegerValue(VALUE_TEXT_WRAP_DEFAULT)); // NOI18N
            fitPolicyValues.put("TEXT_WRAP_ON", MidpTypes.createIntegerValue(VALUE_TEXT_WRAP_ON));           // NOI18N
            fitPolicyValues.put("TEXT_WRAP_OFF", MidpTypes.createIntegerValue(VALUE_TEXT_WRAP_OFF));         // NOI18N
        }
        return fitPolicyValues;
    }
    
}
