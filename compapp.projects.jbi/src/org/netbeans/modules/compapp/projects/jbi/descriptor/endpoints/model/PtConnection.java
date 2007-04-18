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

package org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model;

import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * A service connection helper class that groups connections by port type
 *
 * @author tli
 */
public class PtConnection implements Serializable {
    /**
     * DOCUMENT ME!
     */
//    public static final String INBOUND = "inbound"; // NOI18N

    /**
     * DOCUMENT ME!
     */
//    public static final String OUTBOUND = "outbound"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    String porttype;

    /**
     * DOCUMENT ME!
     */
    List<Port> ports = new ArrayList<Port>(); // ServicePort

    /**
     * DOCUMENT ME!
     */
    List<Endpoint> consumes = new ArrayList<Endpoint>(); // Endpoint

    /**
     * DOCUMENT ME!
     */
    List<Endpoint> provides = new ArrayList<Endpoint>(); // Endpoint

    /**
     *
     */
    public PtConnection() {
        super();

        // TODO Auto-generated constructor stub
    }

    /**
     * DOCUMENT ME!
     *
     * @param porttype
     */
    public PtConnection(String porttype) {
        super();
        this.porttype = porttype;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the port type.
     */
    public String getPorttype() {
        return this.porttype;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the consumes.
     */
    public List<Endpoint> getConsumes() {
        return this.consumes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param consume The consume to add.
     */
    public void addConsume(Endpoint consume) {
        if (!consumes.contains(consume)) {
            if (!isDuplicate(consume, consumes)) {
                consumes.add(consume);
             }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the provides.
     */
    public List<Endpoint> getProvides() {
        return this.provides;
    }

    /**
     * DOCUMENT ME!
     *
     * @param provide The provide to add.
     */
    public void addProvide(Endpoint provide) {
        if (!provides.contains(provide)) {
            if (!isDuplicate(provide, provides)) {
                provides.add(provide);
             }
        }
    }

    private boolean isDuplicate(Endpoint endpoint, List<Endpoint> endpoints) {
        QName serviceQName = endpoint.getServiceQName();
        String endpointName = endpoint.getEndpointName();
        
        for (Endpoint e : endpoints) {
            QName myServiceQName = e.getServiceQName();
            String myEndpointName = e.getEndpointName();
            if (serviceQName.equals(myServiceQName) && 
                    endpointName.equalsIgnoreCase(myEndpointName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the ports.
     */
    public List<Port> getPorts() {
        return this.ports;
    }

    /**
     * DOCUMENT ME!
     *
     * @param port The port to add.
     */
    public void addPort(Port port) {
        if (!ports.contains(port)) {
            ports.add(port);
        }
    }

    /**
     * DOCUMENT ME!
     *
     */
    public String dump() {
        String s = "Connection [" + porttype + "]: "  // NOI18N
                + ports.size() + " ports, "  // NOI18N
                + provides.size() + " providers, "   // NOI18N
                + consumes.size() + " consumers\n";  // NOI18N

        for (int i=0; i<ports.size(); i++) {
            Port p = (Port) ports.get(i);
            Service sv = (Service) p.getParent();
            String tns = ((Definitions) sv.getParent()).getTargetNamespace();
            s += "\tport[" + i + "]:     {" + tns + "}" + sv.getName() + "." + p.getName() + "\n";  // NOI18N
        }

        for (int i=0; i<provides.size(); i++) {
            Endpoint p = (Endpoint) provides.get(i);
            s += "\tprovides[" + i + "]: " + p.getServiceQName() + "." + p.getEndpointName() + "\n";  // NOI18N
        }

        for (int i=0; i<consumes.size(); i++) {
            Endpoint p = (Endpoint) consumes.get(i);
            s += "\tconsumes[" + i + "]: " + p.getServiceQName() + "." + p.getEndpointName() + "\n";  // NOI18N
        }

        return s;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
    }
}