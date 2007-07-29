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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

/**
 * Test the ExpandSameLineDef hint
 * 
 * @author Tor Norbye
 */
public class ExpandSameLineDefTest extends HintTestBase {

    public ExpandSameLineDefTest(String testName) {
        super(testName);
    }

//    // Not working yet
//    public void testRegistered() throws Exception {
//        ensureRegistered(new ExpandSameLineDef());
//    }
    
    public void testWrongLine() throws Exception {
        String caretLine = "cl^ass FooControllerTest < Test::Unit::TestCase";
        findHints(this, new ExpandSameLineDef(), "testfiles/sameline.rb", caretLine);
    }

    public void testExpandableLine() throws Exception {
        String caretLine = "cla^ss FooController; def rescue_action(e) raise e end; end";
        findHints(this, new ExpandSameLineDef(), "testfiles/sameline.rb", caretLine);
    }
    
    public void testApplyFix() throws Exception {
        String caretLine = "cla^ss FooController; def rescue_action(e) raise e end; end";
        applyHint(this, new ExpandSameLineDef(), "testfiles/sameline.rb", caretLine, "class");
    }
}
