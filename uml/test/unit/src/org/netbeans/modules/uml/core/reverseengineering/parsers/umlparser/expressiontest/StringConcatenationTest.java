package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.expressiontest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;


/**
 * @author aztec
 */
public class StringConcatenationTest extends AbstractUmlParserTestCase {
    public static void main(String[] args) {
        TestRunner.run(StringConcatenationTest.class);
    }
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testStringConcatenation() {
        execute(getClass().getSimpleName());
    }
}
