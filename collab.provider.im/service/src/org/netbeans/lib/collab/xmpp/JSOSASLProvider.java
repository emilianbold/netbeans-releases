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

package org.netbeans.lib.collab.xmpp;

import java.util.LinkedList;
import java.util.List;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.jabberstudio.jso.sasl.SASLClientInfo;
import org.jabberstudio.jso.sasl.SASLMechanism;
import org.jabberstudio.jso.sasl.SASLMechanismManager;
import org.jabberstudio.jso.sasl.SASLPacket;
import org.netbeans.lib.collab.SASLClientProvider;
import org.netbeans.lib.collab.SASLData;
import org.netbeans.lib.collab.SASLProviderException;

/**
 * Implementation which delegates to JSO (native) providers for SASL.
 *
 * @author Mridul Muralidharan
 */
public class JSOSASLProvider implements NativeSASLClientProvider {
    private String mechanism;
    private String loginName;
    private String password;
    private String server;
    private SASLMechanism saslMechanism;
    
    /** Creates a new instance of JSOSASLProvider */
    public JSOSASLProvider(String mechanism) {
        this.mechanism = mechanism;
    }

    public void setLoginName(String loginName){
        this.loginName = loginName;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public void setServer(String server){
        this.server = server;
    }
    
    protected synchronized void initialise(){
        if (null == saslMechanism){
            
            CallbackHandler cbh = new CallbackHandler(){
                public void handle(Callback[] callbacks) 
                    throws java.io.IOException, UnsupportedCallbackException{
                    int count = 0;

                    while (count < callbacks.length){
                        Callback cb = callbacks[count];
                        count ++;
                        if (cb instanceof NameCallback){
                            ((NameCallback)cb).setName(loginName);
                        }
                        if (cb instanceof PasswordCallback){
                            ((PasswordCallback)cb).setPassword(password.toCharArray());
                        }
                    }
                }
            };
            
            SASLClientInfo client = new SASLClientInfo();
            List tlist = new LinkedList();
            tlist.add(mechanism);
            client.setMechanismNames(tlist);
            client.setCallbackHandler(cbh);
            client.setServer(server);
            
            saslMechanism = SASLMechanismManager.getInstance().
                    createClientMechanism(client);

            if (null == saslMechanism){
                // should not happen ....
                throw new UnsupportedOperationException("SASL Mechanism : " + 
                        mechanism + " does not seem to be supported");
            }
        }
    }
    
    public void init() throws SASLProviderException{
        loginName = null;
        password = null;
        server = null;
        saslMechanism = null;
    }

    public void close() {
        loginName = null;
        password = null;
        server = null;
        saslMechanism = null;
    }

    public void process(SASLData data) throws SASLProviderException{
        throw new SASLProviderException("unsupported method");
    }
    
    public Object process(Object packet) throws SASLProviderException{
        try{
            initialise();

            return saslMechanism.evaluate((SASLPacket)packet);
        }catch(Exception ex){
            throw new SASLProviderException("Exception evaluating sasl challenge by jso" , ex);
        }
    }
}
