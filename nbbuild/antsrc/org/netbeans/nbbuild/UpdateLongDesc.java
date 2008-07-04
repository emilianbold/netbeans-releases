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
package org.netbeans.nbbuild;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * @author rbalada
 */
public class UpdateLongDesc extends Task {

    private String keyword = null;
    private String l10nDirStr = null;
    private String l10nFileStr = null;
    private String locales = null;
    private String workRootStr = null;
    private File workRoot = null;
    private File l10nDir = null;
    private File l10nFile = null;

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setL10nDir(String l10nDir) {
        this.l10nDirStr = l10nDir;
    }

    public void setL10nFile(String l10nFile) {
        this.l10nFileStr = l10nFile;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    public void setWorkRoot(String workRoot) {
        this.workRootStr = workRoot;
    }

    private static String native2ascii(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= 0xFF) {
                sb.append(c);
            } else {
                sb.append("\\u");
                String tmp = Integer.toHexString((int) c);
                while (tmp.length() < 4) {
                    tmp = "0" +tmp;
                }
                sb.append(tmp);
            }
        }
        return sb.toString();
    }

    public 
    @Override
    void execute() throws BuildException {
        if (keyword == null) {
            throw new BuildException("You have to set keyword to look for in OpenIDE-Module-Long-Description within bundles to modify.", getLocation());
        }
        if (l10nDirStr == null) {
            throw new BuildException("You have to set parameter 'l10ndir' to specify where to look for localized bundles under workroot.", getLocation());
        }
        if (l10nFileStr == null) {
            throw new BuildException("You have to set parameter 'l10nfile' to specify localized values of OpenIDE-Module-Long-Description keyword.", getLocation());
        }
        if (locales == null) {
            throw new BuildException("You have to set parameter 'locales' to specify in which locales you wish to make the change. Use value 'default' for default languge (en).", getLocation());
        }
        if (workRootStr == null) {
            throw new BuildException("You have to set parameter 'workroot' to specify root of working directory. It can be relative to project basedir.", getLocation());
        }
        workRoot = new File(workRootStr);
        if (!(workRoot.exists())) {
            workRoot = new File(getProject().getBaseDir(), workRootStr);
            if (!(workRoot.exists())) {
                throw new BuildException("Unable to find work root directory '" + workRootStr + "'.", getLocation());
            }
        }
        l10nDir = new File(workRoot, l10nDirStr);
        if ((!(l10nDir.exists())) && (!(locales.equals("default")))) {
            throw new BuildException("Unable to find specified l10n directory '" + l10nDirStr + "' as directory '" + l10nDir.getAbsolutePath() + "'.", getLocation());
        }
        l10nFile = new File(getProject().getBaseDir(), l10nFileStr);
        if (!(l10nFile.exists())) {
            throw new BuildException("Unable to find specified l10n property file '" + l10nFileStr + "' as '" + l10nFile.getAbsolutePath() + "'.", getLocation());
        }
        FileInputStream fis;
        Properties props = new Properties();
        try {
            fis = new FileInputStream(l10nFile);
            props.load(fis);
            fis.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
            throw new BuildException("Unable to read l10nfile property file '" + l10nFile.getAbsolutePath() + "'.", ex, getLocation());
        } catch (IOException ex) {
            Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
            throw new BuildException("Unable to read l10nfile property file '" + l10nFile.getAbsolutePath() + "'.", ex, getLocation());
        }
        log("Going to update bundle properties with PatchesInfo reference", Project.MSG_VERBOSE);

        StringTokenizer st = new StringTokenizer(locales, ",");
        File currentWorkRoot;
        String locale;
        String searchMask;
        String propKey;
        String propVal;
        Properties bprops = new Properties();
        String bVal;
        File bundleFile;
        FileOutputStream fos;
        while (st.hasMoreTokens()) {
            locale = st.nextToken();
            log("Checking bundles for locale '" + locale + "'.", Project.MSG_VERBOSE);
            if (locale.equals("default")) {
                currentWorkRoot = workRoot;
                searchMask = "**/Bundle.properties";
                propKey = "OpenIDE-Module-Long-Description";
            } else {
                currentWorkRoot = l10nDir;
                searchMask = "**/Bundle_" + locale + ".properties";
                propKey = "OpenIDE-Module-Long-Description" + "_" + locale;
            }
            propVal = props.getProperty(propKey, null);
            if (propVal == null) {
                throw new BuildException("Missing translation of OpenIDE-Module-Long-Description value for locale '" + locale + "' in l10nfile '" + l10nFile.getAbsolutePath() + "'. Please check property key '" + propKey + "' exists and has reasonable value.", getLocation());
            }
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(currentWorkRoot);
            ds.setIncludes(new String[]{searchMask});
            log("Scanning work root directory '" + currentWorkRoot.getAbsolutePath() + "' for files matching search mask '" + searchMask + "'", Project.MSG_VERBOSE);
            ds.scan();
            String[] bundles = ds.getIncludedFiles();
            log("Found " + bundles.length + " bundles to check", Project.MSG_VERBOSE);
            for (String bundle : bundles) {
                bprops.clear();
                bundleFile = new File(currentWorkRoot, bundle);
                log("Checking bundle file " + bundleFile.getAbsolutePath(), Project.MSG_VERBOSE);
                try {
                    fis = new FileInputStream(bundleFile);
                    bprops.load(fis);
                    fis.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                    throw new BuildException("Unable to read bundle property file '" + bundleFile.getAbsolutePath() + "'.", ex, getLocation());
                } catch (IOException ex) {
                    Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                    throw new BuildException("Unable to read bundle property file '" + bundleFile.getAbsolutePath() + "'.", ex, getLocation());
                }
                if (bprops.getProperty("OpenIDE-Module-Long-Description", null) == null) {
                    log("Bundle file '" + bundleFile.getAbsolutePath() + "' does not contain key OpenIDE-Module-Long-Description", Project.MSG_VERBOSE);
                    continue;
                }
                bVal = bprops.getProperty("OpenIDE-Module-Long-Description");
                if (bVal.contains(keyword)) {
                    log("Bundle file '" + bundleFile.getAbsolutePath() + "' contains key OpenIDE-Module-Long-Description, key it's value already contains required keyword '" + keyword + "'", Project.MSG_VERBOSE);
                    log("DEBUG: " + bundleFile.getAbsolutePath() + ": OpenIDE-Module-Long-Description: " + bVal + "'", Project.MSG_DEBUG);
                    continue;
                }
                File writeFile = new File(bundleFile.getParent(), bundleFile.getName() + ".tmp");
                log("Opening temporary file '" + writeFile.getAbsolutePath() + "' for writing", Project.MSG_DEBUG);
                if (writeFile.exists()) {
                    writeFile.delete();
                }
                try {
                    fos = new FileOutputStream(writeFile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                    throw new BuildException("Unable to write bundle property file '" + bundleFile.getAbsolutePath() + "'.", ex, getLocation());
                }
                try {
                    fis = new FileInputStream(bundleFile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                    throw new BuildException("Unable to read bundle property file '" + bundleFile.getAbsolutePath() + "'.", ex, getLocation());
                } catch (IOException ex) {
                    Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                    throw new BuildException("Unable to read bundle property file '" + bundleFile.getAbsolutePath() + "'.", ex, getLocation());
                }
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br;
                DataOutputStream dos = new DataOutputStream(fos);
                BufferedWriter brw;
                String strLine;
//                try {
                br = new BufferedReader(new InputStreamReader(dis));
                /*                } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                throw new BuildException("Unable to read original bundle file in UTF-8 charset", ex, getLocation());
                }
                try {*/
                brw = new BufferedWriter(new OutputStreamWriter(dos));
                /*                } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                throw new BuildException("Unable to write modified bundle file in UTF-8 charset", ex, getLocation());
                }*/
                Charset cs = Charset.forName("UTF-8");
                try {
                    while ((strLine = br.readLine()) != null) {
                        if (strLine.startsWith("OpenIDE-Module-Long-Description")) {
                            log("DEBUG: Original line: " + strLine, Project.MSG_VERBOSE);
                            String nVal = propVal.replace("\n", "\\n\\");
                            String wText = native2ascii("OpenIDE-Module-Long-Description=" + nVal + " \\");
                            log("DEBUG: Writing value: " + wText, Project.MSG_VERBOSE);
                            brw.write(wText);
                            brw.newLine();
                            wText = native2ascii(bVal);
                            log("DEBUG: Writing value: " + wText, Project.MSG_VERBOSE);
                            brw.write(wText);
                            brw.newLine();
                            if (strLine.endsWith("\\")) {
                                while (((strLine = br.readLine()) != null) && (strLine.endsWith("\\"))) {
                                    log("Skipping line '" + strLine + "'", Project.MSG_VERBOSE);
                                }
                                if (strLine != null) {
                                    log("Skipping line '" + strLine + "'", Project.MSG_VERBOSE);
                                }
                            }
                        } else {
                            //log("DEBUG: Writing line: "+strLine,Project.MSG_DEBUG);
                            brw.write(strLine);
                            brw.newLine();
                        }
                    }
                    brw.flush();
                    brw.close();
                    dos.flush();
                    dos.close();
                    fos.flush();
                    fos.close();
                    br.close();
                    dis.close();
                    fis.close();
                    log("Trying to rename temporary file '" + writeFile.getAbsolutePath() + "' to '" + bundleFile.getAbsolutePath() + "'", Project.MSG_VERBOSE);
                    writeFile.renameTo(bundleFile);
                } catch (IOException ex) {
                    Logger.getLogger(UpdateLongDesc.class.getName()).log(Level.SEVERE, null, ex);
                    throw new BuildException("Unable to write temporary property file '" + writeFile.getAbsolutePath() + "'.", ex, getLocation());
                }
            }
        }
    }
}
