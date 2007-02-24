package org.netbeans.modules.uml.core.generativeframework;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllGenerativeFrameworkTests {
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("GenerativeFramework Tests");

		suite.addTest(new TestSuite(ExpansionResultTestCase.class));
		suite.addTest(new TestSuite(ExpansionVariableTestCase.class));
		suite.addTest(new TestSuite(ExpansionVarLocatorTestCase.class));
		suite.addTest(new TestSuite(FormatterTestCase.class));
		suite.addTest(new TestSuite(IfTestTestCase.class));
		suite.addTest(new TestSuite(IfVariableTestCase.class));
		suite.addTest(new TestSuite(ImportVariableTestCase.class));
		suite.addTest(new TestSuite(IterationVariableTestCase.class));
		suite.addTest(new TestSuite(TemplateManagerTestCase.class));
		suite.addTest(new TestSuite(VariableExpanderTestCase.class));
		suite.addTest(new TestSuite(VariableFactoryTestCase.class));

		return suite;
	}
}
