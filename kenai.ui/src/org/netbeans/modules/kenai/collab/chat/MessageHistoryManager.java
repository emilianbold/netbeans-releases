/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.collab.chat;

import java.util.Vector;

/**
 * A class used for storing a chatroom message history
 * @author joshis - Petr.Dvorak@sun.com
 */
public class MessageHistoryManager {

    private Vector<String> messageHistory = new Vector<String>();
    private int historyIndex = -1;
    private String currentMessage = null;

    /**
     * Add a message to the history. Keeps the relative ordering of the history,
     * new messages are added correctly.
     * @param message The message to be added
     */
    public void addMessage(String message) {
        if (historyIndex > -1) {
            historyIndex++;
        }
        messageHistory.add(0, message);
    }

    /**
     * Get the number of messages in history.
     * @return Size of the message history
     */
    public int getSize() {
        return messageHistory.size();
    }

    /**
     * Sets the not-yet-sent message (it the one that is currently being edited in the text area)
     * @param message Message to be set
     */
    public void setEditedMessage(String message) {
        currentMessage = message;
    }

    /**
     * Set the history to the first message a unintialize variables.
     */
    public void resetHistory() {
        historyIndex = -1;
        currentMessage = null;
    }

    /**
     * Check if you are in the very beginning of the message history
     * @return True if you are in the message history, false otherwise
     */
    public boolean isOnStart() {
        return historyIndex == -1;
    }

    /**
     * Get the message that was send before the currently selected message and (!!!) shif the pointer on it.
     * This is important sideefect, calling this method twice returns differrent messages.
     * @return A message that is before the current message
     */
    public String getPreviousMessage() {
        String ret = null;
        if (historyIndex < messageHistory.size() - 1) {
            historyIndex++;
            ret = messageHistory.get(historyIndex);
        }
        return ret;
    }

    /**
     * Get the message that was send after the currently selected message and (!!!) shif the pointer on it.
     * This is important sideefect, calling this method twice returns differrent messages.
     * @return A message that is after the current message
     */
    public String getNextMessage() {
        String ret = null;
        if (historyIndex > -1) {
            historyIndex--;
            if (historyIndex > -1) {
                ret = messageHistory.get(historyIndex);
            } else {
                ret = currentMessage;
            }
        } else ret = currentMessage;
        return ret;
    }
}
