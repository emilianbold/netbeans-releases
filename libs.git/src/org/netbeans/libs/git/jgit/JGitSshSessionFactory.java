/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.jgit;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

/**
 *
 * @author ondra
 */
public class JGitSshSessionFactory extends JschConfigSessionFactory {

    private OpenSshConfig sshConfig;
    private static SshSessionFactory INSTANCE;

    public static synchronized SshSessionFactory getDefault () {
        if (INSTANCE == null) {
            INSTANCE = new JGitSshSessionFactory();
        }
        return INSTANCE;
    }
    private JSch defaultJSch;
    private final Map<String, JSch> byHostName;

    public JGitSshSessionFactory () {
        byHostName = new HashMap<String, JSch>();
    }

    @Override
    protected void configure (Host host, Session sn) {
        
    }

    @Override
    public synchronized RemoteSession getSession (URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms) throws TransportException {
        if (credentialsProvider != null) {
            String host = uri.getHost();
            CredentialItem.StringType identityFile = new JGitCredentialsProvider.IdentityFileItem("Identity file for " + host, false);
            if (credentialsProvider.isInteractive() && credentialsProvider.get(uri, identityFile) && identityFile.getValue() != null) {
                if (sshConfig == null) {
                    sshConfig = OpenSshConfig.get(fs);
                }

                final OpenSshConfig.Host hc = sshConfig.lookup(host);
                try {
                    JSch jsch = getJSch(hc, fs);
                    // remove all identity files
                    jsch.removeAllIdentity();
                    // and add the one specified by CredentialsProvider
                    jsch.addIdentity(identityFile.getValue());
                } catch (JSchException ex) {
                    throw new TransportException(uri, ex.getMessage(), ex);
                }
            }
        }
        return super.getSession(uri, credentialsProvider, fs, tms);
    }

    @Override
    protected JSch getJSch (Host hc, FS fs) throws JSchException {
        // default jsch to gain known hosts from
        if (defaultJSch == null) {
            defaultJSch = createDefaultJSch(fs);
            defaultJSch.removeAllIdentity();
        }
        String hostName = hc.getHostName();
        JSch jsch = byHostName.get(hostName);
        if (jsch == null) {
            jsch = new JSch();
            jsch.setHostKeyRepository(defaultJSch.getHostKeyRepository());
            byHostName.put(hostName, jsch);
        }
        return jsch;
    }

    @Override
    protected Session createSession (Host hc, String user, String host, int port, FS fs) throws JSchException {
        Session session = super.createSession(hc, user, host, port, fs);
        try {
            List<Proxy> proxies = ProxySelector.getDefault().select(new URI("ssh",
                    null,
                    host,
                    port == -1 ? 22 : port,
                    null, null, null));
            if (proxies.size() > 0) {
                Proxy p = proxies.iterator().next();
                if (p.type() == Proxy.Type.DIRECT) {
                    session.setProxy(null);
                } else {
                    SocketAddress addr = p.address();
                    if (addr instanceof InetSocketAddress) {
                        InetSocketAddress inetAddr = (InetSocketAddress) addr;
                        String proxyHost = inetAddr.getHostName();
                        int proxyPort = inetAddr.getPort();
                        session.setProxy(new ProxyHTTP(proxyHost, proxyPort));
                    }
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(JGitSshSessionFactory.class.getName()).log(Level.INFO, "Invalid URI: " + host + ":" + port, ex);
        }
        return session;
    }
    
}
