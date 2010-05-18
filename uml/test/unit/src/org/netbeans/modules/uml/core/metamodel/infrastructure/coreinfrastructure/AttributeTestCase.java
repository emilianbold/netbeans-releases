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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

/**
 */
public class AttributeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AttributeTestCase.class);
    }
    
    private IClass     clazz;
    private IAttribute attr;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        clazz = createClass("Classe");
        attr  = clazz.createAttribute("float", "burger");
        clazz.addAttribute(attr);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        clazz.removeElement(attr);
        attr.delete();
        project.removeElement(clazz);
        clazz.delete();
    }
    
    public void testSetAssociationEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        project.addElement(end);
        attr.setAssociationEnd(end);
        assertNotNull(attr.getAssociationEnd());
        assertEquals(end.getXMIID(), attr.getAssociationEnd().getXMIID());
    }
    
    public void testGetAssociationEnd()
    {
        // Tested by testSetAssociationEnd
    }
    
    public void testSetDefault()
    {
        IExpression expr = factory.createExpression(project);
        attr.setDefault(expr);
        assertNotNull(attr.getDefault());
        assertEquals(expr.getXMIID(), attr.getDefault().getXMIID());
    }
    
    public void testGetDefault()
    {
        // Tested by testSetDefault()
    }
    
    public void testSetDefault2()
    {
        attr.setDefault2("10.0");
        assertEquals("10.0", attr.getDefault2());
    }
    
    public void testGetDefault2()
    {
        // Tested by testSetDefault2
    }
    
    public void testSetDefault3()
    {
        attr.setDefault3("java", "5.0");
        assertEquals(new ETPairT<String,String>("java", "5.0"), attr.getDefault3());
    }
    
    public void testGetDefault3()
    {
        // Tested by testSetDefault3
    }
    
    public void testSetDerivationRule()
    {
        IExpression expr = factory.createExpression(null);
        attr.setDerivationRule(expr);
        assertNotNull(attr.getDerivationRule());
        assertEquals(expr.getXMIID(), attr.getDerivationRule().getXMIID());
    }
    
    public void testGetDerivationRule()
    {
        // Tested by testSetDerivationRule
    }
    
    public void testSetHeapBased()
    {
        assertFalse(attr.getHeapBased());
        attr.setHeapBased(true);
        assertTrue(attr.getHeapBased());
        attr.setHeapBased(false);
        assertFalse(attr.getHeapBased());
    }
    
    public void testGetHeapBased()
    {
        // Tested by testSetHeapBased
    }
    
    public void testSetIsDerived()
    {
        assertFalse(attr.getIsDerived());
        attr.setIsDerived(true);
        assertTrue(attr.getIsDerived());
        attr.setIsDerived(false);
        assertFalse(attr.getIsDerived());
    }
    
    public void testGetIsDerived()
    {
        // Tested by testSetIsDerived
    }
    
    public void testSetIsPrimaryKey()
    {
        assertFalse(attr.getIsPrimaryKey());
        attr.setIsPrimaryKey(true);
        assertTrue(attr.getIsPrimaryKey());
        attr.setIsPrimaryKey(false);
        assertFalse(attr.getIsPrimaryKey());
    }
    
    public void testGetIsPrimaryKey()
    {
        // Tested by testSetIsPrimaryKey
    }
    
    public void testSetIsWithEvents()
    {
        assertFalse(attr.getIsWithEvents());
        attr.setIsWithEvents(true);
        assertTrue(attr.getIsWithEvents());
        attr.setIsWithEvents(false);
        assertFalse(attr.getIsWithEvents());
    }
    
    public void testGetIsWithEvents()
    {
        // Tested by setIsWithEvents.
    }
}
