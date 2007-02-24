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
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.AbstractMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.FinalMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.NativeMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.PrivateMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.ProtectedMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.PublicMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.StaticMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.StrictfpMethodTest;
import org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest.SynchronizedMethodTest;

public class MethodModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Method Modifier Tests");
		suite.addTest(new TestSuite(AbstractMethodTest.class));
		suite.addTest(new TestSuite(FinalMethodTest.class));
		suite.addTest(new TestSuite(NativeMethodTest.class));
		suite.addTest(new TestSuite(PrivateMethodTest.class));
		suite.addTest(new TestSuite(ProtectedMethodTest.class));
		suite.addTest(new TestSuite(PublicMethodTest.class));
		suite.addTest(new TestSuite(StaticMethodTest.class));
		suite.addTest(new TestSuite(StrictfpMethodTest.class));
		suite.addTest(new TestSuite(SynchronizedMethodTest.class));
		return suite;
	}
}
