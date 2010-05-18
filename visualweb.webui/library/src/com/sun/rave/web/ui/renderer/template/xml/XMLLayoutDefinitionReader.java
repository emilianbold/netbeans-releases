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
package com.sun.rave.web.ui.renderer.template.xml;

import com.sun.rave.web.ui.component.util.descriptors.ComponentType;
import com.sun.rave.web.ui.component.util.descriptors.LayoutAttribute;
import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition;
import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;
import com.sun.rave.web.ui.component.util.descriptors.LayoutFacet;
import com.sun.rave.web.ui.component.util.descriptors.LayoutForEach;
import com.sun.rave.web.ui.component.util.descriptors.LayoutIf;
import com.sun.rave.web.ui.component.util.descriptors.LayoutMarkup;
import com.sun.rave.web.ui.component.util.descriptors.LayoutStaticText;
import com.sun.rave.web.ui.component.util.descriptors.LayoutWhile;
import com.sun.rave.web.ui.component.util.descriptors.Resource;
import com.sun.rave.web.ui.component.util.event.Handler;
import com.sun.rave.web.ui.component.util.event.HandlerDefinition;
import com.sun.rave.web.ui.component.util.event.IODescriptor;
import com.sun.rave.web.ui.util.IncludeInputStream;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;


/**
 *  <p>	This class is responsible for doing the actual parsing of an XML
 *	document following the layout.dtd.  It produces a {@link LayoutElement}
 *	tree with a {@link LayoutDefinition} object at the root of the tree.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class XMLLayoutDefinitionReader {

    /**
     *	Constructor.
     *
     *	@param	url		A URL pointing to the {@link LayoutDefinition}
     *	@param	entityResolver	EntityResolver to use, may be (null)
     *	@param	errorHandler	ErrorHandler to use, may be (null)
     *	@param	baseURI		The base URI passed to DocumentBuilder.parse()
     */
    public XMLLayoutDefinitionReader (URL url, EntityResolver entityResolver, ErrorHandler errorHandler, String baseURI) {
	_url = url;
	_entityResolver = entityResolver;
	_errorHandler = errorHandler;
	_baseURI = baseURI;
    }

    /**
     *	Accessor for the URL.
     */
    public URL getURL() {
	return _url;
    }

    /**
     *	Accessor for the entityResolver.
     */
    public EntityResolver getEntityResolver() {
	return _entityResolver;
    }

    /**
     *	Accessor for the ErrorHandler.
     */
    public ErrorHandler getErrorHandler() {
	return _errorHandler;
    }

    /**
     *	Accessor for the base URI.
     */
    public String getBaseURI() {
	return _baseURI;
    }

    /**
     *	<p> The read method opens the given URL and parses the XML document
     *	    that it points to.  It then walks the DOM and populates a
     *	    {@link LayoutDefinition} structure, which is returned.</p>
     *
     *	@return	The {@link LayoutDefinition}
     *
     *	@throws	IOException
     */
    public LayoutDefinition read() throws IOException {
	// Open the URL
	InputStream inputStream = new IncludeInputStream(
		new BufferedInputStream(getURL().openStream()));
	Document doc = null;

	try {
	    // Get a DocumentBuilderFactory and set it up
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    dbf.setValidating(true);
	    dbf.setIgnoringComments(true);
	    dbf.setIgnoringElementContentWhitespace(false);
	    dbf.setCoalescing(false);
	    // The opposite of creating entity ref nodes is expanding inline
	    dbf.setExpandEntityReferences(true);

	    // Get a DocumentBuilder...
	    DocumentBuilder db = null;
	    try {
		db = dbf.newDocumentBuilder();
	    } catch (ParserConfigurationException ex) {
		throw new RuntimeException(ex);
	    }
	    if (getEntityResolver() != null) {
		db.setEntityResolver(getEntityResolver());
	    }
	    if (getErrorHandler() != null) {
		db.setErrorHandler(getErrorHandler());
	    }

	    // Parse the XML file
	    try {
		doc = db.parse(inputStream, getBaseURI());
	    } catch (SAXException ex) {
		throw new RuntimeException(ex);
	    }
	} finally {
	    try {
		inputStream.close();
	    } catch (Exception ex) {
		// Ignore...
	    }
	}

	// Populate the LayoutDefinition from the Document
	return createLayoutDefinition(doc);
    }

    /**
     *	This method is responsible for extracting all the information out of
     *	the supplied document and filling the {@link LayoutDefinition} structure.
     *
     *	@param	doc	The Document object to read info from
     *
     *	@return	The new {@link LayoutDefinition} Object 
     */
    private LayoutDefinition createLayoutDefinition(Document doc) {
	// Get the document element (LAYOUT_DEFINITION_ELEMENT)
	Node node = doc.getDocumentElement();
	if (!node.getNodeName().equalsIgnoreCase(LAYOUT_DEFINITION_ELEMENT)) {
	    throw new RuntimeException("Document Element must be '"+
		LAYOUT_DEFINITION_ELEMENT+"'");
	}

	// Create a new LayoutDefinition (the id is not propagated here)
	LayoutDefinition ld = new LayoutDefinition("");

	// Do "resources" first, they are defined at the top of the document
	List childElements = getChildElements(node, RESOURCES_ELEMENT);
	Iterator it = childElements.iterator();
	if (it.hasNext()) {
	    // Found the RESOURCES_ELEMENT, there is at most 1
	    addResources(ld, (Node)it.next());
	}

	// Do "types", they need to be defined before parsing the layout
	childElements = getChildElements(node, TYPES_ELEMENT);
	it = childElements.iterator();
	if (it.hasNext()) {
	    // Found the TYPES_ELEMENT, there is at most 1
	    addTypes(ld, (Node)it.next());
	}

	// Do "handlers" next, they need to be defined before parsing the layout
	childElements = getChildElements(node, HANDLERS_ELEMENT);
	it = childElements.iterator();
	if (it.hasNext()) {
	    // Found the HANDLERS_ELEMENT, there is at most 1
	    cacheHandlerDefs((Node)it.next());
	}

	// Look to see if there is an EVENT_ELEMENT defined
	childElements = getChildElements(node, EVENT_ELEMENT);
	it = childElements.iterator();
	if (it.hasNext()) {
	    // Found the EVENT_ELEMENT, there is at most 1
	    // Get the event type
	    Node eventNode = (Node)it.next();
	    String type = (String)getAttributes(eventNode).
		get(TYPE_ATTRIBUTE);

	    // Set the Handlers for the given event type (name)
	    List handlers = ld.getHandlers(type);
	    ld.setHandlers(type, getHandlers(eventNode, handlers));
	}

	// Next look for "layout", there is exactly 1
	childElements = getChildElements(node, LAYOUT_ELEMENT);
	it = childElements.iterator();
	if (it.hasNext()) {
	    // Found the LAYOUT_ELEMENT, there is only 1
	    addChildLayoutElements(ld, (Node)it.next());
	} else {
	    throw new RuntimeException("A '"+LAYOUT_ELEMENT+
		"' element is required in the XML document!");
	}

	// Return the LayoutDefinition
	return ld;
    }

    /**
     *	This method iterates throught the child RESOURCE_ELEMENT nodes and
     *	adds new resource objects to the {@link LayoutDefinition}.
     *
     *	@param	ld	The LayoutDefinition
     *	@param	node	Parent Node containing the RESOURCE_ELEMENT nodes
     */
    private void addResources(LayoutDefinition ld, Node node) {
	// Get the child nodes
	Iterator it = getChildElements(node, RESOURCE_ELEMENT).iterator();

	// Walk children (we only care about RESOURCE_ELEMENT)
	while (it.hasNext()) {
	    // Found a RESOURCE_ELEMENT
	    ld.addResource(createResource((Node)it.next()));
	}
    }

    /**
     *	This method takes the given Resource Element node and reads the
     *	ID_ATTRIBUTE, EXTRA_INFO_ATTRIBUTE and FACTORY_CLASS_ATTRIBUTE
     *	attributes.  It then instantiates a new Resource with the values of
     *	these two attributes.
     *
     *	@param	node	The Resource node to extract information from
     *			when creating the Resource
     */
    private Resource createResource(Node node) {
	// Pull off the attributes
	Map attributes = getAttributes(node);
	String id = (String)attributes.get(ID_ATTRIBUTE);
	String extraInfo =
	    (String)attributes.get(EXTRA_INFO_ATTRIBUTE);
	String factoryClass =
	    (String)attributes.get(FACTORY_CLASS_ATTRIBUTE);

	// Make sure required values are present
	if ((factoryClass == null) || (id == null) || (extraInfo == null) ||
		(factoryClass.trim().equals("")) || (id.trim().equals("")) ||
		(extraInfo.trim().equals(""))) {
	    throw new RuntimeException("'"+ID_ATTRIBUTE+"', '"+
		EXTRA_INFO_ATTRIBUTE+"', and '"+FACTORY_CLASS_ATTRIBUTE+
		"' are required attributes of '"+
		RESOURCE_ELEMENT+"' Element!");
	}

	// Create the new Resource
	return new Resource(id, extraInfo, factoryClass);
    }

    /**
     *	This method iterates through the child COMPONENT_TYPE_ELEMENT nodes
     *	and adds new ComponentTypes to the {@link LayoutDefinition}.
     *
     *	@param	ld	The LayoutDefinition
     *	@param	node	Parent Node containing the COMPONENT_TYPE_ELEMENT nodes
     */
    private void addTypes(LayoutDefinition ld, Node node) {
	// Get the child nodes
	Iterator it = getChildElements(node, COMPONENT_TYPE_ELEMENT).iterator();

	// Walk the COMPONENT_TYPE_ELEMENT elements
	while (it.hasNext()) {
	    ld.addComponentType(createComponentType((Node)it.next()));
	}
    }

    /**
     *	This method takes the given ComponentType Element node and reads the
     *	ID_ATTRIBUTE and FACTORY_CLASS_ATTRIBUTE attributes.  It then
     *	instantiates a new ComponentType with the values of these two
     *	attributes.
     *
     *	@param	node	The ComponentType node to extract information from
     *			when creating the ComponentType
     */
    private ComponentType createComponentType(Node node) {
	// Pull off the attributes
	Map attributes = getAttributes(node);
	String id = (String)attributes.get(ID_ATTRIBUTE);
	String factoryClass =
	    (String)attributes.get(FACTORY_CLASS_ATTRIBUTE);

	// Make sure required values are present
	if ((factoryClass == null) || (id == null) ||
		(factoryClass.trim().equals("")) || (id.trim().equals(""))) {
	    throw new RuntimeException("Both '"+ID_ATTRIBUTE+"' and '"+
		FACTORY_CLASS_ATTRIBUTE+"' are required attributes of '"+
		COMPONENT_TYPE_ELEMENT+"' Element!");
	}

	// Create the new ComponentType
	return new ComponentType(id, factoryClass);
    }

    /**
     *	<p> This method iterates through the child HANDLER_DEFINITION_ELEMENT
     *	    nodes and caches them, so they may be retrieved later by Handlers
     *	    referring to them.</p>
     *
     *	@param	node	Parent Node containing HANDLER_DEFINITION_ELEMENT nodes
     */
    private void cacheHandlerDefs(Node node) {
	HandlerDefinition def = null;

	// Get the child nodes
	Iterator it = getChildElements(node, HANDLER_DEFINITION_ELEMENT).iterator();
	while (it.hasNext()) {
	    // Found a HANDLER_DEFINITION_ELEMENT, cache it
	    def = createHandlerDefinition((Node)it.next());
	    _handlerDefs.put(def.getId(), def);
	}
    }

    /**
     *	<p> This method takes the given HANDLER_DEFINITION_ELEMENT node and
     *	    reads the ID_ATTRIBUTE, CLASS_NAME_ATTRIBUTE, and
     *	    METHOD_NAME_ATTRIBUTE attributes.  It then instantiates a new
     *	    HandlerDefinition object.</p>
     *
     *	<p> Next it looks to see if the HandlerDefinition has child inputDef,
     *	    outputDef, and/or nested handler elements.  If so it processes
     *	    them.</p>
     *
     *	@param	node	The HANDLER_DEFINITION_ELEMENT node to extract
     *			information from when creating the HandlerDefinition.
     *
     *	@return	The newly created HandlerDefinition.
     */
    public HandlerDefinition createHandlerDefinition(Node node) {

	// Create he HandlerDefinition
	Map attributes = getAttributes(node);
	String value = (String)attributes.get(ID_ATTRIBUTE);
	HandlerDefinition hd = new HandlerDefinition(value);

// hd.setDescription(_description)

	// Check for a className
	value = (String)attributes.get(CLASS_NAME_ATTRIBUTE);
	if ((value != null) && !value.equals("")) {
	    // Found a className, now get the methodName
	    String tmpStr =
		(String)attributes.get(METHOD_NAME_ATTRIBUTE);
	    if ((tmpStr == null) || tmpStr.equals("")) {
		throw new IllegalArgumentException("You must provide a '"+
		    METHOD_NAME_ATTRIBUTE+"' attribute on the '"+
		    HANDLER_DEFINITION_ELEMENT+"' element with "+
		    CLASS_NAME_ATTRIBUTE+" atttribute equal to '"+value+"'.");
	    }
	    hd.setHandlerMethod(value, tmpStr);
	}

	// Add child handlers to this HandlerDefinition.  This allows a
	// HandlerDefinition to define handlers that should be invoked before
	// the method defined by this handler definition is invoked.
	List handlers = hd.getChildHandlers();
	hd.setChildHandlers(getHandlers(node, handlers));

	// Add InputDef objects to the HandlerDefinition
	addInputDefs(hd, node);

	// Add OutputDef objects to the HandlerDefinition
	addOutputDefs(hd, node);

	// Return the newly created HandlerDefinition object
	return hd;
    }

    /**
     *	<p> This method creates a List of Handlers from the provided Node.  It
     *	    will look at the child Elements for HANDLER_ELEMENT elements.
     *	    When found, it will create a new Handler object and add it to a
     *	    List that is created internally.  This List is returned.</p>
     *
     *	@param	node	    Node containing HANDLER_ELEMENT elements.
     *	@param	handlers    List of existing handlers.
     *
     *	@return	A List of Handler objects, empty List if no Handlers found
     */
    private List getHandlers(Node node, List handlers) {
	// Get the child nodes
	Iterator it = getChildElements(node, HANDLER_ELEMENT).iterator();

	// Walk children (we only care about HANDLER_ELEMENT)
	if (handlers == null) {
	    handlers = new ArrayList();
	}
	while (it.hasNext()) {
	    // Found a HANDLER_ELEMENT
	    handlers.add(createHandler((Node)it.next()));
	}

	// Return the handlers
	return handlers;
    }

    /**
     *	<p> This method creates a Handler from the given HandlerNode.  It will
     *	    add input and/or output mappings specified by any child Elements
     *	    named INPUT_ELEMENT or OUTPUT_MAPPING_ELEMENT.</p>
     *
     *	@param	handlerNode	The Node describing the Handler to be created.
     *
     *	@return	The newly created Handler.
     */
    private Handler createHandler(Node handlerNode) {
	// Pull off attributes...
	String id = (String)getAttributes(handlerNode).
	    get(ID_ATTRIBUTE);
	if ((id == null) || (id.trim().equals(""))) {
	    throw new RuntimeException("'"+ID_ATTRIBUTE+
		"' attribute not found on '"+HANDLER_ELEMENT+"' Element!");
	}

	// Find the HandlerDefinition associated with this Handler
	HandlerDefinition handlerDef = (HandlerDefinition)_handlerDefs.get(id);
	if (handlerDef == null) {
	    throw new IllegalArgumentException(HANDLER_ELEMENT+" elements "+
		ID_ATTRIBUTE+" attribute must match the "+ID_ATTRIBUTE+
		" attribute of a "+HANDLER_DEFINITION_ELEMENT+
		".  A HANDLER_ELEMENT with '"+id+
		"' was specified, however there is no cooresponding "+
		HANDLER_DEFINITION_ELEMENT+" with a matching "+
		ID_ATTRIBUTE+" attribute.");
	}

	// Create new Handler
	Handler handler =  new Handler(handlerDef);

	// Add the inputs
	Map attributes = null;
	Node inputNode = null;
	Iterator it = getChildElements(handlerNode, INPUT_ELEMENT).iterator();
	while (it.hasNext()) {
	    // Processing an INPUT_ELEMENT
	    inputNode = (Node)it.next();
	    attributes = getAttributes(inputNode);
	    handler.setInputValue(
		(String)attributes.get(NAME_ATTRIBUTE),
		getValueFromNode(inputNode, attributes));
	}

	// Add the OutputMapping objects
	it = getChildElements(handlerNode, OUTPUT_MAPPING_ELEMENT).iterator();
	while (it.hasNext()) {
	    // Processing an OUTPUT_MAPPING_ELEMENT
	    attributes = getAttributes((Node)it.next());
	    handler.setOutputMapping(
		(String)attributes.get(OUTPUT_NAME_ATTRIBUTE),
		(String)attributes.get(TARGET_KEY_ATTRIBUTE),
		(String)attributes.get(TARGET_TYPE_ATTRIBUTE));
	}

	// Return the newly created handler
	return handler;
    }

    /**
     *	<p> This method adds InputDefs to the given HandlerDefinition object.
     *	    It will look at the child elements for those named
     *	    INPUT_DEF_ELEMENT.  It will create an IODescriptor for each and add
     *	    it to the HandlerDefinition.</p>
     *
     *	@param	hd	HandlerDefinition
     *	@param	hdNode	HandlerDefinition Node, its children will be searched
     */
    private void addInputDefs(HandlerDefinition hd, Node hdNode) {
	// Get the child nodes
	Iterator it = getChildElements(hdNode, INPUT_DEF_ELEMENT).iterator();

	// Walk children (we only care about INPUT_DEF_ELEMENT)
	while (it.hasNext()) {
	    // Found a INPUT_DEF_ELEMENT
	    hd.addInputDef(createIODescriptor((Node)it.next()));
	}
    }

    /**
     *	<p> This method adds OutputDefs to the given HandlerDefinition object.
     *	    It will look at the child elements for those named
     *	    OUTPUT_DEF_ELEMENT.  It will create an IODescriptor for each and
     *	    add it to the HandlerDefinition.</p>
     *
     *	@param	hd	HandlerDefinition
     *	@param	hdNode	HandlerDefinition Node, its children will be searched
     */
    private void addOutputDefs(HandlerDefinition hd, Node hdNode) {
	// Get the child nodes
	Iterator it = getChildElements(hdNode, OUTPUT_DEF_ELEMENT).iterator();

	// Walk children (we only care about OUTPUT_DEF_ELEMENT)
	while (it.hasNext()) {
	    // Found a OUTPUT_DEF_ELEMENT
	    hd.addOutputDef(createIODescriptor((Node)it.next()));
	}
    }

    /** 
     *	<p> This method will create an IODescriptor from the given node.  The
     *	    node must contain atleast a NAME_ATTRIBUTE and a TYPE_ATTRIBUTE
     *	    attribute.  It may also contain a DEFAULT_ATTRIBUTE and a
     *	    REQUIRED_ATTRIBUTE.  These are only meaningful for input
     *	    IODescriptors, however -- this method does not know the difference
     *	    between input and output descriptors.</p>
     *
     *	@param	node	The node holding info used to create an IODescriptor.
     *
     *	@return	A newly created IODescriptor.
     */
    private IODescriptor createIODescriptor(Node node) {
	// Get the attributes
	Map attributes = getAttributes(node);
	String name = (String)attributes.get(NAME_ATTRIBUTE);
	if ((name == null) || name.equals("")) {
	    throw new IllegalArgumentException("Name must be provided!");
	}
	String type = (String)attributes.get(TYPE_ATTRIBUTE);
	if ((type == null) || type.equals("")) {
	    throw new IllegalArgumentException("Type must be provided!");
	}
	Object def = attributes.get(DEFAULT_ATTRIBUTE);
	String req = (String)attributes.get(REQUIRED_ATTRIBUTE);

	// Create the IODescriptor
	IODescriptor ioDesc = new IODescriptor(name, type);
	ioDesc.setDefault(def);
	if (req != null) {
	    ioDesc.setRequired(Boolean.valueOf(req).booleanValue());
	}
// ioDesc.setDescription(attributes.get(DESCRIPTION_ATTRIBUTE))

	// Return the new IODescriptor
	return ioDesc;
    }

    /**
     *	This method adds ...
     *
     *	@param	ld
     *	@param	node
     */
    private void addChildLayoutElements(LayoutElement layElt, Node node) {
	// Get the child nodes
	Iterator it = getChildElements(node).iterator();

	// Walk children (we care about IF_ELEMENT, ATTRIBUTE_ELEMENT,
	// MARKUP_ELEMENT, FACET_ELEMENT, STATIC_TEXT_ELEMENT,
	// COMPONENT_ELEMENT, EVENT_ELEMENT, FOREACH_ELEMENT, and
	// WHILE_ELEMENT)
	Node childNode = null;
	String name = null;
	while (it.hasNext()) {
	    childNode = (Node)it.next();
	    name = childNode.getNodeName();
	    if (name.equalsIgnoreCase(IF_ELEMENT)) {
		// Found a IF_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutIf(layElt, childNode));
	    } else if (name.equalsIgnoreCase(ATTRIBUTE_ELEMENT)) {
		// Found a ATTRIBUTE_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutAttribute(layElt, childNode));
	    } else if (name.equalsIgnoreCase(MARKUP_ELEMENT)) {
		// Found a MARKUP_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutMarkup(layElt, childNode));
	    } else if (name.equalsIgnoreCase(FACET_ELEMENT)) {
		// Found a FACET_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutFacet(layElt, childNode));
	    } else if (name.equalsIgnoreCase(STATIC_TEXT_ELEMENT)) {
		// Found a STATIC_TEXT_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutStaticText(layElt, childNode));
	    } else if (name.equalsIgnoreCase(COMPONENT_ELEMENT)) {
		// Found a COMPONENT_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutComponent(layElt, childNode));
	    } else if (name.equalsIgnoreCase(EVENT_ELEMENT)) {
		// Found a EVENT_ELEMENT
		// Get the event type
		name = (String)getAttributes(childNode).
		    get(TYPE_ATTRIBUTE);
		// Set the Handlers for the given event type (name)
		List handlers = layElt.getHandlers(name);
		layElt.setHandlers(name, getHandlers(childNode, handlers));
	    } else if (name.equalsIgnoreCase(FOREACH_ELEMENT)) {
		// Found a FOREACH_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutForEach(layElt, childNode));
	    } else if (name.equalsIgnoreCase(WHILE_ELEMENT)) {
		// Found a WHILE_ELEMENT
		layElt.addChildLayoutElement(
		    createLayoutWhile(layElt, childNode));
	    } else {
		throw new RuntimeException("Unknown Element Found: '"
			+ childNode.getNodeName() + "' under '"
			+ node.getNodeName() + "'.");
	    }
	}
    }

    /**
     *	<p> This method creates a new {@link LayoutIf}
     *	    {@link LayoutElement}.</p>
     *
     *	@param	parent	The parent LayoutElement.
     *	@param	node	The {@link IF_ELEMENT} node to extract information from
     *			when creating the LayoutIf
     */
    private LayoutElement createLayoutIf(LayoutElement parent, Node node) {
	// Pull off attributes...
	String condition = (String)getAttributes(node).get(
	    CONDITION_ATTRIBUTE);
	if ((condition == null) || (condition.trim().equals(""))) {
	    throw new RuntimeException("'"+CONDITION_ATTRIBUTE+
		"' attribute not found on '"+IF_ELEMENT+"' Element!");
	}

	// Create new LayoutIf
	LayoutElement ifElt =  new LayoutIf(parent, condition);

	// Add children...
	addChildLayoutElements(ifElt, node);

	// Return the if
	return ifElt;
    }

    /**
     *	<p> This method creates a new {@link LayoutForEach}
     *	    {@link LayoutElement}.</p>
     *
     *	@param	parent	The parent {@link LayoutElement}.
     *	@param	node	The {@link #FOREACH_ELEMENT} node to extract
     *			information from when creating the
     *			{@link LayoutForEach}.
     *
     *	@return The new {@link LayoutForEach} {@link LayoutElement}.
     */
    private LayoutElement createLayoutForEach(LayoutElement parent, Node node) {
	// Pull off attributes...
	String list = (String)getAttributes(node).get(
	    LIST_ATTRIBUTE);
	if ((list == null) || (list.trim().equals(""))) {
	    throw new RuntimeException("'"+LIST_ATTRIBUTE+
		"' attribute not found on '"+FOREACH_ELEMENT+"' Element!");
	}
	String key = (String)getAttributes(node).get(
	    KEY_ATTRIBUTE);
	if ((key == null) || (key.trim().equals(""))) {
	    throw new RuntimeException("'"+KEY_ATTRIBUTE+
		"' attribute not found on '"+FOREACH_ELEMENT+"' Element!");
	}

	// Create new LayoutForEach
	LayoutElement forEachElt =  new LayoutForEach(parent, list, key);

	// Add children...
	addChildLayoutElements(forEachElt, node);

	// Return the forEach
	return forEachElt;
    }

    /**
     *	<p> This method creates a new {@link LayoutWhile}
     *	    {@link LayoutElement}.</p>
     *
     *	@param	parent	The parent {@link LayoutElement}.
     *	@param	node	The {@link #WHILE_ELEMENT} node to extract information
     *			from when creating the LayoutWhile.
     *
     *	@return The new {@link LayoutWhile} {@link LayoutElement}.
     */
    private LayoutElement createLayoutWhile(LayoutElement parent, Node node) {
	// Pull off attributes...
	String condition = (String)getAttributes(node).get(
	    CONDITION_ATTRIBUTE);
	if ((condition == null) || (condition.trim().equals(""))) {
	    throw new RuntimeException("'"+CONDITION_ATTRIBUTE+
		"' attribute not found on '"+WHILE_ELEMENT+"' Element!");
	}

	// Create new LayoutWhile
	LayoutElement whileElt =  new LayoutWhile(parent, condition);

	// Add children...
	addChildLayoutElements(whileElt, node);

	// Return the while
	return whileElt;
    }

    /**
     *
     *
     *	@param	parent	The parent {@link LayoutElement}.
     *	@param	node	The ATTRIBUTE_ELEMENT node to extract information from
     *			when creating the LayoutAttribute
     */
    private LayoutElement createLayoutAttribute(LayoutElement parent, Node node) {
	// Pull off attributes...
	Map attributes = getAttributes(node);
	String name = (String)attributes.get(NAME_ATTRIBUTE);
	if ((name == null) || (name.trim().equals(""))) {
	    throw new RuntimeException("'"+NAME_ATTRIBUTE+
		"' attribute not found on '"+ATTRIBUTE_ELEMENT+"' Element!");
	}
	String value = (String)attributes.get(VALUE_ATTRIBUTE);
	String property = (String)attributes.get(PROPERTY_ATTRIBUTE);

	// Create new LayoutAttribute
	LayoutElement attributeElt =  new LayoutAttribute(parent, name, value, property);

	// Add children... (event children are supported)
	addChildLayoutElements(attributeElt, node);

	// Return the LayoutAttribute
	return attributeElt;
    }

    /**
     *
     *
     *	@param	parent	The parent {@link LayoutElement}.
     *	@param	node	The MARKUP_ELEMENT node to extract information from
     *			when creating the LayoutMarkup
     */
    private LayoutElement createLayoutMarkup(LayoutElement parent, Node node) {
	// Pull off attributes...
	Map attributes = getAttributes(node);
	String tag = (String)attributes.get(TAG_ATTRIBUTE);
	if ((tag == null) || (tag.trim().equals(""))) {
	    throw new RuntimeException("'"+TAG_ATTRIBUTE+
		"' attribute not found on '"+MARKUP_ELEMENT+"' Element!");
	}
	String type = (String)attributes.get(TYPE_ATTRIBUTE);

	// Create new LayoutMarkup
	LayoutElement markupElt =  new LayoutMarkup(parent, tag, type);

	// Add children...
	addChildLayoutElements(markupElt, node);

	// Return the LayoutMarkup
	return markupElt;
    }

    /**
     *
     *
     *	@param	parent	The parent {@link LayoutElement}.
     *	@param	node	The FACET_ELEMENT node to extract information from
     *			when creating the LayoutFacet
     */
    private LayoutElement createLayoutFacet(LayoutElement parent, Node node) {
	// Pull off attributes...
	// id
	String id = (String)getAttributes(node).get(ID_ATTRIBUTE);
	if ((id == null) || (id.trim().equals(""))) {
	    throw new RuntimeException("'"+ID_ATTRIBUTE+
		"' attribute not found on '"+FACET_ELEMENT+"' Element!");
	}

	// isRendered
	String rendered = (String)getAttributes(node).get(RENDERED_ATTRIBUTE);
	boolean isRendered = true;
	if ((rendered == null)
		|| rendered.trim().equals("")
		|| rendered.equals(AUTO_RENDERED)) {
	    // Automatically determine if this LayoutFacet should be rendered
	    rendered = AUTO_RENDERED;
	    LayoutElement layoutComponent = parent;
	    while (layoutComponent != null) {
		if (layoutComponent instanceof LayoutComponent) {
		    isRendered = false;
		    break;
		}
		layoutComponent = layoutComponent.getParent();
	    }
	} else {
	    isRendered = Boolean.getBoolean(rendered);
	}

	// Create new LayoutFacet
	LayoutFacet facetElt =  new LayoutFacet(parent, id);
	facetElt.setRendered(isRendered);

	// Add children...
	addChildLayoutElements(facetElt, node);

	// Return the LayoutFacet
	return facetElt;
    }

    /**
     *	<p> This method returns true if any of the parent
     *	    {@link LayoutElement}s are {@link LayoutComponent}s.</p>
     *
     *	@param	elt	The LayoutElement to check.
     *
     *	@return true if it has a LayoutComponent ancestor.
     */
    private static boolean hasLayoutComponentAncestor(LayoutElement elt) {
	elt = elt.getParent();
	while (elt != null) {
	    if (elt instanceof LayoutComponent) {
		return true;
	    } else if (elt instanceof LayoutFacet) {
		// Don't consider it a child if it is a facet
		return false;
	    }
	    elt = elt.getParent();
	}

	// Not found
	return false;
    }

    /**
     *
     *
     *	@param	node	The COMPONENT_ELEMENT node to extract information from
     *			when creating the LayoutComponent
     */
    private LayoutElement createLayoutComponent(LayoutElement parent, Node node) {
	// Pull off attributes...
	Map attributes = getAttributes(node);
	String id = (String) attributes.get(ID_ATTRIBUTE);
	String type = (String) attributes.get(TYPE_ATTRIBUTE);
	if ((type == null) || (type.trim().equals(""))) {
	    throw new RuntimeException("'"+TYPE_ATTRIBUTE+
		"' attribute not found on '"+COMPONENT_ELEMENT+"' Element!");
	}

	// Create new LayoutComponent
	LayoutComponent component =  new LayoutComponent(parent, id,
	    getComponentType(parent, type));

	// Check for overwrite flag
	String overwrite = (String) attributes.get(OVERWRITE_ATTRIBUTE);
	if ((overwrite != null) && (overwrite.length() > 0)) {
	    component.setOverwrite(Boolean.valueOf(overwrite).booleanValue());
	}

	if (hasLayoutComponentAncestor(component)) {
	    component.setFacetChild(false);
	} else {
	    // Need to add this so that it has the correct facet name
	    // Check to see if this LayoutComponent is inside a LayoutFacet
	    while (parent != null) {
		if (parent instanceof LayoutFacet) {
		    // Inside a LayoutFacet, use its id... only if this facet
		    // is a child of a LayoutComponent (otherwise, it is a
		    // layout facet used for layout, not for defining a facet
		    // of a UIComponent)
		    if (hasLayoutComponentAncestor(parent)) {
			id = parent.getUnevaluatedId();
			break;
		    }
		}
		if (parent instanceof LayoutComponent) {
		    // Not inside a LayoutFacet
		    break;
		}
		parent = parent.getParent();
	    }
	    // Set the facet name
	    component.addOption(LayoutComponent.FACET_NAME, id);
	}

	// Add children... (different for component LayoutElements)
	addChildLayoutComponentChildren(component, node);

	// Return the LayoutComponent
	return component;
    }

    /**
     *
     */
    private void addChildLayoutComponentChildren(LayoutComponent component, Node node) {
	// Get the child nodes
	Iterator it = getChildElements(node).iterator();

	// Walk children (we care about FACET_ELEMENT and OPTION_ELEMENT)
	Node childNode = null;
	String name = null;
	while (it.hasNext()) {
	    childNode = (Node)it.next();
	    name = childNode.getNodeName();
	    if (name.equalsIgnoreCase(COMPONENT_ELEMENT)) {
		// Found a COMPONENT_ELEMENT
		component.addChildLayoutElement(
			createLayoutComponent(component, childNode));
	    } else if (name.equalsIgnoreCase(FACET_ELEMENT)) {
		// Found a FACET_ELEMENT
		component.addChildLayoutElement(
			createLayoutFacet(component, childNode));
	    } else if (name.equalsIgnoreCase(OPTION_ELEMENT)) {
		// Found a OPTION_ELEMENT
		addOption(component, childNode);
	    } else if (name.equalsIgnoreCase(EVENT_ELEMENT)) {
		// Found a EVENT_ELEMENT
		// Get the event type
		name = (String)getAttributes(childNode).
		    get(TYPE_ATTRIBUTE);

		// Set the Handlers for the given event type (name)
		List handlers = component.getHandlers(name);
		component.setHandlers(name, getHandlers(childNode, handlers));
	    } else {
		throw new RuntimeException("Unknown Element Found: '"
			+ childNode.getNodeName() + "' under '"
			+ COMPONENT_ELEMENT + "'.");
	    }
	}
    }

    /**
     *	This method adds an option to the given LayoutComponent based on the
     *	information in the given OPTION_ELEMENT Node.
     *
     *	@param	component   The LayoutComponent
     *	@param	node	    The OPTION_ELEMENT node
     */
    private void addOption(LayoutComponent component, Node node) {
	// Pull off the attributes
	Map attributes = getAttributes(node);

	// Get the name
	String name = (String)attributes.get(NAME_ATTRIBUTE);
	if ((name == null) || (name.trim().equals(""))) {
	    throw new RuntimeException("'"+NAME_ATTRIBUTE+
		"' attribute not found on '"+OPTION_ELEMENT+"' Element!");
	}
	name = name.trim();
	
	// Get the value
	Object value = getValueFromNode(node, attributes);

	// Add the option to the component (value may be null)
	component.addOption(name, value);
    }

    /**
     *	<p> This method reads obtains the VALUE_ATTRIBUTE from the given node,
     *	    or from the child LIST_ELEMENT element.  If neither are provided,
     *	    null is returned.  The attribute takes precedence over the child
     *	    LIST_ELEMENT element.</p>
     *
     *	@param	node	    Node containing the value attribute or LIST_ELEMENT
     *	@param	attributes  Map of attributes which may contain VALUE_ATTRIBUTE
     *
     *	@return	The value (as a String or List), or null if not specified
     */
    private Object getValueFromNode(Node node, Map attributes) {
	Object value = attributes.get(VALUE_ATTRIBUTE);
	if (value == null) {
	    // The value attribute may be null if multiple values are supplied.
	    // Walk children (we only care about LIST_ELEMENT)
	    List list = new ArrayList();
	    Iterator it = getChildElements(node, LIST_ELEMENT).iterator();
	    while (it.hasNext()) {
		// Add a value to the List
		list.add(getAttributes((Node)it.next()).
		    get(VALUE_ATTRIBUTE)); 
	    }
	    if (list.size()>0) {
		// Only use the list if it has values
		value = list;
	    }
	}
	return value;
    }

    /**
     *
     *
     *	@param	node	The STATIC_TEXT_ELEMENT node to extract information
     *			from when creating the {@link LayoutStaticText}
     */
    private LayoutElement createLayoutStaticText(LayoutElement parent, Node node) {
	// Create new LayoutComponent
	LayoutStaticText text =
	    new LayoutStaticText(parent, "", getTextNodesAsString(node));

	// Add all the attributes from the static text as options
//	component.addOptions(getAttributes(node));

	// Add escape... FIXME

	// Return the LayoutStaticText
	return text;
    }



    //////////////////////////////////////////////////////////////////////
    //	Utility Methods
    //////////////////////////////////////////////////////////////////////

    /**
     *	This method returns a List of all child Elements below the given Node.
     *
     *	@param	node	The node to pull child elements from.
     *
     *	@return	List of child elements found below the given node.
     */
    public List getChildElements(Node node) {
	return getChildElements(node, null);
    }

    /**
     *	<p> This method returns a List of all child Elements below the given
     *	    Node matching the given name.  If name equals null, all Elements
     *	    below this node will be returned.</p>
     *
     *	@param	node	The node to pull child elements from.
     *	@param	name	The name of the Elements to return.
     *
     *	@return	List of child elements found below the given node matching
     *		the name (if provided).
     */
    public List getChildElements(Node node, String name) {
	// Get the child nodes
	NodeList nodes = node.getChildNodes();
	if (nodes == null) {
	    // No children, just return an empty List
	    return new ArrayList(0);
	}

	// Create a new List to store the child Elements
	List list = new ArrayList();

	// Add all the child Elements to the List
	Node childNode = null;
	for (int idx=0; idx<nodes.getLength(); idx++) {
	    childNode = nodes.item(idx);
	    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
		// Skip TEXT_NODE and other Node types
		continue;
	    }

	    // Add to the list if name is null, or it matches the node name
	    if ((name== null) || childNode.getNodeName().equalsIgnoreCase(name)) {
		list.add(childNode);
	    }
	}

	// Return the list of Elements
	return list;
    }


    /**
     *	This method returns the String representation of all the
     *	Node.TEXT_NODE nodes that are children of the given Node.
     *
     *	@param	node	The node to pull child elements from.
     *
     *	@return	The String representation of all the Node.TEXT_NODE type nodes
     *		under the given node.
     */
    public String getTextNodesAsString(Node node) {
	// Get the child nodes
	NodeList nodes = node.getChildNodes();
	if (nodes == null) {
	    // No children, return null
	    return null;
	}

	// Create a StringBuffer
	StringBuffer buf = new StringBuffer("");

	// Add all the child Element values to the StringBuffer
	Node childNode = null;
	for (int idx=0; idx<nodes.getLength(); idx++) {
	    childNode = nodes.item(idx);
	    if ((childNode.getNodeType() != Node.TEXT_NODE) &&
		    (childNode.getNodeType() != Node.CDATA_SECTION_NODE)) {
		// Skip all other Node types
		continue;
	    }
	    buf.append(childNode.getNodeValue());
	}

	// Return the String
	return buf.toString();
    }



    /**
     *	This method returns a Map of all attributes for the given Node.  Each
     *	attribute name will be stored in the map in lower case so case can be
     *	ignored.
     *
     *	@param	node	The node to pull attributes from.
     *
     *	@return	Map of attributes found on the given node.
     */
    public Map getAttributes(Node node) {
	// Get the attributes
	NamedNodeMap attributes = node.getAttributes();
	if ((attributes == null) || (attributes.getLength() == 0)) {
	    // No attributes, just return an empty Map
	    return new HashMap(0);
	}

	// Create a Map to contain the attributes
	Map map = new HashMap();

	// Add all the attributes to the Map
	Node attNode = null;
	for (int idx=0; idx<attributes.getLength(); idx++) {
	    attNode = attributes.item(idx);
	    map.put(attNode.getNodeName().toLowerCase(),
		    attNode.getNodeValue());
	}

	// Return the map
	return map;
    }


    /**
     *	This utility method returns the requested component type.  If it is
     *	not found, it throws an IllegalArgumentException.
     *
     *	@param	elt	A LayoutElement whose root is {@link LayoutDefinition}
     *	@param	type	The String type to lookup
     *
     *	@return the ComponentType
     */
    public ComponentType getComponentType(LayoutElement elt, String type) {
	// Find the ComponentType
	ComponentType componentType =
	    elt.getLayoutDefinition().getComponentType(type);
	if (componentType == null) {
	    throw new IllegalArgumentException("ComponentType '"+type+
		"' not defined!");
	}
	return componentType;
    }



    //////////////////////////////////////////////////////////////////////
    //	Constants
    //////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_ELEMENT	    =
	"attribute";
    public static final String COMPONENT_ELEMENT	    =
	"component";
    public static final String COMPONENT_TYPE_ELEMENT	    =
	"componenttype";
    public static final String EVENT_ELEMENT		    =
	"event";
    public static final String FACET_ELEMENT		    =
	"facet";
    public static final String FOREACH_ELEMENT		    =
	"foreach";
    public static final String HANDLER_ELEMENT		    =
	"handler";
    public static final String HANDLERS_ELEMENT		    =
	"handlers";
    public static final String HANDLER_DEFINITION_ELEMENT   =
	"handlerdefinition";
    public static final String IF_ELEMENT		    =
	"if";
    public static final String INPUT_DEF_ELEMENT	    =
	"inputdef";
    public static final String INPUT_ELEMENT		    =
	"input";
    public static final String LAYOUT_DEFINITION_ELEMENT    =
	"layoutdefinition";
    public static final String LAYOUT_ELEMENT		    =
	"layout";
    public static final String LIST_ELEMENT		    =
	"list";
    public static final String MARKUP_ELEMENT		    =
	"markup";
    public static final String OPTION_ELEMENT		    =
	"option";
    public static final String OUTPUT_DEF_ELEMENT	    =
	"outputdef";
    public static final String OUTPUT_MAPPING_ELEMENT	    =
	"outputmapping";
    public static final String STATIC_TEXT_ELEMENT	    =
	"statictext";
    public static final String TYPES_ELEMENT		    =
	"types";
    public static final String RESOURCES_ELEMENT	    =
	"resources";
    public static final String RESOURCE_ELEMENT		    =
	"resource";
    public static final String WHILE_ELEMENT		    =
	"while";

    public static final String CLASS_NAME_ATTRIBUTE	    =
	"classname";
    public static final String CONDITION_ATTRIBUTE	    =
	"condition";
    public static final String DEFAULT_ATTRIBUTE	    =
	"default";
    public static final String DESCRIPTION_ATTRIBUTE	    =
	"description";
    public static final String EXTRA_INFO_ATTRIBUTE	    =
	"extrainfo";
    public static final String FACTORY_CLASS_ATTRIBUTE	    =
	"factoryclass";
    public static final String ID_ATTRIBUTE		    =
	"id";
    public static final String KEY_ATTRIBUTE		    =
	"key";
    public static final String LIST_ATTRIBUTE		    =
	"list";
    public static final String METHOD_NAME_ATTRIBUTE	    =
	"methodname";
    public static final String NAME_ATTRIBUTE		    =
	"name";
    public static final String OUTPUT_NAME_ATTRIBUTE	    =
	"outputname";
    public static final String OVERWRITE_ATTRIBUTE	    =
	"overwrite";
    public static final String PROPERTY_ATTRIBUTE	    =
	"property";
    public static final String RENDERED_ATTRIBUTE	    =
	"rendered";
    public static final String REQUIRED_ATTRIBUTE	    =
	"required";
    public static final String TAG_ATTRIBUTE		    =
	"tag";
    public static final String TARGET_KEY_ATTRIBUTE	    =
	"targetkey";
    public static final String TARGET_TYPE_ATTRIBUTE	    =
	"targettype";
    public static final String TYPE_ATTRIBUTE		    =
	"type";
    public static final String VALUE_ATTRIBUTE		    =
	"value";

    public static final String AUTO_RENDERED		    =
	"auto";


    /**
     *	This is used to set the "value" option for static text fields.
     */
//    public static final String VALUE_OPTION	=   "value";

    private URL		    _url		= null;
    private EntityResolver  _entityResolver	= null;
    private ErrorHandler    _errorHandler	= null;
    private String	    _baseURI		= null;

    private Map		    _handlerDefs	= new HashMap();
}
