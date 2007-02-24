package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.exceptionhandlingtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class TryCatchFinallyBlockTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(TryCatchFinallyBlockTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testTryCatchFinallyBlock() {		
		execute(getClass().getSimpleName());
	}
}
