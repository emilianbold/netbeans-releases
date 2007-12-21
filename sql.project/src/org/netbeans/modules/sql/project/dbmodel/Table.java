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
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;


import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

//Internationalization
import java.util.Locale;
import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * Class to hold procedure metadata.
 *
 * @author Susan Chen
 * @author Jonathan Giron
 * @version 
 */
public class Table {
    private String name = "";           // name of table
    private String javaName = "";       // java name of table
    private String catalog = "";        // catalog
    private String schema = "";         // schema
    private int numColumns = 0;         // number of table columns
    private int numColumnsSelected = 0; // number of table columns selected
    private TableColumn[] columns;     // array of table columns
    private String type = "TABLE";     // TABLE, SYSTEM TABLE, VIEW - from driver
    
    private List indexList;             // List of IndexColumn objects
    private List fkColumnList;          // List of KeyColumn objects (PK cols)
    private List pkColumnList;          // List of ForeignKeyColumn objects (FK cols)
    private boolean selected ;


    /**
     * Creates an instance of Table with the given attributes.
     *
     * @param tname Table name
     * @param tcatalog Catalog name
     * @param tschema Schema name
     * @param ttype Table type
     */
    public Table(String tname, String tcatalog, String tschema, String ttype) {
        name = tname;
        catalog = tcatalog;
        schema = tschema;
        type = ttype;
        
        indexList = Collections.EMPTY_LIST;
        fkColumnList = Collections.EMPTY_LIST;
        pkColumnList = Collections.EMPTY_LIST;
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
    public Table(String tname, String jname, String tcatalog, String tschema, String ttype) {
        name = tname;
        javaName = jname;
        catalog = tcatalog;
        schema = tschema;
        type = ttype;
        
        indexList = Collections.EMPTY_LIST;
        fkColumnList = Collections.EMPTY_LIST;
        pkColumnList = Collections.EMPTY_LIST;
    }
    
    /**
     * Creates an instance of Table with the given attributes.
     *
     */
    public Table(Table nTable) {
        name = nTable.getName();
        javaName = nTable.getJavaName();
        catalog = nTable.getCatalog();
        schema = nTable.getSchema();
        numColumns = nTable.getNumColumns();
        numColumnsSelected = nTable.getNumColumnsSelected();
        cloneColumns(nTable.getColumns());
        type = nTable.getType();
        cloneIndexList(nTable.getIndexList());
        cloneForeignKeyColumnList(nTable.getForeignKeyColumnList());
        clonePrimaryKeyColumnList(nTable.getPrimaryKeyColumnList());
        selected = nTable.isSelected();
    }

    /**
     * Get the table name.
     *
     * @return Table name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the table java name.
     *
     * @return Table java name
     */
    public String getJavaName() {
        return javaName;
    }
    
    /**
     * Get the catalog name.
     *
     * @return Catalog name
     */
    public String getCatalog() {
        return catalog;
    }
    
    /**
     * Get the schema name.
     *
     * @return Schema name
     */
    public String getSchema() {
        return schema;
    }
    
    /**
     * Get the number of table columns.
     *
     * @return Number of table columns.
     */
    public int getNumColumns() {
        return numColumns;
    }
    
    /**
     * Get the number of columns selected.
     *
     * @return Number of columns selected.
     */
    public int getNumColumnsSelected() {
        return numColumnsSelected;
    }
    
    /**
     * Get the list of table columns.
     *
     * @return List of table columns
     */
    public TableColumn[] getColumns() {
        return columns;
    }
    
    /**
     * Get the table type.
     *
     * @return Table type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Set the table name.
     *
     * @param newName Table name
     */
    public void setName(String newName) {
        name = newName;
    }
    
    /**
     * Set the table java name.
     *
     * @param newName Table java name
     */
    public void setJavaName(String newJavaName) {
        javaName = newJavaName;
    }
    
    /**
     * Set the catalog name.
     *
     * @param newCatalog Catalog name
     */
    public void setCatalog(String newCatalog) {
        catalog = newCatalog;
    }

    /**
     * Set the schema name.
     *
     * @param newSchema Schema name
     */
    public void setSchema(String newSchema) {
        schema = newSchema;
    }
    
    /**
     * Set the table columns.
     *
     * @param newColumns Table columns
     */
    public void setColumns(TableColumn[] newColumns) {
        columns = newColumns;
        
        // update the number of columns and columns selected
        if (columns != null) {
            numColumns = columns.length;
            
            int count = 0;
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].getIsSelected()) {
                    count++;
                }
            }
            numColumnsSelected = count;
        } else {
            numColumns = 0;
            numColumnsSelected = 0;
        }
    }
    
    /**
     * Clone the table columns.
     *
     * @param newColumns Table columns
     */
    public void cloneColumns(TableColumn[] newColumns) {
        numColumns = 0;
        numColumnsSelected = 0;

        int count = 0;
        if (newColumns != null) {
            numColumns = newColumns.length;
            if (numColumns > 0) {
                columns = new TableColumn[numColumns];
                for (int i = 0; i < numColumns; i++) {
                    columns[i] = new TableColumn(newColumns[i]);
                    if (columns[i].getIsSelected()) {
                        count++;
                    }
                }
            }
            numColumnsSelected = count;
        }
    }

    /**
     * Set the table type.
     *
     * @param newType Table type
     */
    public void setType(String newType) {
        type = newType;
    }
    
    /**
     * Get the index list.
     *
     * @return Index list
     */
    public List getIndexList() {
        return indexList;
    }
    
    /**
     * Set the index list.
     *
     * @param newList Index list
     */
    public void setIndexList(List newList) {
        if (newList != null && newList.size() != 0) {
            try {
                // Test to ensure that List contains nothing but Index objects.
                IndexColumn[] 
                    dummy = (IndexColumn[]) newList.toArray(new IndexColumn[newList.size()]);
            } catch (ArrayStoreException e) {
                throw new IllegalArgumentException(
                        "newList does not contain Index objects!");   
            }
            
            indexList = newList;
        }
    }

    public void cloneIndexList(List newList) {
        indexList = Collections.EMPTY_LIST;

        if (newList != null && newList.size() != 0) {
            indexList = new ArrayList();

            try {
                // Test to ensure that List contains nothing but Index objects.
                IndexColumn[]
                    dummy = (IndexColumn[]) newList.toArray(new IndexColumn[newList.size()]);
                for (int i = 0; i < newList.size(); i++) {
                    IndexColumn iCol = (IndexColumn) newList.get(i);
                    indexList.add(new IndexColumn(iCol));
                }
            } catch (ArrayStoreException e) {
                throw new IllegalArgumentException(
                        "newList does not contain Index objects!");
            }
        }
    }

    //added by Neena
    //to set the selection state of the table 
    
    public void setSelected(boolean selected){
    	this.selected = selected;	
    }

    //added by Neena
    // to get the selection state of the object
    
    public boolean isSelected(){
    	return selected;	
    }	

    /**
     * Gets current List of KeyColumn objects, representing primary key columns
     * in this table.
     *
     * @return List (possibly empty) of KeyColumn instances
     */
    public List getPrimaryKeyColumnList() {
        return pkColumnList;
    }

    /**
     * Sets List of primary key column objects to the given List.
     *
     * @param newList List containing new collection of KeyColumn objects
     * representing primary key columns within this table
     */
    public void setPrimaryKeyColumnList(List newList) {
        if (newList != null && newList.size() != 0) {
            try {
                // Test to ensure that List contains nothing but Index objects.
                KeyColumn[] 
                    dummy = (KeyColumn[]) newList.toArray(new KeyColumn[newList.size()]);
            } catch (ArrayStoreException e) {
                Locale locale = Locale.getDefault();
                ResourceBundle cMessages = ResourceBundle.getBundle("com/stc/oracle/builder/Bundle", locale); // NO i18n
                throw new IllegalArgumentException(
                        cMessages.getString("ERROR_KEY")+"(ERROR_KEY)");// NO i18n
            }
            
            pkColumnList = newList;
        }
    }

    public void clonePrimaryKeyColumnList(List newList) {
        pkColumnList = Collections.EMPTY_LIST;

        if (newList != null && newList.size() != 0) {
            pkColumnList = new ArrayList();

            try {
                // Test to ensure that List contains nothing but Index objects.
                KeyColumn[]
                    dummy = (KeyColumn[]) newList.toArray(new KeyColumn[newList.size()]);
                for (int i = 0; i < newList.size(); i++) {
                    KeyColumn tPK = (KeyColumn) newList.get(i);
                    pkColumnList.add(new KeyColumn(tPK.getName(), tPK.getColumnName(), tPK.getColumnSequence()));
                }
            } catch (ArrayStoreException e) {
                Locale locale = Locale.getDefault();
                ResourceBundle cMessages = ResourceBundle.getBundle("com/stc/oracle/builder/Bundle", locale); // NO i18n
                throw new IllegalArgumentException(
                        cMessages.getString("ERROR_KEY")+"(ERROR_KEY)");// NO i18n
            }
        }
    }


    /**
     * Gets current List of ForeignKeyColumn objects, representing foreign key
     * columns in this table.
     *
     * @return List (possibly empty) of ForeignKeyColumn instances
     */
    public List getForeignKeyColumnList() {
        return fkColumnList;
    }
    
    
    /**
     * Sets List of foreign key column objects to the given List.
     *
     * @param newList List containing new collection of ForeignKeyColumn objects
     * representing foreign key columns within this table
     */
    public void setForeignKeyColumnList(List newList) {
        if (newList != null && newList.size() != 0) {
            try {
                // Test to ensure that List contains nothing but Index objects.
                ForeignKeyColumn[] 
                    dummy = (ForeignKeyColumn[]) newList.toArray(new ForeignKeyColumn[newList.size()]);
            } catch (ArrayStoreException e) {
                Locale locale = Locale.getDefault();
                ResourceBundle cMessages = ResourceBundle.getBundle("com/stc/oracle/builder/Bundle", locale); // NO i18n
                throw new IllegalArgumentException(
                        cMessages.getString("ERROR_FK_KEY")+"(ERROR_FK_KEY)");// NO i18n
            }
            
            fkColumnList = newList;
        }
    }

    public void cloneForeignKeyColumnList(List newList) {
        fkColumnList = Collections.EMPTY_LIST;

        if (newList != null && newList.size() != 0) {
            fkColumnList = new ArrayList();

            try {
                // Test to ensure that List contains nothing but Index objects.
                ForeignKeyColumn[]
                    dummy = (ForeignKeyColumn[]) newList.toArray(new ForeignKeyColumn[newList.size()]);
                for (int i = 0; i < newList.size(); i++) {
                    ForeignKeyColumn fkCol = (ForeignKeyColumn) newList.get(i);
                    fkColumnList.add(new ForeignKeyColumn(fkCol));
                }
            } catch (ArrayStoreException e) {
                Locale locale = Locale.getDefault();
                ResourceBundle cMessages = ResourceBundle.getBundle("com/stc/oracle/builder/Bundle", locale); // NO i18n
                throw new IllegalArgumentException(
                        cMessages.getString("ERROR_FK_KEY")+"(ERROR_FK_KEY)");// NO i18n
            }
        }
    }
}
