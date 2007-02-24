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
import org.netbeans.modules.uml.parser.java.enumtest.BasicEnumTest;
import org.netbeans.modules.uml.parser.java.enumtest.EnumWithLiteralAndAttributeTest;
import org.netbeans.modules.uml.parser.java.enumtest.EnumWithLiteralTest;

public class EnumTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(BasicEnumTest.class));
		suite.addTest(new TestSuite(EnumWithLiteralAndAttributeTest.class));
		suite.addTest(new TestSuite(EnumWithLiteralTest.class));
		return suite;
	}
}
