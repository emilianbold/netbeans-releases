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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.vmd.midp.components.general.Bitmask;
import org.netbeans.modules.vmd.midp.propertyeditors.Bundle;

/**
 *
 * @author Karol Harezlak
 */
public class Constraints extends Bitmask {

    private List<BitmaskItem> bitmaskItems;

    public Constraints(int bitmask) {
        super (bitmask);
    }

    public List<BitmaskItem> getBitmaskItems() {
        if (bitmaskItems == null) {
            bitmaskItems = new ArrayList<BitmaskItem>();
            bitmaskItems.add(new BitmaskItem (TextFieldCD.VALUE_ANY, Bundle.getMessage("LBL_TEXTFIELDPE_ANY"),"ANY"));  // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_EMAILADDR, Bundle.getMessage("LBL_TEXTFIELDPE_EMAIL"),"EMAILADDR"));  // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_NUMERIC, Bundle.getMessage("LBL_TEXTFIELDPE_NUMERIC"),"NUMERIC")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_PHONENUMBER, Bundle.getMessage("LBL_TEXTFIELDPE_PHONE"),"PHONENUMBER")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_URL, Bundle.getMessage("LBL_TEXTFIELDPE_URL"),"URL")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_DECIMAL, Bundle.getMessage("LBL_TEXTFIELDPE_DECIMAL"),"DECIMAL")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_CONSTRAINT_MASK, Bundle.getMessage("LBL_TEXTFIELDPE_ANY"),"CONSTRAINT_MASK")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_PASSWORD, Bundle.getMessage("LBL_TEXTFIELDPE_PASSWORD"),"PASSWORD")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_UNEDITABLE, Bundle.getMessage("LBL_TEXTFIELDPE_UNDEDITABLE"),"UNEDITABLE")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_SENSITIVE, Bundle.getMessage("LBL_TEXTFIELDPE_SENSITIVE"),"SENSITIVE")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_NON_PREDICTIVE, Bundle.getMessage("LBL_TEXTFIELDPE_NONPREDICTIVE"),"VALUE_NON_PREDICTIVE")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_WORD, Bundle.getMessage("LBL_TEXTFIELDPE_CAPS_WORD"),"INITIAL_CAPS_WORD")); // NOI18N 
            bitmaskItems.add(new BitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_SENTENCE, Bundle.getMessage("LBL_TEXTFIELDPE_CAPS_SENTENCE"),"INITIAL_CAPS_SENTENCE")); // NOI18N 
        }
        
        return bitmaskItems;
    }

    
}
