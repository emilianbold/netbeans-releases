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

import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.extensions.xsd.WadlSchema;
import org.netbeans.modules.websvc.rest.wadl.model.extensions.xsd.impl.WadlSchemaImpl;
import org.netbeans.modules.websvc.rest.wadl.model.spi.*;
import org.w3c.dom.Element;

/**
 *
 * @author Ayub Khan
 */
public class WadlComponentFactoryImpl implements WadlComponentFactory {
    
    private WadlModel model;
    /** Creates a new instance of WadlComponentFactoryImpl */
    public WadlComponentFactoryImpl(WadlModel model) {
        this.model = model;
    }
    
    public WadlComponent create(Element element, WadlComponent context) {
        ElementFactory factory = ElementFactoryRegistry.getDefault().get(
                Util.getQName(element, (WadlComponentBase)context));
        return create(factory, element, context);
    }
    
    private WadlComponent create(ElementFactory factory, Element element, WadlComponent context) {
        if(factory != null ){
            return factory.create(context, element);
        } else {
            return new GenericExtensibilityElement(model, element);
        }
    }
    
    public WadlComponent create(WadlComponent parent, QName qName) {
       String q = qName.getPrefix();
       if (q == null || q.length() == 0) {
           q = qName.getLocalPart();
       } else {
           q = q + ":" + qName.getLocalPart();
       }

       ElementFactory factory = ElementFactoryRegistry.getDefault().get(qName);
       Element element = model.getDocument().createElementNS(qName.getNamespaceURI(), q);
       return create(factory, element, parent);
    }
    
    // XSD
    public WadlSchema createWadlSchema() {
        return new WadlSchemaImpl(model);
    }

    public Resources createResources() {
        return new ResourcesImpl(model);
    }

    public Request createRequest() {
        return new RequestImpl(model);
    }

    public Response createResponse() {
        return new ResponseImpl(model);
    }

    public Grammars createGrammars() {
        return new GrammarsImpl(model);
    }

    public Doc createDoc() {
        return new DocImpl(model);
    }

    public Resource createResource() {
        return new ResourceImpl(model);
    }

    public ResourceType createResourceType() {
        return new ResourceTypeImpl(model);
    }

    public Option createOption() {
        return new OptionImpl(model);
    }

    public Application createApplication() {
        return new ApplicationImpl(model);
    }

    public Link createLink() {
        return new LinkImpl(model);
    }

    public Include createInclude() {
        return new IncludeImpl(model);
    }

    public Param createParam() {
        return new ParamImpl(model);
    }

    public Method createMethod() {
        return new MethodImpl(model);
    }

    public Representation createRepresentation() {
        return new RepresentationImpl(model);
    }

    public Fault createFault() {
        return new FaultImpl(model);
    }
}

