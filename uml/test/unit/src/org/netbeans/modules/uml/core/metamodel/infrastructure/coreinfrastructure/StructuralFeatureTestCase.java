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

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for StructuralFeature.
 */
public class StructuralFeatureTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(StructuralFeatureTestCase.class);
    }

    private IStructuralFeature feat;
    private IClassifier c;
    
    protected void setUp() throws Exception
    {
        super.setUp();
     
        c = createClass("Czar");
        IAttribute at = c.createAttribute("char", "dz");
        c.addAttribute(at);
        feat = at;
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        feat.delete();
        c.delete();
    }

    public void testSetClientChangeability()
    {
        feat.setClientChangeability(0);
        assertEquals(0, feat.getClientChangeability());
        feat.setClientChangeability(1);
        assertEquals(1, feat.getClientChangeability());
    }
    
    public void testGetClientChangeability()
    {
        // Tested by setClientChangeability.
    }
    
    public void testSetIsTransient()
    {
        assertFalse(feat.getIsTransient());
        feat.setIsTransient(true);
        assertTrue(feat.getIsTransient());
        feat.setIsTransient(false);
        assertFalse(feat.getIsTransient());
    }
    
    public void testGetIsTransient()
    {
        // Tested by setIsTransient.
    }
    
    public void testSetIsVolatile()
    {
        assertFalse(feat.getIsVolatile());
        feat.setIsVolatile(true);
        assertTrue(feat.getIsVolatile());
        feat.setIsVolatile(false);
        assertFalse(feat.getIsVolatile());
    }
    
    public void testGetIsVolatile()
    {
        // Tested by setIsVolatile.
    }
    
//    public void testSetMultiplicity()
//    {
//        IMultiplicity mul = factory.createMultiplicity(null);
//        feat.setMultiplicity(mul);
//        
//        assertEquals(mul.getXMIID(), feat.getMultiplicity().getXMIID());
//    }
    
    public void testGetMultiplicity()
    {
        // Tested by setMultiplicity.
    }
    
    public void testSetOrdering()
    {
        feat.setOrdering(0);
        assertEquals(0, feat.getOrdering());
        feat.setOrdering(1);
        assertEquals(1, feat.getOrdering());
    }
    
    public void testGetOrdering()
    {
        // Tested by setOrdering.
    }
    
    public void testSetType2()
    {
        feat.setType2("ANSI");
        assertEquals("ANSI", feat.getTypeName());
    }
    public void testSetTypeName()
    {
        feat.setTypeName("ANSI");
        assertEquals("ANSI", feat.getTypeName());
    }
    
    public void testGetTypeName()
    {
        // Tested by setTypeName.
    }
    
    public void testSetType()
    {
        IClassifier cz = createClass("NewType");
        feat.setType(cz);
        assertEquals(cz.getXMIID(), feat.getType().getXMIID());
    }
    
    public void testGetType()
    {
        // Tested by setType.
    }
}
