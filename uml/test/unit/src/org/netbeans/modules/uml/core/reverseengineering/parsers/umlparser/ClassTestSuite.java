package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.AnanymousClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.BasicClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.ClassBlockTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.ClassContainsInterfaceTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.GeneralizationClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.GeneralizationImplementationClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.ImplementationClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.MultipleClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest.NestedClassTest;

public class ClassTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Class Tests");
		suite.addTest(new TestSuite(AnanymousClassTest.class));
		suite.addTest(new TestSuite(ClassBlockTest.class));
		suite.addTest(new TestSuite(ClassContainsInterfaceTest.class));
		suite.addTest(new TestSuite(BasicClassTest.class));
		suite
				.addTest(new TestSuite(
						GeneralizationImplementationClassTest.class));
		suite.addTest(new TestSuite(GeneralizationClassTest.class));
		suite.addTest(new TestSuite(ImplementationClassTest.class));
		suite.addTest(new TestSuite(MultipleClassTest.class));
		suite.addTest(new TestSuite(NestedClassTest.class));
		return suite;
	}
}
