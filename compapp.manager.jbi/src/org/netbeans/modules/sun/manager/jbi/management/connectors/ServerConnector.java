/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
