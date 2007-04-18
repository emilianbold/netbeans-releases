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
import org.netbeans.modules.classfile.ArrayElementValue;
import org.netbeans.modules.classfile.ElementValue;
import org.netbeans.modules.classfile.NestedElementValue;

/**
 *
 * @author echou
 */
public class ResourcesAnnoWrapper extends AnnotationWrapperBase {
    
    private ResourceAnnoWrapper[] value;
    
    /** Creates a new instance of ResourcesAnnoWrapper */
    public ResourcesAnnoWrapper(Annotation anno) {
        AnnotationComponent ac = anno.getComponent("value"); // NOI18N
        if (ac == null) {
            this.value = new ResourceAnnoWrapper[] {};
        }
        ArrayElementValue aev = (ArrayElementValue) ac.getValue();
        ElementValue[] evs = aev.getValues();
        this.value = new ResourceAnnoWrapper[evs.length];
        for (int i = 0; i < evs.length; i++) {
            NestedElementValue nev = (NestedElementValue) evs[i];
            this.value[i] = new ResourceAnnoWrapper(nev.getNestedValue());
        }
    }
    
    public ResourceAnnoWrapper[] value() {
        return this.value;
    }
}
