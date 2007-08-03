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

package org.netbeans.modules.form;

import java.util.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

public class CreationDescriptor {

    // style flags - for finding best creator for a set or properties
    public static final int CHANGED_ONLY = 1;
    public static final int PLACE_ALL = 2;

    public interface Creator {

        public int getParameterCount();

        public Class[] getParameterTypes();

        public Class[] getExceptionTypes();

        public String[] getPropertyNames();

        public Object createInstance(FormProperty[] props)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException;

        public Object createInstance(Object[] paramValues)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException;
        
        // [this will become useless when we can rely on getCodeOrigin(...)]
        public String getJavaCreationCode(FormProperty[] props, Class expressionType, String genericTypes);

        public CodeExpressionOrigin getCodeOrigin(CodeExpression[] params);
    }

    private Class describedClass;
    private ArrayList creators = new ArrayList(10);
    private Object[] defaultParams;
    private Creator defaultCreator;

    private static final Class[] emptyTypes = { };
    private static final String[] emptyNames = { };
    private static final Object[] emptyParams = { };

    public CreationDescriptor() {        
    }

    public CreationDescriptor(Class descClass,
                              Class[][] constrParamTypes,
                              String[][] constrPropNames,
                              Object[] defParams)
    throws NoSuchMethodException, // if some constructor is not found            
           IllegalArgumentException 
    {   
        addConstructorCreators(descClass, constrParamTypes, constrPropNames, defParams);
    }
    
    public CreationDescriptor(Class factoryClass, 
                              Class descClass,
                              String methodName,                                
                              Class[][] constrParamTypes,
                              String[][] constrPropNames,
                              CreationFactory.PropertyParameters[] propertyParameters,   
                              Object[] defParams)
    throws NoSuchMethodException, // if some method is not found                                                           
           IllegalArgumentException  
    {           
        addMethodCreators(factoryClass, descClass, methodName, constrParamTypes, 
                          constrPropNames, propertyParameters, defParams);
    }
    
    public void addConstructorCreators(Class descClass,
                                       Class[][] constrParamTypes,
                                       String[][] constrPropNames,
                                       Object[] defParams)
    throws NoSuchMethodException // if some constructor is not found
    {   
        setDescribedClass(descClass);        
        if (constrParamTypes != null && constrParamTypes.length > 0) {
            
            for (int i=0; i < constrParamTypes.length; i++)
                creators.add( new ConstructorCreator(describedClass,
                                                         constrParamTypes[i],
                                                         constrPropNames[i]) );;
        }

        defaultParams = defParams == null ? emptyParams : defParams;
    }

    public void addMethodCreators(Class factoryClass, 
                                  Class descClass,
                                  String methodName,                                
                                  Class[][] constrParamTypes,
                                  String[][] constrPropNames,
                                  CreationFactory.PropertyParameters[] propertyParameters,   
                                  Object[] defParams)
    throws NoSuchMethodException // if some method is not found
    {                               
        setDescribedClass(descClass);
        
        if (constrParamTypes != null && constrParamTypes.length > 0) {
            
            CreationFactory.Property2ParametersMapper[] properties;
            for (int i=0; i < constrParamTypes.length; i++) {
                
                properties = new CreationFactory.Property2ParametersMapper[constrParamTypes[i].length];
                for (int j = 0; j < constrParamTypes[i].length; j++) {
                    properties[j] = new CreationFactory.Property2ParametersMapper(constrParamTypes[i][j], constrPropNames[i][j]);
                    if(propertyParameters != null && propertyParameters.length > 0) {
                        for (int ppi = 0; ppi < propertyParameters.length; ppi++) {
                            if( propertyParameters[ppi].getPropertyName().equals(constrPropNames[i][j]) ) {
                                properties[j].setPropertyParameters(propertyParameters[ppi]);      
                            }
                        }                        
                    }
                }               
                
                creators.add( new MethodCreator(factoryClass, 
                                               describedClass,                        
                                               methodName,
                                               properties));  
                
            }
            
        }      
        
        defaultParams = defParams == null ? emptyParams : defParams;
    }
    
    public void addCreator(Creator creator, Object[] defaultParams) {
        creators.add(creator);
        this.defaultParams = defaultParams;
    }

    private void setDescribedClass(Class descClass) throws IllegalArgumentException {
        if(describedClass==null){
            describedClass = descClass;
        } else if (describedClass!=descClass) {
            throw new IllegalArgumentException();
        }        
    }
    
