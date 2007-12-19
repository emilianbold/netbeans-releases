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
package org.netbeans.modules.php.rt.providers.impl;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public abstract class HostImpl implements Host {

    public static final String DEFAULT_PORT = "80";         // NOI18N
    // server record name

    public static final String NAME = "name";     // NOI18N
    // http server name

    public static final String DOMAIN = "domain";     // NOI18N
    // http server port

    public static final String PORT = "port";     // NOI18N
    // base orl of http server

    public static final String BASE_DIRECTORY_PATH = "base-directory-path"; // NOI18N

    private static final String MSG_NOT_CONFIGURED_HTTP = "MSG_NotConfiguredHttp"; // NOI18N

    public HostImpl(String name, AbstractProvider provider) {
        this(name, null, null, null, provider);
    }

    public HostImpl(String name, String domain, String port, String baseDir,
            AbstractProvider provider) {
        assert name != null;
        myName = name;
        setProperty(DOMAIN, domain);
        setProperty(PORT, port);
        setProperty(BASE_DIRECTORY_PATH, baseDir);
        myProvider = provider;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#getProvider()
     */
    public AbstractProvider getProvider() {
        return myProvider;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HostImpl)) {
            return false;
        }
        HostImpl impl = (HostImpl) obj;
        boolean idEq = getName().equals(impl.getName());
        if (!idEq){
            return false;
        }
        /*
        boolean domainEq = getDomain().equals( impl.getDomain() );
        if ( !domainEq ){
            return false;
        }
        return getPort().equals( impl.getPort() );
         */
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    //return getDomain().hashCode()*37 + getPort().hashCode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#getProperty(java.lang.String)
     */
    public Object getProperty(String key) {
        if (NAME.equals(key)) {
            return myName;
        } else if (PORT.equals(key)) {
            return myPort;
        } else if (DOMAIN.equals(key)) {
            return myDomain;
        } else if (BASE_DIRECTORY_PATH.equals(key)) {
            return myBaseDirectory;
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Host#putProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String key, Object value) {
        /*
         * Prevent resetting main properties of this class.
         * One need to reisntatiate class instead of changing these props.
         */
        assert !NAME.equals(key) : "Property with key " + key + // NOI18N
                " is not mutable. You need to reinstatiate this class"; // NOI18N
        if (DOMAIN.equals(key)) {
            myDomain = toStringValue(value);
        } else if (PORT.equals(key)) {
            myPort = toStringValue(value);
        } else if (BASE_DIRECTORY_PATH.equals(key)) {
            myBaseDirectory = toStringValue(value);
        }
    }

    public void remove() {
        AbstractProvider provider = getProvider();
        provider.remove(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDisplayName();
    }

    public String getId() {
        return getName();
    }

    public String getDisplayName() {
        return getName();
    }

    public String getServerName() {
            if (getPort().equals(DEFAULT_PORT)) {
                return getDomain();
            } else {
                return getDomain() + ":" + getPort();     // NOI18N
            }
    }

    protected String toStringValue(Object value){
        if (value == null){
            return "";
        }
        assert value instanceof String;
        return (String) value;
    }

    protected char[] toCharArrValue(Object value){
        if (value == null){
            return "".toCharArray();
        }
        assert value instanceof char[];
        return (char[]) value;
    }

    protected String getName() {
        return myName;
    }

    protected String getDomain() {
        return myDomain;
    }

    protected String getPort() {
        if (myPort == null || myPort.trim().length() == 0) {
            return DEFAULT_PORT;
        }
        return myPort;
    }

    protected String getBaseDirectory() {
        return myBaseDirectory;
    }

    private String myName;
    private String myDomain;
    private String myPort;
    private String myBaseDirectory;
    private AbstractProvider myProvider;

    /**
     * contains Host helper methods
     */
    public static class Helper {

        public final static String HTTP = "http";
        
        public static String noHttpMessage(){
            return NbBundle.getMessage(HostImpl.class, MSG_NOT_CONFIGURED_HTTP);
            //return HTTP+"://";
        }

        public static boolean isHttpReady(HostImpl host){
            String domain = host.getDomain();
            if (domain == null || domain.trim().length() == 0){
                return false;
            }
            return true;
        }
        
        public static String getHttpUrl(HostImpl host) {
            if (!isHttpReady(host)){
                return null;
            }
            
            String httpUrlResult = null;

            String domain = host.getDomain();
            int port = Integer.parseInt(host.getPort());
            String baseDirectoryPath = (String) host.getProperty(BASE_DIRECTORY_PATH);

            if (    baseDirectoryPath != null 
                    && !baseDirectoryPath.startsWith("/")) 
            {
                baseDirectoryPath = "/" + baseDirectoryPath;
            }

            URL httpUrl = null;
            try {
                httpUrl = new URL(HTTP, domain, port, baseDirectoryPath);
                httpUrlResult = httpUrl.toString();
            } catch (MalformedURLException ex) {
                httpUrlResult = null;
            } catch (Exception e) {
                httpUrlResult = null;
            }
            return httpUrlResult;
        }

        /**
        * joins base url with additional subdirectory path.
        * e.g. 
        * <pre>
        * 'http://server/project' with 'subproject'
        * </pre>
        * result is 
        * <pre>
        * 'http://server/project/subproject'
        * </pre>
        */
        public static String addSubdirectoryToUrl(String baseUrl,
                String subdir) throws MalformedURLException {
            if (subdir == null) {
                return baseUrl;
            }

            String context = subdir;
            if (!context.startsWith("/")) {
                context = "/" + context;
            }

            URL rootUrl = new URL(baseUrl);
            String rootPath = rootUrl.getPath();
            // prevent double slash on rootPath and context connection
            if (rootPath.endsWith("/") && context.startsWith("/")) {
                rootPath = rootPath.substring(0, rootPath.length() - 1);
            }

            URL resultUrl = new URL(rootUrl, rootPath + context);

            return resultUrl.toString();
        }
    }
}
