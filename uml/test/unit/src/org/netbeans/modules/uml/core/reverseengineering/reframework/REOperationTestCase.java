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


package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for REOperation.
 */
public class REOperationTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REOperationTestCase.class);
    }

    private REOperation reo;
    private IOperation  op;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reo = new REOperation();

        IClass c = createClass("Clazz");
        op = c.createOperation("int", "walter");
        c.addOperation(op);
        op.addParameter(op.createParameter("int", "dyke"));
        op.addParameter(op.createParameter("char", "seebeck"));
        op.setReturnType2("int");
        
        op.getParameters().get(0).getElementNode().addAttribute("type", "int");
        op.getParameters().get(1).getElementNode().addAttribute("type", "char");
        op.getParameters().get(2).getElementNode().addAttribute("type", "int");        
        
        element = op.getReturnType().getElementNode();
        addToken("IsPrimitive", "true");

        op.setConcurrency(BaseElement.CCK_GUARDED);
        
        reo.setEventData(op.getElementNode());
        
        element = op.getElementNode();
        Element o  = (Element) element.selectSingleNode("UML:Element.ownedElement");
        Element exc = XMLManip.createElement(o, "UML:Exception");
        exc.addAttribute("name", "IOException");
        
        IStructuralFeature feat = createType("NavigableEnd");
        IMultiplicity mul = createType("Multiplicity");
        mul.setRangeThroughString("[0..2]");
        feat.setMultiplicity(mul);
        
        Node n = feat.getElementNode().selectSingleNode("UML:TypedElement.multiplicity");
        n.detach();
        element.add(n);
    }

    public void testClone()
    {
        IClass x = createClass("X");
        IOperation o = reo.clone(x);
        assertEquals("walter", o.getName());
        assertEquals("int", o.getReturnType2());
        
        ETList<IParameter> pars = o.getParameters();
        assertEquals(3, pars.size());
        assertEquals("dyke", pars.get(1).getName());
        assertEquals("seebeck", pars.get(2).getName());
    }

    public void testGetConcurrency()
    {
        assertEquals(BaseElement.CCK_GUARDED, reo.getConcurrency());
    }

    public void testGetIsAbstract()
    {
        op.setIsAbstract(true);
        assertTrue(reo.getIsAbstract());
    }

    public void testGetIsConstructor()
    {
        op.setIsConstructor(true);
        assertTrue(reo.getIsConstructor());
    }

    public void testGetIsNative()
    {
        op.setIsNative(true);
        assertTrue(reo.getIsNative());
    }

    public void testGetIsPrimitive()
    {
        assertTrue(reo.getIsPrimitive());
    }

    public void testGetIsStrictFP()
    {
        op.setIsStrictFP(true);
        assertTrue(reo.getIsStrictFP());
    }

    public void testGetMultiplicity()
    {
        ETList<IREMultiplicityRange> ranges = reo.getMultiplicity();
        assertEquals(1, ranges.size());
        IREMultiplicityRange r = ranges.get(0);
        assertEquals("0", r.getLower());
        assertEquals("2", r.getUpper());
    }

    public void testGetParameters()
    {
        ETList<IREParameter> pars = reo.getParameters();
        assertEquals(3, pars.size());
        assertEquals("dyke", pars.get(1).getName());
        assertEquals("seebeck", pars.get(2).getName());
    }

    public void testGetRaisedExceptions()
    {
        IStrings s = reo.getRaisedExceptions();
        assertEquals(1, s.getCount());
        assertEquals("IOException", s.get(0));
    }
}
