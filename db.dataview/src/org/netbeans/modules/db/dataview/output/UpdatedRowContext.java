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
package org.netbeans.modules.db.dataview.output;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.TableModel;
import org.netbeans.modules.db.dataview.meta.DBException;

/**
 * Holds the updated row data
 *
 * @author Ahimanikya Satapathy
 */
class UpdatedRowContext {

    private Map<String, String> updateStatements = new LinkedHashMap<String, String>();
    private Map<String, String> rawUpdateSQL = new LinkedHashMap<String, String>();
    private Map<String, List<Object>> valuesList = new LinkedHashMap<String, List<Object>>();
    private Map<String, List<Integer>> typesList = new LinkedHashMap<String, List<Integer>>();
    private SQLStatementGenerator stmtGenerator;

    public UpdatedRowContext(SQLStatementGenerator stmtGenerator) {
        this.stmtGenerator = stmtGenerator;
    }

    // TODO: We can defer creating these statements till user request to execute
    public void createUpdateStatement(int row, int col, Object value, TableModel tblModel) throws DBException {
        List<Object> values = new ArrayList<Object>();
        List<Integer> types = new ArrayList<Integer>();
        String changeData = (row + 1) + ";" + (col + 1); // NOI18N
        String[] updateStmt = stmtGenerator.generateUpdateStatement(row, col, value, values, types, tblModel);

        updateStatements.put(changeData, updateStmt[0]);
        rawUpdateSQL.put(changeData, updateStmt[1]);
        valuesList.put(changeData, values);
        typesList.put(changeData, types);
    }

    public void resetUpdateState() {
        updateStatements = new LinkedHashMap<String, String>();
        rawUpdateSQL = new LinkedHashMap<String, String>();
        valuesList = new LinkedHashMap<String, List<Object>>();
        typesList = new LinkedHashMap<String, List<Integer>>();
    }

    public Set<String> getUpdateKeys() {
        return updateStatements.keySet();
    }

    public String getUpdateStmt(String key) {
        return updateStatements.get(key);
    }

    public void removeUpdateStmt(String key) {
        rawUpdateSQL.remove(key);
        updateStatements.remove(key);
        valuesList.remove(key);
        typesList.remove(key);
    }

    public String getRawUpdateStmt(String key) {
        return rawUpdateSQL.get(key);
    }

    public List<Integer> getTypeList(String key) {
        return typesList.get(key);
    }

    public List<Object> getValueList(String key) {
        return valuesList.get(key);
    }
}
