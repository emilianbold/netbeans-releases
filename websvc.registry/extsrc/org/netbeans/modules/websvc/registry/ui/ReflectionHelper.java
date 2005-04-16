/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import com.sun.xml.rpc.processor.model.java.JavaParameter;
import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaArrayType;
import com.sun.xml.rpc.processor.model.java.JavaStructureMember;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaStructureType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationEntry;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;


import java.lang.reflect.Method;
import java.lang.reflect.Array;

import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;
import java.util.LinkedList;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import java.rmi.RemoteException;

import java.net.URLClassLoader;

/**
 *
 * @author  david
 */
public class ReflectionHelper {
    
    
    public static Object makeStructureType(JavaStructureType inType,URLClassLoader urlClassLoader,String inPackageName)
    throws WebServiceReflectionException {
        Class typeClass = null;
        if(null == urlClassLoader) return null;
        /**
         * We need to save off the current classLoader and set the context to the one passed in for
         * executing the method.
         */
        
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        
        /**
         * Now set the new classLoader to the one passed in.
         */
        Thread.currentThread().setContextClassLoader(urlClassLoader);
        String className = inPackageName + "." + inType.getFormalName();
        
        Object returnValue = null;
        try {
            typeClass = Class.forName(className,true,urlClassLoader);
        } catch(ClassNotFoundException cnfe) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
        }
        
