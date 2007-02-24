package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class TryMultiCatchTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(TryMultiCatchTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testTryMultiCatch() {		
		execute(getClass().getSimpleName());
	}
}
