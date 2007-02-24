package org.netbeans.modules.uml.parser.java.modifiertest;

import org.netbeans.modules.uml.parser.java.modifiertest.constructormodifiertest.PrivateConstructorTest;
import org.netbeans.modules.uml.parser.java.modifiertest.constructormodifiertest.ProtectedConstructorTest;
import org.netbeans.modules.uml.parser.java.modifiertest.constructormodifiertest.PublicConstructorTest;
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

public class ConstructorModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Constructor Modifier Tests");
		suite.addTest(new TestSuite(PrivateConstructorTest.class));
		suite.addTest(new TestSuite(ProtectedConstructorTest.class));
		suite.addTest(new TestSuite(PublicConstructorTest.class));
		return suite;
	}
}
