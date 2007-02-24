package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class InterfaceContainsClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(InterfaceContainsClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testInterfaceContainsClass() {		
		execute(getClass().getSimpleName());
	}
}
