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

package org.netbeans.modules.edm.model;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.edm.model.visitors.SQLVisitedObject;

import org.netbeans.modules.edm.model.EDMException;

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

    public List<DBTable> getSourceTables();

    public Collection<SQLJoinTable> getSQLJoinTables();
    
    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy();    

    public boolean isSourceColumnVisible(SQLDBColumn table);

    public void removeTablesAndJoins(SourceTable sTable) throws EDMException;

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
