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
import org.netbeans.modules.uml.parser.java.operationtest.OperationContainsClassTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithPrimitiveDatatypeArgumentTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithPrimitiveDatatypeReturnKeywordTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithThrowsClauseTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithObjectArgumentTest;
import org.netbeans.modules.uml.parser.java.operationtest.OperationWithObjectAsReturnTypeTest;
import org.netbeans.modules.uml.parser.java.constructortest.BasicConstructorTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorPrimitiveTypeArgumentTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorWithObjectArgumentTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorWithSuperKeywordTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorWithSuperMethodTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorAttributeCallUsingThisKeywordTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorMethodCallUsingThisKeywordTest;
import org.netbeans.modules.uml.parser.java.constructortest.ConstructorWithThrowsClauseTest;

public class ConstructorTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Constructor Tests");
		suite.addTest(new TestSuite(BasicConstructorTest.class));
		suite.addTest(new TestSuite(ConstructorPrimitiveTypeArgumentTest.class));
		suite.addTest(new TestSuite(ConstructorWithObjectArgumentTest.class));
		suite.addTest(new TestSuite(ConstructorWithSuperKeywordTest.class));
		suite.addTest(new TestSuite(ConstructorWithSuperMethodTest.class));
		suite.addTest(new TestSuite(ConstructorAttributeCallUsingThisKeywordTest.class));
		suite.addTest(new TestSuite(ConstructorMethodCallUsingThisKeywordTest.class));
		suite.addTest(new TestSuite(ConstructorWithThrowsClauseTest.class));
		return suite;
	}
}
