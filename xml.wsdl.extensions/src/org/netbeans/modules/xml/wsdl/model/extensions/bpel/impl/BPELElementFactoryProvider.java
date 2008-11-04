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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

/**
 * @authro Nam Nguyen
 * 
 * changed by
 * @author ads
 */
public class BPELElementFactoryProvider {
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class CorrelationPropertyFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.PROPERTY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new CorrelationPropertyImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class PartnerLinkTypeFactory extends ElementFactory {
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.PARTNER_LINK_TYPE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new PartnerLinkTypeImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class RoleFactory  extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.ROLE.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new RoleImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class PropertyAliasFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.PROPERTY_ALIAS.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new PropertyAliasImpl(context.getModel(), element);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class QueryFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            return Collections.singleton(BPELQName.QUERY.getQName());
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new QueryImpl(context.getModel(), element);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.model.spi.ElementFactory.class)
    public static class DocumentationFactory extends ElementFactory{
        public Set<QName> getElementQNames() {
            Set<QName> set = new HashSet<QName>();
            set.add( BPELQName.DOCUMENTATION_VARPROP.getQName() );
            set.add( BPELQName.DOCUMENTATION_PLNK.getQName() );
            return set;
        }
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new DocumentationImpl(context.getModel(), element);
        }
    }
    
}
