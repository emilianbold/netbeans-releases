/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
