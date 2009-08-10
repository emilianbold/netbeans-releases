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

import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Jiri Rechtacek
 */
public class InsertStatementAnalyzerTest extends TestCase {

    public InsertStatementAnalyzerTest(String testName) {
        super(testName);
    }

    /**
     * Just ensuring the analyzer doesn't end in an infinite loop.
     */
    public void testCanAnalyze() throws Exception {
        assertNull(doAnalyze(""));
        assertCanAnalyze("insert");
        assertCanAnalyze("insert into");
        assertCanAnalyze("insert into tab (id, name) values (\"1\", \"John\")");
        assertCanAnalyze("insert into tab values (\"2\", \"Scott\", \"Staff\"");
    }

    public void testAnalyzeInsertWholeTable() throws Exception {
        InsertStatement statement = doAnalyze("insert into tab values (\"2\", \"Scott\", \"Staff\"");
        List<String> columns = statement.getColumns ();
        assertEquals(0, columns.size());
    }

    public void testAnalyzeInsertChosenColumns() throws Exception {
        InsertStatement statement = doAnalyze("insert into tab (id, name) values (\"1\", \"John\")");
        List<String> columns = statement.getColumns ();
        assertEquals(2, columns.size());
    }

//    public void testAnalyzeFromCommaDelimitedTableNames() throws Exception {
//        InsertStatement statement = doAnalyze("select * from foo, bar b");
//        assertEquals(Collections.singleton(new QualIdent("foo")), statement.getFromClause().getUnaliasedTableNames());
//        assertEquals(Collections.singletonMap("b", new QualIdent("bar")), statement.getFromClause().getAliasedTableNames());
//    }
//
//    public void testAnalyzeFromJoinTableNames() throws Exception {
//        InsertStatement statement = doAnalyze("select * from foo f inner join bar on f.");
//        assertEquals(Collections.singleton(new QualIdent("bar")), statement.getFromClause().getUnaliasedTableNames());
//        assertEquals(Collections.singletonMap("f", new QualIdent("foo")), statement.getFromClause().getAliasedTableNames());
//    }
//
//    public void testEndOfFromClauseIssue145143() throws Exception {
//        Set<QualIdent> expected = Collections.singleton(new QualIdent("foo"));
//        assertEquals(expected, doAnalyze("select * from foo where max(bar, baz) < 0").getFromClause().getUnaliasedTableNames());
//        assertEquals(expected, doAnalyze("select * from foo having max(bar, baz) < 0").getFromClause().getUnaliasedTableNames());
//        assertEquals(expected, doAnalyze("select * from foo order by bar, baz").getFromClause().getUnaliasedTableNames());
//        assertEquals(expected, doAnalyze("select * from foo group by bar, baz").getFromClause().getUnaliasedTableNames());
//    }
//
//    public void testFromClause() throws Exception {
//        InsertStatement statement = doAnalyze("select foo");
//        assertNull(statement.getFromClause());
//
//        statement = doAnalyze("select foo from");
//        assertNotNull(statement.getFromClause());
//    }
//
//    public void testUnquote() throws Exception {
//        InsertStatement statement = doAnalyze("select * from \"foo\".\"bar\"");
//        assertEquals(Collections.singleton(new QualIdent("foo", "bar")), statement.getFromClause().getUnaliasedTableNames());
//
//        statement = doAnalyze("select * from \"foo\".\"\" inner join \"baz\"");
//        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("foo"), new QualIdent("baz"))), statement.getFromClause().getUnaliasedTableNames());
//
//        statement = doAnalyze("select * from \"\"");
//        assertTrue(statement.getFromClause().getUnaliasedTableNames().isEmpty());
//    }
//
//    public void testUnknownIdentifiers() throws Exception {
//        // SQL generated from embedded code may have __UNKNOWN__ flags in it -
//        // make sure these are handled correctly
//        InsertStatement stmt = doAnalyze("SELECT __UNKNOWN__");
//        List<List<String>> selectValues = stmt.getSelectValues();
//        assertEquals(1, selectValues.size());
//        assertEquals(Arrays.asList("__UNKNOWN__"), selectValues.get(0));
//
//        stmt = doAnalyze("SELECT foo FROM __UNKNOWN__, bar");
//        selectValues = stmt.getSelectValues();
//        assertEquals(1, selectValues.size());
//        assertEquals(Arrays.asList("foo"), selectValues.get(0));
//        FromClause fromClause = stmt.getFromClause();
//        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("__UNKNOWN__"), new QualIdent("bar"))), fromClause.getUnaliasedTableNames());
//
//        // From PHP, sometimes you get weird things if the string isn't complete,
//        // we should be able to handle this...
//        stmt = doAnalyze("SELECT  FROM foo,\necho");
//        selectValues = stmt.getSelectValues();
//        assertEquals(0, selectValues.size());
//        fromClause = stmt.getFromClause();
//        Set<QualIdent> tableNames = fromClause.getUnaliasedTableNames();
//        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("foo"), new QualIdent("echo"))), tableNames);
//    }
//
//    public void testSubqueries() throws Exception {
//        String sql = "select * from foo where exists (select id from bar where bar.id = foo.id and (select count(id) from baz where bar.id = baz.id) = 1) order by xyz";
//        int firstSubStart = sql.indexOf("(select") + 1;
//        int firstSubEnd = sql.indexOf(" order", firstSubStart) - 1;
//        int secondSubStart = sql.indexOf("(select", firstSubStart) + 1;
//        int secondSubEnd = sql.indexOf(" = 1", secondSubStart) - 1;
//
//        InsertStatement statement = doAnalyze(sql);
//        assertEquals(0, statement.startOffset);
//        assertEquals(sql.length(), statement.endOffset);
//        assertTrue(statement.getFromClause().getUnaliasedTableNames().contains(new QualIdent("foo")));
//        assertEquals(1, statement.getSubqueries().size());
//
//        InsertStatement subquery = statement.getSubqueries().get(0);
//        assertEquals(firstSubStart, subquery.startOffset);
//        assertEquals(firstSubEnd, subquery.endOffset);
//        assertEquals(sql.length(), statement.endOffset);
//        assertTrue(subquery.getFromClause().getUnaliasedTableNames().contains(new QualIdent("bar")));
//        assertEquals(1, statement.getSubqueries().size());
//
//        subquery = subquery.getSubqueries().get(0);
//        assertEquals(secondSubStart, subquery.startOffset);
//        assertEquals(secondSubEnd, subquery.endOffset);
//        assertTrue(subquery.getFromClause().getUnaliasedTableNames().contains(new QualIdent("baz")));
//        assertEquals(0, subquery.getSubqueries().size());
//    }
//
//    public void testContext() throws Exception {
//        String sql = "select customer_id from customer inner join invoice on customer.id = invoice.customer_id, foobar " +
//                "where vip = 1 group by customer_id having count(items) < 2 order by customer_id asc";
//        InsertStatement statement = doAnalyze(sql);
//        assertNull(statement.getContextAtOffset(0));
//        assertEquals(SelectContext.SELECT, statement.getContextAtOffset(sql.indexOf(" customer_id")));
//        assertEquals(SelectContext.FROM, statement.getContextAtOffset(sql.indexOf("customer ")));
//        assertEquals(SelectContext.JOIN_CONDITION, statement.getContextAtOffset(sql.indexOf(".id =")));
//        assertEquals(SelectContext.FROM, statement.getContextAtOffset(sql.indexOf(" foobar")));
//        assertEquals(SelectContext.WHERE, statement.getContextAtOffset(sql.indexOf("vip")));
//        assertEquals(SelectContext.GROUP_BY, statement.getContextAtOffset(sql.indexOf("customer_id having")));
//        assertEquals(SelectContext.HAVING, statement.getContextAtOffset(sql.indexOf("count")));
//        assertEquals(SelectContext.ORDER_BY, statement.getContextAtOffset(sql.indexOf("customer_id asc")));
//    }

    public void testDetectKind() throws Exception {
        assertNull(doDetectKind("foo"));
        assertEquals(SQLStatementKind.INSERT, doDetectKind("insert"));
        assertEquals(SQLStatementKind.INSERT, doDetectKind("insert into tab"));
        assertFalse(SQLStatementKind.INSERT.equals (doDetectKind("select")));
        assertFalse(SQLStatementKind.INSERT.equals (doDetectKind("select * from foo")));
    }

    private static InsertStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return InsertStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
    }

    private static SQLStatementKind doDetectKind(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        return SQLStatementAnalyzer.analyzeKind(hi.tokenSequence(SQLTokenId.language()));
    }

    public static void assertCanAnalyze(String sql) throws IOException {
        assertNotNull(doAnalyze(sql));
    }
}
