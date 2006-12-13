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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.jaxrpc;

/**
 *
 * @author mkuchtiak
 */

/** Provides the general information from WSDL file
 *
 * @author Milan Kuchtiak
 */
public interface PortInformation {

    public String[] getServiceNames();

    public ServiceInfo getServiceInfo(String serviceName);

    public java.util.List getBindings();

    public java.util.List getImportedSchemas();

    public String getTargetNamespace();

    public java.util.List/*PortInfo*/ getEntirePortList();


    public static interface PortInfo {

        public String getPortType();

        public String getBinding();

        public String getBindingType();

        public String getPort();
    }
    
    public static interface ServiceInfo {
        
        public String getServiceName();

        public java.util.List/*PortInfo*/ getPorts();
    }
}
