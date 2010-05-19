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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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

public class ProcResultSetDescriptor extends DBObjectImpl {
    private int numColumns = 0;

    private ArrayList columns = null;

    /**
     * * constructor
     */
    public ProcResultSetDescriptor() {
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
