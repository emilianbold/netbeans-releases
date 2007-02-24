package org.netbeans.modules.uml.parser.java;


import org.netbeans.modules.uml.parser.java.modifiertest.ConstructorModifierTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.modifiertest.AttributeModifierTestSuite;
import org.netbeans.modules.uml.parser.java.modifiertest.ClassModifierTestSuite;
import org.netbeans.modules.uml.parser.java.modifiertest.InterfaceModifierTestSuite;
import org.netbeans.modules.uml.parser.java.modifiertest.MethodModifierTestSuite;

public class ModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Modifier Tests");
		suite.addTest(AttributeModifierTestSuite.suite());
		suite.addTest(ClassModifierTestSuite.suite());
		suite.addTest(InterfaceModifierTestSuite.suite());
		suite.addTest(MethodModifierTestSuite.suite());
                suite.addTest(ConstructorModifierTestSuite.suite());
		return suite;
	}
}