    public CreationDescriptor(Class descClass) {
//        throws NoSuchMethodException // if public empty constructor doesn't exist
        describedClass = descClass;

        try {
            ConstructorCreator creator = new ConstructorCreator(describedClass,
                                                                emptyTypes,
                                                                emptyNames);
            creators.add( creator );
        }
        catch (NoSuchMethodException ex) { // ignore
            Logger.getLogger(CreationDescriptor.class.getName())
                    .log(Level.INFO, "[WARNING] No default constructor for "+descClass.getName(), ex); // NOI18N
        }

        defaultParams = emptyParams;
    }

    // ---------

    public Class getDescribedClass() {
        return describedClass;
    }
    
    /**
     * This method allows sub-classes to return name of the
     * described class without the need to load the class itself.
     *
     * @return name of the described class e.g. (<code>getDescribedClass().getName()</code>).
     */
    public String getDescribedClassName() {
        return getDescribedClass().getName();
    }

    public Creator[] getCreators() {
        return (Creator[]) creators.toArray(new Creator[creators.size()]);
    }

    public Creator findBestCreator(FormProperty[] properties, int style) {
        if (creators == null)
            return null;

        Creator[] allCreators = getCreators();
        int[] evals = CreationFactory.evaluateCreators(
                        allCreators , properties, (style & CHANGED_ONLY) != 0);
        int best = CreationFactory.getBestCreator(
                     allCreators , properties, evals, (style & PLACE_ALL) != 0);
        return allCreators [best];
    }

    public Object createDefaultInstance() throws InstantiationException,
                                                 IllegalAccessException,
                                                 IllegalArgumentException,
                                                 InvocationTargetException,
                                                 NoSuchMethodException
    {
        return getDefaultCreator().createInstance(defaultParams);
    }

    private Creator getDefaultCreator() throws NoSuchMethodException {
        if( defaultCreator == null ) {
            defaultCreator = findDefaultCreator();
        } 
        return defaultCreator;
    }
    // ----------

    // finds first constructor that matches defaultConstrParams
    private Creator findDefaultCreator() throws NoSuchMethodException {        
        for (Iterator it = creators.iterator(); it.hasNext();) {
            
            Creator creator = (Creator) it.next();
            Class[] paramTypes = creator.getParameterTypes();
            
            if (paramTypes.length == defaultParams.length) {
                int ii;
                for (ii=0; ii < paramTypes.length; ii++) {
                    Class cls = paramTypes[ii];
                    Object param = defaultParams[ii];

                    if (cls.isPrimitive()) {
                        if (param == null
                            || (param instanceof Integer && cls != Integer.TYPE)
                            || (param instanceof Boolean && cls != Boolean.TYPE)
                            || (param instanceof Double && cls != Double.TYPE)
                            || (param instanceof Long && cls != Long.TYPE)
                            || (param instanceof Float && cls != Float.TYPE)
                            || (param instanceof Short && cls != Short.TYPE)
                            || (param instanceof Byte && cls != Byte.TYPE)
                            || (param instanceof Character && cls != Character.TYPE))
                        break;
                    }
                    else if (param != null && !cls.isInstance(param))
                        break;
                }
                if (ii == paramTypes.length) {
                    return creator;                    
                }
            }
        }
        throw new NoSuchMethodException();
    }

    // ----------

    static class ConstructorCreator implements Creator {
        private Class theClass;
        private Constructor constructor;
//        private Class[] constructorParamTypes;
        private String[] constructorPropNames;

        ConstructorCreator(Class cls, Class[] paramTypes, String[] propNames)
            throws NoSuchMethodException
        {
            if (paramTypes == null)
                paramTypes = emptyTypes;
            if (propNames == null)
                propNames = emptyNames;
            if (paramTypes.length != propNames.length)
                throw new IllegalArgumentException();

            constructor = cls.getConstructor(paramTypes);
            theClass = cls;
//            constructorParamTypes = paramTypes;
            constructorPropNames = propNames;
        }

        public final int getParameterCount() {
            return constructorPropNames.length; //constructorParamTypes.length;
        }

        public final Class[] getParameterTypes() {
            return constructor.getParameterTypes(); //constructorParamTypes;
        }

        public final Class[] getExceptionTypes() {
            return constructor.getExceptionTypes();
        }

        public final String[] getPropertyNames() {
            return constructorPropNames;
        }

        public Object createInstance(FormProperty[] props)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException
        {
            Object[] paramValues = new Object[constructorPropNames.length];

            for (int i=0; i < constructorPropNames.length; i++) {
                FormProperty prop = CreationFactory.findProperty(
                                        constructorPropNames[i], props);
                if (prop == null)
                    return null; // should not happen

                try {
                    paramValues[i] = prop.getRealValue();
                } catch (Exception ex) { // unlikely to happen
                    InstantiationException iex = new InstantiationException();
                    iex.initCause(ex);
                    throw(iex);
                }
            }

            return constructor.newInstance(paramValues);
        }

