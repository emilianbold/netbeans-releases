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
package org.netbeans.modules.websvc.rest.nodes;


import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

/**
 *
 * @author Peter Liu
 */
public class OpenCookieFactory {
    
    public static OpenCookie create(Project project, String className) {
        return create(project, className, null);
    }
    
    public static OpenCookie create(Project project, String className, String methodName) {
        
        try {
            FileObject source = SourceGroupSupport.getFileObjectFromClassName(className, project);
            
            return new OpenCookieImpl(source, className, methodName);
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.toString());
        }
        
        return null;
    }
    
    private static class OpenCookieImpl implements OpenCookie {
        private DataObject dataObj;
        private JavaSource javaSource;
        private String className;
        private String methodName;
        
        public OpenCookieImpl(FileObject source, String className, String methodName) {
            try {
                dataObj = DataObject.find(source);
            } catch (Exception de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
            
            javaSource = JavaSource.forFileObject(source);
            this.className = className;
            this.methodName = methodName;
        }
        
        public void open() {
            if (dataObj == null) return;
            
            OpenCookie oc = (OpenCookie) dataObj.getCookie(OpenCookie.class);
            
            if (oc != null) {
                oc.open();
            }
            
            LineCookie lc = (LineCookie) dataObj.getCookie(LineCookie.class);
          
            if (lc != null) {
                long[] position = JavaSourceHelper.getPosition(javaSource, methodName);
                Line line = lc.getLineSet().getOriginal((int) position[0]);
                line.show(Line.SHOW_SHOW, (int) position[1]);
            }
        }
    }
    
}
