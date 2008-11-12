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

package org.netbeans.modules.php.rt.providers.impl.local;
import org.netbeans.modules.php.rt.providers.impl.local.nodes.LocalServerNode;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
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
public class LocalServerProvider extends AbstractProvider<LocalHostImpl> {
    
    private static final String DESCRIPTION     = "TXT_LocalServerDescription"; // NOI18N
    
    private static final String TYPE_NAME       = "TXT_LocalServerTypeName";    // NOI18N
    
    private static final String DOCUMENT_ROOT   = "documentRoot";               // NOI18N 
    
    private static final String WEB_SERVER_CONFIG_FILE   = "webServerConfig";   // NOI18N 
    
    private static final String PHP_CONFIG_FILE   = "phpConfig";                // NOI18N 
    
    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    public LocalServerProvider(){
        myConfig = new LocalUiConfigProvider( this );
        myProjectProvider = new LocalProjectConfigProvider( this );
        myCommandProvider = new LocalCommandProvider( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getConfigProvider()
     */
    public UiConfigProvider getConfigProvider() {
        return myConfig;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getTypeName()
     */
    public String getTypeName() {
        return NbBundle.getMessage( LocalServerProvider.class, TYPE_NAME );
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
        return NbBundle.getMessage( LocalServerProvider.class, DESCRIPTION );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#createNode(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    public Node createNode( Host host ) {
        LocalHostImpl hostImpl = (LocalHostImpl)host;
        String name = hostImpl.getDisplayName();
        return new LocalServerNode( name , hostImpl ) ;
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getProjectConfigProvider()
     */
    public ProjectConfigProvider getProjectConfigProvider() {
        return myProjectProvider;
    }
    
    protected LocalHostImpl configureHost( FileObject object ){
        // check if suggested file object contains data for supported host object
        if (!isLocalHostObject(object.getAttributes())){
            return null;
        }
        
        String name = (String) object.getAttribute(NAME);
        
        String domain = (String) object.getAttribute(HOST_NAME);
        String port = (String) object.getAttribute( PORT );
        String baseDir = (String) object.getAttribute( BASE_DIRECTORY_PATH  );

        String docRoot = (String)object.getAttribute( DOCUMENT_ROOT);
        String webServerConfig = (String)object.getAttribute( WEB_SERVER_CONFIG_FILE );
        String phpConfig = (String)object.getAttribute( PHP_CONFIG_FILE );

        if ( port == null ) {
            port = HostImpl.DEFAULT_PORT;
        }

        LocalHostImpl impl = new LocalHostImpl( name, domain, port, baseDir, this );
        impl.setProperty(LocalHostImpl.DOCUMENT_PATH  , docRoot);
        impl.setProperty(LocalHostImpl.WEB_CONFIG_FILE  , webServerConfig);
        impl.setProperty(LocalHostImpl.PHP_CONFIG_FILE  , phpConfig);
        return impl;
    }
    
    @Override
    protected boolean acceptHost(FileObject fileObject, Host host) {
        if (host instanceof LocalHostImpl) {
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
    protected void addHost( LocalHostImpl host )
    {
        super.addHost(host);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#serializeAdded(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    @Override
    protected void serializeAdded( LocalHostImpl host )
    {
        serializeUpdated(host, host);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#updateHost(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    @Override
    protected void updateHost(LocalHostImpl oldHost, LocalHostImpl newHost) {
        super.updateHost(oldHost, newHost);
    }

    @Override
    protected void serializeUpdated(LocalHostImpl oldHost, LocalHostImpl newHost) {
        FileObject dir = getHostsDir();
        if (dir == null) {
            return;
        }

        String recordName = newHost.getName();
        
        String httpHostName = newHost.getDomain();
        String httpPort = newHost.getPort();
        String httpBaseDir = newHost.getBaseDirectory();
        
        String localPath = (String)newHost.getProperty( 
                LocalHostImpl.DOCUMENT_PATH );
        String webServerConfig = (String)newHost.getProperty( 
                LocalHostImpl.WEB_CONFIG_FILE );
        String phpConfig = (String)newHost.getProperty( 
                LocalHostImpl.PHP_CONFIG_FILE );
        
        FileObject hostObject = getHostObject(dir, oldHost );
        
        String fileName = FileUtil.findFreeFileName(dir, HOST, null);
        
        try {
            if (hostObject == null) {
                hostObject = dir.createData(fileName);
            }
            
            hostObject.setAttribute(NAME, recordName);
            hostObject.setAttribute(HOST_NAME, httpHostName);
            hostObject.setAttribute( BASE_DIRECTORY_PATH, httpBaseDir);
            hostObject.setAttribute( DOCUMENT_ROOT, localPath);
            hostObject.setAttribute( WEB_SERVER_CONFIG_FILE, webServerConfig);
            hostObject.setAttribute( PHP_CONFIG_FILE, phpConfig);
            if ( httpPort != null && !httpPort.equals( HostImpl.DEFAULT_PORT )) {
                hostObject.setAttribute( PORT, httpPort);
            }
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        }        
    }

    private boolean isLocalHostObject(Enumeration<String> attributes){
        if (attributes == null){
            return false;
        }
        List<String> list = Collections.list(attributes);
        return list.contains(DOCUMENT_ROOT);
    }
    
    private final LocalUiConfigProvider myConfig;
    
    private final LocalProjectConfigProvider myProjectProvider;
    
    private final LocalCommandProvider myCommandProvider;


}
