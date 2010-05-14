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

/* *************************************************************************
 *
 *          Copyright (c) 2005, Sun Microsystems,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          Sun Microsystems.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          Sun Microsystems.
 *
 ***************************************************************************/
package org.netbeans.modules.wsdlextensions.jdbc.builder.model;

import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;


/**
 * @author Venkat P
 */
public interface DBQueryModel {

    public void init(DBTable souTable);

    public String createInsertQuery() throws Exception;

    public String createUpdateQuery(String where) throws Exception;

    public String createDeleteQuery(String where) throws Exception;

    public String createFindQuery(String where) throws Exception;

    public String createPoolQuery(String where) throws Exception;

    public String getParamOrder(String queryType) throws Exception;

    public String getPrimaryKey() throws Exception;

}
