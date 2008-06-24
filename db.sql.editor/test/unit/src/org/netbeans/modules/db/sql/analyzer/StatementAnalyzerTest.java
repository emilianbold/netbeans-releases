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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class StatementAnalyzerTest extends TestCase {

    public StatementAnalyzerTest(String testName) {
        super(testName);
    }

    public void testParse() throws Exception {
        assertCanAnalyze("select");
        assertCanAnalyze("select from dual");
        assertCanAnalyze("select count(*) table_count from table");
        assertCanAnalyze("select component, count(id) from issuezilla group by component");
        assertCanAnalyze("select * from a where (select count(*) from b where a.x = b.x) > 42");

        StatementAnalyzer analyzer = doAnalyze("select f.bar as bingo, count(*), max(id) + 1 themax, cat.sch.foo.baz from foo f inner join bar");
        List<List<String>> selectValues = analyzer.getSelectValues();
        assertEquals(3, selectValues.size());
        assertEquals(Arrays.asList("bingo"), selectValues.get(0));
        assertEquals(Arrays.asList("themax"), selectValues.get(1));
        assertEquals(Arrays.asList("cat", "sch", "foo", "baz"), selectValues.get(2));
        FromTables fromTables = analyzer.getFromTables();
        assertEquals(2, fromTables.size());
        FromTable fromTable = fromTables.get(0);
        assertEquals(Arrays.asList("foo"), fromTable.getParts());
        assertEquals("f", fromTable.getAlias());
        fromTable = fromTables.get(1);
        assertEquals(Arrays.asList("bar"), fromTable.getParts());
        assertNull(fromTable.getAlias());

        analyzer = doAnalyze("select foo");
        assertNull(analyzer.getFromTables());

        analyzer = doAnalyze("select foo from");
        assertEquals(0, analyzer.getFromTables().size());
    }

    private StatementAnalyzer doAnalyze(String sql) throws IOException {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        StatementAnalyzer analyzer = new StatementAnalyzer(hi.tokenSequence(SQLTokenId.language()));
        return analyzer;
    }

    public static void assertCanAnalyze(String sql) throws Exception {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        StatementAnalyzer analyzer = new StatementAnalyzer(hi.tokenSequence(SQLTokenId.language()));
    }
}
