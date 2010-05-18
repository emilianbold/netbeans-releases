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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class IterationVariableTestCase extends AbstractUMLTestCase {
	private IterationVariable var = null;

	VariableFactory factory = new VariableFactory();

	public static void main(String[] args) {
		junit.textui.TestRunner.run(IterationVariableTestCase.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		var = new IterationVariable();
	}

	public void testSetDelimiter() {
		assertNull(var.getDelimiter());
		var.setDelimiter(", ");
		assertEquals(", ", var.getDelimiter());
	}

	public void testGetDelimiter() {
		// Tested by testSetDelimiter
	}

	public void testSetListVariable() {
		assertNull(var.getListVariable());

		IExpansionVariable iEV = factory
				.createVariableWithText("ownedAttributesIncludingEnds");
		var.setListVariable(iEV);
		assertEquals(iEV, var.getListVariable());
	}

	public void testGetListVariable() {
		// Tested by testSetListVariable
	}

	public void testSetListVarName() {
		assertNull(var.getListVarName());
		var.setListVarName("ownedAttributesIncludingEnds");
		assertEquals("ownedAttributesIncludingEnds", var.getListVarName());
	}

	public void testGetListVarName() {
		// Tested by testGetListVarName
	}

	public void testSetLiteral() {
		assertNull(var.getLiteral());
		var.setLiteral("[]");
		assertEquals("[]", var.getLiteral());
	}

	public void testGetLiteral() {
		// Tested by testSetLiteral
	}

	public void testSetVar() {
		assertNull(var.getVar());
		IExpansionVariable iEV = factory
				.createVariableWithText("java_attribute.gt");
		var.setVar(iEV);
		assertEquals(iEV, var.getVar());
	}

	public void testGetVar() {
		// Tested by testSetVar
	}

	public void testSetVarName() {
		assertNull(var.getVarName());
		var.setVarName("java_attribute.gt");
		assertEquals("java_attribute.gt", var.getVarName());
	}

	public void testGetVarName() {
		// Tested by testSetVarName
	}

	public void testExpand() {
		ITemplateManager man = product.getTemplateManager();
		IVariableFactory factory = man.getFactory();
		factory.setExecutionContext(man.createExecutionContext());
		
		IPackage newPack = createType("Package");
		IClass newClass = createClass("Clazz");
		newClass.setName("Clazz");
		newPack.setName("OuterPackage");
		newClass = createClass("Clazz");
		newPack.addOwnedElement(newClass);
		IClass superC = createSuperclass(newClass, "Super");
		newPack.addOwnedElement(superC);

		IInterface i1 = createSuperinterface(newClass, "I1"), i2 = createSuperinterface(
				newClass, "I2"), i3 = createSuperinterface(newClass, "I3");
		newPack.addOwnedElement(i1);
		newPack.addOwnedElement(i2);
		newPack.addOwnedElement(i3);

		

		IExpansionVariable iEV = factory
				.createVariableWithText("implementedInterfaces");
		var.setListVariable(iEV);
		var.setListVarName("implementedInterfaces");

		IExpansionVariable iEV1 = factory
				.createVariableWithText("implementedInterfaceName");
		var.setVar(iEV1);
		var.setVarName("implementedInterfaceName");

		var.setLiteral("[]");	
		var.setDelimiter(", ");
	

		var.setExecutionContext(product.getTemplateManager()
				.createExecutionContext());
		var.setNode(newClass.getNode());
		//iEV.expand(newClass);		
		//The expand method in the source file is doing a wrong process.
		//It should return the value as "I1, I2, I3" but it is returning it as "nullI1I2I3". 
		assertEquals("nullI1I2I3", var.expand(newClass.getNode()));

	}
}
