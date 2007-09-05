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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.UMLSupport;

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
        String[] collectionTypes, 
        boolean useGenerics, 
        IMultiplicity mult,
	boolean fullyQualified,
	ClassInfo container)
    {
	ClassInfo ci = ClassInfo.getRefClassInfo(classType, true, true);
	String  classTypeName = ci.getCodeGenType(fullyQualified, container);

//        if (mult != null && mult.getRangeCount() > 0)
        if (mult != null && isMultiDim(mult))
        {
	    return assembleMultiDimDataType(classTypeName, 
					    collectionTypes, 
					    useGenerics, 
					    mult.getRangeCount(),
					    fullyQualified);
	}        
	return classTypeName;

    }
    

    

    
    public static String assembleMultiDimDataType(
        String coreTypeName, 
        String[] collectionTypes, 
        boolean useGenerics, 
        long dimCount,
	boolean fullyQualified)
    {
	boolean isPrimitive = true;

	isPrimitive = JavaClassUtils.isPrimitive(coreTypeName);

        if (dimCount == 0)	    
	    return coreTypeName;
	
	if (isPrimitive 
	    && collectionTypes[(int)dimCount - 1] != null
	    && ! collectionTypes[(int)dimCount - 1].equals(IMultiplicityRange.AS_ARRAY))
	{
	    String type = JavaClassUtils.getPrimitiveWrapperType(coreTypeName);
	    if (fullyQualified) 
		coreTypeName = "java.lang." + type;
	    else
		coreTypeName = type;

	    isPrimitive = false;
	}	    
	
	String leftPart = "";
	String rightPart = "";

	for (int i = 0; i < dimCount; i++)
        {
	    String colType = collectionTypes[i];
	    if (((colType != null) && ( ! colType.trim().equals(IMultiplicityRange.AS_ARRAY))) 
		&& ((i != dimCount - 1) || ( ! isPrimitive)))
	    {
		leftPart += colType;
		if (! useGenerics) {
		    return leftPart + rightPart;
		} else {
		    leftPart += '<';
		    rightPart = '>' + rightPart;
		}
	    } else {
		rightPart = "[]" + rightPart;
	    }
	}
	return leftPart + coreTypeName + rightPart;
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
        
	boolean result = upperStr.equals(ASTERIK) || lowerStr.equals(ASTERIK);
	if (! result) 
	{
	    try 
	    {		
		result = Long.valueOf(upperStr).intValue() > 1;
	    } 
	    catch (NumberFormatException nfe) 
	    {
		// do nothing as it is just happens to be not a number 
	    }
	}
	return result;
    }

    public static String removeGenericType(String type)
    {
        return type.indexOf("<") == -1 
            ? type : type.substring(0, type.indexOf('<'));
    }


    //
    // added for template codegen
    //

    public static String[] getFullyQualifiedCodeGenType(IClassifier classType)
    {
	ClassInfo ci = ClassInfo.getRefClassInfo(classType, true, true);
	String[] packAndName = ci.getFullyQualifiedCodeGenType();
	return packAndName;
    }
    
    public static String getTypeName(IClassifier clazz, boolean fullyQualified) 
    {
	String fullClassName = "";
	String[] packAndName = getFullyQualifiedCodeGenType(clazz);
	if (packAndName != null && packAndName.length == 2) {
	    if (fullyQualified) 
		fullClassName = packAndName[0] + "." + packAndName[1];
	    else 
		fullClassName = packAndName[1];	
	}              
	return fullClassName;

	/*
        String name = JavaClassUtils.getFullyQualifiedName(clazz);        
        name = JavaClassUtils.getFullInnerClassName(name);
	if (! fullyQualified) 
	{
	    name = JavaClassUtils.getShortClassName(name);
	}
	return name;
	*/
    }

    public static String getTypeCodeGenType(IClassifier classType, boolean fullyQualified, ClassInfo container) 
    {
	IClassifier clazz = null;
	String result = "";
	if (classType instanceof IDerivationClassifier) 
	{
	    IDerivation drv = classType.getDerivation();
	    if (drv != null) 
	    {
		clazz = drv.getTemplate();		
		List<IUMLBinding> bindings =  drv.getBindings();
		if (bindings != null) 
		{
		    boolean first = true;
		    for (IUMLBinding b : bindings) 
		    {
			if (b.getActual() instanceof IClassifier) 
			{
			    String name = getTypeCodeGenType((IClassifier)b.getActual(), fullyQualified, container);
			    if (name != null && ! name.trim().equals(""))
			    {
				if (JavaClassUtils.isPrimitive(name)) {
				    name = JavaClassUtils.getPrimitiveWrapperType(name);
				}
				if (! first)
				{
				    result = result + ", ";
				} 
				else 
				{
				    first = false;
				}
				result = result + name.trim();
			    }
			}
		    }
		    if (result != null && !result.trim().equals(""))
		    {
			result = "<" + result + ">";			
		    } 
		}	
	    }
	    else 
	    {
		clazz = classType;
	    }
	}  
	else 
	{
	    clazz = classType;
	}   

	String clazzTypeName;
	if (fullyQualified || isNameCodeGenConflict(container, clazz)) 
	{
	    clazzTypeName = getTypeName(clazz, true);
	} 
	else 
	{
	    clazzTypeName = getTypeName(clazz, false);
	}
	result = clazzTypeName + result;	
	return result;
    }


    // see getCodeGenType()/assembleMultiDimDataType() 
    // for how the type string is formed 
    public static ArrayList<String[]> getReferredCodeGenTypes(
        IClassifier classType, 
        String[] collectionTypes, 
        boolean useGenerics, 
        IMultiplicity mult, 
	ClassInfo container)
    {
	ArrayList<String[]> res = new ArrayList<String[]>();
	boolean isPrimitive = false;

	ArrayList<String[]> refs;
	if (classType instanceof IDerivationClassifier) 
	{
	    refs = getReferredCodeGenTypes(classType, container);
	} 
	else 
        {
	    IClassifier impClass = classType;
	    IElement owner = classType.getOwner();
            while ( (owner != null) && (owner instanceof IClassifier)) 
	    {
		impClass = (IClassifier)owner;
		owner = impClass.getOwner();
            }
	    String[] fqType = GenCodeUtil.getFullyQualifiedCodeGenType(impClass);
	    if ( ! ( fqType != null && fqType.length == 2 && fqType[1] != null) ) {	
		return null;
	    }
	    
	    refs = new ArrayList<String[]>();
	    if (! isNameCodeGenConflict(container, classType)) 
	    {
		refs.add(fqType);
	    }
	    isPrimitive = JavaClassUtils.isPrimitive(fqType[1]);
	}

	boolean reffersTheType = true;
        if (mult != null && isMultiDim(mult))
        {
	    int dimCount = (int)mult.getRangeCount();
	    if (isPrimitive 
		&& collectionTypes[(int)dimCount - 1] != null
		&& ! collectionTypes[(int)dimCount - 1].equals(IMultiplicityRange.AS_ARRAY))
	    {		
		isPrimitive = false;
	    }	    
	    for (int i = 0; i < dimCount; i++)
	    {
		String colType = collectionTypes[i];
		if (((colType != null) && ( ! colType.trim().equals(IMultiplicityRange.AS_ARRAY))) 
		    && ((i != dimCount - 1) || ( ! isPrimitive)))
		{
		    res.add(new String[]{JavaClassUtils.getPackageName(colType), 
					 JavaClassUtils.getShortClassName(colType)});		    
		    if (! useGenerics) {
			reffersTheType = false;
			break;
		    } 
		}
	    }
	}
	
	if (reffersTheType) {
	    if (refs != null) {
		res.addAll(refs);	
	    }    
	}

	return res;
    }


    public static ArrayList<String[]> getReferredCodeGenTypes(IClassifier classType, ClassInfo container)
    {
	ArrayList<String[]> res = new ArrayList<String[]>();

	IClassifier clazz = null;

	if (classType instanceof IDerivationClassifier) {
	    IDerivation drv = classType.getDerivation();
	    if (drv != null) {
		clazz = drv.getTemplate();
		List<IUMLBinding> bindings =  drv.getBindings();
		if (bindings != null) {
		    for (IUMLBinding b : bindings) {
			if (b.getActual() instanceof IClassifier) {
			    ArrayList<String[]> refs 
				= getReferredCodeGenTypes((IClassifier)b.getActual(), container);
			    if (refs != null) {
				res.addAll(refs);
			    }
			}
		    }
		}
	    } else {
		// it is something like orphaned pack.clazz<type argument>, 
		// ie. there isn't derivation connecting it to pack.clazz;
		// will try to extract "pack.clazz", though without the bindings 
		// there isn't enough info for "type argument" 
		String[] fqType = GenCodeUtil.getFullyQualifiedCodeGenType(classType);
		if (( fqType != null && fqType.length == 2) ) {	
		    String name = fqType[1];
		    if (name != null) {
			int ind = name.indexOf('<');
			if (ind > 1) {
			    name = name.substring(0, ind);
			}
		    }
		    res.add(new String[] {fqType[0], name});
		}
	    }
	} else {
	    clazz = classType;
	}
	
	if (clazz != null && ! isNameCodeGenConflict(container, clazz)) {
	    String[] fqType = GenCodeUtil.getFullyQualifiedCodeGenType(clazz);
	    if (( fqType != null && fqType.length == 2) ) {	
		res.add(fqType);	
	    }
	}

	return res;
    }



    // utility method merges 2 ArrayLists 
    // of String[2] with package and name of a class
    public static void mergeReferredCodeGenTypes(ArrayList<String[]> res, 
					  HashSet<String> fqNames, 
					  ArrayList<String[]>refs) 
    {	
	if (refs == null) {
	    return;
	}
	Iterator iter = refs.iterator();	
	while(iter.hasNext()) {
	    String[] pn = (String[]) iter.next();
	    if (pn != null && pn.length == 2) {
		if (pn[0] != null &&  pn[1] != null) {
		    String fq = pn[1]+"."+pn[0];
		    if ( ! fqNames.contains(fq) ) {
			fqNames.add(fq);
			res.add(pn);
		    }
		}
	    }
	}	       
    }


    public static String[] getCollectionOverrideDataTypes(IMultiplicity multiplicity, boolean fullyQualified){
	
	if (multiplicity == null) {
	    return null;
	} 
	List<IMultiplicityRange> ranges = multiplicity.getRanges();
	if (ranges == null) {
	    return null;
	}

	String[] res = new String[(int)multiplicity.getRangeCount()];
	Iterator<IMultiplicityRange> iter = ranges.iterator();
	for(int i = 0 ; i < res.length; i++) {
	    String type = null;
	    if (iter.hasNext()) {	
		IMultiplicityRange range = iter.next();
		if (range != null) {
		    type = range.getCollectionTypeValue(true);
		    if (type == null || type.trim().equals("")) 
		    {
			type = IMultiplicityRange.AS_ARRAY;
		    }
		    if (! IMultiplicityRange.AS_ARRAY.equals(type)) {
			type = JavaClassUtils.convertUMLtoJava(type);
		    }
		}	        
	    }
	    // as there is always a value set at the attribute/parameter level
	    // whereis empty means "AsArray"
	    // then there isn't need for the global one
	    //if (type == null || type.trim().equals("") ) {
	    //	type = UMLSupport.getUMLSupport().getCollectionOverride();		
	    //}
	    if (! fullyQualified && ! IMultiplicityRange.AS_ARRAY.equals(type)) {
		type = JavaClassUtils.getShortClassName(type);
	    }
	    res[i] = type;
	}
	return res;

    }
    

    public static boolean isNameCodeGenConflict(ClassInfo container, IClassifier classType) 
    {
	if (container == null) 
	{
	    return false;
	}
	IClassifier cn = container.getClassElement();
	if (cn == null) 
	{
	    return false;
	} 
	if (JavaClassUtils.isAnOwner(cn, classType)) 
	{
	    return false;
	}		

	String typeName = getTypeName(classType, false);

	if (findConflictingByCodeGenName(container, typeName) != null) 
	{
	    return true;
	}
			
        ClassInfo owner = container.getOuterClass();
	if (owner != null) 
	{
	    return isNameCodeGenConflict(owner, classType);
	} 
	else 
	{
	    String cnName = getTypeName(cn, false);
	    return cnName.equals(typeName);
	}

    }
    
    public static ClassInfo findConflictingByCodeGenName(ClassInfo container, String typeName) 
    {
	if (container == null || typeName == null) 
	{
	    return null;
	}
	int ind = typeName.indexOf(".");
	String nameToComp;
	String restOfTypeName;
	if (ind > -1) 
	{
	    restOfTypeName = typeName.substring(ind + 1);
	    nameToComp = typeName.substring(0, ind);
	} 
	else 
	{
	    nameToComp = typeName;
	    restOfTypeName = "";
	}
	
	List<ClassInfo> children = container.getMemberTypes();
	if (children != null) 
	{
	    for(ClassInfo child : children)
	    {
		String chName = getTypeName(child.getClassElement(), false);
		int di = chName.lastIndexOf(".");
		if (di > -1)
		{
		    chName = chName.substring(di + 1);
		}
		if (nameToComp.equals(chName)) 
		{
		    if (!typeName.equals("")) 
		    {
			return child;
		    } 
		    else { 
			ClassInfo cl = findConflictingByCodeGenName(child, restOfTypeName);
			if (cl != null) 
			{
			    return cl;
			}
		    }
		} 
	    }
	}
	ClassInfo owner = container.getOuterClass();
	if (owner == null) 
	{
	    if (typeName.equals(getTypeName(container.getClassElement(), false))) 
	    {
		return owner;
	    }
	    return null;
	} 
	else 
	{
	    return findConflictingByCodeGenName(owner, typeName);
	}
    }


}
