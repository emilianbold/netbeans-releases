package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.enumtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class EnumWithLiteralAndAttributeTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(EnumWithLiteralAndAttributeTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEnumWithLiteralAndAttribute() {		
		execute(getClass().getSimpleName());
	}
}
