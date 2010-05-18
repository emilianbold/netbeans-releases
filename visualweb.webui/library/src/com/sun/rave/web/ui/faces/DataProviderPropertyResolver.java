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
package com.sun.rave.web.ui.faces;

import java.util.ArrayList;
import java.util.List;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.model.SelectItem;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.TableRowDataProvider;
import com.sun.rave.web.ui.model.Option;

/**
 * <p><code>DataProviderPropertyResolver</code> is a
 * <code>PropertyResolver</code> implementation that, if the <code>base</code>
 * parameter is a {@link DataProvider}, passes calls to <code>getValue()</code>,
 * <code>getType()</code>, <code>isReadOnly()</code>, and <code>setValue()</code>
 * to the corresponding {@link DataProvider} instance.  Otherwise, it follows
 * the standard JSF decorator pattern and delegates processing to the decorated
 * <code>PropertyResolver</code> instance.</p>
 *
 * <p>These expressions are supported:</p>
 *
 * <p>
 * <code>#{...myDataProvider.value.FIELD_ID}</code><br>
 * <code>#{...myDataProvider.value['FIELD_ID']}</code><br>
 * --> binds to the value of the {@link FieldKey} corresponding to 'FIELD_ID' in
 * a DataProvider *or* the cursor row of a TableDataProvider.  If the specified
 * FIELD_ID does not correspond to a FieldKey in the DataProvider, this property
 * resolver will throw a PropertyNotFoundException, and include any nested
 * exceptions.</p>
 *
 * <p>
 * <code>#{...myDataProvider.value[':ROWKEY:']}</code><br>
 * --> binds to the 'cursorRow' {@link RowKey} of a TableDataProvider or the
 * 'tableRow' RowKey of a TableRowDataProvider.  If the DataProvider is not one
 * of these, this binds to nothing.  Note that cursor or tableRow can be
 * *driven* by this binding.  It is not read-only.</p>
 *
 * <p>
 * <code>#{...myDataProvider.value[':ROWID:']}</code><br>
 * --> binds to the 'cursorRow' {@link RowKey}'s ID (String) of a
 * TableDataProvider or the 'tableRow' RowKey's ID of a TableRowDataProvider.
 * If the DataProvider is not one of these, this binds to nothing.  Note that
 * cursor or tableRow can be *driven* by this binding.  It is not read-only.</p>
 *
 * <p>
 * <code>#{...myDataProvider.selectItems.FIELD_ID}</code><br>
 * <code>#{...myDataProvider.selectItems['FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.selectItems['VALUE_FIELD_ID,LABEL_FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.selectItems['VALUE_FIELD_ID,LABEL_FIELD_ID,DESC_FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.selectItems[':ROWKEY:,:ROWKEY:,:ROWKEY:']}</code><br>
 * <code>#{...myDataProvider.selectItems[':ROWID:,:ROWID:,:ROWID:']}</code><br>
 * --> binds to an array of {@link SelectItem} generated by scanning the rows of
 * the TableDataProvider (without moving the cursor).  If the base object is a
 * DataProvider, but not a TableDataProvider, the resulting SelectItem[] will
 * have one element.  Note that the special :ROWKEY: and :ROWID: field IDs can
 * be used here.</p>
 *
 * <p>
 * <code>#{...myDataProvider.options.FIELD_ID}</code><br>
 * <code>#{...myDataProvider.options['FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.options['VALUE_FIELD_ID,LABEL_FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.options['VALUE_FIELD_ID,LABEL_FIELD_ID,DESC_FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.options[':ROWKEY:,:ROWKEY:,:ROWKEY:']}</code><br>
 * <code>#{...myDataProvider.options[':ROWID:,:ROWID:,:ROWID:']}</code><br>
 * --> binds to an array of {@link Option} generated by scanning the rows of the
 * TableDataProvider (without moving the cursor).  If the base object is a
 * DataProvider, but not a TableDataProvider, the resulting Option[] will have
 * one element.  Note that the special :ROWKEY: and :ROWID: field IDs can be
 * used here.</p>
 *
 * <p>
 * <code>#{...myDataProvider.stringList.FIELD_ID}</code><br>
 * <code>#{...myDataProvider.stringList['FIELD_ID']}</code><br>
 * <code>#{...myDataProvider.stringList[':ROWKEY:']}</code><br>
 * <code>#{...myDataProvider.stringList[':ROWID:']}</code><br>
 * --> binds to an array of String generated by scanning the rows of the
 * TableDataProvider (without moving the cursor) and calling toString() on each
 * value.  If the base object is a DataProvider, but not a TableDataProvider,
 * the resulting String[] will have one element.  Note that the special :ROWKEY:
 * and :ROWID: field IDs can be used here.</p>
 *
 * @author Joe Nuxoll
 */
