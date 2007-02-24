package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.constructormodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class PrivateConstructorTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(PrivateConstructorTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPrivateConstructor() {		
		execute(getClass().getSimpleName());
	}
}
