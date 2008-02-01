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

package org.netbeans.modules.websvc.saas.model;

import java.util.List;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices.Header;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices.SaasMetadata;

/**
 *
 * @author nam
 */
public class Saas {
    public static final String PROP_PARENT_GROUP = "parentGroup";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_WADL = "http://research.sun.com/wadl/2006/10";
    //private static final String CUSTOM = "custom";
    
    private SaasServices delegate;
    private SaasGroup parentGroup;
        
    public Saas(SaasServices services, SaasGroup parentGroup) {
        this.delegate = services;
        this.parentGroup = parentGroup;
    }

    public SaasServices getDelegate() {
        return delegate;
    }

    public SaasGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(SaasGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public String getUrl() {
        return delegate.getUrl();
    }

    public SaasMetadata getSaasMetadata() {
        return delegate.getSaasMetadata();
    }

    public List<Method> getMethods() {
        return delegate.getMethods().getMethod();
    }

    public Header getHeader() {
        return delegate.getHeader();
    }

    public String getDisplayName() {
        return (String) delegate.getDisplayName();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public String getApiDoc() {
        return delegate.getApiDoc();
    }
}
