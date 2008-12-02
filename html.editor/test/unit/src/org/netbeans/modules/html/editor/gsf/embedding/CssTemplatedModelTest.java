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

package org.netbeans.modules.html.editor.gsf.embedding;

import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;

public class CssTemplatedModelTest extends NbTestCase {

    public CssTemplatedModelTest(String name) {
        super(name);
    }

    public void testGetCode() throws Exception {
        BaseDocument document = createJSPDocument("<style>h1 { background: <%=s %>;}</style>");
        CssTemplatedModel model = CssTemplatedModel.get(document);
        assertEquals("h1 { background: GENERATED_CODE;;}", model.getCode());

        document = createJSPDocument("<style>h1 { background: <%=s %>}</style>");
        model = CssTemplatedModel.get(document);
        assertEquals("h1 { background: GENERATED_CODE;}", model.getCode());

        document = createJSPDocument("<style>a {background: red } h1 { <%=s %>}</style>");
        model = CssTemplatedModel.get(document);
        assertEquals("a {background: red } h1 {                }", model.getCode());

        document = createJSPDocument("<style>a {background: red } <%=s %> { background: blue }</style>");
        model = CssTemplatedModel.get(document);
        assertEquals("a {background: red } GENERATED_CODEE { background: blue }", model.getCode());

        document = createJSPDocument("<style>a {background: red } <%=s %></style>");
        model = CssTemplatedModel.get(document);
        assertEquals("a {background: red } GENERATED_CODE;", model.getCode());

        document = createJSPDocument("<a style=\"color: red; background: <%=s %>\"/>");
        model = CssTemplatedModel.get(document);
        assertEquals("\n SELECTOR {\n\tcolor: red; background: GENERATED_CODE;\n}\n", model.getCode());

        document = createJSPDocument("<a style=\"color: red; <%=s %>\"/>");
        model = CssTemplatedModel.get(document);
        assertEquals("\n SELECTOR {\n\tcolor: red;                \n}\n", model.getCode());

        document = createJSPDocument("<a style=\"<%=s %>: red\"/>");
        model = CssTemplatedModel.get(document);
        assertEquals("\n SELECTOR {\n\tGENERATED_CODE : red\n}\n", model.getCode());

    }

    public static BaseDocument createJSPDocument(String s) throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/x-jsp");
        String contBefore = "<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>" +
                "<html><% String s = \"red\"; %>";
        String contAfter = "<h1><%=System.currentTimeMillis() %></h1></html>";
        doc.insertString(0, contBefore+s+contAfter, null);
        return doc;
    }

}

