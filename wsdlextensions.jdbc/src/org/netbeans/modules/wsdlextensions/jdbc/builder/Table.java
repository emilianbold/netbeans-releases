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

package org.netbeans.modules.wsdlextensions.jdbc.builder;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * Class to hold procedure metadata.
 * 
 * @author
 */
public class Table {
    private String name = ""; // name of table

    private String javaName = ""; // java name of table

    private String catalog = ""; // catalog

    private String schema = ""; // schema

    private int numColumns = 0; // number of table columns

    private int numColumnsSelected = 0; // number of table columns selected

    private TableColumn[] columns; // array of table columns

    private String type = "TABLE"; // TABLE, SYSTEM TABLE, VIEW - from driver

    private List indexList; // List of IndexColumn objects

    private List fkColumnList; // List of KeyColumn objects (PK cols)

    private List pkColumnList; // List of ForeignKeyColumn objects (FK cols)

    private boolean selected;

    /**
     * Creates an instance of Table with the given attributes.
     * 
     * @param tname Table name
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param ttype Table type
     */
    public Table(final String tname, final String tcatalog, final String tschema, final String ttype) {
        this.name = tname;
        this.catalog = tcatalog;
        this.schema = tschema;
        this.type = ttype;

        this.indexList = Collections.EMPTY_LIST;
        this.fkColumnList = Collections.EMPTY_LIST;
        this.pkColumnList = Collections.EMPTY_LIST;
    }

    /**
     * Creates an instance of Table with the given attributes.
     * 
     * @param tname Table name
     * @param jname Table java name
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param ttype Table type
     */
    public Table(final String tname, final String jname, final String tcatalog, final String tschema, final String ttype) {
        this.name = tname;
        this.javaName = jname;
        this.catalog = tcatalog;
        this.schema = tschema;
        this.type = ttype;

        this.indexList = Collections.EMPTY_LIST;
        this.fkColumnList = Collections.EMPTY_LIST;
        this.pkColumnList = Collections.EMPTY_LIST;
    }

    /**
     * Creates an instance of Table with the given attributes.
     */
    public Table(final Table nTable) {
        this.name = nTable.getName();
        this.javaName = nTable.getJavaName();
        this.catalog = nTable.getCatalog();
        this.schema = nTable.getSchema();
        this.numColumns = nTable.getNumColumns();
        this.numColumnsSelected = nTable.getNumColumnsSelected();
        this.cloneColumns(nTable.getColumns());
        this.type = nTable.getType();
        this.cloneIndexList(nTable.getIndexList());
        this.cloneForeignKeyColumnList(nTable.getForeignKeyColumnList());
        this.clonePrimaryKeyColumnList(nTable.getPrimaryKeyColumnList());
        this.selected = nTable.isSelected();
    }

    /**
     * Get the table name.
     * 
     * @return Table name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the table java name.
     * 
     * @return Table java name
     */
    public String getJavaName() {
        return this.javaName;
    }

    /**
     * Get the catalog name.
     * 
     * @return Catalog name
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * Get the schema name.
     * 
     * @return Schema name
     */
    public String getSchema() {
        return this.schema;
    }

    /**
     * Get the number of table columns.
     * 
     * @return Number of table columns.
     */
    public int getNumColumns() {
        return this.numColumns;
    }

    /**
     * Get the number of columns selected.
     * 
     * @return Number of columns selected.
     */
    public int getNumColumnsSelected() {
        return this.numColumnsSelected;
    }

    /**
     * Get the list of table columns.
     * 
     * @return List of table columns
     */
    public TableColumn[] getColumns() {
        return this.columns;
    }

    /**
     * Get the table type.
     * 
     * @return Table type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the table name.
     * 
     * @param newName Table name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Set the table java name.
     * 
     * @param newName Table java name
     */
    public void setJavaName(final String newJavaName) {
        this.javaName = newJavaName;
    }

