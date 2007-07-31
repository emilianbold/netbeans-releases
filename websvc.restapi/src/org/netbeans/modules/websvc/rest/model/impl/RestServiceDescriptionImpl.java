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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public class RestServiceDescriptionImpl extends PersistentObject implements RestServiceDescription {
    
    private String name;
    private String uriTemplate;
    private String className;
    private Map<String, RestMethodDescriptionImpl> methods;
    private boolean isRest;
  
    public RestServiceDescriptionImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        
        this.name = typeElement.getSimpleName().toString();
        this.uriTemplate = Utils.getUriTemplate(typeElement);
        this.className = typeElement.getQualifiedName().toString();
        this.isRest = true;
  
        initMethods(typeElement);
    }
    
    
    private void initMethods(TypeElement typeElement) {
        methods = new HashMap<String, RestMethodDescriptionImpl>();
        AnnotationModelHelper helper = getHelper();
        
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                addMethod(element);
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    public List<RestMethodDescription> getMethods() {
        List<RestMethodDescription> list = new ArrayList<RestMethodDescription>();
        
        for (RestMethodDescriptionImpl method : methods.values()) {
            list.add((RestMethodDescription) method);
        }
        
        return list;
    }
 
    public String getClassName() {
        return className;
    }
 
    public boolean isRest() {
        return isRest;
    }
    
    public Status refresh(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            return Status.REMOVED;
        }
        
        boolean isRest = false;
        boolean isModified = false;
        
        if (Utils.hasUriTemplate(typeElement)) {
            isRest = true;
        }
        
        String newValue = typeElement.getSimpleName().toString();
        
        
        // Refresh the resource name.
        if (this.name != newValue) {
            this.name = newValue;
            isModified = true;
        }
        
        // Refresh the class name.
        newValue = typeElement.getQualifiedName().toString();
        if (this.className != newValue) {
            this.className = newValue;
            isModified = true;
        }
        
        // Refresh the uriTemplate.
        newValue = Utils.getUriTemplate(typeElement);
        if (!this.uriTemplate.equals(newValue)) {
            this.uriTemplate = newValue;
            isModified = true;
        }
        
        Map<String, RestMethodDescriptionImpl> prevMethods = methods;
        methods = new HashMap<String, RestMethodDescriptionImpl>();
        
        // Refresh all the methods.
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                String methodName = element.getSimpleName().toString();
                
                RestMethodDescriptionImpl method = prevMethods.get(methodName);
                
                if (method != null) {
                    Status status = method.refresh(element);
                    
                    switch (status) {
                    case REMOVED:
                        if (addMethod(element)) {
                            isRest = true;
                        }
                        isModified = true;
                        break;
                    case MODIFIED:
                        isRest = true;
                        isModified = true;
                        methods.put(methodName, method);
                        break;
                    case UNMODIFIED:
                        isRest = true;
                        methods.put(methodName, method);
                        break;
                    }
                } else {
                    if (addMethod(element)) {
                        isRest = true;
                        isModified = true;
                    }
                }
            }
        }
        
        if (methods.size() != prevMethods.size()) {
            isModified = true;
        }
        
        if (!isRest) {
            this.isRest = false;
            return Status.REMOVED;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
    
    
    private boolean addMethod(Element element) {
        RestMethodDescriptionImpl method = RestMethodDescriptionFactory.create(element);
        
        if (method != null) {
            methods.put(element.getSimpleName().toString(), method);
            
            return true;
        }
        return false;
    }
    
    public String toString() {
        return name + "[" + uriTemplate + "]"; //NOI18N
    }
}
