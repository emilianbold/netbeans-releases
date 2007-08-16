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

package org.netbeans.modules.websvc.manager.ui;

import com.sun.tools.ws.processor.model.java.JavaArrayType;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.model.java.JavaType;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author  David Botterill
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
         * TODO: make sure this get's specified included as a requirements spec for JAXRPC for the API
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
        if(childClassName.indexOf(".") == -1 && !(childType instanceof JavaSimpleType)) {
            childClassName = inPackageName + "." + childClassName;
        }
        Method method = null;
        Object childValue = null;
        if(childType instanceof JavaArrayType) {
            /**
             * If the child is an array, strip off the [] to get the class name.
             */
            if(childClassName.indexOf("[") > 0) {
                childClassName = childClassName.substring(0,childClassName.indexOf("["));
            }
            try {
                childClass = Class.forName(childClassName,true,urlClassLoader);
            } catch(ClassNotFoundException cnfe) {
                throw new WebServiceReflectionException("ClassNotFoundException",cnfe);
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
    
    public static Object getTypedParameterArray(ArrayList inArrayList, JavaType inType, 
        URLClassLoader urlClassLoader,String inPackageName)
    throws WebServiceReflectionException {
        if(null == urlClassLoader) return null;
        /**
         * First, strip off the "[]".
         */
        
        String parameterClassName = inPackageName + "." + inType.getFormalName();
        if(parameterClassName.indexOf("[") > 0) {
            parameterClassName = parameterClassName.substring(0,parameterClassName.indexOf("["));
        }
        Class parameterClass = null;
        try {
            parameterClass = Class.forName(parameterClassName,true,urlClassLoader);
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
                                              URLClassLoader urlClassLoader) throws WebServiceReflectionException {
        Class clazz = null;
        if(null == urlClassLoader) return null;
        
        /**
         * We need to save off the current classLoader and set the context to the one passed in for
         * executing the method.
         */        
        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            /**
             * Now set the new classLoader to the one passed in.
             */
            Thread.currentThread().setContextClassLoader(urlClassLoader);

            /**
             * Get an instance of the Class
             */
            try {
                clazz = Class.forName(inClassName, true, urlClassLoader);
            } catch (ClassNotFoundException cnfe) {
                throw new WebServiceReflectionException("ClassNotFoundException", cnfe);
            }

            /**
             * Instantiate the Class so we can call the method on it.
             */
            Object classInstance = null;
            try {
                classInstance = clazz.newInstance();
            } catch (InstantiationException ia) {
                throw new WebServiceReflectionException("InstantiationExceptoin", ia);
            } catch (IllegalAccessException iae) {
                throw new WebServiceReflectionException("IllegalAccessException", iae);
            }


            Method method = null;
            Object[] paramValues = inParamList.toArray();
            /**
             * Take the parameters and make an array of Classes based on the type of each Object.
             * For each parameter, we need to have the type of the original parameter for the JavaMethod
             * and do the following conversions:
             * 1. from ArrayList to a typed array. (done prior)
             * 2. from objects to primitives
             */
            LinkedList classList = new LinkedList();
            List parameterList = inMethod.getParametersList();
            for (int ii = 0; null != paramValues && ii < paramValues.length; ii++) {

                /**
                 * If the parameter type is a primitive, we've stored the value as a reference
                 * type and need to convert it back to a primitive.
                 */
                Class classToAdd = null;
                if (null != parameterList && ii < parameterList.size()) {
                    JavaParameter actualParameter = (JavaParameter) parameterList.get(ii);
                    if (isPrimitiveClass(actualParameter.getType().getFormalName())) {
                        classToAdd = referenceClass2PrimitiveClass(paramValues[ii].getClass());
                    } else if (actualParameter.getType().getFormalName().equals("java.util.Calendar") && !actualParameter.isHolder()) {
                        classToAdd = java.util.Calendar.class;
                    }else if (paramValues[ii] == null) {
                        try {
                            classToAdd = Class.forName(actualParameter.getType().getFormalName(), true, urlClassLoader);
                        }catch (Exception ex) {
                            throw new WebServiceReflectionException("Exception", ex);
                        }
                    } else {
                        classToAdd = paramValues[ii].getClass();
                    }
                }
                classList.add(classToAdd);
            }
            Class[] paramClasses = (Class[]) classList.toArray(new Class[0]);

            /**
             * Now instantiate the method to call.
             */
            try {
                method = clazz.getMethod(inMethod.getName(), paramClasses);
            } catch (NoSuchMethodException nsme) {
                throw new WebServiceReflectionException("NoSuchMethodException", nsme);
            }


            Object returnObject = null;
            try {
                // Need to turn on the test mode before the invoking the method
                Method testModeMethod = clazz.getMethod("testMode", new Class[]{Boolean.class});
                testModeMethod.invoke(classInstance, new Object[]{Boolean.TRUE});

                // Invoke the method
                returnObject = method.invoke(classInstance, paramValues);

                // And turn the test mode off when finishing
                testModeMethod.invoke(classInstance, new Object[]{Boolean.FALSE});
            } catch (InvocationTargetException ite) {
                throw new WebServiceReflectionException("InvocationTargetException", ite);
            } catch (IllegalArgumentException ia) {
                throw new WebServiceReflectionException("IllegalArgumentException", ia);
            } catch (IllegalAccessException iae) {
                throw new WebServiceReflectionException("IllegalAccessException", iae);
            } catch (Exception e) {
                throw new WebServiceReflectionException("Exception", e);
            }

            return returnObject;
        }finally {
            // Reset the classloader
            Thread.currentThread().setContextClassLoader(savedLoader);
        }
        
    }
    
    public static Object getStructureValue(NodeData inParentData, JavaStructureMember inMember,
    URLClassLoader urlClassLoader,String inPackageName)
    throws WebServiceReflectionException {
        
        Class typeClass = null;
        if(null == urlClassLoader) return null;
        /**
         * Get the class of the parent structure to look for the getter method on
         */
        JavaType parentType = inParentData.getNodeType();
        Object parentValue = inParentData.getNodeValue();
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
         *  The parent type, JavaStructureType should have the getter method name in the JavaStructureMember for
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
    
}
