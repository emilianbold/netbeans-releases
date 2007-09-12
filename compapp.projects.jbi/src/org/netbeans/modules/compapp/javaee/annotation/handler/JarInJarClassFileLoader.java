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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.javaee.annotation.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;

/**
 *
 * @author gpatil
 */
public class JarInJarClassFileLoader implements ClassFileLoader{
    private URL jarInJarUrl;
    private String rootClassDir;
    private Logger logger = Logger.getLogger(JarClassFileLoader.class.getName());
    
    /**
     * j - URL to the WEB/EJB archive inside an 'ear'.
     * root - Root of class files, inside a EJB/WEB archive in an 'ear'.
     **/
    public JarInJarClassFileLoader(URL j, String root) {
        this.jarInJarUrl = j;
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
        
    private ClassFile loadClassFileFor(String entryName) throws IOException{
        ClassFile ret = null;
        JarURLConnection jConn = null;        
        JarInputStream jis = null;
        InputStream is = null;
        URLConnection uConn = this.jarInJarUrl.openConnection();
        try {
            if (uConn instanceof JarURLConnection){
                jConn = (JarURLConnection) uConn;
                is = jConn.getInputStream();
                jis = new JarInputStream(is);
                JarEntry je = jis.getNextJarEntry();
                while ((je != null) && (!entryName.equals(je.getName()))){
                    je = jis.getNextJarEntry();
                }
                if (je != null){
                    ret = new ClassFile(jis);
                }
            }
        } finally {
            ProjectUtil.close(jis);
            ProjectUtil.close(is);
        }
        return ret;
    }
    
    public ClassFile getClassFileFromInternalName(String internalClassName){
        ClassFile cf = null;
        internalClassName = getClassPath(internalClassName);
        try {
            cf = loadClassFileFor(rootClassDir + internalClassName);
        } catch (IOException ex){
            logger.warning("Exception while reading class file:" + internalClassName + ":" + ex.getMessage());
        }
        
        return cf;
    }
    
    public ClassFile getClassFile(ClassName className){
        ClassFile cf = null;
        try {
            cf = loadClassFileFor(rootClassDir + getClassPath(className));
        } catch (IOException ex){
            logger.warning("Exception while reading class file:" + className + ":" + ex.getMessage());
        }
        
        return cf;
    }

    public ClassFile getClassFileUsingJarEntry(JarEntry je){
        ClassFile cf = null;
        try {
            cf = loadClassFileFor(je.getName());
        } catch (IOException ex){
            logger.warning("Exception while reading class file:" + je.getName() + ":" + ex.getMessage());
        }
        return cf;
    }    
}