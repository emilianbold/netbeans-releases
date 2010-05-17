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
import java.util.Iterator;
import java.util.List;
import com.sun.jsfcl.std.reference.CompositeReferenceData;
import com.sun.jsfcl.std.reference.ReferenceDataItem;
import com.sun.jsfcl.std.reference.ReferenceDataManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * I expect to find a value named referenceDataGetter in the property descriptor I get given.
 * @author eric
 *
 * @deprecated
 */
public class ChooseOneReferenceDataPropertyEditor extends AbstractPropertyEditor
    implements ExPropertyEditor {
    /**
     * This String attribute defines the class to be used by the ReferenceDataPropertyEditor the
     * property has defined as its propertyEditor.  This will be used to determine the list of choices
     * the property will present either via a drop down or customer panel on the property sheet.
     */
    public static final String REFERENCE_DATA_NAME = "referenceDataDefiner"; // NOI18N

    protected final static int MAX_CHOICE_COUNT_FOR_TAGS = 8;

    protected CompositeReferenceData compositeReferenceData;
    protected long lastRefreshTime;
    protected ReferenceDataItem valueReferenceDataItem;

    public ChooseOneReferenceDataPropertyEditor() {

    }

    public void attachToNewDesignProperty() {

        super.attachToNewDesignProperty();
        compositeReferenceData = getCompositeReferenceDataImp();
        setValue(getDesignProperty().getValue());
    }

    /**
     * Gets the property value as a string suitable for presentation
     * to a human to edit.
     *
     * @return The property value as a string suitable for presentation
     *       to a human to edit.
     * <p>   Returns "null" is the value can't be expressed as a string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *       be prepared to parse that string back in setAsText().
     */
    public String getAsText() {

        return getStringForItem(valueReferenceDataItem);
    }

    public CompositeReferenceData getCompositeReferenceDataImp() {
        String name = (String)getDesignProperty().getPropertyDescriptor().getValue(
            REFERENCE_DATA_NAME);
        if (name == null) {
            throw new RuntimeException("Property named: " +
                getDesignProperty().getPropertyDescriptor().getDisplayName() + " has no " +
                REFERENCE_DATA_NAME + "specified !!!"); // NOI18N
        }
        CompositeReferenceData result = ReferenceDataManager.getInstance().
            getCompositeReferenceData(name, getProject(), getDesignProperty());
        if (result == null) {
            throw new RuntimeException("Property named: " +
                getDesignProperty().getPropertyDescriptor().getDisplayName() +
                " got null for reference data named: " + name); // NOI18N
        }
        return result;
    }

    /**
     * A PropertyEditor may chose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *      not supported.
     */
    public Component getCustomEditor() {
        return new ChooseOneReferenceDataPanel(this, getDesignProperty());
    }

    public ReferenceDataItem getItemByName(String name) {

        if (name == null) {
            return getItemByValue(null);
        }
        name = name.trim();
        for (Iterator iterator = getItems().iterator(); iterator.hasNext(); ) {
            ReferenceDataItem item = (ReferenceDataItem)iterator.next();
            if (name.equals(item.getName())) {
                return item;
            }
        }
        if (getCompositeReferenceData().canAddRemoveItems()) {
            ReferenceDataItem item = getCompositeReferenceData().getDefiner().newItem(name, name, false, true);
            getCompositeReferenceData().add(item);
            return item;
        }
        return null;
    }

    public ReferenceDataItem getItemByValue(Object value) {

        for (Iterator iterator = getItems().iterator(); iterator.hasNext(); ) {
            ReferenceDataItem item = (ReferenceDataItem)iterator.next();
            if (value == null) {
                if (item.getValue() == null) {
                    return item;
                }
            } else {
	            if (value.equals(item.getValue())) {
	                return item;
	            }
            }
        }
        return null;
    }

    public List getItems() {

        return getCompositeReferenceData().getItemsSorted();
    }

    public CompositeReferenceData getCompositeReferenceData() {

        return compositeReferenceData;
    }

    public String getJavaInitializationString() {

        if (valueReferenceDataItem == null || valueReferenceDataItem.isUnsetMarker()) {
            return null;
        }
        if (valueReferenceDataItem.getJavaInitializationString() != null) {
            return valueReferenceDataItem.getJavaInitializationString();
        }
        if (valueReferenceDataItem.getValue() == null) {
            return null;
        }
        if (valueReferenceDataItem.getValue() instanceof String) {
            return stringToJavaSourceString((String)valueReferenceDataItem.getValue());
        }
        throw new RuntimeException("Badly setup reference data item: " +
            getCompositeReferenceData().getName() + ":" + valueReferenceDataItem.getName());
    }

    protected String getStringForItem(ReferenceDataItem item) {

        if (item == null) {
            return ""; // NOI18N
        }
        String string;
        if (getCompositeReferenceData().isValueAString()) {
            string = (String)item.getValue();
        } else {
            string = item.getName();
        }
        if (string == null) {
            string = ""; // NOI18N
        }
        return string;
    }

    public String[] getTags() {

        if (getCompositeReferenceData().canAddRemoveItems()) {
            return null;
        }
        List items = getItems();
        if (items.size() > MAX_CHOICE_COUNT_FOR_TAGS) {
            return null;
        }

        String[] result = new String[items.size()];
        int i = 0;
        for (Iterator iterator = items.iterator(); iterator.hasNext(); i++) {
            result[i] = ((ReferenceDataItem)iterator.next()).getName();
        }
        return result;
    }

    public ReferenceDataItem getValueReferenceDataItem() {

        return valueReferenceDataItem;
    }

    public boolean isPaintable() {

        // EATTODO: This CHEEZY as all get out, but its the only call I'm pretty sure is
        //   called first, before the others, and not as often as getTags in one pass when
        //  there is need of tags :(  I used to have it on getTags, but that gets called too
        //  many times, plus the fact that getTags turns around and immediately causes
        //  items to be rebuild again.
        // MUST FIND a better way to do this
        if (System.currentTimeMillis() - lastRefreshTime > 10000) {
            refreshItems();
            lastRefreshTime = System.currentTimeMillis();
        }
        return super.isPaintable();
    }

    public void refreshItems() {

        getCompositeReferenceData().invalidateDesignContextRelatedCaches();
    }

    /**
     * Sets the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     *
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws java.lang.IllegalArgumentException {

        valueReferenceDataItem = getItemByName(text);
        if (valueReferenceDataItem == null) {
            super.setValue(null);
            throw new LocalizedMessageRuntimeException("Unknown value: \"" + text + "\"");
        }
        super.setValue(valueReferenceDataItem.getValue());
    }

    public void setValue(Object object) {

        setValueImp(object);
        super.setValue(object);
    }

    public void setValueImp(Object object) {

        valueReferenceDataItem = getItemByValue(object);
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {

        if (getCompositeReferenceData().canAddRemoveItems()) {
            return true;
        }
        return getItems().size() > MAX_CHOICE_COUNT_FOR_TAGS;
    }
    
    
    private PropertyEnv propertyEnv;

    public void attachEnv(PropertyEnv propertyEnv) {
        this.propertyEnv = propertyEnv;
    }
    
    public PropertyEnv getEnv() {
        return this.propertyEnv;
    }

}
