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
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 */
public class BehaviorTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(BehaviorTestCase.class);
    }
    
    private IBehavior behavior;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        behavior = (IBehavior)FactoryRetriever.instance().createType("Procedure", null);
        //behavior.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(behavior);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        project.removeElement(behavior);
        behavior.delete();
    }
    
    public void testSetContext()
    {
        assertNull(behavior.getContext());
        IClassifier context = createClass("Context");
        behavior.setContext(context);
        assertNotNull(behavior.getContext());
        assertEquals(context.getXMIID(), behavior.getContext().getXMIID());        
    }
    
    public void testGetContext()
    {
        // Tested by setContext.
    }
    
    public void testSetIsReentrant()
    {
        assertFalse(behavior.getIsReentrant());
        behavior.setIsReentrant(true);
        assertTrue(behavior.getIsReentrant());
        behavior.setIsReentrant(false);
        assertFalse(behavior.getIsReentrant());
    }
    
    public void testGetIsReentrant()
    {
        // Tested by setIsReentrant.
    }
    
    public void testAddParameter()
    {
        IParameter par = factory.createParameter(null);
        behavior.addParameter(par);
        
        ETList<IParameter> pars = behavior.getParameters();
        assertNotNull(pars);
        assertEquals(1, pars.size());
        assertEquals(par.getXMIID(), pars.get(0).getXMIID());
    }
    
    public void testRemoveParameter()
    {
        testAddParameter();
        behavior.removeParameter(behavior.getParameters().get(0));
        ETList<IParameter> pars = behavior.getParameters();
        assertTrue(pars == null || pars.size() == 0);
    }
    
    public void testGetParameters()
    {
        // Tested by testAddParameter.
    }
    public void testSetRepresentedFeature()
    {
        IClass clazz = createClass("First");
        IOperation oper = factory.createOperation(clazz);
        clazz.addOperation(oper);
        behavior.setRepresentedFeature(oper);
        assertNotNull(behavior.getRepresentedFeature());
        assertEquals(
            oper.getXMIID(),
            behavior.getRepresentedFeature().getXMIID());
    }
    
    public void testGetRepresentedFeature()
    {
        // Tested by setRepresentedFeature.
    }
    
    public void testSetSpecification()
    {
        IClass clazz = createClass("First");
        IOperation oper = factory.createOperation(clazz);
        clazz.addOperation(oper);
        behavior.setSpecification(oper);
        assertNotNull(behavior.getSpecification());
        assertEquals(oper.getXMIID(), behavior.getSpecification().getXMIID());
    }
    
    public void testGetSpecification()
    {
        // Tested by setSpecification.
    }
}
