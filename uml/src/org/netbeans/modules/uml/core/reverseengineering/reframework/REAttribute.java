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
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REAttribute extends REClassFeature implements IREAttribute
{

    /**
     * Specifies an expression that is used to specify the initial value of the attribute.
     * @param pVal [out] The initial value.
     */
    public String getInitialValue()
    {
        return getTokenDescriptorValue("InitialValue");
    }

    /**
     * Specifies if the attribute is a primitive attribute or an object instance.
     *
     * @param *pVal [out] True if primitive, False otherwise.
     */
    public boolean getIsPrimitive()
    {
        return Boolean.valueOf(getTokenDescriptorValue("IsPrimitive"))
                      .booleanValue();
    }

    public ETList<IREMultiplicityRange> getMultiplicity()
    {
        REXMLCollection<IREMultiplicityRange> coll =
                new REXMLCollection<IREMultiplicityRange>(
                    REMultiplicityRange.class,
                    "UML:TypedElement.multiplicity/UML:Multiplicity/" +
                    "UML:Multiplicity.range/UML:MultiplicityRange");
        try
        {
            coll.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return coll;
    }
    
    public void setMultiplicity(ETList<IREMultiplicityRange> mul)
    {
        throw new UnsupportedOperationException("Can't set multiplicity");
    }
    
    public String getType()
    {
        return XMLManip.getAttributeValue(getEventData(), "type");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREAttribute#getIsVolatile()
     */
    public boolean getIsVolatile()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isVolatile");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREAttribute#getIsTransient()
     */
    public boolean getIsTransient()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isTransient");
    }
}