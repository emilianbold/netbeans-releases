package org.netbeans.modules.uml.parser.java;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.classtest.BasicClassTest;
import org.netbeans.modules.uml.parser.java.classtest.AnanymousClassTest;
import org.netbeans.modules.uml.parser.java.classtest.ClassBlockTest;
import org.netbeans.modules.uml.parser.java.classtest.ClassContainsInterfaceTest;
import org.netbeans.modules.uml.parser.java.classtest.GeneralizationImplementationClassTest;
import org.netbeans.modules.uml.parser.java.classtest.GeneralizationClassTest;
import org.netbeans.modules.uml.parser.java.classtest.ImplementationClassTest;
import org.netbeans.modules.uml.parser.java.classtest.MultipleClassTest;
import org.netbeans.modules.uml.parser.java.classtest.NestedClassTest;
import org.netbeans.modules.uml.parser.java.operationtest.BasicOperationTest;
import org.netbeans.modules.uml.parser.java.ConstructorTestSuite;
import org.netbeans.modules.uml.parser.java.operationtest.OperationContainsClassTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithPrimitiveDatatypeArgumentTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithPrimitiveDatatypeReturnKeywordTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithThrowsClauseTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithObjectArgumentTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithObjectAsReturnTypeTest;

public class OperationTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Operation Tests");
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
