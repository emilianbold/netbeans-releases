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

import junit.textui.TestRunner;
/**
 * Test cases for Aggregation.
 */
public class AggregationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        TestRunner.run(AggregationTestCase.class);
    }
    
    private  IClass       aggregator, part;
    private  IAggregation aggregation;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        aggregator = createClass("Aggregator");
        part       = createClass("Part");
        
        aggregation = (IAggregation) relFactory.createAssociation2(aggregator, 
            part, AssociationKindEnum.AK_AGGREGATION, false, false, project);
        assertNotNull(aggregation);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        project.removeOwnedElement(aggregator);
        project.removeOwnedElement(part);
        aggregator.delete();
        part.delete();
    }
    
    public void testSetAggregateEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        aggregation.setAggregateEnd(end);
        
        assertNotNull(aggregation.getAggregateEnd());
        assertEquals(end.getXMIID(), aggregation.getAggregateEnd().getXMIID());
    }
    
    public void testGetAggregateEnd()
    {
        assertEquals(
            aggregator.getXMIID(),
            aggregation.getAggregateEnd().getParticipant().getXMIID());
    }
    
    public void testIsAggregateEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        aggregation.setAggregateEnd(end);
        assertTrue(aggregation.isAggregateEnd(end));
    }
    
    public void testSetAggregateEnd2()
    {
        IClass newAgg = createClass("NewAggregator");
        aggregation.setAggregateEnd2(newAgg);
        
        assertNotNull(aggregation.getAggregateEnd().getParticipant());
        assertEquals(
            newAgg.getXMIID(),
            aggregation.getAggregateEnd().getParticipant().getXMIID());
    }
    
    public void testReverseEnds()
    {
        aggregation.reverseEnds();
        assertEquals(aggregator.getXMIID(), 
            aggregation.getPartEnd().getParticipant().getXMIID());
        assertEquals(part.getXMIID(),
            aggregation.getAggregateEnd().getParticipant().getXMIID());
    }
    
    public void testSetIsComposite()
    {
        aggregation.setIsComposite(true);
        assertTrue(aggregation.getIsComposite());
        aggregation.setIsComposite(false);
        assertFalse(aggregation.getIsComposite());
    }
    
    public void testGetIsComposite()
    {
        // Tested by testSetIsComposite
    }
    
    public void testGetPartEnd()
    {
        assertNotNull(aggregation.getPartEnd());
        assertEquals(
            part.getXMIID(),
            aggregation.getPartEnd().getParticipant().getXMIID());
    }
    
    public void testSetPartEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        aggregation.setPartEnd(end);
        
        assertNotNull(aggregation.getPartEnd());
        assertEquals(end.getXMIID(), aggregation.getPartEnd().getXMIID());
    }
    
    public void testSetPartEnd2()
    {
        IClass newPart = createClass("NewPart");
        aggregation.setPartEnd2(newPart);
        
        assertNotNull(aggregation.getPartEnd().getParticipant());
        assertEquals(
            newPart.getXMIID(),
            aggregation.getPartEnd().getParticipant().getXMIID());
    }
    
    public void testTransformToAssociation()
    {
        IAssociation assoc = aggregation.transformToAssociation();
        assertNotNull(assoc);
    }
}
