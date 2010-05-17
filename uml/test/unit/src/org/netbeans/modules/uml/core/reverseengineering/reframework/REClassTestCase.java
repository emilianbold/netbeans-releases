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
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;

/**
 * Test cases for REClass.
 */
public class REClassTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClassTestCase.class);
    }

    private REClass rec;
    private IClass  c;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rec = new REClass();
        c = createClass("P");
        
        IClass a = createClass("A");
        IClass b = createClass("B");

        c.addOwnedElement(a);
        c.addOwnedElement(b);
        c.addAttribute(c.createAttribute("int", "a"));
        c.addAttribute(c.createAttribute("char", "b"));
        c.setIsAbstract(true);

        rec.setEventData(c.getNode());
        
        Element e = c.getElementNode();
        element = e;
        
        addToken("Package", "cucumber::anemone");
        Element tds = XMLManip.createElement(e, "TokenDescriptors");
        Element r  = XMLManip.createElement(tds, "TRealization");
        Element i  = XMLManip.createElement(r, "Interface");
        i.addAttribute("value", "Real");
        r  = XMLManip.createElement(tds, "TGeneralization");
        i  = XMLManip.createElement(r, "SuperClass");
        i.addAttribute("value", "Unreal");
    }
    
    public void testGetGeneralizations()
    {
        IREGeneralization regs = rec.getGeneralizations();
        assertEquals(1, regs.getCount());
        assertEquals("Unreal", regs.item(0).getName());
    }
    
    public void testGetRealizations()
    {
        assertEquals("Real", rec.getRealizations().item(0).getName());
    }

    public void testGetPackage()
    {
        IPackage p = createType("Package");
        p.addOwnedElement(c);
        assertEquals("cucumber::anemone", rec.getPackage());
    }

    public void testGetAllInnerClasses()
    {
        ETList<IREClass> cs = rec.getAllInnerClasses();
        assertEquals(2, cs.size());
        assertEquals("A", cs.get(0).getName());
        assertEquals("B", cs.get(1).getName());
    }

    public void testGetAttributes()
    {
        ETList<IREAttribute> cs = rec.getAttributes();
        assertEquals(2, cs.size());
        assertEquals("a", cs.get(0).getName());
        assertEquals("b", cs.get(1).getName());
    }

    public void testGetIsAbstract()
    {
        assertTrue(rec.getIsAbstract());
    }

    public void testGetIsInterface()
    {
        assertFalse(rec.getIsInterface());
    }

    public void testGetIsLeaf()
    {
        c.setIsAbstract(false);
        c.setIsLeaf(true);
        assertTrue(rec.getIsLeaf());
    }

    public void testGetOperations()
    {
        c.addOperation(c.createOperation("float", "a"));
        c.addOperation(c.createOperation("double", "b"));
        ETList<IREOperation> ops = rec.getOperations();
        
        // 7 = 4 accessors for the two existing attributes, plus constructor 
        // and the two operations we created above.
        // assertEquals(7, ops.size());
        // assertEquals("a", ops.get(5).getName());
        // assertEquals("b", ops.get(6).getName());
        // IZ=119824 - conover
        // something must have changed because there is never any operations
        // until the above two are added.
        assertEquals(2, ops.size());
        assertEquals("a", ops.get(0).getName());
        assertEquals("b", ops.get(1).getName());
    }
}
