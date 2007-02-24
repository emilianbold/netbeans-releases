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
import org.netbeans.modules.uml.parser.java.exceptionhandlingtest.ThrowKeywordInsideMethodTest;
import org.netbeans.modules.uml.parser.java.exceptionhandlingtest.ThrowsKeywordInMethodDeclarationTest;
import org.netbeans.modules.uml.parser.java.exceptionhandlingtest.TryCatchFinallyBlockTest;
import org.netbeans.modules.uml.parser.java.exceptionhandlingtest.TryCatchTest;
import org.netbeans.modules.uml.parser.java.exceptionhandlingtest.TryFinallyTest;
import org.netbeans.modules.uml.parser.java.exceptionhandlingtest.TryMultiCatchTest;

public class ExceptionHandlingTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(ThrowKeywordInsideMethodTest.class));
		suite.addTest(new TestSuite(ThrowsKeywordInMethodDeclarationTest.class));
		suite.addTest(new TestSuite(TryCatchFinallyBlockTest.class));
		suite.addTest(new TestSuite(TryCatchTest.class));
		suite.addTest(new TestSuite(TryFinallyTest.class));
		suite.addTest(new TestSuite(TryMultiCatchTest.class));
		
		return suite;
	}
}
