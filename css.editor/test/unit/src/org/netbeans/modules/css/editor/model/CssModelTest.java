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
package org.netbeans.modules.css.editor.model;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;

/**
 *
 * @author marekfukala
 */
public class CssModelTest extends TestBase {

    public CssModelTest() {
        super(CssModelTest.class.getName());
    }

    public void testBasis() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "h1 { color: red; }";
        //                0123456789012345678
        //                0         1

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
        assertTrue(result instanceof CssParserResult);
        assertNotNull(result);

        CssModel model = CssModel.create((CssParserResult) result);
        assertNotNull(model);

        List<CssRule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(1, rules.size());

        CssRule rule = rules.get(0);
        assertNotNull(rule);
        assertEquals("h1", rule.name());
        assertEquals(3, rule.getRuleOpenBracketOffset());
        assertEquals(17, rule.getRuleCloseBracketOffset());

        List<CssRuleItem> items = rule.items();
        assertNotNull(items);
        assertEquals(1, items.size());

        CssRuleItem item = rule.items().get(0);
        assertNotNull(item);

        CssRuleItem.Item property = item.key();
        assertNotNull(property);
        assertEquals("color", property.name());
        assertEquals(5, property.offset());

        CssRuleItem.Item value = item.value();
        assertNotNull(value);
        assertEquals("red", value.name());
        assertEquals(12, value.offset());

    }

    public void testWsAfterPropertyName() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "h1 { color : red; }";
        //                0123456789012345678
        //                0         1

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
        assertTrue(result instanceof CssParserResult);
        assertNotNull(result);

        CssModel model = CssModel.create((CssParserResult) result);
        assertNotNull(model);

        List<CssRule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(1, rules.size());


        CssRuleItem item = rules.get(0).items().get(0);
        assertNotNull(item);

        CssRuleItem.Item property = item.key();
        assertNotNull(property);

        //whitespace between the property name and the semicolon must not be present
        //in the property name
        assertEquals("color", property.name());
        assertEquals(5, property.offset());

    }

    public void testSeverelyBrokenSource() throws org.netbeans.modules.parsing.spi.ParseException, BadLocationException {
        String content = "@ ";
        //                0123456789012345678
        //                0         1

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
        assertTrue(result instanceof CssParserResult);
        assertNotNull(result);

        CssModel model = CssModel.create((CssParserResult) result);
        assertNotNull(model);

        List<CssRule> rules = model.rules();
        assertNotNull(rules);
        assertEquals(0, rules.size()); //no rules
    }

}