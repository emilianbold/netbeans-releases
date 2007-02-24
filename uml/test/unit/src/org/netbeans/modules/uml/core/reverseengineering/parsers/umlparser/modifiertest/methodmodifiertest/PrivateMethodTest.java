package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class PrivateMethodTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(PrivateMethodTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPrivateMethod() {		
		execute(getClass().getSimpleName());
	}
}
