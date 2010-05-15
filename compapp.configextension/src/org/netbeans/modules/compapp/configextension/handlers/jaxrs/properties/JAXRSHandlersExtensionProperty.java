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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.jaxrs.properties;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.configextension.handlers.properties.AbstractHandlersExtensionProperty;

/**
 *
 * @author jqian
 */
public class JAXRSHandlersExtensionProperty extends AbstractHandlersExtensionProperty {

    private static final String HANDLER_NAMESPACE_URI =
            "http://www.sun.com/jbi/restbc/jaxrs_filters"; // NOI18N
    private static final String HANDLER_ELEMENT_NAME = "filter"; // NOI18N

    public JAXRSHandlersExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                propertyName, displayName, description);
    }

    protected List<String> getHandlerBaseClassNames(boolean inbound) {
        List<String> ret = new ArrayList<String>();
        if (inbound) {
            ret.add("com.sun.jersey.spi.container.ContainerRequestFilter"); // NOI18N
            ret.add("com.sun.jersey.spi.container.ContainerResponseFilter"); // NOI18N
        } else {
            ret.add("com.sun.jersey.api.client.filter.ClientFilter"); // NOI18N
        }
        return ret;
    }

    protected String getHandlerNamespaceURI() {
        return HANDLER_NAMESPACE_URI;
    }

    protected String getHandlerElementName() {
        return HANDLER_ELEMENT_NAME;
    }

}
