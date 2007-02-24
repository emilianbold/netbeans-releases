package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class PublicClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(PublicClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPublicClass() {		
		execute(getClass().getSimpleName());
	}
}
