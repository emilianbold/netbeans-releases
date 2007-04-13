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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.javamodel;

import java.util.List;

/**
 *
 * @author mkuchtiak
 */
public class JavadocModel {
    
    private String text;
    private List<String> inlineJavadoc;
    private List<String> paramJavadoc;
    private List<String> throwsJavadoc;
    private String returnJavadoc;
    
    /** Creates a new instance of MethodModel */
    JavadocModel() {
    }
    
    /** Creates a new instance of MethodModel */
    JavadocModel(String text) {
        this.text=text;
    }
    
    public String getText() {
        return text;
    }
    
    void setText(String text) {
        this.text=text;
    }
    
    public List<String> getInlineJavadoc() {
        return inlineJavadoc;
    }

    void setInlineJavadoc(List<String> inlineJavadoc) {
        this.inlineJavadoc = inlineJavadoc;
    }

    public List<String> getParamJavadoc() {
        return paramJavadoc;
    }

    void setParamJavadoc(List<String> paramJavadoc) {
        this.paramJavadoc = paramJavadoc;
    }

    public List<String> getThrowsJavadoc() {
        return throwsJavadoc;
    }

    void setThrowsJavadoc(List<String> throwsJavadoc) {
        this.throwsJavadoc = throwsJavadoc;
    }

    public String getReturnJavadoc() {
        return returnJavadoc;
    }

    void setReturnJavadoc(String returnJavadoc) {
        this.returnJavadoc = returnJavadoc;
    }

        
    public boolean isEqualTo(JavadocModel model) {
        if (!Utils.isEqualTo(text,model.text)) return false;
        return true;
    }
}
