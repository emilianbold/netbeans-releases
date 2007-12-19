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
package org.netbeans.modules.php.rt.providers.impl.local.apache;

/**
 * @author ads
 *
 */
class HttpdHost implements Comparable {

    private static final String DEFAULT_NAME = "localhost"; // NOI18N
    private static final String DEFAULT_PORT = "80"; // NOI18N

    public HttpdHost(String path, String platformConfigPath) {
        this(null, path, platformConfigPath);
    }

    public HttpdHost(String hostName, String path, String platformConfigPath) {
        this(hostName, null, path, platformConfigPath);
    }

    public HttpdHost(String hostName, String hostPort, String path, String platformConfigPath) {
        myHostName = hostName == null ? DEFAULT_NAME : hostName;
        myHostPort = hostPort == null ? DEFAULT_PORT : hostPort;
        myHostPath = path;
        myPlatformConfigPath = platformConfigPath;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (myHostPort.equals(DEFAULT_PORT)) {
            return myHostName + " [" + myHostPath + "]";
        } else {
            return myHostName + ":" + myHostPort + " [" + myHostPath + "]";
        }
    }

    public String getName() {
        return myHostName;
    }

    public String getPort() {
        return myHostPort;
    }

    public String getPath() {
        return myHostPath;
    }

    public String getPlarformPath() {
        return myPlatformConfigPath;
    }

    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HttpdHost)) {
            return false;
        }
        HttpdHost host = (HttpdHost) obj;
        return equal(myHostName, host.myHostName) 
                && equal(myHostPort, host.myHostPort) 
                && equal(myHostPath, host.myHostPath) 
                && equal(myPlatformConfigPath, host.myPlatformConfigPath);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = myHostName == null ? 0 : myHostName.hashCode();
        int pathHash = myHostPath == null ? 0 : myHostPath.hashCode();
        /*
         *  skip myPlatformConfigPath hash becuase there is just minor possiblity to have
         *  two hosts with equal host name and document root and different platform locations.
         */
        return hash * 37 + pathHash;
    }

    private boolean equal(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }
    private String myHostName;
    private String myHostPort;
    private String myHostPath;
    private String myPlatformConfigPath;
}