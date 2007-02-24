package org.netbeans.modules.uml.parser.java.expressiontest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ArithmaticExpressionTest extends AbstractParserTestCase {
    
    final String fileName = "ArithmaticExpressionTestFile.java";
    
    // These are the expected states for the above mentioned file
    
    final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Assignment Expression","Identifier","Assignment Expression","Identifier","Assignment Expression","Identifier","Increment Post Unary Expression","Identifier","Increment Unary Expression","Identifier","Variable Definition","Modifiers","Type","Initializer","Plus Assignment Expression","Identifier","Plus Expression","Plus Expression","Increment Post Unary Expression","Identifier","Identifier","Identifier","Variable Definition","Modifiers","Type","Initializer","Divide Expression","Identifier","Identifier"};
    
    // These are the expected tokens for the above mentioned file
    
    final String[][] expectedTokens ={ {"ArithmaticExpressionTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"int",  "Primitive Type"} ,{"k",  "Name"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"=",  "Operator"} ,{"j",  "Identifier"} ,{"=",  "Operator"} ,{"k",  "Identifier"} ,{"2",  "Integer Constant"} ,{"++",  "Operator"} ,{"i",  "Identifier"} ,{"++",  "Operator"} ,{"k",  "Identifier"} ,{"int",  "Primitive Type"} ,{"shortcutOp",  "Name"} ,{"10",  "Integer Constant"} ,{"+=",  "Operator"} ,{"shortcutOp",  "Identifier"} ,{"+",  "Operator"} ,{"+",  "Operator"} ,{"++",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{"k",  "Identifier"} ,{"int",  "Primitive Type"} ,{"Div",  "Name"} ,{"/",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static void main(String[] args) {
        TestRunner.run(ArithmaticExpressionTest.class);
    }
    
    public void testArithmaticExpression() {
        execute(fileName, expectedStates, expectedTokens);
    }
    
}
