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


import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.NamedCollection;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class ImportVariableTestCase extends AbstractUMLTestCase {
	private ImportVariable var = null;

	String varText = "ImportVar <test=\"nodeName\" result=\"1:UML:Interface\" resultAction=\"1:java_interface_head.gt\" var=\"java_class_head.gt\">;";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ImportVariableTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		var = new ImportVariable();
	}

	public void testSetTemplate() {
		assertNull(var.getTemplate());
		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_import_statement.gt";

		var.setTemplate(conLoc);
		assertEquals(conLoc, var.getTemplate());
	}

	public void testGetTemplate() {
		// Tested by testSetTemplate.
	}

	public void testSetTestClause() {
		assertNull(var.getTestClause());
		var.setTestClause("nodeName");
		assertEquals("nodeName", var.getTestClause());		
	}

	public void testGetTestClause() {
		// Tested by testSetTestClause.
	}

	public void testSetConditions() {
		ETList<INamedCollection> resultActions = new ETArrayList<INamedCollection>();
		INamedCollection pair = new NamedCollection();
		pair.setName("UML:Interface");
		pair.setData("java_interface_head.gt");
		resultActions.add(pair);

		assertNull(var.getConditions());

		var.setConditions(resultActions);
		assertEquals(resultActions, var.getConditions());

		assertEquals("UML:Interface", var.getConditions().get(0).getName());
		assertEquals("java_interface_head.gt", var.getConditions().get(0)
				.getData());

	}

	public void testGetConditions() {
		// tested by testSetConditions.
	}

	public void testExpand() {

		String conLoc = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
				+ File.separator + "scripts" + File.separator + "java"
				+ File.separator + "java_import_statement.gt";
		var.setTemplate(conLoc);

		IInterface newClass = createInterface("Clazz");

		var.setExecutionContext(product.getTemplateManager()
				.createExecutionContext());
		var.setNode(newClass.getNode());
		assertEquals(System.getProperty("line.separator") + "import Clazz.*;", var.expand(newClass.getNode()));
	}

}
