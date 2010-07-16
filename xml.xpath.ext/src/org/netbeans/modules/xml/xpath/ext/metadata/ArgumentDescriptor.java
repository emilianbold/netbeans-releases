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

package org.netbeans.modules.xml.xpath.ext.metadata;

import java.util.Collections;
import java.util.List;

/**
 * Describes an argument (parameter) of a method or operation.
 * 
 * @author nk160297
 */
public final class ArgumentDescriptor implements AbstractArgument {

    private XPathType mType;
    private int mMinOccurs;
    private int mMaxOccurs;
    private String mDescription;
    private List<XPathType> mCanBeAssignedFrom;
    
    public static final List<XPathType> NOT_ASSIGNABLE = Collections.emptyList(); 
    
    public static final List<XPathType> NODE_SET_ASSIGNABLE = 
            Collections.singletonList(XPathType.NODE_SET_TYPE);
    
    public ArgumentDescriptor(XPathType type, 
            boolean isMandatory, boolean isRepeated, 
            List<XPathType> canBeAssignedFrom) {
        mType = type;
        mMinOccurs = isMandatory ? 1 : 0;
        mMaxOccurs = isRepeated ? Integer.MAX_VALUE : 1;
        mCanBeAssignedFrom = canBeAssignedFrom;
    }

    public ArgumentDescriptor(XPathType type, 
            int minCount, int maxCount, String description, 
            List<XPathType> canBeAssignedFrom) {
        mType = type;
        mMinOccurs = minCount;
        mMaxOccurs = maxCount;
        mDescription = description;
        mCanBeAssignedFrom = canBeAssignedFrom;
    }

    public XPathType getArgumentType() {
        return mType;
    }
    
    public String getDescription() {
        return mDescription;
    }

    public boolean isMandatory() {
        return mMinOccurs > 0;
    }

    public boolean isRepeated() {
        return mMaxOccurs > 1;
    }

    public int getMinOccurs() {
        return mMinOccurs;
    }
    
    public int getMaxOccurs() {
        return mMaxOccurs;
    }
    
    /**
     * Null value means any type.
     */ 
    public List<XPathType> canBeAssignedFromTypes() {
        return mCanBeAssignedFrom;
    }

    //==========================================================================
    
    /** 
     * Predefined instances of argument descriptor.
     * They are immutable so can be used like constants. 
     */
    public interface Predefined {
        List<XPathType> NOT_ASSIGNABLE = Collections.emptyList(); 
    
        List<XPathType> NODE_SET_ASSIGNABLE = 
                Collections.singletonList(XPathType.NODE_SET_TYPE);
        
        //---------------------------------------------------------
        
        ArgumentDescriptor ANY_TYPE =
            new ArgumentDescriptor(XPathType.ANY_TYPE, true, false, null);

        ArgumentDescriptor OPTIONAL_ANY_TYPE = 
            new ArgumentDescriptor(XPathType.ANY_TYPE, false, false, null);

        ArgumentDescriptor SIMPLE_BOOLEAN =
            new ArgumentDescriptor(XPathType.BOOLEAN_TYPE, true, false, null);

        ArgumentDescriptor SIMPLE_STRING = 
            new ArgumentDescriptor(XPathType.STRING_TYPE, true, false, null);

        ArgumentDescriptor OPTIONAL_STRING = 
            new ArgumentDescriptor(XPathType.STRING_TYPE, false, false, null);

        ArgumentDescriptor REPEATED_STRING_2MIN = 
            new ArgumentDescriptor(XPathType.STRING_TYPE, 2, Integer.MAX_VALUE, 
            null, null);

        ArgumentDescriptor REPEATED_NUMBER_2MIN = 
            new ArgumentDescriptor(XPathType.NUMBER_TYPE, 2, Integer.MAX_VALUE, 
            null, null);

        ArgumentDescriptor REPEATED_ANY_TYPE_0MIN = 
            new ArgumentDescriptor(XPathType.ANY_TYPE, 0, Integer.MAX_VALUE, 
            null, null);

        ArgumentDescriptor SIMPLE_NUMBER = 
            new ArgumentDescriptor(XPathType.NUMBER_TYPE, true, false, null);

        ArgumentDescriptor OPTIONAL_NUMBER = 
            new ArgumentDescriptor(XPathType.NUMBER_TYPE, false, false, null);

        ArgumentDescriptor SIMPLE_NODE = new ArgumentDescriptor(
                XPathType.NODE_TYPE, true, false, NODE_SET_ASSIGNABLE);

        ArgumentDescriptor OPTIONAL_NODE = new ArgumentDescriptor(
                XPathType.NODE_TYPE, false, false, NODE_SET_ASSIGNABLE);

        ArgumentDescriptor SIMPLE_NODE_SET = new ArgumentDescriptor(
                XPathType.NODE_SET_TYPE, true, false, NOT_ASSIGNABLE);

        ArgumentDescriptor REPEATED_NODE_SET_2MIN = new ArgumentDescriptor(
                XPathType.NODE_SET_TYPE, 2, Integer.MAX_VALUE, 
                null, NOT_ASSIGNABLE);

        ArgumentDescriptor SIMPLE_DATE_TIME_STRING = 
            new ArgumentDescriptor(XPathType.DATE_TIME_TYPE, true, false, null);
    }
    
}
