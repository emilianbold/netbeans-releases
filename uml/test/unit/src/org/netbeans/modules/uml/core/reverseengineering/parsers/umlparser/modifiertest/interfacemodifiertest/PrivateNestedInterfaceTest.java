package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.interfacemodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class PrivateNestedInterfaceTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(PrivateNestedInterfaceTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPrivateNestedInterface() {		
		execute(getClass().getSimpleName());
	}
}
