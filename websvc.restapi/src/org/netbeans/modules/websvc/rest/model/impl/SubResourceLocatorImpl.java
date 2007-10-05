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
