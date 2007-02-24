package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest.ThrowKeywordInsideMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest.ThrowsKeywordInMethodDeclarationTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest.TryCatchFinallyBlockTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest.TryCatchTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest.TryFinallyTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest.TryMultiCatchTest;

public class ExceptionHandlingTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Class Tests");
		suite.addTest(new TestSuite(ThrowKeywordInsideMethodTest.class));
		suite
				.addTest(new TestSuite(
						ThrowsKeywordInMethodDeclarationTest.class));
		suite.addTest(new TestSuite(TryCatchFinallyBlockTest.class));
		suite.addTest(new TestSuite(TryCatchTest.class));
		suite.addTest(new TestSuite(TryFinallyTest.class));
		suite.addTest(new TestSuite(TryMultiCatchTest.class));

		return suite;
	}
}
