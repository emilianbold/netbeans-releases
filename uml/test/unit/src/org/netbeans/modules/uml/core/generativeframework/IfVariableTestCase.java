/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.generativeframework;

import java.io.File;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * Test cases for IfVariable.
 * 
 */
public class IfVariableTestCase extends AbstractUMLTestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(IfVariableTestCase.class);
	}

	private IfVariable var;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		var = new IfVariable();
	}

	public void testAddTest() {
		// Default tests should be 0.
		assertEquals(0, var.getTests().size());

		// Initialize and set name to IfTest
		IIfTest ifTest1 = new IfTest();
		ifTest1.setTest("FirstTest");
		IIfTest ifTest2 = new IfTest();
		ifTest2.setTest("SecondTest");

		// addTest with duplicate IfTest
		var.addTest(ifTest1);
		var.addTest(ifTest1);// Duplicate IFTests
		assertEquals(1, var.getTests().size());

		// add more IfTest
		var.addTest(ifTest2);
		assertEquals(2, var.getTests().size());
		assertEquals("FirstTest", var.getTests().get(0).getTest());
		assertEquals("SecondTest", var.getTests().get(1).getTest());

		// Remove Iftest
		var.removeTest(ifTest1);
		assertEquals(1, var.getTests().size());
		assertEquals("SecondTest", var.getTests().get(0).getTest());		
	}

	public void testRemoveTest() {
		// Tested by testAddTest.
	}

	public void testGetTests() {
		// Tested by testAddTest.
	}

	public void testExpand() {
		TemplateManager man = new TemplateManager();
		VariableFactory vf = new VariableFactory();
		VariableExpander ve = new VariableExpander();
		// String conLoc =
		// ProductHelper.getConfigManager().getDefaultConfigLocation();
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator; // NOI18N

		ve.setConfigFile(new File(conLoc + "ExpansionVar.etc").toString()); // NOI18N
		vf.setExecutionContext(ve);
		ve.setManager(man);
		man.setFactory(vf);
		var.setExecutionContext(man.getVariableExpander());

		Element el = DocumentFactory.getInstance().createElement("p"); // NOI18N
		XMLManip.setAttributeValue(el, "isVolatile", "true"); // NOI18N
		XMLManip.setAttributeValue(el, "isStatic", "true"); // NOI18N

		IfTest ift = new IfTest();
		ift.setTest("volatile"); // NOI18N
		ift.setResultAction("static"); // NOI18N

		var.addTest(ift);
		assertEquals("true", var.expand(el)); // NOI18N
	}

	public void testGetKind() {
		assertEquals(VariableKind.VK_NONE, var.getKind());
		var.setKind(VariableKind.VK_ATTRIBUTE);
		assertEquals(VariableKind.VK_ATTRIBUTE, var.getKind());
	}

	public void testSetKind() {
		// Tested by testGetKind.
	}
}
