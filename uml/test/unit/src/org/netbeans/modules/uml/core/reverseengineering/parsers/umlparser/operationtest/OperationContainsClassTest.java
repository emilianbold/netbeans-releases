package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class OperationContainsClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(OperationContainsClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testOperationContainsClass() {		
		execute(getClass().getSimpleName());
	}
}
