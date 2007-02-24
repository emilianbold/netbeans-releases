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
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REAction extends ParserData implements IREAction
{
    /**
     * Retrieves the arguments for the action.
     * @param pVal [out] The action arguments or NULL if there are no arguments.
     */
    public ETList<IREArgument> getArguments()
    {
        try
        {
            return new REXMLCollection<IREArgument>(
                    REArgument.class,
                    "UML:Element.ownedElement/UML:InputPin",
                    getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the name of the model element that is receiving the event.
     * The name will be the UML fully scoped name of the receiving model element.
     * @param pVal [out] The receiver of the action.
     */
    public String getReceiver()
    {
        Node node = getEventData();
        if (node != null)
        {
            // The Receiver information is stored on the sub tag called 
            // UML:Action.message/UML:Message.  So, I must first get the 
            // message node and then retrieve the receiver.
            // AZTEC: No evidence that UML:Action.message is still used, going
            //        with ownedElement instead.
//            String query = "UML:Action.message/UML:Message";
            String query = "UML:Element.ownedElement/UML:Message";
            Node messageNode = node.selectSingleNode(query);
            if (messageNode != null)
                return XMLManip.getAttributeValue(messageNode, "receiver");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREAction#getSender()
     */
    public String getSender()
    {
        Node n = getEventData();
        if (n != null)
        {
            // The Receiver information is stored on the sub tag called 
            // UML:Action.message/UML:Message.  So, I must first get the 
            // message node and then retrieve the receiver.
            // AZTEC: No evidence that UML:Action.message is still used, going
            //        with ownedElement instead.
            Node messageNode = 
                XMLManip.selectSingleNode(n, "UML:Element.ownedElement/UML:Message");
            
            return messageNode != null?
                           XMLManip.getAttributeValue(messageNode, "sender")
                         : null;
        }
        return null;
    }

    /**
     * Retrieves the source code comment for the action.
     * @param pVal [out] The comment.
     */
    public String getComment()
    {
        // C++ code also hardcodes this.
        return "";
    }

    /**
     * Retrieves whether the message is a asynchronous message.
     * @param pVal true if asynchronous, false otherwise.
     */
    public boolean getIsAsynchronous()
    {
        // C++ code also hardcodes this.
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREAction#getTarget()
     */
    public String getTarget()
    {
        return XMLManip.getAttributeValue(getEventData(), "target");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREAction#getRecurrence()
     */
    public String getRecurrence()
    {
        return XMLManip.getAttributeValue(getEventData(), "recurrence");
    }
}
