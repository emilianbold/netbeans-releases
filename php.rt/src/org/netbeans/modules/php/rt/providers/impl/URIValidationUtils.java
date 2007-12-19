/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class URIValidationUtils {
    
    private final static String LBL_VALIDATING_INET_ADDRESS 
            = "LBL_ValidateInetAddress"; // NOI18N
    private static final String MSG_WARNING_DOMAIN = "MSG_DomainWarning"; // NOI18N

    /**
     * Tries to Determine the IP address of a host, given the host's name
     * Uses InetAddress.getByName.
     * 
     * 
     * @param addressName host name
     * @return false if InetAddress.getByName result is null or 
     * UnknownHostException is thrown. Returns true otherwise.
     */
    public static void validateInetAddress(final String addressName) {
        new Thread(){
            @Override
            public void run(){
                String title = NbBundle.getMessage(URIValidationUtils.class, 
                        LBL_VALIDATING_INET_ADDRESS, addressName);
                
                ProgressHandle progress = ProgressHandleFactory
                        .createHandle(title); // NOI18N
                progress.start();

                try {
                    if (!doInetAdderssValidation(addressName)) {
                        
                        String message = NbBundle.getMessage(URIValidationUtils.class,
                                MSG_WARNING_DOMAIN, addressName);
                        NotifyDescriptor descriptor 
                                = new NotifyDescriptor.Message(message);
                        DialogDisplayer.getDefault().notify(descriptor);
                    }
                } finally {
                    progress.finish();
                }
            }
        }.start();
    }
    
    public static boolean isHostNameValid(String hostName){
        try {
            new URI(null, hostName, null, null);
        } catch (URISyntaxException ex) {
            return false;
        }
        return true;
    }

    public static boolean isPathValid(String path){
        try {
            new URI(null, null, path, null);
        } catch (URISyntaxException ex) {
            return false;
        }
        return true;
    }
    
    private static boolean doInetAdderssValidation(String addressName) {
        boolean valid = true;
        try {
            InetAddress address = InetAddress.getByName(addressName);
            valid = address != null;
        } catch (UnknownHostException e) {
            valid = false;
        }
        return valid;
    }
    
}
