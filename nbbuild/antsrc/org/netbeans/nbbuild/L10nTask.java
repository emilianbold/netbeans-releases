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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * This task was created to create L10N kits.
 * The xml to call this task might look like:
 * <l10nTask topdirs="${f4jroot} ${nbroot}" modules="${all_modules}"
 *           localizableFile="${localizableF}" generatedFile="${genFile}"
 *           changedFile="${changedFile}" buildDir="${l10n-build}"
 *           distDir="${l10n-dist}" buildNumber="${buildnumber}"
 *           globalFile=""/>
 *
 *
 * Each resulting kit will be a tar file containing one directory
 *   for each repository examined.
 * Each repository_directory will contain one tar-file
 *   for each module with changed localizable files.
 * The structure of the tar file is as follows:
 * repository_dir/
 *                 module_tar/
 *                            generatedFile
 *                            contentFile
 *                            localizableFile1.html
 *                            localizableFile2.gif...
 *
 * 1.  All localizable files (as defined in "localizableFile")
 *     "localizableFile" is a list of file patterns difining all localizable source files.
 *     "localizableFile" itself is not included in the kit.
 * 2.  A content file (as named in "contentFile")
 *     This is a list of the names of all localizable files which need to be (re)localized.
 * 3.  A generated file (as named in "generatedFile")
 *     This is a list of the names of all localizable files with the most recent
 *     CVS revision number.
 *
 * The generated file need not be committed to cvs until the files come back
 * localized. (This is a change from the procedure followed in late 2001).
 *
 * As of version 1.1.2, converted Gzip methods to use ant 1.4.1, and added ability to
 * 1) embed exclude patterns in the l10n.list file
 *		(must use keyword "exclude " followed by the pattern.)
 * 2) add ability to embed reading of additional files
 *		(must use the keyword "read " followed by the path to the file)
 *		The special case "read global" will read the file set by the property globalFile
 * 3) add the ability to embed properties in the patterns. i
 *		The special case "l10n-module" property will be set from within L10nTask
 *
 * @author Erica Grevemeyer
 * @version 1.1,	Feb  4 11:21:09 PST 2002
 * @version 1.1.2	Aug 29 15:23:51 PDT 2002
 */
public class L10nTask extends Task {

    File nbmsDir = null;
    File tmpDir = null;
    File patternsFile = null;

    public void execute() throws BuildException {
        LineNumberReader lnr = null;
        try {
            if (nbmsDir == null) {
                throw new BuildException("Required variable not set.  Set 'nbmsdir' in the calling build script file");
            }
            if (!nbmsDir.exists() || !nbmsDir.isDirectory()) {
                throw new BuildException("'nbmsdir' has to exist and be directory where are all NBMs stored");
            }
            if (patternsFile == null) {
                throw new BuildException("Required variable not set.  Set 'patternsFile' in the calling build script file");
            }
            if (!patternsFile.exists() || !patternsFile.isFile()) {
                throw new BuildException("'patternsFile' has to exist and be file with patterns what should be included in the kit");
            }
            lnr = new LineNumberReader(new FileReader(patternsFile));
            String line = null;
            Map<String, Set<String>> includes = new HashMap<String, Set<String>>();
            Map<String, Set<String>> excludes = new HashMap<String, Set<String>>();

            //Read all the patterns from patternsFile
            while ((line = lnr.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                if (!line.startsWith("exclude ")) {  //Include pattern

                    String[] p = line.split(":");
                    if (p.length != 2) {
                        if (line.endsWith(":")) {
                            includes.put(line.substring(0, line.length() - 1), null);
                            continue;
                        } else {
                            throw new BuildException("Wrong pattern '" + line + "' found in pattern file: " + patternsFile.getAbsolutePath());
                        }
                    }
                    Set<String> files = includes.get(p[0]);
                    if (files == null) {
                        files = new HashSet<String>();
                        includes.put(p[0], files);
                    }
                    files.add(p[1]);
                } else {        //Exlude pattern

                    line = line.substring("exclude ".length());
                    String[] p = line.split(":");
                    if (p.length != 2) {
                        if (line.endsWith(":")) {
                            excludes.put(line.substring(0, line.length() - 1), null);
                            continue;
                        } else {
                            throw new BuildException("Wrong pattern '" + line + "' found in pattern file: " + patternsFile.getAbsolutePath());
                        }
                    }
                    Set<String> files = excludes.get(p[0]);
                    if (files == null) {
                        files = new HashSet<String>();
                        excludes.put(p[0], files);
                    }
                    files.add(p[1]);
                }
            }
            lnr.close();

            //Unzip all the NBMs
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(nbmsDir);
            ds.setIncludes(new String[]{"**/*.nbm"});
            ds.scan();
            String[] nbms = ds.getIncludedFiles();
            Expand unzip = (Expand) getProject().createTask("unzip");
            for (String nbm : nbms) {
                File nbmFile = new File(nbmsDir, nbm);
                File nbmDir = new File(tmpDir, nbm);
                nbmDir.mkdirs();
                unzip.setSrc(nbmFile);
                unzip.setDest(nbmDir);
                unzip.execute();
            }
            ds.setBasedir(tmpDir);
            String[] includesKeys = includes.keySet().toArray(new String[]{""});
            String[] excludesKeys = excludes.keySet().toArray(new String[]{""});
            if (includesKeys[0] != null) {
                ds.setIncludes(includesKeys);
            }
            if (excludesKeys[0] != null) {
                ds.setExcludes(excludesKeys);
            }

            //Go though all the found files maching the first part of the pattern
            ds.scan();
            Zip zip = (Zip) getProject().createTask("zip");
            zip.setDestFile(new File(getProject().getBaseDir(), "l10n.zip"));
            for (String file : ds.getIncludedFiles()) {
                ZipFileSet zipFileSet = new ZipFileSet();
                boolean matching = false;
                for (String include : includesKeys) {
                    if (SelectorUtils.matchPath(include, file)) {
                        Set<String> incPattern = includes.get(include);
                        if (incPattern != null) {
                            matching = true;
                            zipFileSet.appendIncludes(incPattern.toArray(new String[]{""}));
                        } else {
                            FileSet fileSet = new FileSet();
                            fileSet.setDir(tmpDir);
                            fileSet.setIncludes(file);
                            zip.addFileset(fileSet);
                        }
                    }
                }
                if (matching) {
                    for (String exclude : excludesKeys) {
                        if (SelectorUtils.matchPath(exclude, file)) {
                            Set<String> excPattern = excludes.get(exclude);
                            if (excPattern != null) {
                                zipFileSet.appendExcludes(excPattern.toArray(new String[]{""}));
                            }
                        }
                    }

                    File oneFile = new File(tmpDir, file);
                    zipFileSet.setSrc(oneFile);
                    zipFileSet.setPrefix(file);
                    zip.addZipfileset(zipFileSet);
                }
            }
            zip.execute();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(L10nTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(L10nTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                lnr.close();
            } catch (IOException ex) {
                Logger.getLogger(L10nTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setNbmsdir(File nbmsDir) {
        this.nbmsDir = nbmsDir;
    }

    public void setTmpdir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setPatternsFile(File patternsFile) {
        this.patternsFile = patternsFile;
    }
}
