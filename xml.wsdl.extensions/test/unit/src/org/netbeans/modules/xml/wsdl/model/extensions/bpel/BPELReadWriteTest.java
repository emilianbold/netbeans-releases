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
import java.util.Collection;

import junit.framework.TestCase;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.model.extensions.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.extensions.Util;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl.PartnerLinkTypeImpl;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class BPELReadWriteTest extends TestCase {
    
    public BPELReadWriteTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testRead() throws Exception {
        NamespaceLocation.VEHICLE.refreshResourceFile();
        NamespaceLocation.HOTEL.refreshResourceFile();
        NamespaceLocation.AIRLINE.refreshResourceFile();
        NamespaceLocation.OTA.refreshResourceFile();
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        checkRead(model);
    }
    
    private void checkRead(WSDLModel model) throws Exception {
        Definitions d = model.getDefinitions();
        
        //<bpws:property name="ItineraryRefId" type="xs:string" /> 
        Collection<CorrelationProperty> ees = d.getExtensibilityElements(CorrelationProperty.class);
        CorrelationProperty cp = ees.iterator().next();
        assertEquals("CorrelationProperty.name", "ItineraryRefId", cp.getName());
        NamedComponentReference<GlobalType> typeRef = ees.iterator().next().getType();
        assertEquals("CorrelationProperty.type", "http://www.w3.org/2001/XMLSchema", typeRef.getEffectiveNamespace());
        GlobalType type = typeRef.get();
        assertEquals("orrelationProperty.type.name", "string", type.getName());
        
        //<bpws:propertyAlias propertyName="tres:ItineraryRefId" messageType="ares:ReserveAirlineIn" 
        //     part="itinerary" query="/ota:TravelItinerary/ota:ItineraryRef/ota:UniqueID"/>
        
        Collection<PropertyAlias> pas = d.getExtensibilityElements(PropertyAlias.class);
        assertEquals("PropertyAlias.count", 7, pas.size());
        ArrayList<PropertyAlias> pasl = new ArrayList<PropertyAlias>(pas);
        PropertyAlias pa = pasl.get(2);
        assertEquals("PropertyAlias.property", d.getTargetNamespace(), pa.getPropertyName().getEffectiveNamespace());
        AbstractDocumentComponent ac = (AbstractDocumentComponent) pa;
        assertEquals("property prefix", BPELQName.VPROP_PREFIX, ac.getPeer().getPrefix());
        NamedComponentReference<CorrelationProperty> gr = pa.getPropertyName();
        assertEquals("PropertyAlias.propertyName", "tres:ItineraryRefId", gr.getRefString());
        NamedComponentReference<Message> msgRef = pa.getMessageType();
        assertEquals("PropertyAlias.messageType", "ares:ReserveAirlineIn", msgRef.getRefString());
        WSDLModel airModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        Message resairIn = FindWSDLComponent.findComponent(Message.class, airModel.getDefinitions(), "/definitions/message[@name='ReserveAirlineIn']");
        assertEquals("PropertyAlias.property", cp, pa.getPropertyName().get());
        assertEquals("PropertyAlias.messageType", resairIn.getName(), pa.getMessageType().get().getName());
        assertEquals("PropertyAlias.part", "itinerary", pa.getPart());
        Query q = pa.getQuery();
        assertEquals("PropertyAlias.query", "/TravelItinerary/ItineraryRef/UniqueID", q.getContent());
	
        //<plnk:partnerLinkType name="TravelReservationPartnerLinkType">
        //    <plnk:role name="TravelReservationServiceRole">
        //    <plnk:portType name="tres:TravelReservationPortType"/>
        PartnerLinkType plt = d.getExtensibilityElements(PartnerLinkType.class).iterator().next();
        assertEquals("partnerLinkType.name", "TravelReservationPartnerLinkType", plt.getName());
        Role role1 = plt.getRole1();
        assertEquals("partnerLinkType.role1.name", "TravelReservationServiceRole", role1.getName());
        NamedComponentReference<PortType> ptRef = role1.getPortType();
        PortType pt = d.getPortTypes().iterator().next();
        assertEquals("partnerLinkType.role1.portType", pt, ptRef.get());
        assertEquals("element prefix", "plnk", ((PartnerLinkTypeImpl)plt).getPeer().getPrefix());
        assertNotNull("definitions.xmlns."+BPELQName.PLNK_PREFIX, ((AbstractDocumentComponent)d).getPrefixes().get(BPELQName.PLNK_PREFIX));
        assertNotNull("definitions.xmlns."+BPELQName.VPROP_PREFIX, ((AbstractDocumentComponent)d).getPrefixes().get(BPELQName.VPROP_PREFIX));
    }

    private PropertyAlias addPropertyAlias(BPELComponentFactory fact, Definitions d, CorrelationProperty property, Message messageType) {
        PropertyAlias pa = fact.createPropertyAlias(d);
        d.addExtensibilityElement(pa);
        pa.setPropertyName(pa.createReferenceTo(property, CorrelationProperty.class));;
        pa.setMessageType(pa.createReferenceTo(messageType, Message.class));
        pa.setPart("itinerary");
        Query q = new BPELComponentFactory(pa.getModel()).createQuery(pa);
        q.setContent("/TravelItinerary/ItineraryRef/UniqueID");
        pa.setQuery(q);
        return pa;
    }
    
    private void doWrite(WSDLModel model) throws Exception {
        BPELComponentFactory fact = new BPELComponentFactory(model);
        Definitions d = model.getDefinitions();
        d.setTargetNamespace(NamespaceLocation.TRAVEL.getNamespace());
        
        Import im = model.getFactory().createImport(); d.addImport(im);
        im.setNamespace(NamespaceLocation.AIRLINE.getNamespace());
        im.setLocation(NamespaceLocation.AIRLINE.getLocation());
        im = model.getFactory().createImport(); d.addImport(im);
        im.setNamespace(NamespaceLocation.HOTEL.getNamespace());
        im.setLocation(NamespaceLocation.HOTEL.getLocation());
        im = model.getFactory().createImport(); d.addImport(im);
        im.setNamespace(NamespaceLocation.VEHICLE.getNamespace());
        im.setLocation(NamespaceLocation.VEHICLE.getLocation());
        
        Types types = model.getFactory().createTypes(); d.setTypes(types);
        WSDLSchema wschema = model.getFactory().createWSDLSchema();
        types.addExtensibilityElement(wschema);
        Schema emSchema = wschema.getSchemaModel().getSchema();
        emSchema.setTargetNamespace("http://www.sun.com/javaone/05/TravelReservationService");
        GlobalElement ge = wschema.getSchemaModel().getFactory().createGlobalElement();
        ge.setName("itineraryFault"); 
        ge.setType(ge.createReferenceTo(Util.getPrimitiveType("string"), GlobalSimpleType.class));
        emSchema.addElement(ge);
        
        Message inMsg = model.getFactory().createMessage(); d.addMessage(inMsg); 
        inMsg.setName("ItineraryIn");
        Message outMsg = model.getFactory().createMessage(); d.addMessage(outMsg);
        outMsg.setName("ItineraryOut");
        
        //<portType name="TravelReservationPortType">
        //<operation name="buildItinerary">
        //    <input message="tns:ItineraryIn"/>
        //    <output message="tns:ItineraryOut"/>
        //    <fault name="itineraryProblem" message="tns:ItineraryFault"/>
        PortType pt = model.getFactory().createPortType();
        d.addPortType(pt); pt.setName("TravelReservationPortType");
        RequestResponseOperation rro = model.getFactory().createRequestResponseOperation();
        pt.addOperation(rro); rro.setName("buildItinerary");
        Input in = model.getFactory().createInput(); rro.setInput(in);
        in.setMessage(in.createReferenceTo(inMsg, Message.class));
        
        Output out = model.getFactory().createOutput(); rro.setOutput(out);
        out.setMessage(out.createReferenceTo(outMsg, Message.class));

        Fault fault = model.getFactory().createFault(); rro.addFault(fault);
        Message faultMsg = model.getFactory().createMessage(); faultMsg.setName("ItineraryFault");
        fault.setMessage(fault.createReferenceTo(faultMsg, Message.class));
        
        SchemaModel schemaModel = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.OTA);
        Schema primitives = schemaModel.findSchemas("http://www.w3.org/2001/XMLSchema").iterator().next();
        GlobalSimpleType stringType = FindSchemaComponentFromDOM.find(
                GlobalSimpleType.class, primitives, "/schema/simpleType[@name='string']");
        //<bpws:property name="ItineraryRefId" type="xs:string" /> 
        CorrelationProperty cp = fact.createCorrelationProperty(d);
        d.addExtensibilityElement(cp);
        cp.setName("ItineraryRefId");
        cp.setType(cp.createSchemaReference(stringType, GlobalType.class));
        model.endTransaction();
        model.startTransaction();
        //messageType="tres:ItineraryIn"
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, d, 
                "/definitions/message[@name='ItineraryIn']"));

        //messageType="ares:AirlineReservedIn"
        WSDLModel airModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.AIRLINE);
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, airModel.getDefinitions(), 
                "/definitions/message[@name='AirlineReservedIn']"));
        //messageType="ares:ReserveAirlineIn"
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, airModel.getDefinitions(), 
                "/definitions/message[@name='ReserveAirlineIn']"));

        //messageType="vres:VehicleReservedIn"
        WSDLModel vehModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, vehModel.getDefinitions(), 
                "/definitions/message[@name='VehicleReservedIn']"));
        //messageType="vres:ReserveVehicleIn"
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, vehModel.getDefinitions(), 
                "/definitions/message[@name='ReserveVehicleIn']"));
        
        //messageType="hres:HotelReservedIn"
        WSDLModel hotelModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.HOTEL);
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, hotelModel.getDefinitions(), 
                "/definitions/message[@name='HotelReservedIn']"));
        //messageType="hres:ReserveHotelIn"
        addPropertyAlias(fact, d, cp, FindWSDLComponent.findComponent(Message.class, hotelModel.getDefinitions(), 
                "/definitions/message[@name='ReserveHotelIn']"));
        
        //<plnk:partnerLinkType name="TravelReservationPartnerLinkType">
        //    <plnk:role name="TravelReservationServiceRole">
        //    <plnk:portType name="tres:TravelReservationPortType"/>
        
        PartnerLinkType plt = fact.createPartnerLinkType(d);
        d.addExtensibilityElement(plt);
        plt.setName("TravelReservationPartnerLinkType");
        Role role1 = fact.createRole(plt);
        plt.setRole1(role1);
        role1.setName("TravelReservationServiceRole");
        role1.setPortType(role1.createReferenceTo(pt, PortType.class));
        
        assertEquals("element prefix", "plnk", ((PartnerLinkTypeImpl)plt).getPeer().getPrefix());
    }

    public void testWrite() throws Exception {
        TestCatalogModel.getDefault().addNamespace(NamespaceLocation.EMPTY_TRAVEL);
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.EMPTY_TRAVEL);
        model.startTransaction();
        doWrite(model);
        model.endTransaction();
        checkRead(model);
    }
    
    public String getTestResourcePath() {
        return "resources/TravelReservationService.wsdl";
    }
}
