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

package org.netbeans.modules.sql.framework.model;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface SQLJoinView extends SQLContainerObject, SQLCanvasObject, SQLVisitedObject {

    public boolean containsSourceTable(SourceTable table);

    /**
     * get the alias name for this join view
     * 
     * @return alias name
     */
    public String getAliasName();

    public SQLJoinOperator getJoinofTable(SQLJoinTable jTable);

    public SQLJoinTable getJoinTable(SourceTable sTable);

    /**
     * get table qualified name
     * 
     * @return qualified table name prefixed with alias
     */
    public String getQualifiedName();

    /**
     * get the root join located in this join view
     * 
     * @return root join
     */
    public SQLJoinOperator getRootJoin();

    public List getSourceTables();

    public Collection getSQLJoinTables();
    
    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy();    

    public boolean isSourceColumnVisible(SQLDBColumn table);

    public void removeTablesAndJoins(SourceTable sTable) throws BaseException;

    /**
     * set the alias name for this join view
     * 
     * @param aName alias name
     */
    public void setAliasName(String aName);
    
    /**
     * set group by object
     * 
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy);    

}
