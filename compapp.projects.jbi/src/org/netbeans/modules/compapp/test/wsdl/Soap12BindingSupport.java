/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.compapp.test.wsdl;

import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address;

/**
 *
 * @author jqian
 */
public class Soap12BindingSupport extends SoapBindingSupport {

    private final String SOAP12_ENVELOPE_NAMESPACE = 
            "http://www.w3.org/2003/05/soap-envelope"; // NOI18N

    private final String SOAP12_ENVELOPE_NAMESPACE_PREFIX = "soap12env"; // NOI18N

    private final String SOAP12_DEFAULT_ENCODING_STYLE =
            "http://www.w3.org/2003/05/soap-encoding"; // NOI18N

    public Soap12BindingSupport(
            Binding binding,
            Definitions definition,
            SchemaTypeLoader schemaTypeLoader) {
        super(binding, definition, schemaTypeLoader);
    }

    @Override
    protected String getSoapEnvelopeNamespace() {
        return SOAP12_ENVELOPE_NAMESPACE;
    }

    @Override
    protected String getSoapEnvelopeNamespacePrefix() {
        return SOAP12_ENVELOPE_NAMESPACE_PREFIX;
    }

    @Override
    protected String getDefaultEncodingStyle() {
        return SOAP12_DEFAULT_ENCODING_STYLE;
    }

    @Override
    public String[] getEndpoints() {
        List<String> result = new ArrayList<String>();

        for (Service service : mDefinition.getServices()) {
            for (Port port : service.getPorts()) {
                if (port.getBinding().get() == mBinding) {
                    List<ExtensibilityElement> eeList =
                            port.getExtensibilityElements();
                    SOAP12Address soapAddress =
                            Util.getAssignableExtensiblityElement(
                            eeList, SOAP12Address.class);
                    if (soapAddress != null) {
                        result.add(soapAddress.getLocation());
                    }
                }
            }
        }
        return (String[])result.toArray(new String[0]);
    }
}
