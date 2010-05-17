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
public interface ContentStream {

    /**
     * It should be used for rejecting the stream if the Available methods
     * are not supported.
     */
    public static String METHOD_NOT_SUPPORTED = "no-valid-streams";

    /**
     * It should be used for rejecting the stream if the stream information is
     * not statisfactory.
     */
    public static String BAD_REQUEST = "bad-profile";

    /**
     * Lists the supported methods for streaming the data
     * @return An array of string as defined in StreamingService
     */
    public String[] getSupportedMethods();
    
    /**
     * This method should be used to accept the ContentStream. 
     * @param preferredMethod The preferred method as defined in StreamingService
     * @param profile The updated ReceiverStreamingProfile.
     * @param listener The ContentStreamListener for getting notifications
     * @throws IllegalStateException if the stream was already accepted or rejected
     * @throws IllegalArgumentException if the preferred method is not one of the supported methods
     * @throws CollaborationException
     */
    public void accept(String preferredMethod, ReceiverStreamingProfile profile, ContentStreamListener listener) throws CollaborationException;
    
     /**
     * This method should be used to reject the ContentStream. 
     * @param reason The reason for rejecting the stream as defined in ContentStream. If the
     * reason is not specified in ContentStream then a custom message should be used.
     * @throws IllegalStateException if the stream was already accepted or rejected
     * @throws CollaborationException
     */
    public void reject(String reason) throws CollaborationException;
    
    /**
     * This method should be used to abort the ContentStream. 
     * @throws IllegalStateException if the stream was already closed
     * @throws CollaborationException
     */
    public void abort() throws CollaborationException;
    
    /**
     * This method returns the number of bytes transferred through the stream. This method
     * can be used to monitor the progress of the content transfer
     * @return The number of bytes transferred
     */
    public long getTransferredBytes();
    
}
