package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.operationtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class OperationWithThrowsClauseTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(OperationWithThrowsClauseTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testOperationWithThrowsClause() {		
		execute(getClass().getSimpleName());
	}
}
