/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public final class GenCodeUtil
{
    private GenCodeUtil(){}

    
    public static boolean isValidClassType(String type)
    {
        // valid class type are anything but:
        // null, empty string, void, String, or a primitive
        return !(
            type == null ||
            type.length() == 0 ||
            type.equals("void") || // NOI18N
            type.equals("String") || // NOI18N
            type.equals("java.lang.String") || // NOI18N
            JavaClassUtils.isPrimitive(type));
    }
    
    public static String getCodeGenType(
        IClassifier classType, 
        String collectionType, 
        boolean useGenerics, 
        IMultiplicity mult)
    {
        // get complete package name - "com::foo::bar"
        IPackage owningPkg = classType.getOwningPackage();
        String fullPkgName = owningPkg.getFullyQualifiedName(false);

        // default package elements have the project as the owning package
        if (owningPkg instanceof IProject)
            fullPkgName = "";
        
        // get fully qualified name - "com::foo::bar::Outer::Middle::Inner"
        String qualName = classType.getFullyQualifiedName(false);
        String fullClassName = qualName;
        
        if (isValidClassType(fullClassName))
        {
            // extract the full class name - "Outer::Middle::Inner"
            // and convert to dot notation = "Outer.Middle.Inner"

            if (fullPkgName.length() > 0)
            {
                fullClassName = JavaClassUtils.convertUMLtoJava(
                    qualName.substring(fullPkgName.length()+2));
            }

            // it's in the default package
            else
                fullClassName = JavaClassUtils.convertUMLtoJava(qualName);
        }
        
//        if (fullClassName.indexOf('.') > 0)
//        {
//            // we have an inner class, so trim off all outer classes possible
//            // i.e. - A.B.C used by A can be reduced to B.C
//            // TODO
//        }
        
//        if (mult != null && mult.getRangeCount() > 0)
        if (mult != null && isMultiDim(mult))
        {
            if (!JavaClassUtils.isPrimitive(fullClassName) && 
                collectionType != null && collectionType.length() > 0)
            {
                // TODO: use parameter's Collection Override Data Type and
                // Use Generics property instead of the global preferences

                return assembleCollectionDataType(
                    fullClassName, collectionType, 
                    useGenerics, mult.getRangeCount());
            }
            
            else
                return assembleArrayDataType(
                    fullClassName, mult.getRangeCount());
        }
        
        else
            // return getReturnParameter().getType();
            return fullClassName;
    }
    
    
    public static String assembleCollectionDataType(
        String coreType, 
        String collectionType, 
        boolean useGenerics, 
        long dimCount)
    {
        if (dimCount == 0)
            return coreType;
        
        if (JavaClassUtils.isPrimitive(coreType))
            coreType = JavaClassUtils.getPrimitiveWrapperType(coreType);
        
        StringBuffer retType = new StringBuffer(collectionType);

        if (useGenerics)
        {
            String endAngles = ""; // NOI18N

            for (int i=0; i <= dimCount-1; i++)
            {
                endAngles += '>'; // NOI18N

                if (i < dimCount-1)
                    retType.append('<').append(collectionType);

                else if (i == dimCount-1)
                    retType.append('<').append(coreType);
            }

            return retType.append(endAngles).toString();
        }
        
        else
            return collectionType;
    }

    private static String assembleArrayDataType(
        String dataType, long dimCount)
    {
        for (int i=0; i < dimCount; i++)
        {
            dataType += "[]";
        }
        
        return dataType;
    }

    public final static String ASTERIK = "*";
    
    public static boolean isMultiDim(IMultiplicity mult)
    {
        if (mult == null || mult.getRanges().size() == 0)
            return false;
     
        // if more than one dimension, even if all are upper limit of 1,
        // we still want to use Collections
        else if (mult.getRanges().size() > 1)
            return true;

        String lowerStr = mult.getRanges().get(0).getLower();
        String upperStr = mult.getRanges().get(0).getUpper();
        
        return upperStr.equals(ASTERIK) || lowerStr.equals(ASTERIK) || 
                Long.valueOf(upperStr).intValue() > 1;
    }

    public static String removeGenericType(String type)
    {
        return type.indexOf("<") == -1 
            ? type : type.substring(0, type.indexOf('<'));
    }
    
}
