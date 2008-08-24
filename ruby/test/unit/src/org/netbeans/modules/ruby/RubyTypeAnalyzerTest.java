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

package org.netbeans.modules.ruby;

import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.openide.filesystems.FileObject;

/**
 * @todo Test compound assignment:  x = File::Stat.new
 *
 * @author Tor Norbye
 */
public class RubyTypeAnalyzerTest extends RubyTestBase {
    
    public RubyTypeAnalyzerTest(String testName) {
        super(testName);
    }

    private RubyTypeAnalyzer getAnalyzer(String file, String caretLine, boolean findMethod) throws Exception {
        FileObject fo = getTestFile(file);
        BaseDocument doc = getDocument(fo);
        GsfTestCompilationInfo info = getInfo(fo);
        Node root = AstUtilities.getRoot(info);
        initializeRegistry();
        RubyIndex index = RubyIndex.get(info.getIndex(RubyMimeResolver.RUBY_MIME_TYPE));

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = info.getText().indexOf(caretLine);
            assertTrue(lineOffset != -1);
            caretOffset = lineOffset + caretDelta;
        }
        
        AstPath path = new AstPath(root, caretOffset);
        Node node = path.leaf();
        
        if (findMethod) {
            MethodDefNode method = AstUtilities.findMethodAtOffset(root, caretOffset);
            assertNotNull(method);
            
            root = method;
        }
        
        RubyTypeAnalyzer instance = new RubyTypeAnalyzer(index, root, node, caretOffset, caretOffset, doc, fo);

        return instance;
    }
    
    public void testGetType() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("testfiles/types.rb", " l^oc = {", false);

        assertEquals("Integer", instance.getType("x"));
        // y is reassigned later in the file - make sure that at this
        // point in scope we have the right type
        assertEquals("File", instance.getType("y"));
        assertEquals("Hash", instance.getType("$baz"));
        assertEquals("Fixnum", instance.getType("@bar"));
        assertEquals("Array", instance.getType("@foo"));
    }

    public void testGetType2() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("testfiles/types.rb", " # d^one", false);

        // Y is assigned different types - make sure that at this position, it's a number
        assertEquals("Fixnum", instance.getType("y"));
        // Lots of reassignments - track types through vars, statics, fields, classvars
        assertEquals("Hash", instance.getType("loc"));
        assertEquals("Hash", instance.getType("$glob"));
        assertEquals("Hash", instance.getType("@field"));
        assertEquals("Hash", instance.getType("@@clsvar"));
        assertEquals("Hash", instance.getType("loc2"));
    }
 
    public void testTypeAssertions() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("testfiles/types.rb", " l^oc = {", true);
        assertEquals("String", instance.getType("param1"));
        assertEquals("Hash", instance.getType("param2"));
    }

    public void testBegin() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("testfiles/types2.rb", " @f^iles = ARGV.dup", true);
        assertEquals("GetoptLong", instance.getType("go"));
    }

    public void testRailsController() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("testfiles/type_controller.rb", "^end", false);
        assertEquals("ActionController::CgiRequest", instance.getType("request"));
    }

// This test doesn't work; the behavior works in the IDE but the
// Lucene index isn't returning local symbols in the testing framework yet    
//    public void testComplex1() throws Exception {
//        RubyTypeAnalyzer instance = getAnalyzer("testfiles/types3.rb", "^caret", false);
//        assertEquals("Product", instance.getType("@product"));
//    }

//    public void testComplex2() throws Exception {
//        RubyTypeAnalyzer instance = getAnalyzer("testfiles/types3.rb", "^caret", true);
//        assertEquals("ActiveRecord::ConnectionAdapters::TableDefinition", instance.getType("t"));
//    }

    //public void testComplex3() throws Exception {
    //    // XXX TODO 
    //    assertFalse("Check that I do closures for each, collect, map, etc.", true);
    //    // also check to_s
    //}
    
    // TODO: Make sure I can handle compound expressions like this one:
    //  Product.find(params[:id]).destroy

    public void testMigrationType() throws Exception {
        RubyTypeAnalyzer instance = getAnalyzer("testfiles/migrate/20080726182725_create_posts.rb", " t.^time", true);

        assertEquals("ActiveRecord::ConnectionAdapters::TableDefinition", instance.getType("t"));
    }
}
