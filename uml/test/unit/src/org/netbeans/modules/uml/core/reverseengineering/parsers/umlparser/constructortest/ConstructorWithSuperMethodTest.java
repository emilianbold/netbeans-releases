package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.constructortest;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

/**
 * @author aztec
 */
public class ConstructorWithSuperMethodTest extends AbstractUmlParserTestCase {
	public static void main(String[] args) {
		TestRunner.run(ConstructorWithSuperMethodTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testConstructorWithSuperMethod() {
		execute(getClass().getSimpleName());
	}

}
