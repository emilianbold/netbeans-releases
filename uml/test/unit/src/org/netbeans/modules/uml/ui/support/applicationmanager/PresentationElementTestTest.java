/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
