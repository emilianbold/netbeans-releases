package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.attributemodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class VolatileAttributeTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(VolatileAttributeTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testVolatileAttribute() {		
		execute(getClass().getSimpleName());
	}
}
