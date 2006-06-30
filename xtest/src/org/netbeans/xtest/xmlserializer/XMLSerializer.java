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



package org.netbeans.xtest.xmlserializer;

import org.netbeans.xtest.util.XMLFactoryUtil;
import org.netbeans.xtest.xmlserializer.ClassMappingRegistry.ContainerFieldMapping;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

// XML DOM imports
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;

public class XMLSerializer {
    
    
    public static final int ALL = -1;
    
    
    // private contructor - this class cannot be instintiated
    private XMLSerializer() {
    }
    
    
    // return true if the argument class is XML serializable
    public static boolean isClassSerializable(Class clazz) {
        // is the class XML serializable
        if (XMLSerializable.class.isAssignableFrom(clazz)) {
            return true;
        }
        
        for (int i=0; i < SERIALIZABLE_CLASSES.length; i++) {
            if (SERIALIZABLE_CLASSES[i].getName().equals(clazz.getName())) {
                return true;
            }
        }
        // class was not found as a serializable
        return false;
    }
    
    
    /*
     * returns true if given class implements
     * XMLBean interface
     */
    public static boolean implementsXMLSerializable(Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        for (int i=0; i<interfaces.length ; i++) {
            if (interfaces[i].equals(XMLSerializable.class)) {
                return true;
            }
        }
        return false;
    }
    
    
    // serialize to DOM document
    public static Document toDOMDocument(XMLSerializable object) throws XMLSerializeException, ParserConfigurationException {
        return toDOMDocument(object,ALL);
    }
    
    // serialize to DOM document
    public static Document toDOMDocument(XMLSerializable object, int depth) throws XMLSerializeException, ParserConfigurationException {
        Document doc = XMLFactoryUtil.newDocumentBuilder().newDocument();
        doc.appendChild(toDOMElement(object, doc, depth));
        return doc;
    }
    
    // serialize to DOM element
    public static Element toDOMElement(XMLSerializable object, Document doc, int depth) throws XMLSerializeException {
        
        String elementName = GlobalMappingRegistry.getElementNameForClass(object.getClass());
        
        if (elementName == null) {
            // cannot find element name - cannot serialize this bean
            // throw exception
            throw new XMLSerializeException("Root element corresponding to "+object.getClass()+" is not registered. Cannot serialize to XML.");
        }
        
        Element element = doc.createElement(elementName);
        appendToDOMElement(doc, element, object, depth);
        
        return element;
    }
    
    
    // serialize from DOM document
    public static XMLSerializable getXMLSerializable(Document doc) throws XMLSerializeException {
        return getXMLSerializable(doc,ALL);
    }
    
    // serialize from DOM document
    public static XMLSerializable getXMLSerializable(Document doc, int depth) throws XMLSerializeException {
        Element element = doc.getDocumentElement();
        return getXMLSerializable(element,depth);
    }
    
    // serialize from DOM element
    public static XMLSerializable getXMLSerializable(Element element, int depth) throws XMLSerializeException {    	        
        // if depth is zero, we don't want dig deeper        
        if (depth==0) {
            // there is nothing to be done ...
            return null;
        }
        // otherwise continue with getting the bean
        String elementName = element.getTagName();
        // find a class to be loaded from this element
        Class elementClass = GlobalMappingRegistry.getClassForElementName(elementName);
        if (elementClass == null) {
            throw new XMLSerializeException("Cannot find registered class for element "+elementName);
        }
        // process the class
        XMLSerializable xmlSerializable = processXMLSerializableClass(element,elementClass,depth);
        if (xmlSerializable instanceof Validation) {
            ((Validation)xmlSerializable).validate();
        }
        if (xmlSerializable instanceof PostInitialization) {
            ((PostInitialization)xmlSerializable).postInitialize();
        }
        return xmlSerializable;
    }
    
    
    
    //
    // private constants
    //
    
    // list of classes directly (no subclasses)
    // serializable by xmlserializable package
    private static final Class[] SERIALIZABLE_CLASSES = {
        // primitive numbers
        Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
        Float.TYPE, Double.TYPE,
        // other primitives
        Boolean.TYPE, Character.TYPE,
        // number classes
        Byte.class, Short.class, Integer.class, Long.class,
        Float.class, Double.class, BigInteger.class,
        BigDecimal.class,
        // other classes
        Boolean.class, Character.class, String.class,
        Date.class, Timestamp.class
    };
    
