/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
    {
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
        return getChildElementText(JSFConfigQNames.BASE_NAME.getQName(getModel().getVersion()));
    }

    public void setBaseName(String baseName) {
        setChildElementText(BASE_NAME, baseName, JSFConfigQNames.BASE_NAME.getQName(getModel().getVersion()));
    }

    public String getVar() {
        return getChildElementText(JSFConfigQNames.VAR.getQName(getModel().getVersion()));
    }

    public void setVar(String var) {
        setChildElementText(VAR, var, JSFConfigQNames.VAR.getQName(getModel().getVersion()));
    }

    @Override
    protected List<String> getSortedListOfLocalNames() {
        return RESOURCE_BUNDLE_SORTED_ELEMENTS;
    }
}
