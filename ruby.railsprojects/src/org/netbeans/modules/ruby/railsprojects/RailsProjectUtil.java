/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.GemFilesParser;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Miscellaneous utilities for the Rails project module.
 *
 * @author Jiri Rechtacek
 */
public class RailsProjectUtil {
    
    private RailsProjectUtil () {}

    /**
     * Gets the contents of the given file as text.
     * 
     * @param toRead
     * @return the contents; an empty string if anything went wrong.
     */
    static String asText(File toRead) {

        BufferedReader fr = null;
        try {
            fr = new BufferedReader(new FileReader(toRead));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = fr.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                sb.append("\n"); // NOI18N
            }
            
            return sb.toString();

        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return "";
    }
    /** Get the version string out of a ruby version.rb file */
    public static String getVersionString(File versionFile) {
        try {
            Pattern VERSION_ELEMENT = Pattern.compile("\\s*[A-Z]+\\s*=\\s*(\\d+)\\s*"); // NOI18N
            BufferedReader br = new BufferedReader(new FileReader(versionFile));
            int major = 0;
            int minor = 0;
            int tiny = 0;
            for (int line = 0; line < 10; line++) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }

                if (s.indexOf("MAJOR") != -1) { // NOI18N
                    Matcher m = VERSION_ELEMENT.matcher(s);
                    if (m.matches()) {
                        major = Integer.parseInt(m.group(1));
                    }
                } else if (s.indexOf("MINOR") != -1) { // NOI18N
                    Matcher m = VERSION_ELEMENT.matcher(s);
                    if (m.matches()) {
                        minor = Integer.parseInt(m.group(1));
                    }
                } else if (s.indexOf("TINY") != -1) { // NOI18N
                    Matcher m = VERSION_ELEMENT.matcher(s);
                    if (m.matches()) {
                        tiny = Integer.parseInt(m.group(1));
                    }
                }
            }
            br.close();
            
        
            return major + "." + minor + "." + tiny; // NOI18N
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        return null;
    }

    /**
     * Gets the rails version the given <code>project</code> uses. Returns
     * version <code>0</code> if the version could not be determined.
     *
     * @param project
     * @return the version; <code>0</code> if unknown, never <code>null</code>.
     */
    public static RailsVersion getRailsVersion(Project project) {
        GemManager gemManager = RubyPlatform.gemManagerFor(project);
        // Add in the builtins first (since they provide some more specific
        // UI configuration for known generators (labelling the arguments etc.)
        String railsVersion = gemManager.getLatestVersion("rails"); // NOI18N

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

        FileObject environment = project.getProjectDirectory().getFileObject("config/environment.rb"); // NOI18N
        if (environment != null && environment.isValid()) {
            String specifiedVersion = getSpecifiedRailsVersion(environment);
            if (specifiedVersion != null) {
                railsVersion = specifiedVersion;
            }
        }

        if (railsVersion == null) {
            return new RailsVersion(0);
        }
        return versionFor(railsVersion);
    }

    /** Return the version of Rails requested in environment.rb */
    public static String getSpecifiedRailsVersion(final FileObject environment) {
        BufferedReader br = null;
        try {
            // Look for version specifications like
            //    RAILS_GEM_VERSION = '2.1.0' unless defined? RAILS_GEM_VERSION
            // in environment.rb
            br = new BufferedReader(new InputStreamReader(environment.getInputStream()));

            Pattern VERSION_PATTERN = Pattern.compile("\\s*RAILS_GEM_VERSION\\s*=\\s*['\"]" + GemFilesParser.VERSION_REGEX + "['\"].*"); // NOI18N
            for (int line = 0; line < 20; line++) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                if (s.indexOf("RAILS_GEM_VERSION") != -1) { // NOI18N
                    Matcher m = VERSION_PATTERN.matcher(s);
                    if (m.matches()) {
                        return m.group(1);
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return null;
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
            prefix += "/"; // NOI18N
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

    /**
     * Parses a <code>RailsVersion</code> from the given <code>version</code>. 
     * The excepted format is <code>"X.Y.Z"</code>, all but the major version
     * being optional. The returned <code>RailsVersion<code> will always have also
     * the minor and revision version specified, both defaulting to <code>0</code>.
     * <strong>Returns a version representing 0.0.0 if the
     * version could not be parsed</strong>.
     * @param version
     * @return
     */
    public static RailsVersion versionFor(String version) {
        try {
            if (!version.contains(".")) { //NOI18N
                return new RailsVersion(Integer.parseInt(version));
            }
            String[] splitted = version.split("\\."); //NOI18N
            if (splitted.length == 2) {
                return new RailsVersion(Integer.parseInt(splitted[0]),
                        Integer.parseInt(splitted[1]));
            } else if (splitted.length >= 3) {
                return new RailsVersion(Integer.parseInt(splitted[0]),
                        Integer.parseInt(splitted[1]),
                        Integer.parseInt(splitted[2]));
            }
        } catch (NumberFormatException ne) {
            return new RailsVersion(0);
        }
        return new RailsVersion(0);

    }

    /**
     * Represents a rails version.
     */
    public static final class RailsVersion implements Comparable<RailsVersion> {
        private final int major;
        private final int minor;
        private final int revision;

        public RailsVersion(int major) {
            this(major, 0);
        }
        public RailsVersion(int major, int minor) {
            this(major, minor, 0);
        }

        public RailsVersion(int major, int minor, int revision) {
            this.major = major;
            this.minor = minor;
            this.revision = revision;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getRevision() {
            return revision;
        }

        public String asString() {
            return getMajor() + "." + getMinor() + "." + getRevision();
        }

        public boolean isRails3OrHigher() {
            return compareTo(new RailsVersion(3)) >= 0;
        }

        public int compareTo(RailsVersion o) {
            if (major > o.major) {
                return 1;
            }
            if (major == o.major) {
                if (minor > o.minor) {
                    return 1;
                }
                if (minor == o.minor) {
                    if (revision > o.revision) {
                        return 1;
                    }
                    return revision == o.revision ? 0 : -1;
                }
            }
            return -1;
        }


    }
}
