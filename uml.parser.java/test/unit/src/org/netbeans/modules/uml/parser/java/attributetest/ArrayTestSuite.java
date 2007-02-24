package org.netbeans.modules.uml.parser.java.attributetest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.attributetest.arraytest.ArrayDeclarationTest;
import org.netbeans.modules.uml.parser.java.attributetest.arraytest.ArrayInitializationTest;
import org.netbeans.modules.uml.parser.java.attributetest.arraytest.AssignElementsTest;

public class ArrayTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(ArrayDeclarationTest.class));
		suite.addTest(new TestSuite(ArrayInitializationTest.class));
		suite.addTest(new TestSuite(AssignElementsTest.class));
		
		return suite;
	}
}
