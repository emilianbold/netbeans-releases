package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class BasicInterfaceTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(BasicInterfaceTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBasicInterface() {		
		execute(getClass().getSimpleName());
	}
}
