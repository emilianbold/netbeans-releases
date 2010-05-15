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

package org.netbeans.modules.wsdlextensions.sap;

/**
 *
 * Represents the client address element under the SAP address element
 * <sap:clientparams useLoadBalancing="No" applicationServerGroup="appServGroup" messageServerHostname="msgServHostName"/>
 *
 * @author Sun Microsystems
*/
public interface SAPAddressClient extends SAPComponent {

    public static final String SAPADDRCLIENT_USELOADBAL = "useLoadBalancing";
    public static final String SAPADDRCLIENT_APPSERVGROUP = "applicationServerGroup";
    public static final String SAPADDRCLIENT_MSGSERVHOSTNAME = "messageServerHostname";
    
    public void setUseLoadBalancing(String useLoadBalancing);

    public String getUseLoadBalancing();
    
    public void setApplicationServerGroup(String applicationServerGroup);

    public String getApplicationServerGroup();
    
    public void setMessageServerHostname(String msgServerHostname);

    public String getMessageServerHostname();
    
}
