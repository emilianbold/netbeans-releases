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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for AssociationEnd.
 */
public class AssociationEndTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AssociationEndTestCase.class);
    }

    private IAssociation assoc;
    private IClass first, second;
    private IAssociationEnd end;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        first = createClass("First");
        second = createClass("Second");
        assoc = relFactory.createAssociation(first, second, project);

        end = assoc.getEnds().get(0);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();

        project.removeElement(first);
        project.removeElement(second);
        project.removeElement(assoc);
        first.delete();
        second.delete();
        assoc.delete();
        end = null;
    }
    
    public void testGetAssociation()
    {
        assertNotNull(end.getAssociation());
        assertEquals(assoc.getXMIID(), end.getAssociation().getXMIID());
    }
    
    public void testSetAssociation()
    {
        IClass third = createClass("Third");
        IClass fourth = createClass("Fourth");
        IAssociation assoc2 =
            relFactory.createAssociation(third, fourth, project);
        end.setAssociation(assoc2);
        
        assertNotNull(end.getAssociation());
        assertEquals(assoc2.getXMIID(), end.getAssociation().getXMIID());
    }
    
    public void testGetIsNavigable()
    {
        assertFalse(end.getIsNavigable());
        end = end.makeNavigable();
        assertTrue(end.getIsNavigable());
    }
    
    public void testMakeNavigable()
    {
        // Tested by testGetIsNavigable
    }
    
    public void testGetOtherEnd()
    {
        ETList<IAssociationEnd> other = end.getOtherEnd();
        assertNotNull(other);
        assertEquals(1, other.size());
        assertEquals(assoc.getEnds().get(1).getXMIID(), other.get(0).getXMIID());
    }
    
    public void testGetOtherEnd2()
    {
        IAssociationEnd other = end.getOtherEnd2();
        assertNotNull(other);
        assertEquals(assoc.getEnds().get(1).getXMIID(), other.getXMIID());
    }
    
    public void testSetParticipant()
    {
        IClass jefferson = createClass("Jefferson");
        end.setParticipant(jefferson);
        assertNotNull(end.getParticipant());
        assertEquals(jefferson.getXMIID(), end.getParticipant().getXMIID());
    }
    
    public void testGetParticipant()
    {
        // Tested by testSetParticipant
    }
    
    public void testCreateQualifier()
    {
        IAttribute qualifier = end.createQualifier("float", "bizarre");
        assertNotNull(qualifier);
        assertEquals("bizarre", qualifier.getName());
    }
    
    public void testAddQualifier()
    {
        IAttribute qualifier = end.createQualifier("float", "bizarre");
        end.addQualifier(qualifier);
        
        ETList<IAttribute> quals = end.getQualifiers();
        assertNotNull(quals);
        assertEquals(1, quals.size());
        assertEquals(qualifier.getXMIID(), quals.get(0).getXMIID());
    }
    
    public void testRemoveQualifier()
    {
        testAddQualifier();
        end.removeQualifier(end.getQualifiers().get(0));
        assertTrue(
            end.getQualifiers() == null || end.getQualifiers().size() == 0);
    }
    
    public void testCreateQualifier2()
    {
        IClass ike;
        IAttribute qualifier = end.createQualifier2(ike = createClass("Ike"), 
                                                    "p");
        assertNotNull(qualifier);
        end.addQualifier(qualifier);
        assertEquals("p", qualifier.getName());
        assertEquals(ike.getXMIID(), qualifier.getType().getXMIID());
    }
    
    public void testCreateQualifier3()
    {
        IAttribute qual = end.createQualifier3();
        assertNotNull(qual);
    }

    public void testGetQualifiers()
    {
        // Tested by preceding qualifier methods.
    }
    
    
    public void testIsSameParticipant()
    {
        assertTrue(end.isSameParticipant(first));
        assertFalse(end.isSameParticipant(second));
    }
}
