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

package org.netbeans.modules.html.parser;

import java.util.ArrayList;
import java.util.List;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.StackNode;
import nu.validator.htmlparser.impl.StateSnapshot;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzer;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class ReinstatingTreeBuilderTest extends NbTestCase {

    public ReinstatingTreeBuilderTest(String name) {
        super(name);
    }

    public void testCanFollow() throws ParseException {
        String code = "<!doctype html><body><table><tr></table></body>";
        HtmlParseResult result = parse(code);
        assertNotNull(result);
        assertNotNull(result.root());

        AstNodeUtils.dumpTree(result.root());

        AstNode tr = AstNodeUtils.query(result.root(), "html/body/table/tbody/tr");
        assertNotNull(tr);

        List<StackNode> stack = makeStack(tr);

        System.out.println("mode=" + tr.treeBuilderState);

        StateSnapshot snapshot = Html5Parser.makeTreeBuilderSnapshot(tr);
        ReinstatingTreeBuilder tb = new ReinstatingTreeBuilder(snapshot);

        assertCanFollow(false, tr, ElementName.P, tb);
        
        assertCanFollow(true, tr, ElementName.TD, tb);
        assertCanFollow(false, tr, ElementName.TABLE, tb);

        assertCanFollow(false, tr, ElementName.DIV, tb);
        assertCanFollow(false, tr, ElementName.BODY, tb);

        assertCanFollow(false, tr, ElementName.TSPAN, tb);

        //----------------------

        AstNode body = AstNodeUtils.query(result.root(), "html/body");
        assertNotNull(body);
        

        snapshot = Html5Parser.makeTreeBuilderSnapshot(body);
        tb = new ReinstatingTreeBuilder(snapshot);

        assertCanFollow(true, body, ElementName.TSPAN, tb);
        
    }

    private List<StackNode> makeStack(AstNode n) {
        List<StackNode> stack = new ArrayList<StackNode>();
        AstNode node = n;
        System.out.println("stack:");
        do {
            stack.add(0, new StackNode("http://www.w3.org/1999/xhtml", (ElementName)node.elementName, node));
            System.out.println(node);
        } while((node = node.parent()) != null && !node.isRootNode());
   
        return stack;
    }

    private void assertCanFollow(boolean expected, AstNode node, ElementName element, ReinstatingTreeBuilder builder) {
        boolean canFollow =  builder.canFollow(node, element);
        assertEquals(builder.errorOrFatalError, expected, canFollow);
    }


      private HtmlParseResult parse(CharSequence code) throws ParseException {
        HtmlSource source = new HtmlSource(code);
        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();

        assertNotNull(result);

        return result;
    }

}