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
 * A SASL client provider allows for extending the IM server to support custom SASL
 * protocols.
 * These are created from SASLClientProviderFactory for the required mechanism.
 *
 * @author Mridul Muralidharan
 */
public interface SASLClientProvider {

    /**
     * If there was a loginName specified , it will be passed on to the provider
     * using this method , else null will be passed.
     */
    public void setLoginName(String loginName);

    /**
     * If there was a password specified , it will be passed on to the provider
     * using this method , else null will be passed.
     */
    public void setPassword(String password);
    
    /**
     * Set's the server (or domain) to which the user wants to log into.
     */
    public void setServer(String server);
    
    /**
     * Called before 'using' this instance of the provider so that it can
     * initialise itself.
     * Invocation of this method indicates that there will be subsequent calls to
     * SASLClientProvider#process
     */
    public void init() throws SASLProviderException;

    /**
     * The whole SASL auth process is encapsulated within this method.
     * There will be a series of challenges passed on to the provider which were
     * issued by the server.
     * The client provider will evaluate them and pass an appropriate response.
     * Not all the challenges need to have a payload data associated in the SASLData,
     * like the initial SASLData#START state ,etc.
     * The client should respond back with a SASLData which which are one of :
     * START (for the initial request) , RESPONSE , ABORT , FAILURE or SUCCESS 
     * (when server responds with SUCCESS).
     *
     * If a status of FAILURE or ABORT is passed to the provider as part of the request
     * then it indicates that the authentication has been denied/failed at the server
     * side.
     *
     * Similarly, the provider can return a status of FAILURE to indicate 
     * client side authentication error.
     *
     * @exception SASLProviderException
     * This will result in sending a Abort SASL packet to the server and the
     * authentication process will fail.
     */
    public void process(SASLData data) throws SASLProviderException;
    
    /**
     * Always called after the provider's use has completed.
     * For every sucessful init() there will be a call to close()
     * This will be called irrespective of whether auth suceeds or fails.
     * This hook can be used to cleanup any resources in use by the provider.
     */
    public void close();
}
