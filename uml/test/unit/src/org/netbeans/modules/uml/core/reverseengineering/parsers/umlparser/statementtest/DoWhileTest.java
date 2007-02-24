package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.statementtest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class DoWhileTest extends AbstractUmlParserTestCase {

	public static void main(String[] args) {
		TestRunner.run(DoWhileTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testDoWhile() {
		execute(getClass().getSimpleName());
	}

}
