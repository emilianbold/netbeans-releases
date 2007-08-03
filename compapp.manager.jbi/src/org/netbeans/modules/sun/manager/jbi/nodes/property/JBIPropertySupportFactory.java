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

package org.netbeans.modules.sun.manager.jbi.nodes.property;

import java.beans.PropertyEditor;
import java.util.logging.Level;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import org.netbeans.modules.sun.manager.jbi.editors.EnvironmentVariablesEditor;
import org.netbeans.modules.sun.manager.jbi.editors.JBILogLevelEditor;
import org.netbeans.modules.sun.manager.jbi.editors.PasswordEditor;
import org.netbeans.modules.sun.manager.jbi.editors.SimpleTabularDataEditor;
import org.netbeans.modules.sun.manager.jbi.nodes.AppserverJBIMgmtNode;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.openide.nodes.PropertySupport;

/**
 * 
 * @author jqian
 */
public class JBIPropertySupportFactory {
    
//    private static Logger logger;
//    private static JBIPropertySupportFactory instance;
//    private EnhancedPropertyEditorFactory editorFactory;
    
//    private static final String ARRAY_DELIM = " , "; // NOI18N
//    
//    /** Used in order to determine datatype map for editors **/
//    static Map<String, Class> typeToClassesMap = new HashMap<String, Class>();
//    
//    static {
//        typeToClassesMap.put("short", String.class); // NOI18N
//        typeToClassesMap.put("long", String.class); // NOI18N
//        typeToClassesMap.put("int", String.class); // NOI18N
//        typeToClassesMap.put("boolean", String.class); // b/c of enhanced editor // NOI18N
//        typeToClassesMap.put("float", String.class); // NOI18N
//        typeToClassesMap.put("double", String.class); // NOI18N
//        typeToClassesMap.put("byte", String.class); // NOI18N
//        typeToClassesMap.put("char", String.class); // NOI18N
//        typeToClassesMap.put(String[].class.getName(), String.class);
//        typeToClassesMap.put(Boolean.class.getName(), String.class);
//        typeToClassesMap.put(java.util.Map.class.getName(), String.class);
//    };
//
//    private JBIPropertySupportFactory() {
////        editorFactory = EnhancedPropertyEditorFactory.getInstance();
//    }    
//    
//    /**
//     * Returns the singleton instance of this factory.
//     *
//     * @return An instance of PropertySupportFactory.
//     */
//    public static JBIPropertySupportFactory getInstance() {
//        if (instance == null) {
//            instance = new JBIPropertySupportFactory();
//        }
//        return instance;
//    }    
    
    /**
     * Returns the appropriate PropertySupport given the MBean Attribute and its
     * MBeanAttributeInfo.
     *
     * @param parent    an instance of AppserverJBIMgmtNode. This is necessary 
     *        for us to create the anonymous PropertySupport class that calls 
     *        the setProperty method implementation of a subclass of an instance
     *        of AppserverJBIMgmtNode. 
     * @param attr  an MBean Attribute object containing the name/value
     * @param info  the MBeanAttributeInfo for this Attribute
     * 
     * @return a PropertySupport for the attribute
     */
    public static PropertySupport getPropertySupport(
            final AppserverJBIMgmtNode parent, 
            final Attribute attr,
            final MBeanAttributeInfo info) {
        
        PropertySupport support = null;
        
        if (info.getType().equals("java.util.logging.Level")) { // NOI18N    
            support = createLogLevelProperty((JBIComponentNode)parent, attr, info);
        } else if (info.getType().equals("javax.management.openmbean.TabularData")) { // NOI18N
            TabularDataSupport tabularDataSupport = 
                    (TabularDataSupport) attr.getValue();
            TabularType tabularType = tabularDataSupport.getTabularType();
            int columnCount = tabularType.getRowType().keySet().size();
            if (columnCount == 3) { // new typed environment variables
                support = createEnvironmentVariablesProperty(
                        (JBIComponentNode)parent, attr, info);
            } else {  // untyped environment variables (for backward compatibility)
                support = createTabularDataProperty(
                        (JBIComponentNode)parent, attr, info);
            }
        } else {
            if (info.isWritable()) {
                support = createReadWritePropertySupport(parent, attr, info);
            } else {
                support = createReadOnlyPropertySupport(attr, info);
            }
        }
        return support;
    }
  
    private static PropertySupport createReadOnlyPropertySupport(
            final Attribute attr, final MBeanAttributeInfo info) {
        
        return new PropertySupport.ReadOnly<String>(
                attr.getName(),
                String.class, //getClassFromStringType(info.getType()), 
                info.getName(), 
                info.getDescription()) {
            
            public String getValue() {
                return (String) calculateReturnObjectType(attr);
            }
        };
    }    
  
    private static PropertySupport createReadWritePropertySupport(
            final AppserverJBIMgmtNode parent, final Attribute attr,
            final MBeanAttributeInfo info) {
        PropertySupport support = null;
        if (attr.getValue() instanceof Boolean) {
            support = getBooleanPropertySupport(parent, attr, info); 
        } else if (attr.getValue() instanceof Integer) {
            support = getReadWriteIntegerPropertySupport(parent, attr, info);
        } else {                    
            support = getReadWriteStringPropertySupport(parent, attr, info);
        }
        return support;
    }    
    
    public static PropertySupport getBooleanPropertySupport(
            final AppserverJBIMgmtNode parent,
            final Attribute attr,
            final MBeanAttributeInfo info) {
         
        return new PropertySupport.ReadWrite<Boolean>(
                attr.getName(),
                Boolean.class, 
                info.getName(), 
                info.getDescription()) {
            
            Attribute attribute = attr;
            
            public Boolean getValue() {
                return (Boolean) attribute.getValue(); 
            }
            
            public void setValue(Boolean val) {
                attribute = updateAttribute(parent, getName(), val, attribute);
            }
        };
    }
        
