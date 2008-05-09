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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2throw new UnsupportedOperationException("Not supported yet."); or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.axis2.config.model.impl;

import org.netbeans.modules.websvc.axis2.config.model.Axis2QNames;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Visitor;
import org.netbeans.modules.websvc.axis2.config.model.GenerateWsdl;
import org.netbeans.modules.websvc.axis2.config.model.JavaGenerator;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.w3c.dom.Element;

public class ServiceImpl extends Axis2ComponentImpl implements Service {
    
    private static String NAME_ATTR_PROP = Axis2Attributes.attrName.getName();
    
    public ServiceImpl(Axis2ModelImpl model, Element e) {
        super(model, e);
    }
    
    public ServiceImpl(Axis2ModelImpl model) {
        this(model, createElementNS(model, Axis2QNames.SERVICE));
    }
    
    public void accept(Axis2Visitor visitor) {
        visitor.visit(this);
    }

    public String getServiceClass() {
        return getChildElementText(Axis2QNames.SERVICE_CLASS.getQName());
    }

    public void setServiceClass(String serviceClass) {
        setChildElementText(SERVICE_CLASS_PROP, serviceClass, Axis2QNames.SERVICE_CLASS.getQName());
    }

    public GenerateWsdl getGenerateWsdl() {
        return super.getChild(GenerateWsdl.class);
    }

    public void setGenerateWsdl(GenerateWsdl value) {
        GenerateWsdl child = super.getChild(GenerateWsdl.class);
        if (child != null) {
            super.removeChild(GENERATE_WSDL_PROP, child);
        }
        if (value != null) {
            appendChild(GENERATE_WSDL_PROP, value);
        }
    }

    public String getNameAttr() {
        return getAttribute(Axis2Attributes.attrName);
    }

    public void setNameAttr(String name) {
        super.setAttribute(NAME_ATTR_PROP, Axis2Attributes.attrName, name);
    }

    public String getWsdlUrl() {
        return getChildElementText(Axis2QNames.WSDL_URL.getQName());
    }

    public void setWsdlUrl(String serviceUrl) {
        setChildElementText(WSDL_URL_PROP, serviceUrl, Axis2QNames.WSDL_URL.getQName());
    }

    public JavaGenerator getJavaGenerator() {
        return super.getChild(JavaGenerator.class);
    }

    public void setJavaGenerator(JavaGenerator javaGenerator) {
        JavaGenerator child = super.getChild(JavaGenerator.class);
        if (child != null) {
            super.removeChild(JAVA_GENERATOR_PROP, child);
        }
        if (javaGenerator != null) {
            appendChild(JAVA_GENERATOR_PROP, javaGenerator);
        }
    }
    
}
