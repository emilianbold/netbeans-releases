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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.redelivery.properties;

import javax.xml.namespace.QName;

/**
 *
 * @author jqian
 */
public class Endpoint {

    private static final String COLON = ":"; // NOI18N
    private QName serviceQName;
    private String endpointName;

    public Endpoint(QName serviceQName, String endpointName) {
        this.serviceQName = serviceQName;
        this.endpointName = endpointName;
    }

    public QName getServiceQName() {
        return serviceQName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    /**
     * Gets the service QName in prefixed form.
     * 
     * @return service QName in prefixed form, e.x., "ns1:serviceName"
     */
    public String getPrefixedServiceName() {
        if (serviceQName == null) {
            return "";
        }
        return serviceQName.getPrefix() + COLON + serviceQName.getLocalPart();
    }

    /**
     * Parses the string representation of an endpoint in the form of
     * {namespaceURI}serviceName:endpointName
     * 
     * @param stringValue   string representation of an endpoint
     * 
     * @return the parsed endpoint
     */
    // TODO: validation, and no-namespace 
    public static Endpoint valueOf(String stringValue) {
        String serviceQNameAsString =
                stringValue.substring(0, stringValue.lastIndexOf(COLON));
        String endpointName =
                stringValue.substring(stringValue.lastIndexOf(COLON) + 1);
        QName serviceQName = QName.valueOf(serviceQNameAsString);

        return new Endpoint(serviceQName, endpointName);
    }

    @Override
    public String toString() {
        String ret = (serviceQName == null) ? "null" : serviceQName.toString(); // NOI18N
        ret = ret + COLON + endpointName;
        return ret;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || !(other instanceof Endpoint)) {
            return false;
        }

        Endpoint otherEndpoint = (Endpoint) other;

        if (endpointName == null && otherEndpoint.getEndpointName() != null) {
            return false;
        }

        if (serviceQName == null && otherEndpoint.getServiceQName() != null) {
            return false;
        }

        if (endpointName.equals(otherEndpoint.getEndpointName()) &&
                serviceQName.equals(otherEndpoint.getServiceQName())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + endpointName.hashCode();
        hash = hash * 31 + serviceQName.hashCode();
        return hash;
    }
}
