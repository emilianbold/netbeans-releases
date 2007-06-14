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

package com.sun.sql.rowset;

import java.sql.SQLException;
import javax.sql.RowSetMetaData;

/**
 * An object that contains information about the columns in a
 * <code>RowSet</code> object.  This interface is
 * an extension of the <code>RowSetMetaData</code> interface with 
 * methods for setting the values that are conspiculously missing on
 * <code>RowSetMetaData</code>.
 */

public interface RowSetMetaDataX extends RowSetMetaData {

    public void setColumnClassName(int columnIndex, String className) throws SQLException;
    public void setDefinitelyWritable(int columnIndex, boolean value) throws SQLException;
    public void setReadOnly(int columnIndex, boolean value) throws SQLException;
    public void setWritable(int columnIndex, boolean value) throws SQLException;
}





