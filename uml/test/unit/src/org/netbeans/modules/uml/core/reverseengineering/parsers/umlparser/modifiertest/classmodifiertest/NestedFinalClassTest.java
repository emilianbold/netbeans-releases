package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class NestedFinalClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(NestedFinalClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testNestedFinalClass() {		
		execute(getClass().getSimpleName());
	}
}
