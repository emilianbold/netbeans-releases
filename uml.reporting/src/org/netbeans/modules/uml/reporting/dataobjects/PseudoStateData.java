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

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 *
 * @author Sheryl
 */
public class PseudoStateData extends ElementDataObject
{
    private IPseudoState element;
    
    /** Creates a new instance of PseudoStateData */
    public PseudoStateData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IPseudoState)
            this.element = (IPseudoState)e;
    }
    
    public IPseudoState getElement()
    {
        return element;
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Container
        };
    }
    
    protected Object[] getPropertyValues()
    {
        return new Object[] {getElement().getAlias(), getVisibility(getElement()),
        getElement().getContainer()};
    }
    
}

