package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.attributemodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class FinalAttributeTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(FinalAttributeTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFinalAttribute() {		
		execute(getClass().getSimpleName());
	}
}
