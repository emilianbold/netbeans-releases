package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class FinalClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(FinalClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFinalClass() {		
		execute(getClass().getSimpleName());
	}
}
