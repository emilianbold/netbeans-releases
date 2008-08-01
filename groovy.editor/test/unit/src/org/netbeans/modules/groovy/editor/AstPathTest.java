/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor;

import java.util.Iterator;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.gsf.api.CompilationInfo;

/**
 *
 * @author Martin Adamek
 */
public class AstPathTest extends GroovyTestBase {

    public AstPathTest(String testName) {
        super(testName);
    }

    public void testMiniClass() throws Exception {
        Iterator<ASTNode> it = getPath("testfiles/Hello.groovy", "class FourthClass {^}").iterator();
        assertEquals(ClassNode.class, it.next().getClass());
        assertEquals(ModuleNode.class, it.next().getClass());
        assertFalse(it.hasNext());
    }

    public void testMiniClass2() throws Exception {
        Iterator<ASTNode> it = getPath("testfiles/MiniClass.groovy", "class MiniClass {^}").iterator();
        assertEquals(ClassNode.class, it.next().getClass());
        assertEquals(ModuleNode.class, it.next().getClass());
        assertFalse(it.hasNext());
    }

// Disable this Test till it's fixed.
//    public void testMiniClass3() throws Exception {
//        Iterator<ASTNode> it = getPath("testfiles/MiniClass2.groovy", "class MiniClass2 { Cl^ }").iterator();
//        assertEquals(ClassNode.class, it.next().getClass());
//        assertEquals(ModuleNode.class, it.next().getClass());
//        assertFalse(it.hasNext());
//    }

    public void testScript() throws Exception {
        Iterator<ASTNode> it = getPath("testfiles/GroovyScopeTestcase.groovy", "pri^ntln \"Starting testcase\"").iterator();
        assertEquals(ConstantExpression.class, it.next().getClass());
        assertEquals(MethodCallExpression.class, it.next().getClass());
        assertEquals(ExpressionStatement.class, it.next().getClass());
        assertEquals(BlockStatement.class, it.next().getClass());
        assertEquals(ClassNode.class, it.next().getClass());
        assertEquals(ModuleNode.class, it.next().getClass());
        assertFalse(it.hasNext());
    }

    private AstPath getPath(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        GroovyParserResult result = (GroovyParserResult) info.getEmbeddedResult(GroovyTokenId.GROOVY_MIME_TYPE, 0);
        ModuleNode moduleNode = result.getRootElement().getModuleNode();
        return new AstPath(moduleNode, caretOffset, (BaseDocument) info.getDocument());
    }
    
}
