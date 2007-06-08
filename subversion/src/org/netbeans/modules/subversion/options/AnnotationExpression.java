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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tigris.subversion.svnclientadapter.SVNUrl;

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
    
    public String getCopyName(String url) {
        Matcher m = getUrlPatern().matcher(url);
        if (m.matches()) {
            String ae = getAnnotationExp();

            StringBuffer copyName = new StringBuffer();
            StringBuffer groupStr = new StringBuffer();                    
            boolean inGroup = false;

            for (int i = 0; i < ae.length(); i++) {
                char c = ae.charAt(i);
                if(c == '\\') {
                    inGroup = true;                                                                      
                    continue;
                } else if(inGroup) {
                    if(Character.isDigit(c)) {                                
                        groupStr.append(c);                                                                                                                                            
                    } else {
                        if(groupStr.length() > 0) {
                            try {
                                int group = Integer.valueOf(groupStr.toString()).intValue();    
                                copyName.append(m.group(group));
                            } catch (Exception e) {
                                copyName.append('\\');
                                copyName.append(groupStr);
                            }
                            groupStr = new StringBuffer();                    
                        } else {
                            copyName.append('\\');
                            copyName.append(c);
                        }                                
                        inGroup = false;
                    }                                                                
                    continue;                            
                }
                copyName.append(c);
            }
            if(groupStr.length() > 0) {
                try {
                    int group = Integer.valueOf(groupStr.toString()).intValue();
                    copyName.append(m.group(group));
                } catch (Exception e) {
                    copyName.append('\\');
                    copyName.append(groupStr);
                }                                            
            }
            return copyName.toString();     
        }
        return null;
    }
    
    
}
