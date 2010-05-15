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
package org.netbeans.modules.wsdlextensions.sap.impl;

import org.netbeans.modules.wsdlextensions.sap.SAPAddress;
import org.netbeans.modules.wsdlextensions.sap.SAPAddressClient;
import org.netbeans.modules.wsdlextensions.sap.SAPAddressServer;
import org.netbeans.modules.wsdlextensions.sap.SAPBinding;
import org.netbeans.modules.wsdlextensions.sap.SAPFmOperation;

import org.netbeans.modules.xml.xam.dom.Attribute;

public enum SAPAttribute implements Attribute {

    SAPADDR_APPSERVERHOST(SAPAddress.SAPADDR_APPSERVERHOST),
    SAPADDR_CLIENTNUM(SAPAddress.SAPADDR_CLIENTNUM),
    SAPADDR_SYSNUM(SAPAddress.SAPADDR_SYSNUM),
    SAPADDR_SYSID(SAPAddress.SAPADDR_SYSID),
    SAPADDR_USER(SAPAddress.SAPADDR_USER),
    SAPADDR_PW(SAPAddress.SAPADDR_PW),
    SAPADDR_LANG(SAPAddress.SAPADDR_LANG),
    SAPADDR_ABAPDEBUG(SAPAddress.SAPADDR_ABAPDEBUG),
    SAPADDR_ISUNI(SAPAddress.SAPADDR_ISUNI),
    SAPADDR_GWHOST(SAPAddress.SAPADDR_GWHOST),
    SAPADDR_GWSERVICE(SAPAddress.SAPADDR_GWSERVICE),
    SAPADDR_ROUTERSTR(SAPAddress.SAPADDR_ROUTERSTR),
    SAPADDRCLIENT_USELOADBAL(SAPAddressClient.SAPADDRCLIENT_USELOADBAL),
    SAPADDRCLIENT_APPSERVGROUP(SAPAddressClient.SAPADDRCLIENT_APPSERVGROUP),
    SAPADDRCLIENT_MSGSERVHOSTNAME(SAPAddressClient.SAPADDRCLIENT_MSGSERVHOSTNAME),
    SAPADDRSERVER_PROGID(SAPAddressServer.SAPADDRSERVER_PROGID),
    SAPBINDING_TRXMODE(SAPBinding.SAPBINDING_TRXMODE),
    SAPBINDING_TRXIDDB(SAPBinding.SAPBINDING_TRXIDDB),
    SAPBINDING_MAXTIDDBROWS(SAPBinding.SAPBINDING_MAXTIDDBROWS),
    SAPFMOPER_FUNCTIONNAME(SAPFmOperation.SAPFMOPER_FUNCTIONNAME);
    private String name;
    private Class type;
    private Class subtype;

    SAPAttribute(String name) {
        this(name, String.class);
    }

    SAPAttribute(String name, Class type) {
        this(name, type, null);
    }

    SAPAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }

    public Class getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Class getMemberType() {
        return subtype;
    }

    @Override
    public String toString() {
        return name;
    }
}
