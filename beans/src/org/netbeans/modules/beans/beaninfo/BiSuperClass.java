/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

import org.netbeans.modules.beans.JMIUtils;
import org.netbeans.modules.beans.GenerateBeanException;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.javacore.jmiimpl.javamodel.MetadataElement;
import org.netbeans.jmi.javamodel.*;

import javax.jmi.reflect.JmiException;

/** Singleton - utility class


 @author Petr Hrebejk
*/
final class BiSuperClass extends Object {


    /** Creates a ClassElement containing all methods from classElement and it's superclasses */

    static JavaClass createForClassElement( JavaClass classElement ) throws GenerateBeanException {
        assert JMIUtils.isInsideTrans();
        try {
            JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(classElement);
            JavaClass result = jmodel.getJavaClass().createJavaClass();

            result.setName( classElement.getName() );

            JavaClass ce = classElement;
            int methodsAdded = 0;           // Workaround for getMethd

            HashSet visited = new HashSet();
            while ( ce != null && visited.add(ce) && !"java.lang.Object".equals(ce.getName())) { // NOI18N
                List/*<Method>*/ methods = JMIUtils.getMethods(ce);
                for (Iterator it = methods.iterator(); it.hasNext();) {
                    Method method = (Method) it.next();
                    if ( ( method.getModifiers() & Modifier.PUBLIC ) == 0 )
                        continue;

                    if ( methodsAdded == 0 || result.getMethod( method.getName(), getParameterTypes(method), false ) == null ) {
                        Method methodToAdd = duplicateMethod(method, jmodel);
                        result.getFeatures().add(methodToAdd);
                        methodsAdded ++;
                    }
                }
            
                ce = ce.getSuperClass();
            }

            /*
            MethodElement[] methods = result.getMethods();
            for( int i = 0; i < methods.length; i++ ) 
              System.out.println ( methods[i].getName() );
            */
            return result;
        } catch (JmiException e) {
            throw new GenerateBeanException(e); 
        }
    }
    
    /**
     * makes shallow copy of method in particular extent.
     * @param orig original method
     * @param jmodel extent where the copy is created
     * @return new method
     * @throws JmiException
     */ 
    private static Method duplicateMethod(Method orig, JavaModelPackage jmodel) throws JmiException {
        Method m = jmodel.getMethod().createMethod(
                orig.getName(),
                null,
                orig.getModifiers(),
                null,
                null,
                null,
                null,
                duplicateTypeParameters(orig, jmodel),
                duplicateParameters(orig, jmodel),
                duplicateExceptionNames(orig, jmodel),
                null,
                orig.getDimCount()
                );
        m.setType(orig.getType());
        return m;
    }

    private static List duplicateTypeParameters(Method orig, JavaModelPackage jmodel) {
        List tparams = orig.getTypeParameters();
        if (tparams == null) {
            return null;
        }
        
        List duplicates = new ArrayList(tparams.size());
        for (Iterator it = tparams.iterator(); it.hasNext();) {
            TypeParameter typeParameter = (TypeParameter) it.next();
            TypeParameter duplicate = duplicateTypeParameter(typeParameter, jmodel);
            duplicates.add(duplicate);
        }
        return duplicates;
    }

    private static TypeParameter duplicateTypeParameter(TypeParameter typeParameter, JavaModelPackage jmodel) {
        TypeParameter duplicate = jmodel.getTypeParameter().createTypeParameter();
        duplicate.setName(typeParameter.getName());
        duplicate.setSuperClass(typeParameter.getSuperClass());
        List/*<JavaClass>*/ bounds = typeParameter.getInterfaces();
        List/*<JavaClass>*/ copyOfBounds = new ArrayList(bounds.size());
        for (Iterator it = bounds.iterator(); it.hasNext();) {
            Object bound = it.next();
            if (bound instanceof TypeParameter) {
                copyOfBounds.add(duplicateTypeParameter((TypeParameter) bound, jmodel));
            } else {
                copyOfBounds.add(bound);
            }
        }
        duplicate.getInterfaces().addAll(copyOfBounds);
        return duplicate;
    }

    /**
     * makes shallow copy of parameters in particular extent.
     * @param orig method with original list of parameters
     * @param jmodel extent where the copy is created
     * @return list of parameters
     * @throws JmiException
     */ 
    private static List duplicateParameters(Method orig, JavaModelPackage jmodel) throws JmiException {
        List parameters = orig.getParameters();
        if (parameters == null) {
            return null;
        }
        List duplicates = new ArrayList(parameters.size());
        for (Iterator it = parameters.iterator(); it.hasNext();) {
            Parameter parameter = (Parameter) it.next();
            Parameter duplicate = jmodel.getParameter().createParameter(
                    parameter.getName(),
                    null,
                    parameter.isFinal(),
                    null,
                    parameter.getDimCount(), 
                    parameter.isVarArg()
            );
            duplicate.setType(parameter.getType());
            duplicates.add(duplicate);
        }
        return duplicates;
    }
    
    private static List/*<Type>*/ getParameterTypes(Method method) throws JmiException {
        List/*<Parameter>*/ params = method.getParameters();
        if (params == null) {
            return Collections.EMPTY_LIST;
        }
        List types = new ArrayList(params.size());
        for (Iterator it = params.iterator(); it.hasNext();) {
            Parameter param = (Parameter) it.next();
            types.add(param.getType());
        }
        return types;
    }

    private static List/*<MultipartId>*/ duplicateExceptionNames(Method orig, JavaModelPackage jmodel) {
        List/*<JavaClass>*/ exIds = orig.getExceptions();
        
        List/*<MultipartId>*/ duplicates = new ArrayList/*<MultipartId>*/(exIds.size());
        for (Iterator it = exIds.iterator(); it.hasNext(); ) {
            JavaClass ex = (JavaClass) it.next();
            MultipartId duplicate = jmodel.getMultipartId().createMultipartId(ex.getName(), null, null);
            duplicates.add(duplicate);
        }
        return duplicates;
    }

}
