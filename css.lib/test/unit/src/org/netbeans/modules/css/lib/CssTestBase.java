/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib;

import java.io.PrintWriter;
import java.util.*;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.PropertyModel;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.ValueGrammarElement;
import org.netbeans.modules.css.lib.properties.GrammarParser;
import org.netbeans.modules.css.lib.api.properties.GrammarResolver;

/**
 *
 * @author marekfukala
 */
public class CssTestBase extends CslTestBase {

    protected static boolean PRINT_GRAMMAR_RESOLVE_TIMES = false;
    protected static boolean PRINT_INFO_IN_ASSERT_RESOLVE = false;

    public CssTestBase(String testName) {
        super(testName);
    }
    
    protected Collection<GrammarResolver.Feature> getEnabledGrammarResolverFeatures() {
        return Collections.emptyList();
    }

    protected CssParserResult assertResultOK(CssParserResult result) {
        return assertResult(result, 0);
    }

    protected CssParserResult assertResult(CssParserResult result, int problems) {
        assertNotNull(result);
        assertNotNull(result.getParseTree());

        if (problems != result.getDiagnostics().size()) {
            TestUtil.dumpResult(result);
        }

        int foundProblemsCount = result.getDiagnostics().size();
        assertEquals(problems, foundProblemsCount);

        if (foundProblemsCount == 0) {
            //Check whether the parse tree covers the whole file only if it is not broken. 
            //This doesn't mean an errorneous file should not produce parse tree
            //fully covering the source. Just there're some cases where it doesn't work now.
            //TODO: enable the parse tree tokens consistency check for all parse result, not just for the errorneous ones.
            assertNoTokenNodeLost(result);
        }

        return result;
    }

    /**
     * Checks whether the parser result covers every character in the source
     * code. In another words ensure there are no lexer tokens which doesn't
     * have a corresponding parse tree token node.
     */
    protected void assertNoTokenNodeLost(CssParserResult result) {
        final StringBuilder sourceCopy = new StringBuilder(result.getSnapshot().getText());

        NodeVisitor.visitChildren(result.getParseTree(), Collections.<NodeVisitor<Node>>singleton(new NodeVisitor<Node>() {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.token) {
                    for (int i = node.from(); i < node.to(); i++) {
                        sourceCopy.setCharAt(i, Character.MAX_VALUE);
                    }
                }

                return false;
            }
        }));

        for (int i = 0; i < sourceCopy.length(); i++) {
            if (sourceCopy.charAt(i) != Character.MAX_VALUE) {
                assertTrue(String.format("No token node found for char '%s' at offset %s of the parser source.", sourceCopy.charAt(i), i), false);
            }
        }
    }

    protected ResolvedProperty assertResolve(PropertyModel propertyModel, String inputText) {
        return assertResolve(propertyModel, inputText, true);
    }
    
    protected ResolvedProperty assertResolve(PropertyModel propertyModel, String inputText, boolean expectedSuccess) {
        return assertResolve(propertyModel.getGrammarElement(), inputText, expectedSuccess);
    }
    
    protected ResolvedProperty assertResolve(String grammar, String inputText) {
        return assertResolve(grammar, inputText, true);
    }

    protected ResolvedProperty assertNotResolve(String grammar, String inputText) {
        return assertResolve(grammar, inputText, false);
    }

    protected ResolvedProperty assertResolve(String grammar, String inputText, boolean expectedSuccess) {
        long a = System.currentTimeMillis();
        GroupGrammarElement tree = GrammarParser.parse(grammar);
        long b = System.currentTimeMillis();
        return assertResolve(tree, inputText, expectedSuccess);
    }

    protected ResolvedProperty assertResolve(GroupGrammarElement tree, String inputText) {
        return assertResolve(tree, inputText, true);
    }

    protected ResolvedProperty assertResolve(GroupGrammarElement tree, String inputText, boolean expectedSuccess) {

        long a = System.currentTimeMillis();
        
        ResolvedProperty pv = new ResolvedProperty(createGrammarResolver(tree), inputText);
        long c = System.currentTimeMillis();

        if (PRINT_INFO_IN_ASSERT_RESOLVE) {
            System.out.println("Tokens:");
            System.out.println(dumpList(pv.getTokens()));
            System.out.println("Grammar:");
            System.out.println(tree.toString2(0));
        }
        if (PRINT_GRAMMAR_RESOLVE_TIMES) {
            System.out.println(String.format("Input '%s' resolved in %s ms.", inputText, c - a));
        }
        if (pv.isResolved() != expectedSuccess) {
            assertTrue("Unexpected parsing result", false);
        }

        return pv;
    }
    
    private GrammarResolver createGrammarResolver(GroupGrammarElement tree) {
        GrammarResolver grammarResolver = new GrammarResolver(tree);
        for(GrammarResolver.Feature feature : getEnabledGrammarResolverFeatures()) {
            grammarResolver.enableFeature(feature);
        }
        return grammarResolver;
    }

    protected void assertParseFails(String grammar, String inputText) {
        assertResolve(grammar, inputText, false);
    }

    protected void assertAlternatives(ResolvedProperty propertyValue, String... expected) {
        Set<ValueGrammarElement> alternatives = propertyValue.getAlternatives();
        Collection<String> alts = convert(alternatives);
        Collection<String> expc = new ArrayList<String>(Arrays.asList(expected));
        if (alts.size() > expc.size()) {
            alts.removeAll(expc);
            throw new AssertionFailedError(String.format("Found %s unexpected alternative(s): %s", alts.size(), toString(alts)));
        } else if (alts.size() < expc.size()) {
            expc.removeAll(alts);
            throw new AssertionFailedError(String.format("There're %s expected alternative(s) missing : %s", expc.size(), toString(expc)));
        } else {
            Collection<String> alts2 = new ArrayList<String>(alts);
            Collection<String> expc2 = new ArrayList<String>(expc);

            alts2.removeAll(expc);
            expc2.removeAll(alts);

            assertTrue(String.format("Missing expected: %s; Unexpected: %s", toString(expc2), toString(alts2)), alts2.isEmpty() && expc2.isEmpty());

        }
    }

    protected void assertAlternatives(String grammar, String input, String... expected) {
        GroupGrammarElement tree = GrammarParser.parse(grammar);
        GrammarResolver grammarResolver = createGrammarResolver(tree);
        ResolvedProperty pv = new ResolvedProperty(grammarResolver, input);
        assertAlternatives(pv, expected);
    }

    private Collection<String> convert(Set<ValueGrammarElement> toto) {
        Collection<String> x = new HashSet<String>();
        for (ValueGrammarElement e : toto) {
            String strVal = (e.isUnit() ? "!" : "") + e.value();
            x.add(strVal);
        }
        return x;
    }

    private String toString(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i = c.iterator(); i.hasNext();) {
            sb.append('"');
            sb.append(i.next());
            sb.append('"');
            if (i.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    protected String dumpList(Collection<?> col) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> itr = col.iterator(); itr.hasNext();) {
            sb.append('"');
            sb.append(itr.next());
            sb.append('"');
            if (itr.hasNext()) {
                sb.append(',');
                sb.append(' ');
            }
        }
        return sb.toString();
    }
    
     protected void dumpTree(org.netbeans.modules.css.lib.api.properties.Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dump(node, 0, pw);
        pw.flush();
    }

    private void dump(org.netbeans.modules.css.lib.api.properties.Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print("    ");
        }
        pw.print(tree.toString());
        pw.println();
        for (org.netbeans.modules.css.lib.api.properties.Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }
}
