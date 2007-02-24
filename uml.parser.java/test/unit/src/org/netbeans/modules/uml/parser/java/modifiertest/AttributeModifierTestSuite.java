package org.netbeans.modules.uml.parser.java.modifiertest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.FinalAttributeTest;
import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.PrivateAttributeTest;
import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.ProtectedAttributeTest;
import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.PublicAttributeTest;
import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.StaticAttributeTest;
import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.TransientAttributeTest;
import org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest.VolatileAttributeTest;

public class AttributeModifierTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Attribute Modifier Tests");

		suite.addTest(new TestSuite(FinalAttributeTest.class));
		suite.addTest(new TestSuite(PrivateAttributeTest.class));
		suite.addTest(new TestSuite(ProtectedAttributeTest.class));
		suite.addTest(new TestSuite(PublicAttributeTest.class));
		suite.addTest(new TestSuite(StaticAttributeTest.class));
		suite.addTest(new TestSuite(TransientAttributeTest.class));
		suite.addTest(new TestSuite(VolatileAttributeTest.class));
		return suite;
	}
}
