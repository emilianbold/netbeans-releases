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

/**
 * This class represents the data for each node in the TreeTable.
 *
 * @author  David Botterill
 */
public class TypeNodeData {
    public static final int IN = 0;
    public static final int OUT = 1;
    public static final int IN_OUT = 2;
    
    private String typeName;
    private Object typeValue;
    private String typeClass;
    private String genericType;
    private int holderType = IN;
    private boolean assignable = true;
    
    public TypeNodeData() {
        
    }
    
    public TypeNodeData(String inType, String inParameterName) {
        this(inType, null, inParameterName, null);
    }
    
    public TypeNodeData(String inType, Object parameterValue) {
        this(inType, null, null, parameterValue);
    }
    
    public TypeNodeData(String inType, String genericType, String inParameterName, Object parameterValue) {
        this.typeClass = inType;
        this.typeName = inParameterName;
        this.genericType = genericType;
        this.typeValue = parameterValue;
    }
    
    public void setTypeClass(String inType) {
        typeClass=inType;
    }

    public boolean isAssignable() {
        return assignable;
    }

    public void setAssignable(boolean assignable) {
        this.assignable = assignable;
    }
    
    public String getTypeClass() {
        return typeClass;
    }
    
    public String getGenericType() {
        return genericType;
    }
    
    public void setGenericType(String innerType) {
        this.genericType = innerType;
    }
    
    public void setTypeName(String inParameterName) {
        typeName=inParameterName;
    }
    
    public String getTypeName() {
        return typeName;
    }
    public void setTypeValue(Object inValue) {
        typeValue=inValue;
    }
    
    public Object getTypeValue() {
        return typeValue;
    }
    
    public String getRealTypeName() {
        if (ReflectionHelper.isArray(typeClass)) {
            return typeClass;
        }else if (genericType != null && genericType.length() > 0) {
            return typeClass + "<" + genericType + ">";
        }else {
            return typeClass;
        }
    }
    
    public int getHolderType() {
        return holderType;
    }
    
    public void setHolderType(int mode) {
        this.holderType = mode;
    }
}
