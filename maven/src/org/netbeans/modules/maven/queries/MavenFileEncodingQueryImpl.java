/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class MavenFileEncodingQueryImpl extends  FileEncodingQueryImplementation {

    private final NbMavenProjectImpl project;
    private static final String ENCODING_PARAM = "encoding"; //NOI18N
    
    public MavenFileEncodingQueryImpl(NbMavenProjectImpl proj) {
        project = proj;
    }

    @Override
    public Charset getEncoding(FileObject file) {
        MavenProject mp = project.getOriginalMavenProject();
        if (mp == null) {
            return Charset.defaultCharset();
        }
        try {
            String defEnc = mp.getProperties().getProperty(Constants.ENCODING_PROP);
            //TODO instead of SD
            FileObject src = FileUtilities.convertStringToFileObject(mp.getBuild().getSourceDirectory());
            if (src != null && (src.equals(file) || FileUtil.isParentOf(src, file))) {
                String compileEnc = PluginPropertyUtils.getPluginProperty(project,
                        Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, ENCODING_PARAM, "compile"); //NOI18N;
                if (compileEnc != null && compileEnc.indexOf("${") == -1) { //NOI18N - guard against unresolved values.
                    return Charset.forName(compileEnc);
                }
                if (defEnc != null) {
                    return Charset.forName(defEnc);
                }
            }
            FileObject testsrc = FileUtilities.convertStringToFileObject(mp.getBuild().getTestSourceDirectory());
            if (testsrc != null && (testsrc.equals(file) || FileUtil.isParentOf(testsrc, file))) {
                String testcompileEnc = PluginPropertyUtils.getPluginProperty(project,
                        Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, ENCODING_PARAM, "testCompile"); //NOI18N
                if (testcompileEnc != null && testcompileEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                    return Charset.forName(testcompileEnc);
                }
                if (defEnc != null) {
                    return Charset.forName(defEnc);
                }
            }

            //possibly more complicated with resources, one can have explicit declarations in the
            // pom plugin configuration.
            try {
                if (isWithin(project.getResources(false), file)) {
                    String resourceEnc = PluginPropertyUtils.getPluginProperty(project,
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES, ENCODING_PARAM, "resources"); //NOI18N
                    if (resourceEnc != null && resourceEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                        return Charset.forName(resourceEnc);
                    }
                    if (defEnc != null) {
                        return Charset.forName(defEnc);
                    }
                }

            } catch (MalformedURLException x) {
                Exceptions.printStackTrace(x);
            }

            try {
                if (isWithin(project.getResources(true), file)) {
                    String testresourceEnc = PluginPropertyUtils.getPluginProperty(project,
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES, ENCODING_PARAM, "testResources"); //NOI18N
                    if (testresourceEnc != null && testresourceEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                        return Charset.forName(testresourceEnc);
                    }
                    if (defEnc != null) {
                        return Charset.forName(defEnc);
                    }
                }
            } catch (MalformedURLException malformedURLException) {
                Exceptions.printStackTrace(malformedURLException);
            }

            try {
                if (isWithin(new URI[]{project.getSiteDirectory()}, file)) {
                    String siteEnc = PluginPropertyUtils.getPluginProperty(project,
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SITE, "inputEncoding", "site"); //NOI18N
                    if (siteEnc != null && siteEnc.indexOf("${") == -1) {//NOI18N - guard against unresolved values.
                        return Charset.forName(siteEnc);
                    }
                    if (defEnc != null) {
                        return Charset.forName(defEnc);
                    }
                }
            } catch (MalformedURLException malformedURLException) {
                Exceptions.printStackTrace(malformedURLException);
            }

            if (defEnc != null) {
                return Charset.forName(defEnc);
            }
        } catch (UnsupportedCharsetException uce) {
            Logger.getLogger(MavenFileEncodingQueryImpl.class.getName()).log(Level.FINE, uce.getMessage(), uce);
        } catch (IllegalCharsetNameException icne) {
            Logger.getLogger(MavenFileEncodingQueryImpl.class.getName()).log(Level.FINE, icne.getMessage(), icne);
        }
        return Charset.defaultCharset();
    }
    
    private boolean isWithin(URI[] res, FileObject file) throws MalformedURLException {
        for (URI ur : res) {
            FileObject fo = URLMapper.findFileObject(ur.toURL());
            if (fo != null && (fo.equals(file) || FileUtil.isParentOf(fo, file))) {
                return true;
            } 
        }
        return false;
        
    }
    

}