    private static PropertySupport getReadWriteStringPropertySupport(
            final AppserverJBIMgmtNode parent, 
            final Attribute attr,
            final MBeanAttributeInfo info) {
        
        return new PropertySupport.ReadWrite<String>(
                attr.getName(),
                String.class, //getClassFromStringType(info.getType()), 
                info.getName(), 
                info.getDescription()) {
            
            Attribute attribute = attr;
            
            public String getValue() {
                return (String) calculateReturnObjectType(attribute);
            }
            
            public void setValue(String obj) {
                attribute = updateAttribute(parent, getName(), obj, attribute);
            }
        };
    }
    
    private static PropertySupport getReadWriteIntegerPropertySupport(
            final AppserverJBIMgmtNode parent, 
            final Attribute attr,
            final MBeanAttributeInfo info) {
        
        return new PropertySupport.ReadWrite<Integer>(
                attr.getName(),
                Integer.class, //getClassFromStringType(info.getType()), 
                info.getName(), 
                info.getDescription()) {
            
            Attribute attribute = attr;
            
            public Integer getValue() {
                return (Integer) calculateReturnObjectType(attribute);
            }
            
            public void setValue(Integer obj) {
                attribute = updateAttribute(parent, getName(), obj, attribute);
            }
        };
    }
    
    private static PropertySupport createTabularDataProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        return createTabularDataProperty(
                parent, attr, info, SimpleTabularDataEditor.class);
    }
    
    private static PropertySupport createEnvironmentVariablesProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        return createTabularDataProperty(
                parent, attr, info, EnvironmentVariablesEditor.class);
    }
    
    private static PropertySupport createTabularDataProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info, 
            final Class editorClass) {

        return new PropertySupport.ReadWrite<TabularDataSupport>(
                info.getName(), // attr.getName() ?
                TabularDataSupport.class,
                info.getName(),
                info.getDescription()) {

            Attribute attribute = attr;

            public TabularDataSupport getValue() {
                return (TabularDataSupport) attribute.getValue();
            }

            public void setValue(TabularDataSupport val){
                attribute = updateAttribute(parent, getName(), val, attribute);
            }

            public PropertyEditor getPropertyEditor(){
                try {
                    return (PropertyEditor) editorClass.newInstance(); 
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
       
    public static PropertySupport createLogLevelProperty(
            final JBIComponentNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        
        return new PropertySupport.ReadWrite<Level>(
                info.getName(),
                Level.class,
                info.getName(),
                info.getDescription()) {
            
            Attribute attribute = attr;
            
            public Level getValue() {
                return (Level) attribute.getValue();
            }
            
            public void setValue(Level val){
                Attribute updatedAttribute = null;
                try {
                    updatedAttribute = parent.setLoggerSheetProperty(attr.getName(), val);
                } catch (RuntimeException rex) {
                    rex.printStackTrace();
                }
                if (updatedAttribute != null) {
                    attribute = updatedAttribute;
                }
            }
            
            public PropertyEditor getPropertyEditor(){
                return new JBILogLevelEditor();
            }
        };
    }
    
    public static PropertySupport createPasswordProperty(
            final AppserverJBIMgmtNode parent,
            final Attribute attr, 
            final MBeanAttributeInfo info) {
        
        return new PropertySupport.ReadWrite<String>(
                info.getName(),
                String.class,
                info.getName(),
                info.getDescription()) {
            
            Attribute attribute = attr;
            
            public String getValue() {
                return (String) attribute.getValue();
            }
            
            public void setValue(String val){
                attribute = updateAttribute(parent, getName(), val, attribute);
            }
            
            public PropertyEditor getPropertyEditor(){
                return new PasswordEditor();
            }
        };
    }
     
    private static Object calculateReturnObjectType(Attribute attr) {
        Object obj = attr.getValue();
//        if (obj instanceof ObjectName[]) {
//            ObjectName[] objNames = (ObjectName[])obj;
//            String[] arrayNames = new String[objNames.length];
//            for (int i=0; i < objNames.length; i++){
//                arrayNames[i] = objNames[i].toString();
//            }
//            return arrayNames;
//        } else if (obj instanceof String[]) {
//            String[] values = (String[])obj;
//            StringBuffer returnVals = new StringBuffer();
//            int pos = 0;
//            for(int i=0; i < values.length; i++){
//                returnVals.append(values[i]);
//                pos++;
//                if (pos < values.length) {
//                    returnVals.append(ARRAY_DELIM);
//                }
//            }
//            return returnVals;
//        } else {
            return obj;
//        }
    }    
    
//    private static Class getClassFromStringType(final String type) {
//        Class clazz = null;
//        try {
//            clazz = typeToClassesMap.get(type);
//            if (clazz == null) {
//                clazz = Class.forName(type);
//            }
//            if (clazz == null) {
//                throw new ClassNotFoundException(type);
//            }
//            if (!String.class.isAssignableFrom(clazz) && 
//                    !Number.class.isAssignableFrom(clazz)) {
//                throw new ClassNotFoundException(type);
//            }
//        } catch(ClassNotFoundException e) {
//            logger.log(Level.FINE, e.getMessage(), e);
//        }
//        return clazz;
//    }
            
    private static Attribute updateAttribute(AppserverJBIMgmtNode parent,
            String attrName, Object attrValue, Attribute attribute){
        Attribute updatedAttribute = null;
        try {
            updatedAttribute = parent.setSheetProperty(attrName, attrValue);
        } catch (RuntimeException rex) {
            rex.printStackTrace();
        }
        
        if (updatedAttribute != null) {
            return updatedAttribute;
        }
        
        return attribute;
    }    
}
