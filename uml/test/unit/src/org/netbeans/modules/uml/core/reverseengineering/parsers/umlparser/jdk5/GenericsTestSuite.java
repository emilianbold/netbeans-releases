package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.generics.AutoBoxingTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.generics.AutoUnBoxingTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.generics.BasicGenericsTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.generics.GenericsArgumentAndReturntypeTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.jdk5.generics.GenericsElementAddTest;

public class GenericsTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Generics Tests");
		suite.addTest(new TestSuite(BasicGenericsTest.class));
		suite.addTest(new TestSuite(AutoBoxingTest.class));
		suite.addTest(new TestSuite(AutoUnBoxingTest.class));
		suite.addTest(new TestSuite(GenericsElementAddTest.class));
		suite.addTest(new TestSuite(GenericsArgumentAndReturntypeTest.class));		
		return suite;
	}
}
