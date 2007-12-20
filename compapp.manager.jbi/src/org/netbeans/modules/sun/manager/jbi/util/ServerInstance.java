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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.util;

import java.io.Serializable;


/**
 * DOCUMENT ME!
 *
 * @author Graj TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
/* 

*/
public class ServerInstance implements Serializable {
    
    static final String DISPLAY_NAME = "displayName"; // NOI18N
    static final String DOMAIN = "DOMAIN"; // NOI18N
    static final String HTTP_MONITOR_ON = "HttpMonitorOn"; // NOI18N
    static final String HTTP_PORT_NUMBER = "httpportnumber"; // NOI18N
    static final String LOCATION = "LOCATION"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String URL = "url"; // NOI18N
    static final String USER_NAME = "username"; // NOI18N
    
    private String displayName;
    private String domain;
    private String httpMonitorOn;
    private String httpPortNumber;
    private String location;
    private String password;
    private String url;
    private String userName;
    private String hostName;
    private String adminPort;
    
  
    public ServerInstance() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param displayName
     * @param domain
     * @param httpMonitorOn
     * @param httpPortNumber
     * @param location
     * @param password
     * @param url
     * @param userName
     */
    public ServerInstance(String displayName, String domain, 
            String httpMonitorOn, String httpPortNumber, 
            String location, String password, 
            String url, String userName) {
        this.displayName = displayName;
        this.domain = domain;
        this.httpMonitorOn = httpMonitorOn;
        this.httpPortNumber = httpPortNumber;
        this.location = location;
        this.password = password;
        this.url = url;
        this.userName = userName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the domain.
     */
    public String getDomain() {
        return this.domain;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param domain The domain to set.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the httpMonitorOn.
     */
    public String getHttpMonitorOn() {
        return this.httpMonitorOn;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param httpMonitorOn The httpMonitorOn to set.
     */
    public void setHttpMonitorOn(String httpMonitorOn) {
        this.httpMonitorOn = httpMonitorOn;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the httpPortNumber.
     */
    public String getHttpPortNumber() {
        return this.httpPortNumber;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param httpPortNumber The httpPortNumber to set.
     */
    public void setHttpPortNumber(String httpPortNumber) {
        this.httpPortNumber = httpPortNumber;
    }
    
    /**
     * Gets local domain's parent location. For remote domain, this location
     * is empty.
     *
     * @return Returns the location.
     */
    public String getLocation() {
        return this.location;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the url.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * Gets the location part from the URL. For remote server, the location 
     * is undefined. The location part from the URL gives the location of 
     * local server installation.
     */
    public String getUrlLocation() {
        String url = getUrl();
        int index = url.indexOf(']');   // NOI18N
        if (index != -1) {
            return url.substring(1, index);
        } else {
            return null;    // unconfigured Tomcat, for example
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
        
        String[] separator = this.url.split(":"); // NOI18N
        
        int k = separator.length -1;
        
        /* NB5.0 Format changed...
           [C:\alaska\root\jbi\runtime\Sun\AppServer]deployer:Sun:AppServer::localhost:4848
           [/home/tli/SUNWappserver]deployer:Sun:AppServer::localhost:24848"
         */
        this.hostName = separator[k-1];
        this.adminPort = separator[k];
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the adminPort.
     */
    public String getAdminPort() {
        return this.adminPort;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the hostName.
     */
    public String getHostName() {
        return this.hostName;
    }
        
    /**
     * DOCUMENT ME!
     */
    public void printOut() {
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
        System.out.println("//   -- Instance values --"); // NOI18N
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
        System.out.println("// displayName is :" + this.displayName); // NOI18N
        System.out.println("// domain is :" + this.domain); // NOI18N
        System.out.println("// httpMonitorOn is :" + this.httpMonitorOn); // NOI18N
        System.out.println("// httpPortNumber is :" + this.httpPortNumber); // NOI18N
        System.out.println("// location is :" + this.location); // NOI18N
        System.out.println("// password is :" + this.password); // NOI18N
        System.out.println("// url is :" + this.url); // NOI18N
        System.out.println("// userName is :" + this.userName); // NOI18N
        System.out.println("// hostName is :" + this.hostName); // NOI18N
        System.out.println("// adminPort is :" + this.adminPort); // NOI18N
        System.out.println("/////////////////////////////////////////////////"); // NOI18N
    }
}
