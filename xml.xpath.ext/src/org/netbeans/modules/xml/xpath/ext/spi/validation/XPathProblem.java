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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.xpath.ext.spi.validation;

import org.openide.util.NbBundle;

/**
 * The enumeration of common problems which can happen while 
 * the XPath model performs resolution of ext references.
 * 
 * @author nk160297
 */
public enum XPathProblem {
    XPATH_PARSING_EXCEPTION,
    BAD_XPATH_EXPRESSION,
    UNSUPPORTED_AXIS, 
    UNKNOWN_ATTRIBUTE, 
    UNKNOWN_ELEMENT, 
    UNKNOWN_ATTRIBUTE_WITH_NAMESPACE, 
    UNKNOWN_ELEMENT_WITH_NAMESPACE, 
    AMBIGUOUS_ELEMENT, 
    AMBIGUOUS_ATTRIBUTE, 
    UNKNOWN_NAMESPACE_PREFIX, 
    ELEMENT_UNNECESSARY_PREFIX, 
    ATTRIBUTE_UNNECESSARY_PREFIX, 
    MISSING_NAMESPACE_PREFIX, 
    MISSING_SCHEMA_IMPORT, 
    ELEMENT_PREFIX_REQUIRED, 
    ELEMENT_SPECIFIC_PREFIX_REQUIRED, 
    ATTRIBUTE_PREFIX_REQUIRED, 
    ATTRIBUTE_SPECIFIC_PREFIX_REQUIRED, 
    GLOBAL_ATTRIBUTE_PREFIX_REQUIRED, 
    GLOBAL_ATTRIBUTE_SPECIFIC_PREFIX_REQUIRED, 
    GLOBAL_ELEMENT_PREFIX_REQUIRED, 
    GLOBAL_ELEMENT_SPECIFIC_PREFIX_REQUIRED, 
    UNKNOWN_EXTENSION_FUNCTION, 
    PREFIX_REQUIRED_FOR_EXT_FUNCTION, 
    OTHER_PREFIX_REQUIRED_FOR_EXT_FUNCTION, 
    ATTR_PREFIX_FROM_LIST_REQUIRED, 
    ELEM_PREFIX_FROM_LIST_REQUIRED, 
    ATTR_MAYBE_PREFIX_FROM_LIST_REQUIRED, 
    ELEM_MAYBE_PREFIX_FROM_LIST_REQUIRED, 
    ATTR_PREFIX_REDUNDANT, 
    ELEM_PREFIX_REDUNDANT, 
    AMBIGUOUS_ABSOLUTE_PATH_BEGINNING, 
    ATTEMPT_GO_UPPER_THAN_ROOT, 
    MISSING_PARENT_SCHEMA_CONTEXT, 
    EXPR_CONTAINS_STUB, 
    RUNTIME_NOT_SUPPORT_OPERATION;
    
    public String getMsgTemplate() {
        return NbBundle.getMessage(XPathProblem.class, this.toString());
    }
}
