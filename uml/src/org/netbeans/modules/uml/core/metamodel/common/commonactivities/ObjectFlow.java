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

/*
 * File       : ObjectFlow.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;

/**
 * @author Aztec
 */
public class ObjectFlow extends ActivityEdge implements IObjectFlow
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#getEffect()
     */
    public int getEffect()
    {
        return getObjectFlowEffectKind("effect");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#getIsMulticast()
     */
    public boolean getIsMulticast()
    {
        return getBooleanAttributeValue("isMultiCast", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#getIsMultiReceive()
     */
    public boolean getIsMultiReceive()
    {
        return getBooleanAttributeValue("isMultiReceive", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#getSelection()
     */
    public IBehavior getSelection()
    {
        return new ElementCollector< IBehavior >()
            .retrieveSingleElementWithAttrID(this, "selection", IBehavior.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#getTransformation()
     */
    public IBehavior getTransformation()
    {
        return new ElementCollector< IBehavior >()
            .retrieveSingleElementWithAttrID(this, "transformation", IBehavior.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#setEffect(int)
     */
    public void setEffect(int nKind)
    {
        setObjectFlowEffectKind("effect", nKind);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#setIsMulticast(boolean)
     */
    public void setIsMulticast(boolean value)
    {
        setBooleanAttributeValue("isMultiCast", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#setIsMultiReceive(boolean)
     */
    public void setIsMultiReceive(boolean value)
    {
        setBooleanAttributeValue("isMultiReceive", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#setSelection(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
     */
    public void setSelection(IBehavior value)
    {
        setElement(value, "selection");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow#setTransformation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
     */
    public void setTransformation(IBehavior value)
    {
        setElement(value, "transformation");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:ObjectFlow", doc, node);
    }       

}
