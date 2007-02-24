package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.BasicOperationTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.OperationContainsClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.OperationWithObjectArgumentTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.OperationWithObjectAsReturnTypeTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.OperationWithPrimitiveDatatypeArgumentTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.OperationWithPrimitiveDatatypeReturnKeywordTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest.OperationWithThrowsClauseTest;

public class OperationTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Operation Tests");
		suite.addTest(new TestSuite(BasicOperationTest.class));
		suite.addTest(new TestSuite(OperationContainsClassTest.class));
		suite.addTest(new TestSuite(OperationWithPrimitiveDatatypeArgumentTest.class));
		suite.addTest(new TestSuite(OperationWithPrimitiveDatatypeReturnKeywordTest.class));
		suite.addTest(new TestSuite(OperationWithThrowsClauseTest.class));
		suite.addTest(new TestSuite(OperationWithObjectArgumentTest.class));
		suite.addTest(new TestSuite(OperationWithObjectAsReturnTypeTest.class));
		suite.addTest(ConstructorTestSuite.suite());
		return suite;
	}
}
