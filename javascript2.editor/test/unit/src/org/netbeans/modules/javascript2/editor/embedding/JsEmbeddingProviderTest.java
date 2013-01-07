/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.embedding;

import java.util.List;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider.EmbeddingPosition;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsEmbeddingProviderTest extends JsTestBase {

    public JsEmbeddingProviderTest(String testName) {
        super(testName);
    }

    public void testExtractJsEmbeddings01() throws Exception {
        String text = "                          ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(embeddings.size(), 1);
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(text.length(), embeddings.get(0).getLength());

        embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 5);
        assertEquals(embeddings.size(), 1);
        assertEquals(5, embeddings.get(0).getOffset());
        assertEquals(text.length(), embeddings.get(0).getLength());
    }

    public void testExtractJsEmbeddings02() throws Exception {
        String text = "    <!-- anything \n -->             ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(embeddings.size(), 2);
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(4, embeddings.get(0).getLength());
        assertEquals(19, embeddings.get(1).getOffset());
        assertEquals(17, embeddings.get(1).getLength());

        embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 5);
        assertEquals(embeddings.size(), 2);
        assertEquals(5, embeddings.get(0).getOffset());
        assertEquals(4, embeddings.get(0).getLength());
        assertEquals(24, embeddings.get(1).getOffset());
        assertEquals(17, embeddings.get(1).getLength());
    }

    public void testExtractJsEmbeddings03() throws Exception {
        String text = "       <!-- Cau Pisliku -->             ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(embeddings.size(), 2);
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("<!-- Cau Pisliku -->"), embeddings.get(0).getLength());

        embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 5);
        assertEquals(embeddings.size(), 2);
        assertEquals(5, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("<!-- Cau Pisliku -->"), embeddings.get(0).getLength());
    }

    public void testExtractJsEmbeddings04() throws Exception {
        String text = "<!-- comment --> javascript code \n<!-- comment -->";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(1, embeddings.size());
        assertEquals(text.indexOf(" javascript code "), embeddings.get(0).getOffset());
        assertEquals(18, embeddings.get(0).getLength());
    }

    public void testExtractJsEmbeddings05() throws Exception {
        String text = "\n"+
"\n"+
"                <!-- aaaaaaaa0 -->asdf\n"+
"                <!-- sdfsdsdsds\n"+
"\n"+
"    asfdasdfasd fasdf asdf\n"+
"\n"+
"    -->\n"+
"\n"+
"            dfasdasd      <!-- aaaaaaaa1 -->\n"+
"\n"+
"              asdfa  <!-- aaaaaaaa2 -->afs dfasdfasd\n"+
"                <!-- aaaaaaaa3 -->\n"+
"\n"+
"                asdfasdfasd\n"+
"\n"+
"                <!-- aaaaaaaa4 -->\n"+
"\n"+
"                afsdasdf\n"+
"\n"+
"        ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 193);
        assertEquals(6, embeddings.size());
        assertEquals(193, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa0 -->") + 18 + 193, embeddings.get(1).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa1 -->") + 18 + 193, embeddings.get(2).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa2 -->") + 18 + 193, embeddings.get(3).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa3 -->") + 18 + 193, embeddings.get(4).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa4 -->") + 18 + 193, embeddings.get(5).getOffset());

        assertEquals(18, embeddings.get(0).getLength());
        assertEquals(101, embeddings.get(1).getLength());
        assertEquals(23, embeddings.get(2).getLength());
        assertEquals(30, embeddings.get(3).getLength());
        assertEquals(47, embeddings.get(4).getLength());
        assertEquals(36, embeddings.get(5).getLength());
    }

    public void testExtractJsEmbeddings06() throws Exception {
        String text = "\n  <!--\n  myImage = new Image();\n  myImage = '../templates/images/indicator.gif';\n  //-->";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(2, embeddings.size());
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("  myImage = new Image();"), embeddings.get(1).getOffset());
        assertEquals(3, embeddings.get(0).getLength());
        assertEquals(81, embeddings.get(1).getLength());
    }

    public void testIssue223883() throws Exception {
        String text =   "<!--//--><![CDATA[//><!--\n" +
                        "    var abcd ='11111';\n" +
                        "//--><!]]>";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(1, embeddings.size());
        assertEquals(text.indexOf("    var abcd ='11111';\n"), embeddings.get(0).getOffset());
        assertEquals(33, embeddings.get(0).getLength());
    }

    public void testIssue217081() throws Exception {
        String text =   "<!--\n" +
                        "    ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(1, embeddings.size());
        assertEquals(text.indexOf("    "), embeddings.get(0).getOffset());
        assertEquals(4, embeddings.get(0).getLength());
    }

}
