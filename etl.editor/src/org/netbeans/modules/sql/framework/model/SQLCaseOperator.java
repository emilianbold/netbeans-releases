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

package org.netbeans.modules.sql.framework.model;

import java.util.List;

import com.sun.sql.framework.exception.BaseException;

/**
 * Case operator used when we want to do a join and lookup
 * 
 * @version $Revision$
 * @author Sudhi Seshachala
 */
public interface SQLCaseOperator extends SQLConnectableObject, SQLCanvasObject {

    public static final String DEFAULT = "default";

    public static final String SWITCH = "switch";

    /**
     * addSQLWhen adds an SQLWhen object to the when list.
     * 
     * @param when is the SQLWhen to be added.
     * @return boolean true when added.
     * @throws BaseException when the input is null.
     */
    public boolean addSQLWhen(SQLWhen when) throws BaseException;

    /**
     * createSQLWhen creates a new SQLWhen object and returns it.
     * 
     * @param theName is the name for the SQLWhen object.
     * @param predicate is a comparison object to use.
     * @param thenAction is an operand to use.
     * @return SQLWhen as the newly created object.
     * @throws BaseException if any input params are passed in as null.
     */
    public SQLWhen createSQLWhen() throws BaseException;

    public String generateNewWhenName();

    /**
     * get list of child sql objects
     */
    public List getChildSQLObjects();

    public int getJdbcType();

    /**
     * Gets a specific SQLWhen by name.
     * 
     * @param whenName of the SQLWhen object to return.
     * @return SQLWhen instance with the given name.
     */
    public SQLWhen getWhen(String whenName);

    /**
     * getWhenCount returns the size of the when list.
     * 
     * @return int the size of the list.
     */
    public int getWhenCount();

    public List<SQLWhen> getWhenList();

    /**
     * removeSQLWhen removes an SQLWhen object to the when list.
     * 
     * @param when is the SQLWhen to be removed.
     * @return boolean true when removed.
     * @throws BaseException when the input is null.
     */
    public boolean removeSQLWhen(SQLWhen when) throws BaseException;

}

