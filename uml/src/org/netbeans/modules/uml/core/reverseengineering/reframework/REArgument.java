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
public class REArgument extends ParserData implements IREArgument
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREArgument#getName()
     */
    public String getName()
    {
        return XMLManip.getAttributeValue(getEventData(), "name");
    }

    /**
     * The value of the argument, the value may be an expression.
     * @param pVal [out] The value of the argument.
     */
    public String getValue()
    {
        String kind = XMLManip.getAttributeValue(getEventData(), "kind");
        return !"Type".equals(kind)?
                    XMLManip.getAttributeValue(getEventData(), "value")
                  : null;
    }

    /**
     * Retrieves the type of the argument.
     * @param pVal [out] The arguments type.
     */
    public String getType()
    {
        String kind = XMLManip.getAttributeValue(getEventData(), "kind");
        return "Type".equals(kind)?
                    XMLManip.getAttributeValue(getEventData(), "value")
                  : null;
    }
}