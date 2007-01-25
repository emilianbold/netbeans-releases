/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

import java.util.ArrayList;

public class PreparedStmtResultSetDescriptor extends DBObjectImpl {
    private int numColumns = 0;

    private ArrayList columns = null;

    /**
     * * constructor
     */
    public PreparedStmtResultSetDescriptor() {
        this.columns = new ArrayList();
    }

    /**
     * getter for numColumns; *
     * 
     * @return numColumns;
     */
    public int getNumColumns() {
        return this.numColumns;
    }

    /**
     * setter for numColumns; *
     * 
     * @param numColumns number of columns;
     */
    public void setNumColumns(final int numColumns) {
        this.numColumns = numColumns;
    }

    /**
     * getter for columns;
     * 
     * @return columns;
     */
    public ArrayList getColumns() {
        return this.columns;
    }

    /**
     * setter for columns;
     * 
     * @param columns list of <code>ColumnDescriptor</code> * objects;
     */
    public void setColumns(final ArrayList columns) {
        this.columns = columns;
    }
}
