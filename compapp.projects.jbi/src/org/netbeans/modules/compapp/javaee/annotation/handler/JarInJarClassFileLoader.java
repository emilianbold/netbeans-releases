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