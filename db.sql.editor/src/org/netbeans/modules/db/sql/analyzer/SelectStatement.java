/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.sql.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import org.netbeans.modules.db.sql.analyzer.SQLStatementAnalyzer.TableIdent;

/**
 *
 * @author Andrei Badea
 */
public class SelectStatement extends SQLStatement {

    private final List<List<String>> selectValues;
    private final List<SelectStatement> subqueries;

    SelectStatement(int startOffset, int endOffset, List<List<String>> selectValues, TablesClause fromClause, List<SelectStatement> subqueries, SortedMap<Integer, Context> offset2Context) {
        super(startOffset, endOffset, offset2Context, fromClause);
        this.kind = SQLStatementKind.SELECT;
        this.selectValues = selectValues;
        this.subqueries = subqueries;
    }

    public TablesClause getTablesInEffect(int offset) {
        List<SelectStatement> statementPath = new ArrayList<SelectStatement>();
        fillStatementPath(offset, statementPath);
        if (statementPath.size() == 0) {
            return null;
        }
        if (statementPath.size() == 1) {
            return statementPath.get(0).getTablesClause();
        }
        Collections.reverse(statementPath);
        Set<QualIdent> unaliasedTableNames = new HashSet<QualIdent>();
        Map<String, QualIdent> aliasedTableNames = new HashMap<String, QualIdent>();
        for (SelectStatement statement : statementPath) {
            TablesClause statementFromClause = statement.getTablesClause();
            if (statementFromClause != null) {
                unaliasedTableNames.addAll(statementFromClause.getUnaliasedTableNames());
                for (Entry<String, QualIdent> entry : statementFromClause.getAliasedTableNames().entrySet()) {
                    String alias = entry.getKey();
                    QualIdent tableName = entry.getValue();
                    if (!aliasedTableNames.containsKey(alias)) {
                        aliasedTableNames.put(alias, tableName);
                    }
                }
            }
        }
        return new TablesClause(Collections.unmodifiableSet(unaliasedTableNames), Collections.unmodifiableMap(aliasedTableNames));
    }

    public List<List<String>> getSelectValues() {
        return selectValues;
    }

    public List<SelectStatement> getSubqueries() {
        return subqueries;
    }

    private void fillStatementPath(int offset, List<SelectStatement> path) {
        if (offset >= startOffset && offset <= endOffset) {
            path.add(this);
            for (SelectStatement subquery : subqueries) {
                subquery.fillStatementPath(offset, path);
            }
        }
    }
}
