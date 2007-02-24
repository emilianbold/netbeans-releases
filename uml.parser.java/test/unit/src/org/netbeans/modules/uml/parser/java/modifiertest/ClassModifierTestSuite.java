package org.netbeans.modules.uml.parser.java.modifiertest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.AbstractInnerClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.AbstractClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.NestedFinalClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.FinalClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.PrivateNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.ProtectedNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.PublicNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.PublicClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.StaticNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.StrictfpNestedClassTest;
import org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest.StrictfpClassTest;

public class ClassModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Modifier Tests");
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
