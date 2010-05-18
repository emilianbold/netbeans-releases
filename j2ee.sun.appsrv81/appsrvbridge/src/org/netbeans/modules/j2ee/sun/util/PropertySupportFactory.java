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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.sun.util;

import java.beans.PropertyEditor;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.openide.nodes.PropertySupport;
import org.openide.execution.NbClassPath;

import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.DomainRootNode;
import org.netbeans.modules.j2ee.sun.ide.runtime.nodes.ResourceLeafNode;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtActiveNode;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.LogLevelEditor;

/**
 *
 */
public class PropertySupportFactory {
    
    private static Logger logger;
    private static PropertySupportFactory factory;
    private EnhancedPropertyEditorFactory editorFactory;
    
    private static final String ARRAY_DELIM = " , ";
    
    ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/util/Bundle");
    
    /** Used in order to determine datatype map for editors **/
    static java.util.Map typeToClassesMap = new java.util.HashMap();
    
    static {
        typeToClassesMap.put("short", String.class);
        typeToClassesMap.put("long", String.class); 
        typeToClassesMap.put("int", String.class); 
        typeToClassesMap.put("boolean", String.class); // b/c of enhanced editor
        typeToClassesMap.put("float", String.class); 
        typeToClassesMap.put("double", String.class); 
        typeToClassesMap.put("byte", String.class); 
        typeToClassesMap.put("char", String.class); 
        typeToClassesMap.put(String[].class.getName(), String.class);
        typeToClassesMap.put(Boolean.class.getName(), String.class);
        typeToClassesMap.put(java.util.Map.class.getName(), String.class);
    };   
    
    static {
        logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
    }
    
    /** 
     * Creates a new instance of PropertySupportFactory 
     */
    private PropertySupportFactory() {
        editorFactory = EnhancedPropertyEditorFactory.getInstance();
    }
    
    
    /**
     * Returns an instance of this factory.
     *
     * @return An instance of PropertySupportFactory.
     */
    public static PropertySupportFactory getInstance() {
        if(factory == null) {
            factory = new PropertySupportFactory();
        }
        return factory;
    }
    
    
    /**
     * Returns the appropriate PropertySupport given the MBean Attribute and its
     * MBeanAttributeInfo. First this method determines the type of the 
     * attribute and then whether or not it's writable. After that, the 
     * appropriate PropertySupport is created with a corresponding editor. 
     *
     * @param An instance of AppserverMgmtActiveNode. This is necessary for us
     *        to create the anonymous PropertySupport class that calls the 
     *        setProperty method implementation of a subclass of an instance
     *        of AppserverMgmtActiveNode. This is unfortunate because it forces
     *        a compile-time dependency on 
     *        org.netbeans.modules.j2ee.sun.ide.runtime.nodes. This maybe 
     *        eliminated by placing the logic of this factory in the 
     *        abstract class definition for AppserverMgmtActiveNode but not 
     *        advised since we want to extend editor support without affecting 
     *        the node creation hierarchy. The nodes should be agnostic to the 
     *        property creation and editor support for specific and general
     *        attributes.
     * @param attr An MBean Attribute object containing the name/value.
     * @param info The MBeanAttributeInfo for this Attribute.
     * @return A PropertySupport for the attribute.
     */
    public PropertySupport getPropertySupport(
            final AppserverMgmtActiveNode parent, final Attribute attr, 
            final MBeanAttributeInfo info) {
        PropertySupport support = null;
        String attrName = attr.getName();
        if(Arrays.asList(PropertyConstants.CUSTOM_PROPERTIES).contains(attrName)){
            support = getCustomPropertyEditors(parent, attr, info, attrName);
        }else{
            if(attr.getValue() instanceof String[]){
                String[] strArray = (String[])attr.getValue();
                if(info.isWritable())
                    support = createStringArrayWritableProperty(parent, attr, info, strArray.getClass());
                else
                    support = createStringArrayReadOnlyProperty(attr, info, strArray.getClass());
                support.setValue ("item.separator", " ");//NOI18N
            }else{
                if(info.isWritable()) {
                    if ((parent.getNodeType().equals(NodeTypes.WEB_APPLICATION)) 
                            && (attrName.equals(PropertyConstants.CONTEXTROOT))
                            && (!parent.isServerLocal())) {
                        support = createReadOnlyPropertySupport(attr, info);
                    } else {
                        support = createReadWritePropertySupport(parent, attr, info);
                    }
                } else {
                    support = createReadOnlyPropertySupport(attr, info);
                }
            }
        }
        return support;
    }
    
