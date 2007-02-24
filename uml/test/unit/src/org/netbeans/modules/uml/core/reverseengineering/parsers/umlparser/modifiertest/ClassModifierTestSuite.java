package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.AbstractClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.AbstractInnerClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.FinalClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.NestedFinalClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.PrivateNestedClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.ProtectedNestedClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.PublicClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.PublicNestedClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.StaticNestedClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.StrictfpClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest.StrictfpNestedClassTest;

public class ClassModifierTestSuite  {	
	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Class Modifier Tests");
		suite.addTest(new TestSuite(AbstractInnerClassTest.class));
		suite.addTest(new TestSuite(AbstractClassTest.class));
		suite.addTest(new TestSuite(NestedFinalClassTest.class));
		suite.addTest(new TestSuite(FinalClassTest.class));
		suite.addTest(new TestSuite(PrivateNestedClassTest.class));
		suite.addTest(new TestSuite(ProtectedNestedClassTest.class));
		suite.addTest(new TestSuite(PublicNestedClassTest.class));
		suite.addTest(new TestSuite(PublicClassTest.class));
		suite.addTest(new TestSuite(StaticNestedClassTest.class));
		suite.addTest(new TestSuite(StrictfpNestedClassTest.class));
		suite.addTest(new TestSuite(StrictfpClassTest.class));
		return suite;
	}
}
