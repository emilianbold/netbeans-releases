package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.AssertStatementTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.DoWhileTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.EnhancedForLoopTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.ForLoopTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.IfElseIfTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.IfElseTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.IfTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.SwitchCaseTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest.WhileContinueBreakTest;

public class StatementTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Statement Tests");
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
