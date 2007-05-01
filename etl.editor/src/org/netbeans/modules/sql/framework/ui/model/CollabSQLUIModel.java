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
package org.netbeans.modules.sql.framework.ui.model;

import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;

import com.sun.sql.framework.exception.BaseException;

public interface CollabSQLUIModel extends SQLUIModel {

    /**
     * Indicates whether the table represented by the given DBTable already exists in this
     * model
     * 
     * @param table DBTable whose existence is to be tested
     * @return true if table (source or target) exists in the model, false otherwise
     */
    public boolean exists(DBTable table);

    public SQLJoinView getJoinView(SourceTable sTable);

    public RuntimeDatabaseModel getRuntimeDbModel();

    public SQLDefinition getSQLDefinition();

    public boolean isReloaded();

    // reload
    public void reLoad(String sqlDefinitionXml) throws BaseException;

    public void removeDanglingColumnReference(SourceColumn column) throws BaseException;

    public void restoreLinks();

    public void restoreObjects() throws BaseException;

    /**
     * Rebuilds view model based on object pool and SQLDefinition hierarchy.
     */
    public void restoreUIState() throws BaseException;

    public void setReloaded(boolean reloaded);

    public void setSQLDefinition(SQLDefinition sqlDefinition);

}
