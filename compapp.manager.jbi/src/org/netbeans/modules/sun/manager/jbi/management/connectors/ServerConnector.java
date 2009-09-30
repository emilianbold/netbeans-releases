/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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


/*
 * Created on Dec 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.sun.manager.jbi.management.connectors;

import java.io.Serializable;


/**
 * DOCUMENT ME!
 *
 * @author Graj TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ServerConnector implements Serializable {
    /**
     * DOCUMENT ME!
     */
    String hostName;

    /**
     * DOCUMENT ME!
     */
    String port;

    /**
     * DOCUMENT ME!
     */
    String userName;

    /**
     * DOCUMENT ME!
     */
    String password;

    /**
     *
     */
    public ServerConnector(
        String hostNameParam, String portParam, String userNameParam, String passwordParam
    ) {
        this.hostName = hostNameParam;
        this.port = portParam;
        this.userName = userNameParam;
        this.password = passwordParam;
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
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
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
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
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
     *
     * @param hostName The hostName to set.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the port.
     */
    public String getPort() {
        return this.port;
    }

    /**
     * DOCUMENT ME!
     *
     * @param port The port to set.
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * DOCUMENT ME!
     */
    public void printOut() {
        System.out.println("/////////////////////////////////"); // NOI18N
        System.out.println("//  -- Server Connector    --  //"); // NOI18N
        System.out.println("/////////////////////////////////"); // NOI18N
        System.out.println("// HostName is: " + hostName); // NOI18N
        System.out.println("// Port is: " + port); // NOI18N
        System.out.println("// UserName is: " + userName); // NOI18N
        System.out.println("// Password is: " + password); // NOI18N
        System.out.println("/////////////////////////////////"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
    }
}
