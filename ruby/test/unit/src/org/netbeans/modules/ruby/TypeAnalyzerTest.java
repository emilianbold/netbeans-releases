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

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;

/**
 * @todo Test compound assignment:  x = File::Stat.new
 *
 * @author Tor Norbye
 */
public class TypeAnalyzerTest extends RubyTestBase {
    
    public TypeAnalyzerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetType() {
        Node root = getRootNode("testfiles/types.rb");
        TypeAnalyzer instance = new TypeAnalyzer(root, null, 794, 794, null, null);
        assertEquals("Integer", instance.getType("x"));
        // y is reassigned later in the file - make sure that at this
        // point in scope we have the right type
        assertEquals("File", instance.getType("y"));
        assertEquals("Hash", instance.getType("$baz"));
        assertEquals("Fixnum", instance.getType("@bar"));
        assertEquals("Array", instance.getType("@foo"));
    }

    public void testGetType2() {
        Node root = getRootNode("testfiles/types.rb");
        TypeAnalyzer instance = new TypeAnalyzer(root, null, 974, 974, null, null);
        // Y is assigned different types - make sure that at this position, it's a number
        assertEquals("Fixnum", instance.getType("y"));
        // Lots of reassignments - track types through vars, statics, fields, classvars
        assertEquals("Hash", instance.getType("loc"));
        assertEquals("Hash", instance.getType("$glob"));
        assertEquals("Hash", instance.getType("@field"));
        assertEquals("Hash", instance.getType("@@clsvar"));
        assertEquals("Hash", instance.getType("loc2"));
    }
 
    public void testTypeAssertions() {
        String file = "testfiles/types.rb";
        FileObject fileObject = getTestFile(file);
        BaseDocument doc = getDocument(fileObject);

        Node root = getRootNode(file);
        int offset = 794;
        MethodDefNode method = AstUtilities.findMethodAtOffset(root, offset);
        assertNotNull(method);
        TypeAnalyzer instance = new TypeAnalyzer(method, null, offset, offset, doc, fileObject);
        assertEquals("String", instance.getType("param1"));
        assertEquals("Hash", instance.getType("param2"));
    }

    public void testBegin() {
        String file = "testfiles/types2.rb";
        FileObject fileObject = getTestFile(file);
        BaseDocument doc = getDocument(fileObject);
        Node root = getRootNode(file);
        int pos = 3000;
        MethodDefNode method = AstUtilities.findMethodAtOffset(root, pos);
        assertNotNull(method);
        AstPath path = new AstPath(root, pos);
        Node node = path.leaf();
        TypeAnalyzer instance = new TypeAnalyzer(method, node, pos, pos, doc, fileObject);
        assertEquals("GetoptLong", instance.getType("go"));
    }

    public void testRailsController() {
        String file = "testfiles/type_controller.rb";
        FileObject fo = getTestFile(file);
        Node root = getRootNode(file);
        BaseDocument doc = getDocument(fo);
        int pos = 46;
        AstPath path = new AstPath(root, pos);
        Node node = path.leaf();
        TypeAnalyzer instance = new TypeAnalyzer(root, node, pos, pos, doc, fo);
        assertEquals("ActionController::CgiRequest", instance.getType("request"));
    }
}
