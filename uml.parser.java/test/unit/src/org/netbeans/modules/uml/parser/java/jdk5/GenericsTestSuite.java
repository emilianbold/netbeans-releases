package org.netbeans.modules.uml.parser.java.jdk5;

import org.netbeans.modules.uml.parser.java.jdk5.generics.AutoBoxingTest;
import org.netbeans.modules.uml.parser.java.jdk5.generics.AutoUnBoxingTest;
import org.netbeans.modules.uml.parser.java.jdk5.generics.BasicGenericsTest;
import org.netbeans.modules.uml.parser.java.jdk5.generics.GenericsElementAddTest;
import org.netbeans.modules.uml.parser.java.jdk5.generics.GenericsArgumentAndReturntypeTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class GenericsTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Generics Tests");
		suite.addTest(new TestSuite(BasicGenericsTest.class));
		suite.addTest(new TestSuite(AutoBoxingTest.class));
		suite.addTest(new TestSuite(AutoUnBoxingTest.class));
		suite.addTest(new TestSuite(GenericsElementAddTest.class));
		suite.addTest(new TestSuite(GenericsArgumentAndReturntypeTest.class));		
		return suite;
	}
}
