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
public class JumpEvent extends MethodDetailParserData implements IJumpEvent
{
    /**
     * The destination of the jump.  The destination
     * is an option property.  If destination is an
     * empty string then the execution continues after
     * the current block
     *
     * @param *pVal [in] The destination of the jump.
     */
    public String getDestination()
    {
        Node ipn = getXMLNode("UML:InputPin");
        return ipn != null? XMLManip.getAttributeValue(ipn, "value") : null;
    }

    /**
     * Specifies the type of jump that occured.  The Jump can be
     * a goto, break, or continue.
     *
     * @param *pVal [in] The type of jump. 
     *                   goto     - JE_GOTO
     *                   break    - JE_BREAK
     *                   continue - JE_CONTINUE
     */
    public int getJumpType()
    {
        String value = XMLManip.getAttributeValue(getEventData(), "type");
        if ("Break".equals(value))
            return JE_BREAK;
        else if ("Continue".equals(value))
            return JE_CONTINUE;
        else if ("Throw".equals(value))
            return JE_THROW;
        return JE_GOTO;
    }
}