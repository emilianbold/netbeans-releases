package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class FinalMethodTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(FinalMethodTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFinalMethod() {		
		execute(getClass().getSimpleName());
	}
}
