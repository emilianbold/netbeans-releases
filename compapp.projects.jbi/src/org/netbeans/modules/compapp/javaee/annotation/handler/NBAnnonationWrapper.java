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

/*
 * AnnonationFactory.java
 *
 * Created on December 13, 2006, 6:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.annotation.handler;

import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.ElementValue;

/**
 * @author gpatil
 */
public class NBAnnonationWrapper {
    private Annotation anno = null;
    
    public NBAnnonationWrapper(Annotation a){
        this.anno = a;
    }
    
    public String getStringValue(String propName){
        String ret = null;
        if (anno != null) {
            AnnotationComponent ac = anno.getComponent(propName);
            ElementValue ev = null;
            if (ac != null){
                ev = ac.getValue();
                if (ev != null){
                    ret = ev.toString();
                    int indx = ret.indexOf("=");
                    if (indx >= 0){
                        ret = ret.substring(indx + 1);
                    }
                }
            }
        }
        return ret;
    }
}
