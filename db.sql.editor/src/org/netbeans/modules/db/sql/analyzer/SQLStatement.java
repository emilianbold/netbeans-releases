/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.sql.analyzer;

import java.util.Map.Entry;
import java.util.SortedMap;

/**
 *
 * @author Jiri Rechtacek
 */
public class SQLStatement {

    SQLStatementKind kind;
    int startOffset, endOffset;
    SortedMap<Integer, Context> offset2Context;
    TablesClause tablesClause;

    SQLStatement(int startOffset, int endOffset, SortedMap<Integer, Context> offset2Context) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.offset2Context = offset2Context;
    }

    SQLStatement(int startOffset, int endOffset, SortedMap<Integer, Context> offset2Context, TablesClause tablesClause) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.offset2Context = offset2Context;
        this.tablesClause = tablesClause;
    }

    public SQLStatementKind getKind() {
        return kind;
    }

    public Context getContextAtOffset(int offset) {
        Context result = null;
        for (Entry<Integer, Context> entry : offset2Context.entrySet()) {
            if (offset >= entry.getKey()) {
                result = entry.getValue();
            } else {
                break;
            }
        }
        return result;
    }

    TablesClause getTablesClause() {
        return tablesClause;
    }

    public enum Context {

        START(0),
        // DROP TABLE
        DROP_TABLE(300),
        // INSERT
        INSERT_INTO(400),
        COLUMNS(420),
        VALUES(430),
        // SELECT
        SELECT(500),
        FROM(510),
        JOIN_CONDITION(520),
        WHERE(530),
        GROUP(540),
        GROUP_BY(550),
        HAVING(560),
        ORDER(570),
        ORDER_BY(580),
        // UPDATE
        UPDATE(600),
        SET(610);

        private final int order;

        private Context(int order) {
            this.order = order;
        }

        public boolean isAfter(Context context) {
            return this.order >= context.order;
        }
    }
}
