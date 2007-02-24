package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class BasicClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(BasicClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBasicClass() {		
		execute(getClass().getSimpleName());
	}
}
