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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.visual.ui.preview;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.lib.api.model.Stylesheet;
import org.netbeans.modules.css.lib.api.model.Rule;
import org.netbeans.modules.css.visual.CssRuleContent;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class CssPreviewGeneratorTest extends CslTestBase {

    public CssPreviewGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new CssLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/x-css";
    }
    
    public void testCleanPseudoClass() throws ParseException, IOException {
       Stylesheet model = modelFor(":focus { }");
       Rule rule = rule(model, 0);
       CssRuleContext context = context(model, rule);
       String preview = CssPreviewGenerator.getPreviewCode(context).toString();

       assertEqualsIgnoreWS("<html><head><style type=\"text/css\">Xfocus {}</style>"
               + "</head><body><div><Xfocus>Sample Text</Xfocus></div></body></html>", preview);

    }

    public void testPseudoClass() throws ParseException, IOException {
       Stylesheet model = modelFor("a:focus { }");
       Rule rule = rule(model, 0);
       CssRuleContext context = context(model, rule);

       String preview = CssPreviewGenerator.getPreviewCode(context).toString();
       assertEqualsIgnoreWS("<html><head><style type=\"text/css\">aXfocus {}</style>"
               + "</head><body><a><aXfocus>Sample Text</aXfocus></a></body></html>", preview);
    }

    private CssRuleContext context(Stylesheet model, Rule rule) {
        return new CssRuleContext(CssRuleContent.create(rule), model, null, null);
    }

    private Rule rule(Stylesheet model, int index) {
        List<Rule> rules = model.rules();
        assertNotNull(rules);
        assertTrue(String.format("No rule for index %s, there's only %s rules", index, rules.size()),
                rules.size() > index);
        Rule rule = rules.get(index);
        assertNotNull(rule);
        return rule;
    }

    private Stylesheet modelFor(String code) throws ParseException, IOException {
        FileObject fo = createTempFile("test.css", code);
        Document doc = getDocument(fo);
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
        assertTrue(result instanceof CssCslParserResult);

        Stylesheet model = Stylesheet.create(((CssCslParserResult) result).getWrappedCssParserResult());
        assertNotNull(model);

        return model;
    }

    private FileObject createTempFile(String path, String content) throws IOException {
        createFile(FileUtil.toFileObject(getWorkDir()), path, content);

        File wholeInputFile = new File(getWorkDir(), path);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    private void assertEqualsIgnoreWS(String expected, String tested) {
        String _expe = expected.replaceAll("\\s*", "");
        String _test = tested.replaceAll("\\s*", "");
        assertEquals(_expe, _test);
    }
}