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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class REActionSequence extends REAction implements IREActionSequence
{
    private List<IREAction> m_ActionList = new ArrayList<IREAction>();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREActionSequence#getIsBreakCalled()
     */
    public boolean getIsBreakCalled()
    {
        return Boolean
            .valueOf(XMLManip.getAttributeValue(getEventData(), "break"))
            .booleanValue();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREActionSequence#item(int)
     */
    public IREAction item(int index)
    {
        return m_ActionList.get(index);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREActionSequence#getCount()
     */
    public int getCount()
    {
        return m_ActionList.size();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IParserData#setEventData(org.dom4j.Node)
     */
    public void setEventData(Node node)
    {
        String name = node.getName();
        List children = ((Element) node).elements();
        for (int i = 0, count = children.size(); i < count; ++i)
        {
            createAction((Node) children.get(i));
        }
        super.setEventData(node);
    }
    
    protected void createAction(Node node)
    {
        String name = ((Element)node).getQualifiedName();
        IREAction act = null;
        if ("UML:CallAction".equals(name))
            act = new RECallAction();
        else if ("UML:CreateAction".equals(name))
            act = new RECreateAction();
        else if ("UML:ReturnAction".equals(name))
            act = new REReturnAction();
        else if ("UML:SendAction".equals(name))
            ; // Not implemented
        else if ("UML:TerminateAction".equals(name))
            ; // Not implemented
        else if("UML:UninterruptedAction".equals(name))
            ; // Not implemented
        else if ("UML:DestroyAction".equals(name))
            act = new REDestroyAction();
        else if ("UML:ActionSequence".equals(name))
            act = new REActionSequence();
        if (act != null)
        {
            act.setEventData(node);
            m_ActionList.add(act);
        }
    }
}