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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class REClassFeature extends REClass implements IREClassFeature
{

    /**
     * Retrieves the scope of the class element.  The scope of a feature
     * can be an instance or a classifier.
     * @return The scope.
     */
    public int getOwnerScope()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isStatic")?
					ScopeKind.SK_CLASSIFIER
                  : ScopeKind.SK_INSTANCE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClassFeature#getType()
     */
    public String getType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves whether the class feature is a constant.
     * @return true if the feature is constant, false otherwise.
     */
    public boolean getIsConstant()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isFinal");
    }
}
