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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.axi.sync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.Sequence;


/**
 * The unit test covers various use cases of sync on Element
 * and ElementRef.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SyncElementTest extends AbstractSyncTestCase {
    
    public static final String TEST_XSD         = "resources/address.xsd";
    public static final String GLOBAL_ELEMENT   = "address";
    
    
    /**
     * SyncElementTest
     */
    public SyncElementTest(String testName) {
	super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
	TestSuite suite = new TestSuite(SyncElementTest.class);
	return suite;
    }
    
    public void testElementType() throws Exception {
	removeElementFromType();
	removeAttributeFromAttrGroup();
	changeType();
	changeAttributeRef();
	changeTypeContent();
        changeNameOfElement();
	//deleteGlobalType();
    }
    
    public void testElement() throws Exception {
	removeGlobalElement();
	addGlobalElement();
	changeElementRef();
    }
    
    /**
     * Removes an element from type "USAddress".
     * Element count should be one less.
     */
    private void removeElementFromType() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	assert(childCount == 3);
	GlobalComplexType gct = findGlobalComplexType("USAddress");
	getSchemaModel().startTransaction();
	Sequence s = (Sequence)gct.getChildren().get(0);
	s.removeContent(s.getContent().get(0));
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	childCount = address.getChildElements().size();
	assert(childCount == 2);
    }
    
    /**
     * Removes an attribute from attribute group.
     * child count should be one less.
     */
    private void removeAttributeFromAttrGroup() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildren().size();
	assert(childCount == 4);
	GlobalAttributeGroup gag = findGlobalAttributeGroup("attr-group");
	getSchemaModel().startTransaction();
	LocalAttribute attr = (LocalAttribute)gag.getChildren().get(1);
	gag.removeLocalAttribute(attr);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	childCount = address.getChildren().size();
	assert(childCount == 3);
    }
    
    /**
     * Change the type of element "address" from
     * "USAddress" to "USAddress1".
     */
    private void changeType() throws Exception {
	PropertyListener l = new PropertyListener();
	getAXIModel().addPropertyChangeListener(l);
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	assert(childCount == 2);
	GlobalElement ge = (GlobalElement)globalElement.getPeer();
	getSchemaModel().startTransaction();
	setType(ge, "USAddress1");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	getAXIModel().removePropertyChangeListener(l);
	childCount = address.getChildElements().size();
	assert(childCount == 5);
    }
    
    /**
     * Change the content of "USAddress1".
     */
    private void changeTypeContent() throws Exception {
	PropertyListener l = new PropertyListener();
	getAXIModel().addPropertyChangeListener(l);
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	assert(childCount == 5);
	getSchemaModel().startTransaction();
	GlobalGroup gg = findGlobalGroup("group2");
	GroupReference gr = (GroupReference)(findGlobalComplexType("USAddress1").getChildren().get(0));
	NamedComponentReference ref = getSchemaModel().getFactory().
	    createGlobalReference(gg, GlobalGroup.class, gr);
	gr.setRef(ref);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	getAXIModel().removePropertyChangeListener(l);
	childCount = address.getChildElements().size();
	assert(childCount == 3);
    }
    
    private void changeNameOfElement() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	GlobalElement e = findGlobalElement("address");
	getSchemaModel().startTransaction();
	e.setName("NewAddress");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	assert(childCount == address.getChildElements().size());
        assert(address.getName().equals("NewAddress"));
    }
    
    private void changeNameOfType() throws Exception {
	Element address = findAXIGlobalElement("address");
	int childCount = address.getChildElements().size();
	GlobalComplexType type = findGlobalComplexType("USAddress1");
	getSchemaModel().startTransaction();
	type.setName("USAddress2");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	assert(childCount == address.getChildElements().size());
    }
    
    private void changeElementRef() throws Exception {
	getSchemaModel().startTransaction();
	GlobalGroup group = findGlobalGroup("group1");
	ElementReference ref = (ElementReference)group.getChildren().get(0).getChildren().get(0);
	GlobalElement ge = findGlobalElement("fullName");
	NamedComponentReference ncr = getSchemaModel().getFactory().
	    createGlobalReference(ge, GlobalElement.class, ref);
	ref.setRef(ncr);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
    }
    
    /**
     * Remove a global element.
     */
    private void removeGlobalElement() throws Exception {
	int elementCount = getAXIModel().getRoot().getElements().size();
	Element address = findAXIGlobalElement("NewAddress");        
	GlobalElement ge = (GlobalElement)address.getPeer();
	getSchemaModel().startTransaction();
	getSchemaModel().getSchema().removeElement(ge);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	int newCount = getAXIModel().getRoot().getElements().size();
	assert( (elementCount-1) == newCount);
    }
    
    private void addGlobalElement() throws Exception {
	int elementCount = getAXIModel().getRoot().getElements().size();
	getSchemaModel().startTransaction();
	GlobalElement ge = getSchemaModel().getFactory().createGlobalElement();
	ge.setName("address");
	setType(ge, "USAddress1");
	getSchemaModel().getSchema().addElement(ge);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	int newCount = getAXIModel().getRoot().getElements().size();
	assert( (elementCount+1) == newCount);
    }
    
    private void setType(GlobalElement ge, String globalComplexType) {
	for(GlobalComplexType type : getSchemaModel().getSchema().getComplexTypes()) {
	    if(type.getName().equals(globalComplexType)) {
		NamedComponentReference ref = getSchemaModel().getFactory().
		    createGlobalReference(type, GlobalType.class, ge);
		ge.setType(ref);
	    }
	}
    }
    
    private void renameGlobalElement() throws Exception {
	getSchemaModel().startTransaction();
	GlobalElement ge = (GlobalElement)globalElement.getPeer();
	ge.setName("address1");
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	assert(globalElement.getName().equals("address1"));
    }
    
    private void changeAttributeRef() throws Exception {
	getSchemaModel().startTransaction();
	GlobalAttributeGroup group = findGlobalAttributeGroup("attr-group");
	AttributeReference ref = (AttributeReference)group.getChildren().get(0);
	GlobalAttribute ga = findGlobalAttribute("countryString");
	NamedComponentReference ncr = getSchemaModel().getFactory().
	    createGlobalReference(ga, GlobalAttribute.class, ref);
	ref.setRef(ncr);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
    }

    /**
     * Remove a GCT "USAddress1", even tho it is being used by "address".
     */
    private void deleteGlobalType() throws Exception {
	Element address = findAXIGlobalElement("NewAddress");
	int childCount = address.getChildElements().size();
	assert(childCount == 3);
	GlobalComplexType gct = findGlobalComplexType("USAddress1");
	getSchemaModel().startTransaction();
        getSchemaModel().getSchema().removeComplexType(gct);
	getSchemaModel().endTransaction();
	getAXIModel().sync();
	childCount = address.getChildElements().size();
	assert(childCount == 0);
    }
    
    static class PropertyListener implements PropertyChangeListener {
	List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
	
	public void propertyChange(PropertyChangeEvent evt) {
	    events.add(evt);
	}
	public List<PropertyChangeEvent> getEvents() {
	    return events;
	}
	public void clearEvents() { events.clear();}
    }
}
