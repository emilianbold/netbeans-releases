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
public interface StreamingService {
    /**
     * This method streams the data through the server
     */
    public static String INBAND_STREAM_METHOD = "http://jabber.org/protocol/ibb";

    /**
     * This method streams the data bypassing the server
     */
    public static String OUTBAND_STREAM_METHOD = "jabber:iq:oob";

    /**
     * Opens a ContentStream with the recipient. The stream will be used to stream
     * the data to the recipient
     * @param rcpt - The recipient of the stream
     * @param methods - The array of methods for the transfer as defined in StreamingService
     * @param profile - The sender streaming profile containing the meta data about the stream
     * @param listener - The ContentStreamListener to be used for notifications
     * @throws CollaborationException
     */
    public ContentStream open(String rcpt, String[] methods,
                              SenderStreamingProfile profile,
                              ContentStreamListener listener)
                              throws CollaborationException;

    /**
     * intialize the service by providing a StreamingServiceListener.
     * Service should be initialized by calling this method before using
     * any of the services of StreamingService
     * @param listener StreamingServiceListener
     */
    public void initialize(StreamingServiceListener listener)
                                                throws CollaborationException;

    /**
     * Add an additional StreamingServiceListener to receive the event notifications.
     * To receive all the initial events the first StreamingServiceListener should be
     * added while calling {@link StreamingService#initialize} StreamingService.
     * @param listener The StreamingServiceListener to be added.
     */
    public void addStreamingServiceListener(StreamingServiceListener listener);

    /**
     * Removes an already added ConfereneServiceListener. To prevent loss of any event
     * notification it is advised to have atleast one StreamingServiceListener
     * @param listener The StreamingServiceListener to be removed.
     */
    public void removeStreamingServiceListener(StreamingServiceListener listener);
}