    //
    // private methods - serialize to XML
    //
    
    private static void appendToDOMElement(Document doc, Element element, XMLSerializable object, int depth) throws XMLSerializeException {
        
        // check the depth
        if (depth == 0) {
            // return , there is nothing to do
            return;
        }
        
        // get the registered field names for this object
        ClassMappingRegistry registry = object.registerXMLMapping();
        Class clazz = object.getClass();
        ClassMappingRegistry.FieldMapping[] fieldMappings = registry.getFieldMappingsFromRegistry();
        
        for (int i=0; i < fieldMappings.length; i++) {
            
            Field mappedField = fieldMappings[i].getField();
            mappedField.setAccessible(true);
            
            try {
                // get the value of the mapped field
                Object fieldValue = mappedField.get(object);
                
                if (fieldValue != null) {
                    String xmlName = fieldMappings[i].getXMLName();
                    if (fieldMappings[i].isSimple()) {
                        // simple field serialization
                        serializeSimpleField(doc,element,(ClassMappingRegistry.SimpleFieldMapping)fieldMappings[i],
                        fieldValue,depth);
                        
                    } else if (fieldMappings[i].isContainer()) {
                        // container field serialization
                        serializeContainerField(doc,element,(ClassMappingRegistry.ContainerFieldMapping)fieldMappings[i],
                        fieldValue,depth);
                    }
                }  // fieldValue != null
            } catch (IllegalAccessException iae) {
                throw new XMLSerializeException("Cannot access field "+mappedField.getName()+" in class "+clazz.getName(),iae);
            }
        } // for cycle
    }
    
    
    
    // simple field type serializing method
    private static void serializeSimpleField(Document doc, Element element, ClassMappingRegistry.SimpleFieldMapping fieldMapping, Object fieldValue, int depth) throws XMLSerializeException {
        switch (fieldMapping.getXMLType()) {
            case ClassMappingRegistry.ATTRIBUTE :
                insertAttribute(element, fieldMapping.getXMLName(), fieldValue);
                break;
                
            case ClassMappingRegistry.ELEMENT :
                insertElement(doc, element, fieldMapping.getXMLName(), fieldValue, depth);
                break;
                
            case ClassMappingRegistry.PCDATA :
                insertPCDATA(doc, element, fieldValue);
                break;
                
            case ClassMappingRegistry.CDATA :
                insertCDATA(doc, element, fieldValue);
                break;
                
            default :
                throw new XMLSerializeException("Unknown xml mapping type - should not happen");
        }
    }
    
