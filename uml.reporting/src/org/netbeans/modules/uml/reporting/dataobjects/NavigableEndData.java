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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class NavigableEndData extends AssociationEndData
{
    private INavigableEnd element;
    
    /** Creates a new instance of NavigableEndData */
    public NavigableEndData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof INavigableEnd)
            this.element = (INavigableEnd)e;
    }
    
    public INavigableEnd getElement()
    {
        return element;
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Static,
            Property_Transient,
            Property_Volatile,
            Property_Type,
            Property_Client_Changeability,
            Property_Multiplicity,
            Property_Default,
            Property_Primary_Key,
            Property_Redefined,
            Property_Participant,
            Property_Navigable
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isStatic = new Boolean(getElement().getIsStatic());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isVolatile = new Boolean(getElement().getIsVolatile());
        Boolean isPrimaryKey = new Boolean(getElement().getIsPrimaryKey());
        Boolean isRedefined = new Boolean(getElement().getIsRedefined());
        Boolean isNavigable = new Boolean(getElement().getIsNavigable());
        
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal, isStatic, isTransient, isVolatile,
        getElement().getType(), NbBundle.getMessage(AssociationEndData.class,
                "ClientChangeability" + getElement().getClientChangeability()),
        getElement().getMultiplicity().getRangeAsString(true),
        getElement().getDefault().getBody(), isPrimaryKey, isRedefined,
        getElement().getParticipant(), isNavigable};
    }
}
