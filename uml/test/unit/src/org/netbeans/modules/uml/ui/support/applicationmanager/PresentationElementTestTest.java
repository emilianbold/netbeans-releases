
package org.netbeans.modules.uml.ui.support.applicationmanager;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADPresentationTypesMgrImpl;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;

/**
 *
 * @author Trey Spiva
 */
public class PresentationElementTestTest extends TestCase
{
    ADPresentationTypesMgrImpl m_TypesManager = new ADPresentationTypesMgrImpl();
    
    /**
     * Constructor for PresentationElementTestTest.
     * @param name
     */
    public PresentationElementTestTest(String name)
    {
        super(name);
    }
    
    public void testPresentationFileCreation()
    {
        m_TypesManager.createDefaultXMLFile("C:\\PresentationTest.xml");
    }
    
    public void testRetrievalOfButtonInfo()
    {
        String value = m_TypesManager.getButtonInitString("ID_VIEWNODE_UML_RECTANGLE",
            IDiagramKind.DK_CLASS_DIAGRAM);
        
        assertTrue("The button init string for ID_VIEWNODE_UML_RECTANGLE is not correct",
            value.equals("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_RECTANGLE"));
        
        String value2 = m_TypesManager.getButtonInitString("ID_VIEWNODE_UML_CLASS",
            IDiagramKind.DK_SEQUENCE_DIAGRAM);
        
        assertTrue("The button init string for ID_VIEWNODE_UML_CLASS is not correct",
            value2.equals("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class"));
    }
    
    public void testDefaultConnectorView()
    {
        String value = m_TypesManager.getDefaultConnectorView();
        assertTrue("The Default Connector view failed.", value.equals(""));
    }
    
    public void testDefaultLabelView()
    {
        String value = m_TypesManager.getDefaultLabelView();
        assertTrue("The Default Label view failed.", value.equals(""));
    }
    
    public void testInitStringDetails()
    {
        PresentationTypeDetails details = m_TypesManager.getInitStringDetails("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_RECTANGLE",
            IDiagramKind.DK_CLASS_DIAGRAM);
        
        assertTrue("The MetaType property for Graphic GST_RECTANGLE is not correct",
            details.getMetaType().equals("Graphic"));
        
        assertTrue("The DrawEngine property for Graphic GST_RECTANGLE is not correct",
            details.getEngineName().equals("GraphicDrawEngine"));
        
        assertTrue("The Object Kind property for Graphic GST_RECTANGLE is not correct",
            details.getObjectKind() == 5);
        
        PresentationTypeDetails details2 = m_TypesManager.getInitStringDetails("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI self",
            IDiagramKind.DK_SEQUENCE_DIAGRAM);
        
        assertTrue("The MetaType property for Graphic GST_RECTANGLE is not correct",
            details2.getMetaType().equals("SelfMessage"));
        
        assertTrue("The DrawEngine property for Graphic GST_RECTANGLE is not correct",
            details2.getEngineName().equals("LifelineDrawEngine"));
        
        assertTrue("The Object Kind property for Graphic GST_RECTANGLE is not correct",
            details2.getObjectKind() == 4);
    }
    
    public void testMetaTypeInitString()
    {
        String value = m_TypesManager.getMetaTypeInitString("Actor",
            IDiagramKind.DK_CLASS_DIAGRAM);
        
        assertTrue("The Metatype init string for Actor is not correct",
            value.equals("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor"));
        
        value = m_TypesManager.getMetaTypeInitString("ActivityEdge",
            IDiagramKind.DK_ACTIVITY_DIAGRAM);
        
        assertTrue("The Metatype init string for Actor is not correct",
            value.equals("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge"));
    }
    
    public void testOwnerMetaType()
    {
        String value = m_TypesManager.getOwnerMetaType("Attribute");
        assertTrue("The Owner Metatype for Attribute is not correct",
            value.equals("Class"));
        
        value = m_TypesManager.getOwnerMetaType("Operation");
        assertTrue("The Owner Metatype for Operation is not correct",
            value.equals("Class"));
    }
    
    public void testPresentationElementMetaType()
    {
        String value = m_TypesManager.getPresentationElementMetaType("AssociationEnd", "");
        assertTrue("The Presentation Element Metatype for AssociationEnd is not correct",
            value.equals("AssociationEdgePresentation"));
    }
    
    public void testVersionInformation()
    {
        assertTrue("Presentation Types Mgr Version is not correct.",
            m_TypesManager.getPresentationTypesMgrVersion().equals("1.0"));
        
        assertTrue("Presentation Types Mgr Version is not correct.",
            m_TypesManager.getVersion().equals("1.0"));
    }
    
    //**************************************************
    // Initaliztion Code.
    //**************************************************
    
    protected void setUp() throws Exception
    {
        ICoreProduct product = new ADProduct();
        CoreProductManager.instance().setCoreProduct(product);
        
        product.initialize();
    }
    
    protected void tearDown() throws Exception
    {
        // TODO Auto-generated method stub
        super.tearDown();
    }
    
}
