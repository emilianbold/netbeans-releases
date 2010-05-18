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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.eclipse.core.net.proxy;

/**
 * @author Maros Sandor
 */
public interface IProxyData {
    /**
     * Type constant (value "HTTP") which identifies an HTTP proxy.
     * @see #getType()
     */
    public static final String HTTP_PROXY_TYPE = "HTTP"; //$NON-NLS-1$
	
    /**
     * Type constant (value "HTTPS") which identifies an HTTPS proxy.
     * @see #getType()
     */
    public static final String HTTPS_PROXY_TYPE = "HTTPS"; //$NON-NLS-1$
	
    /**
     * Type constant (value "SOCKS") which identifies an SOCKS proxy.
     * @see #getType()
     */
    public static final String SOCKS_PROXY_TYPE = "SOCKS"; //$NON-NLS-1$
	
    /**
     * Return the type of this proxy. Additional proxy types may be
     * added in the future so clients should accommodate this.
     * @return the type of this proxy
     * @see #HTTP_PROXY_TYPE
     * @see #HTTPS_PROXY_TYPE
     * @see #SOCKS_PROXY_TYPE
     */
    String getType();
	
    /**
     * Return the host name for the proxy server or <code>null</code>
     * if a proxy server of this type is not available.
     * @return the host name for the proxy server or <code>null</code>
     */
    String getHost();
	
    /**
     * Set the host name for the proxy server of this type.
     * If no proxy server of this type is available, the host name should
     * be set to <code>null</code>.
     * <p>
     * Setting this value will not affect the data returned from {@link IProxyService#getProxyData()}.
     * Clients can change the global settings by changing the proxy data instances and then
     * by calling {@link IProxyService#setProxyData(IProxyData[])} with the adjusted data.
     * @param host the host name for the proxy server or <code>null</code>
     */
    void setHost(String host);
	
    /**
     * Return the port that should be used when connecting to the host or -1
     * if the default port for the proxy protocol should be used.
     * @return the port that should be used when connecting to the host
     */
    int getPort();
	
    /**
     * Set the port that should be used when connecting to the host. Setting the port 
     * to a value of -1 will indicate that the default port number for
     * the proxy type should be used.
     * <p>
     * Setting this value will not affect the data returned from {@link IProxyService#getProxyData()}.
     * Clients can change the global settings by changing the proxy data instances and then
     * by calling {@link IProxyService#setProxyData(IProxyData[])} with the adjusted data.
     * @param port the port that should be used when connecting to the host
     * 	or -1 if the default port is to be used
     */
    void setPort(int port);
	
    /**
     * Return the id of the user that should be used when authenticating 
     * for the proxy. A <code>null</code> is returned if there is no
     * authentication information.
     * @return the id of the user that should be used when authenticating 
     * for the proxy or <code>null</code>
     */
    String getUserId();
	
    /**
     * Set the id of the user that should be used when authenticating 
     * for the proxy. A <code>null</code> should be used if there is no
     * authentication information.
     * <p>
     * Setting this value will not affect the data returned from {@link IProxyService#getProxyData()}.
     * Clients can change the global settings by changing the proxy data instances and then
     * by calling {@link IProxyService#setProxyData(IProxyData[])} with the adjusted data.
     * @param userid the id of the user that should be used when authenticating 
     * for the proxy or <code>null</code>
     */
    void setUserid(String userid);
	
    /**
     * Return the password that should be used when authenticating 
     * for the proxy. A <code>null</code> is returned if there is no
     * password or the password is not known.
     * @return the password that should be used when authenticating 
     * for the proxy or <code>null</code>
     */
    String getPassword();
	
    /**
     * Set the password that should be used when authenticating 
     * for the proxy. A <code>null</code> should be passed if there is no
     * password or the password is not known.
     * <p>
     * Setting this value will not affect the data returned from {@link IProxyService#getProxyData()}.
     * Clients can change the global settings by changing the proxy data instances and then
     * by calling {@link IProxyService#setProxyData(IProxyData[])} with the adjusted data.
     * @param password the password that should be used when authenticating 
     * for the proxy or <code>null</code>
     */
    void setPassword(String password);
	
    /**
     * Returns whether the proxy requires authentication. If the proxy
     * requires authentication but the user name and password fields of
     * this proxy data are null, the client can expect the connection
     * to fail unless they somehow obtain the authentication information.
     * @return whether the proxy requires authentication
     */
    boolean isRequiresAuthentication();

    /**
     * Set the values of this data to represent a disabling of its type.
     * Note that the proxy type will not be disabled unless the client
     * calls {@link IProxyService#setProxyData(IProxyData[])} with the
     * disabled data as a parameter. A proxy data can be enabled by setting
     * the host.
     */
    void disable();
    
}
