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
package org.netbeans.modules.web.jsf.editor.el;

import com.sun.el.lang.EvaluationContext;
import java.beans.FeatureDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.swing.SwingUtilities;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsf.editor.JsfHtmlExtension;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import javax.el.ELException;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.web.jsf.editor.TestBase;
import static org.junit.Assert.*;

/**
 * Currently no real tests but temporary test code just to help
 * understanding and debug the EL parser.
 */
public class JsfElParserTest extends TestBase {

    private static NodeVisitor printing;

    public JsfElParserTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        HtmlExtension.register("text/xhtml", new JsfHtmlExtension()); //NOI18N

        printing = new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                System.out.println("Node: " + node + ", image: " + node.getImage() + ", class:" + node.getClass().getName());
            }
        };
    }

    @Test
    public void testParseDeferred() {
        String expr = "#{taskController.removeTask(cc.attrs.story, cc.attrs.task)}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseDeferred2() {
        String expr = "#{customer.name}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseImmediate() {
        String expr = "${sessionScope.cart.numberOfItems > 0}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseImmediate2() {
        String expr = "{sessionScope.cart.total}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseImmediate3() {
        String expr = "${customer.name}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseImmediate4() {
        String expr = "${customer}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseImmediate5() {
        String expr = "${customer.address[\"street\"]}";
        Node result = JsfElParser.parse(expr);
        assertNotNull(result);
        print(expr, result);
    }

    @Test
    public void testParseInvalid() {
        String expr = "${customer.ad";
        try {
            JsfElParser.parse(expr);
            fail("Should not parse: " + expr);
        } catch (ELException ele) {
            System.out.println("ELE: " + ele.getCause());
            assertTrue(true);
        }
    }

    @Test
    public void testParseResult() throws Exception {
        FileObject file = getTestFile("testfiles/home.xhtml");
        initEL(file);
        assertNotNull(file);
        Document doc = getDefaultDocument(file);
        ELParserResult parserResult = JsfElParser.create(doc).parse();

        assertNotNull(parserResult);
        assertTrue(parserResult.isValid());

        List<ELElement> elements = parserResult.getElements();
        assertEquals(11, elements.size());
        
        for (ELElement each : elements) {
            assertTrue(each.isValid());
        }

        // check order
        assertEquals("#{game.smallest}", elements.get(0).getExpression());
        assertEquals("#{game.number eq game.guess}", elements.get(4).getExpression());
        assertEquals("#{game.number gt game.guess and game.guess ne 0}", elements.get(9).getExpression());
        assertEquals("#{game.number lt game.guess and game.guess ne 0}", elements.get(10).getExpression());
    }

    @Test
    public void testParseResultErrors() throws Exception {
        FileObject file = getTestFile("testfiles/home_with_errors.xhtml");
        initEL(file);
        assertNotNull(file);
        Document doc = getDefaultDocument(file);
        ELParserResult parserResult = JsfElParser.create(doc).parse();

        assertNotNull(parserResult);
        assertFalse(parserResult.isValid());

        List<ELElement> elements = parserResult.getElements();
        assertEquals(11, elements.size());

        // check elements with errors
        assertFalse(elements.get(2).isValid());
        assertNotNull(elements.get(2).getError());
        assertFalse(elements.get(4).isValid());
        assertNotNull(elements.get(4).getError());

        // check valid elems
        assertTrue(elements.get(0).isValid());
        assertNull(elements.get(0).getError());
        assertTrue(elements.get(9).isValid());
        assertNull(elements.get(9).getError());
    }

    static void print(String expr, Node node) {
        System.out.println("------------------------------");
        System.out.println("AST for " + expr);
        System.out.println("------------------------------");
        printTree(node, 0);
    }

    private static void printTree(Node node, int level) {
        StringBuilder indent = new StringBuilder(level);
        for (int i = 0; i < level; i++) {
            indent.append(" ");
        }
        System.out.println(indent.toString() + node + ", offset: start - " + node.startOffset()  + " end - " + node.endOffset() + ", image: " + node.getImage() + ", class: " + node.getClass().getSimpleName());
        try {
            System.out.println("Type: " + node.getType(getEvaluationContext()) );
        } catch (UnsupportedOperationException ue) {
            System.out.println("Type unresolvable");
        }
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            printTree(child, ++level);
        }
    }

    private void initEL(FileObject file) throws ParseException {
        final HtmlExtension hext = new JsfHtmlExtension();

        //init the EL embedding
        Source source = Source.create(file);
        ParserManager.parse(Collections.singletonList(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult result = (HtmlParserResult) WebUtils.getResultIterator(resultIterator, "text/html").getParserResult();

                //enable EL
                ((JsfHtmlExtension) hext).checkELEnabled(result);
                //block until the recolor AWT task finishes
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        //no-op
                    }
                });
            }
        });

    }

    private static EvaluationContext getEvaluationContext() {

        final FunctionMapper fm = new FuncMapper();
        final VariableMapper vm = new VarMapper();
        ELContext elc = new ELContext() {

            @Override
            public ELResolver getELResolver() {
                return new ELResolver() {

                    @Override
                    public Object getValue(ELContext elc, Object o, Object o1) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Class<?> getType(ELContext elc, Object o, Object o1) {
                        return "".getClass();
                    }

                    @Override
                    public void setValue(ELContext elc, Object o, Object o1, Object o2) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public boolean isReadOnly(ELContext elc, Object o, Object o1) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elc, Object o) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Class<?> getCommonPropertyType(ELContext elc, Object o) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                };
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return fm;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return vm;
            }

            @Override
            public boolean isPropertyResolved() {
                return true;
            }


        };

        EvaluationContext ec = new EvaluationContext(elc, fm, vm);
        return ec;
    }

    private static class FuncMapper extends FunctionMapper {

        @Override
        public Method resolveFunction(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private static class VarMapper extends VariableMapper {

        @Override
        public ValueExpression resolveVariable(String string) {
            System.out.println("string: " + string);
            return new ValueExpression() {

                @Override
                public Object getValue(ELContext elc) {
                    return "unknown";
                }

                @Override
                public void setValue(ELContext elc, Object o) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isReadOnly(ELContext elc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Class<?> getType(ELContext elc) {
                    return "".getClass();
                }

                @Override
                public Class<?> getExpectedType() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public String getExpressionString() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean equals(Object o) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int hashCode() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isLiteralText() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }

        @Override
        public ValueExpression setVariable(String string, ValueExpression ve) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