    /**
     * Set the catalog name.
     * 
     * @param newCatalog Catalog name
     */
    public void setCatalog(final String newCatalog) {
        this.catalog = newCatalog;
    }

    /**
     * Set the schema name.
     * 
     * @param newSchema Schema name
     */
    public void setSchema(final String newSchema) {
        this.schema = newSchema;
    }

    /**
     * Set the table columns.
     * 
     * @param newColumns Table columns
     */
    public void setColumns(final TableColumn[] newColumns) {
        this.columns = newColumns;

        // update the number of columns and columns selected
        if (this.columns != null) {
            this.numColumns = this.columns.length;

            int count = 0;
            for (int i = 0; i < this.columns.length; i++) {
                if (this.columns[i].getIsSelected()) {
                    count++;
                }
            }
            this.numColumnsSelected = count;
        } else {
            this.numColumns = 0;
            this.numColumnsSelected = 0;
        }
    }

    /**
     * Clone the table columns.
     * 
     * @param newColumns Table columns
     */
    public void cloneColumns(final TableColumn[] newColumns) {
        this.numColumns = 0;
        this.numColumnsSelected = 0;

        int count = 0;
        if (newColumns != null) {
            this.numColumns = newColumns.length;
            if (this.numColumns > 0) {
                this.columns = new TableColumn[this.numColumns];
                for (int i = 0; i < this.numColumns; i++) {
                    this.columns[i] = new TableColumn(newColumns[i]);
                    if (this.columns[i].getIsSelected()) {
                        count++;
                    }
                }
            }
            this.numColumnsSelected = count;
        }
    }

    public void addColumn(final TableColumn col) {
        if (null == col) {
            return;
        }

        int numCols = 0;
        if (null != this.columns) {
            numCols = this.columns.length;
        }
        final TableColumn[] newTable = new TableColumn[numCols + 1];
        for (int i = 0; i < numCols; i++) {
            newTable[i] = this.columns[i];
        }
        newTable[numCols] = col;
        this.setColumns(newTable);
    }

    public void removeColumn(final int index) {
        if (null == this.columns || index > this.columns.length) {
            return;
        }

        final int numCols = this.columns.length;

        final TableColumn[] newTable = new TableColumn[numCols - 1];
        for (int i = 0, j = 0; i < numCols; i++, j++) {
            if (i == index) {
                j--;
            } else {
                newTable[j] = this.columns[i];
            }
        }
        this.setColumns(newTable);
    }

    /**
     * Set the table type.
     * 
     * @param newType Table type
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Get the index list.
     * 
     * @return Index list
     */
    public List getIndexList() {
        return this.indexList;
    }

    /**
     * Set the index list.
     * 
     * @param newList Index list
     */
    public void setIndexList(final List newList) {
        if (newList != null && newList.size() != 0) {
            try {
                // Test to ensure that List contains nothing but Index objects.
                final IndexColumn[] dummy = (IndexColumn[]) newList.toArray(new IndexColumn[newList.size()]);
            } catch (final ArrayStoreException e) {
                throw new IllegalArgumentException("newList does not contain Index objects!");
            }

            this.indexList = newList;
        }
    }

    public void cloneIndexList(final List newList) {
        this.indexList = Collections.EMPTY_LIST;

        if (newList != null && newList.size() != 0) {
            this.indexList = new ArrayList();

            try {
                // Test to ensure that List contains nothing but Index objects.
                final IndexColumn[] dummy = (IndexColumn[]) newList.toArray(new IndexColumn[newList.size()]);
                for (int i = 0; i < newList.size(); i++) {
                    final IndexColumn iCol = (IndexColumn) newList.get(i);
                    this.indexList.add(new IndexColumn(iCol));
                }
            } catch (final ArrayStoreException e) {
                throw new IllegalArgumentException("newList does not contain Index objects!");
            }
        }
    }

    // added by Neena
    // to set the selection state of the table

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    // added by Neena
    // to get the selection state of the object

    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Gets current List of KeyColumn objects, representing primary key columns in this table.
     * 
     * @return List (possibly empty) of KeyColumn instances
     */
    public List getPrimaryKeyColumnList() {
        return this.pkColumnList;
    }

