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
 * MQAddress.java
 *
 * Created on December 14, 2006, 12:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.mq;

/**
 *
 * @author rchen
 */
public interface MQAddress extends MQComponent {
    
    public static final String ATTR_QUEUEMANAGERNAME = "queueManagerName";
    public static final String ATTR_HOSTNAME = "hostName";
    public static final String ATTR_PORT = "portNumber";
    public static final String ATTR_CHANNEL = "channelName";
    public static final String ATTR_CIPHERSUITE = "cipherSuite";
    public static final String ATTR_SSLPEERNAME = "sslPeerName";


    public String getQueueManagerName();
    public void setQueueManagerName(String val);
    
    public String getHostName();
    public void setHostName(String val);
    
    public String getPortNumber();
    public void setPortNumber(String val);
    
    public String getChannelName();
    public void setChannelName(String val);
    
    String getCipherSuite();
    void setCipherSuite(String cipherSuite);
    
    String getSslPeerName();
    void setSslPeerName(String peerName);
}
