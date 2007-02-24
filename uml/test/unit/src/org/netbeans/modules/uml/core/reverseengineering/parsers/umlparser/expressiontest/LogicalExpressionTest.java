package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.expressiontest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class LogicalExpressionTest extends AbstractUmlParserTestCase {

	public static void main(String[] args) {
		TestRunner.run(LogicalExpressionTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testLogicalExpression() {
		execute(getClass().getSimpleName());
	}

}
