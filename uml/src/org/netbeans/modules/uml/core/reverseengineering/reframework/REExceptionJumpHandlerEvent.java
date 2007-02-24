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
public class REExceptionJumpHandlerEvent extends MethodDetailParserData
        implements IREExceptionJumpHandlerEvent
{
    /**
     * Determines if the exception handler is the default exception handler.
     *
     * @param pVal [out] The value
     */
    public boolean getIsDefault()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isDefault");
    }

    /**
     * The string representation of the event data.
     *
     * @param pVal [out] The string representation
     */
    public String getStringRepresentation()
    {
        String ret = null;
        boolean isDefault = getIsDefault();
        if (!isDefault)
        {
            ret = getExceptionType();
            if (ret != null && ret.length() > 0)
            {
                String name = getExceptionName();
                if (name != null && name.length() > 0)
                    ret += " " + name;
            }
        }
        return ret;
    }

    /**
     * The name of exception type that is being handled.
     *
     * @param pVal [out] The type of the exception.
     */
    public String getExceptionType()
    {
        Node n = getXMLNode("UML:Signal");
        return n != null? XMLManip.getAttributeValue(n, "name") : null;
    }

    /**
     * The name of the exception that was caught.
     *
     * @param pVal [out] The name of the exception.
     */
    public String getExceptionName()
    {
        Node n = getXMLNode("UML:Signal");
        return n != null? XMLManip.getAttributeValue(n, "instanceName") : null;
    }
}