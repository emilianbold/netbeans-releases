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
