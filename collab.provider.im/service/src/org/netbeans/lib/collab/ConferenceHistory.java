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
 * This class should be used to convey the parameters desired for getting the message
 * history for the conference room. The server will return the messages which satifies all
 * the criteria specified in this class.
 *
 * @author Rahul Shah
 */
public class ConferenceHistory {

    private int _maxStanzas = -1;
    private int _maxChars = -1;
    private java.util.Date _since = null;
    private int _seconds = -1;

    /**
     * The maximum number of history messages to be retrieved
     * @return maximum nunber of messages
     */
    public int getMaxMessages() {
        return _maxStanzas;
    }

    /**
     * The maximum number of history messages to be retrieved
     * @param maxMessages The maximum nuber of messages.
     */
    public void setMaxMessages(int maxMessages) {
        this._maxStanzas = maxMessages;
    }

    /**
     * Maximum number of chars in the history messages 
     * to be retrieved
     */
    /*public int getMaxChars() {
        return _maxChars;
    }*/

    /**
     * Maximum number of chars in the history messages 
     * @param maxChars Maximum chars to be retrieved
     */
    /*public void setMaxChars(int maxChars) {
        this._maxChars = maxChars;
    }*/

    /**
     * The date since which the history is requested
     */
    public java.util.Date getSince() {
        return _since;
    }

    /**
     * The date since which the history is requested
     */
    public void setSince(java.util.Date since) {
        this._since = since;
    }

    /**
     * The method should be used to reuqest the history in last X seconds
     */
    public int getSeconds() {
        return _seconds;
    }

    /**
     * The method should be used to reuqest the history in last X seconds
     */
    public void setSeconds(int X) {
        this._seconds = X;
    }
}
