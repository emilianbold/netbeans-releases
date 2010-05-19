/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.ui.model;

import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;

import com.sun.etl.exception.BaseException;
import org.netbeans.modules.sql.framework.model.DBTable;

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