    private PropertySupport getCustomPropertyEditors(final AppserverMgmtActiveNode parent, final Attribute attr, 
            final MBeanAttributeInfo info, String attrName){
            PropertySupport support = null;
            if(attrName.equals(PropertyConstants.PROPERTY_PAIRS_FIELD) && (!parent.getNodeType().equals(NodeTypes.JVM))
                && (parent instanceof ResourceLeafNode)){
                ResourceLeafNode resourceNode = (ResourceLeafNode)parent;
                support = createExtraProperties(resourceNode, attr, info);
            }else{
                if(attrName.equals(PropertyConstants.DATASOURCE_TYPE_FIELD)){
                    if(parent.getNodeType().equals(NodeTypes.CONNECTION_POOL))
                        support = createWritablePropertySupportWithEditor(parent, attr, info, attrName);
                    else
                        support = createWritablePropertySupportWithoutEditor(parent, attr, info);
                }else   
                    support = createWritablePropertySupportWithEditor(parent, attr, info, attrName);
            }
            return support;
    }
    
    /**
     * Creates a read-only PropertySupport object out of a name/value pair. 
     * This is used to pass to a Sheet.Set object for display in a NetBeans
     * properties sheet.
     *
     * @param name The name of the property to display.
     * @param value The value of the property to display.
     *
     * @return The read-only PropertySupport object for the name/value pair.
     */
    private PropertySupport createReadOnlyPropertySupport(
            final Attribute attr, final MBeanAttributeInfo info) {  
        String name = attr.getName();
        return new PropertySupport.ReadOnly(name, 
                    getClassFromStringType(info.getType()), name, name) {
                public Object getValue() {
                    return calculateReturnObjectType(attr);
                }
        };
    }
    
    
     /**
     * Creates a read-only PropertySupport object out of a name/value pair. 
     * This is used to pass to a Sheet.Set object for display in a NetBeans
     * properties sheet.
     *
     * @param name The name of the property to display.
     * @param value The value of the property to display.
     *
     * @return The read-only PropertySupport object for the name/value pair.
     */
    private PropertySupport createReadWritePropertySupport(
            final AppserverMgmtActiveNode parent, final Attribute attr, 
            final MBeanAttributeInfo info) {  
        PropertySupport support = null;
        if(attr.getValue() instanceof Boolean || info.getType().equalsIgnoreCase("boolean")) {  //NOI18N
            support = 
                createWritablePropertySupportWithEditor(parent, attr, info, "Boolean"); //NOI18N
        } else {
            if(Arrays.asList(PropertyConstants.JVM_STR_TO_ARR).contains(attr.getName())){
                if(parent.isServerLocal()){
                     support = createNetBeansClassPathProperty(parent, attr, info);
                }else{
                    String[] arrayNames = new String[]{};
                    support = createModifiedStringArrayReadOnlyProperty(parent, attr, info, arrayNames.getClass());
                }
            }else{
                support =
                        createWritablePropertySupportWithoutEditor(parent, attr, info);
            }
        }
        return support;
    }
    
    
    /**
     *
     *
     */
    private PropertySupport createWritablePropertySupportWithEditor(
            final AppserverMgmtActiveNode parent, final Attribute attr, 
            final MBeanAttributeInfo info, final String customType) {
        String name = attr.getName();
        return new PropertySupport.ReadWrite(name, 
                    getClassFromStringType(info.getType()), name, name) {
                Attribute attribute = attr;
                public Object getValue() {
                    Object obj = attribute.getValue();
                    if(obj != null) {
                        obj = obj.toString();
                    }
                    return obj;
                }
                
                public void setValue(Object obj) {
                    attribute = revertAttribute(parent, getName(), obj, attribute);
                }
                
                @Override
                public PropertyEditor getPropertyEditor() {
                   return editorFactory.getEnhancedPropertyEditor(
                        attribute.getValue(), customType);
                }
            };
    }
    
    
    /**
     *
     *
     */
    private PropertySupport createWritablePropertySupportWithoutEditor(
            final AppserverMgmtActiveNode parent, final Attribute attr,
            final MBeanAttributeInfo info) {
        String name = attr.getName();
        return new PropertySupport.ReadWrite(name, 
                    getClassFromStringType(info.getType()), name, name) {
                Attribute attribute = attr;
                public Object getValue() {
                    return calculateReturnObjectType(attribute);
                }
                
                public void setValue(Object obj) {
                    attribute = revertAttribute(parent, getName(), obj, attribute);
                }    
            };
    }
    
    
    
