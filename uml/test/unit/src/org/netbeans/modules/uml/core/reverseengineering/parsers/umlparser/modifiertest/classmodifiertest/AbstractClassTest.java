package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class AbstractClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(AbstractClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAbstractClass() {		
		execute(getClass().getSimpleName());
	}
}
