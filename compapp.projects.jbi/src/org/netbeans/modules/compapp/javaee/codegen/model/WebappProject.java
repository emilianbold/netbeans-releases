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
 * @TODO Scaning any other jars embedded inside WEB-INF\lib.
 * @author gpatil
 */
public class WebappProject extends AbstractProject{
    private static Logger logger = Logger.getLogger(WebappProject.class.getName());
    private static String CLASSES_DIR = "WEB-INF/classes/" ;
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
                if (je.getName().startsWith(CLASSES_DIR) && je.getName().endsWith(".class")){
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
