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
 * JarClassFileLoader.java
 *
 * Created on December 13, 2006, 6:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.annotation.handler;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;

/**
 *
 * @author gpatil
 */
public class JarClassFileLoader implements ClassFileLoader{
    private JarFile jf;
    private String rootClassDir;
    private Logger logger = Logger.getLogger(JarClassFileLoader.class.getName());
    
    public JarClassFileLoader(JarFile j, String root) {
        this.jf = j;
        this.rootClassDir = root;
    }
    
    private String getClassPath(ClassName cn){
        String ret = cn.getExternalName(true).replaceAll("\\.", "\\/");//NOI18N
        ret = ret + ".class" ; //NOI18N
        return ret;
    }
    
    private String getClassPath(String internalClassName){
        return getClassPath(ClassName.getClassName(internalClassName));
    }
    
    public ClassFile getClassFileFromInternalName(String internalClassName){
        ClassFile cf = null;
        internalClassName = getClassPath(internalClassName);
        try {
            JarEntry je = jf.getJarEntry(rootClassDir + internalClassName); // No I18N
            cf = new ClassFile(jf.getInputStream(je));
        } catch (IOException ex){
            logger.warning("Exception while reading class file:" + internalClassName + ":" + ex.getMessage());
        }
        
        return cf;
    }
    
    public ClassFile getClassFile(ClassName className){
        ClassFile cf = null;
        try {
            JarEntry je = jf.getJarEntry(rootClassDir + getClassPath(className)); // No I18N
            cf = new ClassFile(jf.getInputStream(je));
        } catch (IOException ex){
            logger.warning("Exception while reading class file:" + className + ":" + ex.getMessage());
        }
        
        return cf;
    }

    public ClassFile getClassFileUsingJarEntry(JarEntry je){
        ClassFile cf = null;
        try {
            cf = new ClassFile(jf.getInputStream(je));
        } catch (IOException ex){
            logger.warning("Exception while reading class file:" + je.getName() + ":" + ex.getMessage());
        }
        return cf;
    }    
}
