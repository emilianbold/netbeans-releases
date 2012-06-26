/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.c2c.api;

import com.tasktop.c2c.client.commons.client.CredentialsInjector;
import com.tasktop.c2c.server.profile.service.ActivityServiceClient;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.mylyn.commons.net.IProxyProvider;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.commons.net.AuthenticatedProxy;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NetworkSettings;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ondra
 */
public final class ClientFactory {

    private static ClientFactory instance;
    private ClassPathXmlApplicationContext appContext;
    
    private ClientFactory () {
        
    }

    public static synchronized ClientFactory getInstance () {
        if (instance == null) {
            instance = new ClientFactory();
        }
        return instance;
    }
    
    public CloudClient getClient (String url, PasswordAuthentication auth) {
        WebLocation location = new WebLocation(url, 
                auth.getUserName(), 
                auth.getPassword() == null ? "" : new String(auth.getPassword()), 
                new ClientFactory.ProxyProvider());
        ClassPathXmlApplicationContext context = getContext();
        ProfileWebServiceClient profileClient = context.getBean(ProfileWebServiceClient.class);
        ActivityServiceClient activityClient = context.getBean(ActivityServiceClient.class);
        CredentialsInjector.configureRestTemplate(location, (RestTemplate) context.getBean(RestTemplate.class));
        return new CloudClient(profileClient, activityClient, location);
    }
    
    private static ClassPathXmlApplicationContext createContext(String[] resourceNames, ClassLoader classLoader) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.setClassLoader(classLoader);
        context.setConfigLocations(resourceNames);
        context.refresh();
        return context;
    }

    private ClassPathXmlApplicationContext getContext () {
        if (appContext == null) {
            appContext = createContext(new String[] { 
                "org/netbeans/modules/team/c2c/applicationContext-activityServiceForClient.xml", 
                "org/netbeans/modules/team/c2c/applicationContext-profileServiceForClient.xml" }, Thread.currentThread().getContextClassLoader());
        }
        return appContext;
    }
    
    private static class ProxyProvider implements IProxyProvider {

	@Override
	public Proxy getProxyForHost(String host, String proxyType) {
            try {
                String scheme = null;
                if (IProxyData.HTTPS_PROXY_TYPE.equals(proxyType)) {
                    scheme = "https://"; //NOI18N
                } else if (IProxyData.HTTP_PROXY_TYPE.equals(proxyType)) {
                    scheme = "http://"; //NOI18N
                }
                if (scheme != null) {
                    URI uri = new URI(scheme + host);
                    List<Proxy> select = ProxySelector.getDefault().select(uri);
                    if (select.size() > 0) {
                        Proxy p = select.get(0);
                        String uname = NetworkSettings.getAuthenticationUsername(uri);
                        if (uname != null && !uname.trim().isEmpty()) {
                            String pwdkey = NetworkSettings.getKeyForAuthenticationPassword(uri);
                            char[] pwd = null;
                            if (pwdkey != null && !pwdkey.trim().isEmpty()) {
                                pwd = Keyring.read(pwdkey);
                            }
                            if (pwd != null) {
                                p = new AuthenticatedProxy(p.type(), p.address(), uname, new String(pwd));
                                Arrays.fill(pwd, (char) 0);
                            }
                        }
                        return p;
                    }
                }
            } catch (URISyntaxException ex) {
            }
            return Proxy.NO_PROXY;
	}
    }
    
}
