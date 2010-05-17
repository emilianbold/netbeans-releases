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


package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

/**
 * Test cases for Lifeline.
 */
public class LifelineTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LifelineTestCase.class);
    }

    private ILifeline life;
    private IInteraction lifeInter;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        life = createType("Lifeline");
        lifeInter = createType("Interaction");
        life.setInteraction(lifeInter);
    }
    
    public void testAddCoveringFragment()
    {
        IInteractionFragment frag = createType("CombinedFragment");
        life.addCoveringFragment(frag);
        assertEquals(1, life.getCoveringFragments().size());
        assertEquals(frag.getXMIID(), 
                life.getCoveringFragments().get(0).getXMIID());
    }

    public void testRemoveCoveringFragment()
    {
        testAddCoveringFragment();
        life.removeCoveringFragment(life.getCoveringFragments().get(0));
        assertEquals(0, life.getCoveringFragments().size());
    }

    public void testGetCoveringFragments()
    {
        // Tested by testAddCoveringFragment.
    }

    public void testCreateCreationalMessage()
    {
        // No code to test
//        ILifeline to = createType("Lifeline");
//        to.setInteraction((IInteraction) createType("Interaction"));
//        assertNotNull(life.createCreationalMessage(to));
    }

    public void testCreateDestructor()
    {
        assertNotNull( life.createDestructor() );
    }

    public void testSetDiscriminator()
    {
        IExpression e = createType("Expression");
        life.setDiscriminator(e);
        assertEquals(e.getXMIID(), life.getDiscriminator().getXMIID());
    }

    public void testGetDiscriminator()
    {
        // Tested by testSetDiscriminator.
    }

    public void testAddEvent()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        life.addEvent(eo);
        assertEquals(1, life.getEvents().size());
        assertEquals(eo.getXMIID(), life.getEvents().get(0).getXMIID());
    }

    public void testRemoveEvent()
    {
        testAddEvent();
        life.removeEvent(life.getEvents().get(0));
        assertEquals(0, life.getEvents().size());
    }

    public void testGetEvents()
    {
        // Tested by testAddEvent.
    }

    public void testSetInteraction()
    {
        IInteraction inter = createType("Interaction");
        life.setInteraction(inter);
        assertEquals(inter.getXMIID(), life.getInteraction().getXMIID());
    }

    public void testGetInteraction()
    {
        // Tested by testSetInteraction.
    }

    public void testCreateMessage()
    {
        IInteraction fromOwner = createType("Interaction"),
                     toOwner = createType("Interaction");
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction(toOwner);
        assertNotNull( life.createMessage(fromOwner, toElement, toOwner, 
                    (IOperation) createType("Operation"), 
                BaseElement.MK_SYNCHRONOUS) );
    }

    public void testInsertMessage()
    {
        IInteraction fromOwner = createType("Interaction"),
                     toOwner = createType("Interaction");
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction(toOwner);
        IMessage fromBefore = life.createMessage(fromOwner, toElement, toOwner, 
                    (IOperation) createType("Operation"), 
                BaseElement.MK_SYNCHRONOUS);
        
        assertNotNull( life.insertMessage(fromBefore, fromOwner, toElement, 
                toOwner, 
                (IOperation) createType("Operation"), 
                BaseElement.MK_SYNCHRONOUS) );
    }

    public void testDeleteMessage()
    {
        IInteraction fromOwner = createType("Interaction"),
                     toOwner = createType("Interaction");
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction(toOwner);
        IMessage m = life.createMessage(fromOwner, toElement, toOwner, 
                    (IOperation) createType("Operation"), 
                BaseElement.MK_SYNCHRONOUS);
        
        life.deleteMessage(m);
        
        // How do we verify this?
    }

    public void testAddPartDecomposition()
    {
        IPartDecomposition pd = createType("PartDecomposition");
        life.addPartDecomposition(pd);
        assertEquals(1, life.getPartDecompositions().size());
        assertEquals(pd.getXMIID(), life.getPartDecompositions().get(0).getXMIID());
    }

    public void testRemovePartDecomposition()
    {
        testAddPartDecomposition();
        life.removePartDecomposition(life.getPartDecompositions().get(0));
        assertEquals(0, life.getPartDecompositions().size());
    }

    public void testGetPartDecompositions()
    {
        // Tested by testAddPartDecomposition.
    }

    public void testSetRepresentingClassifierWithAlias()
    {
        createClass("Z");
        life.setRepresentingClassifierWithAlias("Z");
        assertEquals("Z", life.getRepresentingClassifier().getName());
    }

    public void testSetRepresentingClassifier()
    {
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        assertEquals(c.getXMIID(), life.getRepresentingClassifier().getXMIID());
    }

    public void testGetRepresentingClassifier()
    {
        // Tested by testSetRepresentingClassifier.
    }

    public void testSetRepresents()
    {
        ITypedElement tel = createType("Attribute");
        life.setRepresents(tel);
        assertEquals(tel.getXMIID(), life.getRepresents().getXMIID());
    }

    public void testGetRepresents()
    {
        // Tested by testSetRepresents.
    }

    public void testInitializeWith()
    {
        IClassifier c = createType("Class");
        life.initializeWith(c);
        assertEquals(c.getXMIID(), 
                life.getRepresents().getType().getXMIID());
    }

    public void testInitializeWithActor()
    {
        IActor a = createType("Actor");
        life.initializeWithActor(a);
        assertEquals(a.getXMIID(), 
                life.getRepresents().getXMIID());
    }

    public void testInitializeWithClass()
    {
        IClass c = createType("Class");
        life.initializeWithClass(c);
        assertEquals(c.getXMIID(), 
                life.getRepresents().getType().getXMIID());
    }

    public void testInitializeWithComponent()
    {
        IComponent c = createType("Component");
        life.initializeWithComponent(c);
        assertEquals(c.getXMIID(), 
                life.getRepresents().getType().getXMIID());
    }
}
