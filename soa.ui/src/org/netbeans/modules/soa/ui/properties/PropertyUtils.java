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
package org.netbeans.modules.soa.ui.properties;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.ClassBasedPool;
import org.netbeans.modules.soa.ui.SoaConstants;
import org.netbeans.modules.soa.ui.properties.editors.PropBundle;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Utility class. It helps to register different kinds of properties.
 *
 * @author Vitaly Bychkov
 * @author nk160297
 */
public abstract class PropertyUtils {
    
    public static final ClassBasedPool<Component> propertyCustomizerPool =
            new ClassBasedPool<Component>();
    
    public static final ClassBasedPool<PropertyEditor> propertyEditorPool =
            new ClassBasedPool<PropertyEditor>();
    
    
    public Node.Property registerCalculatedProperty(
            Object objRef, Sheet.Set targetPropertySet,
            PropertyType propertyType,
            String getterName, String setterName) {
        return registerCalculatedProperty( objRef , targetPropertySet ,
                null, null, propertyType , getterName , setterName , null );
    }
    
    public Node.Property registerCalculatedProperty(
            Object objRef, Sheet.Set targetPropertySet,
            PropertyType propertyType,
            String getterName, String setterName, String removerName) {
        return registerCalculatedProperty( objRef , targetPropertySet ,
                null, null, propertyType , getterName , setterName , removerName );
    }
    
    public Node.Property registerCalculatedProperty(
            Object objRef, Sheet.Set targetPropertySet,
            String boundAttributeName,
            Class boundElementClass,
            PropertyType propertyType,
            String getterName, String setterName, String removerName ) {
        return registerPropertyImpl(
                null,
                objRef,
                targetPropertySet,
                boundAttributeName, boundElementClass,
                propertyType,
                getterName, setterName, removerName );
    }
    
    public Node.Property registerProperty(
            InstanceRef instanceRef,
            Sheet.Set targetPropertySet,
            PropertyType propertyType,
            String getterName, String setterName, String removerName) {
        return registerPropertyImpl(
                instanceRef,
                null,
                targetPropertySet,
                null, null,
                propertyType,
                getterName, setterName, removerName);
    }
    
    public Node.Property registerElementProperty(
            InstanceRef instanceRef,
            Object objRef,
            Sheet.Set targetPropertySet,
            Class boundElementClass,
            PropertyType propertyType,
            String getterName, String setterName, String removerName) {
        return registerPropertyImpl(
                instanceRef,
                objRef,
                targetPropertySet,
                null,
                boundElementClass,
                propertyType,
                getterName, setterName, removerName);
    }
    
    public Node.Property registerAttributeProperty(
            InstanceRef instanceRef,
            Sheet.Set targetPropertySet,
            String boundAttributeName,
            PropertyType propertyType,
            String getterName, String setterName, String removerName) {
        return registerPropertyImpl(
                instanceRef,
                null,
                targetPropertySet,
                boundAttributeName,
                null,
                propertyType,
                getterName, setterName, removerName);
    }
    