    /**
     * Sets List of primary key column objects to the given List.
     * 
     * @param newList List containing new collection of KeyColumn objects representing primary key
     *            columns within this table
     */
    public void setPrimaryKeyColumnList(final List newList) {
        if (newList != null && newList.size() != 0) {
            try {
                // Test to ensure that List contains nothing but Index objects.
                final KeyColumn[] dummy = (KeyColumn[]) newList.toArray(new KeyColumn[newList.size()]);
            } catch (final ArrayStoreException e) {
                final ResourceBundle cMessages = NbBundle.getBundle(Table.class);
                throw new IllegalArgumentException(cMessages.getString("ERROR_KEY") + "(ERROR_KEY)");// NO
                // i18n
            }

            this.pkColumnList = newList;
        }
    }

    public void clonePrimaryKeyColumnList(final List newList) {
        this.pkColumnList = Collections.EMPTY_LIST;

        if (newList != null && newList.size() != 0) {
            this.pkColumnList = new ArrayList();

            try {
                // Test to ensure that List contains nothing but Index objects.
                final KeyColumn[] dummy = (KeyColumn[]) newList.toArray(new KeyColumn[newList.size()]);
                for (int i = 0; i < newList.size(); i++) {
                    final KeyColumn tPK = (KeyColumn) newList.get(i);
                    this.pkColumnList.add(new KeyColumn(tPK.getName(), tPK.getColumnName(), tPK.getColumnSequence()));
                }
            } catch (final ArrayStoreException e) {
                final ResourceBundle cMessages = NbBundle.getBundle(Table.class);
                throw new IllegalArgumentException(cMessages.getString("ERROR_KEY") + "(ERROR_KEY)");// NO
                // i18n
            }
        }
    }

    /**
     * Gets current List of ForeignKeyColumn objects, representing foreign key columns in this
     * table.
     * 
     * @return List (possibly empty) of ForeignKeyColumn instances
     */
    public List getForeignKeyColumnList() {
        return this.fkColumnList;
    }

    /**
     * Sets List of foreign key column objects to the given List.
     * 
     * @param newList List containing new collection of ForeignKeyColumn objects representing
     *            foreign key columns within this table
     */
    public void setForeignKeyColumnList(final List newList) {
        if (newList != null && newList.size() != 0) {
            try {
                // Test to ensure that List contains nothing but Index objects.
                final ForeignKeyColumn[] dummy = (ForeignKeyColumn[]) newList.toArray(new ForeignKeyColumn[newList.size()]);
            } catch (final ArrayStoreException e) {
                final ResourceBundle cMessages = NbBundle.getBundle(Table.class);
                throw new IllegalArgumentException(cMessages.getString("ERROR_FK_KEY") + "(ERROR_FK_KEY)");// NO
                // i18n
            }

            this.fkColumnList = newList;
        }
    }

    public void cloneForeignKeyColumnList(final List newList) {
        this.fkColumnList = Collections.EMPTY_LIST;

        if (newList != null && newList.size() != 0) {
            this.fkColumnList = new ArrayList();

            try {
                // Test to ensure that List contains nothing but Index objects.
                final ForeignKeyColumn[] dummy = (ForeignKeyColumn[]) newList.toArray(new ForeignKeyColumn[newList.size()]);
                for (int i = 0; i < newList.size(); i++) {
                    final ForeignKeyColumn fkCol = (ForeignKeyColumn) newList.get(i);
                    this.fkColumnList.add(new ForeignKeyColumn(fkCol));
                }
            } catch (final ArrayStoreException e) {
                final ResourceBundle cMessages = NbBundle.getBundle(Table.class);
                throw new IllegalArgumentException(cMessages.getString("ERROR_FK_KEY") + "(ERROR_FK_KEY)");// NO
                // i18n
            }
        }
    }
}
