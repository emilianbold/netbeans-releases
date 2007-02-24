package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.AbstractMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.FinalMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.NativeMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.PrivateMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.ProtectedMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.PublicMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.StaticMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.StrictfpMethodTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest.SynchronizedMethodTest;

public class MethodModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Method Modifier Tests");
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
