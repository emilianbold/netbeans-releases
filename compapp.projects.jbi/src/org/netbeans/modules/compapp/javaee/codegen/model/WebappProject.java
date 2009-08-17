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

/*
 * WebappProject.java
 *
 * Created on October 6, 2006, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.netbeans.modules.compapp.javaee.annotation.ClassInfo;
import org.netbeans.modules.compapp.javaee.annotation.handler.JarClassFileLoader;

/**
 * Scans the WEB-INF folder for any Class files.
 * Scans for JAX-WS annotations.
 * @author gpatil
 */
public class WebappProject extends AbstractProject{
    private static Logger logger = Logger.getLogger(WebappProject.class.getName());
    private static String CLASSES_DIR = "WEB-INF/classes/" ; //NOI18N
    private static String SU_FILE_EXT = "war" ; //NOI18N
    /**
     * Creates a new instance of WebappProject
     */
    public WebappProject(String path2War) {
        super(path2War);
        this.projType = JavaEEProject.ProjectType.WEB;
    }
        
    protected void scanForEndpoints() throws IOException {
        JarFile jf = new JarFile(this.jarPath);
        try {
            JarClassFileLoader cl = new JarClassFileLoader(jf, CLASSES_DIR);        
            Enumeration<JarEntry> jes = jf.entries();
            while(jes.hasMoreElements()){
                JarEntry je = jes.nextElement();
                if (je.getName().startsWith(CLASSES_DIR) && je.getName().endsWith(".class")){ //NOI18N
                    logger.finest("Checking Annotation in:" + je.getName());
                    // Load the class only if annotations are present.
                    if (ClassInfo.containsAnnotation(Channels.newChannel(jf.getInputStream(je)), je.getSize(), annotations)) {
                        logger.finest("Found Annotation in:" + je.getName());
                        handleAnnotations(cl, je);                    
                    }
                }
            }
        } finally {
            if (jf != null){
                try {
                    jf.close();
                } catch (Exception ex){
                    // Ignore
                }
            }
        }
    }

    protected String renameToSvcUnitExtension(String javaEEName){
        StringBuffer ret = new StringBuffer();
        int index = -1;
        if (javaEEName != null){
            index = javaEEName.lastIndexOf("."); //NOI18N
            if (index >= 0){
                ret.append(javaEEName.substring(0, index + 1));
                ret.append(SU_FILE_EXT);
            }
        }
        
        return ret.toString();
    }
    
    protected URL getClassPathURL(){
        URL ret = null;
        try {
            ret = new URL("jar:file:" + this.jarPath + "!/" + CLASSES_DIR);
        } catch (Exception ex){
            logger.warning("Error while getting to to:" + this.jarPath);
        }
        
        return ret;
    }
    
}
