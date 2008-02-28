/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.railsprojects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Miscellaneous utilities for the Rails project module.
 * @author  Jiri Rechtacek
 */
public class RailsProjectUtil {
    
    private RailsProjectUtil () {}

    /** Get the version string out of a ruby version.rb file */
    public static String getVersionString(File versionFile) {
        try {
            Pattern VERSION_ELEMENT = Pattern.compile("\\s*[A-Z]+\\s*=\\s*(\\d+)\\s*");
            BufferedReader br = new BufferedReader(new FileReader(versionFile));
            StringBuilder sb = new StringBuilder();
            int major = 0;
            int minor = 0;
            int tiny = 0;
            for (int line = 0; line < 10; line++) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }

                if (s.indexOf("MAJOR") != -1) {
                    Matcher m = VERSION_ELEMENT.matcher(s);
                    if (m.matches()) {
                        major = Integer.parseInt(m.group(1));
                    }
                } else if (s.indexOf("MINOR") != -1) {
                    Matcher m = VERSION_ELEMENT.matcher(s);
                    if (m.matches()) {
                        minor = Integer.parseInt(m.group(1));
                    }
                } else if (s.indexOf("TINY") != -1) {
                    Matcher m = VERSION_ELEMENT.matcher(s);
                    if (m.matches()) {
                        tiny = Integer.parseInt(m.group(1));
                    }
                }
            }
            br.close();
            
        
            return major + "." + minor + "." + tiny;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        return null;
    }
    
    public static String getRailsVersion(Project project) {
        GemManager gemManager = RubyPlatform.gemManagerFor(project);
        // Add in the builtins first (since they provide some more specific
        // UI configuration for known generators (labelling the arguments etc.)
        String railsVersion = gemManager.getVersion("rails"); // NOI18N

        FileObject railsPlugin = project.getProjectDirectory().getFileObject("vendor/rails/railties"); // NOI18N
        if (railsPlugin != null) {
            FileObject versionFo = railsPlugin.getFileObject("lib/rails/version.rb"); // NOI18N
            if (versionFo != null) {
                File versionFile = FileUtil.toFile(versionFo);
                String version = RailsProjectUtil.getVersionString(versionFile);
                if (version != null) {
                    railsVersion = version;
                }
            }
        }
        
        return railsVersion;
    }
    
    /**
     * Returns the property value evaluated by RailsProject's PropertyEvaluator.
     *
     * @param p project
     * @param value of property
     * @return evaluated value of given property or null if the property not set or
     * if the project doesn't provide RakeProjectHelper
     */    
    public static Object getEvaluatedProperty(Project p, String value) {
        if (value == null) {
            return null;
        }
        RailsProject j2seprj = p.getLookup().lookup(RailsProject.class);
        if (j2seprj != null) {
            return j2seprj.evaluator().evaluate(value);
        } else {
            return null;
        }
    }
    
    public static void getAllScripts(String prefix, FileObject sourcesRoot, List<String> result) {
        FileObject children[] = sourcesRoot.getChildren();
        if (!"".equals(prefix)) {
            prefix += "/";
            //prefix += ".";
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i].isData()) {
                if (children[i].getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                    result.add(prefix + children[i].getNameExt());
                }
            }
            if (children[i].isFolder()) {
                getAllScripts(prefix + children[i].getNameExt(), children[i], result);
            }
        }
    }
    
    
    /**
     * Creates an URL of a classpath or sourcepath root
     * For the existing directory it returns the URL obtained from {@link File#toUri()}
     * For archive file it returns an URL of the root of the archive file
     * For non existing directory it fixes the ending '/'
     * @param root the file of a root
     * @param offset a path relative to the root file or null (eg. src/ for jar:file:///lib.jar!/src/)" 
     * @return an URL of the root
     * @throws MalformedURLException if the URL cannot be created
     */
    public static URL getRootURL (File root, String offset) throws MalformedURLException {
        URL url = root.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        } else if (!root.exists()) {
            url = new URL(url.toExternalForm() + "/"); // NOI18N
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }
}
