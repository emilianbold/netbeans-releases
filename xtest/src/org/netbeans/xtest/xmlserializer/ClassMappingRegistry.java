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


 /*
 * XMLBeanRegistry.java
 *
 * Created on September 9, 2002, 11:38 PM
 */

package org.netbeans.xtest.xmlserializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;


/** ClassMappingRegistry is a simple registry implementation for XMLSerializer. It has
 * two basic functionalities:
 *
 * 1: Classes objects can be mapped to element names and vice versa (only root elements can be
 * mapped to classes)
 *
 * 2: Fields in classes implementing XMLSerializable inteface can be mapped to
 * element/attribute/pcdata/cdata. When mapping to element/attribute, a name of
 * it in the xml from can be registered
 * @author martin.brehovsky@sun.com
 */
public class ClassMappingRegistry {

	/** mapping does not exist */
	public static final short NO_MAPPING = 0x0;
	/** unkonwn type */
	public static final short UNKNOWN_TYPE = 0x0;
	/** Element type */
	public static final short ELEMENT = 1;
	/** Attribute type */
	public static final short ATTRIBUTE = 2;
	/** PCDATA type */
	public static final short PCDATA = 4;
	/** CDATA type */
	public static final short CDATA = 8;
	/** All types */
	public static final short ALL_TYPES = ELEMENT+ATTRIBUTE+PCDATA+CDATA;



	// element type for arrays / collections
	/* collection is mapped directly to element */
	public static final short DIRECT = 1;
	/* collection is nested in the specified element */
	public static final short SUBELEMENT = 2;
	/* all container types */
	private static final short ALL_CONTAINER_TYPES = DIRECT+SUBELEMENT;
	// NO_COLLECTION - private element type 
	private static final short NO_CONTAINER = 0;
	


	
	// class which registers as a XMLSerializable class
	private Class clazz;

	// mapping registry
	private ArrayList registryArray;

	/** Creates a new instance */
	public ClassMappingRegistry(Class clazz) {
		if (clazz == null) {
			throw new 	IllegalArgumentException("Cannot create ClassMappingRegistry for 'null' class");
		}
		this.clazz = clazz;
		this.registryArray = new ArrayList();
	}
	
	public void registerSimpleField(String fieldName, short xmlType, String xmlName) throws MappingException {
		// get the field
		Field registeredField = findFieldInClass(fieldName);
		// check possible duplicity of XMLName
		checkXMLName(xmlName,fieldName);
		
		// check whether this field is serializable
		Class fieldClass = registeredField.getType();
		if (!XMLSerializer.isClassSerializable(fieldClass)) {
			throw new MappingException("Field "+fieldName+" is of type "+fieldClass.getName()+" which is not directly XML serializable");
		}	

				
		SimpleFieldMapping simpleFieldMapping;
		
		switch (xmlType) {
			case ATTRIBUTE: 
				// register attribute
				simpleFieldMapping = new SimpleFieldMapping(registeredField, ClassMappingRegistry.ATTRIBUTE, xmlName);
				break;
			case ELEMENT:
				// register element
				simpleFieldMapping = new SimpleFieldMapping(registeredField, ClassMappingRegistry.ELEMENT, xmlName);
				break;
			case PCDATA:
				// register PCDATA
				simpleFieldMapping = new SimpleFieldMapping(registeredField, ClassMappingRegistry.PCDATA, xmlName);
				break;
			case CDATA:
				// register CDATA
				simpleFieldMapping = new SimpleFieldMapping(registeredField, ClassMappingRegistry.CDATA, xmlName);
				break;
			default: 
				// unknown xml mapping type
				throw new MappingException("Unknown xmlType for field "+fieldName);
		}
		
		// register the field
		addFieldMapping(simpleFieldMapping);
	}


	// container register
	public void registerContainerField(String fieldName, String xmlName, short elementType) throws MappingException {
		if ((elementType != DIRECT)&(elementType != SUBELEMENT)) {
			throw new MappingException("unknown supplied Element Name");
		}
		// get the field
		Field registeredField = findFieldInClass(fieldName);
		// check whether the class is array
		if ((!registeredField.getType().isArray())&(!Collection.class.isAssignableFrom(registeredField.getType()))) {
			throw new MappingException("Cannot register field "+fieldName+", because it is not an array or collection");
		}				
		// check possible duplicity of XMLName
		checkXMLName(xmlName,fieldName);
		
		// check serializability of array's type !!!!
		///		!!!! what about collection ?????????
		if (registeredField.getType().isArray()) {
			Class componentClass = registeredField.getType().getComponentType();
			if (!XMLSerializer.isClassSerializable(componentClass)) {
				throw new MappingException("Field "+fieldName+" is of component type "+componentClass.getName()+" which is not directly XML serializable");
			}			
		}

		// register container field
		ContainerFieldMapping arrayFieldMapping = new ContainerFieldMapping(registeredField, xmlName, elementType);
		addFieldMapping(arrayFieldMapping);
	}

	
	
