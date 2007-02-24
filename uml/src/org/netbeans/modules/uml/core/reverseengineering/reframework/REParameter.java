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

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REParameter extends ParserData implements IREParameter {
    /**
     * Specifies an expression whose evaluation yields a value to be used
     * when no argument is supplied for the Parameter.
     * @param pVal [out] The default value.
     */
    public String getDefaultValue() {
        Node node = getXMLNode("UML:Parameter.defaultValue");
        if (node != null) {
            // The first child node (Actually the only child node) will
            // be one of the expression nodes.  Since there are a number
            // of different expression nodes I can not do a xstring query.
            // However, every expression tag has the same interface.  I
            // need to retrieve the body attribute from the expression node.
            Node expr = XMLManip.getFirstChild(node);
            return expr != null? XMLManip.getAttributeValue(expr, "body") : null;
        }
        return null;
    }
    
    /**
     * Specifies what kind of a Parameter is required.  A parameter can be
     * an in parameter,  out parameter, or in/out parameter."
     * @param pVal [out] The parameter kind.
     */
    public int getKind() {
        String dir = XMLManip.getAttributeValue(getEventData(), "direction");
        int dirk = BaseElement.PDK_INOUT;
        if ("in".equals(dir))
            dirk = BaseElement.PDK_IN;
        else if ("out".equals(dir))
            dirk = BaseElement.PDK_OUT;
        else if ("result".equals(dir))
            dirk = BaseElement.PDK_RESULT;
        return dirk;
    }
    
    public ETList<IREMultiplicityRange> getMultiplicity() {
        
        REXMLCollection<IREMultiplicityRange> mul =
                new REXMLCollection<IREMultiplicityRange>(
                REMultiplicityRange.class,
                "UML:TypedElement.multiplicity/UML:Multiplicity" +
                "/UML:Multiplicity.range/UML:MultiplicityRange");
        try {
            mul.setDOMNode(getEventData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mul;
    }
    
    /**
     * Designates a type to which an argument value must conform.
     * @param pVal [out] The parameter type.
     */
    public String getType() {
        String retVal = XMLManip.getAttributeValue(getEventData(), "type");
        
        return retVal.replace(".", "::");
    }
    
    /**
     * Retrieve the name of the parameter.
     * @param pVal [out] The name of the parameter.
     */
    public String getName() {
        return XMLManip.getAttributeValue(getEventData(), "name");
    }
    
    /**
     * Specifies if the parameter is a primitive or an object instance.
     *
     * @param *pVal [out] True if primitive, False otherwise.
     */
    public boolean getIsPrimitive() {
        return Boolean.valueOf(getTokenDescriptorValue("IsPrimitive"))
        .booleanValue();
    }
}