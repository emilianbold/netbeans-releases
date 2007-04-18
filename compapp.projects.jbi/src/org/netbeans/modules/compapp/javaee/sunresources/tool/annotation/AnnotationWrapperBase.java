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

package org.netbeans.modules.compapp.javaee.sunresources.tool.annotation;

import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.ClassElementValue;
import org.netbeans.modules.classfile.PrimitiveElementValue;

/**
 *
 * @author echou
 */
public class AnnotationWrapperBase {
    
    public String getStringValue(Annotation anno, String compName) {
        AnnotationComponent ac = anno.getComponent(compName);
        if (ac == null) {
            return ""; // NOI18N
        }
        PrimitiveElementValue ev = (PrimitiveElementValue) ac.getValue();
        CPEntry value = ev.getValue();
        return (String) value.getValue();
    }
    
    public boolean getBooleanValue(Annotation anno, String compName) {
        AnnotationComponent ac = anno.getComponent(compName);
        if (ac == null) {
            return true;
        }
        PrimitiveElementValue ev = (PrimitiveElementValue) ac.getValue();
        CPEntry value = ev.getValue();
        return ((Boolean) value.getValue()).booleanValue();
    }
    
    public String getClassValue(Annotation anno, String compName) {
        AnnotationComponent ac = anno.getComponent(compName);
        if (ac == null) {
            return "java.lang.Object"; // NOI18N
        }
        ClassElementValue ev = (ClassElementValue) ac.getValue();
        return ev.getClassName().getExternalName();
    }
}
