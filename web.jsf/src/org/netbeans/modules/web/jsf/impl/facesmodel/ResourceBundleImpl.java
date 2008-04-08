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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.w3c.dom.Element;


/**
 * The resource-bundle element inside the application element
 * references a java.util.ResourceBundle instance by name
 * using the var element.  ResourceBundles referenced in this
 * manner may be returned by a call to
 * Application.getResourceBundle() passing the current
 * FacesContext for this request and the value of the var
 * element below.
 * 
 * @author Petr Pisl
 */

public class ResourceBundleImpl extends DescriptionGroupImpl implements ResourceBundle {

    protected static final List<String> RESOURCE_BUNDLE_SORTED_ELEMENTS = new ArrayList<String>();
    static {
        RESOURCE_BUNDLE_SORTED_ELEMENTS.addAll(DescriptionGroupImpl.DESCRIPTION_GROUP_SORTED_ELEMENTS);
        RESOURCE_BUNDLE_SORTED_ELEMENTS.add(JSFConfigQNames.BASE_NAME.getLocalName());
        RESOURCE_BUNDLE_SORTED_ELEMENTS.add(JSFConfigQNames.VAR.getLocalName());
    }
    
    public ResourceBundleImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public ResourceBundleImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.RESOURCE_BUNDLE));
    }
            
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    public String getBaseName() {
        return getChildElementText(JSFConfigQNames.BASE_NAME.getQName(getNamespaceURI()));
    }

    public void setBaseName(String baseName) {
        setChildElementText(BASE_NAME, baseName, JSFConfigQNames.BASE_NAME.getQName(getNamespaceURI()));
    }

    public String getVar() {
        return getChildElementText(JSFConfigQNames.VAR.getQName(getNamespaceURI()));
    }

    public void setVar(String var) {
        setChildElementText(VAR, var, JSFConfigQNames.VAR.getQName(getNamespaceURI()));
    }

    @Override
    protected List<String> getSortedListOfLocalNames() {
        return RESOURCE_BUNDLE_SORTED_ELEMENTS;
    }
}