    /**
     *
     *
     */
    private Object calculateReturnObjectType(Attribute attr) {
        Object obj = attr.getValue();
        if(obj instanceof ObjectName[]) {
            ObjectName[] objNames = (ObjectName[])obj;
            String[] arrayNames = new String[objNames.length];
            for(int i=0; i < objNames.length; i++){
                arrayNames[i] = objNames[i].toString();
            }
            return arrayNames;
        } if(obj instanceof String[]) {
            String[] values = (String[])obj;
            StringBuffer returnVals = new StringBuffer();
            int pos = 0;
            for(int i=0; i < values.length; i++){
                returnVals.append(values[i]);
                pos++;
                if(pos < values.length) {
                    returnVals.append(ARRAY_DELIM);
                }
            }
            return returnVals;
        } else if(obj != null || !(obj instanceof String)) {
            if(obj == null) {
                return "";
            }
            obj = obj.toString();
            return obj;
        } else {
            return "";
        } 
    }
    
    
    /**
     *
     *
     */
    private static Class getClassFromStringType(final String type) {
        Class clazz = null;
        try {
            clazz = (Class) typeToClassesMap.get(type);
            if (clazz == null) {
                clazz = Class.forName(type);
            }
            if (clazz == null) {
                throw new ClassNotFoundException(type);
            }
            if (!String.class.isAssignableFrom(clazz) 
                    && ! Number.class.isAssignableFrom(clazz)) {
                throw new ClassNotFoundException(type);
            }
        } catch(ClassNotFoundException e) {
            logger.log(Level.FINE, e.getMessage(), e);
        }
        return clazz;
    }

    PropertySupport createExtraProperties(final ResourceLeafNode parent, final Attribute attr, final MBeanAttributeInfo info) {
        return new PropertySupport.ReadWrite(
        info.getName(), 
        NameValuePairsPropertyEditor.class,
        bundle.getString("LBL_ExtParams"), //NOI18N
        bundle.getString("DSC_ExtParams")) { //NOI18N
            Attribute attribute = attr;
            public Object getValue() {
                return attribute.getValue();
            }
              
            public void setValue(Object obj) {
                if(obj instanceof Object[]){
                    //Map containing previous set of properties
                    java.util.Map attributeMap = (java.util.Map)attribute.getValue();
                    java.util.Map oldAttrMap = (java.util.Map)attribute.getValue();
                    
                    //Create a an array of updated properties
                    Object[] currentVal = (Object[])obj;
                    NameValuePair[] pairs = getNameValuePairs(currentVal);
                    java.util.Map propertyList = new java.util.HashMap();
                    java.util.Map newPropsMap = new java.util.HashMap();

                    for(int i=0; i<pairs.length; i++){
                        String name = pairs[i].getParamName();
                        String value = pairs[i].getParamValue();
                        if (attributeMap.containsKey(name)) {
                            String prevValue = (String)attributeMap.get(name);
                            if (! prevValue.equals(value)) {
                                Attribute attr = new Attribute(name, value);
                                newPropsMap.put(name, attr);
                            } else {
                                oldAttrMap.remove(name);
                            }
                        } else {
                            Attribute attr = new Attribute(name, value);
                            newPropsMap.put(name, attr);
                        }
                        propertyList.put(name, value);
                    }
                    Object[] props = newPropsMap.values().toArray();
                    parent.updateExtraProperty(props, oldAttrMap);
                     
                    //Required to do this set to update UI
                    attribute = new Attribute(getName(), propertyList);
                }                    
            }

            @Override
            public PropertyEditor getPropertyEditor(){
                return new NameValuePairsPropertyEditor(attribute.getValue());
            }
            
            
        };
   }//createExtraProperties
    
