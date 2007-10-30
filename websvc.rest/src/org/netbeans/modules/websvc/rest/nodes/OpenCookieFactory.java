/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
