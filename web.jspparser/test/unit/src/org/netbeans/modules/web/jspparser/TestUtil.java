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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.jspparser;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.modules.web.core.jsploader.JspParserAccess;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author  pj97932
 */
class TestUtil {
    
    static FileObject getFileInWorkDir(String path, NbTestCase test) throws Exception {
        File f = new File(Manager.getWorkDirPath());
        FileObject workDirFO = FileUtil.fromFile(f)[0];
        StringTokenizer st = new StringTokenizer(path, "/");
        FileObject tempFile = workDirFO;
        while (st.hasMoreTokens()) {
            tempFile = tempFile.getFileObject(st.nextToken());
        }
        return tempFile;
    }
    
     static JspParserAPI.WebModule getWebModule(FileObject fo){
        WebModule wm =  WebModule.getWebModule(fo);
        if (wm == null) {
            return null;
        }
        FileObject wmRoot = wm.getDocumentBase();
        if (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo)) {
            return JspParserAccess.getJspParserWM(WebModule.getWebModule(fo));
        }
        return null;
    }
     
    /*static JspParserAPI.WebModule getWebModule(FileObject wmRoot, FileObject jspFile) throws Exception {
        WebModule wm = createWebModule(new UnpWarWebModuleImplementation(wmRoot));
        return JspParserAccess.getJspParserWM(wm);
    }
    
    private static WebModule createWebModule(WebModuleImplementation impl) throws Exception {
        java.lang.reflect.Constructor c = WebModule.class.getDeclaredConstructor(
            new Class[] {WebModuleImplementation.class});
        c.setAccessible(true);
        return (WebModule)c.newInstance(new Object[] {impl});
    }
    
    static class UnpWarWebModuleImplementation implements WebModuleImplementation {
        
        private FileObject docBase;
        private String contextPath;
        
        public UnpWarWebModuleImplementation(FileObject docBase) {
            this.docBase = docBase;
            contextPath = "";
        }

        public FileObject getDocumentBase () {
            return docBase;
        }
    
        public FileObject getJavaSourcesFolder () {
            return docBase.getFileObject("WEB-INF/classes");
        }
    
        public String getContextPath () {
            return contextPath;
        }
    
        public void setContextPath (String path) {
            this.contextPath = path;
        }
        
        public String getJ2eePlatformVersion (){
            return "";
        }
        
        public ClassPath getJavaSources (){
            return null;
        }
    }*/
    
}
