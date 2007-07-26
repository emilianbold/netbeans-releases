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
    
    public HttpMethodImpl(Element methodElement) {
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
        if (!Utils.hasHttpMethod(element)) {
            return Status.REMOVED;
        }
        
        boolean isModified = false;
        
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
