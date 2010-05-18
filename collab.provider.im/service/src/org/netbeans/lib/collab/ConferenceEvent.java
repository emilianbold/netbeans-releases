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
 * An Event is a piece of information originated by the service, as opposed
 * to a Message which is originated by another user of the service.  The format
 * of pre-defined events is documented here.  Nothing prevents a deployment
 * to define other types of events by extending EventType.
 *
 * @since version 0.1
 *
 */
public interface ConferenceEvent {

    /**
     * Returns the serialized version of the event (XML data)
     * $return XML data.
     */
    public String toString();

    /**
     * Returns the event type
     * @return int code for this event type
     */
    public int getType();


    /**
     * indication of user input.  This allows the application to convey 
     * to the user that a message is being composed by another member of
     * the conference.  The XML format associated with this event is
     * defined in XXX.  Example
     * <code><ul>
     * <p><tt>&lt;mevent type='accessmodified'></tt>
     * <br><tt>&nbsp;&nbsp;&nbsp; &lt;subject destination='fred@example.com'</tt>
     * <br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;accesslevel='MANAGE' /></tt>
     *<br><tt>&lt;/mevent></tt><tt></tt>
     * </code></ul>
     */
    public final int ETYPE_USER_INPUT_STARTED = 9;
    
    /**
     * indication of user input.  This allows the application to convey 
     * to the user that a message composing is stoped by another member of
     * the conference.  The XML format associated with this event is
     * defined in XXX.  Example
     * <code><ul>
     * <p><tt>&lt;mevent type='accessmodified'></tt>
     * <br><tt>&nbsp;&nbsp;&nbsp; &lt;subject destination='fred@example.com'</tt>
     * <br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;accesslevel='MANAGE' /></tt>
     *<br><tt>&lt;/mevent></tt><tt></tt>
     * </code></ul>
     */
    public final int ETYPE_USER_INPUT_STOPED = 10;

    /**
     * indicates that a member of the NotificationSession has left it.  Example
     * content:
     * <ul><code>
     * <p><tt>&lt;mevent type='userleft'></tt>
     * <br><tt>&nbsp;&nbsp;&nbsp; &lt;subject destination='fred@example.com' /></tt>
     * <br><tt>&lt;/mevent></tt>
     * </code></ul>
     */
    public final int ETYPE_USER_LEFT = 11;

    /**
     * indicates that a member of the NotificationSession has left it.
     * example content:   
     * <code><ul>
     * <p><tt>&lt;mevent type='userjoined'></tt>
     * <br><tt>&nbsp;&nbsp;&nbsp; &lt;subject destination='fred@example.com'</tt>
     * <br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;accesslevel='INVITE' /></tt>
     * <br><tt>&lt;/mevent></tt>
     * </code></ul>
     */
    public final int ETYPE_USER_JOINED = 12;

    /**
     * indicates a change of service access level for the current user.
     * it applies to the current conference.  Example
     * content:
     * <ul><code>
     * <p><tt>&lt;mevent type='accessmodified'></tt>
* <br><tt>&nbsp;&nbsp;&nbsp; &lt;subject destination='fred@example.com'</tt>
* <br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;accesslevel='MANAGE' /></tt>
* <br><tt>&lt;/mevent></tt><tt></tt>
     * </code></ul>
     */
    public final int ETYPE_ACCESS_MODIFIED = 20;

    /**
     * The conference is being terminated
     */
    public final int ETYPE_CLOSE = 30;

    /**
     * The current user has successfully joined the conference.
     */
    public final int ETYPE_ACTIVE = 31;

    /**
     * The current user has started the moderation for the conference.
     */
    public final int ETYPE_MODERATION_STARTED = 32;
    
    /**
     * The current user has stoped the moderation for the conference.
     */
    public final int ETYPE_MODERATION_STOPED = 33;
    
    /**
     * 
     */
    public final int ETYPE_OTHER = 99;

    
}
