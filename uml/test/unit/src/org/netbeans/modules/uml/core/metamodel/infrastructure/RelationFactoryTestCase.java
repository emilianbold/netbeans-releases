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