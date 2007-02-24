package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ProtectedNestedClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ProtectedNestedClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testProtectedNestedClass() {		
		execute(getClass().getSimpleName());
	}
}
