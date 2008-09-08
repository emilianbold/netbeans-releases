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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class SQLStatementAnalyzerTest extends TestCase {

    public SQLStatementAnalyzerTest(String testName) {
        super(testName);
    }

    /**
     * Just ensuring the analyzer doesn't end in an infinite loop.
     */
    public void testCanAnalyze() throws Exception {
        assertCanAnalyze("select");
        assertCanAnalyze("select from dual");
        assertCanAnalyze("select count(*) table_count from table");
        assertCanAnalyze("select component, count(id) from issuezilla group by component");
        assertCanAnalyze("select * from a where (select count(*) from b where a.x = b.x) > 42");
        assertCanAnalyze("select * from a where (select (select");
    }

    public void testAnalyzeSimple() throws Exception {
        SQLStatement statement = doAnalyze("select f.bar as bingo, count(*), max(id) + 1 themax, cat.sch.foo.baz from foo f inner join bar");
        List<List<String>> selectValues = statement.getSelectValues();
        assertEquals(3, selectValues.size());
        assertEquals(Arrays.asList("bingo"), selectValues.get(0));
        assertEquals(Arrays.asList("themax"), selectValues.get(1));
        assertEquals(Arrays.asList("cat", "sch", "foo", "baz"), selectValues.get(2));
        FromClause fromClause = statement.getFromClause();
        assertEquals(Collections.singletonMap("f", new QualIdent("foo")), fromClause.getAliasedTableNames());
        assertEquals(Collections.singleton(new QualIdent("bar")), fromClause.getUnaliasedTableNames());
    }

    public void testAnalyzeFromCommaDelimitedTableNames() throws Exception {
        SQLStatement statement = doAnalyze("select * from foo, bar b");
        assertEquals(Collections.singleton(new QualIdent("foo")), statement.getFromClause().getUnaliasedTableNames());
        assertEquals(Collections.singletonMap("b", new QualIdent("bar")), statement.getFromClause().getAliasedTableNames());
    }

    public void testAnalyzeFromJoinTableNames() throws Exception {
        SQLStatement statement = doAnalyze("select * from foo f inner join bar on f.");
        assertEquals(Collections.singleton(new QualIdent("bar")), statement.getFromClause().getUnaliasedTableNames());
        assertEquals(Collections.singletonMap("f", new QualIdent("foo")), statement.getFromClause().getAliasedTableNames());
    }

    public void testEndOfFromClauseIssue145143() throws Exception {
        Set<QualIdent> expected = Collections.singleton(new QualIdent("foo"));
        assertEquals(expected, doAnalyze("select * from foo where max(bar, baz) < 0").getFromClause().getUnaliasedTableNames());
        assertEquals(expected, doAnalyze("select * from foo having max(bar, baz) < 0").getFromClause().getUnaliasedTableNames());
        assertEquals(expected, doAnalyze("select * from foo order by bar, baz").getFromClause().getUnaliasedTableNames());
        assertEquals(expected, doAnalyze("select * from foo group by bar, baz").getFromClause().getUnaliasedTableNames());
    }

    public void testFromClause() throws Exception {
        SQLStatement statement = doAnalyze("select foo");
        assertNull(statement.getFromClause());

        statement = doAnalyze("select foo from");
        assertNotNull(statement.getFromClause());
    }

    public void testUnquote() throws Exception {
        SQLStatement statement = doAnalyze("select * from \"foo\".\"bar\"");
        assertEquals(Collections.singleton(new QualIdent("foo", "bar")), statement.getFromClause().getUnaliasedTableNames());

        statement = doAnalyze("select * from \"foo\".\"\" inner join \"baz\"");
        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("foo"), new QualIdent("baz"))), statement.getFromClause().getUnaliasedTableNames());

        statement = doAnalyze("select * from \"\"");
        assertTrue(statement.getFromClause().getUnaliasedTableNames().isEmpty());
    }

    public void testSubqueries() throws Exception {
        String sql = "select * from foo where exists (select id from bar where bar.id = foo.id and (select count(id) from baz where bar.id = baz.id) = 1) order by xyz";
        int firstSubStart = sql.indexOf("(select") + 1;
        int firstSubEnd = sql.indexOf(" order", firstSubStart) - 1;
        int secondSubStart = sql.indexOf("(select", firstSubStart) + 1;
        int secondSubEnd = sql.indexOf(" = 1", secondSubStart) - 1;

        SQLStatement statement = doAnalyze(sql);
        assertEquals(0, statement.startOffset);
        assertEquals(sql.length(), statement.endOffset);
        assertTrue(statement.getFromClause().getUnaliasedTableNames().contains(new QualIdent("foo")));
        assertEquals(1, statement.getSubqueries().size());

        SQLStatement subquery = statement.getSubqueries().get(0);
        assertEquals(firstSubStart, subquery.startOffset);
        assertEquals(firstSubEnd, subquery.endOffset);
        assertEquals(sql.length(), statement.endOffset);
        assertTrue(subquery.getFromClause().getUnaliasedTableNames().contains(new QualIdent("bar")));
        assertEquals(1, statement.getSubqueries().size());

        subquery = subquery.getSubqueries().get(0);
        assertEquals(secondSubStart, subquery.startOffset);
        assertEquals(secondSubEnd, subquery.endOffset);
        assertTrue(subquery.getFromClause().getUnaliasedTableNames().contains(new QualIdent("baz")));
        assertEquals(0, subquery.getSubqueries().size());
    }

    private static SQLStatement doAnalyze(String sql, Quoter quoter) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        return SQLStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
    }

    private static SQLStatement doAnalyze(String sql) throws IOException {
        return doAnalyze(sql, SQLIdentifiersTestUtilities.createNonASCIIQuoter("\""));
    }

    public static void assertCanAnalyze(String sql) throws IOException {
        doAnalyze(sql);
    }
}