        try {
            returnValue  = typeClass.newInstance();
        } catch(InstantiationException ia) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("InstantiationException",ia);
        } catch(IllegalAccessException iae) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("IllegalAccessException",iae);
        }
        
        /**
         * Make sure to reset the classloader
         */
        Thread.currentThread().setContextClassLoader(savedLoader);
        
        return returnValue;
    }
    
    public static Object makeEnumerationType(JavaEnumerationType inType,URLClassLoader urlClassLoader,String inPackageName, Object value)
    throws WebServiceReflectionException {
        Class typeClass = null;
        if(null == urlClassLoader) return null;
        
        try {
            typeClass = Class.forName(inPackageName + "." + inType.getFormalName(),
            true,
            urlClassLoader);
        } catch(ClassNotFoundException cnfe) {
            throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
        }
        
        /**
         * Now get the static method "fromString" for the class.
         * TODO: make sure this get's specified included as a requirements spec for JAXRPC
         */
        
        Method method = null;
        try {
            Class [] paramClasses = new Class[] {String.class};
            method = typeClass.getMethod("fromString", paramClasses);
            
        } catch(NoSuchMethodException nsme) {
            throw new WebServiceReflectionException("NoSuchMethodException",nsme);
        }
        
        String literalValue=null;
        if (value!=null && value instanceof String) {
            literalValue = (String)value;
        } else {
            /**
             * Get the first Enumeration entry and us it to get the Enumeration instance.
             */
            JavaEnumerationEntry entry = (JavaEnumerationEntry)((JavaEnumerationType)inType).getEntriesList().get(0);
            literalValue = entry.getLiteralValue();
        }
        
        Object returnObject = null;
        try {
            String [] params = new String[1];
            params[0] = literalValue;
            returnObject = method.invoke(typeClass,params);
        } catch(InvocationTargetException ite) {
            throw new WebServiceReflectionException("InvocationTargetException",ite);
            
        } catch(IllegalArgumentException ia) {
            throw new WebServiceReflectionException("IllegalArgumentException",ia);
            
        } catch(IllegalAccessException iae) {
            throw new WebServiceReflectionException("IllegalAccessException",iae);
        }
        
        return returnObject;
        
        
    }
    
    public static Object makeEnumerationType(JavaEnumerationType inType,URLClassLoader urlClassLoader,String inPackageName)
    throws WebServiceReflectionException {
        return makeEnumerationType(inType, urlClassLoader, inPackageName, null);  
    }
    
    public static void setStructureValue(TypeNodeData inParentData, TypeNodeData inChildData,
    URLClassLoader urlClassLoader,String inPackageName )
    throws WebServiceReflectionException {
        Class typeClass = null;
        if(null == urlClassLoader) return;
        
        JavaType parentType = inParentData.getParameterType();
        Object parentValue = inParentData.getParameterValue();
        try {
            typeClass = Class.forName(inPackageName + "." + parentType.getFormalName(),
            true,
            urlClassLoader);
        } catch(ClassNotFoundException cnfe) {
            throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
        }
        
        /**
         * Now get the "setter" for the child's field name.
         * TODO: make sure this get's specified included as a requirements spec for JAXRPC
         */
        /**
         *  The parent type, JavaStructureType should have the setter method name in the JavaStructureMember for
         *  the child.
         */
        JavaStructureMember member =  ((JavaStructureType)parentType).getMemberByName(inChildData.getParameterName());
        String setterName = member.getWriteMethod();
        
        /**
         * Now we have to create a class for the child type to use in looking for the Method.
         */
        Class childClass = null;
        String originalChildClassName = inChildData.getParameterType().getFormalName();
        JavaType childType = inChildData.getParameterType();
        String childClassName = originalChildClassName;
        Method method = null;
        Object childValue = null;
        if(childType instanceof JavaArrayType) {
            /**
             * If the child is an array, strip off the [] to get the class name.
             */
            if(childClassName.indexOf("[") > 0) {
                childClassName = childClassName.substring(0,childClassName.indexOf("["));
            }
            
            if(!ReflectionHelper.isPrimitiveClass(childClassName)) {
                try {
                    if(childClassName.indexOf(".") == -1) {
                        childClassName = inPackageName + "." + childClassName;
                    }
                    childClass = Class.forName(childClassName,true,urlClassLoader);
                } catch(ClassNotFoundException cnfe) {
                    throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
                }
            } else {
                childClass = ReflectionHelper.primitiveType2PrimitiveClass(childClassName);                
            }
            
            /**
             * If the child is an array type, we've stored it as an ArrayList.  Now we need
             * to create an array of the correct type and populate it with the values.
             */
            Object childArrayObject = null;
            if(null != inChildData.getParameterValue() &&
            inChildData.getParameterValue() instanceof ArrayList) {
                
                Object [] childObjects = ((ArrayList)inChildData.getParameterValue()).toArray();
                if(null == childObjects) {
                    childArrayObject = Array.newInstance(childClass,1);
                    
                } else {
                    childArrayObject = Array.newInstance(childClass,childObjects.length);
                    /**
                     * now set the values in the array.
                     */
                    for(int ii=0; ii < childObjects.length;ii++ ) {
                        Array.set(childArrayObject, ii, childObjects[ii]);
                    }
                }
                
                childValue = childArrayObject;
                
            } else {
                childArrayObject = Array.newInstance(childClass,1);
            }
            try {
                Class [] paramClasses = new Class[] {childArrayObject.getClass()};
                method = typeClass.getMethod(setterName, paramClasses);
                
            } catch(NoSuchMethodException nsme) {
                throw new WebServiceReflectionException("NoSuchMethodException",nsme);
            }
            
        } else {
            childValue = inChildData.getParameterValue();
            if(!ReflectionHelper.isPrimitiveClass(childClassName)) {
                /**
                 * If the class type of the child value is not primitive, we want to use reflection to load
                 * the class so we can use the runtime class loader.
                 */
                try {
                    if(childClassName.indexOf(".") == -1) {
                        childClassName = inPackageName + "." + childClassName;
                    }
                    childClass = Class.forName(childClassName,true,urlClassLoader);
                } catch(ClassNotFoundException cnfe) {
                    throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
                }
            } else {
                /**
                 * Now we need to get the Primitive class from the Object representation (e.g. for Float we need
                 * float).
                 */
                childClass = ReflectionHelper.referenceClass2PrimitiveClass(childValue.getClass());                
            }
            
            
            try {
                Class [] paramClasses = new Class[] {childClass};
                method = typeClass.getMethod(setterName, paramClasses);
                
            } catch(NoSuchMethodException nsme) {
                throw new WebServiceReflectionException("NoSuchMethodException",nsme);
            }            
        }
        
        
        Object returnObject = null;
        try {
            Object [] params = new Object[1];
            params[0] = childValue;
            returnObject = method.invoke(parentValue,params);
        } catch(InvocationTargetException ite) {
            throw new WebServiceReflectionException("InvocationTargetException",ite);
            
        } catch(IllegalArgumentException ia) {
            throw new WebServiceReflectionException("IllegalArgumentException",ia);
            
        } catch(IllegalAccessException iae) {
            throw new WebServiceReflectionException("IllegalAccessException",iae);
        }
        
    }
    
    public static Object getTypedParameterArray(ArrayList inArrayList, JavaType inType, URLClassLoader urlClassLoader, String inPackageName)
    throws WebServiceReflectionException {
        if(null == urlClassLoader) return null;
        /**
         * First, strip off the "[]".
         */
        
        String parameterClassName = inType.getFormalName();
        if(parameterClassName.indexOf("[") > 0) {
            parameterClassName = parameterClassName.substring(0,parameterClassName.indexOf("["));
        }
        
        Class parameterClass = null;
        try {
            parameterClass = Class.forName(inPackageName + "." + parameterClassName,true,urlClassLoader);
        } catch(ClassNotFoundException cnfe) {
            throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
        }
        /**
         * First we need to create a typed array for the return type.
         */
        Object typedArrayObject = Array.newInstance(parameterClass, new int[]{0});
        
        /**
         * Now get the array parameter.
         */
        Object typedArray = inArrayList.toArray((Object [])typedArrayObject);
        
        return typedArray;
        
    }
    
    public static Object callMethodWithParams(String inClassName, LinkedList inParamList, JavaMethod inMethod,
    URLClassLoader urlClassLoader, String inMethodName)
    throws WebServiceReflectionException {
        Class clazz = null;
        if(null == urlClassLoader) return null;
        
//        ClassLoader parent = urlClassLoader;
//        while(null!=parent) {
//            System.out.println("ClassLoader=" + parent);
//            if(parent instanceof URLClassLoader) {
//                URL [] urls = ((URLClassLoader)parent).getURLs();
//                for(int ii=0; urls != null && ii < urls.length; ii++) {
//                    System.out.println("\t\t URL[" + ii + "]=" + urls[ii].toString());
//                }
//            }
//            parent = parent.getParent();
//
//        }

        /**
         * We need to save off the current classLoader and set the context to the one passed in for
         * executing the method.
         */
        
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        
        /**
         * Now set the new classLoader to the one passed in.
         */
        Thread.currentThread().setContextClassLoader(urlClassLoader);
        
        /**
         * Get an instance of the Class
         */
        try {
            clazz = Class.forName(inClassName,true,urlClassLoader);
        } catch(ClassNotFoundException cnfe) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            
            throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
        }
        
        /**
         * Instantiate the Class so we can call the method on it.
         */
        Object classInstance = null;
        try {
            classInstance  = clazz.newInstance();
        } catch(InstantiationException ia) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("InstantiationExceptoin",ia);
        } catch(IllegalAccessException iae) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("IllegalAccessException",iae);
        }
        
        
        Method method = null;
        Object [] paramValues = inParamList.toArray();
        /**
         * Take the parameters and make an array of Classes based on the type of each Object.
         * For each parameter, we need to have the type of the original parameter for the JavaMethod
         * and do the following conversions:
         * 1. from ArrayList to a typed array. (done prior)
         * 2. from objects to primitives
         */
        LinkedList classList = new LinkedList();
        List parameterList = inMethod.getParametersList();
        for(int ii=0; null != paramValues && ii < paramValues.length; ii++ ) {
            
            /**
             * If the parameter type is a primitive, we've stored the value as a reference
             * type and need to convert it back to a primitive.
             */
            Class classToAdd = null;
            if(null != parameterList && ii < parameterList.size()) {
                JavaParameter actualParameter =  (JavaParameter)parameterList.get(ii);
                if(isPrimitiveClass(actualParameter.getType().getFormalName())){
                    classToAdd = referenceClass2PrimitiveClass(paramValues[ii].getClass());
                } else if(actualParameter.getType().getFormalName().equals("java.util.Calendar")) {
                    classToAdd = java.util.Calendar.class;
                } else {
                    classToAdd = paramValues[ii].getClass();
                }
            }
            classList.add(classToAdd);
        }
        Class [] paramClasses = (Class [])classList.toArray(new Class[0]);
        //        /**
        //         * list out the methods.
        //         */
        //        Method [] methods = clazz.getMethods();
        //        String methodName = null;
        //        for(int ii=0; ii < methods.length ;ii++){
        //            methodName = methods[ii].toString();
        //            System.err.println("Method [" + ii + "]=" + methodName);
        //        }
        /**
         * Now instantiate the method to call.
         */
        try {
            method = clazz.getMethod(inMethodName, paramClasses);
            
        } catch(NoSuchMethodException nsme) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("NoSuchMethodException",nsme);
        }
        
        
        Object returnObject = null;
        try {
            returnObject = method.invoke(classInstance,paramValues);
        } catch(InvocationTargetException ite) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("InvocationTargetException",ite);
            
        } catch(IllegalArgumentException ia) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("IllegalArgumentException",ia);
            
        } catch(IllegalAccessException iae) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("IllegalAccessException",iae);
        } catch(Exception e) {
            /**
             * Make sure to reset the classloader
             */
            Thread.currentThread().setContextClassLoader(savedLoader);
            throw new WebServiceReflectionException("Exception",e);
            
        }
        
        /**
         * Make sure to reset the classloader
         */
        Thread.currentThread().setContextClassLoader(savedLoader);
        
        return returnObject;
        
    }
    
    public static Object getStructureValue(ResultNodeData inParentData, JavaStructureMember inMember,
    URLClassLoader urlClassLoader,String inPackageName)
    throws WebServiceReflectionException {
        
        Class typeClass = null;
        if(null == urlClassLoader) return null;
        /**
         * Get the class of the parent structure to look for the getter method on
         */
        JavaType parentType = inParentData.getResultType();
        Object parentValue = inParentData.getResultValue();
        try {
            typeClass = Class.forName(inPackageName + "." + parentType.getFormalName(),
            true,
            urlClassLoader);
        } catch(ClassNotFoundException cnfe) {
            throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
        }
        
        /**
         * Now get the "getter" for the child's field name.
         * TODO: make sure this get's specified included as a requirements spec for JAXRPC
         */
        /**
         *  The parent type, JavaStructureType should have the setter method name in the JavaStructureMember for
         *  the child.
         */
        String getterName = inMember.getReadMethod();
        
        
        Method method = null;
        try {
            method = typeClass.getMethod(getterName, new Class[0]);
            
        } catch(NoSuchMethodException nsme) {
            throw new WebServiceReflectionException("NoSuchMethodException",nsme);
        }
        
        /**
         * Now use the getter to get the value of the subtype.
         */
        Object returnObject = null;
        try {
            returnObject = method.invoke(parentValue,new Object[0]);
        } catch(InvocationTargetException ite) {
            throw new WebServiceReflectionException("InvocationTargetException",ite);
            
        } catch(IllegalArgumentException ia) {
            throw new WebServiceReflectionException("IllegalArgumentException",ia);
            
        } catch(IllegalAccessException iae) {
            throw new WebServiceReflectionException("IllegalAccessException",iae);
        }
        
        return returnObject;
    }
    
    public static boolean isPrimitiveClass(String inType) {
        if(inType.equalsIgnoreCase("int")) {
            return true;
        } else if(inType.equalsIgnoreCase("byte")) {
            return true;
        } else if(inType.equalsIgnoreCase("boolean")) {
            return true;
        } else if(inType.equalsIgnoreCase("float")) {
            return true;
        } else if(inType.equalsIgnoreCase("double")) {
            return true;
        } else if(inType.equalsIgnoreCase("long")) {
            return true;
        } else if(inType.equalsIgnoreCase("short")) {
            return true;
        } else return false;
    }
    
    public static Class referenceClass2PrimitiveClass(Class inClass) {
        if(null == inClass) return inClass;
        if(inClass.getName().equalsIgnoreCase("java.lang.Boolean")) {
            return boolean.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Byte")) {
            return byte.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Double")) {
            return double.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Float")) {
            return float.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Integer")) {
            return int.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Long")) {
            return long.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Short")) {
            return short.class;
        } else if(inClass.getName().equalsIgnoreCase("java.lang.Character")) {
            return char.class;
        } else return inClass;
    }
    
    public static Class primitiveType2PrimitiveClass(String typeName) {
        if("boolean".equals(typeName)) {
            return boolean.class;
        } else if("int".equals(typeName)) {
            return int.class;
        } else if("long".equals(typeName)) {
            return long.class;
        } else if("double".equals(typeName)) {
            return double.class;
        } else if("byte".equals(typeName)) {
            return byte.class;
        } else if("float".equals(typeName)) {
            return float.class;
        } else if("short".equals(typeName)) {
            return short.class;
        } else if("char".equals(typeName)) {
            return char.class;
        }
        return null;
    }
}
