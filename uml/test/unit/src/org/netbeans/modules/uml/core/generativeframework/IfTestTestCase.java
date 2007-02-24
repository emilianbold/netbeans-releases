package org.netbeans.modules.uml.core.generativeframework;

import junit.framework.TestCase;

/**
 * @author aztec
 */
public class IfTestTestCase extends TestCase {
	private IfTest ifTest = null;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(IfTestTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		ifTest = new IfTest();
	}

	public void testSetTest() {
		assertNull(ifTest.getTest());
		ifTest.setTest("volatile");
		assertEquals("volatile", ifTest.getTest());
	}

	public void testGetTest() {
		// Tested by testSetTest.
	}

	public void testSetResultAction() {
		assertNull(ifTest.getResultAction());
		ifTest.setResultAction("static");
		assertEquals("static", ifTest.getResultAction());
	}

	public void testGetResultAction() {
		// Tested by testSetResultAction.
	}

}
