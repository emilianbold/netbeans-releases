package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.AttributeModifierTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.ClassModifierTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.ConstructorModifierTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.InterfaceModifierTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.MethodModifierTestSuite;

public class ModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Modifier Tests");
		suite.addTest(AttributeModifierTestSuite.suite());
		suite.addTest(ClassModifierTestSuite.suite());
		suite.addTest(InterfaceModifierTestSuite.suite());
		suite.addTest(MethodModifierTestSuite.suite());
		suite.addTest(ConstructorModifierTestSuite.suite());
		return suite;
	}
}