    PropertySupport createStringArrayReadOnlyProperty(final Attribute a, final MBeanAttributeInfo attr, final Class type) {
        return new PropertySupport.ReadOnly(
        attr.getName(),
        type,
        attr.getName(),
        attr.getName()) {
            Attribute attribute = a;
            public Object getValue() {
                Object val[] = (Object[])attribute.getValue();
                if (attribute.getValue() instanceof ObjectName[]){
                    ObjectName[] objNames = (ObjectName[])val;
                    String[] arrayNames = new String[objNames.length];
                    for(int i=0; i<objNames.length; i++){
                        arrayNames[i] = objNames[i].toString();
                    }
                    return (Object)arrayNames;
                }else{
                    return attribute.getValue();
                }
            }
        };
    }//createStringArrayReadOnlyProperty
    
    PropertySupport createStringArrayWritableProperty(final AppserverMgmtActiveNode parent, final Attribute attr, final MBeanAttributeInfo info, final Class type) {
        return new PropertySupport.ReadWrite(
        info.getName(),
        type,
        info.getName(),
        info.getName()) {
            Attribute attribute = attr;
            public Object getValue() {
                Object val[] = (Object[])attribute.getValue();
                if (attribute.getValue() instanceof ObjectName[]){
                    ObjectName[] objNames = (ObjectName[])val;
                    String[] arrayNames = new String[objNames.length];
                    for(int i=0; i<objNames.length; i++){
                        arrayNames[i] = objNames[i].toString();
                    }
                    return (Object)arrayNames;
                }else{
                    String[] values = (String[])attribute.getValue();
                    return values;
                }
            }
            public void setValue(Object obj) {
                attribute = revertAttribute(parent, getName(), obj, attribute);
            }
        };
    }//createStringArrayWritableProperty
    
    PropertySupport createNetBeansClassPathProperty(final AppserverMgmtActiveNode parent, final Attribute attr, final MBeanAttributeInfo info) {
        return new PropertySupport.ReadWrite(
                info.getName(),
                NbClassPath.class,
                info.getName(),
                info.getName()) {
            Attribute attribute = attr;
            public Object getValue() {
                if(attribute.getValue() != null){
                    String val = replacePathSeperatorToken(attribute.getValue().toString());
                    return new NbClassPath(val);
                }else
                    return null;
            }
            
            public void setValue(Object val){
                String value = ((NbClassPath)val).getClassPath();
                value = stripQuotes(value);
                Object obj = replacePathSeperator(value);
                attribute = revertAttribute(parent, getName(), obj, attribute);
            }
        };
    }//createNetBeansClassPathProperty
    
