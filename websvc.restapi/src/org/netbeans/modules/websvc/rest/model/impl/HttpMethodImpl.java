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
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public class HttpMethodImpl extends RestMethodDescriptionImpl implements HttpMethod {
    
    private String type;
    private String consumeMime;
    private String produceMime;
    
    public HttpMethodImpl(ExecutableElement methodElement) {
        super(methodElement);   
        
        this.type = Utils.getHttpMethod(methodElement);
        this.consumeMime = Utils.getConsumeMime(methodElement);
        this.produceMime = Utils.getProduceMime(methodElement);
    }

    public String getType() {
        return type;
    }
    
    public String getConsumeMime() {
        return consumeMime;
    }
    
    public String getProduceMime() {
        return produceMime;
    }
    
    public Status refresh(Element element) {    
        boolean isModified = false;
        
        if (super.refresh(element) == Status.MODIFIED) {
            isModified = true;
        }
        
        if (!Utils.hasHttpMethod(element)) {
            return Status.REMOVED;
        }
    
        String newValue = Utils.getConsumeMime(element);
        if (!consumeMime.equals(newValue)) {
            consumeMime = newValue;
            isModified = true;
        }
        
        newValue = Utils.getProduceMime(element);
        if (!produceMime.equals(newValue)) {
            produceMime = newValue;
            isModified = true;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
}
