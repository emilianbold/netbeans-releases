package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.expressiontest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class RelationalExpressionTest extends AbstractUmlParserTestCase {
	public static void main(String[] args) {
		TestRunner.run(RelationalExpressionTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testRelationalExpression() {
		execute(getClass().getSimpleName());
	}

}
