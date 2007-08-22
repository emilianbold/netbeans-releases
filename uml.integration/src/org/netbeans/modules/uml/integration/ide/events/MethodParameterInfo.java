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

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbPreferences;

/**
 * The MethodParameterInfo is used to store the parameter information used
 * to define the parameters for constructors and methods.
 *
 * @see ConstructorInfo
 */
public class MethodParameterInfo implements Cloneable 
{
    private String mOrigType = null;
    private String mNewType = null;
    private String mName = null;
    private String fullyQualifiedType = null;
    private IParameter parameterElement = null;
    private MethodInfo parentMethodInfo = null;
    
    /** 
     * The number of multiplicty ranges that are associated with the parameter. 
     * In Java parameters can not define a range.  So, all we need to know is
     * how many ranges there are.
     */
    private int numberOfDimensions = 0;
    
    /**
     * Constructs a MethodParameterInfo.
     */
    public MethodParameterInfo() 
    {
        this(null, null);
    }

    /**
     * Constructs a MethodParameterInfo and sets the type of the parameter.
     */
    public MethodParameterInfo(String type) 
    {
        this(type, null);
    }

    /**
         * Constructs a MethodParameterInfo and sets the type and name of the parameter.
     */
    public MethodParameterInfo(String type, String name) 
    {
        setType(type);
        setName(name);
    }

    public MethodParameterInfo(
        MethodInfo methInfo, IParameter param, boolean collecOverride) 
    {
        setParentMethodInfo(methInfo);
        setParameterElement(param);
        setType(getType(param, collecOverride));
        String paramName = param.getName();
        if (paramName != null)
            setName(paramName.trim());

        String type = getType();
        if (type == null || type.trim().length() == 0)
            setType("int");
        
        // conover
        // have to check for null parameter type because there is a bug
        // in the rev eng code that doesn't parse some types properly
        // leaving them null in the model. Just need to guard agains this
        if (param.getType() == null)
            setFullyQualifiedType("error-type_was_null"); // NOI18N
        
        else
            setFullyQualifiedType(param.getType().getFullyQualifiedName(false));
        
        IMultiplicity mult = param.getMultiplicity();
        if (mult != null)
        {
            ETList<IMultiplicityRange> ranges = mult.getRanges();
            if (ranges != null)
            {
                numberOfDimensions = ranges.size();
            }
        }
    }

    /**
     * Sets the original type of the parameter.
     */
    public void setType(String type) {
        mOrigType = type;
    }

    /**
     * Retrieves the original type of the parameter.
     */
    public String getType() {
        return mOrigType;
    }

    /**
     * Sets the new type of the parameter.
     */
    public void setNewType(String type) {
        mNewType = type;
    }

    /**
     * Retrieves the new type of the parameter.
     */
    public String getNewType() {
        return mNewType;
    }

    /**
     * Sets the name of the parameter.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Gets the name of the parameter.
     */
    public String getName() {
        return mName;
    }

