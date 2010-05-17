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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Test cases for RelationFactory.
 */
public class RelationFactoryTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RelationFactoryTestCase.class);
    }
    
    private IClassifier first, second;
    private IInterface  i1, i2;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        // TODO Auto-generated method stub
        super.setUp();
        
        first = createClass("First");
        second = createClass("Second");
        
        i1 = createInterface("I1");
        i2 = createInterface("I2");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        first.delete();
        second.delete();
        i1.delete();
        i2.delete();
    }
    
    public void testDetermineCommonRelations()
    {
        IAssociation assoc = relFactory.createAssociation(first, second, project);
        ETList<IElement> els = new ETArrayList<IElement>();
        els.add(first);
        els.add(second);
        ETList<IRelationProxy> rels = relFactory.determineCommonRelations(els);
        assertEquals(1, rels.size());
    }
    
    public void testCreateAssociation()
    {
        assertNotNull(relFactory.createAssociation(first, second, project));
    }
    
    public void testCreateAssociation2()
    {
        assertNotNull(relFactory.createAssociation2(first, second,
            AssociationKindEnum.AK_ASSOCIATION, false, false, project));
        assertTrue(relFactory.createAssociation2(first, second,
            AssociationKindEnum.AK_AGGREGATION, false, false, project)
            instanceof IAggregation);
        assertNotNull(relFactory.createAssociation2(first, second,
            AssociationKindEnum.AK_COMPOSITION, false, false, project));
    }
    
    public void testDetermineCommonRelations2()
    {
        // TODO: Can't test this until we have diagrams working.
    }
    
    public void testDetermineCommonRelations3()
    {
        // TODO: We probably have to get diagrams working before we can test
        // this.
        IAssociation assoc = relFactory.createAssociation(first, second, project);
        ETList<IElement> els = new ETArrayList<IElement>();
        els.add(first);
        els.add(second);
        ETList<IRelationProxy> rels = relFactory.determineCommonRelations3(els,
            els);
        assertEquals(1, rels.size());
        assertEquals(assoc.getXMIID(), rels.get(0).getConnection().getXMIID());
    }
    
    public void testCreateDependency()
    {
        assertNotNull(relFactory.createDependency(first, second, project));
    }
    
    public void testCreateDependency2()
    {
        assertNotNull(relFactory.createDependency2(first, i1, "Usage", project));
    }
    
    public void testCreateDerivation()
    {
        second.addTemplateParameter(createClass("T"));
        assertNotNull(relFactory.createDerivation(first, second));
    }
    
    public void testCreateGeneralization()
    {
        assertNotNull(relFactory.createGeneralization(first, second));
    }
    
    public void testCreateImplementation()
    {
        assertNotNull(relFactory.createImplementation(first, i1, project).getParamTwo());
    }
    
    public void testCreateImport()
    {
        assertNotNull(relFactory.createImport(first, second));
    }
    
    public void testCreatePresentationReference()
    {
        // TODO: We'll want to code this once presentation elements and diagrams
        //       are fully coded.
//        assertNotNull(relFactory.createPresentationReference(first, null));
    }
    
    public void testCreateReference()
    {
        assertNotNull(relFactory.createReference(first, second));
    }
}
