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

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for RECallAction.
 */
public class RECallActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RECallActionTestCase.class);
    }

    private RECallAction reca;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reca = new RECallAction();
        
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:CallAction");
        el.addAttribute("operation", "tumbleweed");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");

        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Roger");
        tdp.addAttribute("type", "OperationOwner");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Zelazny");
        tdp.addAttribute("type", "Name");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Angband");
        tdp.addAttribute("type", "ContainingClass");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "StaticInstance");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "StaticOperation");

        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Zebra");
        tdp.addAttribute("type", "ReturnTypeClass");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "ungulate");
        tdp.addAttribute("type", "ReturnTypePackage");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Name");
        tdp.addAttribute("type", "ReturnType");
        
        reca.setEventData(el);
    }
    
    public void testGetImplementingClass()
    {
        assertEquals("Roger", reca.getImplementingClass());
    }

    public void testGetInstanceName()
    {
        assertEquals("Zelazny", reca.getInstanceName());
    }

    public void testGetInstanceOwner()
    {
        assertEquals("Angband", reca.getInstanceOwner());
    }

    public void testGetIsInstanceStatic()
    {
        assertTrue(reca.getIsInstanceStatic());
    }

    public void testGetIsOperationStatic()
    {
        assertTrue(reca.getIsOperationStatic());
    }

    public void testGetOperationName()
    {
        assertEquals("tumbleweed", reca.getOperationName());
    }

    public void testGetReturnTypeClass()
    {
        assertEquals("Zebra", reca.getReturnTypeClass());
    }

    public void testGetReturnTypePackage()
    {
        assertEquals("ungulate", reca.getReturnTypePackage());
    }

    public void testGetReturnType()
    {
        assertEquals("Name", reca.getReturnType());
    }
}
