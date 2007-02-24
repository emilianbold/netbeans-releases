package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.enumtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class EnumWithLiteralTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(EnumWithLiteralTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEnumWithLiteral() {		
		execute(getClass().getSimpleName());
	}
}
