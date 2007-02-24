package org.netbeans.modules.uml.parser.java.attributetest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest.VariableDeclaredWithinClassTest;
import org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest.VariableDeclaredWithinMethodTest;
import org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest.VariableDeclaredWithinBlockTest;
import org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest.VariableInForLoopVariableTest;

public class SimpleVariableDeclarationTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(VariableDeclaredWithinClassTest.class));
		suite.addTest(new TestSuite(VariableDeclaredWithinMethodTest.class));
		suite.addTest(new TestSuite(VariableDeclaredWithinBlockTest.class));
		suite.addTest(new TestSuite(VariableInForLoopVariableTest.class));
		return suite;
	}
}