    // container field type serializing method
    private static void serializeContainerField(Document doc,	Element element, ClassMappingRegistry.ContainerFieldMapping containerMapping, Object fieldValue, int depth) throws XMLSerializeException {
        String containerRootElementName = containerMapping.getXMLName();
        switch (containerMapping.getContainerMappingType()) {
            case ClassMappingRegistry.DIRECT :
                
                if (fieldValue.getClass().isArray()) {
                    
                    int arraySize = Array.getLength(fieldValue);
                    for (int i=0; i<arraySize; i++) {
                        Object oneItem = Array.get(fieldValue, i);
                        if (oneItem != null ) {
                            insertElement(doc, element, containerRootElementName, oneItem, depth);
                        }
                    }
                    
                } else if (Collection.class.isAssignableFrom(fieldValue.getClass())) {
                    
                    Iterator i = ((Collection)fieldValue).iterator();
                    while (i.hasNext()) {
                        Object oneItem = i.next();
                        if (oneItem != null) {
                            insertElement(doc, element, containerRootElementName, oneItem, depth);
                        }
                    }
                    
                }
                
                break;
            case ClassMappingRegistry.SUBELEMENT :
                Element containerRootElement = doc.createElement(containerRootElementName);
                
                if (fieldValue.getClass().isArray()) {
                    
                    int arraySize = Array.getLength(fieldValue);
                    for (int i = 0; i < arraySize; i++) {
                        Object oneItem = Array.get(fieldValue, i);
                        if (oneItem != null) {
                            // find record in the containerMappingRegistry
                            String subElementName = containerMapping.getElementNameForClass(oneItem.getClass());
                            if (subElementName != null) {
                                insertElement(doc,containerRootElement,subElementName,oneItem,depth);
                            } else {
                                throw new XMLSerializeException("Cannot find appropriate mapping for class "
                                +oneItem.getClass().getName()+" when serializing container field "
                                +containerMapping.getField().getName());
                                
                            }
                        }
                    }
                    
                } else if (Collection.class.isAssignableFrom(fieldValue.getClass())) {
                    
                    Iterator i = ((Collection)fieldValue).iterator();
                    while (i.hasNext()) {
                        Object oneItem = i.next();
                        if (oneItem != null) {
                            String subElementName = containerMapping.getElementNameForClass(oneItem.getClass());
                            if (subElementName != null) {
                                insertElement(doc,containerRootElement,subElementName,oneItem,depth);
                            } else {
                                throw new XMLSerializeException("Cannot find appropriate mapping for class "
                                +oneItem.getClass().getName()+" when serializing container field "
                                +containerMapping.getField().getName());
                            }
                        }
                    }
                }
                element.appendChild(containerRootElement);
                break;
            default :
                throw new XMLSerializeException("Unknown container mapping type - should not happen");
        } // switch
    }
    
    
    // serialize attribute
    private static void insertAttribute(Element element, String attributeName, Object attributeValue ) throws XMLSerializeException {
        element.setAttribute(attributeName,attributeValue.toString());
    }
    
    // serialize element (only a single element)
    private static void insertElement(Document doc, Element element, String elementName, Object elementValue, int depth) throws XMLSerializeException {
        Element newElement = doc.createElement(elementName);
        element.appendChild(newElement);
        // check whether elementValue is either collection or array
        
        if ( implementsXMLSerializable(elementValue.getClass())) {
            // recursive iteration
            appendToDOMElement(doc, newElement, (XMLSerializable)elementValue, depth-1);
        } else {
            // just save the field value in the element
            Text textNode = doc.createTextNode(elementValue.toString());
            newElement.appendChild(textNode);
        }
    }
    
    
    
    // serialize PCDATA
    private static void insertPCDATA(Document doc, Element element, Object pcdataValue) {
        Text aText = doc.createTextNode(pcdataValue.toString());
        element.appendChild(aText);
    }
    
    // serialize CDATA
    private static void insertCDATA(Document doc, Element element, Object cdataValue) {
        CDATASection cdataSection =  doc.createCDATASection(cdataValue.toString());
        element.appendChild(cdataSection);
    }
    
    
    //
    // private methods - deserialize from XML
    //
    
    /**
     * Method processXMLSerializable.
     * @param xmlSerializable
     * @param element
     * @param depth
     */
    
    private static XMLSerializable processXMLSerializableClass(Element element, Class clazz, int depth) throws XMLSerializeException {
        // instintiate this class
        try {
/*        	Constructor aConstructor = clazz.getConstructor();
                aConstructor.setAccessible(true);
                XMLSerializable xmlSerializable = (XMLSerializable) aConstructor.newInstance(null);
 **/
            XMLSerializable xmlSerializable = (XMLSerializable) clazz.newInstance();
            // now work on the rest of the XMLSerializable object
            processXMLSerializable(xmlSerializable, element, depth);
            return xmlSerializable;
        } catch (InstantiationException ie) {
            throw new XMLSerializeException("Cannot instintiate class "+clazz.getName(),ie);
        } catch (IllegalAccessException iae) {
            throw new XMLSerializeException("Cannot instintiate class "+clazz.getName()+" - illegal access",iae);
        }
    }
    
