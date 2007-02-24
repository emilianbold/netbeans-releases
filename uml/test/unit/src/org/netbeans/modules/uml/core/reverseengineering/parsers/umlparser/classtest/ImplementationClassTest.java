package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.classtest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class ImplementationClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(ImplementationClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testImplementationClass() {		
		execute(getClass().getSimpleName());
	}
}
