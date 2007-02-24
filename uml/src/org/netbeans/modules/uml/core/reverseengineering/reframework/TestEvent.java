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

import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
//import com.sun.corba.se.internal.orbutil.Condition;

/**
 */
public class TestEvent extends MethodDetailParserData implements ITestEvent
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.ITestEvent#getStringRepresentation()
     */
    public String getStringRepresentation()
    {
        Node n = getEventData();
        String ret = "";
        if (n != null)
        {
            // NOTE: It would be better to use a XSLT script to generate
            //       this value.
            Node clauseParent = n.getParent();
            if (clauseParent != null)
            {
                // We will always want the conditional Node.  For normal conditionals 
                // that is the prefect place to look for the test condition.  For switch
                // statments the conditional node has the jump condition.
                Node conditionalParent = clauseParent.getParent();
                if (conditionalParent != null)
                {
                    String name = "";
                    if(clauseParent instanceof org.dom4j.Element)
                       name = ((org.dom4j.Element)clauseParent).getQualifiedName();
                    else
                       name = clauseParent.getName();
                       
                    if ("UML:ConditionalAction.switchClause".equals(name))
                    {
                        ret = getTestExpression(conditionalParent, ret);
                        
                        // If we have a switch statement then then the representing string 
                        // should be <Jump Expression> = <Case Expression> (, <Case Expression>)
                        //
                        // So, first get the jump condition.
                        if (ret != null && ret.length() > 0)
                            ret += " = ";
                    }
                }
                
                ret += getTestExpression(n, "");
            }
        }
        return ret;
    }

    /**
     * @param n
     * @return
     */
    private String getTestExpression(Node n, String val)
    {
        val = getTestExpression(n, "./UML:Clause.test", val);
        // There are two different test node name (That is UML for you, not able
        // to standardize on anything.
        if (val == null || val.length() == 0)
            val = getTestExpression(n, "./UML:LoopAction.test", val);
        return val;
    }

    /**
     * @param n
     * @param string
     * @return
     */
    private String getTestExpression(Node n, String query, String curval)
    {
        List nodes = XMLManip.selectNodeList(n, query);
        String ret = curval != null? curval : "";
        int added = 0;
        if (nodes != null)
        {    
            for (int i = 0, count = nodes.size(); i < count; ++i)
            {    
                Node cur = (Node) nodes.get(i);
                if (cur != null)
                {
                    String val = XMLManip.getAttributeValue(cur, "representation");
                    if (val == null || val.length() == 0)
                        continue;
                    if (added++ > 0)
                        ret += ", ";
                    ret += val;
                }
            }
        }
        return ret;
    }
}