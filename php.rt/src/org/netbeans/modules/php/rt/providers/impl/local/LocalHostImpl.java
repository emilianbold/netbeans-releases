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

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.openide.util.NbBundle;


/**
 * TODO: web and php config files are temporarily stored into LocalHostImpl.
 * Move into ancestor.
 * 
 * @author ads
 *
 */
public class LocalHostImpl extends HostImpl {
    
    public static final String DOCUMENT_PATH   = "document-path";      // NOI18N
    public static final String WEB_CONFIG_FILE   = "web-config-file";      // NOI18N
    public static final String PHP_CONFIG_FILE   = "php-config-file";      // NOI18N
    
    private static final String MSG_NOT_CONFIGURED_FILE = "MSG_NotConfiguredFile"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(LocalHostImpl.class.getName());
    
    public LocalHostImpl( String name, LocalServerProvider provider ){
        super(name, provider);
    }
    
    public LocalHostImpl(String name, String domain, String port, String baseDir, 
            LocalServerProvider provider) 
    {
        super(name, domain, port, baseDir, provider);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty( String key ) {
        Object obj = super.getProperty(key);
        if ( obj != null ) {
            return obj;
        }
        else if ( DOCUMENT_PATH.equals( key ) ) {
            return myDocumentPath;
        }
        else if ( WEB_CONFIG_FILE.equals( key ) ) {
            return myWebServerConfig;
        }
        else if ( PHP_CONFIG_FILE.equals( key ) ) {
            return myPhpConfig;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#putProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty( String key, Object value ) {
        super.setProperty(key, value);
        if ( DOCUMENT_PATH.equals( key )) {
            myDocumentPath = toStringValue(value);
        }
        else if ( WEB_CONFIG_FILE.equals( key )) {
            myWebServerConfig = toStringValue(value);
        }
        else if ( PHP_CONFIG_FILE.equals( key )) {
            myPhpConfig = toStringValue(value);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.HostImpl#getName()
     */
    @Override
    protected String getName() {
        return super.getName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.HostImpl#getDomain()
     */
    @Override
    protected String getDomain()
    {
        return super.getDomain();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.HostImpl#getPort()
     */
    @Override
    protected String getPort()
    {
        return super.getPort();
    }
    
    @Override
    protected String getBaseDirectory() {
        return super.getBaseDirectory();
    }
    
    
    protected String getDocumentPath() {
        return myDocumentPath;
    }
    
    private String myDocumentPath;
    
    private String myWebServerConfig;
    
    private String myPhpConfig;
    
    /**
     * contains FtpHost helper methods
     */
    public static class Helper extends HostImpl.Helper {

        public final static String FILE = "file";

        public static String noFileMessage(){
            return NbBundle.getMessage(LocalHostImpl.class, MSG_NOT_CONFIGURED_FILE);
            //return FILE+":/";
        }
        
        public static boolean isWebConfigSet(LocalHostImpl host){
            String conf = (String) host.getProperty(LocalHostImpl.WEB_CONFIG_FILE);
            return isStringValueSet(conf);
        }

        public static boolean isPhpConfigSet(LocalHostImpl host){
            String conf = (String) host.getProperty(LocalHostImpl.PHP_CONFIG_FILE);
            return isStringValueSet(conf);
        }

        public static boolean isFileReady(LocalHostImpl host){
            String path = (String) host.getProperty(LocalHostImpl.DOCUMENT_PATH);
            return isStringValueSet(path);
        }

        public static String getFileUrl(LocalHostImpl host) {
            if (!isFileReady(host)){
                return null;
            }
            
            String path = (String) host.getProperty(LocalHostImpl.DOCUMENT_PATH);

            if (path != null) {
                path = path.replace('/', File.separatorChar);
                if (path.endsWith(File.separator)) {
                    path = path.substring(0, path.length() - 1);
                }
            }
            
            return path;
        }

        public static String addSubdirectoryToPath(String parent, String child){
            File parentFile = new File (parent);
            File childFile = new File(parentFile, child);
            return childFile.getPath();
        }
        
        private static boolean isStringValueSet(String value){
            if (value == null || value.trim().length() == 0){
                return false;
            }
            return true;
        }
        
        
    }
    
}
