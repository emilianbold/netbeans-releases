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
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.model.extensions.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.extensions.Util;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class SyncUpdateTest extends TestCase {
    private TestComponentListener listener;
    
    public SyncUpdateTest(String testName) {
        super(testName);
    }

    public static class TestComponentListener implements ComponentListener {
        ArrayList<ComponentEvent> events = new ArrayList<ComponentEvent>();
        
        public void valueChanged(ComponentEvent evt) {
            events.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            events.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            events.add(evt);
        }

        public void assertChangeEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.VALUE_CHANGED &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive ATTRIBUTE event on " + source, true);
        }
        
        public void assertChildAddedEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.CHILD_ADDED &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive CHILD_ADDED event on " + source, true);
        }

        public void assertChildRemovedEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.CHILD_REMOVED &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive CHILD_REMOVED event on " + source, true);
        }
        
        public void reset() { events.clear(); events = new ArrayList<ComponentEvent>(); }
    }
    
    protected void setUp() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
    }

    private WSDLModel setup(NamespaceLocation wsdl) throws Exception {
        WSDLModel m = TestCatalogModel.getDefault().getWSDLModel(wsdl);
        listener = new TestComponentListener();
        m.addComponentListener(listener);
        return m;
    }

    protected void tearDown() throws Exception {
        listener.reset();
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public <T extends ReferenceableWSDLComponent> T find(WSDLModel model, String name, Class<T> type) {
        return new FindReferencedVisitor<T>(model.getDefinitions()).find(name, type);
    }
    
    public void testSyncAddRemoveBPEL() throws Exception {
        WSDLModel model = setup(NamespaceLocation.VEHICLE);
        
        PartnerLinkType plt = find(model, "VehicleReservationPartnerLinkType", PartnerLinkType.class);
        Role role1 = plt.getRole1();
        Role role2 = plt.getRole2();
        
        Document doc = ((AbstractDocumentModel)model).getBaseDocument();
        Util.setDocumentContentTo(doc, "resources/Vehicle_PartnerLinkChanged.wsdl");
	model.sync();
        
        listener.assertChildRemovedEvent(plt);
        assertNull("role2 removed", plt.getRole2());
        
        // now do the reverse
        Util.setDocumentContentTo(doc, "resources/VehicleReservationService.wsdl");
        model.sync();
        
        listener.assertChildAddedEvent(plt);
        assertEquals("role2 added", "VehicleReservationCallbackServiceRole", plt.getRole2().getName());
    }

    public void testSyncUpdateRenameAndReferences() throws Exception {
        WSDLModel model = setup(NamespaceLocation.VEHICLE);
        
        PartnerLinkType plt = find(model, "VehicleReservationPartnerLinkType", PartnerLinkType.class);
        Role role1 = plt.getRole1();
        assertEquals("VehicleReservationServiceRole", role1.getName());
        NamedComponentReference<PortType> ptRef1 = role1.getPortType();
        assertNotNull(ptRef1);
        
        Util.setDocumentContentTo(model, "resources/Vehicle_PartnerLinkChanged2.wsdl");
        model.sync();
        
        listener.assertChangeEvent(role1);
        NamedComponentReference<PortType> ptRef = role1.getPortType();
        assertNotNull(ptRef);
        assertEquals("VehicleReservationPortType2", ptRef.get().getName());
    }
}
