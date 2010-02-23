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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
public final class GemFilesParser {

    /**
     * Extension of files containing gems specification residing in {@link
     * #SPECIFICATIONS}.
     */
    private static final String DOT_GEM_SPEC = ".gemspec"; // NOI18N
    /**
     * The pattern for capturing the gem name and version from file names.
     */
    private static final Pattern PATTERN = Pattern.compile("([\\w-]+)\\-(\\d(?:\\.\\d)*(\\.beta)?)"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(GemFilesParser.class.getName());
    /**
     * The files to check for gems.
     */
    private final File[] specFiles;
    
    /**
     * Key => gem name, value => List of GemInfos representing installed versions.
     */
    private Map<String, List<GemInfo>> resultMap;

    /**
     * Constructs a new GemFilesParser for the given <code>files</code>.
     */
    public GemFilesParser(File... specFiles) {
        Parameters.notNull("files", specFiles); //NOI18N
        this.specFiles = specFiles;
    }

    /**
     * Finds the files representing installed gems.
     * Use {@link getFiles()} and {@link getGemInfos()} to retrieve the results.
     */
    public void parseGems() {

        resultMap = new HashMap<String, List<GemInfo>>();

        for (File spec : specFiles) {
            // See if it looks like a gem
            String fileName = spec.getName();
            if (!fileName.endsWith(DOT_GEM_SPEC)) {
                continue;
            }

            fileName = fileName.substring(0, fileName.length() - DOT_GEM_SPEC.length());

            String[] nameAndVersion = parseNameAndVersion(fileName);
            if (nameAndVersion == null) {
                LOGGER.fine("Could not resolve the name and version for " + fileName);
                continue;
            }

            String name = nameAndVersion[0];
            String version = nameAndVersion[1];
            
            List<GemInfo> versions = resultMap.get(name);
            
            if (versions == null) {
                versions = new ArrayList<GemInfo>();
                resultMap.put(name, versions);
            } 
            versions.add(new GemInfo(name, version, spec));
        }
        sortVersions();
    }

    private void sortVersions() { 
        for (String key : resultMap.keySet()) {
            List<GemInfo> versions = resultMap.get(key);
            Collections.sort(versions);
        }
    }
    
    private void checkInitialiazed() {
        if (resultMap == null) {
            throw new IllegalStateException("Not initialized, you must run the chooseGems method first");
        }
    }

    // todo: javadoc + rename
    public static Map<String, List<GemInfo>> getGemInfos(final File[] specFiles) {
        GemFilesParser gemFilesParser = new GemFilesParser(specFiles);
        gemFilesParser.parseGems();
        return gemFilesParser.getGemInfos();
    }
    
    Map<String, List<GemInfo>> getGemInfos() {
        checkInitialiazed();
        return resultMap;
    }

    /**
     * @return the found gem files.
     */
    public File[] getFiles(boolean onlyLatestVersions) {

        checkInitialiazed();

        List<File> resultList = new ArrayList<File>();

        for (String key : resultMap.keySet()) {
            List<GemInfo> versions = resultMap.get(key);
            if (onlyLatestVersions) {
                resultList.add(versions.get(0).getSpecFile());
            } else {
                for (GemInfo each : versions) {
                    resultList.add(each.getSpecFile());
                }
            }
                    
        }
        return resultList.toArray(new File[resultList.size()]);

    }

    /**
     * Parses the gem name and version from the given file name.
     * @param fileName the file name to parse, e.g. my-gem-1.2.3
     * @return a string array of length 2 containing the name [0] and version [1]
     * or <code>null</code> if parsing was unsuccessful.
     */
    public static String[] parseNameAndVersion(String fileName) {
        Matcher m = PATTERN.matcher(fileName);
        if (!m.find() || m.groupCount() < 2) {
            //XXX: can there be gems without a version number?
            LOGGER.fine("Couldn't parse name and version for " + fileName);
            return null;
        }
        String name = m.group(1);
        String version = m.group(2);
        return new String[]{name, version};
    }

    /**
     * Parses the gem name and version from the given URL.
     * @param gemUrl the url to parse; must represent an URL of a gem.
     * @return a string array of length 2 containing the name [0] and version [1]
     * or <code>null</code> if parsing was unsuccessful.
     */
    public static String[] parseNameAndVersion(URL gemUrl) {
        return parseNameAndVersion(Gems.getGemName(gemUrl));
    }

}
