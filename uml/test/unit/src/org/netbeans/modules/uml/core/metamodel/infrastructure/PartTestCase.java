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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;

/**
 * Test cases for Part.
 */
public class PartTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PartTestCase.class);
    }

    private IPart part;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        part = factory.createPart(null);
        project.addElement(part);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        part.delete();
    }
    
    public void testSetDefiningFeature()
    {
        IClass cl = createClass("D");
        IAttribute at = cl.createAttribute("int", "k");
        cl.addAttribute(at);

        part.setDefiningFeature(at);
        assertEquals(at.getXMIID(), part.getDefiningFeature().getXMIID());
    }

    public void testGetDefiningFeature()
    {
        // Tested by testSetDefiningFeature.
    }

    public void testSetInitialCardinality()
    {
        part.setInitialCardinality(10);
        assertEquals(10, part.getInitialCardinality());
        part.setInitialCardinality(1);
        assertEquals(1, part.getInitialCardinality());
    }

    public void testGetInitialCardinality()
    {
        // Tested by testSetInitialCardinality.
    }

    public void testSetIsWhole()
    {
        assertFalse(part.getIsWhole());
        part.setIsWhole(true);
        assertTrue(part.getIsWhole());
        part.setIsWhole(false);
        assertFalse(part.getIsWhole());
    }

    public void testGetIsWhole()
    {
        // Tested by testSetIsWhole.
    }

    public void testSetPartKind()
    {
        part.setPartKind(BaseElement.PK_AUXILIARY);
        assertEquals(BaseElement.PK_AUXILIARY, part.getPartKind());
        part.setPartKind(BaseElement.PK_INTERFACEIMPLEMENTATION);
        assertEquals(BaseElement.PK_INTERFACEIMPLEMENTATION, part.getPartKind());
    }

    public void testGetPartKind()
    {
        // Tested by testSetPartKind.
    }
}