    public void update() {
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException ignored) {}
        return null;
    }

    private boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    public boolean equals(Object other) {
        if (other instanceof MethodParameterInfo) {
            MethodParameterInfo ot = (MethodParameterInfo) other;
            return equals(getType(), ot.getType()) &&
                equals(getName(), ot.getName());
        }
        return false;
    }

    public static String getType(IParameter p) {
        return getType(p, false);
    }

    public static boolean isArray(String type) {
        return type != null && type.indexOf('[') != -1;
    }

    public static String getType(IParameter param, boolean collectOverride) 
    {
        if (param != null)
        {
            IBehavioralFeature feature = param.getBehavioralFeature();            
            String typeName = null;
            
            if(param.getType() != null)
                typeName = param.getType().getName();

            if (feature != null)
            {
                IClassifier clazz = feature.getFeaturingClassifier();

                typeName = JavaClassUtils.replaceDollarSign(
                    JavaClassUtils.getFullyQualifiedName(param.getType()));
            }

            int mul = MemberInfo.getMultiplicity(param.getMultiplicity());
            
            if (mul > 0 && collectOverride) 
            {
                String override = 
                    UMLSupport.getUMLSupport().getCollectionOverride();
                
                if (override != null)
                    return override;
            }
            
            typeName = MemberInfo.getArrayType(typeName, mul);
            return typeName != null ? typeName.trim() : typeName;
        }
        
        return "";
    }

    public String getFullyQualifiedType() {
        return fullyQualifiedType;
    }

    public void setFullyQualifiedType(String fullyQualifiedType) {
        this.fullyQualifiedType = fullyQualifiedType;
    }
    
    
    
    public String getCodeGenType()
    {
	return getCodeGenType(false);
    }

    public String getCodeGenType(boolean fullyQualified)
    {
        // if no return param, probably a Constructor; return empty string
        if (getParameterElement() == null)
            return ""; // NOI18N
        
        // if this is the parameter for "the" main method, then don't override
        // the String[] type with a Collection type.
        //      public static void main(String[] args)
        if (getParentMethodInfo().getName().equals("main") && // NOI18N
            getParentMethodInfo().getParameters().length == 1 &&
            (getParentMethodInfo().getModifiers().intValue() & Modifier.PUBLIC) > 0 &&
            (getParentMethodInfo().getModifiers().intValue() & Modifier.STATIC) > 0 &&
            getType().equals("String[]") && // NOI18N
            getParentMethodInfo().getReturnParameter().getType().equals("void")) // NOI18N
        {
            return getType();
        }
        
	if (fullyQualified) 
	{
	    if (codeGenTypeFullyQualified == null) 
	    { 
		codeGenTypeFullyQualified 
		    = GenCodeUtil.getCodeGenType
		    (getParameterElement().getType(), 
		     GenCodeUtil.getCollectionOverrideDataTypes
		         (getParameterElement().getMultiplicity(), fullyQualified),
		     isUseGenerics(),
		     getParameterElement().getMultiplicity(),
		     fullyQualified,
		     getParentMethodInfo().getContainingClass());
	    }
	    return codeGenTypeFullyQualified;
	}
	else 
	{
	    if (codeGenTypeShort == null) 
	    { 
		codeGenTypeShort 
		    = GenCodeUtil.getCodeGenType
		    (getParameterElement().getType(), 
		     GenCodeUtil.getCollectionOverrideDataTypes
		         (getParameterElement().getMultiplicity(), fullyQualified),
		     isUseGenerics(),
		     getParameterElement().getMultiplicity(),
		     fullyQualified,
		     getParentMethodInfo().getContainingClass());
	    }
	    return codeGenTypeShort;
	}
    }
    
    private String codeGenTypeFullyQualified = null;
    private String codeGenTypeShort = null;
    
    public int getNumberOfDimensions()
    {
        return numberOfDimensions;
    }
    
    /*
    public String getCollectionOverrideDataType() 
    {
        // TODO: when Collection Override Data Type property is added at the 
        // Parameter element level, this will no longer use the global default
        
        return UMLSupport.getUMLSupport().getCollectionOverride();
    }
    

    public boolean isCollectionType() 
    {
        return Util.isValidCollectionDataType(getFullyQualifiedType());
    }
    
    
    public boolean isUseCollectionOverride()
    {
        // TODO: conover - change this to use attribute level property
        // rather than the global preference
        return getParameterElement().getMultiplicity().getRangeCount() > 0 &&
            Util.isValidCollectionDataType(getCollectionOverrideDataType());
    }
    */

    public boolean isUseGenerics()
    {
        // TODO: conover - eventually, use the atribute level property
        // instead of this global preference
        //kris richards - made change to nbpreferences
        return NbPreferences.forModule(DummyCorePreference.class).getBoolean("UML_USE_GENERICS_DEFAULT", true); // NOI18N
    }
    
    public IParameter getParameterElement()
    {
        return parameterElement;
    }

    public void setParameterElement(IParameter parameterElement)
    {
        this.parameterElement = parameterElement;
    }

    public MethodInfo getParentMethodInfo()
    {
        return parentMethodInfo;
    }

    public void setParentMethodInfo(MethodInfo parentMethodInfo)
    {
        this.parentMethodInfo = parentMethodInfo;
    }



    //
    // added for template codegen
    //

    // see getCodeGenType() for how the type string is formed 
    public ArrayList<String[]> getReferredCodeGenTypes()
    {
	return GenCodeUtil
	    .getReferredCodeGenTypes(getParameterElement().getType(), 
				     GenCodeUtil.getCollectionOverrideDataTypes
				         (getParameterElement().getMultiplicity(), true),
				     isUseGenerics(),
				     getParameterElement().getMultiplicity(),
				     getParentMethodInfo().getContainingClass());
    }


}

