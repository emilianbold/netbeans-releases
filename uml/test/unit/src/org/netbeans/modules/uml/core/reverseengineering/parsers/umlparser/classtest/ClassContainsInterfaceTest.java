package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ClassContainsInterfaceTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ClassContainsInterfaceTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testClassContainsInterface() {		
		execute(getClass().getSimpleName());
	}
}
