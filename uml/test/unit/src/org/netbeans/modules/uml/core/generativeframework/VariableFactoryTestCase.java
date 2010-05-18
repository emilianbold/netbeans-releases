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

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class VariableFactoryTestCase extends AbstractUMLTestCase {
	private IVariableFactory factory = null;

	private ITemplateManager man = product.getTemplateManager();

	public static void main(String[] args) {
		junit.textui.TestRunner.run(VariableFactoryTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		factory = new VariableFactory();
	}

	public void testCreateVariable() {
		org.dom4j.Element el = DocumentFactory.getInstance().createElement("p");
		assertNotNull(factory.createVariable(el));
	}

	public void testSetExecutionContext() {
		assertNull(factory.getExecutionContext());
		assertNull(factory.getPopContext());

		IVariableExpander ive = new VariableExpander();
		factory.setExecutionContext(ive);

		// After adding one element
		assertNotNull(factory.getExecutionContext());
		assertNotNull(factory.getPopContext());

		// After removing element
		assertNull(factory.getExecutionContext());
		assertNull(factory.getPopContext());

	}

	public void testGetExecutionContext() {
		// Tested By testSetExecutionContext.
	}

	public void testGetPopContext() {
		// Tested By testSetExecutionContext
	}

	public void testSetConfigFile() {
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "config" + File.separator
				+ "ExpansionVar.etc";
		assertNull(factory.getConfigFile());
		factory.setConfigFile("abcde");
		assertEquals("abcde", factory.getConfigFile());
		factory.setConfigFile(conLoc);
	}

	public void testGetConfigFile() {
		// Tested By testSetConfigFile.
	}

	public void testCreateVariableWithText() {		
		factory.setExecutionContext(man.createExecutionContext());
		IExpansionVariable var = factory
				.createVariableWithText("implementedInterfaces");
		assertNotNull(var);
	}

	public void testGetOverrideVariables() {
		assertEquals(0, factory.getOverrideVariables().size());
		ETList<IExpansionVariable> value = new ETArrayList<IExpansionVariable>();
		org.dom4j.Element el1 = DocumentFactory.getInstance()
				.createElement("p");
		org.dom4j.Element el2 = DocumentFactory.getInstance()
				.createElement("p");

		IExpansionVariable iev1 = new ExpansionVariable();
		iev1.setNode(el1);
		iev1.setName("First");

		IExpansionVariable iev2 = new ExpansionVariable();
		iev2.setNode(el2);
		iev2.setName("Second");

		value.add(iev1);
		factory.setOverrideVariables(value);
		assertEquals(1, factory.getOverrideVariables().size());

		factory.addOverride(iev1);
		assertEquals(1, factory.getOverrideVariables().size());

		factory.addOverride(iev2);
		assertEquals(2, factory.getOverrideVariables().size());

		factory.removeOverride(iev1);
		assertEquals(1, factory.getOverrideVariables().size());

	}

	public void testSetOverrideVariables() {
		// Tested By testGetOverrideVariables.
	}

	public void testAddOverride() {
		// Tested By testGetOverrideVariables.
	}

	public void testRemoveOverride() {
		// Tested By testGetOverrideVariables.
	}

}
