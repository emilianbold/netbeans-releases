/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xpath;


/**
 * Interface for an XPath parser wrapper.
 *
 * @author Enrico Lelina
 * @version $Revision$
 */
public interface XPathModel {

    /** XPath function names that are not considered part of the core. */
    String[] VALID_FUNCTION_NAMES = {
        "unparsed-entity-uri", "system-property",
        "string-literal", "element-available", "function-available",
        "document", "current", "generate-id", "stringToBytes", "bytesToString",
        "getCurrentTime", "getGUID", "getBPId", "convert", "incrementDatetime",
        "decrementDatetime", "exists", 
        "current-time", "current-date", "current-dateTime"
    };
    
    /** BPEL4WS extensions wihtout the namespace prefix. */
    String[] VALID_BPWS_FUNCTION_NAMES = {
        "getContainerData", "getContainerProperty", "getLinkStatus"
    };

    /**
     * Parses an XPath expression.
     * @param expression the XPath expression to parse
     * @return an instance of XPathExpression
     * @throws XPathException for any parsing errors
     */
    XPathExpression parseExpression(String expression)
        throws XPathException;
}
