package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.BasicConstructorTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorAttributeCallUsingThisKeywordTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorMethodCallUsingThisKeywordTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorPrimitiveTypeArgumentTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorWithObjectArgumentTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorWithSuperKeywordTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorWithSuperMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest.ConstructorWithThrowsClauseTest;

public class ConstructorTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Constructor Tests");
		suite.addTest(new TestSuite(BasicConstructorTest.class));
		suite
				.addTest(new TestSuite(
						ConstructorPrimitiveTypeArgumentTest.class));
		suite.addTest(new TestSuite(ConstructorWithObjectArgumentTest.class));
		suite.addTest(new TestSuite(ConstructorWithSuperKeywordTest.class));
		suite.addTest(new TestSuite(ConstructorWithSuperMethodTest.class));
		suite.addTest(new TestSuite(
				ConstructorAttributeCallUsingThisKeywordTest.class));
		suite.addTest(new TestSuite(
				ConstructorMethodCallUsingThisKeywordTest.class));
		suite.addTest(new TestSuite(ConstructorWithThrowsClauseTest.class));
		return suite;
	}
}
