package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class StaticNestedClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(StaticNestedClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStaticNestedClass() {		
		execute(getClass().getSimpleName());
	}
}
