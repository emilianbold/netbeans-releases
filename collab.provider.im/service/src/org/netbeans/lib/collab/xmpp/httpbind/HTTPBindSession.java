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
package org.netbeans.lib.collab.xmpp.httpbind;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.xmpp.StreamSourceCreator;
import org.netbeans.lib.collab.xmpp.XMPPRegistrationListenerWrapper;
import org.netbeans.lib.collab.xmpp.XMPPSession;
import org.netbeans.lib.collab.xmpp.XMPPSessionProvider;

/**
 *
 * @author Mridul Muralidharan
 */
public class HTTPBindSession extends XMPPSession implements 
        HTTPBindConstants , NegotiationConstants{
    
    private DataAvailableNotifier dataAvailableNotifier;
    
    /**
     * Creates a new instance of HTTPBindSession 
     */
    protected HTTPBindSession() {
    }
    
    public HTTPBindSession(XMPPSessionProvider fac,
                       String serviceUrl,
                       String destination,
                       String loginName,
                       String password,
                       int loginType,
                       CollaborationSessionListener listener,
                       StreamSourceCreator streamSrcCreator)
                       throws CollaborationException
    {
        super(fac, serviceUrl, destination, 
              loginName, password, loginType, listener,
              streamSrcCreator);
    }
    
    public void setDataAvailableNotifier(DataAvailableNotifier dataAvailableNotifier){
        this.dataAvailableNotifier = dataAvailableNotifier;
    }

    public DataAvailableNotifier getDataAvailableNotifier(){
        return dataAvailableNotifier;
    }
    
    protected void registerImpl(Runnable callback) throws IOException{
        // Register the runnable with the JEP124StreamSourceCreator
        synchronized(this){
            if (null == getDataAvailableNotifier()){
                StreamSourceCreator creator = getStreamSourceCreator();

                if (creator instanceof DataAvailableNotifier){
                    // Check for instanceof
                    setDataAvailableNotifier((DataAvailableNotifier)creator);
                }
                // else - NPE !
            }
            getDataAvailableNotifier().addInputDataListeners(callback);
        }
    }
        
    protected HTTPBindStreamSourceCreator getHTTPStreamSourceCreator(){
        assert (getStreamSourceCreator() 
            instanceof HTTPBindStreamSourceCreator);
        return (HTTPBindStreamSourceCreator)getStreamSourceCreator();
    }
    
    
    protected void configureTimeouts(){
        super.configureTimeouts();
        String waittime = (String)getHTTPStreamSourceCreator().getNegotiatedParameters()
            .get(NegotiationConstants.WAIT_REQ_PARAM);
        String pollingtime = (String)getHTTPStreamSourceCreator().getNegotiatedParameters()
            .get(NegotiationConstants.POLLING_REQ_PARAM);
        int wtime = 0;
        int ptime = 0;

        if (null != waittime){
            try{
                wtime = Integer.parseInt(waittime);
            }catch(NumberFormatException nfEx){
                // log it ?
            }
        }
        if (null != pollingtime){
            try{
                ptime = Integer.parseInt(pollingtime);
            }catch(NumberFormatException nfEx){
                // log it ?
            }
        }

        setRequestTimeout(Math.max(getRequestTimeout() , wtime * 1000) + ptime * 1000);
        setShortRequestTimeout(getShortRequestTimeout() + ptime * 1000);
        setFeatureTimeout(getFeatureTimeout() + ptime * 1000);
    }
    

    protected int getConnectParameters(String serviceUrl , StringBuffer hostSb , 
            StringBuffer domainSb) throws CollaborationException{
        
        String domain = null;
        String host = null;

        HTTPBindStreamSourceCreator creator = getHTTPStreamSourceCreator();

        Map params = creator.getConnParams();
        domain = (String)params.get(TO_DOMAIN_PARAMETER);
        host = (String)params.get(ROUTE_PARAMETER);
        if (null == host){
            host = domain;
        }

        if (null == domain){
            throw new CollaborationException(
                    "Invalid service url , target domain not specified : " +  // NOI18N
                        serviceUrl);
        }

        // We are always provided a domain , so dont bother with validateDomain.
        //domain = validateDomain(domain);
        
        hostSb.setLength(0);
        hostSb.append(host);
        domainSb.setLength(0);
        domainSb.append(domain);
        // immaterial
        return 5222;
    }


    protected void setRedirectedHost(String host){
        getHTTPStreamSourceCreator().setParameter(ROUTE_PARAMETER , "xmpp:" + host);
        super.setRedirectedHost(host);
    }

    /*
    public void register(String serviceURL, 
            XMPPRegistrationListenerWrapper listener) throws CollaborationException {
        try{
            URL url = new URL(serviceURL);
            Map params = HTTPBindSessionProvider.parseQuery(url.getQuery());
            
            String route = (String)params.get(ROUTE_PARAMETER);
            
            if (null != route){
                serviceURL = route;
            }
            else{
                String domain = (String)params.get(TO_DOMAIN_PARAMETER);

                if (null != domain){
                    serviceURL = domain;
                }
            }
        }catch(Exception ex){}
        super.register(serviceURL , listener);
    }
     */

    protected void startTLSImpl(){
        // We should not be calling this !
        XMPPSessionProvider.info("Trying to do TLS for httpbind , ignoring.");
    }

    protected void enableAuthLegacyMode(){
        // disable legacy mode for httpbind
    }
    protected boolean isAuthLegacyModeEnabled(){
        // disable legacy mode for httpbind
        return false;
    }
    protected void disableAuthLegacyMode(){
        // disable legacy mode for httpbind
    }

    protected void sendKeepAlive() {
	    // noop
    }
}

