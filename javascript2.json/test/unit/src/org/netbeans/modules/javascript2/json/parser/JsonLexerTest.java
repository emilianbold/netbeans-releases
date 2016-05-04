/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.json.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class JsonLexerTest extends NbTestCase {
    public JsonLexerTest(@NonNull final String name) {
        super(name);
    }

    public void testLineCommentLex() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1 //comment\n}");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1"," ","}");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testLineCommentLexEOF() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1} //comment");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1","}"," ");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testCommentLex() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1 /*comment*/\n}");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1"," ","\n","}");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testCommentLexEOF() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1 /*comment \n}");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1"," ");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public static void test1() throws Exception {
        final String str = "{\n" +
            "    \"cloud\" : \"my-cloud\",\n" +
            "    \"net\" : {\n" +
            "        \"vlan\" : \"my-id\", /* vlan tag \n" +
            "        \"subnet\" : 3,\n" +
            "        \"private\" : true\n" +
            "    },\n" +
            "    \"nodes\" : [   \n" +
            "        {\n" +
            "            \"name\" : \"storage\",\n" +
            "            \"ip\" : \"10.0.0.10\",\n" +
            "            \"image\" : \"ubuntu\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\" : \"web\",\n" +
            "            \"ip\" : \"10.0.0.11\",\n" +
            "            \"image\" : \"ubuntu\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
        final ANTLRInputStream in = new ANTLRInputStream(str);
        final JsonLexer lexer = new JsonLexer(in, true, true);
        lexer.getAllTokens().stream().forEach((t)->System.out.println(t));
    }
}
