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


package org.netbeans.modules.uml.reporting.dataobjects;

import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IComplexActivityGroup;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class ComplexActivityGroupData extends ActivityGroupData
{
    private IComplexActivityGroup element;
    
    /** Creates a new instance of ComplexActivityGroupData */
    public ComplexActivityGroupData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IComplexActivityGroup)
            this.element = (IComplexActivityGroup)e;
    }
    
    public IComplexActivityGroup getElement()
    {
        return element;
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Kind,
            Property_GroupKind
        };
    }
    
    protected Object[] getPropertyValues()
    {
        String kind = NbBundle.getMessage(ComplexActivityGroupData.class,
                "ActivityKind"+getElement().getKind());
        String groupKind = NbBundle.getMessage(ComplexActivityGroupData.class,
                "ActivityGroupKind"+getElement().getGroupKind());
        
        return new Object[] {getElement().getAlias(), getVisibility(getElement()),
        kind, groupKind};
    }
}
