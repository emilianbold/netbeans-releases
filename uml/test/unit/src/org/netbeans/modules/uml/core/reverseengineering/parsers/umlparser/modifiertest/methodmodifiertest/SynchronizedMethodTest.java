package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.methodmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class SynchronizedMethodTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(SynchronizedMethodTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSynchronizedMethod() {		
		execute(getClass().getSimpleName());
	}
}
