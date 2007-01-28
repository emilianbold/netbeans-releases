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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import com.sun.jsfcl.std.reference.ReferenceDataItem;

/**
 * @author eric
 *
 * @deprecated
 */
public class ChooseManyOfManyReferenceDataPropertyEditor extends
    ChooseManyReferenceDataPropertyEditor {
    protected List valueListOfManyOfManyReferenceDataItems;

    /**
     *
     */
    public ChooseManyOfManyReferenceDataPropertyEditor() {
        super();
    }

    public String getAsText() {

        return getStringForManyOfManyItems(valueListOfManyOfManyReferenceDataItems);
    }

    public Component getCustomEditor() {

        return new ChooseManyOfManyReferenceDataPanel(this, getDesignProperty());
    }

    protected List getManyOfManyItemsForString(String string) {
        ArrayList result, itemList;
        StringTokenizer tokenizer;
        String token;
        ReferenceDataItem item;

        result = new ArrayList(16);
        if (string == null) {
            return result;
        }
        tokenizer = new StringTokenizer(string, " \t\n\r\f,", true);
        itemList = new ArrayList(16);
        result.add(itemList);
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            token = token.trim();
            if (token.length() == 0 || Character.isWhitespace(token.charAt(0))) {
                // whitespace token
            } else if (token.equals(",")) {
                itemList = new ArrayList(16);
                result.add(itemList);
            } else {
                item = getItemByName(token);
                itemList.add(item);
            }
        }
        return result;
    }

    protected String getStringForManyOfManyItems(List manyOfManyList) {
        StringBuffer buffer;
        Iterator manyOfManyIterator, manyIterator;
        ReferenceDataItem item;
        List manyItems;
        String result;

        if (manyOfManyList == null) {
            Object value = getDesignProperty().getValue();
            if (value != null) {
                return value.toString();
            } else {
                return "";
            }
        }
        buffer = new StringBuffer(256);
        manyOfManyIterator = manyOfManyList.iterator();
        while (manyOfManyIterator.hasNext()) {
            manyItems = (List)manyOfManyIterator.next();
            manyIterator = manyItems.iterator();
            while (manyIterator.hasNext()) {
                item = (ReferenceDataItem)manyIterator.next();
                buffer.append(getStringForItem(item));
                if (manyIterator.hasNext()) {
                    buffer.append(" ");
                }
            }
            if (manyOfManyIterator.hasNext()) {
                buffer.append(", ");
            }
        }
        result = buffer.toString();
        return result;
    }

    public List getValueListOfManyOfManyReferenceDataItems() {

        return valueListOfManyOfManyReferenceDataItems;
    }

    public void setValueImp(Object object) {

        valueListOfManyOfManyReferenceDataItems = getManyOfManyItemsForString((String)object);
    }

}
