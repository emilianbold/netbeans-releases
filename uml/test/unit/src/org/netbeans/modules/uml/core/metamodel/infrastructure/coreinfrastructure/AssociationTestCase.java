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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 */
public class AssociationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AssociationTestCase.class);
    }
    
    private IAssociation assoc;
    private IClass first, second;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        first = createClass("First");
        second = createClass("Second");
        assoc = relFactory.createAssociation(first, second, project);
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
    }
    
    public void testGetAllParticipants()
    {
        ETList<IElement> parts = assoc.getAllParticipants();
        assertNotNull(parts);
        assertEquals(2, parts.size());
        assertEquals(first.getXMIID(), parts.get(0).getXMIID());
        assertEquals(second.getXMIID(), parts.get(1).getXMIID());
    }
    
    public void testRemoveEnd()
    {
        assoc.removeEnd(assoc.getEnds().get(0));
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(1, ends.size());
        assertEquals(
            second.getXMIID(),
            ends.get(0).getParticipant().getXMIID());
    }
    
    public void testAddEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        assoc.addEnd(end);
        
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(3, ends.size());
        assertEquals(end.getXMIID(), ends.get(2).getXMIID());
    }
    
    public void testAddEnd2()
    {
        IClass third = createClass("Third");
        assoc.addEnd2(third);
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(3, ends.size());
        assertEquals(
            third.getXMIID(),
            ends.get(2).getParticipant().getXMIID());
    }
    
    public void testAddEnd3()
    {
        // Note: this is no different from testAddEnd2
        
        IClass third = createClass("Third");
        assoc.addEnd3(third);
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(3, ends.size());
        assertEquals(
            third.getXMIID(),
            ends.get(2).getParticipant().getXMIID());
    }
    
    public void testGetEndIndex()
    {
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(2, ends.size());
        assertEquals(0, assoc.getEndIndex(ends.get(0)));
        assertEquals(1, assoc.getEndIndex(ends.get(1)));
    }
    
    public void testGetEnds()
    {
        // Tested by most methods upstairs.
    }
    

    public void testSetIsDerived()
    {
        assoc.setIsDerived(true);
        assertTrue(assoc.getIsDerived());
        assoc.setIsDerived(false);
        assertFalse(assoc.getIsDerived());
    }
    
    public void testGetIsDerived()
    {
        // Tested by testSetIsDerived
    }
    
    public void testGetIsReflexive()
    {
        assertFalse(assoc.getIsReflexive());
        // Point association back at first class.
        assoc.getEnds().get(1).setParticipant(first);
        assertEquals(
            first.getXMIID(),
            assoc.getEnds().get(0).getParticipant().getXMIID());
        assertTrue(assoc.getIsReflexive());
    }
    
    public void testGetNumEnds()
    {
        assertEquals(2, assoc.getNumEnds());
        assertEquals(assoc.getEnds().size(), assoc.getNumEnds());
    }
    
    public void testTransformToAggregation()
    {
        assertNotNull(assoc.transformToAggregation(false));
    }
}
