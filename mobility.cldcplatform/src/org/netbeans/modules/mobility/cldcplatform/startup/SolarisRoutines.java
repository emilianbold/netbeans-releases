/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.cldcplatform.startup;
//package com.iplanet.ias.installer.utilities;

/** JDK Imports **/
import org.openide.ErrorManager;

import java.io.*;
import java.util.*;


/**	Solaris package and OS patch related utility functions
 * are defined here.
 **/


public class SolarisRoutines {
    
    /**	Find out if the package is available on the box,takes the package name as
     * argument. returns PkgInfo class.
     **/
    public static PkgInfo findPackage(final String packageName) {
        int result = -1;
        final StringBuffer cmdOutput = new StringBuffer();
        // Execute Solaris 'pkginfo' command and store the output in buffer for parsing
        try {
            result = executeRuntimeCommand( "/bin/pkginfo -i -l " + packageName,cmdOutput); // NOI18N
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
        
        if ( result == 0) {
            // Pkginfo did not fail, so return it.
            return new SolarisRoutines.PkgInfo(cmdOutput);
        }
        return null;
    }
    
    
    /** Creates a script running showrev (workaround: otherwise Runtime.exec chokes on long outputs)
     * returns the file*/
    public static File createPatchScript()
    throws IOException{
        final File showrev = new File("/bin/showrev"); // NOI18N
        if (!showrev.exists()) {
            throw new FileNotFoundException("/bin/showrev is not found to check the installed patches"); // NOI18N
        }
        
        final File script = File.createTempFile("appserver-showrev", ".sh", new File("/tmp")); // NOI18N
        final Process proc = Runtime.getRuntime().exec("chmod +x " + script); // NOI18N
        try {
            proc.waitFor();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        final FileWriter out = new FileWriter(script);
        out.write("/bin/showrev -p | grep $1\n"); // NOI18N
        out.close();
        return script;
    }
    
    /** checks whether all the patches in patchList are installed
     *  returns an array of uninstalled patches or null if all the patches are installed
     */
    public static String[] getUninstalledPatches(final String[] patchList)
    throws IOException{
        
        final File script = createPatchScript();
        final String filename = script.getAbsolutePath();
        final ArrayList<String> uninstalledPatches = new ArrayList<String>(Arrays.asList(patchList));
        final Iterator<String> iter = uninstalledPatches.iterator();
        while (iter.hasNext()) {
            final String patch = iter.next();
            if (checkPatch(patch, filename) == PATCH_INSTALLED) {
                iter.remove();
            }
        }
        script.delete();
        return (uninstalledPatches.size() > 0) ? (String[]) uninstalledPatches.toArray(new String[uninstalledPatches.size()]) : null;
    }
    
    /**	Check if the given patch is instaled on the box or not. Takes patch number as argument, returns
     * an integer indicating whether the patch is installed/missing or unknown.
     * scriptAbsolutePath: location of the script running showrev
     * (workaround: otherwise Runtime.exec chokes on long outputs)
     **/
    public static final int PATCH_INSTALLED = 0;
    public static final int PATCH_MISSING = 1; //needs new update for the patch
    public static final int PATCH_UNKNOWN = 2; //no info about patch
    
    public static int checkPatch(final String patchId, final String scriptAbsolutePath)
    {
        int result = -1;
        final StringBuffer cmdOutput = new StringBuffer();
        final String patchMajorVersion = patchId.substring(0,patchId.indexOf('-'));
        // Execute Solaris 'pkginfo' command and store the output in buffer for parsing
        try {
            result = executeRuntimeCommand(scriptAbsolutePath+ " " + patchMajorVersion, cmdOutput); // NOI18N
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return PATCH_UNKNOWN;
        }
        //System.out.println("In checkPatch-> " + patchId + " " + result);
        if ( result == 0) {
            // showrev did not fail, so now parse the output buffer.
            if (checkPatchInstalled(patchId,cmdOutput)) {
                return PATCH_INSTALLED;
            }
            return PATCH_MISSING;
        } 
        return PATCH_UNKNOWN;
    }
    
    /**	Parses the output buffer of showrev -p cmd and scans for the given patch id in the
     * buffer. Returns true/false based on the scan results.
     **/
    private static boolean checkPatchInstalled(final String patchId, final StringBuffer buffer) {
        // First store all the lines with patch numbers in the vector
        final String output = buffer.toString();
        String infoString;
        final String patchMajorVersion = patchId.substring(0,patchId.indexOf('-'));
        final String patchMinorVersion = patchId.substring(patchId.indexOf('-')+1);
        int lineNum=0;
        int patchCount=0;
        int currentLine=0;
        int startIndex=0;
        // First go through the output and count the number of lines with the string "Patch:"
        lineNum = output.indexOf("Patch:", 0); // NOI18N
        while (lineNum != -1) {
            lineNum++;
            patchCount++;
            lineNum = output.indexOf("Patch:", lineNum); // NOI18N
        }
        lineNum = 0;
        while ( currentLine < patchCount ) {
            lineNum = output.indexOf("Patch:", lineNum+1); // NOI18N
            
            if ( lineNum == -1) {
                // We have reached the last occurence of patch string in the buffer..
                infoString =output.substring(startIndex);
            } else {
                infoString = output.substring(startIndex, lineNum);
            }
            /** Isolate the patch number alone from the output, In the string it will be like this
             * "Patch:  108940-07 <sometext>".
             **/
            final int beforePatchNum = infoString.indexOf(' ');
            final int afterPatchNum = infoString.indexOf(' ', beforePatchNum+1);
            final String patch = infoString.substring(beforePatchNum,afterPatchNum).trim();
            final String majorVersion = patch.substring(0,patch.indexOf('-'));
            final String minorVersion = patch.substring(patch.indexOf('-') + 1);
            
            if (majorVersion.equals(patchMajorVersion)) {
                if (minorVersion.compareTo(patchMinorVersion) >= 0) {
                    return true;
                }
            }
            
            startIndex = lineNum;
            currentLine++;
        }
        
        return false;
    }
    
    /** finds the pid of the process and then kills it with all its shildren processes */
	public static void killProcess(final String processCmd)
    {
        //strip the args
        String command = processCmd.trim();
        int index;
        if ((index = command.indexOf(' ')) != -1) // NOI18N
            command = command.substring(0, index);
        
        final String pgrepCmd = "/bin/pgrep -f " + command; // NOI18N
        final String ptreeCmd = "/bin/ptree "; // NOI18N
        final String killCmd = "/bin/kill -9 "; // NOI18N
        
        try {
            int result = -1;
            StringBuffer cmdOutput = new StringBuffer();
            result = executeRuntimeCommand( pgrepCmd, cmdOutput);
            //System.out.println("Pgrep -> \n" + cmdOutput.toString());
            if (( result == 0) && (cmdOutput.length() != 0)) {
                final StringTokenizer stArr = new StringTokenizer(cmdOutput.toString());
                while (stArr.hasMoreTokens()) {
                    cmdOutput = new StringBuffer();
                    final String token = stArr.nextToken().trim();
                    try {
                        Integer.valueOf(token);
                    } catch (NumberFormatException ex) {
                        //token is not an integer
                        continue;
                    }
                    //give some time so that most of the children are spawned
                    Thread.sleep(3000);
                    result = executeRuntimeCommand( ptreeCmd + token, cmdOutput);
                    if (( result == 0) && (cmdOutput.length() != 0)) {
                        final String pidList = parsePtreeInformation(cmdOutput, command);
                        result = executeRuntimeCommand( killCmd + pidList, cmdOutput);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    /**Parse the ptree cmd output and get the pid's of the children processes
     **/
    private static String parsePtreeInformation(final StringBuffer buffer, final String command) {
        final StringTokenizer stArr = new StringTokenizer(buffer.toString(), "\n"); // NOI18N
        boolean isCmdFound = false;
        String pidList = ""; // NOI18N
        while (stArr.hasMoreTokens()) {
            final String token = stArr.nextToken().trim();
            int index;
            
            if (!isCmdFound) {
                if ((index = token.indexOf(command)) != -1) {
                    isCmdFound = true;
                } else
                    continue;
            }
            
            if ((index = token.indexOf(' ')) != -1) // NOI18N
                pidList = pidList + " " + token.substring(0,index); // NOI18N
        }
        return pidList;
    }
    
    public static int executeRuntimeCommand(final String command, final StringBuffer commandOutput)
    throws Exception{
        final Process proc = Runtime.getRuntime().exec(command);
        try {
            proc.waitFor();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        final InputStream pIn = proc.getInputStream();
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(pIn));
        if (inputReader.ready()) {
            int eof = 0;
            while (eof != -1) {
                final char[] buffer = new char[250];
                eof = inputReader.read(buffer,0,250);
                commandOutput.append(buffer);
            }
        }
        inputReader.close();
        return proc.exitValue();
    }
    
    static class PkgInfo {
        private String version = ""; //NOI18N
        private String location = ""; //NOI18N
        private double versionNumber = 0;
        private Date revision;
        private String pkgInst = ""; //NOI18N
        
        private PkgInfo() {
            // to avoid instantiation
        }
        
        public PkgInfo(StringBuffer info) {
            parsePackageInformation(info);
        }
        
        //getters
        public String getPkgInst() { return pkgInst; }
        public String getVersion() { return version; }
        public String getLocation() { return location; }
        public double getVersionNumber() { return versionNumber;}
        public Date getRevision() { return revision; }
        
        /** Compares two PkgInfo versions.
         * Returns 0 if they are equal
         *         -1 if this PkgInfo is older
         *         1 if this pkgInfo is newer
         */
        public int compareTo(final PkgInfo pkg) {
            if (versionNumber > pkg.getVersionNumber()) return 1;
            else if (versionNumber < pkg.getVersionNumber()) return -1;
            //else check revision
            if (( revision != null) && (pkg.getRevision() != null)){
                return revision.compareTo(pkg.getRevision());
            }
            return 0;
        }
        
        /** Parse the pkginfo cmd output
         *  BASEDIR:. This will be location where the package is installed.
         *  VERSION:. This will be version of the package {VERSIONNUMBER + REVISON}
         **/
        private void parsePackageInformation(final StringBuffer buffer) {
            final StringTokenizer stArr = new StringTokenizer(buffer.toString());
            while (stArr.hasMoreTokens()) {
                final String token = stArr.nextToken();
                if ("BASEDIR:".equals(token)) { // NOI18N
                    location = stArr.nextToken();
                } else if ("PKGINST:".equals(token)) { // NOI18N
                    pkgInst = stArr.nextToken();
                } else if ("VERSION:".equals(token)) { // NOI18N
                    version = stArr.nextToken();
                    //TO DO: only supported in JDK1.4 -> split();
                    //for now, use the two lines below:
                    final String key = ",REV="; // NOI18N
                    final int index = version.indexOf(key);
                    if (index != -1) {
                        try {
                            versionNumber = Double.parseDouble(version.substring(0, index));
                            final String rev = version.substring(index + key.length());
                            final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy.MM.dd.hh.mm"); // NOI18N
                            revision = formatter.parse(rev);
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            
                        }
                    }
                }
            }
        }
    }
    
    /**testing purposes*/
    public static void main(final String[] args) {
        try {
            final File script = createPatchScript();
            final String filename = script.getAbsolutePath();
            System.out.println("Checking patch 108827-35 -> " + checkPatch("108827-35", filename)); // NOI18N
            System.out.println("Checking patch 109326-09 -> " + checkPatch("109326-09", filename)); // NOI18N
            System.out.println("Checking patch 110380-04 -> " + checkPatch("110380-04", filename)); // NOI18N
            System.out.println("Checking patch 110934-10 -> " + checkPatch("110934-10", filename)); // NOI18N
            System.out.println("Checking patch 109320-01 -> " + checkPatch("109320-01", filename)); // NOI18N
            System.out.println("Checking patch 108435-05 -> " + checkPatch("108435-05", filename)); // NOI18N
            System.out.println("Class -> " + SolarisRoutines.class.getName()); // NOI18N
            System.out.println("os -> " + System.getProperty("os.arch") + " " // NOI18N
                    + System.getProperty("os.name") + " " // NOI18N
                    + System.getProperty("os.version")); // NOI18N
            script.delete();
            final String[] patches = new String[] {"108827-35", "109326-09", "110380-04", "110934-10", "109320-01", "108435-05"}; // NOI18N
            final String[] results = getUninstalledPatches(patches);
            final String resultStr = (results == null) ? "all installed" : Arrays.asList(results).toString(); // NOI18N
            System.out.println("All installed -> " + Arrays.asList(patches) + "\n" + resultStr); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
}


