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
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.extension.bpel;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider;
import org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.AbstractLookup.Content;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider.class)
public class WSDLExtLookupProvider implements WSDLLookupProvider, Lookup.Provider {

    private Lookup lookup;
    private InstanceContent content;
    
    public WSDLExtLookupProvider() {
        content = new InstanceContent();
        lookup = new AbstractLookup(content);
        content.add(new NewCustomizerProvider() {
        
            public Property getProperty(ExtensibilityElement element,
                    QName elementQName, QName attributeQName, boolean isOptional) {
                if (elementQName.equals(BPELQName.PROPERTY_ALIAS.getQName())) {
                    if (attributeQName.getLocalPart().equals("propertyName")) {
                        try {
                            return new PropertyNameProperty(new ExtensibilityElementPropertyAdapter(element, "propertyName", isOptional), String.class, "getValue", "setValue");
                        } catch (NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        
        });
    }

    protected Content getContent() {
        return content;
    }
    
    public Provider getProvider(String namespace) {
        if (namespace.equals(BPELQName.VARPROP_NS)) {
            return this;
        }
        return null;
    }

    

    public Lookup getLookup() {
        return lookup;
    }

}
