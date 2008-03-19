/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package com.sun.sql.rowset;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.rowset.RowSetMetaDataImpl;

/**
 * 
 * Implements {@link com.sun.sql.rowset.RowSetMetaDataX}
 * 
 */
public class RowSetMetaDataXImpl extends RowSetMetaDataImpl implements RowSetMetaDataX {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("com.sun.sql.rowset.Bundle", Locale.getDefault()); // NOI18N

    String[]  columnClassName;
    boolean[] definitelyWritable;
    boolean[] readOnly;
    boolean[] writable;


    public void setColumnCount(int columnCount) throws SQLException {
        super.setColumnCount(columnCount);

        /*
         * If the columnCount is Integer.MAX_VALUE,
         * we do not initialize.
         * even if we try to initialize the colCount with 
         * columnCount = Integer.MAx_VALUE-1, we'll run out
         * of memory.  This is to pass TCK.
         */
        if(!(columnCount == Integer.MAX_VALUE)) {
            columnClassName = new String[columnCount + 1];
            definitelyWritable = new boolean[columnCount + 1];
            readOnly = new boolean[columnCount + 1];
            writable = new boolean[columnCount + 1];
        }
    }

    public void setColumnClassName(int columnIndex, String className) throws SQLException {
        checkColumnIndex(columnIndex);
        columnClassName[columnIndex] = className;
    }

    public void setDefinitelyWritable(int columnIndex, boolean value) throws SQLException {
        checkColumnIndex(columnIndex);
        definitelyWritable[columnIndex] = value;
    }

    public void setReadOnly(int columnIndex, boolean value) throws SQLException {
        checkColumnIndex(columnIndex);
        readOnly[columnIndex] = value;
    }

    public void setWritable(int columnIndex, boolean value) throws SQLException {
        checkColumnIndex(columnIndex);
        writable[columnIndex] = value;
    }

    public String getColumnClassName(int columnIndex) throws SQLException {
        checkColumnIndex(columnIndex);
        String className = columnClassName[columnIndex];
        if (className == null) {
            throw new SQLException(rb.getString("SCHEMA_HAS_CHANGED"));
        }
        if (className.equals("byte[]")) {
            className = new byte[0].getClass().getName(); //"[B";
        } else if (className.equals("char[]")) {
            className = new char[0].getClass().getName(); //"[C";
        } else if (className.equals("short[]")) {
            className = new short[0].getClass().getName(); //"[S";
        } else if (className.equals("int[]")) {
            className = new int[0].getClass().getName(); //"[I";
        } else if (className.equals("long[]")) {
            className = new long[0].getClass().getName(); //"[J";
        } else if (className.equals("float[]")) {
            className = new float[0].getClass().getName(); //"[F";
        } else if (className.equals("double[]")) {
            className = new double[0].getClass().getName(); //"[D";
        } else if (className.equals("boolean[]")) {
            className = new boolean[0].getClass().getName(); //"[Z";
        }
        return className;
    }

    public boolean isDefinitelyWritable(int columnIndex) throws SQLException {
        checkColumnIndex(columnIndex);
        return definitelyWritable[columnIndex];
    }

    public boolean isReadOnly(int columnIndex) throws SQLException {
        checkColumnIndex(columnIndex);
        return readOnly[columnIndex];
    }

    public boolean isWritable(int columnIndex) throws SQLException {
        checkColumnIndex(columnIndex);
        return writable[columnIndex];
    }

    private void checkColumnIndex(int columnIndex) throws SQLException {
        if (columnIndex <= 0 || columnIndex > getColumnCount()) {
            throw new SQLException(MessageFormat.format(rb.getString("INVALID_COLUMN_INDEX"), //NOI18N
                new Object[] { new Integer(columnIndex) }));
        }
    }
}



