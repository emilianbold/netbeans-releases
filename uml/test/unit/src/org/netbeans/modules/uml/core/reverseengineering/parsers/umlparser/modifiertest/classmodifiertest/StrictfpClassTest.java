package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class StrictfpClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(StrictfpClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStrictfpClass() {		
		execute(getClass().getSimpleName());
	}
}
