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

package org.netbeans.modules.maven.osgi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class AccessQueryImpl implements AccessibilityQueryImplementation {
    private NbMavenProject mavenProject;
    private Project project;
    private WeakReference<List<Pattern>> ref;
    static List<Pattern> DEFAULT_IMP = Collections.singletonList(Pattern.compile(".*"));
    
    public AccessQueryImpl(Project prj) {
        project = prj;
        mavenProject = prj.getLookup().lookup(NbMavenProject.class);
    }
    
    /**
     *
     * @param pkg
     * @return
     */
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject srcdir = org.netbeans.modules.maven.api.FileUtilities.convertStringToFileObject(mavenProject.getMavenProject().getBuild().getSourceDirectory());
        if (srcdir != null) {
            String path = FileUtil.getRelativePath(srcdir, pkg);
            if (path != null) {
                String name = path.replace('/', '.');
                return check(name);
            }
        }
        
        return null;
    }
    
    private Boolean check(String value) {
        boolean matches = false;
        String[] exps = PluginPropertyUtils.getPluginPropertyList(mavenProject.getMavenProject(),
                OSGIConstants.GROUPID_FELIX, OSGIConstants.ARTIFACTID_BUNDLE_PLUGIN,
                "instructions", "Export-Package", "manifest");
        String[] imps = PluginPropertyUtils.getPluginPropertyList(mavenProject.getMavenProject(),
                OSGIConstants.GROUPID_FELIX, OSGIConstants.ARTIFACTID_BUNDLE_PLUGIN,
                "instructions", "Private-Package", "manifest");
        String exp = null;
        if (exps != null && exps.length == 1) {
            exp = exps[0];
        }
        String imp = null;
        if (imps != null && imps.length == 1) {
            imp = imps[0];
        }
        if (exp != null) {
            List<Pattern> pubPatt = preparePackagesPatterns(exp);
            for (Pattern pattern : pubPatt) {
                matches = pattern.matcher(value).matches();
                if (matches) {
                    return Boolean.TRUE;
                }
            }
        } 
        List<Pattern> privPatt = imp != null ? preparePackagesPatterns(imp) : DEFAULT_IMP;
        for (Pattern pattern : privPatt) {
            matches = pattern.matcher(value).matches();
            if (matches) {
                return Boolean.FALSE;
            }
        }
        if (exp == null) {
            //handle default behaviour if not defined..
            //TODO handle 1.x bundle plugin defaults..
            if (!value.contains(".impl") && !value.contains(".internal")) { //NOI18N
                return Boolean.TRUE;
            }

        }
        return null;
    }
    
    
    static List<Pattern> preparePackagesPatterns(String value) {
        List<Pattern> toRet = new ArrayList<Pattern>();
        if (value != null) {
            StringTokenizer tok = new StringTokenizer(value, " ,", false); //NOI18N
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                token = token.trim();
                boolean recursive = false;
                if (token.endsWith(".*")) { //NOI18N
                    token = token.substring(0, token.length() - ".*".length()); //NOI18N
                    recursive = true;
                }
                token = token.replace(".","\\."); //NOI18N
                if (recursive) {
                    token = token + ".*"; //NOI18N
                }
                toRet.add(Pattern.compile(token));
            }
        }
        return toRet;
    }
    
}
