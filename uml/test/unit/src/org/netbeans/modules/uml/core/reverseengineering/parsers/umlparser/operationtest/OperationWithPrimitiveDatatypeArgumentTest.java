package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class OperationWithPrimitiveDatatypeArgumentTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(OperationWithPrimitiveDatatypeArgumentTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testOperationWithPrimitiveDatatypeArgument() {		
		execute(getClass().getSimpleName());
	}
}
