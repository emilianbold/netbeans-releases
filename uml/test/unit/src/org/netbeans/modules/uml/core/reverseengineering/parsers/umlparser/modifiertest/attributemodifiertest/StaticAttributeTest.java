package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.attributemodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class StaticAttributeTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(StaticAttributeTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStaticAttribute() {		
		execute(getClass().getSimpleName());
	}
}
