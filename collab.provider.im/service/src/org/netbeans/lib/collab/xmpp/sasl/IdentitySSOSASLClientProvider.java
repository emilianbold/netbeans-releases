/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006, 2016 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.lib.collab.xmpp.sasl;

import java.io.ByteArrayOutputStream;
import org.netbeans.lib.collab.SASLClientProvider;
import org.netbeans.lib.collab.SASLData;
import org.netbeans.lib.collab.SASLProviderException;

/**
 *
 * @author mridul
 */
public class IdentitySSOSASLClientProvider implements SASLClientProvider{
    
    private String loginName;
    private String token;
    private String server;
    
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setPassword(String password) {
        this.token = password;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void init() throws SASLProviderException {
        // do nothing.
    }

    public void process(SASLData data) throws SASLProviderException {
        int status = data.getRequestStatus();
        if (status == SASLData.START){
            // send uid/passwd
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try{
                baos.write(loginName.getBytes("UTF-8"));
                baos.write(0);
                baos.write(token.getBytes("UTF-8"));
                baos.write(0);
                data.setResponseData(baos.toByteArray());
                data.setResponseStatus(SASLData.START);
            } catch(Exception ex){
                // Unexpected ...
                data.setResponseStatus(SASLData.ABORT);
            }
            return ;
        }
        if (status == SASLData.SUCCESS){
            // Cool !
            data.setResponseData(null);
            data.setResponseStatus(SASLData.SUCCESS);
            return ;
        }
        
        // Everything else is unexpected ...
        data.setResponseData(null);
        data.setResponseStatus(SASLData.FAILURE);
        return ;
    }

    public void close() {
        // do nothing.
    }
}