    /**
     * This method provides a common implementation for other public methods.
     */
    private Node.Property registerPropertyImpl(
            InstanceRef instanceRef,
            final Object objRef,
            Sheet.Set targetPropertySet,
            String boundAttributeName,
            Class boundElementClass,
            PropertyType propertyType,
            String getterName, String setterName, String removerName) {
        try {
            Class propClass = propertyType.getPropertyClass();
            String propName = propertyType.toString();
            String displayName = propertyType.getDisplayName();
            Class propEditorClass = propertyType.getPropertyEditorClass();
            //
            if (objRef == null && instanceRef != null) {
                PropertyUtils.Reflection prop = getReflection(
                        instanceRef, propClass, getterName, setterName, removerName);
                
                if (prop != null) {
                    prop.setName(propName);
                    prop.setValue(SoaConstants.PROPERTY_TYPE_ATTRIBUTE, propertyType);
                    prop.setDisplayName(displayName);

                    if (propEditorClass != null) {
                        prop.setPropertyEditorClass(propEditorClass);
                    }
                    //
                    if (boundAttributeName != null && boundAttributeName.length() > 0) {
                        prop.setValue(SoaConstants.BOUNDED_ATTRIBUTE_NAME,
                                boundAttributeName);
                    }
                    if (boundElementClass != null) {
                        prop.setValue(SoaConstants.BOUNDED_ELEMENT_CLASS,
                                boundElementClass);
                    }
                    //
                    targetPropertySet.put(prop);
                    return prop;
                }
            } else if (objRef != null && instanceRef == null) {
                //
                // Create fake instance ref which return objRef
                InstanceRef tempInnstanceRef = new InstanceRef() {
                    public Object getReference() {
                        return objRef;
                    }
                    public Object getAlternativeReference() {
                        return null;
                    }
                };
                //
                PropertyUtils.Reflection prop = getReflection(
                        tempInnstanceRef, propClass, getterName, setterName,
                        removerName);
                //                PropertySupport.Reflection prop = new PropertySupport.Reflection(
                //                        objRef, propClass, getterName, setterName);
                // The remover method doesn't supported in this case!
                //
                if (prop != null) {
                    prop.setName(propName);
                    prop.setValue(SoaConstants.PROPERTY_TYPE_ATTRIBUTE, propertyType);
                    prop.setDisplayName(displayName);
                    if (propEditorClass != null) {
                        prop.setPropertyEditorClass(propEditorClass);
                    }
                    //
                    if (boundAttributeName != null && boundAttributeName.length() > 0) {
                        prop.setValue(SoaConstants.BOUNDED_ATTRIBUTE_NAME,
                                boundAttributeName);
                    }
                    if (boundElementClass != null) {
                        prop.setValue(SoaConstants.BOUNDED_ELEMENT_CLASS,
                                boundElementClass);
                    }
                    //
                    targetPropertySet.put(prop);
                    return prop;
                }
            } else {
                assert true : "Illegal arguments!"; // NOI18N
            }
            
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    /**
     * This method is intended to populate the column model of tables.
     *
     * TODO
     * This approach is artificial and can be replaced in future.
     * Really it seems the Column model can use PropertyType for the initialization.
     */
    public Node.Property createPropertyStub(PropertyType propertyType) {
        Node.Property prop = null;
        try {
            Class propClass = propertyType.getPropertyClass();
            String propName = propertyType.toString();
            String displayName = propertyType.getDisplayName();
            Class propEditorClass = propertyType.getPropertyEditorClass();
            //
            prop = new PropertySupport.ReadOnly(propName, propClass,
                    displayName, "This property is stub!") {  // NOI18N
                
                public Object getValue()
                        throws IllegalAccessException, InvocationTargetException {
                    return null;
                }
                
            };
            if (prop != null) {
                prop.setValue(SoaConstants.PROPERTY_TYPE_ATTRIBUTE, propertyType);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return prop;
    }
    
    //    public static Node.Property registerSimpleTypeProperty(
    //            Sheet.Set targetPropertySet, String propName, String displayName,
    //            Class propClass, Object referent,
    //            String getterName, String setterName) {
    //        Node.Property prop = null;
    //        try {
    //            prop = new PropertySupport.Reflection(
    //                    referent, propClass, getterName, setterName);
    //            if (prop != null) {
    //                prop.setName(propName);
    //                prop.setDisplayName(displayName);
    //                targetPropertySet.put(prop);
    //            }
    //        } catch (NoSuchMethodException ex) {
    //            ErrorManager.getDefault().notify(ex);
    //        }
    //        return prop;
    //    }
    
    //======================================================================
    
    public Node.Property lookForPropertyByName(
            Node node, String desiredPropName) {
        if (desiredPropName == null || desiredPropName.length() == 0) {
            return null;
        }
        //
        for (Node.PropertySet propSet : node.getPropertySets()) {
            for (Node.Property prop : propSet.getProperties()) {
                //
                // Check if the source property has a name. If it isn't named then skip it.
                String propName = prop.getName();
                if (desiredPropName.equals(propName)) {
                    return prop;
                }
            }
        }
        return null;
    }
    
    public Node.Property lookForPropertyByType(
            Node node, PropertyType desiredPropType) {
        if (desiredPropType == null) {
            return null;
        }
        //
        return lookForPropertyByType(node.getPropertySets(), desiredPropType);
    }
    
    public Node.Property lookForPropertyByType(
            Node.PropertySet[] propSetArr, PropertyType desiredPropType) {
        //
        for (Node.PropertySet propSet : propSetArr) {
            for (Node.Property prop : propSet.getProperties()) {
                //
                // Check if the source property has a name. If it isn't named then skip it.
                Object propType = prop.getValue(
                        SoaConstants.PROPERTY_TYPE_ATTRIBUTE);
                if (propType == null) {
                    continue;
                }
                assert propType instanceof PropertyType;
                if (desiredPropType.equals((PropertyType)propType)) {
                    return prop;
                }
            }
        }
        return null;
    }
    
    public Node.Property lookForPropertyByBoundedAttribute(
            Node node, String attrName) {
        if (attrName == null || attrName.length() == 0) {
            return null;
        }
        //
        for (Node.PropertySet propSet : node.getPropertySets()) {
            for (Node.Property prop : propSet.getProperties()) {
                //
                // Check if the source property has a name. If it isn't named then skip it.
                Object boundedAttributeName = prop.getValue(
                        SoaConstants.BOUNDED_ATTRIBUTE_NAME);
                if (boundedAttributeName == null) {
                    continue;
                }
                assert boundedAttributeName instanceof String;
                if (attrName.equals((String)boundedAttributeName)) {
                    return prop;
                }
            }
        }
        return null;
    }
    
    public Node.Property lookForPropertyByBoundedElement(
            Node node, Class elementClass) {
        if (elementClass == null) {
            return null;
        }
        //
        for (Node.PropertySet propSet : node.getPropertySets()) {
            for (Node.Property prop : propSet.getProperties()) {
                //
                // Check if the source property has a name. If it isn't named then skip it.
                Object boundedElementClass = prop.getValue(
                        SoaConstants.BOUNDED_ELEMENT_CLASS);
                if (boundedElementClass == null) {
                    continue;
                }
                assert boundedElementClass instanceof Class;
                if (((Class)boundedElementClass).isAssignableFrom(elementClass)) {
                    return prop;
                }
            }
        }
        return null;
    }
    
    public abstract Reflection getReflection(InstanceRef instanceRef, Class valueType,
                Method getter, Method setter, Method remover);
    
    public abstract Reflection getReflection(InstanceRef instanceRef, Class valueType,
                String getterName, String setterName, String removerName) throws NoSuchMethodException;
    //======================================================================
    
    /**
     * Support for properties which take the instance from an
     * InstanceRef.getReference() and use Java Reflection for looking the methods.
     */
    public static class Reflection extends Node.Property {
        /** Instance of a bean. */
        protected InstanceRef myInstanceRef;
        
        /** setter method */
        private Method mySetter;
        
        /** getter method */
        private Method myGetter;
        
        /** remover method */
        private Method myRemover;
        
        /** class of property editor */
        private Class propertyEditorClass;
        
        private PropertyEditor myPropertyEditor;
        
        /** Create a support with method objects specified.
         * The methods must be public.
         * @param instanceRef the InstanceRef to work on
         * @param valueType type of the property
         * @param getter getter method, can be <code>null</code>
         * @param setter setter method, can be <code>null</code>
         * @param remover remover method, can be <code>null</code>
         * @throws IllegalArgumentException if the methods are not public
         */
        public Reflection(InstanceRef instanceRef, Class valueType,
                Method getter, Method setter, Method remover) {
            super(valueType);
            assert instanceRef != null;
            //
            this.myInstanceRef = instanceRef;
            this.mySetter = setter;
            this.myGetter = getter;
            this.myRemover = remover;
            //
            checkMethods();
        }
        
        /** Create a support with methods specified by name.
         * The instance class will be examined for the named methods.
         * But if the instance class is not public, the nearest public superclass
         * will be used instead, so that the getters and setters remain accessible.
         * @param parentNode the BpelNode to work on
         * @param valueType type of the property
         * @param getter name of getter method, can be <code>null</code>
         * @param setter name of setter method, can be <code>null</code>
         * @param remover remover method, can be <code>null</code>
         * @exception NoSuchMethodException if the getter or setter methods cannot be found
         */
        public Reflection(InstanceRef instanceRef, Class valueType,
                String getterName, String setterName, String removerName)
                throws NoSuchMethodException {
            super(valueType);
            assert instanceRef != null;
            //
            Method getter = null;
            Method setter = null;
            Method remover = null;
            //
            this.myInstanceRef = instanceRef;
            Class[] targetClassesArr = findAccessibleClasses(instanceRef);
            //
            if (getterName != null) {
                getter = tryFindMethodByName(
                        targetClassesArr, getterName, new Class[0]);
            }
            //
            if (setterName != null) {
                setter = tryFindMethodByName(
                        targetClassesArr, setterName, new Class[] {valueType});
            }
            //
            if (removerName != null) {
                remover = tryFindMethodByName(
                        targetClassesArr, removerName, new Class[0]);
            }
            //
            this.mySetter = setter;
            this.myGetter = getter;
            this.myRemover = remover;
            //
            checkMethods();
        }
        
        private Class[] findAccessibleClasses(InstanceRef instanceRef) {
            Class[] result = new Class[2];
            int counter = 0;
            //
            Object instance = instanceRef.getReference();
            if (instance != null){
                Class targetClass = findAccessibleClass(instance);
                if (targetClass != null) {
                    result[counter] = targetClass;
                    counter++;
                }
            }
            //
            instance = instanceRef.getAlternativeReference();
            if (instance != null){
                Class targetClass = findAccessibleClass(instance);
                if (targetClass != null) {
                    result[counter] = targetClass;
                    counter++;
                }
            }
            //
            if (counter == 0) {
                throw new RuntimeException("InstanceRef doesn't provide any instances"); // NOI18N
            }
            //
            return result;
        }
        
        /**
         * Looks for the method in the classes from the targetClassesArr.
         * The first suitable method is returned.
         * The NoSuchMethodException is thrown if no any method is found.
         */
        private Method tryFindMethodByName(Class[] targetClassesArr,
                String methodName, Class[] params) throws NoSuchMethodException {
            Method result = null;
            boolean methodFound = false;
            if (methodName != null) {
                for (Class targetClass : targetClassesArr) {
                    if (targetClass != null) {
                        try {
                            result = targetClass.getMethod(methodName, params);
                        } catch (NoSuchMethodException ex) {
                            // do nothing
                        }
                    }
                    if (result != null) {
                        methodFound = true;
                        break;
                    }
                }
            }
            if (!methodFound) {
                StringBuffer classesNames = new StringBuffer();
                boolean isFirst = true;
                for (Class classObj : targetClassesArr) {
                    if (classObj != null) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            classesNames.append("; "); // NOI18N
                        }
                        classesNames.append(classObj.getName());
                    }
                }
                //
                throw new NoSuchMethodException(
                        "The method \"" + methodName + "\" can't be found " +
                        "by the PropertyUtils.Reflection in the classes: " +
                        classesNames); // NOI18N
            }
            return result;
        }
        
        /** Create a support based on the property name.
         * The getter and setter methods are constructed by capitalizing the first
         * letter in the name of propety and prefixing it with <code>get</code> and
         * <code>set</code>, respectively.
         *
         * @param instanceRef the InstanceRef to work on
         * @param valueType type of the property
         * @param property name of property
         * @exception NoSuchMethodException if the getter or setter methods cannot be found
         */
        public Reflection(InstanceRef instanceRef, Class valueType, String property)
                throws NoSuchMethodException {
            super(valueType);
            assert instanceRef != null;
            //
            Method getter = null;
            Method setter = null;
            Method remover = null;
            //
            Object instance = instanceRef.getReference();
            Class targetClass = findAccessibleClass(instance);
            //
            getter = targetClass.getMethod(
                    firstLetterToUpperCase(property, "get"), new Class[0]); // NOI18N
            if (getter == null) {
                getter = targetClass.getMethod(
                        firstLetterToUpperCase(property, "is"), new Class[0]); // NOI18N
            }
            //
            setter = targetClass.getMethod(
                    firstLetterToUpperCase(property, "set"), new Class[] {valueType}); // NOI18N
            //
            remover = targetClass.getMethod(
                    firstLetterToUpperCase(property, "remove"), new Class[0]); // NOI18N
            //
            this.myInstanceRef = instanceRef;
            this.mySetter = setter;
            this.myGetter = getter;
            this.myRemover = remover;
            //
            checkMethods();
        }
        
        private void checkMethods() throws IllegalArgumentException {
            if ((myGetter != null) && !Modifier.isPublic(myGetter.getModifiers())) {
                throw new IllegalArgumentException(
                        "Cannot use a non-public getter " + myGetter); // NOI18N
            }
            
            if ((mySetter != null) && !Modifier.isPublic(mySetter.getModifiers())) {
                throw new IllegalArgumentException(
                        "Cannot use a non-public setter " + mySetter); // NOI18N
            }
            
            if ((myRemover != null) && !Modifier.isPublic(myRemover.getModifiers())) {
                throw new IllegalArgumentException(
                        "Cannot use a non-public remover " + myRemover); // NOI18N
            }
            //
        }
        
        /**
         * Return the reference ptovider associated with the Property
         */
        public InstanceRef getInstanceRef() {
            return myInstanceRef;
        }
        
        protected Class findAccessibleClass(Object instance) {
            return findAccessibleClass(instance.getClass());
        }
        
        /** Find the nearest superclass (or same class) that is public to this one. */
        protected Class findAccessibleClass(Class clazz) {
            if (Modifier.isPublic(clazz.getModifiers())) {
                return clazz;
            } else {
                Class sup = clazz.getSuperclass();
                
                if (sup == null) {
                    return Object.class; // handle interfaces
                }
                
                return findAccessibleClass(sup);
            }
        }
        
        /** Helper method to convert the first letter of a string to uppercase.
         * And prefix the string with some next string.
         */
        private static String firstLetterToUpperCase(String s, String pref) {
            switch (s.length()) {
            case 0:
                return pref;
                
            case 1:
                return pref + Character.toUpperCase(s.charAt(0));
                
            default:
                return pref + Character.toUpperCase(s.charAt(0)) + s.substring(1);
            }
        }
        
        /* Can read the value of the property.
         * @return <CODE>true</CODE> if the read of the value is supported
         */
        public boolean canRead() {
            return myGetter != null;
        }
        
        /* Getter for the value.
         * @return the value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public Object getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (myGetter == null) {
                throw new IllegalAccessException();
            }
            //
            try {
                try {
                    return invokeMethod(myInstanceRef, myGetter, new Object[0]);
                } catch (IllegalAccessException ex) {
                    try {
                        myGetter.setAccessible(true);
                        //
                        return invokeMethod(myInstanceRef, myGetter, new Object[0]);
                    } finally {
                        myGetter.setAccessible(false);
                    }
                }
            } catch (IllegalArgumentException iae) {
                //Provide a better message for debugging
                StringBuffer sb = new StringBuffer("Attempted to invoke method "); // NOI18N
                sb.append(myGetter.getName());
                sb.append(" from class "); // NOI18N
                sb.append(myGetter.getDeclaringClass().getName());
                sb.append(" on an instance of "); // NOI18N
                sb.append(myGetter.getDeclaringClass().getName());
                sb.append(" Problem:"); // NOI18N
                sb.append(iae.getMessage());
                
                IllegalArgumentException nue = new IllegalArgumentException(sb.toString());
                ErrorManager.getDefault().annotate(nue, iae);
                throw nue;
            }
        }
        
        /* Can write the value of the property.
         * @return <CODE>true</CODE> if the write of the value is supported
         */
        public boolean canWrite() {
            Model model = getModel();
            if (model != null) {
                if (!XAMUtils.isWritable(model)) return false;
            }
            return mySetter != null;
        }
        
        
        public Model getModel() {
            DocumentComponent dc = getDocumentComponent();
            return (dc != null) ? dc.getModel() : null;
        }
        
        
        public DocumentComponent getDocumentComponent() {
            Object ref = myInstanceRef.getReference();
            
            if (ref != null) {
                if (ref instanceof DocumentComponent) {
                    return (DocumentComponent) ref;
                }
            }
            
            if (myInstanceRef.getAlternativeReference() != null) {
                if (myInstanceRef.getAlternativeReference()
                        instanceof DocumentComponent) {
                    return (DocumentComponent) myInstanceRef.getAlternativeReference();
                }
            }
            
            return null;
        }
        
        
        /* Setter for the value.
         * @param val the value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public void setValue(Object newValue)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            try {
//System.out.println();
//System.out.println("SET VALUE");
                if (newValue == null ||
                        (newValue instanceof String &&
                        ((String)newValue).length() == 0) ) {
                    if (canRemove()) {
                        try {
                            invokeMethod(myInstanceRef, myRemover, new Object[0]);
                        } catch (IllegalAccessException ex) {
                            try {
                                myRemover.setAccessible(true);
                                invokeMethod(myInstanceRef, myRemover, new Object[0]);
                            } finally {
                                myRemover.setAccessible(false);
                            }
                        }
                    } else {
                        Object oldValue = getValue();
                        if (oldValue == null ||
                                (oldValue instanceof String &&
                                ((String)oldValue).length() == 0) ) {
                            // DO NOTHING because of value hasn't been changed
                        } else {
                            // The following code is dangerous because of the
                            // property editor can be used for other purposes.
                            PropertyEditor pEditor = getPropertyEditor();
                            pEditor.setValue(oldValue);
                            String oldText = pEditor.getAsText();
                            //
                            if (oldText == null || oldText.length() == 0) {
                                // DO NOTHING because of value hasn't been changed
                            } else {
                                //
                                String msg = NbBundle.getMessage(
                                        PropBundle.class, "ERR_REQURED_PROPERTY"); // NOI18N
                                PropertyType pType = (PropertyType)getValue(
                                        SoaConstants.PROPERTY_TYPE_ATTRIBUTE);
                                String propName;
                                if (pType != null) {
                                    propName = pType.getDisplayName();
                                } else {
                                    propName = getName();
                                }
                                //
                                msg = MessageFormat.format(msg, propName);
                                throw new InvocationTargetException(
                                        new VetoException(msg, null));
                            }
                        }
                    }
                } else {
                    try {
                        if (mySetter == null) {
                            throw new IllegalAccessException();
                        }
                        //
                        invokeMethod(myInstanceRef, mySetter, new Object[] {newValue});
                    } catch (IllegalAccessException ex) {
                        try {
                            mySetter.setAccessible(true);
                            invokeMethod(myInstanceRef, mySetter, new Object[] {newValue});
                        } finally {
                            mySetter.setAccessible(false);
                        }
                    }
                }
            } catch (InvocationTargetException ex) {
                Throwable targetEx = ex.getTargetException();
                //
                boolean processed = false;
                if (targetEx instanceof VetoException) {
                    //
                    // Check if the setValue has been called from the property sheet
                    StackTraceElement[] stackTraceArr = ex.getStackTrace();
                    for (StackTraceElement stElement : stackTraceArr) {
                        if (stElement.getMethodName().equals("stopCellEditing") &&
                                stElement.getClassName().equals(
                                "org.openide.explorer.propertysheet.SheetCellEditor")) { // NOI18N
                            //
                            // The setValue is invoked from the propery sheet inplace editor
                            //
                            UserNotification.showMessage(
                                    targetEx.getLocalizedMessage());
                            processed = true;
                            break;
                        } else if (stElement.getMethodName().equals("cancelValue") &&
                                stElement.getClassName().equals(
                                "org.openide.explorer.propertysheet.PropertyDialogManager")) { // NOI18N
                            //
                            // Ignore the exception if user press the Cancel button
                            processed = true;
                            break;
                        } else if (stElement.getMethodName().equals("actionPerformed") &&
                                stElement.getClassName().equals(
                                "org.openide.explorer.propertysheet.PropertyDialogManager")) { // NOI18N
                            //
                            // The setValue is invoked from the propery customizer dialog
                            //
                            throw new PropertyVetoError(targetEx);
                        }
                    }
                } else if (!(targetEx instanceof Exception)) {
                    ErrorManager.getDefault().notify(targetEx);
                    processed = false;
                }
                //
                if (!processed) {
                    throw ex;
                }
            }
        }
        
        /* Can remove the value of the property.
         * @return <CODE>true</CODE> if the remove of the value is supported
         */
        public boolean canRemove() {
//System.out.println();
//System.out.println("can remove");
            Model model = getModel();
            if (model != null) {
//System.out.println("1: " + XAMUtils.isWritable(model));
                if (!XAMUtils.isWritable(model)) return false;
            }
//System.out.println("r: " + myRemover);
            return myRemover != null;
        }
        
        public void removeValue()
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (myRemover == null) {
                throw new IllegalAccessException();
            }
            
            //
            setValue(null);
            
        }
        
        /* Returns property editor for this property.
         * @return the property editor or <CODE>null</CODE> if there should not be
         *    any editor.
         */
        public synchronized PropertyEditor getPropertyEditor() {
            if (myPropertyEditor == null) {
                if (propertyEditorClass != null) {
                    Object result = null;
                    //
                    //                if (propertyEditorClass != null &&
                    //                        Reusable.class.isAssignableFrom(propertyEditorClass)) {
                    //                    result = PropertyUtils.propertyEditorPool.
                    //                            getObjectByClass(propertyEditorClass);
                    //                }
                    //                //
                    //                // Try to load static method "getInstance"
                    //                if (result == null) {
                    //                    try {
                    //                        Method method = propertyEditorClass.
                    //                                getMethod("getInstance"); // NOI18N
                    //                        result = method.invoke(propertyEditorClass);
                    //                    } catch (Exception ex) {
                    //                        // DO Nothing
                    //                    }
                    //                }
                    //
                    // Try to call the default constructor
                    if (result == null) {
                        try {
                            result = propertyEditorClass.newInstance();
                        } catch (Exception ex) {
                            // DO Nothing
                        }
                    }
                    //
                    if (result instanceof PropertyEditor) {
                        myPropertyEditor = (PropertyEditor)result;
                    }
                }
            }
            //
            if (myPropertyEditor != null) {
                return myPropertyEditor;
            } else {
                return super.getPropertyEditor();
            }
        }
        
        /** Set the property editor explicitly.
         * @param clazz class type of the property editor
         */
        public void setPropertyEditorClass(Class clazz) {
            propertyEditorClass = clazz;
        }
        
        private Object invokeMethod(InstanceRef instanceRef,
                Method method, Object... args)
                throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException {
            Class<?> requiredClass = method.getDeclaringClass();
            //
            Object instance = instanceRef.getReference();
            if (instance != null && requiredClass.isAssignableFrom(instance.getClass())) {
                return method.invoke(instance, args);
            }
            //
            instance = instanceRef.getAlternativeReference();
            if (instance != null && requiredClass.isAssignableFrom(instance.getClass())) {
                return method.invoke(instance, args);
            }
            //
            throw new IllegalArgumentException("The method \"" + method.getName() +
                    "\" can't be called for the specified objecs"); // NOI18N
        }
    }
}