	// register container subelements
	public void registerContainerSubtype(String fieldName, Class clazz, String elementName) throws MappingException {
		// get the field
		Field registeredField = findFieldInClass(fieldName);
		// is this field really a registered container field ?
		FieldMapping mapping = getFieldMappingFromRegistry(registeredField);
		if (mapping == null) {
			throw new MappingException("Field "+fieldName+" is not registered, cannot register subtype");
		}
		if (!mapping.isContainer()) {
			throw new MappingException("Field "+fieldName+" is not a container type, cannot register subtype");
		}
		
		// cast to ContainerFieldMapping -> we are going to map class/element pair
		ContainerFieldMapping containerMapping = (ContainerFieldMapping)mapping;
		// register the pair		
		containerMapping.addMappingPair(clazz,elementName);		
	}
	
	

	// getters


	public Field[] getFieldsFromRegistry(short xmlType) {
		ArrayList rows = getFieldMappingsFromRegistry(xmlType);
		Field[] fields = new Field[rows.size()];
		for (int i = 0; i < rows.size(); i++) {
			fields[i] = ((FieldMapping) rows.get(i)).registeredField;
		}
		return fields;
	}


	public Field[] getFieldsFromRegistry() {
		return getFieldsFromRegistry(ALL_TYPES);
	}


	public Field getFieldFromRegistry(String xmlName) {
		FieldMapping mapping = getFieldMappingFromRegistry(xmlName);
		if (mapping != null) {
			return mapping.getField();
		} else {
			return null;
		}
	}
	


	public String getXMLNameFromRegistry(Field field) {
		FieldMapping mapping = getFieldMappingFromRegistry(field);
		if (mapping != null) {
			return mapping.getXMLName();
		} else {
			return null;
		}
	}	
	
	public short getXMLTypeFromRegistry(Field field) {
		FieldMapping mapping = getFieldMappingFromRegistry(field);
		if (mapping != null) {
			return mapping.getXMLType();
		} else {
			return UNKNOWN_TYPE;
		}	
	}


	// private methods
	
	private void addFieldMapping(FieldMapping mapping) {
		registryArray.add(mapping);
	}	
	
	// finds a Field for a supplied field name in the registry's class
	private Field findFieldInClass(String fieldName) throws MappingException {
		if (fieldName == null) {
			throw new IllegalArgumentException("fieldName cannot be null");
		}                
                Class examinedClazz = clazz;
                while (examinedClazz != null) {
                    try {
                        Field aField = examinedClazz.getDeclaredField(fieldName);
                        return aField;
                    } catch (NoSuchFieldException nsfe) {
                        // get superclass
                        examinedClazz = clazz.getSuperclass();
                    }
                }
                throw new MappingException("Field "+fieldName+" does not exist in class:"+clazz.getName());
	}
	
	
	// check whether XMLName was not used
	private void checkXMLName(String xmlName, String fieldName) throws MappingException {	
		if (isXMLNameRegistered(xmlName)) {
			throw new MappingException("Cannot register field "+fieldName+", chosen xml name: "+xmlName+" is already in use");
		}
	}
		
	
	private boolean isXMLNameRegistered(String xmlName) {
		Iterator i = registryArray.iterator();
		while (i.hasNext()) {
			FieldMapping fieldMapping = (FieldMapping) i.next();
			if (fieldMapping.xmlName.equals(xmlName)) {
				return true;
			}
		}
		return false;		
	}
	

	public FieldMapping getFieldMappingFromRegistry(String xmlName) {
		Iterator i = registryArray.iterator();
		while (i.hasNext()) {
			FieldMapping fieldMapping = (FieldMapping) i.next();
			if (fieldMapping.getXMLName().equals(xmlName)) {
				return fieldMapping;
			}
		}
		return null;
	}


	public FieldMapping getFieldMappingFromRegistry(Field field) {
		Iterator i = registryArray.iterator();
		while (i.hasNext()) {
			FieldMapping fieldMapping = (FieldMapping) i.next();
			if (fieldMapping.getField().equals(field)) {
				return fieldMapping;
			}
		}
		return null;
	}	
	
	public FieldMapping[] getFieldMappingsFromRegistry() {
		FieldMapping[] mappings = new FieldMapping[registryArray.size()];
		for (int i=0; i < mappings.length; i++) {
			mappings[i] = (FieldMapping)registryArray.get(i);
		}
		return mappings;
	}
	


