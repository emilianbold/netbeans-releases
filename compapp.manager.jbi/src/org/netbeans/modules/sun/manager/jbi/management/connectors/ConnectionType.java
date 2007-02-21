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

/**
 * Defines the type of connection to use
 * @author graj
 */
public enum ConnectionType {
    HTTP("s1ashttp"), HTTPS("s1ashttps"), JRMP("jmxrmi");

    String protocol;

    /** @param protocolString */
    private ConnectionType(String protocolString) {
        this.protocol = protocolString;
    }

    /** @return the protocol */
    public String getProtocol() {
        return protocol;
    }

    /** @return the protocol */
    public String getDescription() {
        switch (this) {
        case HTTP:
            return "Unsecure HTTP Connection";
        case HTTPS:
            return "Secure HTTP Connection";
        case JRMP:
            return "JSR-160 JRMP Connection";
        default:
            return "Unknown";
        }
    }

}
