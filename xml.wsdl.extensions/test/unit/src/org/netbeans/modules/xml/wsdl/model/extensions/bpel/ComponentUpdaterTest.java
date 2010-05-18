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

    public void NO_testCanPasteAll_Travel() throws Exception {
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
    
    public void NO_testCanPasteAll_Airline() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void NO_testCanPasteAll_Hotel() throws Exception {
        model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.HOTEL);
        recursiveCanPasteChildren(model.getDefinitions());
        recursiveCannotPasteChildren(model.getDefinitions());
    }
    
    public void NO_testCanPasteAll_Vehicle() throws Exception {
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
