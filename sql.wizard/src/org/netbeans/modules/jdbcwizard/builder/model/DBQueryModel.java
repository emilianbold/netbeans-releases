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
package org.netbeans.modules.jdbcwizard.builder.model;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;


/**
 * @author Venkat P
 */
public interface DBQueryModel {

    public void init(DBTable souTable);

    public String createInsertQuery() throws Exception;

    public String createUpdateQuery() throws Exception;

    public String createDeleteQuery() throws Exception;

    public String createFindQuery() throws Exception;

    public String createPoolQuery() throws Exception;

    public String getParamOrder(String queryType) throws Exception;

    public String getPrimaryKey() throws Exception;

}
