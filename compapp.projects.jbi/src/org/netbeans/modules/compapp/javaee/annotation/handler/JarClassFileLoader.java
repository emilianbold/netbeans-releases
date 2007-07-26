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
public class JarClassFileLoader {
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
