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

package org.netbeans.modules.db.sql.visualeditor.api;

import java.awt.Component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilder;

/**
 * Class to encapsulate a visual SQL editor.
 *
 * @author Jim Davidson
 */
public final class VisualSQLEditor {

    // Private fields
    
    /**
     * Property corresponding to the SQL statement; for listening.
     */
    public static final String		PROP_STATEMENT="STATEMENT";

    private String 			statement;
    private VisualSQLEditorMetaData 	metadata;
    private DatabaseConnection		dbconn;
    private Component			queryBuilder=null;
    
    private PropertyChangeSupport 	changeSupport = new PropertyChangeSupport(this);


    /*
     * Constructor for VisualSQLEditor.
     * Package protected, used only by Factory class.
     */
    VisualSQLEditor(DatabaseConnection dbconn, String statement, VisualSQLEditorMetaData metadata) {
	this.dbconn = dbconn;
	this.statement = statement;
	this.metadata = metadata;
    }

    /**
     * Create and open the QueryBuilder that backs up this VisualSQLEditor instance.
     *
     * @return the new QueryBuilder component
     */
    public Component open() {
        // return QueryBuilder.open(dbconn, statement, metadata);
	queryBuilder = QueryBuilder.open(dbconn, statement, metadata, this);
	return queryBuilder;
    }


    /**
     * Returns the current value of the SQL statement
     *
     * @return the statement
     */
    public String getStatement(){
	return this.statement;
    }

    /**
     * Sets the value of the SQL statement
     *
     * @param statement - the new statement value
     */
    public void setStatement(String statement) {
	String oldValue = this.statement;
	this.statement = statement;
        changeSupport.firePropertyChange(PROP_STATEMENT, oldValue, statement);
    }


    /**
     * Adds a property change listener.  The only property of interest is PROP_STATEMENT,
     * which contains the SQL query.
     *
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener){
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.  
     *
     * @param listener - the listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener){
        changeSupport.removePropertyChangeListener(listener);
    }

}

