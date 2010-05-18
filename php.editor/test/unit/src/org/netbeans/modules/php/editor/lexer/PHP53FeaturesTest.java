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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.lexer;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Pisl
 */
public class PHP53FeaturesTest extends PHPLexerTestBase {

    public PHP53FeaturesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGoto() throws Exception {
        performTest("jump01");
        performTest("jump02");
        performTest("jump03");
        performTest("jump04");
        performTest("jump05");
        performTest("jump06");
        performTest("jump07");
        performTest("jump08");
        performTest("jump09");
        performTest("jump10");
        performTest("jump11");
        performTest("jump12");
        performTest("jump13");
    }
    
    public void testNowDoc() throws Exception {
        performTest("nowdoc_000");
        performTest("nowdoc_001");
        performTest("nowdoc_002");
        performTest("nowdoc_003");
        performTest("nowdoc_004");
        performTest("nowdoc_005");
        performTest("nowdoc_006");
        performTest("nowdoc_007");
        performTest("nowdoc_008");
        performTest("nowdoc_009");
        performTest("nowdoc_010");
        performTest("nowdoc_011");
        performTest("nowdoc_012");
        performTest("nowdoc_013");
        performTest("nowdoc_014");
        performTest("nowdoc_015");
    }
    
    public void testHereDoc53() throws Exception {
        performTest("heredoc_001");
        performTest("heredoc_002");
        performTest("heredoc_003");
        performTest("heredoc_004");
        performTest("heredoc_005");
        performTest("heredoc_006");
        performTest("heredoc_007");
        performTest("heredoc_008");
        performTest("heredoc_009");
        performTest("heredoc_010");
        performTest("heredoc_011");
        performTest("heredoc_012");
        performTest("heredoc_013");
        performTest("heredoc_014");
    }
}
