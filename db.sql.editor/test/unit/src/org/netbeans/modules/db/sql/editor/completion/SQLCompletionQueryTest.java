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

package org.netbeans.modules.db.sql.editor.completion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionQueryTest extends NbTestCase {

    private final String testName;
    private final boolean stdout;

    public SQLCompletionQueryTest(String testName) {
        this(testName, false);
    }

    public SQLCompletionQueryTest(String testName, boolean stdout) {
        super("testCompletion");
        this.testName = testName;
        this.stdout = stdout;
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        // Find a way to add the tests automatically (java.util.zip?).

        suite.addTest(new SQLCompletionQueryTest("selectAll"));
        suite.addTest(new SQLCompletionQueryTest("selectNoSchemas"));
        suite.addTest(new SQLCompletionQueryTest("selectSyntheticSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectSimple"));
        suite.addTest(new SQLCompletionQueryTest("selectSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectSchemaTable"));
        suite.addTest(new SQLCompletionQueryTest("selectTableInDefaultSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectTableInNonDefaultSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectColumn"));

        suite.addTest(new SQLCompletionQueryTest("selectEmptyFromClause"));
        suite.addTest(new SQLCompletionQueryTest("selectAllFrom"));
        suite.addTest(new SQLCompletionQueryTest("selectSimpleFrom"));
        suite.addTest(new SQLCompletionQueryTest("selectFromQualTableInDefaultSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectFromUnqualTableInDefaultSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectFromSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectFromSchemaTableNotInFromClause"));
        suite.addTest(new SQLCompletionQueryTest("selectFromQuoted"));

        suite.addTest(new SQLCompletionQueryTest("selectQuotedSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectQuotedColumn"));
        suite.addTest(new SQLCompletionQueryTest("selectQuote"));

        suite.addTest(new SQLCompletionQueryTest("fromAll"));
        suite.addTest(new SQLCompletionQueryTest("fromSimple"));
        suite.addTest(new SQLCompletionQueryTest("fromSchema"));
        suite.addTest(new SQLCompletionQueryTest("selectFromJoinCondition"));
        suite.addTest(new SQLCompletionQueryTest("selectFromJoinConditionAlias"));

        suite.addTest(new SQLCompletionQueryTest("whereAll"));
        suite.addTest(new SQLCompletionQueryTest("whereSimple"));
        suite.addTest(new SQLCompletionQueryTest("whereSchema"));
        suite.addTest(new SQLCompletionQueryTest("whereSchemaTable"));
        suite.addTest(new SQLCompletionQueryTest("groupBySimple"));
        suite.addTest(new SQLCompletionQueryTest("orderBySimple"));

        suite.addTest(new SQLCompletionQueryTest("selectSubquery"));

        suite.addTest(new SQLCompletionQueryTest("script"));

        // Does not work yet.
        // suite.addTest(new SQLCompletionQueryTest("selectFromTableInNonDefaultSchema", true));

        return suite;
    }

    public void testCompletion() throws Exception {
        StringBuilder sqlData = new StringBuilder();
        List<String> modelData = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(SQLCompletionQueryTest.class.getResource(testName + ".test").openStream(), "utf-8"));
        try {
            boolean separatorRead = false;
            for (String line = null ; (line = reader.readLine()) != null;) {
                if (line.startsWith("#") || line.trim().length() == 0) {
                    continue;
                }
                if (line.equals("--")) {
                    separatorRead = true;
                } else {
                    if (separatorRead) {
                        modelData.add(line);
                    } else {
                        sqlData.append(line);
                    }
                }
            }
        } finally {
            reader.close();
        }
        String sql = sqlData.toString();
        Metadata metadata = TestMetadata.create(modelData);
        if (stdout) {
            performTest(sql, metadata, System.out);
        } else {
            File result = new File(getWorkDir(), testName + ".result");
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(result), "utf-8"));
            try {
                performTest(sql, metadata, writer);
            } finally {
                writer.close();
            }
            File pass = new File(getWorkDir(), testName + ".pass");
            InputStream input = SQLCompletionQueryTest.class.getResource(testName + ".pass").openStream();
            try {
                copyStream(input, pass);
            } finally {
                input.close();
            }
            assertFile(testName, result, pass, null);
        }
    }

    private static void performTest(String sql, Metadata metadata, Appendable output) throws Exception {
        int caretOffset = sql.indexOf('|');
        if (caretOffset >= 0) {
            sql = sql.replace("|", "");
        } else {
            throw new IllegalArgumentException();
        }
        SQLCompletionQuery query = new SQLCompletionQuery(null);
        SQLCompletionEnv env = SQLCompletionEnv.create(sql, caretOffset);
        for (SQLCompletionItem item : query.doQuery(env, metadata, SQLIdentifiersTestUtilities.createNonASCIIQuoter("\""))) {
            output.append(item.toString());
            output.append('\n');
        }
    }

    private static void copyStream(InputStream input, File dest) throws IOException {
        OutputStream output = new FileOutputStream(dest);
        try {
            FileUtil.copy(input, output);
        } finally {
            output.close();
        }
    }

    @Override
    public String toString() {
        return testName + "(" + getClass().getName() + ")";
    }
}
