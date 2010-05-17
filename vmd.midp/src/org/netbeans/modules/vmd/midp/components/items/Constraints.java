/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
