package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class ConstructorAttributeCallUsingThisKeywordTest extends
		AbstractUmlParserTestCase {

	public static void main(String[] args) {
		TestRunner.run(ConstructorAttributeCallUsingThisKeywordTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testConstructorAttributeCallUsingThisKeyword() {
		execute(getClass().getSimpleName());
	}

}
