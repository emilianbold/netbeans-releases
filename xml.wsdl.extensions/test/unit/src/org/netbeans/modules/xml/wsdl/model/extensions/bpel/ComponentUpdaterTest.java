/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel;

import java.util.ArrayList;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.model.extensions.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.CorrelationPropertyImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.DocumentationImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.QueryImpl;

/**
 *
 * @author Nam Nguyen
 */
public class ComponentUpdaterTest extends TestCase {
    private WSDLModel model;
    private Definitions definitions;
    
    public ComponentUpdaterTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ComponentUpdaterTest.class);
        return suite;
    }

    public void testRemoveAll_Travel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        definitions = model.getDefinitions();
    }

    public void testRemoveAll_Airline() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        definitions = model.getDefinitions();
    }

    public void testRemoveAll_Hotel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.HOTEL);
        definitions = model.getDefinitions();
    }

    public void testRemoveAll_Vehicle() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        definitions = model.getDefinitions();
    }

    static void checkRemoveAll(WSDLComponent target) throws Exception {
        target.getModel().startTransaction();
        recursiveRemoveChildren(target);
        assertEquals("children removed", 0, target.getChildren().size());
        target.getModel().endTransaction();
    }
    
    static void recursiveRemoveChildren(WSDLComponent target) {
        WSDLModel model = target.getModel();
        ArrayList<WSDLComponent> children = new ArrayList<WSDLComponent>(target.getChildren());
        for (WSDLComponent child : children) {
            recursiveRemoveChildren(child);
        }
        if (target.getParent() != null) {
            model.removeChildComponent(target);
        }
    }

    public void testCanPasteAll_Travel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void testCanNotPaste_Airline() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        PartnerLinkType plt = model.getDefinitions().getExtensibilityElements(PartnerLinkType.class).get(0);
        Role role1 = plt.getRole1();
        assertFalse(role1.canPaste(plt));
        assertFalse(model.getDefinitions().canPaste(new QueryImpl(model)));
        assertFalse(model.getDefinitions().getTypes().canPaste(new CorrelationPropertyImpl(model)));
    }
    
    public void testCanPasteAll_Airline() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void testCanPasteAll_Hotel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.HOTEL);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void testCanPasteAll_Vehicle() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public static void recursiveCanPasteChildren(WSDLComponent target) {
        WSDLModel model = target.getModel();
        ArrayList<WSDLComponent> children = new ArrayList<WSDLComponent>(target.getChildren());
        for (WSDLComponent child : children) {
            recursiveCanPasteChildren(child);
        }
        if (target.getParent() != null) {
            assertTrue(target.getParent().canPaste(target));
        }
    }

    public static void recursiveCannotPasteChildren(WSDLComponent target) {
        WSDLModel model = target.getModel();
        ArrayList<WSDLComponent> children = new ArrayList<WSDLComponent>(target.getChildren());
        for (WSDLComponent child : children) {
            recursiveCannotPasteChildren(child);
        }
        if (target.getParent() != null) {
            String msg = target.getClass().getName() + " canPaste " + target.getParent().getClass().getName();
            assertFalse(msg, target.canPaste(target.getParent()));
        }
    }
}