    public PropertySupport createLogLevelProperty(final DomainRootNode parent, final Attribute attr, final MBeanAttributeInfo info) {
        return new PropertySupport.ReadWrite(
                info.getName(),
                LogLevelEditor.class,
                info.getName(),
                info.getName()) {
            Attribute attribute = attr;
            public Object getValue() {
                return attribute.getValue();
            }
            
            public void setValue(Object val){
                Attribute updatedAttribute = null;
                try {
                    updatedAttribute = parent.setSheetProperty(getName(), val);
                } catch (RuntimeException rex) {
                    //catching runtime exception from isServerInDebug
                }
                if(updatedAttribute != null){
                    attribute = updatedAttribute;
                }
            }
            
            @Override
            public PropertyEditor getPropertyEditor(){
                return new LogLevelEditor();
            }
        };
    }//createLogLevelProperty
    
    PropertySupport createModifiedStringArrayReadOnlyProperty(final AppserverMgmtActiveNode parent, final Attribute attr, final MBeanAttributeInfo info, final Class classType) {
        return new PropertySupport.ReadOnly(
        info.getName(),
        classType,
        info.getName(),
        info.getName()) {
            char sepChar = ';';
            Attribute attribute = attr;
            public Object getValue() {
                Object val = attribute.getValue();
                String[] value = null;
                if(val != null){
                    String strVal = replacePathSeperatorToken(val.toString());
                    if(strVal != null){
                        sepChar = getSeperationChar(strVal);
                    }
                    value = createClasspathArray(strVal);
                }
                return value;
            }
        };
    }//createModifiedStringArrayReadOnlyProperty
    
    public String replacePathSeperatorToken(String tokenString){
        String token = "path.separator"; //NOI18N
        String resolvedToken = File.pathSeparator;
        tokenString = tokenString.replaceAll("\\$\\{"+token+"\\}", resolvedToken); //NOI18N
        return tokenString;
    }
    
    public String replacePathSeperator(String tokenString){
        String token = "path.separator"; //NOI18N
        String resolvedToken = File.pathSeparator;
        tokenString = tokenString.replaceAll(resolvedToken, "\\$\\{"+token+"\\}"); //NOI18N
        return tokenString;
    }
    
    private Attribute revertAttribute(AppserverMgmtActiveNode parent, String attrName, Object attrValue, Attribute attribute){
        Attribute updatedAttribute = null;
        try {
            updatedAttribute = parent.setSheetProperty(attrName, attrValue);
        } catch (RuntimeException rex) {
            //catching runtime exception from isServerInDebug
        }
        
        if(updatedAttribute != null){
            return updatedAttribute;
        }
        
        return attribute;
    }
   
    private NameValuePair[] getNameValuePairs(Object[] attrVal){
        NameValuePair[] pairs = new NameValuePair[attrVal.length];
        for (int j = 0; j < attrVal.length; j++) {
            NameValuePair pair = (NameValuePair)attrVal[j];
            pairs[j] = pair;
        }
        return pairs;
    }
    
    private static String[] createClasspathArray(Object cpath){
        Vector path = new Vector();
        if(cpath != null){
            String classPath = cpath.toString();
            char sepChar = getSeperationChar(classPath);
            while(classPath.indexOf(sepChar) != -1){
                int index = classPath.indexOf(sepChar);
                String val = classPath.substring(0, index);
                path.add(val);
                classPath = classPath.substring(index+1, classPath.length());
            }
            path.add(classPath);
        }
        if(path != null){
            Object[] finalPath = (Object[])path.toArray();
            String[] value = new String[finalPath.length];
            for(int i=0; i<finalPath.length; i++){
                value[i] = finalPath[i].toString();
            }
            
            return value;
        }else
            return null;
    }
    
    private static char getSeperationChar(String classPath){
        if(classPath.indexOf(";") != -1) //NOI18N
            return ';';
        else
            return ':';
    }
    
    private String stripQuotes(String classPath){
        if(classPath.startsWith("\"")){ //NOI18N
            int index = classPath.indexOf("\""); //NOI18N
            classPath = classPath.substring(index + 1, classPath.lastIndexOf("\"")); //NOI18N
        }    
        return classPath;
    }
}
