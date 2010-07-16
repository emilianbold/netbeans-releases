/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.gsf;

import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;

/**
 *
 * @author marekfukala
 */
public class CssGSFParserTest extends TestBase {

    public CssGSFParserTest() {
        super(CssGSFParserTest.class.getName());
    }

    //issue #160780
    public void testFatalParserError() throws ParseException {
        //fatal parse error on such input, AST root == null
        String content = "@charset";
        Document doc = getDocument(content);
        Source source = Source.create(doc);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof CssParserResult);

        CssParserResult cssresult = (CssParserResult)result;
        assertNull(cssresult.root());
        assertEquals(1, cssresult.getDiagnostics().size());
    }


    public void testParserSuccess() throws ParseException {
        String content = "@charset \"iso-8859-1\";\n h1 { color: red; }";
        Document doc = getDocument(content);
        Source source = Source.create(doc);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof CssParserResult);

        CssParserResult cssresult = (CssParserResult)result;
        assertNotNull(cssresult.root());
        assertEquals(0, cssresult.getDiagnostics().size());
    }

    public void testParserError() throws ParseException {
        String content = "h1 { color: ;}";
        Document doc = getDocument(content);
        Source source = Source.create(doc);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof CssParserResult);

        CssParserResult cssresult = (CssParserResult)result;
        assertNotNull(cssresult.root());

        List<? extends Error> errors = cssresult.getDiagnostics();
        assertEquals(1, errors.size());
        
        assertEquals(Severity.ERROR, errors.get(0).getSeverity());
    }

    public void testParserWarning() throws ParseException {
        String content = "h1 { color: bracalova; }"; //invalid color
        Document doc = getDocument(content);
        Source source = Source.create(doc);
        final Result[] _result = new Result[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = resultIterator.getParserResult();
            }
        });

        Result result = _result[0];
        assertNotNull(result);
        assertTrue(result instanceof CssParserResult);

        CssParserResult cssresult = (CssParserResult)result;
        assertNotNull(cssresult.root());

        List<? extends Error> errors = cssresult.getDiagnostics();
        assertEquals(1, errors.size());

        assertEquals(Severity.WARNING, errors.get(0).getSeverity());
    }

}
