package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.modifiertest.classmodifiertest;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.AbstractUmlParserTestCase;

public class AbstractInnerClassTest extends AbstractUmlParserTestCase {	
	public static void main(String[] args) {
		TestRunner.run(AbstractInnerClassTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAbstractInnerClass() {		
		execute(getClass().getSimpleName());
	}
}
