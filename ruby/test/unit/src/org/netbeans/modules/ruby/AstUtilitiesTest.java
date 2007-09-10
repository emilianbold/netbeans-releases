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

import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;

/**
 *
 * @todo Lots of other methods to test!
 *  
 * @author Tor Norbye
 */
public class AstUtilitiesTest extends RubyTestBase {
    
    public AstUtilitiesTest(String testName) {
        super(testName);
    }

    public void testFindbySignature1() throws Exception {
        // Test top level methods
        Node root = getRootNode("testfiles/top_level.rb");
        Node node = AstUtilities.findBySignature(root, "Object#bar(baz)");
        assertNotNull(node);
        assertEquals("bar", ((INameNode)node).getName());
    }

    public void testFindbySignature2() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#test_sorting(coll)");
        assertNotNull(node);
        assertEquals("test_sorting", ((INameNode)node).getName());
    }

    public void testFindbySignatureInstance() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#@dialogs");
        assertNotNull(node);
        assertEquals(node.nodeId, NodeTypes.INSTASGNNODE);
        assertEquals("@dialogs", ((INameNode)node).getName());
    }

    public void testFindbySignatureClassVar() throws Exception {
        Node root = getRootNode("testfiles/ape.rb");
        Node node = AstUtilities.findBySignature(root, "Ape#@@debugging");
        assertNotNull(node);
        assertEquals(node.nodeId, NodeTypes.CLASSVARASGNNODE);
        assertEquals("@@debugging", ((INameNode)node).getName());
    }
}
