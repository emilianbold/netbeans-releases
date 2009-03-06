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

/**
 *
 * @author Jiri Rechtacek
 */
public class InsertStatement extends SQLStatement {

    private final SQLStatementKind kind;
    int startOffset, endOffset;
    private final List<String> columns;
    private final List<String> values;
    private final SortedMap<Integer, InsertContext> offset2Context;

    InsertStatement(SQLStatementKind kind, int startOffset, int endOffset, List<String> columns, List<String> values, SortedMap<Integer, InsertContext> offset2Context) {
        this.kind = kind;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.columns = columns;
        this.values = values;
        this.offset2Context = offset2Context;
    }

    public SQLStatementKind getKind() {
        return kind;
    }

    public FromClause getTablesInEffect(int offset) {
        List<InsertStatement> statementPath = new ArrayList<InsertStatement>();
        fillStatementPath(offset, statementPath);
        if (statementPath.size() == 0) {
            return null;
        }
        Collections.reverse(statementPath);
        Set<QualIdent> unaliasedTableNames = new HashSet<QualIdent>();
        Map<String, QualIdent> aliasedTableNames = new HashMap<String, QualIdent>();
        return new FromClause(Collections.unmodifiableSet(unaliasedTableNames), Collections.unmodifiableMap(aliasedTableNames));
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<String> getValues() {
        return values;
    }

    public InsertContext getContextAtOffset(int offset) {
        InsertContext result = null;
        for (Entry<Integer, InsertContext> entry : offset2Context.entrySet()) {
            if (offset >= entry.getKey()) {
                result = entry.getValue();
            } else {
                break;
            }
        }
        return result;
    }

    private void fillStatementPath(int offset, List<InsertStatement> path) {
        if (offset >= startOffset && offset <= endOffset) {
            path.add(this);
        }
    }

    public enum InsertContext {

        INSERT,
        INTO,
        COLUMNS,
        VALUES,
    }
}
