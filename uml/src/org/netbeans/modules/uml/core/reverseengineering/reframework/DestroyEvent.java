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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class DestroyEvent extends MethodDetailParserData
        implements IDestroyEvent
{
    /**
     * Is the type of the instance a primitive or an object.
     *
     * @param *pVal [out] True if the type is a primitive data type.
     */
    public boolean getIsPrimitive()
    {
        return Boolean.valueOf(getTokenDescriptorValue("IsPrimitive"))
                    .booleanValue();
    }

    /**
     * The type of the instance that is being destroyed.
     *
     * @param *pVal [out] The type of the instance.
     */
    public String getInstanceTypeName()
    {
        return XMLManip.getAttributeValue(getEventData(), "classifier");
    }

    /**
     * The name of the instnace that is being destroyed.
     *
     * @param *pVal [out] The name of the instance.
     */
    public String getInstanceName()
    {
        // Why retrieve the InputPin node, and call it the output pin?
        Node outputPinNode = getXMLNode("UML:InputPin");
        return outputPinNode != null?
					XMLManip.getAttributeValue(outputPinNode, "value")
                  : null;
    }
}