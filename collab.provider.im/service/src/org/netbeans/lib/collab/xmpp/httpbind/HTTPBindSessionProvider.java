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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidParameterException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.RegistrationListener;
import org.netbeans.lib.collab.SecureRegistrationListener;
import org.netbeans.lib.collab.SecureSessionListener;
import org.netbeans.lib.collab.util.BufferedByteChannel;
import org.netbeans.lib.collab.util.StringUtility;
import org.netbeans.lib.collab.xmpp.XMPPSession;
import org.netbeans.lib.collab.xmpp.XMPPSessionProvider;


/**
 *
 * @author Mridul Muralidharan
 */
public class HTTPBindSessionProvider extends XMPPSessionProvider implements HTTPBindConstants{
    
    protected URL gatewayURL;
    protected String serverDomain;
    protected Map connParameters;
    
    public void setGatewayURL(String gatewayUrl) 
        throws InvalidParameterException , MalformedURLException{
        
        URL url = new URL(gatewayUrl);

        /*
         * Parse this url for obtaining the following:
         * 1) A 'plain' gateway url - with protocol , host , port only.
         * 2) Get the parameters out of the parameters.
         * 3) Get the serverDomain parameter out of the param and set it (special case).
         */

        String query = url.getQuery();

        Map parameters = parseQuery(query);

        setConnParameters(parameters);
        setServerDomain((String)parameters.get(TO_DOMAIN_PARAMETER));

        this.gatewayURL = new URL(
                url.getProtocol() , url.getHost() , url.getPort() , url.getFile());
    }
    
    public Object register(SocketChannel sch, Runnable callback) throws IOException{
        // NOOP this - we are not using NIO based event handling.
        return null;
    }

    public Object register(SocketChannel sch, Runnable callback,
			   BufferedByteChannel writes)
	throws IOException
    {
        // NOOP this - we are not using NIO based event handling.
        return null;
    }
    
    public void cancel(Object key){
        // NOOP this - we are not using NIO based event handling.
        return ;
    }

    protected XMPPSession createSession(String serviceUrl, String destination,
                String loginName, 
                String password, 
                int loginType, 
                CollaborationSessionListener collaborationSessionListener) 
                                                throws CollaborationException
    {
        try{
            setGatewayURL(serviceUrl);
        }catch(InvalidParameterException ipEx){
            throw new CollaborationException(ipEx);
        }catch(MalformedURLException mfuEx){
            throw new CollaborationException(mfuEx);
        }
        Map params = new HashMap();
        params.putAll(getConnParameters());
        params.put("com.sun.im.service.CollaborationSessionListener", 
                collaborationSessionListener);

        HTTPBindStreamSourceCreator streamSourceCreator = new HTTPBindStreamSourceCreator(
                getGatewayURL() , params , collaborationSessionListener);
        
        HTTPBindSession session = new HTTPBindSession(this, getServerDomain()//serviceUrl
			       , destination, loginName, password, loginType, 
                               collaborationSessionListener, streamSourceCreator);
        //session.setDataAvailableNotifier(streamSourceCreator);
        
        return session;
    }

    protected String getDomain(String loginName, String serviceUrl)
    {
        /*
         * If there is a domain specified in the loginName , use that
         * else , fallback on what is present in the connParams
         * else throw InvalidArgumentException();
         */
        String domain = StringUtility.getDomainFromAddress(loginName, null);

        if (null == domain) {
	    try {
		URL url = new URL(serviceUrl);
		
		/*
		 * Parse this url for obtaining the domain name
		 * this code is copied from above.  need cleanup
		 * todo
		 */
		String query = url.getQuery();
		Map parameters = parseQuery(query);
		
		domain = (String)parameters.get(TO_DOMAIN_PARAMETER);
		
	    } catch(Exception e) {
	    }
	}
        
        assert null != domain;

	return domain;
    }


    synchronized void startKeepAlive(long period) {
        // NOOP - no need for this.
        return ;
    }


    // Move to a util class - copied from org.netbeans.lib.collab.xmpp.ProxySessionProvider
    public static Map parseQuery(String query){
        
	HashMap attributes = new HashMap();
	if (query != null) {
	    for (StringTokenizer st = new StringTokenizer(query, "&"); // NOI18N
		 st.hasMoreTokens(); ) {
		String avp = st.nextToken();
		int eqi = avp.indexOf("="); // NOI18N
		if (eqi > 0 && eqi < avp.length()) {
		    try {
			String val =
			    URLDecoder.decode(avp.substring(eqi + 1),
					      "UTF-8"); // NOI18N
			attributes.put(avp.substring(0, eqi), val);
		    } catch (UnsupportedEncodingException uee) {
			// never happens with UTF8
		    }
		}
	    }
	}
        return attributes;
    }
    
    public URL getGatewayURL(){
        return gatewayURL;
    }
    
    public String getServerDomain(){
        return serverDomain;
    }

    public void setServerDomain(String serverDomain){
        this.serverDomain = serverDomain;
    }
    
    public Map getConnParameters(){
        return connParameters;
    }

    protected void setConnParameters(Map connParameters){
        this.connParameters = connParameters;
    }
}

