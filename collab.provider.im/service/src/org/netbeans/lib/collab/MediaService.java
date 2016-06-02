/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.lib.collab;

import org.netbeans.lib.collab.xmpp.jingle.*;
import org.netbeans.lib.collab.MediaListener;
import org.jabberstudio.jso.Packet;


/**
 *
 * @author jerry
 */
public interface MediaService {

        /**
         * add a listener for Voip requests
         */
        public void addListener(MediaListener listener);
        /**
         * remove an active listener
         */
        public void removeListener(MediaListener listener);

        /**
         * Send a call initiate request
         * @param target the user id of the person to call
         */
        public void initiate(String target);
        /**
         * Send a redirect to a caller.
         * @param id The session id
         * @param caller User id of the caller who initiated the call
         * @param location where the call should be redirected
         **/
	public void redirect(String id, String caller, String redirLocation);
	/**
	 This method should be called from the client only
	 It tries to find if there are any server-side components
	 that can service voip requests
	 */
	public String findMediaGateway();

        /**
         * Send a terminate request to the peer
         * @param id The session ID
         * @param target the userid to send the request
         */
        public void terminate(String id, String target);

        /**
         * Find media addresses of a user
         * Used to locate the addresses at which a user can be reached
         * This could be, for eg, a telephone number or a SIP url
         * @param userid the User whose addresses are to be found
         */

        public String getAddress(String userid);

}
