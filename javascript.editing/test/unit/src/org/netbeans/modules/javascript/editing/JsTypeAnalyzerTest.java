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

package org.netbeans.modules.javascript.editing;

import java.util.Collections;
import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 * @todo Test compound assignment:  x = File::Stat.new
 *
 * @author Tor Norbye
 */
public class JsTypeAnalyzerTest extends JsTestBase {

    public JsTypeAnalyzerTest(String testName) {
        super(testName);
    }

    private JsTypeAnalyzer getAnalyzer(String file, final String caretLine, final boolean findMethod) throws Exception {
        FileObject fo = getTestFile(file);
        Source source = Source.create(fo);
        
        RepositoryUpdater.getDefault().start(true);
        
        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(source.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(source, caretOffset);
        } else {
            caretOffset = -1;
        }

        indexFile(file);

        final JsTypeAnalyzer [] result = new JsTypeAnalyzer [] { null };
        ParserManager.parseWhenScanFinished(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                JsParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);

                Node root = jspr.getRootNode();
                initializeRegistry();
                JsIndex index = JsIndex.get(Collections.singleton(r.getSnapshot().getSource().getFileObject().getParent()));

                AstPath path = new AstPath(root, caretOffset);
                Node node = path.leaf();

                if (findMethod) {
                    Node method = AstUtilities.findMethodAtOffset(root, caretOffset);
                    assertNotNull(method);

                    root = method;
                }

                result[0] = new JsTypeAnalyzer(jspr, index, root, node, caretOffset, caretOffset);
            }
        });

        return result[0];
    }

    public void testGetType() throws Exception {
        JsTypeAnalyzer instance = getAnalyzer("testfiles/types1.js", "// E^ND", false);

        assertNotNull(instance);
        assertEquals("String", instance.getType("a"));
        assertEquals("Number", instance.getType("b"));
        assertEquals("Number", instance.getType("c"));
        assertEquals("String", instance.getType("d"));
        assertEquals("RegExp", instance.getType("e"));
        assertEquals("Function", instance.getType("f"));
        assertEquals("String", instance.getType("g"));
        assertEquals("RegExp", instance.getType("h"));
        assertEquals("MyObj", instance.getType("i"));
        assertEquals("MyObj", instance.getType("j"));
        assertEquals("String", instance.getType("k"));
        assertNull(instance.getType("l"));
        assertEquals("Boolean", instance.getType("m"));
        assertEquals("Boolean", instance.getType("n"));
        assertEquals("Spry.Effect.Foo", instance.getType("o"));
        assertEquals("Spry.Effect.AnimatedElement", instance.getType("p"));
    }

    public void testGetType2() throws Exception {
        JsTypeAnalyzer instance = getAnalyzer("testfiles/types2.js", "// I^nitial", true);

        assertNotNull(instance);
        assertEquals("Mixed", instance.getType("el"));
        assertEquals("Object|Array", instance.getType("values"));
        assertEquals("Boolean", instance.getType("returnElement"));
        assertEquals(null, instance.getType("unknown"));
    }

//    public void testReturnTypes() throws Exception {
//        JsTypeAnalyzer instance = getAnalyzer("testfiles/types2.js", "alert(^mycall);", true);
//
//        assertEquals("HTMLElement|Axt.Element", instance.getType("call"));
//    }
//    
    //public void testGetReturnType1() throws Exception {
    //    JsTypeAnalyzer instance = getAnalyzer("testfiles/types2.js", "^alert(mycall)", true);
    //
    //    assertEquals("HTMLElement|Axt.Element", instance.getType("mycall"));
    //}
    
    // TODO - test intermediate state, e.g. asking for the value of k in the middle of the file before
    // reassignment should yield earlier value
}
