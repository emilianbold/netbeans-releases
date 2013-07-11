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
package org.netbeans.modules.javascript2.editor.parser;

import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.javascript2.editor.JsonTestBase;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.parser.SanitizingParser.Context;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Hejl
 */
public class JsonParserTest extends JsonTestBase {

    public JsonParserTest(String testName) {
        super(testName);
    }

    public void testComments1() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"  //line comment\n"
            + "/*comment*/\n"
            + "}\n",
            Collections.singletonList("<html><pre>test.json:2:16 Expected , or } but found /"));
    }

    public void testComments2() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"  //line comment\n"
            + "}\n",
            Collections.singletonList("<html><pre>test.json:2:16 Expected , or } but found /"));
    }

    public void testComments3() throws Exception {
        parse("{\n"
            + "\"name\": \"test\"\n"
            + "/*comment*/\n"
            + "}\n",
            Collections.singletonList("<html><pre>test.json:3:0 Expected , or } but found /"));
    }

    private void parse(String original, List<String> errors) throws Exception {

        JsonParser parser = new JsonParser();
        Document doc = getDocument(original);
        Snapshot snapshot = Source.create(doc).createSnapshot();
        Context context = new JsParser.Context("test.json", snapshot, -1);
        JsErrorManager manager = new JsErrorManager(snapshot, JsTokenId.jsonLanguage());
        parser.parseContext(context, JsParser.Sanitize.NEVER, manager);

        assertEquals(errors.size(), manager.getErrors().size());
        for (int i = 0; i < errors.size(); i++) {
            if (!manager.getErrors().get(i).getDisplayName().startsWith(errors.get(i))) {
                fail("Error was expected to start with: " + errors.get(i) + " but was: "
                        + manager.getErrors().get(i).getDisplayName());
            }
        }
    }
}
