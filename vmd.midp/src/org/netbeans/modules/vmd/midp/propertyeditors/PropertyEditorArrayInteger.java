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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.vmd.api.model.PropertyValue;

import org.netbeans.modules.vmd.api.properties.GroupPropertyEditor;
import org.netbeans.modules.vmd.api.properties.GroupValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyEditorArrayInteger extends GroupPropertyEditor implements ExPropertyEditor{
    
    private static String ERROR_WARNING = NbBundle.getMessage(PropertyEditorArrayInteger.class, "LBL_ARRAY_INTEGER_DIALOG"); // NOI18N
    
    public static DesignPropertyEditor create() {
        return new PropertyEditorArrayInteger();
    }
    
    @Override
    public boolean supportsCustomEditor() {
        return false;
    }
   
    @Override
    public String getAsText() {
        StringBuffer text = new StringBuffer();
        text.append('['); // NOI18N
        GroupValue values = getValue();
        for (Iterator<String> i = Arrays.asList(getValue().getPropertyNames()).iterator() ; i.hasNext() ; ) {
            PropertyValue value = (PropertyValue) values.getValue(i.next());
            text.append(value.getPrimitiveValue ());
            if (i.hasNext()) {
                text.append(','); //NOI18N
            }
        }
        text.append(']'); //NOI18N
        return text.toString();
    }
    
    @Override
    public void setAsText(String text) {
        String newText = decodeValuesFromText(text);
        
        if (newText == null)
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERROR_WARNING + ' ' + text)); //NOI18N
        else {
            GroupValue values = getValue();
            Iterator<String> propertyNamesIter = Arrays.asList(values.getPropertyNames()).iterator();
            for (String number : newText.split(",")) { //NOI18N
                values.putValue(propertyNamesIter.next(), MidpTypes.createIntegerValue(Integer.parseInt(number)));
            }
            setValue(values);
        }
    }
    
    private String decodeValuesFromText(String text) {
        text = text.trim().replaceAll(Pattern.compile("[\\[$\\]]").pattern(), ""); //NOI18N
        if (Pattern.compile("[^0123456789,]").matcher(text).find() //NOI18N
            || text.split(",").length != getValue().getPropertyNames().length) { //NOI18N
            return null;
        }
        
        return text;
    }
    
    @Override
   public Boolean canEditAsText() {
        return true;
   }
    
}
