/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.rest.wadl.model.impl;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.model.WadlComponent;
import org.netbeans.modules.websvc.rest.wadl.model.spi.*;
import org.w3c.dom.Element;


/**
 * @author Ayub Khan
 */
public class WadlElementFactoryProvider {
   
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class ApplicationFactory extends ElementFactory {

        public ApplicationFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.APPLICATION.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            throw new UnsupportedOperationException("Root 'application' should be bootstrapped when Wadl model is created"); //NOI18N
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class DocFactory extends ElementFactory {

        public DocFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.DOC.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new DocImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class GrammarsFactory extends ElementFactory {

        public GrammarsFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.GRAMMARS.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new GrammarsImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class ResourcesFactory extends ElementFactory {

        public ResourcesFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.RESOURCES.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new ResourcesImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class ResourceFactory extends ElementFactory {

        public ResourceFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.RESOURCE.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new ResourceImpl(context.getModel(), el);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class ResourceTypeFactory extends ElementFactory {

        public ResourceTypeFactory() {
        }

        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.RESOURCE_TYPE.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new ResourceTypeImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class MethodFactory extends ElementFactory {

        public MethodFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.METHOD.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new MethodImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class RequestFactory extends ElementFactory {

        public RequestFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.REQUEST.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new RequestImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class ResponseFactory extends ElementFactory {

        public ResponseFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.RESPONSE.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new ResponseImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class ParamFactory extends ElementFactory {

        public ParamFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.PARAM.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new ParamImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class OptionFactory extends ElementFactory {

        public OptionFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.OPTION.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new OptionImpl(context.getModel(), el);
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class LinkFactory extends ElementFactory {

        public LinkFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.LINK.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new LinkImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class IncludeFactory extends ElementFactory {

        public IncludeFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.INCLUDE.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new IncludeImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class RepresentationFactory extends ElementFactory {

        public RepresentationFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.REPRESENTATION.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new RepresentationImpl(context.getModel(), el);
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.rest.wadl.model.spi.ElementFactory.class)
    public static class FaultFactory extends ElementFactory {

        public FaultFactory() {
        }
        
        public Set<QName> getElementQNames() {
            return Collections.singleton(WadlQNames.FAULT.getQName());
        }
        public WadlComponent create(WadlComponent context, Element el) {
            checkArgument(getElementQNames(), Util.getQName(el, (WadlComponentBase) context));
            return new FaultImpl(context.getModel(), el);
        }
    }
    
    private static QName getImpliedQName(Element el) {
        String ns = el.getNamespaceURI();
        if (ns == null) { // this can happen if new element has not added to xdm tree
            ns = WadlQNames.WADL_NS_URI;
        }
        return new QName(ns, el.getLocalName());
    }
    
    private static void checkArgument(Set<QName> wqnames, QName qname) {
        checkArgument(wqnames.iterator().next(), qname);
    }

    private static void checkArgument(QName wqname, QName qname) {
        if (! wqname.equals(qname)) {
            throw new IllegalArgumentException("Invalid element "+qname.getLocalPart()); //NOI18N
        }
    }
}
