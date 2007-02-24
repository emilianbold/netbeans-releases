package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class AssertStatementTest extends AbstractUmlParserTestCase {
	public static void main(String[] args) {
		TestRunner.run(AssertStatementTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAssertStatement() {
		execute(getClass().getSimpleName());
	}

}
