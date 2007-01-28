/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.faces.data;

public interface RowSetColumnBindable extends RowSetBindable {
    public static final String PROPNAME_BOUND_COLUMN = "boundColumn"; //NOI18N

    public void setBoundColumn(ColumnBinding column);

    public ColumnBinding getBoundColumn();
}
