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
 * @author  Milos Kleint
 */
public class EnhancedMessageEvent extends MessageEvent {

    /**
     * Sent by MergedResponse when 2 files were merged.
     * The value is a String instance that tells the full path to the file.
     */
    public static final String MERGED_PATH = "Merged_Response_File_Path"; // NOI18N

    /**
     * Sent when a file was successfully sent to server.
     */
    public static final String FILE_SENDING = "File_Sent_To_Server"; // NOI18N

    /**
     * Sent when a all requests were sent to server.
     * Value is a String with value of "ok".
     */
    public static final String REQUESTS_SENT = "All_Requests_Were_Sent"; // NOI18N

    /**
     * Sent before all request are processed.
     * Value is an Integer object.
     */
    public static final String REQUESTS_COUNT = "Requests_Count"; // NOI18N

    private String key;
    private Object value;

    /**
     * Construct a MessageEvent
     * @param source the source of the event
     * @param key identifier. Specifies what the value object is.
     * @param value. Some value passed to the listeners. The key parameter helps
     *   the listeners to identify the value and handle it correctly.
     * for stderr rather than stdout), false otherwise
     */

    public EnhancedMessageEvent(Object source, String key, Object value) {
        super(source, "", false); // NOI18N
        this.key = key;
        this.value = value;
    }

    /** Getter for property key.
     * @return Value of property key.
     */
    public String getKey() {
        return key;
    }

    /** Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /** Getter for property value.
     * @return Value of property value.
     */
    public Object getValue() {
        return value;
    }

    /** Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
