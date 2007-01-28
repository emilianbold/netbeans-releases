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
package com.sun.jsfcl.std.property;

import com.sun.jsfcl.std.reference.ReferenceDataManager;
import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.StringTokenizer;
import com.sun.jsfcl.std.reference.ReferenceDataItem;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author eric 
 *         Winston Prakash (modification to include help ID)
 *
 * @deprecated
 */
public class ChooseManyReferenceDataPropertyEditor extends ChooseOneReferenceDataPropertyEditor implements ExPropertyEditor{

    protected ReferenceDataItem[] valueReferenceDataItems;
    protected String separator;

    public ChooseManyReferenceDataPropertyEditor() {

        separator = getDefaultSeparator();
    }

    /**
     * Make this into a parameter read from the properties environment.
     * @return
     */
    protected boolean getAllowDuplicates() {

        return false;
    }

    public String getAsText() {

        return getStringForManyItems(valueReferenceDataItems);
    }

    public Component getCustomEditor() {

        return new ChooseManyReferenceDataPanel(this, getDesignProperty());
    }

    protected String getDefaultSeparator() {

        return " "; //NOI18N
    }

    protected ReferenceDataItem[] getManyItemsForString(String string) {
        ArrayList list;
        StringTokenizer tokenizer;
        String token;
        ReferenceDataItem item;

        if (string == null) {
            return new ReferenceDataItem[0];
        }
        tokenizer = new StringTokenizer(string);
        list = new ArrayList(16);
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            token = token.trim();
            item = getItemByName(token);
            if (item != null) {
                list.add(item);
            }
        }
        ReferenceDataItem result[];

        result = new ReferenceDataItem[list.size()];
        result = (ReferenceDataItem[])list.toArray(result);
        return result;
    }

    protected String getStringForManyItems(Object[] items) {
        StringBuffer buffer;

        if (items == null) {
            return ""; //NOI18N
        }
        buffer = new StringBuffer(256);
        for (int i = 0; i < items.length; i++) {
            ReferenceDataItem item = (ReferenceDataItem)items[i];
            String string = getStringForItem(item);
            buffer.append(string);
            if (i != (items.length - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }

    public String[] getTags() {

        return null;
    }

    public void setAsText(String text) throws java.lang.IllegalArgumentException {

        setValue(text);
    }

    public ReferenceDataItem[] getValueReferenceDataItems() {

        return valueReferenceDataItems;
    }

    public void setValueImp(Object object) {

        valueReferenceDataItems = getManyItemsForString((String)object);
    }

    public boolean supportsCustomEditor() {

        return true;
    }

    public void attachEnv(PropertyEnv env){
        // Add the help button 
        String name = (String)getDesignProperty().getPropertyDescriptor().getValue(REFERENCE_DATA_NAME);
        if (name.equals(ReferenceDataManager.STYLE_CLASSES)){
            FeatureDescriptor desc = env.getFeatureDescriptor();
            desc.setValue(ExPropertyEditor.PROPERTY_HELP_ID, "projrave_ui_elements_propeditors_styleclass_prop_ed");
        }
    }
    
}
