/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.event;

/**
 * An event sent from the server to indicate that a message should be
 * displayed to the user
 * @author  Robert Greig
 */
public class MessageEvent extends CVSEvent {
    /**
     * Holds value of property message.
     */
    private String message;

    /**
     * Whether the message is an error message
     */
    private boolean error;

    /** Holds value of property tagged. */
    private boolean tagged;

    private final byte[] raw;


    public MessageEvent(Object source, String message, byte[] raw, boolean isError) {
        super(source);
        setMessage(message);
        setError(isError);
        setTagged(false);
        this.raw = raw;
    }

    /**
     * Construct a MessageEvent
     * @param source the source of the event
     * @param message the message text
     * @param isError true if the message is an error message (i.e. intended
     * for stderr rather than stdout), false otherwise
     */
    public MessageEvent(Object source, String message, boolean isError) {
        this(source, message, null, isError);
    }

    /**
     * Construct a MessageEvent with no message text
     * @param source the source of the event
     */
    public MessageEvent(Object source) {
        this(source, null, false);
    }

    /**
     * Getter for property message.
     * @return Value of property message.
     */
    public String getMessage() {
        return message;
    }

    /** @return bytes from wire or null */
    public byte[] getRawData() {
        return raw;
    }

    /**
     * Setter for property message.
     * @param message New value of property message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get whether the message should be displayed in stderr
     * @return true if the message should be sent to stderr, false otherwise
     */
    public boolean isError() {
        return error;
    }

    /**
     * Set whether the message should go to stderr
     * @param error true if the message is an error message, false otherwise
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Fire the event to the event listener. Subclasses should call the
     * appropriate method on the listener to dispatch this event.
     * @param listener the event listener
     */
    protected void fireEvent(CVSListener listener) {
        listener.messageSent(this);
    }

    /** Getter for property tagged.
     * @return Value of property tagged.
     */
    public boolean isTagged() {
        return tagged;
    }

    /** Setter for property tagged.
     * @param tagged New value of property tagged.
     */
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    /**
     * Parses the tagged message using the specified buffer.
     * @returns != null, if the line is finished and could be processed
     */
    public static String parseTaggedMessage(StringBuffer taggedLineBufferNotNull, String taggedMessage) {
        String line = taggedMessage;

        if (line.charAt(0) == '+' || line.charAt(0) == '-') {
            return null;
        }

        String result = null;
        if (line.equals("newline")) {//NOI18N
            result = taggedLineBufferNotNull.toString();
            taggedLineBufferNotNull.setLength(0);
        }
        int index = line.indexOf(' ');
        if (index > 0) {
            taggedLineBufferNotNull.append(line.substring(index + 1));
        }
        return result;
    }
}
