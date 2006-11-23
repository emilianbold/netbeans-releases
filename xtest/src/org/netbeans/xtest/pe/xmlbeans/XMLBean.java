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
 * XMLBean.java
 *
 * Created on October 31, 2001, 2:47 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

import org.w3c.dom.*;
import java.io.*;
import javax.xml.parsers.*;
import org.netbeans.xtest.util.XMLFactoryUtil;
import org.netbeans.xtest.util.XMLWriter;
import org.xml.sax.SAXException;

import java.util.*;
import java.lang.reflect.*;


/**
 * name of the class = name of the XML element
 * currently - all beans must exist in the same package as XMLBean :-(
 * attributes = public variables beginning with xml keyword
 *
 * @author  mb115822
 */
public abstract class XMLBean {

    
    public final static String[] XMLBEAN_PACKAGES = {"org.netbeans.xtest.pe.xmlbeans",
                                                     "org.netbeans.xtest.pes.xmlbeans"};
    public final static String XMLBEAN_ATT_PREFIX = "xmlat_";
    public final static String XMLBEAN_ELEM_PREFIX = "xmlel_";
    public final static String XMLBEAN_FILE_SUFFIX = ".xml";
    
    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("XMLBean."+message);
    }
    
    public static final int ALL_ELEMENTS = -1;
    
    // pcdata of the XML element
    public String xml_pcdata = null;
    // cdata of the XML element
    public String xml_cdata = null;
    
    /** Holds value of property id. */
    private long id;
    
    // utility methods
    public static Object[] addToArray(Object[] array, Object[] objs) throws IllegalArgumentException {
        debugInfo("addToArray(): array = "+array+" objects to add = "+objs);
        Object resultingArray;
        int length = 0;
        if (array == null) {
            if ((objs == null) | ((objs.length > 0) & (objs[0] == null))) {
                debugInfo("addToArray(): both array and obj are null - there is nothing to do - return null");
                return null;
            } else {            
                debugInfo("addToArray(): array is null, but we can construct a new array with just one object");
                resultingArray = Array.newInstance(objs[0].getClass(),objs.length);                
            } 
        } else {
            if (objs == null) {
                debugInfo("addToArray(): objs is null - there is nothing to do - return the existing array");
                return array;
            } else {
                debugInfo("addToArray(): adding to existing array");
                Class arrayType = array.getClass().getComponentType();
                length = array.length;
                debugInfo("addToArray(): discovered array componennt type = "+arrayType+" and length = "+length);
                resultingArray = Array.newInstance(arrayType,length+objs.length);
                debugInfo("addToArray(): copying old array to new one ");
                for (int i=0; i<length; i++) {
                    Array.set(resultingArray,i,array[i]);
                }
            }
        }        
        
        debugInfo("addToArray(): adding the new objects at the end (position = "+length+")");
        for (int i=0; i < objs.length; i++) {
            Array.set(resultingArray,length+i,objs[i]);
        }
        return (Object[])resultingArray;
    }
    
    // wrapper for a single object
     public static Object[] addToArray(Object[] array, Object obj) throws IllegalArgumentException {
         if (obj != null) {
            Object objArray = Array.newInstance(obj.getClass(),1);
            Array.set(objArray,0,obj);
            return addToArray(array,objArray);
         } else {
             return array;
         }
     }
     
     
     public static Object[] shrinkArray(Object[] array) {
         if (array == null) {
             return null;
         } else {
             int length = 0;
             for (int i=0; i<array.length; i++) {
                 if (array[i]!=null) {
                     length++;
                 }
             }
             // construct the new array
             Class arrayType = array.getClass().getComponentType();                
             Object resultingArray = Array.newInstance(arrayType,length);
             // now copy the old array to the shrinked one
             for (int i=0, j=0; i < array.length; i++) {
                 if (array[i]!=null) {
                     Array.set(resultingArray,j,array[i]);
                     j++;
                 }
             }
             // finally - return the array
             return (Object[])resultingArray;
         }
         
     }
    
    
    public boolean isObjectValid(Object obj) {
        if (obj != null) {
            if (obj.getClass().isInstance(this)) {
                return true;                
            }
        }
        return false;
    }
    
    
    
    public static boolean equalObjectsByFields(Object obj1, Object obj2, String[] fieldNames) 
                throws NoSuchFieldException {
        debugInfo("compareObjectsByFields(): start");
        if (fieldNames != null) {
            if (fieldNames.length>0) {
                // now compare objects                
                try {
                    if (obj1.getClass().isInstance(obj2)) {
                        debugInfo("compareObjectsByFields(): objects are of the same type");
                        for (int i=0;i<fieldNames.length;i++) {                            
                            Field field = obj1.getClass().getField(fieldNames[i]);
                            debugInfo("compareObjectsByFields(): comparing field"+field.getName());
                            Object value1 = field.get(obj1);
                            Object value2 = field.get(obj2);
                            if ((value1==null)|(value2==null)) {
                                debugInfo("compareObjectsByFields(): comparing for null");
                                if ((value1==null)&(value2==null)) {
                                    debugInfo("compareObjectsByFields(): both values are null");
                                } else {
                                     debugInfo("compareObjectsByFields(): comparing for null, but 2nd value is not null");
                                     return false;
                                } 
                            } else {
                                if (!value1.equals(value2)) {
                                    debugInfo("compareObjectsByFields(): values differ, fieldName = "+fieldNames[i]);
                                    debugInfo("compareObjectsByFields(): value1="+value1+"; value2="+value2);
                                    return false;
                                }
                            }
                        }
                        debugInfo("compareObjectsByFields(): all fields are equal - true");
                        return true;
                    } else {
                        debugInfo("compareObjectsByFields(): objects are not of the same type - false");
                        return false;
                    }
                } catch (NullPointerException npe) {                    
                    // there was some problem -> objects are not equal
                    debugInfo("compareObjectsByFields(): there was NPE, hence objects are not equal");
                    return false;
                } catch (IllegalAccessException iae) {
                    debugInfo("compareObjectsByFields(): there was IllegalAccessException, throwing NoSuchFieldException");
                    throw new NoSuchFieldException(" thrown IllegalAccessException: "+iae);
                }
            }
        }
        debugInfo("compareObjectsByFields(): throwing NoSuchAFieldException - fieldNames string is empty !!!");
        throw new NoSuchFieldException(" No fields were specified");        
    }
    
    
    public boolean equalByAttributes(Object obj) {
        debugInfo("equalByAttribues(): comparing XMLBean to another XMLBean by all its XML attributes");
        try {
            ArrayList attributeFieldList = new ArrayList();
            Field[] fields = this.getClass().getFields();
            for (int i = 0; i<fields.length; i++) {
                Field field = fields[i];
                if (field.getName().startsWith(XMLBean.XMLBEAN_ATT_PREFIX)) {
                    attributeFieldList.add(field.getName());
                }
            }
            String[] fieldNames = (String[])(attributeFieldList.toArray(new String[0]));
            debugInfo("equalByAttribues(): got field names -> comparing"); 
            return equalObjectsByFields(this,obj,fieldNames);
            // get all fields
        } catch (NoSuchFieldException nsfe) {
            // this should not happen            
            debugInfo("equalByAttribues(): NoSuchFieldException - weird: "+nsfe);
            return false;
        }
    }
    
    public static XMLBean findXMLBean(XMLBean[] existingBeans, XMLBean newBean) {
        debugInfo("findXMLBean(): existingBeans = "+existingBeans+", newBean = "+newBean);
        if (newBean == null) {
            debugInfo("findXMLBean(): newBean is null - cannot find null :-)");
            return null;
        }
        if (existingBeans == null) {
            debugInfo("findXMLBean(): existingBeans is null - what shoud I compare :-)");
            return null;
        }
        debugInfo("findXMLBean(): lets look for a bean ");
        for (int i=0; i<existingBeans.length ; i++) {
             if (newBean.equals(existingBeans[i])) {
                debugInfo("addOrFindEqualBean():, found equal XMLBean");
                return existingBeans[i];
            }
        }
        return null;
    }
       
    
    public static String cutPackage(String className) {
        if (DEBUG) System.out.println("XMLBean.cutPackage: className = "+className);
        int lastDot = className.lastIndexOf('.');
        if (lastDot != -1 ) {
            return className.substring(lastDot+1);
        } else {
            return className;
        }
    }
    
    public static String cutPrefix(String aString, String prefix) {
        if (prefix != null) {
            if (!prefix.equals("")) {                
                if (aString.startsWith(prefix)) {
                    return aString.substring(prefix.length());
                }
            }
        }
        return aString;
    }
    
    
    
    // 
    
    public static void setField(Field field,Object obj, String value) 
                            throws IllegalArgumentException, IllegalAccessException {
        String fieldTypeName = field.getType().getName();
        if (fieldTypeName.equals("java.lang.String")) {
            field.set(obj,value);
            return;
        }
        if (fieldTypeName.equals("int")) {
            field.setInt(obj,Integer.parseInt(value));
            return;
        }
        if (fieldTypeName.equals("short")) {
            field.setShort(obj,Short.parseShort(value));
            return;
        }
        
        if (fieldTypeName.equals("long")) {
            field.setLong(obj,Long.parseLong(value));
            return;
        }
        
        if (fieldTypeName.equals("float")) {
            field.setFloat(obj,Float.parseFloat(value));
            return;
        }
        
        if (fieldTypeName.equals("double")) {
            field.setDouble(obj,Double.parseDouble(value));
            return;
        }
        

        if (fieldTypeName.equals("boolean")) {            
            field.setBoolean(obj,Boolean.valueOf(value).booleanValue());
            return;
        }   
        
        if (fieldTypeName.equals("java.sql.Date")) {
            field.set(obj,java.sql.Date.valueOf(value));
            return;
        }
        
        if (fieldTypeName.equals("java.sql.Timestamp")) {
            field.set(obj,java.sql.Timestamp.valueOf(value));
            return;
        }
        
        throw new IllegalArgumentException("field type '"+fieldTypeName+"' not supported");
        
    }
    
    
    protected void fillAttributes(NamedNodeMap atts)
                      throws NoSuchFieldException {
        for (int i=0; i<atts.getLength(); i++) {
            Node attribute = atts.item(i);            
            String attributeName = attribute.getNodeName();
            // do we have such a attribute in XMLBean ?
            Field attField = this.getClass().getField(XMLBean.XMLBEAN_ATT_PREFIX+attributeName);
            if (attField != null) {
                String value = attribute.getNodeValue();
                if (DEBUG) System.out.println("XMLBean.fillAttributes(): setting field:"+attField.getName()+" with value = "+value);
                try {
                    XMLBean.setField(attField,this,value);
                } catch (Exception e) {
                    // will this work ???
                    NoSuchFieldException nsfe = new NoSuchFieldException("Cannot set field in XMLBean");
                    nsfe.fillInStackTrace();
                    throw nsfe;
                }
            }                        
        }        
    }
    
    
    protected void fillElements(NodeList elements, int depth) throws NoSuchFieldException, ClassNotFoundException {
        // if depth is zero, we don't want dig deeper
        if (depth==0) return;
        // otherwise continue with getting the bean   
        XMLBeanSet xmlBeans = new XMLBeanSet();
        for (int i=0; i<elements.getLength(); i++) {
            Node elementNode = elements.item(i);
            // is it really element ?
            switch (elementNode.getNodeType()) {
                
                case Node.ELEMENT_NODE :
                    Element element = (Element)elementNode;
                    String elementName = elementNode.getNodeName();
                    if (DEBUG) System.out.println("XMLBean.fillElements(): Got child element:"+elementName);
                    Field elemField = this.getClass().getField(XMLBean.XMLBEAN_ELEM_PREFIX+elementName);
                    if (DEBUG) System.out.println("XMLBean.fillElements(): Got field:"+elemField.getName());
                    if (elemField != null) {
                        // ok, lets get the XMLBean instance
                        XMLBean childBean = getXMLBean(element,depth-1);
                        if (DEBUG) System.out.println("XMLBean.fillElements(): got ChildBean!!!"+childBean);
                        // store it together with all other instances of this type
                        xmlBeans.addXMLBean(elemField,childBean);
                    }
                    break;
                case Node.TEXT_NODE :
                    if (DEBUG) System.out.println("XMLBean.fillElements(): Got TEXT_NODE:"+elementNode.getNodeValue());
                    this.xml_pcdata = elementNode.getNodeValue();
                    break;
                    
                case Node.CDATA_SECTION_NODE:
                    if (DEBUG) System.out.println("XMLBean.fillElements(): Got CDATA_NODE:"+elementNode.getNodeValue());
                    this.xml_cdata = elementNode.getNodeValue();
                    break;
                default :
                    if (DEBUG) System.out.println("XMLBean.fillElements(): Got unsupported node:"+elementNode.getNodeValue());
            }
        }
        
        if (DEBUG) System.out.println("XMLBean.fillElements(): all elements processed, now fill variables");
        
        // now we can fill the variables with beans ...
        Field[] fieldsToFill = xmlBeans.getFields();
        if (DEBUG) System.out.println("XMLBean.fillElements(): processing "+fieldsToFill+" fields, length = "+fieldsToFill.length);
        for (int i = 0 ; i < fieldsToFill.length; i++) {
            
            Field field = fieldsToFill[i];
            if (DEBUG) System.out.println("XMLBean.fillElements(): Field i"+i+" field="+field);
            if (DEBUG) System.out.println("XMLBean.fillElements(): filling this field:"+field.getName());
            // get all XML beans instances to be stored in this field
            XMLBean[] xmlBeanInstances = xmlBeans.getXMLBeans(field);
            // store all instances in the field variable
            Object xmlBeanArray = Array.newInstance(field.getType().getComponentType(),xmlBeanInstances.length);
            System.arraycopy(xmlBeanInstances,0,xmlBeanArray,0,xmlBeanInstances.length);
            try {
                field.set(this,xmlBeanArray);
            } catch (IllegalAccessException iae) {
                throw new NoSuchFieldException("Cannot access requested field");
            } 
        }        
    }
    
    
    public static XMLBean getXMLBean(Document doc) throws ClassNotFoundException {
        return getXMLBean(doc,ALL_ELEMENTS);
    }
    
    public static XMLBean getXMLBean(Document doc,int depth) throws ClassNotFoundException {
        Element element = doc.getDocumentElement();
        return getXMLBean(element, depth);
    }
    
    public static XMLBean getXMLBean(Element element) throws ClassNotFoundException {
        return getXMLBean(element,ALL_ELEMENTS);
    }
    
    public static XMLBean getXMLBean(Element element, int depth) throws ClassNotFoundException {
        // if depth is zero, we don't want dig deeper
        if (depth==0) return null;
        // otherwise continue with getting the bean   
        String elName = element.getTagName();
        Class xmlBeanClass = null;
        XMLBean xmlBean = null;
        // try to load class
        for (int i=0; i<XMLBEAN_PACKAGES.length;i++) {
            try {
                xmlBeanClass = Class.forName(XMLBEAN_PACKAGES[i]+"."+elName);
                break;
            } catch (ClassNotFoundException cnfe) {
                // nothing happened, try next package
            }
        }
        if (xmlBeanClass == null) {
            // class not found
            throw new ClassNotFoundException("Cannot find class "+elName+" in all XMLBean specified packages");
        }
        
        if (DEBUG) System.out.println("Trying to instintiate "+elName);
        Object aBean = null;
        try {
            aBean = xmlBeanClass.newInstance();
        } catch (IllegalAccessException iae) {
            throw new ClassNotFoundException("Cannot instintiate class - illegal access: "+xmlBeanClass);
        } catch (InstantiationException ie) {
            throw new ClassNotFoundException("Cannot instintiate class: "+xmlBeanClass);
        }
        // is the bean instance of xmlBean ?
        if (!XMLBean.class.isInstance(aBean)) {
            throw new ClassNotFoundException("class "+xmlBeanClass+" is not instance of XMLBean");
        }
        // try to instantiate the class - must have a constructor with no arguments
        xmlBean = (XMLBean)aBean;
        
        if (DEBUG) System.out.println("XMLBean.getXMLBean(): instintiated new XMLBean");
        // now we have the bean - so lets get attributes and fill them :-).
        NamedNodeMap atts = element.getAttributes();
        if (atts!=null) {
            if (atts.getLength() != 0) {
                try {
                    xmlBean.fillAttributes(atts);
                    if (DEBUG) System.out.println("XMLBean.getXMLBean(): got attributes");
                } catch (NoSuchFieldException nsfe) {
                    throw new ClassNotFoundException("Cannot fill defined attributes", nsfe);
                }
            }
        }
        
        
        // do we have any children - lets get them as beans ... (or set pcdata if applicable)
        NodeList childElements = element.getChildNodes();
        if (childElements != null) {
            if (childElements.getLength()!=0) {
                if (DEBUG) System.out.println("XMLBean.getXMLBean(): have to process childElements, size="+childElements.getLength());
                try {
                    xmlBean.fillElements(childElements,depth);
                } catch (NoSuchFieldException nsfe) {
                    throw new ClassNotFoundException("Cannot fill children elements - no such a field exception");
                }
            }
        }
        return xmlBean;
    }
    
    public Document toDocument() throws DOMException{
        return toDocument(ALL_ELEMENTS);
    }
    
    public Document toDocument(int depth) throws DOMException {
        if (DEBUG) System.out.println("XMLBean:toDocument() begin");
        Document doc =  getDocumentBuilder().newDocument();
        Element element = this.toElement(doc,depth);
        doc.appendChild(element);
        return doc;
    }
    
    public Element toElement(Document doc) throws DOMException {
        return toElement(doc,ALL_ELEMENTS);
    }
    
    public Element toElement(Document doc, int depth) throws DOMException {
        if (DEBUG) System.out.println("XMLBean:toElement() begin, this="+this+" depth="+depth);
        // if depth is zero, we don't want serialize anymore, so return null,
        if (depth==0) return null;
        // otherwise continue with serialization        
        // get the name of the class - it will be used as the name of the
        String fullClassName = this.getClass().getName();
        if (DEBUG) System.out.println("XMLBean:toElement() fullClassName="+fullClassName);
        Package p = this.getClass().getPackage();
        if (DEBUG) System.out.println("XMLBean:toElement() package="+p);
        //String packageName = p.getName();
        //if (DEBUG) System.out.println("XMLBean:toElement() packageName="+packageName);
        // element tag
        String className = cutPackage(fullClassName);
        if (DEBUG) System.out.println("XMLBean:toElement() - className="+className);
        Element element = doc.createElement(className);
        
        // do we have any pcdata ?
        if (this.xml_pcdata!=null) {
            if (DEBUG) System.out.println("XMLBean:toElement() adding PCDATA:"+this.xml_pcdata);
            Text textNode = doc.createTextNode(this.xml_pcdata);
            //textNode.setNodeValue();
            element.appendChild(textNode);
        }
        
        if (this.xml_cdata!=null) {
            if (DEBUG) System.out.println("XMLBean:toElement() adding CDATA:"+this.xml_cdata);
            
            CDATASection cdataNode = doc.createCDATASection(this.xml_cdata);
            element.appendChild(cdataNode);
        }
        
        // now search for variables and add attributes/elements
        Field[] fields = this.getClass().getFields();
        for (int i=0; i< fields.length; i++) {
            Field field = fields[i];
            if (DEBUG) System.out.println("XMLBean:toElement(): processing field="+field.getName());
            // search for attribute
            String fieldName = field.getName();
            if (fieldName.startsWith(XMLBEAN_ATT_PREFIX)) {
                String attributeName = cutPrefix(fieldName,XMLBEAN_ATT_PREFIX);
                Object fieldValue = null;
                try {
                    fieldValue = field.get(this);
                } catch (IllegalAccessException iae) {
                    throw new DOMException(Short.MIN_VALUE,"Cannot access XMLBean's field:"+field);
                }
                if (fieldValue != null) {
                    String value = fieldValue.toString();
                    if (DEBUG) System.out.println("XMLBean:toElement(): got attribute value = "+fieldValue);
                    element.setAttribute(attributeName,value);
                } else {
                    // nothing
                    if (DEBUG) System.out.println("XMLBean:toElement(): field value  = "+fieldValue);
                }
            }
            // search for element
            if (fieldName.startsWith(XMLBEAN_ELEM_PREFIX)) {
                Object value = null;
                try {
                    value = field.get(this);
                } catch (IllegalAccessException iae) {
                    throw new DOMException(Short.MIN_VALUE,"Cannot access XMLBean's field:"+field);
                }
                if (value != null) {
                    if (field.getType().isArray()) {
                        int length = Array.getLength(value);
                        for (int j = 0; j < length; j++) {
                            Object xmlBeanObject = Array.get(value,j);
                            if (xmlBeanObject!=null) {
                                if (xmlBeanObject instanceof XMLBean) {
                                    Element childElement = ((XMLBean)xmlBeanObject).toElement(doc, depth - 1);
                                    if (childElement != null) {
                                        element.appendChild(childElement);
                                    }
                                } else {
                                    if (DEBUG) System.out.println("XMLBean:toElement() object in the array is not instanceof XMLBean");
                                }
                            }
                        }
                    } else {
                        if (DEBUG) System.out.println("XMLBean:toElement() - cannot handle elements from non arrays");
                    }
                }
            }
        }
        // n
        
        return element;
    }
    
    /** Getter for property id.
     * @return Value of property id.
     */
    public long getId() {
        return this.id;
    }    
    
    /** Setter for property id.
     * @param id New value of property id.
     */
    public void setId(long id) {
        this.id = id;
    }    
    
    
    // other utility methods
    
    // XMLBean loader ...    
    public static XMLBean loadXMLBean(File xmlBeanFile) throws IOException, ClassNotFoundException {
        return loadXMLBean(xmlBeanFile,ALL_ELEMENTS);
    }
    
    // loader which allows to specify depth of elements to be loaded
    public static XMLBean loadXMLBean(File xmlBeanFile, int depth) throws IOException, ClassNotFoundException {
        if (xmlBeanFile == null) throw new IllegalArgumentException("parameter cannot be null");
        if (!xmlBeanFile.isFile()) {
            throw new IOException("File "+xmlBeanFile+" is not a valid file, cannot load XMLBean");
        }
        // parse the file
        try {
            Document doc =  getDocumentBuilder().parse(xmlBeanFile);
            XMLBean xmlBean = XMLBean.getXMLBean(doc, depth);
            return xmlBean;
        } catch (SAXException se) {
            throw new IOException("Cannot parse XML, because of :"+se.getMessage());
        }               
    }
    
    // save XMLBean
    public void saveXMLBean(File xmlBeanFile) throws IOException {
        saveXMLBean(xmlBeanFile,ALL_ELEMENTS);
    }
    
    // save XMLBean which allows to specify depth of XML elements to be saved
    public void saveXMLBean(File xmlBeanFile, int depth) throws IOException {
        Document doc = this.toDocument(depth);
        serializeToFile(doc,xmlBeanFile);
    }
    
    // DocumentBuilder
    protected static DocumentBuilder getDocumentBuilder() {
        try {
            return XMLFactoryUtil.newDocumentBuilder();
        }
        catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
         
    // serialize to File
    protected static void serializeToStream(Document doc, OutputStream out) throws IOException {
        XMLWriter xmlWriter = new XMLWriter(out, "UTF-8");
        xmlWriter.write(doc);
        //out.close();
    }
    
    // serialize to File
    protected static void serializeToFile(Document doc, File outFile) throws IOException {
        OutputStream out = new FileOutputStream(outFile);
        serializeToStream(doc,out);
        out.close();
    }
    
    
    // helper class for storing already instantiated XMLBeans grouped by
    // element types
    public static class XMLBeanSet {
        
        private HashMap fields;
        
        public XMLBeanSet() {
            fields = new HashMap();
        }
        
        public void addXMLBean(Field field, XMLBean xmlBean) {
            ArrayList xmlBeansInstances = (ArrayList)fields.get(field);
            if (xmlBeansInstances != null) {
                xmlBeansInstances.add(xmlBean);
            } else {
                xmlBeansInstances = new ArrayList();
                xmlBeansInstances.add(xmlBean);
                fields.put(field,xmlBeansInstances);
            }
        }
        
        public XMLBean[] getXMLBeans(Field field) {
            ArrayList xmlBeansInstances = (ArrayList)fields.get(field);
            if (xmlBeansInstances != null) {
                return (XMLBean[])(xmlBeansInstances.toArray(new XMLBean[0]));
            } else {
                return null;
            }
        }
        
        public Field[] getFields() {
            return (Field[])(fields.keySet().toArray(new Field[0]));
        }
        
    }
   

}