public class DataProviderPropertyResolver extends PropertyResolver {

    public static final String VALUE_KEY        = "value";       // NOI18N
    public static final String SELECT_ITEMS_KEY = "selectItems"; // NOI18N
    public static final String OPTIONS_KEY      = "options";     // NOI18N
    public static final String STRING_LIST_KEY  = "stringList";  // NOI18N
    public static final String ROWID_FKEY       = ":ROWID:";     // NOI18N
    public static final String ROWKEY_FKEY      = ":ROWKEY:";    // NOI18N

    /**
     * storage for nested PropertyResolver (decorator pattern)
     */
    protected PropertyResolver nested;

    /**
     * Constructs a DataProviderPropertyResolver using the specified
     * PropertyResolver as the pass-thru PropertyResolver (decorator pattern)
     *
     * @param nested PropertyResolver
     */
    public DataProviderPropertyResolver(PropertyResolver nested) {
        this.nested = nested;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {

        if (base instanceof DataProvider) {
            DataProvider provider = (DataProvider)base;

            if (VALUE_KEY.equals(property)) {
                return new ValueData(provider);

            } else if (SELECT_ITEMS_KEY.equals(property)) {
                return new SelectItemsData(provider);

            } else if (OPTIONS_KEY.equals(property)) {
                return new OptionsData(provider);

            } else if (STRING_LIST_KEY.equals(property)) {
                return new StringListData(provider);
            }

        } else if (base instanceof ValueData) {
            return ((ValueData)base).getValue(property.toString());

        } else if (base instanceof SelectItemsData) {
            return ((SelectItemsData)base).getSelectItems(property.toString());
        }

        if (nested != null) {
            return nested.getValue(base, property);
        }

        throw new PropertyNotFoundException("Property [" + property +
            "] not found in object [" + base + "]");
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(Object base, int row)
        throws EvaluationException, PropertyNotFoundException {

        return nested.getValue(base, row);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object base, Object property, Object value)
        throws EvaluationException, PropertyNotFoundException {

        if (base instanceof ValueData) {
            ((ValueData)base).setValue("" + property, value);

        } else if (nested != null) {
            nested.setValue(base, property, value);

        } else {
            throw new PropertyNotFoundException("Property [" + property +
                "] not found in object [" + base + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object base, int row, Object value)
        throws EvaluationException, PropertyNotFoundException {

        nested.setValue(base, row, value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {

        if (base instanceof ValueData) {
            return ((ValueData)base).isReadOnly("" + property);
        }

        if (base instanceof SelectItemsData) {
            return true;
        }

        if (base instanceof DataProvider) {
            if (VALUE_KEY.equals(property)) {
                return true;

            } else if (SELECT_ITEMS_KEY.equals(property) ||
                OPTIONS_KEY.equals(property) ||
                STRING_LIST_KEY.equals(property)) {
                return true;
            }

        }

        if (nested != null) {
            return nested.isReadOnly(base, property);
        }

        throw new PropertyNotFoundException("Property [" + property +
            "] not found in object [" + base + "]");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly(Object base, int row)
        throws EvaluationException, PropertyNotFoundException {

        return nested.isReadOnly(base, row);
    }

    /**
     * {@inheritDoc}
     */
    public Class getType(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {

        if (base instanceof DataProvider) {
            if (VALUE_KEY.equals(property)) {
                return ValueData.class;

            } else if (SELECT_ITEMS_KEY.equals(property)) {
                return SelectItemsData.class;

            } else if (OPTIONS_KEY.equals(property)) {
                return OptionsData.class;

            } else if (STRING_LIST_KEY.equals(property)) {
                return StringListData.class;
            }

        } else if (base instanceof ValueData) {
            return ((ValueData)base).getType("" + property);

        } else if (base instanceof SelectItemsData) {
            return ArrayList.class;
        }

        if (nested != null) {
            return nested.getType(base, property);
        }

        throw new PropertyNotFoundException("Property [" + property +
            "] not found in object [" + base + "]");
    }

    /**
     * {@inheritDoc}
     */
    public Class getType(Object base, int row) throws EvaluationException,
        PropertyNotFoundException {
        return nested.getType(base, row);
    }

    // --------------------------------------------------------------- ValueData

    /**
     *
     */
    private class ValueData {

        /**
         *
         */
        protected DataProvider provider;

        /**
         *
         */
        public ValueData(DataProvider provider) {
            this.provider = provider;
        }

        /**
         *
         */
        public Object getValue(String fieldId) throws PropertyNotFoundException {

            if (fieldId == null) {
                return null;
            }

            Object value = null;

            if (ROWKEY_FKEY.equals(fieldId)) {
                if (provider instanceof TableDataProvider) {
                    return ((TableDataProvider)provider).getCursorRow();
                }
                if (provider instanceof TableRowDataProvider) {
                    return ((TableRowDataProvider)provider).getTableRow();
                }

            } else if (ROWID_FKEY.equals(fieldId)) {
                if (provider instanceof TableDataProvider) {
                    return ((TableDataProvider)provider).getCursorRow().getRowId();
                }
                if (provider instanceof TableRowDataProvider) {
                    return ((TableRowDataProvider)provider).getTableRow().getRowId();
                }

            } else {
                try {
                    FieldKey fk = provider.getFieldKey(fieldId);
                    if (fk != null) {
                        // <RAVE> - 6334873 - No exception on empty TDP
                        // value = provider.getValue(fk);
                        try {
                            value = provider.getValue(fk);
                        } catch (IndexOutOfBoundsException e) {
                            value = null;
                        }
                        // </RAVE>
                    } else {
                        throw new PropertyNotFoundException("Field '" + fieldId + "' not found in DataProvider.");
                    }
                } catch (Exception x) {
                    throw new PropertyNotFoundException(x);
                }
            }

            return value;
        }

        /**
         *
         */
        public void setValue(String fieldId, Object value) throws PropertyNotFoundException {

            if (fieldId == null) {
                return;
            }

            if (ROWKEY_FKEY.equals(fieldId) && value instanceof RowKey) {
                if (provider instanceof TableDataProvider) {
                    try {
                        ((TableDataProvider)provider).setCursorRow((RowKey)value);
                        return;
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
                if (provider instanceof TableRowDataProvider) {
                    try {
                        ((TableRowDataProvider)provider).setTableRow((RowKey)value);
                        return;
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }

            } else if (ROWID_FKEY.equals(fieldId) && value instanceof String) {
                if (provider instanceof TableDataProvider) {
                    try {
                        RowKey row = ((TableDataProvider)provider).
                            getRowKey((String)value);
                        ((TableDataProvider)provider).setCursorRow(row);
                        return;
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
                if (provider instanceof TableRowDataProvider) {
                    try {
                        RowKey row = ((TableRowDataProvider)provider).
                            getTableDataProvider().getRowKey((String)value);
                        ((TableRowDataProvider)provider).setTableRow(row);
                        return;
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }

            } else {
                try {
                    FieldKey fk = provider.getFieldKey(fieldId);
                    if (fk != null) {
                        // <RAVE> - 6334873 - No exception on empty TDP
                        // provider.setValue(fk, value);
                        try {
                            provider.setValue(fk, value);
                        } catch (IndexOutOfBoundsException e) {
                            ; // Swallow and ignore
                        }
                        // </RAVE>
                    } else {
                        throw new PropertyNotFoundException("Field '" + fieldId + "' not found in DataProvider.");
                    }
                } catch (Exception x) {
                    throw new PropertyNotFoundException(x);
                }
            }
        }

        /**
         *
         */
        public boolean isReadOnly(String fieldId) throws PropertyNotFoundException {

            if (ROWKEY_FKEY.equals(fieldId) ||
                ROWID_FKEY.equals(fieldId)) {
                return false;
            }

            try {
                FieldKey fk = provider.getFieldKey(fieldId);
                if (fk != null) {
                    return provider.isReadOnly(fk);
                } else {
                    throw new PropertyNotFoundException("Field '" + fieldId + "' not found in DataProvider.");
                }
            } catch (Exception x) {
                throw new PropertyNotFoundException(x);
            }
        }

        /**
         *
         */
        public Class getType(String fieldId) throws PropertyNotFoundException {

            if (ROWKEY_FKEY.equals(fieldId)) {
                return RowKey.class;

            } else if (ROWID_FKEY.equals(fieldId)) {
                return String.class;
            }

            try {
                FieldKey fk = provider.getFieldKey(fieldId);
                if (fk != null) {
                    return provider.getType(fk);
                } else {
                    throw new PropertyNotFoundException("Field '" + fieldId + "' not found in DataProvider.");
                }
            } catch (Exception x) {
                throw new PropertyNotFoundException(x);
            }
        }
    }

    // --------------------------------------------------------- SelectItemsData

    /**
     *
     */
    private class SelectItemsData {

        /**
         *
         */
        protected DataProvider provider;

        /**
         *
         */
        public SelectItemsData(DataProvider provider) {
            this.provider = provider;
        }

        /**
         *
         */
        protected Object getValue(DataProvider provider, String fieldId, RowKey row) {

            if (fieldId == null) {
                return null;
            }

            Object value = null;

            if (ROWKEY_FKEY.equals(fieldId)) {
                value = row != null
                    ? row
                    : (provider instanceof TableRowDataProvider
                        ? ((TableRowDataProvider)provider).getTableRow()
                        : null);

            } else if (ROWID_FKEY.equals(fieldId)) {
                value = row != null
                    ? row.getRowId()
                    : (provider instanceof TableRowDataProvider
                        ? ((TableRowDataProvider)provider).getTableRow().getRowId()
                        : null);

            } else {
                try {
                    FieldKey fk = provider.getFieldKey(fieldId);
                    if (fk != null) {
                        if (row != null &&
                            provider instanceof TableDataProvider) {
                            value = ((TableDataProvider)provider).getValue(fk, row);
                        }
                        else {
                            value = provider.getValue(fk);
                        }
                    }
                } catch (Exception x) {
                    // throw the puppy to help out the developer
                    // diagnose *his* application problem.
                    if ( x instanceof RuntimeException) {
                        throw (RuntimeException)x ;
                    } else {
                        // should never be here....
                        x.printStackTrace() ;
                    }
                }
            }

            return value;
        }

        /**
         *
         */
        protected Object getSelectItem(Object itemValue, Object itemLabel, Object itemDescr) {

            if (itemValue != null && itemLabel != null && itemDescr != null) {
                return new SelectItem(itemValue, itemLabel.toString(), itemDescr.toString());
            }

            else if (itemValue != null && itemLabel != null) {
                return new SelectItem(itemValue, itemLabel.toString());
            }

            else if (itemValue != null) {
                return new SelectItem(itemValue);
            }

            return null;
        }

        /**
         *
         */
        public Object getSelectItems(String columns) {
            /*
             * returns a List of Objects or SelectItems
             *
             * (examples based on PERSON database table keys):
             *
             *  "NAME" -->
             *  returns a List filled with SelectItem objects,
             *  with the 'itemValue' set to NAME's values
             *
             *  "PERSONID,NAME" -->
             *  returns a List filled with SelectItem objects,
             *  with the 'itemValue' set to PERSONID's values,
             *  and the 'itemLabel' set to NAME's values
             *
             *  "PERSONID,NAME,JOBTITLE" -->
             *  returns a List filled with SelectItem objects,
             *  with the 'itemValue' set to PERSONID's values,
             *  the 'itemLabel' set to NAME's values,
             *  and the 'itemDescription' set to JOBTITLE's values
             *
             * Any cases that are out-of-scope throw IllegalArgumentException
             */
            String valueId = null;
            String labelId = null;
            String descrId = null;

            List cols = new ArrayList();
            String col;
            boolean quoteOpen = false;
            int currStart = 0;
            for (int i = 0; i < columns.length(); i++) {
                char c = columns.charAt(i);
                if (c == '\'') {
                    quoteOpen = !quoteOpen;
                }
                else if (c == ',' && !quoteOpen) {
                    col = columns.substring(currStart, i);
                    if (col.length() > 0) {
                        cols.add(col);
                    }
                    currStart = i + 1;
                }
            }

            //get the remaining stuff after the last period
            if (currStart < columns.length()) {
                col = columns.substring(currStart);
                cols.add(col);
            }

            String[] args = (String[])cols.toArray(new String[cols.size()]);
            if (args.length < 1) {
                throw new IllegalArgumentException();
            }
            valueId = args[0];
            if (args.length > 1) {
                labelId = args[1];
            }
            if (args.length > 2) {
                descrId = args[2];
            }

            ArrayList list = new ArrayList();

            if (provider instanceof TableDataProvider) {

                TableDataProvider tableProvider = (TableDataProvider)provider;
                int rowCount = tableProvider.getRowCount();
                if (rowCount < 0) {
                    rowCount = 999;
                }

                RowKey[] rows = tableProvider.getRowKeys(rowCount, null);

                for (int i = 0; i < rows.length; i++) {

                    Object itemValue = getValue(provider, valueId, rows[i]);
                    Object itemLabel = getValue(provider, labelId, rows[i]);
                    Object itemDescr = getValue(provider, descrId, rows[i]);

                    Object selectItem = getSelectItem(itemValue, itemLabel, itemDescr);
                    if (selectItem != null) {
                        list.add(selectItem);
                    }

                }

            } else {

                Object itemValue = getValue(provider, valueId, null);
                Object itemLabel = getValue(provider, labelId, null);
                Object itemDescr = getValue(provider, descrId, null);

                Object selectItem = getSelectItem(itemValue, itemLabel, itemDescr);
                if (selectItem != null) {
                    list.add(selectItem);
                }

            }
            return list;
        }
    }

    // ------------------------------------------------------------- OptionsData

    /**
     *
     */
    private class OptionsData extends SelectItemsData {

        /**
         *
         */
        public OptionsData(DataProvider provider) {
            super(provider);
        }

        /**
         *
         */
        protected Object getSelectItem(Object itemValue, Object itemLabel, Object itemDescr) {

            if (itemValue != null && itemLabel != null && itemDescr != null) {
                return new Option(itemValue, itemLabel.toString(), itemDescr.toString());
            }

            else if (itemValue != null && itemLabel != null) {
                return new Option(itemValue, itemLabel.toString());
            }

            else if (itemValue != null) {
                return new Option(itemValue);
            }

            return null;
        }
    }

    /**
     *
     */
    private class StringListData extends SelectItemsData {

        /**
         *
         */
        public StringListData(DataProvider provider) {
            super(provider);
        }

        /**
         *
         */
        protected Object getSelectItem(Object itemValue, Object itemLabel, Object itemDescr) {
            if (itemValue != null) {
                return new String(itemValue.toString());
            }
            return "";
        }
    }
}
