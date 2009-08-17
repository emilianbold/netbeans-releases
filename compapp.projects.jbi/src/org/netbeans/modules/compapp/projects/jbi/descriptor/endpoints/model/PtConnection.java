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