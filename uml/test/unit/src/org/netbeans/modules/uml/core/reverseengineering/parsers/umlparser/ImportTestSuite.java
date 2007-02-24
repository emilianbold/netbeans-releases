package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.importtest.ImportClassDependencyTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.importtest.ImportPackageDependencyTest;

public class ImportTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Import Tests");
		suite
				.addTest(new TestSuite(
						ImportPackageDependencyTest.class));
		suite.addTest(new TestSuite(ImportClassDependencyTest.class));
		return suite;
	}
}
