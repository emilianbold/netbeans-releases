package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class MultipleInterfaceInheritanceTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(MultipleInterfaceInheritanceTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testMultipleInterfaceInheritance() {		
		execute(getClass().getSimpleName());
	}
}
