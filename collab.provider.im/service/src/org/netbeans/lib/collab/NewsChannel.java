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
 * NewsChannels inherit all the attributes and behaviors of a conference, in
 * particular, members can be instantly notified of new messages, access control
 * rules are created and manages the same way.
 * <p>However,
 * An instant bulletin board differs from a public conference in the following ways.
 * <ul>
 * <li>Messages persist until they
 * are explicitly removed by a member with MANAGE privilege.
 * <li>It is possible to remove or modify messages</li>
 * <li>it is possible to access messages only after 
 * subscribing to the newschannel</li>
 * </ul>
 *
 * 
 * @since version 0.1
 * 
 */
public interface NewsChannel extends Conference {

     /**
      * remove a message from the bulletin board
      * @param MessageId id of the message to remove
      */
     public void removeMessage(String MessageId) throws CollaborationException;
     
    /**
     * modify a message from the bulletin board
     * @param messageId messageID message id
     * @param message the modified message.
     */
     public void modifyMessage(String messageId, Message message) throws CollaborationException;
     /**
      * subscribe to a bulletin board.
      * @param listener the listener by which modifications to the bulletin 
      * board are conveyed asynchronously to the caller.
      */
     public void subscribe(NewsChannelListener listener) throws CollaborationException;
     
     /**
      * set listener to the newschannel. 
      * This method should be invoked for all the subscribed newschannels to be able to 
      * receive newschannel notifications asynchronously.
      * @param listener the listener by which notifications are conveyed asynchronously to the caller.
      */
     public void setListener(NewsChannelListener listener) throws CollaborationException;
     
     /**
      * retrieves all the messages posted to the newschannel. The news messages are
      * notified asynchronously using the callback methods of the NewsChannelListener
      * You have to invoke setListener before invoking this method to be able
      * to receive the news messages posted to the newschannel. 
      */
     public void getMessages() throws CollaborationException;
     
}

