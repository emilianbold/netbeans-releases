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

package org.netbeans.modules.jdbcwizard.builder;

import java.util.ArrayList;

/**
 * * This class represents a parameter in a stored procedure which is a resultset. * It is named
 * ResultSetColumns to avoid confusing it with the jdbc resultset.
 */
public class ResultSetColumns {
    private ArrayList columns = null;

    /**
     * * Holds the name of the ResultSet
     */
    private String name = null;

    /**
     * * constructor
     */
    public ResultSetColumns() {
        this.columns = new ArrayList();
    }

    /**
     * getter for name;
     * 
     * @return name;
     */
    public String getName() {
        return this.name;
    }

    /**
     * setter for name; *
     * 
     * @param name
     */
    public void setName(final String rsName) {
        this.name = rsName;
    }

    /**
     * getter for numColumns; *
     * 
     * @return numColumns;
     */
    public int getNumColumns() {
        return this.columns.size();
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
     * @param columns list of <code>ResultSetColumn</code> * objects;
     */
    public void setColumns(final ArrayList columns) {
        this.columns = columns;
    }

    /**
     * adds a ResultsetColumn object to this list.
     * 
     * @param rsCol <code>ResultSetColumn</code> * object that needs to be added;
     */
    public void add(final ResultSetColumn rsCol) {
        if (rsCol != null) {
            this.columns.add(rsCol);
        }
    }

    /**
     * gets the ResultsetColumn object at the given index.
     * 
     * @param index index of <code>ResultSetColumn</code> * object that needs to be retrieved;
     */
    public ResultSetColumn get(final int index) {
        return (ResultSetColumn) this.columns.get(index);
    }

    /**
     * removes the given ResultSetColumn from the list
     * 
     * @param rsCol <code>ResultSetColumn</code> * object that needs to be removed;
     * @returns true if the Object is in the list & is succesfully removed, false otherwise.
     */
    public boolean remove(final ResultSetColumn rsCol) {
        Object removedRSCol = new Object();
        final int remIndex = this.columns.indexOf(rsCol);
        if (remIndex != -1) {
            removedRSCol = this.columns.remove(remIndex);
        }
        return removedRSCol.equals(rsCol);
    }

    /**
     * removes a ResultSetColumn from the list at the given index
     * 
     * @param index index at which the * object that needs to be removed was set;
     * @returns true if the Object is in the list & is succesfully removed, false otherwise.
     */
    public boolean remove(final int index) {
        Object removedRSCol = null;
        removedRSCol = this.columns.remove(index);
        return removedRSCol != null;
    }

}
