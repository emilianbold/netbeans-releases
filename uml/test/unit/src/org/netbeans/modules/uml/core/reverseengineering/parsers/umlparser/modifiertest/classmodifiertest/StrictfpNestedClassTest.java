package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class StrictfpNestedClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(StrictfpNestedClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStrictfpNestedClass() {		
		execute(getClass().getSimpleName());
	}
}
