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

/**
 */
public class ActionEvent extends ParserData implements IActionEvent
{
    /**
     * Retrieves the action that is executed by the action.
     * @param pVal The action data.
     */
    public IREAction getAction()
    {
        // The XML node that is the event data is also the action.  So
        // get the XML node and create a REAction instance.
        Node actionNode = getEventData();
        return actionNode != null? createAction(actionNode) : null;
    }
    
    /**
     * Create a new action and adds it to the collection of actions.  The 
     * IREAction implementation created depends on the action type.
     */
    protected IREAction createAction(Node node)
    {
        String name = node.getName();
        IREAction action = null;
        if ("UML:CallAction".equals(name))
            action = new RECallAction();
        else if ("UML:CreateAction".equals(name))
            action = new RECreateAction();
        else if ("UML:ReturnAction".equals(name))
            action = new REReturnAction();
        else if("UML:SendAction".equals(name))
            ;// TODO: Implement
        else if("UML:TerminateAction".equals(name))
            ;// TODO: Implement
        else if("UML:UninterruptedAction".equals(name))
            ;// TODO: Implement
        else if("UML:DestroyAction".equals(name))
            action = new REDestroyAction();
        else if("UML:ActionSequence".equals(name))
            action = new REActionSequence();
        
        if (action != null)
            action.setEventData(node);
        return action;
    }
}