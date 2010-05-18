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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab;

/**
 *
 *
 * @since version 0.1
 *
 */
public interface Message extends MessagePart {

    /**
     * acknowledge reception of the message
     * @param status as defined in MessageStatus
     * @see MessageStatus
     */
    public void sendStatus(int status) throws CollaborationException;

    /**
     * send a reply to the alert message
     * @param message reply message.  The reply message must be implemented with the
     * createReply method.
     *
     */
    public void sendReply(Message message) throws CollaborationException;
    
    /**
     * returns the originator address
     * @return originator address
     */
    public String getOriginator();
    
    /**
     * returns the originator address
     * @return originator address
     */
    public void setOriginator(String originator) throws CollaborationException;
    
    /**
     * adds a recipient to a message
     * @param destination recipient's address
     */
    public void addRecipient(String destination) throws CollaborationException;
    
    /**
     * removes a recipient from the recipient list of a message.
     * @param destination recipient's address
     */
    public void removeRecipient(String destination) throws CollaborationException;
    
    /**
     * get the recipients of the message.
     * @return array of recipient addresses
     */
    public String[] getRecipients() throws CollaborationException;
    
    /**
     * returns the message identifier.  The message identifier is generated
     * by the provider.
     * @return message identifier
     */
    public String getMessageId();
    
    /**
     * returns the date of expiration of this message.
     * @return message expiration date
     */
    public java.util.Date getExpirationDate();
            
    /**
     * instantiates a new body part.
     * Note that this is not equivalent to adding a part.  It merely
     * instantiates a MessagePart object which can be added to 
     * this message when complete.
     * @see Message#addPart
     * @return new, empty message part
     */
    public MessagePart newPart() throws CollaborationException;
    
    /**
     * get the value of a header
     */
    public String getHeader(String header);

    /**
     * set the value of a header
     */
    public void setHeader(String header, String value) throws CollaborationException;
           
    /**
     * adds a body part to the end of the message being built
     * @param part message bodypart to add.
     */
    public void addPart(MessagePart part) throws CollaborationException;
    
    /**
     * removes a bodypart
     * @param part Message Part tp remove.
     */
    public void removePart(MessagePart part) throws CollaborationException;
    
    /**
     * get the nested parts of a multi-part message or message part
     */
    public MessagePart[] getParts();
    
    /**
     * @param expireAt date at which the message will expire
     * @deprecated use addProcessingRule
     */
    public void setExpirationDate(java.util.Date expireAt) throws CollaborationException;

    /**
     * adds a message processing rule at the end of the rule set.
     * Rules are to be processed by the service in the order in
     * which they are specified, until one matches.
     *
     * @param condition matching condition for this rule
     * @param action what to do if the condition is matched
     * @exception ServiceUnavailableException the service does not support
     * user-controlled message processing or does not support the specific
     * action/condition combination provided
     * @exception IllegalArgumentException if the condition or action is null
     */
    public MessageProcessingRule addProcessingRule(MessageProcessingRule.Condition condition, MessageProcessingRule.Action action)
	throws ServiceUnavailableException, IllegalArgumentException;

    /**
     * insert a message processing rule at the given position
     * rules are to be processed by the service in the order in
     * which they are specified, until one matches.
     * @param index position of the rule within the rule set.  Order
     * of rule matters since they are applied sequentially until a 
     * match is found.
     * @param condition matching condition for this rule
     * @param action what to do if the condition is matched
     * @exception ServiceUnavailableException the service does not support
     * user-controlled message processing.
     * @exception IndexOutOfBoundsException if the index is out of range
     * @exception IllegalArgumentException if the condition or action is null
     */
    public MessageProcessingRule addProcessingRule(int index, MessageProcessingRule.Condition condition, MessageProcessingRule.Action action)
	throws ServiceUnavailableException, IndexOutOfBoundsException, IllegalArgumentException;


    /**
     * adds a message processing rule at the end of the rule set.
     * Rules are to be processed by the service in the order in
     * which they are specified, until one matches.
     *
     * @param conditions compound conditions.  When setting multiple conditions
     * the resulting condition is equivalent to the intersection
     * of all individual conditions.  i.e. The rule matches only messages
     * which match all individual conditions.
     * @param action what to do if the condition is matched
     * @exception ServiceUnavailableException the service does not support
     * user-controlled message processing or does not support the specific
     * action/condition combination provided.  It may also happen that the
     * implementation does not support multi-condition rules.
     * @exception IllegalArgumentException the conditions array is either
     * empty or contains multiple conditions of the same type.
    public MessageProcessingRule addProcessingRule(MessageProcessingRule.Condition[] conditions, MessageProcessingRule.Action action)
	throws ServiceUnavailableException, IllegalArgumentException;
     */

    /**
     * insert a message processing rule at the given position
     * rules are to be processed by the service in the order in
     * which they are specified, until one matches.
     * @param index position of the rule within the rule set.  Order
     * of rule matters since they are applied sequentially until a 
     * match is found.
     * @param conditions compound conditions.  When setting multiple conditions
     * the resulting condition is equivalent to the intersection
     * of all individual conditions.  i.e. The rule matches only messages
     * which match all individual conditions.
     * @param action what to do if the condition is matched
     * @exception ServiceUnavailableException the service does not support
     * user-controlled message processing.  It may also happen that the
     * implementation does not support multi-condition rules.
     * @exception IllegalArgumentException the conditions array is either
     * empty or contains multiple conditions of the same type.
    public MessageProcessingRule addProcessingRule(int index, MessageProcessingRule.Condition[] conditions, MessageProcessingRule.Action action)
	throws ServiceUnavailableException, IndexOutOfBoundsException, IllegalArgumentException;
     */


    /**
     * removes a message processing rule.
     * @param rule the rule to remove
     * @return true if a matching rule was present and effectively
     * removed from this, false otherwise
     */
    public boolean removeProcessingRule(MessageProcessingRule rule);

    /**
     * removes a message processing rule.
     * @param index position of the rule to remove
     * @return true if a matching rule was present and effectively
     * removed from this, false otherwise
     */
    public MessageProcessingRule removeProcessingRule(int index)
	throws IndexOutOfBoundsException;


}

