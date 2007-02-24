package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class IfElseTest extends AbstractUmlParserTestCase {

	public static void main(String[] args) {
		TestRunner.run(IfElseTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testIfElse() {
		execute(getClass().getSimpleName());
	}

}