        public Object createInstance(Object[] paramValues)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException
        {           
            return constructor.newInstance(paramValues);
        }        
        
        public String getJavaCreationCode(FormProperty[] props, Class expressionType, String genericTypes) {
            StringBuffer buf = new StringBuffer();
            buf.append("new "); // NOI18N
            buf.append(theClass.getCanonicalName());
            if (genericTypes != null) {
                buf.append(genericTypes);
            }
            buf.append("("); // NOI18N

            for (int i=0; i < constructorPropNames.length; i++) {
                FormProperty prop = CreationFactory.findProperty(
                                        constructorPropNames[i], props);
                if (prop == null)
                    return null; // should not happen

                buf.append(prop.getJavaInitializationString());
                if (i+1 < constructorPropNames.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(")"); // NOI18N
            return buf.toString();
        }

        public CodeExpressionOrigin getCodeOrigin(CodeExpression[] params) {
            return CodeStructure.createOrigin(constructor, params);
        }
    }
    
    static class MethodCreator implements Creator {
        private Class factoryClass;
        private Class describedClass;
        private Method method;
        private CreationFactory.Property2ParametersMapper[] properties;
        private String[] propertyNames;
        
        MethodCreator(Class factoryClass, Class describedClass, String methodName, CreationFactory.Property2ParametersMapper[] properties)
            throws NoSuchMethodException
        {            
                        
            ArrayList paramTypesList = new ArrayList();
            propertyNames = new String[properties.length];    
            
            for (int i = 0; i < properties.length; i++) {                                
                for (int j = 0; j < properties[i].getPropertyTypes().length; j++) {
                    paramTypesList.add(properties[i].getPropertyTypes()[j]);                                        
                }                                
                propertyNames[i] = properties[i].getPropertyName();
            }                       
                                    
            Class[] paramTypes = (Class[]) paramTypesList.toArray(new Class[paramTypesList.size()]);

            method = factoryClass.getMethod(methodName, paramTypes);  
                
            this.factoryClass = factoryClass;                   
            this.describedClass = describedClass;
            this.properties = properties; 
            
        }
    
            
        public final int getParameterCount() {
            return propertyNames.length; 
        }

        public final Class[] getParameterTypes() {
            return method.getParameterTypes(); 
        }

        public final Class[] getExceptionTypes() {
            return method.getExceptionTypes();
        }

        public final String[] getPropertyNames() {
            return propertyNames;
        }

        public Object createInstance(FormProperty[] props)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException
        {
                                
            ArrayList paramValuesList = new ArrayList(); 
            for (int i=0; i < properties.length; i++) {
                FormProperty prop = CreationFactory.findProperty(properties[i].getPropertyName(), props);
                if (prop == null)
                    return null; // should not happen

                Object[] propertyParameters = properties[i].getPropertyParametersValues(prop);
                for (int j = 0; j < propertyParameters.length; j++) {
                    paramValuesList.add(propertyParameters[j]);
                }                                        
            }
            
            Object[] paramValues = paramValuesList.toArray(new Object[paramValuesList.size()]);
            
            Object ret = method.invoke(null, paramValues);
            if(ret.getClass() != describedClass) {                
                throw new IllegalArgumentException();
            }
            return ret;
        }

        public Object createInstance(Object[] paramValues)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException
        {                                            
            
            Object ret = method.invoke(null, paramValues);
            if(ret.getClass() != describedClass) {                
                throw new IllegalArgumentException();
            }
            return ret;
        }
        
        public String getJavaCreationCode(FormProperty[] props, Class expressionType, String genericTypes) {
            StringBuffer buf = new StringBuffer();
            if (expressionType == null) expressionType = describedClass;
            if (!expressionType.isAssignableFrom(method.getReturnType())) { // Issue 71220
                buf.append('(').append(expressionType.getName()).append(')');
            }
            buf.append(factoryClass.getName()); // NOI18N
            buf.append("."); // NOI18N
            buf.append(method.getName());
            buf.append("("); // NOI18N


            for (int i=0; i < properties.length; i++) {
                
                String name = properties[i].getPropertyName();                
                FormProperty prop = CreationFactory.findProperty(name, props);
                
                if (prop == null)
                    return null; // should not happen
                        
                    buf.append(properties[i].getJavaParametersString(prop));                     

                if (i+1 < properties.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(")"); // NOI18N
            return buf.toString();
        }

        public CodeExpressionOrigin getCodeOrigin(CodeExpression[] params) {
            // nobody cares ...
            return null; 
        }
    }    

}
