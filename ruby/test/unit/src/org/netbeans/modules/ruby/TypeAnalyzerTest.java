/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
