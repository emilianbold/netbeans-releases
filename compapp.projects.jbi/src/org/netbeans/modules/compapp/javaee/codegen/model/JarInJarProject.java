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
package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import org.netbeans.modules.compapp.javaee.annotation.handler.JarInJarClassFileLoader;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;

/**
 *
 * @author gpatil
 */
public class JarInJarProject extends AbstractProject {

    private static Logger logger = Logger.getLogger(JarInJarProject.class.getName());
    private static String WEB_CLASSES_DIR = "WEB-INF/classes/"; //NOI18N
    protected URL jarPathUrl;
    protected String root = "";

    public JarInJarProject(URL jarInJarPath) {
        super();
        this.jarPathUrl = jarInJarPath;
        String strUrl = this.jarPathUrl.toString();
        if (strUrl.endsWith(".war")){ //NOI18N
            this.projType = JavaEEProject.ProjectType.WEB;
            this.root = WEB_CLASSES_DIR;
        } else {
            this.projType = JavaEEProject.ProjectType.EJB;
        }
    }

    @Override
    public void scanForEndpoints() throws IOException {
        URLConnection uConn = this.jarPathUrl.openConnection();
        JarURLConnection jConn = null;
        JarInputStream jis = null;
        InputStream is = null;

        try {
            if (uConn instanceof JarURLConnection) {
                jConn = (JarURLConnection) uConn;
                is = jConn.getInputStream();
                jis = new JarInputStream(is);
                JarEntry je = jis.getNextJarEntry();

                JarInJarClassFileLoader cl = new JarInJarClassFileLoader(
                        this.jarPathUrl, this.root);
                while (je != null) {
                    if (je.getName().endsWith(".class")) {//NOI18N
                        logger.finest("Checking Annotation in:" + je.getName());
                        handleAnnotations(cl, je);
                    }
                    je = jis.getNextJarEntry();
                }
            }
        } finally {
            ProjectUtil.close(jis);
            ProjectUtil.close(is);
        }
    }
}