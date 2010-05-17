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
