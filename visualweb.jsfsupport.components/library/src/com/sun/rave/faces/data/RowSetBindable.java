/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.faces.data;

import javax.sql.RowSet;

public interface RowSetBindable {
    public static final String PROPNAME_BOUND_ROWSET = "boundRowSet"; //NOI18N

    public void setBoundRowSet(RowSet boundRowSet);

    public RowSet getBoundRowSet();
}
