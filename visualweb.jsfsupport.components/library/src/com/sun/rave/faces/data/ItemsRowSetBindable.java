/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.faces.data;

import javax.sql.RowSet;

public interface ItemsRowSetBindable {
    public static final String PROPNAME_ITEMS_ROWSET = "itemsRowSet"; //NOI18N
    public static final String PROPNAME_ITEMS_VALUE_COLUMN = "itemsValueColumn"; //NOI18N
    public static final String PROPNAME_ITEMS_LABEL_COLUMN = "itemsLabelColumn"; //NOI18N

    public void setItemsRowSet(RowSet itemsRowSet);

    public RowSet getItemsRowSet();

    public void setItemsValueColumn(ColumnBinding itemsValueColumn);

    public ColumnBinding getItemsValueColumn();

    public void setItemsLabelColumn(ColumnBinding itemsLabelColumn);

    public ColumnBinding getItemsLabelColumn();
}
