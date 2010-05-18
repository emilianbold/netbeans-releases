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
