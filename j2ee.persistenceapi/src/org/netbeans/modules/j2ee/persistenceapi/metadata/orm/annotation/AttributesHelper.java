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
package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.lang.String;
import java.lang.String;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

/**
 *
 * @author Andrei Badea
 */
public final class AttributesHelper {
    
    private final TypeElement typeElement;
    private final AnnotationModelHelper helper;
    private final PropertyHandler propertyHandler;
    private final boolean fieldAccess;

    public AttributesHelper(TypeElement typeElement, AnnotationModelHelper helper, PropertyHandler propertyHandler) {
        this.typeElement = typeElement;
        this.helper = helper;
        this.propertyHandler = propertyHandler;
        if (typeElement == null) {
            fieldAccess = true;
            return;
        }
        List<? extends Element> elements = typeElement.getEnclosedElements();
        fieldAccess = EntityMappingsUtilities.hasFieldAccess(helper, elements);
    }
    
    public void parse() {
        if (typeElement == null) {
            return;
        }
        for (Element element : typeElement.getEnclosedElements()) {
            ElementKind elementKind = element.getKind();
            if (fieldAccess) {
                if (ElementKind.FIELD.equals(elementKind)) {
                    handleProperty(element);
                }
            } else {
                if (ElementKind.METHOD.equals(elementKind)) {
                    handleProperty(element);
                }
            }
        }
    }
    
    private void handleProperty(Element element) {
        String propertyName = element.getSimpleName().toString();
        if (ElementKind.METHOD.equals(element.getKind())) {
            propertyName = EntityMappingsUtilities.getterNameToPropertyName(propertyName);
            if (propertyName == null) {
                return;
            }
        }
        propertyHandler.handleProperty(element, propertyName);
    }
    
    public boolean hasFieldAccess() {
        return fieldAccess;
    }
    
    public interface PropertyHandler {
        /**
         * Handle the given property (either a <code>VariableElement</code> for the 
         * property field or an <code>ExecutableElement</code> for the property getter method.
         * 
         * @param element never null.
         * @param propertyName never null.
         */
        void handleProperty(Element element, String propertyName);
    }
}
