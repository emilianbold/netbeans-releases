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
import org.netbeans.modules.uml.parser.java.statementtest.AssertStatementTest;
import org.netbeans.modules.uml.parser.java.statementtest.DoWhileTest;
import org.netbeans.modules.uml.parser.java.statementtest.EnhancedForLoopTest;
import org.netbeans.modules.uml.parser.java.statementtest.ForLoopTest;
import org.netbeans.modules.uml.parser.java.statementtest.IfElseIfTest;
import org.netbeans.modules.uml.parser.java.statementtest.IfElseTest;
import org.netbeans.modules.uml.parser.java.statementtest.IfTest;
import org.netbeans.modules.uml.parser.java.statementtest.SwitchCaseTest;
import org.netbeans.modules.uml.parser.java.statementtest.WhileContinueBreakTest;

public class StatementTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Statement Tests");
		suite.addTest(new TestSuite(AssertStatementTest.class));
		suite.addTest(new TestSuite(DoWhileTest.class));
		suite.addTest(new TestSuite(EnhancedForLoopTest.class));
		suite.addTest(new TestSuite(ForLoopTest.class));		
		suite.addTest(new TestSuite(IfElseIfTest.class));
		suite.addTest(new TestSuite(IfElseTest.class));
		suite.addTest(new TestSuite(IfTest.class));
		suite.addTest(new TestSuite(SwitchCaseTest.class));
		suite.addTest(new TestSuite(WhileContinueBreakTest.class));
		return suite;
	}
}
