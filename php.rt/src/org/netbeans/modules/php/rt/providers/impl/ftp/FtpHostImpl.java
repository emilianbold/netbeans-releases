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

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class FtpHostImpl extends HostImpl {

    public static final String FTP_SERVER = "ftp-server"; // NOI18N
    public static final String FTP_USER_NAME = "ftp-user-name"; // NOI18N
    public static final String FTP_PASSWORD = "ftp-password"; // NOI18N
    public static final String FTP_DIRECTORY = "ftp-directory"; // NOI18N

    private static final String MSG_NOT_CONFIGURED_FTP = "MSG_NotConfiguredFtp"; // NOI18N

    FtpHostImpl( String name, FtpServerProvider provider ){
        super(name, provider);
    }
    
    FtpHostImpl(String name, String domain, String port, String baseDir, 
            FtpServerProvider provider) 
    {
        super(name, domain, port, baseDir, provider);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        Object obj = super.getProperty(key);
        if (obj != null) {
            return obj;
        } else if (FTP_SERVER.equals(key)) {
            return myFtpServer;
        } else if (FTP_USER_NAME.equals(key)) {
            return myFtpUserName;
        } else if (FTP_PASSWORD.equals(key)) {
            return myFtpPassword;
        } else if (FTP_DIRECTORY.equals(key)) {
            return myFtpDirectory;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#putProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String key, Object value) {
        super.setProperty(key, value);
        if (FTP_SERVER.equals(key)) {
            myFtpServer = toStringValue(value);
        } else if (FTP_USER_NAME.equals(key)) {
            myFtpUserName = toStringValue(value);
        } else if (FTP_PASSWORD.equals(key)) {
            myFtpPassword = toCharArrValue(value);
        } else if (FTP_DIRECTORY.equals(key)) {
            myFtpDirectory = toStringValue(value);
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
    protected String getDomain() {
        return super.getDomain();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.HostImpl#getPort()
     */
    @Override
    protected String getPort() {
        return super.getPort();
    }

    @Override
    protected String getBaseDirectory() {
        return super.getBaseDirectory();
    }

    protected String getFtpServer() {
        return myFtpServer;
    }

    private String myFtpServer;

    private String myFtpUserName;
    // password is stored as char[] because of security reasons suggested in Java 2 platform v1.2
    private char[] myFtpPassword;

    private String myFtpDirectory;
    
    /**
     * contains FtpHost helper methods
     */
    public static class Helper extends HostImpl.Helper {

        public final static String FTP = "ftp";

        public static String noFtpMessage(){
            return NbBundle.getMessage(FtpHostImpl.class, MSG_NOT_CONFIGURED_FTP);
            //return FTP+"://";
        }
        
        public static boolean isFtpReady(FtpHostImpl host){
            String ftpServer = (String) host.getProperty(FTP_SERVER);
            if (ftpServer == null || ftpServer.trim().length() == 0){
                return false;
            }
            return true;
        }

        public static String getFtpUrl(FtpHostImpl host) {
            if (!isFtpReady(host)){
                return null;
            }
            
            String ftpUrlResult = null;

            String ftpServer = (String) host.getProperty(FTP_SERVER);
            //String ftpUserName = (String) getProperty(FTP_USER_NAME);
            String ftpDirectory = (String) host.getProperty(FTP_DIRECTORY);

            if (    ftpDirectory != null
                    && !ftpDirectory.startsWith("/")) 
            {
                ftpDirectory = "/" + ftpDirectory;
            }


            URL ftpUrl = null;
            try {
                ftpUrl = new URL(FTP, ftpServer, ftpDirectory);
                ftpUrlResult = ftpUrl.toString();
            } catch (MalformedURLException ex) {
                ftpUrlResult = null;
            } catch (Exception e) {
                ftpUrlResult = null;
            }
            return ftpUrlResult;
        }

        public static String getFtpUrlWithSubdir(FtpHostImpl host,
                String subdirectory) throws MalformedURLException 
        {

            return addSubdirectoryToUrl(getFtpUrl(host), subdirectory);
        }
        
       /**
        * returns ftp initial directory specified in host 
        * with added subdirectory path
        */
        public static String getFtpInitialDirWithSubdir(
                FtpHostImpl host, String subdirectory) 
        {
            String rootPath = getFtpInitialDir(host);
            if (rootPath != null) {

                String contextPath = subdirectory != null ? subdirectory : "";
                if (!contextPath.startsWith("/")) {
                    contextPath = "/" + contextPath;
                }

                if (rootPath.endsWith("/") && contextPath.startsWith("/")) {
                    rootPath = rootPath.substring(0, rootPath.length() - 1);
                }
                return rootPath + contextPath;
            }
            return null;
        }
        
       /**
        * returns ftp initial directory specified in host 
        * with added subdirectory path
        */
        public static String getFtpInitialDir(FtpHostImpl host) {
            String rootPath = (String) host.getProperty( FTP_DIRECTORY );
            if (rootPath != null) {
                if (!rootPath.startsWith("/")) {
                    rootPath = "/" + rootPath;
                }
                return rootPath;
            }
            return null;
        }
        
        /*
        public static String getHttpUrlWithSubdir(FtpHostImpl host,
               String subdirectory) throws MalformedURLException
        {
            return addSubdirectoryToUrl(getHttpUrl(host), subdirectory);
        }
         */
        
        
    }
}
