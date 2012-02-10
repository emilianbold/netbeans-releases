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

import java.util.Collections;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeVisitor;

/**
 *
 * @author marekfukala
 */
public class CssTestBase extends CslTestBase {

    public CssTestBase(String testName) {
        super(testName);
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
        assertEquals(problems, result.getDiagnostics().size());

        return result;
    }

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
}
