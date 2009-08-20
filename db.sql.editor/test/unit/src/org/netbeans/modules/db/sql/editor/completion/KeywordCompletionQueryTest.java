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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.editor.completion;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.metadata.model.api.Metadata;

/** Tests completion of SQL keywords.
 *
 * @author Jiri Skrivanek
 */
public class KeywordCompletionQueryTest extends NbTestCase {

    private static final String[] model = {
        "catalog_1*",
        "  sch_customers*",
        "    tab_customer",
        "      col_customer_id",
        "  sch_accounting",
        "    tab_invoice",
        "      col_invoice_id",
        "    tab_purchase_order",
        "      col_order_id",
        "catalog_2"
    };
    private static List<String> modelData = Arrays.asList(model);
    private static Metadata metadata = TestMetadata.create(modelData);

    public KeywordCompletionQueryTest(String testName) {
        super(testName);
    }

    public void testEmptyStatement() {
        String sql = "|";
        assertItems(doQuery(sql, metadata), "DELETE", "DROP", "INSERT", "SELECT", "UPDATE");
        sql = " |";
        assertItems(doQuery(sql, metadata), "DELETE", "DROP", "INSERT", "SELECT", "UPDATE");
        sql = "D|";
        assertItems(doQuery(sql, metadata), "DELETE", "DROP");
    }

    public void testSelect() {
        String sql = "SELECT|";
        assertItems(doQuery(sql, metadata));
        sql = "SELECT * |";
        assertItems(doQuery(sql, metadata), "FROM");
        sql = "SELECT col_customer_id |";
        assertItems(doQuery(sql, metadata), "FROM");
        sql = "SELECT * FROM|";
        assertItems(doQuery(sql, metadata));
        sql = "SELECT * FROM t |";
        assertItems(doQuery(sql, metadata), "WHERE");
        sql = "SELECT * FROM t WHERE|";
        assertItems(doQuery(sql, metadata));
        sql = "SELECT * FROM t WHERE c1 > 1 |";
        assertItems(doQuery(sql, metadata), "GROUP", "ORDER");
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP|";
        assertItems(doQuery(sql, metadata));
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP |";
        assertItems(doQuery(sql, metadata), "BY");
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP BY c3 |";
        assertItems(doQuery(sql, metadata), "HAVING");
        sql = "SELECT * FROM t WHERE c1 > 1 GROUP |";
        assertItems(doQuery(sql, metadata), "BY");
        sql = "SELECT * FROM t WHERE c1 > 1 ORDER |";
        assertItems(doQuery(sql, metadata), "BY");

        sql = "SELECT * FROM inner join t1 on c1 |";
        assertItems(doQuery(sql, metadata), "WHERE");
    }

    public void testSubqueries() {
        String sql = "SELECT * FROM t WHERE c1 > (SELECT * |";
        assertItems(doQuery(sql, metadata), "FROM");
        sql = "SELECT * FROM t WHERE c1 > (SELECT * FROM t2 |";
        assertItems(doQuery(sql, metadata), "WHERE");
        sql = "SELECT * FROM t WHERE c1 > (SELECT * FROM t2 WHERE c2 = c3 |";
        assertItems(doQuery(sql, metadata), "GROUP", "ORDER");
        sql = "SELECT * FROM t WHERE c1 > (SELECT * FROM t2) |";
        assertItems(doQuery(sql, metadata), "GROUP", "ORDER");
    }

    public void testDelete() {
        String sql = "DELETE|";
        assertItems(doQuery(sql, metadata));
        sql = "DELETE |"; // multiple table delete supported
        assertItems(doQuery(sql, metadata), "FROM", "tab_customer", "sch_accounting", "sch_customers", "catalog_1", "catalog_2");
        sql = "DELETE FROM t |";
        assertItems(doQuery(sql, metadata), "WHERE");
    }

    public void testDrop() {
        String sql = "DROP |";
        assertItems(doQuery(sql, metadata), "TABLE");
    }

    public void testInsert() {
        String sql = "INSERT |";
        assertItems(doQuery(sql, metadata), "INTO");
        sql = "INSERT INTO t |";
        assertItems(doQuery(sql, metadata), "VALUES");
    }

    public void testUpdate() {
        String sql = "UPDATE t |";
        assertItems(doQuery(sql, metadata), "SET");
        sql = "UPDATE t SET c1 = c2 |";
        assertItems(doQuery(sql, metadata), "WHERE");
    }

    private static void assertItems(SQLCompletionItems items, String... expected) {
        int index = 0;
        for (SQLCompletionItem item : items) {
            assertTrue("Expected<" + expected[index] + "> not found in <" + item + ">", item.toString().contains(expected[index]));
            index++;
        }
        assertEquals("Wrong number of items returned.", expected.length, index);
    }

    private static SQLCompletionItems doQuery(String sql, Metadata metadata) {
        int caretOffset = sql.indexOf('|');
        if (caretOffset >= 0) {
            sql = sql.replace("|", "");
        } else {
            throw new IllegalArgumentException("Missing | in SQL staement.");
        }
        SQLCompletionQuery query = new SQLCompletionQuery(null);
        SQLCompletionEnv env = SQLCompletionEnv.forScript(sql, caretOffset);
        return query.doQuery(env, metadata, SQLIdentifiersTestUtilities.createNonASCIIQuoter("\""));
    }
}
