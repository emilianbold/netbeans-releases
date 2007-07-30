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
package org.netbeans.modules.websvc.rest.model.impl;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public class SubResourceLocatorImpl extends RestMethodDescriptionImpl
        implements SubResourceLocator {
    
    private String uriTemplate;
    private String resourceType;
    
    public SubResourceLocatorImpl(ExecutableElement methodElement) {
        super(methodElement);
        
        this.uriTemplate = Utils.getUriTemplate(methodElement);
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public Status refresh(Element element) {
        boolean isModified = false;
        
        if (super.refresh(element) == Status.MODIFIED) {
            isModified = true;
        }
        
        if (!Utils.hasUriTemplate(element)) {
            return Status.REMOVED;
        }
        
        String newValue = Utils.getUriTemplate(element);
        if (!uriTemplate.equals(newValue)) {
            uriTemplate = newValue;
            isModified = true;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
}
