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
 
package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.Arrays;

import org.netbeans.modules.dbschema.*;

public class ForeignKeyElementImpl  extends KeyElementImpl implements ForeignKeyElement.Impl {

    private TableElementImpl tei;
    
    public ForeignKeyElementImpl() {
		this(null, null);
    }

    public ForeignKeyElementImpl(TableElementImpl tei, String name) {
		super(name);
        
        this.tei = tei;
    }
    
    protected DBElementsCollection initializeCollection() {
        return new DBElementsCollection(this, new ColumnPairElement[0]);
    }
  
    public ColumnPairElement[] getColumnPairs() {
        DBElement[] dbe = getColumnCollection().getElements();
        return (ColumnPairElement[]) Arrays.asList(dbe).toArray(new ColumnPairElement[dbe.length]);
    }
    
    public ColumnPairElement getColumnPair(DBIdentifier name) {
		return (ColumnPairElement) getColumnCollection().find(name);
    }
    
    public void changeColumnPairs(ColumnPairElement[] pairs,int action) throws DBException {
        getColumnCollection().changeElements(pairs, action);
    }
    
    public ColumnElement[] getColumns() {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        ColumnElement[] ce = new ColumnElement[cpe.length];
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            ce[i] = ((ForeignKeyElement) element).getDeclaringTable().getColumn(DBIdentifier.create(localColumn));
        }
        
        return ce;
    }
    
    public ColumnElement getColumn(DBIdentifier name) {
        ColumnPairElement[] cpe = getColumnPairs();
        
        if (cpe == null || cpe.length == 0)
            return null;
        
        for (int i = 0; i < cpe.length; i++) {
            String localColumn = cpe[i].getName().getFullName();
            int pos = localColumn.indexOf(";");
            localColumn = localColumn.substring(0, pos);

            if (name.getName().equals(DBIdentifier.create(localColumn).getName())) //need to check
                return ((ForeignKeyElement) element).getDeclaringTable().getColumn(name);
        }
        
        return null;
    }
    
}
