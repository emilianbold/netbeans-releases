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
package org.netbeans.modules.subversion.options;

import java.util.regex.Pattern;

/**
 *
 * @author Tomas Stupka
 */
public class AnnotationExpression {
    
    private String urlExp;
    private String annotationExp;        
    private Pattern urlPattern;        
    
    public AnnotationExpression(String urlExp, String annotationExp) {
        this.urlExp = urlExp;
        this.annotationExp = annotationExp;
        this.urlPattern = Pattern.compile(urlExp);       
    }         
    public String getUrlExp() {
        return urlExp;        
    }
    public String getAnnotationExp() {
        return annotationExp;
    }        
    public Pattern getUrlPatern() {
        return urlPattern;
    }
    void setUrlExp(String urlExp) {
        this.urlExp = urlExp;        
    }
    void setAnnotationExp(String annotationExp) {
        this.annotationExp = annotationExp;
    }            
}
