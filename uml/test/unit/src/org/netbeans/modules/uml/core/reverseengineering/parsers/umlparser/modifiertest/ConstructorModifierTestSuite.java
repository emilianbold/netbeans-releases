package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.constructormodifiertest.PrivateConstructorTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.constructormodifiertest.ProtectedConstructorTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.constructormodifiertest.PublicConstructorTest;

public class ConstructorModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Constructor Modifier Tests");
		suite.addTest(new TestSuite(PrivateConstructorTest.class));
		suite.addTest(new TestSuite(ProtectedConstructorTest.class));
		suite.addTest(new TestSuite(PublicConstructorTest.class));
		return suite;
	}
}