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

/**
 *
 * @author echou
 */
public class ActivationConfigPropertyAnnoWrapper extends AnnotationWrapperBase {
    
    private String propertyName;
    private String propertyValue;
    
    /** Creates a new instance of ActivationConfigPropertyAnnoWrapper */
    public ActivationConfigPropertyAnnoWrapper(Annotation anno) {
        this.propertyName = getStringValue(anno, "propertyName"); // NOI18N
        this.propertyValue = getStringValue(anno, "propertyValue"); // NOI18N
    }
    
    public String propertyName() {
        return this.propertyName;
    }
    public String propertyValue() {
        return this.propertyValue;
    }
}
