package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.constructormodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class PublicConstructorTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(PublicConstructorTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPublicConstructor() {		
		execute(getClass().getSimpleName());
	}
}
