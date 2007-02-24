package org.netbeans.modules.uml.parser.java;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.expressiontest.ArithmaticExpressionTest;
import org.netbeans.modules.uml.parser.java.expressiontest.LogicalExpressionTest;
import org.netbeans.modules.uml.parser.java.expressiontest.RelationalExpressionTest;
import org.netbeans.modules.uml.parser.java.expressiontest.StringConcatenationTest;

public class ExpressionTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Expression Tests");
		suite.addTest(new TestSuite(ArithmaticExpressionTest.class));
		suite.addTest(new TestSuite(LogicalExpressionTest.class));
		suite.addTest(new TestSuite(RelationalExpressionTest.class));
		suite.addTest(new TestSuite(StringConcatenationTest.class));
		return suite;
	}
}