	// get all registry rows of given type;
	private ArrayList getFieldMappingsFromRegistry(short xmlType) {
		ArrayList result = new ArrayList();
		Iterator i = registryArray.iterator();
		while (i.hasNext()) {
			FieldMapping fieldMapping = (FieldMapping) i.next();
			if ((fieldMapping.getXMLType() & xmlType) != 0) {
				result.add(fieldMapping);
			}
		}
		return result;
	}


	// private inner classes    

	// class < - > element mapping
	static class ClassElementMapping {
		private Class clazz;
		private String elementName;
		private String fieldName;

		public ClassElementMapping(String elementName, Class clazz) {
			this(elementName,clazz,null);
		}
		
		public ClassElementMapping(String elementName, Class clazz, String fieldName) {
			if ((elementName == null) | (clazz == null)) {
				throw new IllegalArgumentException("arguments cannot be null");
			}
			this.clazz = clazz;
			this.elementName = elementName;
			this.fieldName = fieldName;
		}

		public Class getClazz() {
			return clazz;
		}

		public String getElementName() {
			return elementName;
		}

		public boolean canBeAddedTo(Collection aCollection) {
			Iterator i = aCollection.iterator();
			while (i.hasNext()) {
				Object obj = i.next();
				if (obj instanceof ClassElementMapping) {
					ClassElementMapping aRow = (ClassElementMapping) obj;
					if (this.getClazz().equals(aRow.getClazz())) {
						return false;
					}
					if (this.getElementName().equalsIgnoreCase(aRow.getElementName())) {
						return false;
					}
				}
			}
			return true;
		}
	}

	// field < - > element/attribute mapping
	abstract static class FieldMapping {

		// fields
		private Field registeredField;
		private String xmlName;
		private short xmlType;

		// constructor
		protected FieldMapping(Field registeredField, short xmlType, String xmlName) throws MappingException {
			if (registeredField == null) {
				throw new MappingException("Registered field is null, cannot register mapping");
			}
			if (xmlName == null) {
				throw new MappingException("Registered xml name is null, cannot register mapping");
			}
			if ((xmlType & ALL_TYPES) == 0) {
				throw new MappingException("Unknown registered xml type, cannot register mapping");				
			}
			this.registeredField = registeredField;
			this.xmlName = xmlName;
			this.xmlType = xmlType;
		}
		
		
		public Field getField() {
			return registeredField;
		}
		
		public String getXMLName() {
			return xmlName;
		}
		
		public short getXMLType() {
			return xmlType;
		}		
		
		public boolean isSimple() {
			if (this instanceof SimpleFieldMapping) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean isContainer() {
			if (this instanceof ContainerFieldMapping) {
				return true;
			} else {
				return false;
			}
		}		

	}
	
	
	static class SimpleFieldMapping extends FieldMapping {
		public SimpleFieldMapping(Field registeredField, short xmlType, String xmlName) throws MappingException {
				super(registeredField,xmlType,xmlName);
		}
	}
	

	// field < - > element/attribute mapping
	static class ContainerFieldMapping extends FieldMapping {

		// type of container mapping
		private short containerMappingType;
		// inner container class/element mappings
		
		private HashMap classElementMap = new HashMap();
		private HashMap elementClassMap = new HashMap();		

		// constructor
		public ContainerFieldMapping(Field registeredField, String xmlName, short containerMappingType) throws MappingException {
			super(registeredField,ELEMENT,xmlName);
			if ((containerMappingType & ALL_CONTAINER_TYPES) == 0) {
				throw new MappingException("Unknown container mapping type, cannot register");
			}
			this.containerMappingType = containerMappingType;
		}

		
		public short getContainerMappingType() {
			return containerMappingType;
		}
		
		public void addMappingPair(Class clazz, String elementName) throws MappingException {
			if (containerMappingType != SUBELEMENT) {
				throw new MappingException("Cannot register class/elements mapping pairs for field "+this.getField().getName()+", container type is not SUBELEMENT");
			}
			// check for duplicity
			if (classElementMap.containsKey(clazz)) {
				throw new MappingException("Class "+clazz.getName()+" is already registered for container field "+this.getField().getName());
			}
			if (elementClassMap.containsKey(elementName)) {
				throw new MappingException("Element name "+elementName+" is already registered for container field "+this.getField().getName());
			}
			// register pairs
			classElementMap.put(clazz,elementName);
			elementClassMap.put(elementName,clazz);
		}
		
		public Class getClassForElementName(String elementName) {
			return (Class)elementClassMap.get(elementName);
		}
		
		public String getElementNameForClass(Class clazz) {
			return (String)classElementMap.get(clazz);			
		}
		
		
		

	}	
	

}
