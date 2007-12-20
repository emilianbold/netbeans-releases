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

import java.util.ArrayList;
import java.util.List;

/**
 * This is the special kind of argument which is intended to represent 
 * a group of arguments. The main use case is describing of repeated 
 * sequence of arguments. 
 * An example is the function "doXslTransform". It has 2 fixed arguments 
 * at the beginning and then a sequence of repeated group of name & value 
 * argumens.
 * 
 * @see org.netbeans.modules.xml.xpath.ext.metadata.XPathMetadataUtils#getArgDescriptorsList
 * @author nk160297
 */
public final class ArgumentGroup implements AbstractArgument {

    private List<AbstractArgument> mArguments;
    private int mMinOccurs;
    private int mMaxOccurs;
    private String mDescription;

    public ArgumentGroup(int minCount, int maxCount, String description, 
            AbstractArgument... arguments) {
        mMinOccurs = minCount;
        mMaxOccurs = maxCount;
        mDescription = description;
        //
        mArguments = new ArrayList<AbstractArgument>(); 
        for (AbstractArgument argument : arguments) {
            mArguments.add(argument);
        }
    }
    
    /**
     * @return the list of arguments in the group. 
     */
    public List<AbstractArgument> getArgumentList() {
        return mArguments;
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
    
}
