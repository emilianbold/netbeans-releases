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
public interface PersonalGateway extends PersonalStoreEntry {

    /**
     * returns the JID of the gateway
     * @return - the jid of the gateway
     */
    public String getHostName() throws CollaborationException;

    /**
     * returns the name of the gateway
     * @return - the name of the gateway e.g AIM Gateway
     */
    public String getName() throws CollaborationException;


    /*
     * returns the legacy service to which it is a gateway
     * @return - the legacy service
     */
    public String getService() throws CollaborationException;
    
           
    /**
     * registers the user with the gateway
     * @param listener - the callback for the registration events
     */
    public void register(RegistrationListener listener) throws CollaborationException;
 
    
    /**
     * unregister the user with the gateway
     * @param listener - callback object for the unregistration events
     * 
     */
    public void unregister(RegistrationListener listener) throws CollaborationException;
    
    
    /**
     * returns the set of features supported by the gateway
     * Each element in the Set is a String object
     * @return Set of features supported by the gateway
     */
    
    public java.util.Set getSupportedFeatures() throws CollaborationException;
    
    
    /**
     * @param feature - feature to check if supported or not supported
     * @return true if the feature is supported by the gateway, otherwise false
     */
    public boolean isSupportedFeature(String feature) throws CollaborationException;
    
       
    /*
     * Is the user registered with the gateway
     * @return true - if the user is registered with the gateway, otherwise false
     */
    
    public boolean isRegistered() throws CollaborationException;
}
