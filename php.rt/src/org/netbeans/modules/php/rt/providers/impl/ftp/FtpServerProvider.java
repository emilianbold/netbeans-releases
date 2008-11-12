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
package org.netbeans.modules.php.rt.providers.impl.ftp;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.nodes.FtpServerNode;
import org.netbeans.modules.php.rt.spi.providers.CommandProvider;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.UiConfigProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.rt.spi.providers.WebServerProvider.class)
public class FtpServerProvider extends AbstractProvider<FtpHostImpl> {

    private static final String DESCRIPTION = "TXT_FtpServerDescription"; // NOI18N
    private static final String TYPE_NAME = "TXT_FtpServerTypeName"; // NOI18N
    
    public static final String FTP_SERVER = "ftpServer"; // NOI18N
    public static final String FTP_USER_NAME = "ftpUserName"; // NOI18N
    public static final String FTP_PASSWORD = "ftpPassword"; // NOI18N
    public static final String FTP_DIRECTORY = "ftpDirectory"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    public FtpServerProvider() {
        myConfig = new FtpUiConfigProvider(this);
        myProjectConfig = new FtpProjectConfigProvider(this);
        myCommandProvider = new FtpCommandProvider(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getConfigProvider()
     */
    public UiConfigProvider getConfigProvider() {
        return myConfig;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getProjectConfigProvider()
     */
    public ProjectConfigProvider getProjectConfigProvider() {
        return myProjectConfig;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getTypeName()
     */
    public String getTypeName() {
        return NbBundle.getMessage(FtpServerProvider.class, TYPE_NAME);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getCommandProvider()
     */
    public CommandProvider getCommandProvider() {
        return myCommandProvider;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getDescription()
     */
    public String getDescription() {
        return NbBundle.getMessage(FtpServerProvider.class, DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#createNode(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    public Node createNode(Host host) {
        FtpHostImpl hostImpl = (FtpHostImpl) host;
        String name = hostImpl.getDisplayName();
        
        FtpServerNode rootNode = new FtpServerNode(name, hostImpl);
        return rootNode;
    }

    // TODO: is it expected check here if this provider is suitable 
    // for given host FileObject and return null if not.
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#configureHost(org.openide.filesystems.FileObject)
     */
    @Override
    protected FtpHostImpl configureHost(FileObject object) {
        // check if suggested file object contains data for supported host object
        if (!isFtpHostObject(object.getAttributes())){
            return null;
        }
        
        
        String name = (String) object.getAttribute(NAME);
        
        String domain = (String) object.getAttribute(HOST_NAME);
        String port = (String) object.getAttribute( PORT );
        String baseDir = (String) object.getAttribute( BASE_DIRECTORY_PATH  );
        
        String ftpServer = (String) object.getAttribute( FTP_SERVER);
        String ftpUserName = (String) object.getAttribute( FTP_USER_NAME);
        char[] ftpPassword = (char[]) object.getAttribute( FTP_PASSWORD);
        String ftpDirectory = (String) object.getAttribute( FTP_DIRECTORY);

        if (port == null) {
            port = HostImpl.DEFAULT_PORT;
        }

        FtpHostImpl impl = new FtpHostImpl(name, domain, port, baseDir, this);
        impl.setProperty(FtpHostImpl.FTP_SERVER, ftpServer);
        impl.setProperty(FtpHostImpl.FTP_USER_NAME, ftpUserName);
        impl.setProperty(FtpHostImpl.FTP_PASSWORD, ftpPassword);
        impl.setProperty(FtpHostImpl.FTP_DIRECTORY, ftpDirectory);

        return impl;
    }

    @Override
    protected boolean acceptHost(FileObject fileObject, Host host) {
        if (host instanceof FtpHostImpl) {
            if (!super.acceptHost(fileObject, host))
                return false;

            return true;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#addHost(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    @Override
    protected void addHost(FtpHostImpl host) {
        super.addHost(host);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#serializeAdded(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    @Override
    protected void serializeAdded(FtpHostImpl host) {
        serializeUpdated(host, host);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#updateHost(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    // access is extended to public to allow name/password saving from ftp client
    @Override
    public void updateHost(FtpHostImpl oldHost, FtpHostImpl newHost) {
        super.updateHost(oldHost, newHost);
    }

    @Override
    protected void serializeUpdated(FtpHostImpl oldHost, FtpHostImpl newHost) {
        //serializeAdded(newHost);
        FileObject dir = getHostsDir();
        if (dir == null) {
            return;
        }


        String recordName = newHost.getName();
        
        String httpHostName = newHost.getDomain();
        String httpPort = newHost.getPort();
        String httpBaseDir = newHost.getBaseDirectory();

        String ftpServer = (String) newHost.getProperty( FtpHostImpl.FTP_SERVER);
        String ftpUserName = (String) newHost.getProperty( FtpHostImpl.FTP_USER_NAME);
        char[] ftpPassword = (char[]) newHost.getProperty( FtpHostImpl.FTP_PASSWORD);
        String ftpDirectory = (String) newHost.getProperty( FtpHostImpl.FTP_DIRECTORY);

        // get oldHost FileObject to store new values
        FileObject hostObject = getHostObject(dir, oldHost);
        String fileName = FileUtil.findFreeFileName(dir, HOST, null);
        try {
            if (hostObject == null) {
                hostObject = dir.createData(fileName);
            }

            hostObject.setAttribute(NAME, recordName);
            hostObject.setAttribute(HOST_NAME, httpHostName);
            hostObject.setAttribute(BASE_DIRECTORY_PATH, httpBaseDir);
            hostObject.setAttribute(FTP_SERVER, ftpServer);
            hostObject.setAttribute(FTP_USER_NAME, ftpUserName);
            hostObject.setAttribute(FTP_PASSWORD, ftpPassword);
            hostObject.setAttribute(FTP_DIRECTORY, ftpDirectory);
            if (httpPort != null && !httpPort.equals(HostImpl.DEFAULT_PORT)) {
                hostObject.setAttribute(PORT, httpPort);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    private boolean isFtpHostObject(Enumeration<String> attributes){
        if (attributes == null){
            return false;
        }
        List<String> list = Collections.list(attributes);
        return  list.contains(FTP_DIRECTORY)
                && list.contains(FTP_SERVER)
                && list.contains(FTP_USER_NAME);
    }
    
    private FtpUiConfigProvider myConfig;

    private ProjectConfigProvider myProjectConfig;

    private FtpCommandProvider myCommandProvider;

}
