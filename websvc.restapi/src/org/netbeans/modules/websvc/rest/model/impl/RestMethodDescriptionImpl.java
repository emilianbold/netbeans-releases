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
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public abstract class RestMethodDescriptionImpl {
    private String name;
    private String returnType;
    
    public RestMethodDescriptionImpl(ExecutableElement methodElement) {       
        this.name = methodElement.getSimpleName().toString();
        this.returnType = methodElement.getReturnType().toString();
    }
    
    public String getName() {
        return name;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public Status refresh(Element element) {
        ExecutableElement methodElement = (ExecutableElement) element;
        boolean isModified = false;
        
        String newValue = methodElement.getSimpleName().toString();
        if (!name.equals(newValue)) {
            name = newValue;
            isModified = true;
        }
        
        newValue = methodElement.getReturnType().toString();
        if (!returnType.equals(newValue)) {
            returnType = newValue;
            isModified = true;
        }
        
        if (isModified) {
            return Status.UNMODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
}
