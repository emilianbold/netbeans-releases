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
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class MethodEvent extends MethodDetailParserData implements IMethodEvent
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getDeclaringClassName()
     */
    public String getDeclaringClassName()
    {
        IREClass rec = getREClass();
        return rec != null? 
                 rec.getName() 
               : getTokenDescriptorValue("DeclaringType");
    }

    public String getFullQNameOfOwner()
    {
        String retVal = getTokenDescriptorValue("DeclaringType");
        
        if((retVal == null) || (retVal.length() <= 0))
        {
            IREClass rec = getREClass();
            if(rec != null)
            {
                retVal = rec.getPackage();
                retVal += "::";
                retVal += rec.getName();
            }
                
        }
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getArguments()
     */
    public ETList<IREArgument> getArguments()
    {
        REXMLCollection<IREArgument> args = 
            new REXMLCollection<IREArgument>(
                REArgument.class,
                "UML:PrimitiveAction.argument/UML:InputPin");
        try
        {
            args.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return args;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getResult()
     */
    public String getResult()
    {
        Node n = getXMLNode("UML:Operation/UML:Element.ownedElement/" +
                "UML:Parameter[@direction='result']");
        return n != null? XMLManip.getAttributeValue(n, "type") : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getInstanceName()
     */
    public String getInstanceName()
    {
        return XMLManip.getAttributeValue(getEventData(), "instance");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getMethodName()
     */
    public String getMethodName()
    {
        return XMLManip.getAttributeValue(getEventData(), "name");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getREClass()
     */
    public IREClass getREClass()
    {
        Node node = getXMLNode("UML:Class|UML:Interface|UML:Enumeration");
        if (node != null)
        {    
            IREClass cl = new REClass();
            cl.setEventData(node);
            return cl;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IMethodEvent#getOperation()
     */
    public IREOperation getOperation()
    {
        Node n = getXMLNode("UML:Operation");
        if (n != null)
        {
            IREOperation op = new REOperation();
            op.setEventData(n);
            return op;
        }
        return null;
    }
}