    private static void processXMLSerializable(XMLSerializable xmlSerializable, Element element, int depth) throws XMLSerializeException {
        // start with attributes
        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            processAttributes(xmlSerializable,attributes);
        }
        // continue with elements/PCDATA/CDATA
        NodeList subElements = element.getChildNodes();
        if (subElements != null) {
            FieldObjectsMap fieldObjectsMap = new FieldObjectsMap();
            for (int i=0; i<subElements.getLength(); i++) {
                Node elementNode = subElements.item(i);
                switch (elementNode.getNodeType()) {
                    case Node.ELEMENT_NODE:
                        // process subelement
                        processElement(xmlSerializable,(Element)elementNode, fieldObjectsMap, depth);
                        break;
                    case Node.TEXT_NODE:
                        // process textnode (PCDATA)
                        processPCDATA(xmlSerializable,elementNode);
                        break;
                    case Node.CDATA_SECTION_NODE:
                        // process CDATA
                        processCDATA(xmlSerializable,elementNode);
                        break;
                    default:
                        // cannot recoginze the rest of nodes
                        // currently do nothing
                        // or may be better throw an exception
                }
            } // end for
            // fill the container field with gathered objects
            Field[] processedFields = fieldObjectsMap.getFields();
            for (int i=0; i < processedFields.length; i++) {
                Object[] objects = fieldObjectsMap.getObjectInstances(processedFields[i]);
                try {
                    insertObjectsIntoArray(xmlSerializable, processedFields[i], objects);
                } catch (IllegalAccessException iae) {
                    throw new XMLSerializeException("Caught IllegalAccessException when setting objects into array");
                }
            }
        }
    }
    
    /**
     * Method insertObjectsIntoArray.
     * @param xmlSerializable
     * @param field
     * @param objects
     */
    private static void insertObjectsIntoArray(XMLSerializable xmlSerializable, Field arrayField, Object[] objects) throws IllegalAccessException {
        Class arrayComponentClass = arrayField.getType().getComponentType();
        Object resultingArray = Array.newInstance(arrayComponentClass,objects.length);
        for (int i=0; i < objects.length; i++) {
            Object object = objects[i];
            if (object instanceof XMLPrimitiveWrapper) {
                ((XMLPrimitiveWrapper)object).setItemInArray(resultingArray,i);
            } else {
                // set the field alone
                Array.set(resultingArray,i,object);
            }
        }
        // now the array is done -> set it to the field
        arrayField.setAccessible(true);
        arrayField.set(xmlSerializable,resultingArray);
    }
    
    
    // process PCDATA
    private static void processPCDATA(XMLSerializable xmlSerializable, Node pcdata) throws XMLSerializeException {
        String value = pcdata.getNodeValue();
        if (value != null) {
            String trimmedValue = value.trim();
            if (trimmedValue.length() > 0) {
                ClassMappingRegistry registry = xmlSerializable.registerXMLMapping();
                // need to do
                throw new UnsupportedOperationException("processPCDATA - not yet implemented:'"+pcdata.getNodeValue()+"'");
            }
        }
    }
    
    // process CDATA
    private static void processCDATA(XMLSerializable xmlSerializable, Node cdata) throws XMLSerializeException {
        throw new UnsupportedOperationException("processCDATA - not yet implemented");
    }
    
    // process elements
    private static void processElement(XMLSerializable xmlSerializable, Element element, FieldObjectsMap fieldObjectsMap, int depth) throws XMLSerializeException {
        ClassMappingRegistry registry = xmlSerializable.registerXMLMapping();
        String elementName = element.getNodeName();
        
        ClassMappingRegistry.FieldMapping fieldMapping = registry.getFieldMappingFromRegistry(elementName);
        if (fieldMapping == null) {
            // no field mapping found !!!
            throw new XMLSerializeException("Cannot find appropriate mapping for element "+elementName+" in class "+xmlSerializable.getClass().getName());
        }
        if (fieldMapping.getXMLType() != ClassMappingRegistry.ELEMENT) {
            throw new XMLSerializeException("Field "+fieldMapping.getField().getName()+" in class "
            +xmlSerializable.getClass().getName()+"	is not registered as xml element");
        }
        
        if (fieldMapping.isSimple()) {
            // field is simple - load the simple field
            loadSimpleField(element,(ClassMappingRegistry.SimpleFieldMapping)fieldMapping, xmlSerializable, depth);
        } else if (fieldMapping.isContainer()) {
            // field is complex - load the complex field
            ClassMappingRegistry.ContainerFieldMapping containerMapping = (ClassMappingRegistry.ContainerFieldMapping) fieldMapping;
            loadContainerField(element, containerMapping, xmlSerializable, fieldObjectsMap, depth);
        } else {
            // this should not happeen !!!!
            throw new XMLSerializeException("Unknown fieldMapping class:"+fieldMapping.getClass().getName()+" - should not happen");
        }
    }
    
    
    
    // process attributes
    private static void processAttributes(XMLSerializable xmlSerializable, NamedNodeMap attributes) throws XMLSerializeException {
        ClassMappingRegistry registry = xmlSerializable.registerXMLMapping();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String attributeName = attribute.getNodeName();
            // do we have such a attribute in XMLSerializable registry
            ClassMappingRegistry.FieldMapping fieldMapping = registry.getFieldMappingFromRegistry(attributeName);
            if (fieldMapping == null) {
                // no field mapping found !!!
                throw new XMLSerializeException("Cannot find appropriate mapping for attribute "+attributeName+" in class"+xmlSerializable.getClass().getName());
            }
            if (fieldMapping.getXMLType() != ClassMappingRegistry.ATTRIBUTE) {
                throw new XMLSerializeException("Field "+fieldMapping.getField().getName()+" in class "
                +xmlSerializable.getClass().getName()+"	is not registered as xml attribute");
            }
            // everything looks ok - get the value
            String value = attribute.getNodeValue();
            // set the field in the object with obtained value
            if (value != null) {
                try {
                    XMLPrimitiveWrapper xmlPrimitive = new XMLPrimitiveWrapper(fieldMapping.getField().getType(),value);
                    xmlPrimitive.setFieldInObject(fieldMapping.getField(),xmlSerializable);
                } catch (IllegalAccessException iae) {
                    throw new XMLSerializeException("Cannot set field "+fieldMapping.getField().getName()
                    +" with value "+value+" in class "+xmlSerializable.getClass().getName(),iae);
                } catch (IllegalArgumentException iae) {
                    throw new XMLSerializeException("Cannot set field "+fieldMapping.getField().getName()
                    +"  with value "+value+" in class "+xmlSerializable.getClass().getName(),iae);
                }
            }
        }
    }
    
    
    // fill in simple field with value of pcdata from this element
    private static void loadSimpleField(Element parentElement, ClassMappingRegistry.SimpleFieldMapping mapping, XMLSerializable parentObject, int depth) throws XMLSerializeException {
        Field mappedField = mapping.getField();
        Class fieldClass = mappedField.getType();
        String elementName = parentElement.getNodeName();
        if (XMLSerializable.class.isAssignableFrom(fieldClass)) {
            // xmlserializable class
            XMLSerializable xmlSerializable = processXMLSerializableClass(parentElement,fieldClass,depth);
            try {
                mappedField.setAccessible(true);
                mappedField.set(parentObject,xmlSerializable);
                return;
            } catch (IllegalAccessException iae) {
                throw new XMLSerializeException("Cannot load field " +mappedField.getName()+ "in class "
                +parentObject.getClass().getName() + " - illegal access", iae);
            }
        } else if (isClassSerializable(fieldClass)) {
            // primitive class - get PCDATA child node
            NodeList children = parentElement.getChildNodes();
            if (children != null) {
                if (children.getLength() == 1) {
                    Node pcData = children.item(0);
                    if (pcData.getNodeType() == Node.TEXT_NODE) {
                        // get the value and insert in the field
                        String value = pcData.getNodeValue();
                        if (value != null) {
                            try {
                                XMLPrimitiveWrapper xmlPrimitive = new XMLPrimitiveWrapper(mappedField.getType(),value);
                                xmlPrimitive.setFieldInObject(mappedField,parentObject);
                                return;
                            } catch (IllegalAccessException iae) {
                                throw new XMLSerializeException("Cannot load field " +mappedField.getName()+ "in class "
                                +parentObject.getClass().getName() + " - illegal access", iae);
                            }
                        }
                    }
                }
            }
            // thereis something wrong
            throw new XMLSerializeException("Unrecoverable problem when deserializing element "+elementName
            + " to field " + mappedField.getName()+ " in class "	+ fieldClass.getClass().getName());
        } else {
            // cannot deserialize class
            throw new XMLSerializeException("Field " + mappedField.getName() + "is of class "
            + fieldClass.getName()	+ " which cannot be xml serialized ");
        }
    }
    
    
    
    private static Object loadSimpleField(Element element, Class elementClass, int depth) throws XMLSerializeException {
        if (XMLSerializable.class.isAssignableFrom(elementClass)) {
            // xmlserializable class
            return processXMLSerializableClass(element,elementClass,depth);
        } else if (isClassSerializable(elementClass)) {
            // primitive class - get PCDATA child node
            NodeList children = element.getChildNodes();
            if (children != null) {
                if (children.getLength() == 1) {
                    Node pcData = children.item(0);
                    if (pcData.getNodeType() == Node.TEXT_NODE) {
                        // get the value and insert in the field
                        String value = pcData.getNodeValue();
                        if (value != null) {
                            try {
                                return new XMLPrimitiveWrapper(elementClass,value);
                            } catch (IllegalArgumentException iae) {
                                throw new XMLSerializeException("Cannot create XMLPrimitiveWrapper for class "+
                                elementClass.getName() + " and value "+value+" - NumberFormatException", iae);
                            }
                        }
                    }
                }
            }
            // thereis something wrong
            throw new XMLSerializeException("Unrecoverable problem when deserializing element "+element
            + " to class "	+ elementClass.getName());
        } else {
            // cannot deserialize class
            throw new XMLSerializeException("Class "+elementClass.getName()+
            " is not supported for XML serialization");
        }
    }
    
    /**
     * Method loadContainerField.
     * @param element
     * @param containerMapping
     * @param xmlSerializable
     * @param depth
     */
    private static void loadContainerField(Element parentElement, ContainerFieldMapping containerMapping, XMLSerializable xmlSerializable,  FieldObjectsMap fieldObjectsMap, int depth) throws XMLSerializeException {
        Field field = containerMapping.getField();
        Class fieldClass = field.getType();
        
        switch (containerMapping.getContainerMappingType()) {
            case ClassMappingRegistry.DIRECT:
                if (fieldClass.isArray()) {
                    Class componentClass = fieldClass.getComponentType();
                    Object object;
                    if (XMLSerializable.class.isAssignableFrom(componentClass)) {
                        object = processXMLSerializableClass(parentElement, componentClass, depth);
                    } else {
                        object = loadSimpleField(parentElement,componentClass,depth);
                    }
                    fieldObjectsMap.addObjectInstance(field, object);
                } else if (Collection.class.isAssignableFrom(fieldClass)) {
                    throw new UnsupportedOperationException("direct mapping collection - not yet implemented");
                } else {
                    //this should neve happend ....
                    throw new XMLSerializeException("Field contains unrecognized type");
                };
                break;
            case ClassMappingRegistry.SUBELEMENT:
                if (fieldClass.isArray()) {
                    Class componentClass = fieldClass.getComponentType();
                    NodeList children = parentElement.getChildNodes();
                    if (children != null) {
                        int length = children.getLength();
                        // create the array
                        Object array = Array.newInstance(componentClass,length);
                        // insert objects
                        for (int i =0; i < children.getLength(); i++) {
                            Node childElement = children.item(i);
                            if (childElement.getNodeType()==Node.ELEMENT_NODE) {
                                Object object;
                                if (XMLSerializable.class.isAssignableFrom(componentClass)) {
                                    object = processXMLSerializableClass((Element)childElement, componentClass, depth);
                                } else {
                                    object = loadSimpleField((Element)childElement,componentClass,depth);
                                }
                                fieldObjectsMap.addObjectInstance(field, object);
                            } else {
                                // other node types are ignored ....
                            }
                        }
                    } else {
                        // nothing to do - may throw exception
                    }
                    // for each subelement create
                } else if (Collection.class.isAssignableFrom(fieldClass)) {
                    throw new UnsupportedOperationException("subelement mapping collection - not yet implemented");
                } else {
                    //this should neve happend ....
                    throw new XMLSerializeException("Field contains unrecognized type");
                }
                break;
        }
        
    }
    
    
    
    
    // helper class for hodling element - xmlserializable objects
    private static class FieldObjectsMap {
        
        private HashMap map;
        
        public FieldObjectsMap() {
            map = new HashMap();
        }
        
        public void addObjectInstance(Field field, Object objectInstance) {
            ArrayList objects = (ArrayList)map.get(field);
            if (objects != null) {
                objects.add(objectInstance);
            } else {
                objects = new ArrayList();
                objects.add(objectInstance);
                map.put(field,objects);
            }
        }
        
        public Object[] getObjectInstances(Field field) {
            ArrayList objectInstances = (ArrayList)map.get(field);
            if (objectInstances != null) {
                return objectInstances.toArray();
            } else {
                return null;
            }
        }
        
        public Field[] getFields() {
            return (Field[])(map.keySet().toArray(new Field[0]));
        }
        
        public boolean removeField(Field field) {
            if (map.containsKey(field)) {
                if (map.remove(field)!=null) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    
    static class XMLPrimitiveWrapper {
        
        private Class primitiveType;
        
        private byte 	xmlByte;
        private short	xmlShort;
        private int	xmlInt;
        private long	xmlLong;
        private float	xmlFloat;
        private double xmlDouble;
        private boolean xmlBoolean;
        private char	xmlChar;
        private Object xmlObject;
        
        // forbidden no argument constructor
        private XMLPrimitiveWrapper() {}
        
        // constructor from class/string value pair
        public XMLPrimitiveWrapper(Class primitiveType, String value) {
                        /*
                        if (!primitiveType.isPrimitive()) {
                                // throw an exception - not a primitive
                                throw new IllegalArgumentException("supplied class :"+primitiveType.getName()+" is not of a primitive type");
                        }
                         */
            this.primitiveType = primitiveType;
            String typeName = primitiveType.getName();
            
            // java primitives
            if (typeName.equals("byte")) {
                xmlByte = Byte.parseByte(value);
            } else if (typeName.equals("short")) {
                xmlShort = Short.parseShort(value);
            } else if (typeName.equals("int")) {
                xmlInt = Integer.parseInt(value);
            } else if (typeName.equals("long")) {
                xmlLong = Long.parseLong(value);
            } else if (typeName.equals("float")) {
                xmlFloat = Float.parseFloat(value);
            } else if (typeName.equals("double")) {
                xmlDouble = Double.parseDouble(value);
            } else if (typeName.equals("boolean")) {
                xmlBoolean = Boolean.valueOf(value).booleanValue();
            } else if (typeName.equals("character")) {
                if (value.length() > 0) {
                    xmlChar = value.charAt(0);
                } else {
                    throw new NumberFormatException("value string does not contain any character");
                }
            } else
                
                // number classes
                if (typeName.equals("java.lang.Byte")) {
                    xmlObject = new Byte(value);
                } else if (typeName.equals("java.lang.Short")) {
                    xmlObject = new Short(value);
                } else if (typeName.equals("java.lang.Integer")) {
                    xmlObject = new Integer(value);
                } else if (typeName.equals("java.lang.Long")) {
                    xmlObject = new Long(value);
                } else if (typeName.equals("java.lang.Float")) {
                    xmlObject = new Float(value);
                } else if (typeName.equals("java.lang.Double")) {
                    xmlObject = new Double(value);
                } else if (typeName.equals("java.math.BigInteger")) {
                    xmlObject = new BigInteger(value);
                } else if (typeName.equals("java.math.BigDecimal")) {
                    xmlObject = new BigDecimal(value);
                } else
                    
                    // other classes
                    if (typeName.equals("java.lang.Boolean")) {
                        xmlObject = new Boolean(value);
                    } else if (typeName.equals("java.lang.Character")) {
                        if (value.length() > 0) {
                            xmlObject = new Character(value.charAt(0));
                        } else {
                            throw new NumberFormatException("value string does not contain any character");
                        }
                    } else if (typeName.equals("java.lang.String")) {
                        xmlObject = new String(value);
                    } else if (typeName.equals("java.sql.Date")) {
                        xmlObject = java.sql.Date.valueOf(value);
                    } else if (typeName.equals("java.sql.Timestamp")) {
                        xmlObject = java.sql.Timestamp.valueOf(value);
                    } else {
                        throw new IllegalArgumentException("Class '" + typeName + "' not supported");
                    }
            
        }
        
        
        public Object getWrappedXMLPrimitive() {
            // java classes
            if (xmlObject != null ) return xmlObject;
            //  java primitives
            if (primitiveType.equals(Byte.TYPE)) return new Byte(xmlByte);
            if (primitiveType.equals(Short.TYPE)) return new Short(xmlShort);
            if (primitiveType.equals(Integer.TYPE)) return new Integer(xmlInt);
            if (primitiveType.equals(Long.TYPE)) return new Long(xmlLong);
            if (primitiveType.equals(Float.TYPE)) return new Float(xmlFloat);
            if (primitiveType.equals(Double.TYPE)) return new Double(xmlDouble);
            if (primitiveType.equals(Boolean.TYPE)) return new Boolean(xmlBoolean);
            if (primitiveType.equals(Character.TYPE)) return new Character(xmlChar);
            // no wrapper was found (strange ...)
            return null;
        }
        
        // set field in a supplied object to the object wrapped by this class
        public void setFieldInObject(Field field, Object obj) throws IllegalAccessException {
            if ((field == null) | (obj == null)) {
                throw new IllegalArgumentException("arguments cannot be null");
            }
            field.setAccessible(true);
            Class fieldType = field.getType();
            if (!primitiveType.equals(fieldType)) {
                // problem - field is not of required type
                return;
            }
            String fieldTypeName = fieldType.getName();
            if (fieldTypeName.equals("byte")) {
                field.setByte(obj, xmlByte);
            } else if (fieldTypeName.equals("short")) {
                field.setShort(obj, xmlShort);
            } else if (fieldTypeName.equals("int")) {
                field.setInt(obj, xmlInt);
            } else if (fieldTypeName.equals("long")) {
                field.setLong(obj, xmlLong);
            } else if (fieldTypeName.equals("float")) {
                field.setFloat(obj, xmlFloat);
            } else if (fieldTypeName.equals("double")) {
                field.setDouble(obj, xmlDouble);
            } else if (fieldTypeName.equals("boolean")) {
                field.setBoolean(obj, xmlBoolean);
            } else if (fieldTypeName.equals("character")) {
                field.setChar(obj, xmlChar);
            } else if (xmlObject != null) {
                field.set(obj,xmlObject);
            }
        }
        
        // set item in an array
        public void setItemInArray(Object array, int index) throws IllegalAccessException {
            if (array == null) {
                throw new IllegalArgumentException("array cannot be null");
            }
            if (!array.getClass().isArray()) {
                throw new IllegalArgumentException("supplied array is not of array type");
            }
            
            Class componentType = array.getClass().getComponentType();
            if (!primitiveType.equals(componentType)) {
                // problem - field is not of required type
                return;
            }
            
            String componentTypeName = componentType.getName();
            if (componentTypeName.equals("byte")) {
                Array.setByte(array, index, xmlByte);
            } else if (componentTypeName.equals("short")) {
                Array.setShort(array, index, xmlShort);
            } else if (componentTypeName.equals("int")) {
                Array.setInt(array, index, xmlInt);
            } else if (componentTypeName.equals("long")) {
                Array.setLong(array, index, xmlLong);
            } else if (componentTypeName.equals("float")) {
                Array.setFloat(array, index, xmlFloat);
            } else if (componentTypeName.equals("double")) {
                Array.setDouble(array, index, xmlDouble);
            } else if (componentTypeName.equals("boolean")) {
                Array.setBoolean(array, index, xmlBoolean);
            } else if (componentTypeName.equals("character")) {
                Array.setChar(array, index, xmlChar);
            } else if (xmlObject != null) {
                Array.set(array, index, xmlObject);
            }
        }
    }
    
    
}

