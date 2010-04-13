/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.parser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author marekfukala
 */
public class SimpleNodeUtilTest {

    public SimpleNodeUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getTrimmedNodeRange method, of class SimpleNodeUtil.
     */
    @Test
    public void testGetTrimmedNodeRange() {
        SimpleNode node = new SimpleNode(0);
        Token firstToken = new Token(CssParserConstants.IDENT);
        firstToken.offset = 10;
        firstToken.image = "  hello"; //2 spaces

        Token lastToken = new Token(CssParserConstants.IDENT);
        lastToken.offset = 10 + firstToken.image.length();
        lastToken.image = " world!   "; //3 spaces
        firstToken.next = lastToken;

        node.firstToken = firstToken;
        node.lastToken = lastToken;

        assertEquals(10, node.startOffset());
        assertEquals(10 + firstToken.image.length() + lastToken.image.length(), node.endOffset());
        assertEquals("  hello world!   ", node.image());

        OffsetRange result = SimpleNodeUtil.getTrimmedNodeRange(node);

        assertEquals(10 + 2, result.getStart());
        assertEquals(10 + firstToken.image.length() + lastToken.image.length() - 3, result.getEnd());
        
    }


}