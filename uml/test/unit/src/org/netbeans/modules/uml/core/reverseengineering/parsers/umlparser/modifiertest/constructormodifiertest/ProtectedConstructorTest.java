package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.constructormodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ProtectedConstructorTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ProtectedConstructorTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testProtectedConstructor() {		
		execute(getClass().getSimpleName());
	}
}
