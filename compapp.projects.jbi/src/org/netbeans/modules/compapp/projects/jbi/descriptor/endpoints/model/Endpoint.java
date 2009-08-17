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

import java.io.Serializable;
import javax.xml.namespace.QName;


/**
 * DOCUMENT ME!
 *
 * @author Graj 
 * @author jqian
 */
public class Endpoint implements Serializable {
    
    private String endpointName;    
    private QName serviceQName;
    private QName interfaceQName;
    private boolean isConsumes;
  
    public Endpoint(String endpointName, 
            QName serviceQName, QName interfaceQName) {
        this.endpointName = endpointName;
        this.serviceQName = serviceQName;
        this.interfaceQName = interfaceQName;
    }
    
    public Endpoint(String endpointName, 
            QName serviceQName, QName interfaceQName,
            boolean isConsumes) {
        this(endpointName, serviceQName, interfaceQName);
        this.isConsumes = isConsumes;
    }
    
    public String getEndpointName() {
        return endpointName;
    }
    
    public QName getServiceQName() {
        return serviceQName;
    }
    
    public QName getInterfaceQName() {
        return interfaceQName;
    }
    
    public boolean isConsumes() {
        return isConsumes;
    }
    
    public String getFullyQualifiedName() {
        return getServiceQName().toString() + "." + getEndpointName();
    }

    @Override
    public boolean equals(Object p) {
        if (this == p) {
            return true;
        }
        
        if (p == null || !(p instanceof Endpoint)) {
            return false;
        }
        
        Endpoint endpoint = (Endpoint) p;
        if (endpointName.equals(endpoint.getEndpointName()) &&
                serviceQName.equals(endpoint.getServiceQName()) &&
                interfaceQName.equals(endpoint.getInterfaceQName())) {
            // don't check direction yet..
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + endpointName.hashCode();
        hash = hash * 31 + serviceQName.hashCode();
        hash = hash * 31 + interfaceQName.hashCode();
        return hash;
    }
}
