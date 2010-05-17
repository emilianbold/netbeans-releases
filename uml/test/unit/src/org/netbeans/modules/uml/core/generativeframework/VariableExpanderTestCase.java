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
import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author aztec
 */
public class VariableExpanderTestCase extends AbstractUMLTestCase {
	private VariableExpander var = null;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(VariableExpanderTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		var = new VariableExpander();
	}

	public void testSetConfigFile() {
		assertNull(var.getConfigFile());
		var.setConfigFile("abc");
		assertEquals("abc", var.getConfigFile());
	}

	public void testGetConfigFile() {
		// Tested by testSetConfigFile.
	}

	public void testRetrieveVarNode() {
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		var.setConfigFile(conLoc);

		// xyz node is not there in Expansionvar.etc.
		assertNull(var.retrieveVarNode("xyz"));

		// Pass existing the node name(abstract).
		Node n = var.retrieveVarNode("abstract");
		assertNotNull(n);
		assertEquals("ExpansionVar", n.getName());
		Element c = new Element();
		c.setNode(n);
		assertEquals("abstract", c.getAttributeValue("name"));
	}

	public void testSetManager() {
		assertNull(var.getManager());
		ITemplateManager man = product.getTemplateManager();
		var.setManager(man);
		assertEquals(man, var.getManager());
	}

	public void testGetManager() {
		// Tested by testSetManager.
	}

	public void testExpand() {

		assertFalse(var.endGathering());

		var.beginGathering();
		assertFalse(var.endGathering());

		/* Prepare Expansion variable and IFTests */
		VariableFactory vf = new VariableFactory();
		ITemplateManager man = product.getTemplateManager();
		vf.setExecutionContext(var);
		var.setManager(man);
		man.setFactory(vf);

		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		var.setConfigFile(conLoc);

		IfVariable ifv = new IfVariable();
		ifv.setExecutionContext(man.getVariableExpander());

		org.dom4j.Element el = DocumentFactory.getInstance().createElement("p");
		XMLManip.setAttributeValue(el, "isVolatile", "true");
		XMLManip.setAttributeValue(el, "isStatic", "true");

		IfTest ift = new IfTest();
		ift.setTest("volatile");
		ift.setResultAction("static");

		ifv.addTest(ift);

		assertNotNull(el);

		var.beginGathering();
		String result = var.expand("", ifv, el);
		assertEquals("true", result);

		// After adding one result endGathering should return true.
		assertTrue(var.endGathering());

	}

	public void testBeginGathering() {
		// Tested by testExpand
	}

	public void testEndGathering() {
		// Tested by testExpand.
	}

	public void testAddResult() {
		// Not Implemented Stubbed in C++ code
	}

	public void testRemoveResult() {
		// Not Implemented Stubbed in C++ code
	}

	public void testAppendResults() {
		// Not Implemented Stubbed in C++ code
	}

	public void testSetExpansionResults() {
		// Not Implemented Stubbed in C++ code
	}

	public void testGetExpansionResults() {
		// Not Implemented Stubbed in C++ code
	}

}
