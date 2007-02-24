package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.parser.java.AttributeTestSuite;
import org.netbeans.modules.uml.parser.java.ClassTestSuite;
import org.netbeans.modules.uml.parser.java.EnumTestSuite;
import org.netbeans.modules.uml.parser.java.ExceptionHandlingTestSuite;
import org.netbeans.modules.uml.parser.java.ExpressionTestSuite;
import org.netbeans.modules.uml.parser.java.ImportTestSuite;
import org.netbeans.modules.uml.parser.java.InterfaceTestSuite;
import org.netbeans.modules.uml.parser.java.ModifierTestSuite;
import org.netbeans.modules.uml.parser.java.OperationTestSuite;
import org.netbeans.modules.uml.parser.java.PackageTest;
import org.netbeans.modules.uml.parser.java.StatementTestSuite;
import org.netbeans.modules.uml.parser.java.ImportTestSuite;
import org.netbeans.modules.uml.parser.java.jdk5.GenericsTestSuite;
import org.netbeans.modules.uml.parser.java.jdk5.StaticImportTest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class AllParserTestSuite {

	public static void main(String[] args) {
          TestRunner.run(suite());

	}

	public static Test suite() {         
		TestSuite suite = new TestSuite("Java Parser Tests");
		suite.addTest(AttributeTestSuite.suite());
		suite.addTest(ClassTestSuite.suite());
		suite.addTest(EnumTestSuite.suite());
		suite.addTest(ExceptionHandlingTestSuite.suite());
		suite.addTest(ExpressionTestSuite.suite());
		suite.addTest(ImportTestSuite.suite());
		suite.addTest(InterfaceTestSuite.suite());
		suite.addTest(ModifierTestSuite.suite());
		suite.addTest(OperationTestSuite.suite());
		suite.addTest(StatementTestSuite.suite());
                suite.addTest(GenericsTestSuite.suite());
                suite.addTest(new TestSuite(StaticImportTest.class));
		suite.addTest(new TestSuite(PackageTest.class));
             	return suite;
	}
}
