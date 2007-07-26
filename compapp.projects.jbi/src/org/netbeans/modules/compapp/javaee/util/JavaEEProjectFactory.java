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
 * JavaEEProjectFactory.java
 *
 * Created on October 9, 2006, 8:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.util;

import org.netbeans.modules.compapp.javaee.codegen.model.EJBProject;
import org.netbeans.modules.compapp.javaee.codegen.model.EnterpriseAppProject;
import org.netbeans.modules.compapp.javaee.codegen.model.JavaEEProject;
import org.netbeans.modules.compapp.javaee.codegen.model.WebappProject;

/**
 *
 * @author gpatil
 */
public class JavaEEProjectFactory {
    private JavaEEProjectFactory() {
    }
    
    public static JavaEEProject getProject(String jarPath) {
        JavaEEProject.ProjectType jarType = getJarType(jarPath);
        JavaEEProject javaEEJar = null;
        
        switch (jarType){
            case EJB:
                javaEEJar = new EJBProject(jarPath);
                break;
            case WEB:
                javaEEJar = new WebappProject(jarPath);
                break;
            case ENT:
                javaEEJar = new EnterpriseAppProject(jarPath);                
                break;
            default:
                throw new IllegalArgumentException("Invalid Jar Type project.");
        }
                
        return javaEEJar;
    }
    
    private static JavaEEProject.ProjectType  getJarType(String jarpath){
        String nj = jarpath.trim();
        nj = nj.toLowerCase();
        JavaEEProject.ProjectType ret = JavaEEProject.ProjectType.EJB;
        
        if (nj.endsWith("war")){ // No I18N
            ret = JavaEEProject.ProjectType.WEB;
        } else if (nj.endsWith("ear")){  // No I18N
            ret = JavaEEProject.ProjectType.ENT;
        }
        
        return ret;
    }
}
