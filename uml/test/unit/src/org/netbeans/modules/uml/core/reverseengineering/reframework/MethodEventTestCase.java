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

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * Test cases for MethodEvent.
 */
public class MethodEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MethodEventTestCase.class);
    }

    private MethodEvent me;
    private String      typeID;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IPrimitiveAction prim = createType("DestroyObjectAction");
        
        prim.getElementNode().addAttribute("instance", "Neo");
        prim.getElementNode().addAttribute("name", "One");

        IOperation op = createType("Operation");
        op.setReturnType2("cheese");
        typeID = op.getReturnType().getTypeID();
        
        Element opel = op.getElementNode();
        opel.detach();
        prim.getElementNode().add(opel);
        
        XMLManip.createElement(prim.getElementNode(), "UML:Class")
            .addAttribute("name", "Morpheus");
        
        IInputPin pin = createType("InputPin");
        pin.getElementNode().addAttribute("kind", "Type");
        pin.getElementNode().addAttribute("value", "fubar");
        
        prim.addArgument(pin);
        
        me = new MethodEvent();
        me.setEventData(prim.getNode());
    }

//    public void testGetArguments()
//    {
//        ETList<IREArgument> args = me.getArguments();
//        assertEquals(0, args.size());
//        assertEquals("fubar", args.get(0).getType());
//    }

    public void testGetDeclaringClassName()
    {
        assertEquals("Morpheus", me.getDeclaringClassName());
    }

    public void testGetInstanceName()
    {
        assertEquals("Neo", me.getInstanceName());
    }

    public void testGetMethodName()
    {
        assertEquals("One", me.getMethodName());
    }

    public void testGetOperation()
    {
        assertNotNull(me.getOperation());
    }

    public void testGetREClass()
    {
        assertNotNull(me.getREClass());
    }

    public void testGetResult()
    {
        assertEquals(typeID, me.getResult());
    }
}
