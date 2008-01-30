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
package org.netbeans.modules.ruby.platform.gems;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Parameters;

/**
 * A helper class for parsing the names and version numbers of 
 * files representing Gems and choosing the newest versions of those.
 * 
 * @author Erno Mononen
 */
class GemFilesParser {

    /**
     * Extension of files containing gems specification residing in {@link
     * #SPECIFICATIONS}.
     */
    private static final String DOT_GEM_SPEC = ".gemspec"; // NOI18N
    /**
     * The pattern for capturing the gem name and version from file names.
     */
    private static final Pattern PATTERN = Pattern.compile("([\\w-]+)\\-([\\d.]+)"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(GemFilesParser.class.getName());
    /**
     * The files to check for gems.
     */
    private final File[] specFiles;
    
    /**
     * Key => gem name, value => (key => gem version, value => gem file).
     */
    private Map<String, Map<String, File>> resultMap;

    /**
     * Constructs a new GemFilesParser for the given <code>files</code>.
     */
    public GemFilesParser(File... specFiles) {
        Parameters.notNull("files", specFiles); //NOI18N
        this.specFiles = specFiles;
    }

    /**
     * Finds the files representing gems and chooses the newest version of each. 
     * Use {@link getFiles()} and {@link getFiles()} to retrieve the results.
     */
    public void chooseGems() {

        resultMap = new HashMap<String, Map<String, File>>();

        for (File spec : specFiles) {
            // See if it looks like a gem
            String fileName = spec.getName();
            if (!fileName.endsWith(DOT_GEM_SPEC)) {
                continue;
            }

            fileName = fileName.substring(0, fileName.length() - DOT_GEM_SPEC.length());

            GemInfo gemInfo = parseInfo(fileName);
            if (gemInfo == null) {
                LOGGER.fine("Could not resolve the name and version for " + fileName);
                continue;
            }

            Map<String, File> nameMap = resultMap.get(gemInfo.getName());

            if (nameMap == null) {
                nameMap = new HashMap<String, File>();
                resultMap.put(gemInfo.getName(), nameMap);
                nameMap.put(gemInfo.getVersion(), spec);
            } else {
                // Decide whether this version is more recent than the one already there
                String oldVersion = nameMap.keySet().iterator().next();

                if (GemManager.compareGemVersions(gemInfo.getVersion(), oldVersion) > 0) {
                    // New version is higher
                    nameMap.clear();
                    nameMap.put(gemInfo.getVersion(), spec);
                }
            }
        }
    }

    private void checkInitialiazed() {
        if (resultMap == null) {
            throw new IllegalStateException("Not initialized, you must run the chooseGems method first");
        }
    }

    // todo: javadoc + rename
    public Map<String, Map<String, File>> getGemMap() {
        checkInitialiazed();
        return resultMap;
    }

    /**
     * @return the found gem files.
     */
    public File[] getFiles() {

        checkInitialiazed();

        List<File> resultList = new ArrayList<File>();

        for (Map<String, File> map : resultMap.values()) {
            for (File f : map.values()) {
                resultList.add(f);
            }
        }
        return resultList.toArray(new File[resultList.size()]);

    }

    // not private because used in tests
    static GemInfo parseInfo(String fileName) {
        Matcher m = PATTERN.matcher(fileName);
        if (!m.find() || m.groupCount() < 2) {
            //XXX: can there be gems without a version number?
            return null;
        }
        return new GemInfo(m.group(1), m.group(2));
    }

    static final class GemInfo {

        private final String name;
        private final String version;

        public GemInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }
}
