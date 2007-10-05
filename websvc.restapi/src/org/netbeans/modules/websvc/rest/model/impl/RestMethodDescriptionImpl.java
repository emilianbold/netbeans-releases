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
