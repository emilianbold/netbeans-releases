package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class TryCatchTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(TryCatchTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testTryCatch() {		
		execute(getClass().getSimpleName());
	}
}
