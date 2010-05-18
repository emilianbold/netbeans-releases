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