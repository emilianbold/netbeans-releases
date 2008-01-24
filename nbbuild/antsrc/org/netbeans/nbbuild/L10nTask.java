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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.GZip;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.types.FileSet;

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
public class L10nTask extends MatchingTask {
    private Hashtable<String,HashSet<String>> modules = new Hashtable<String,HashSet<String>>();
    private String[] allmodules;
    private String[] topdirs;
    private String localizableFile, generatedFile, changedFile,globalFile;
    private String allFile = "l10n.list.all";
    private String excludePattern = "";
    private String includePattern = "";
    // Sets the set of exclude patterns.
    // Patterns may be separated by a comma or a space.
    // The first instance which necessitated this addition was
    // "**/ja/**"
    private String buildDir;
    private String distDir;
    private Vector error = new Vector();
    private Hashtable<String, String> changed = new Hashtable<String, String>();
    private Hashtable<String, String> generatedFileHash = new Hashtable<String, String>();
    private Hashtable<String, String> fullPropHash = null ;
    private HashSet<String> localizableHash = new HashSet<String>();
    private Project p;
    static boolean DEBUG = false;
    boolean cont=false;
    private String buildNumber;
    String[] localizableFiles;
    
    public void execute() throws BuildException {
        // 1. Check that settable variables have been set.
        // If they have not been, abandon ship!
        
        if (modules == null ) {
            throw new BuildException("Required variable not set.  Set 'modules' in the calling .xml file");
        }
        if (topdirs == null ) {
            throw new BuildException("Required variable not set.  Set 'topdirs' in the calling .xml file");
        }
        if (distDir == null ) {
            throw new BuildException("Required variable not set.  Set 'distDir' in the calling .xml file");
        }
        
        if (buildDir == null ) {
            throw new BuildException("Required variable not set.  Set 'buildDir' in the calling .xml file");
        }
        if (localizableFile == null ) {
            throw new BuildException("Required variable not set.  Set 'localizableFile' in the calling .xml file");
        }
        if (globalFile == null) {
            throw new BuildException("Required variable not set.  Set 'globaFile' in the calling .xml file");
        }
        if (generatedFile == null ) {
            throw new BuildException("Required variable not set.  Set 'generatedFile' in the calling .xml file");
        }
        if (changedFile == null ) {
            throw new BuildException("Required variable not set.  Set 'changedFile' in the calling .xml file");
        }
        if (buildNumber == null ) {
            throw new BuildException("Required variable not set.  Set 'buildNumber' in the calling .xml file");
        }
        
        //if the dir doesn't exist, make it.
        // if the dir still doesn't exist, fail.
        File dd = new File(distDir);
        if ( ! dd.exists()  ) {
            dd.mkdirs();
        }
        
        ArrayList<String> sb = new ArrayList<String>();
        for (int i = 0; i<topdirs.length; i++) {
            sb.add(topdirs[i]);
        }
        log("topdirs: " + sb.toString(),Project.MSG_INFO);
        sb = new ArrayList<String>();
        for (int i = 0; i<allmodules.length; i++) {
            sb.add(allmodules[i]);
        }
        log("modules: " + sb.toString(), Project.MSG_INFO);
        log("distDir: "+distDir,Project.MSG_INFO);
        log("buildDir: "+buildDir,Project.MSG_INFO);
        log("localizableFile: "+localizableFile,Project.MSG_INFO);
        log("generatedFile: "+generatedFile,Project.MSG_INFO);
        log("changedFile: "+changedFile,Project.MSG_INFO);
        log("globalFile: "+globalFile,Project.MSG_INFO);
        log("buildNumber: "+buildNumber,Project.MSG_INFO);
        
        
        p=this.getProject();
        
        for (int i=0; i<topdirs.length; i++) {
            log("STARTING TOPDIR "+topdirs[i], Project.MSG_VERBOSE);
            Iterator<String> keyIt = modules.keySet().iterator();
            while (keyIt.hasNext()) {
                String topModule = keyIt.next();
                Delete df = (Delete) this.getProject().createTask("delete");
                df.setDir(new File(topdirs[i]+File.separator+topModule));
                df.setIncludes(topModule+"."+localizableFile+", "+topModule+"."+generatedFile+", "+topModule+"."+changedFile+", "+topModule+"."+allFile+"*");
                df.execute();
                
                @SuppressWarnings("unchecked")
                Hashtable<String,String> props = p.getProperties();
                fullPropHash = props;
                
                generatedFileHash.clear();
                changed.clear();
                localizableHash.clear();
                //localizableFiles.
                log("IN TOPLEVEL MODULE "+topModule, Project.MSG_VERBOSE);
                @SuppressWarnings("unchecked")
                Iterator<String> modIt = modules.get(topModule).iterator();
                while (modIt.hasNext()) {
                    String subModule = modIt.next();
                    log("STARTING SUBMODULE "+subModule, Project.MSG_VERBOSE);
                    
                    File f = new File(topdirs[i]+File.separator+subModule.replace('/', File.separatorChar)+File.separator+localizableFile);
                    log("Localizable file is: "+f.getAbsolutePath(), Project.MSG_VERBOSE);
                    /*
                    if ( ! (f.exists()) ) {
                        log("FILE does not exists "+topdirs[i]+File.separator+subModule.replace('/', File.separatorChar)+File.separator+localizableFile);
                    }
                     */
                    File topDir = new File(topdirs[i]);
                    File modDir = new File(topdirs[i]+File.separator+subModule.replace('/', File.separatorChar));
                    
                    // moved getGeneratedFiles from here to only getGeneratedFiles
                    // if there are localizableFiles.
                    
                    localizableFiles = getLocalizableFiles(topDir, subModule);
                    if (localizableFiles == null ) {
                        log("No Localizable Files for this module. "+topDir+File.separator+subModule.replace('/', File.separatorChar),Project.MSG_VERBOSE);
                    } else {
                        for (int k=0; k<localizableFiles.length; k++) {
                            localizableHash.add(localizableFiles[k]);
                        }
                        //don't look for the generated Files if there are no localizableFiles
                        Hashtable<String,String> gfh = new Hashtable<String, String>();
                        //generatedFileHash
                        gfh = getGeneratedFiles(topDir, subModule);
                        if ( gfh == null ) {
                            log("GENFILEHASH is null ("+topDir+", "+subModule+")", Project.MSG_DEBUG);
                        } else {
                            generatedFileHash.putAll(gfh);
                        }
                    } // if localizableFiles is not null
                } // while submodules
                    /* XXX obsolete:
                Iterator<String> it = localizableHash.iterator();
                while (it.hasNext()) {
                    String oneLocalizableFile =it.next();
                    
                    // DO SOME PRE-PROCESSING HERE. CACHE DIR REACTION (don't check for existance of same dir over & over again).
                    
                    
                    int lastSlashIndex = oneLocalizableFile.lastIndexOf(File.separator);
                    String parentDirFullPath = oneLocalizableFile.substring(0, lastSlashIndex);
                    
                    // Check that the CVS/ dir exists.
                    // If it does not, assume we are dealing with a generated dir
                    // which does not need to be examined by me.
                    if ( ! new File(parentDirFullPath+"/CVS").exists() ) {
                        log("CVSDIR doesn't exist ["+parentDirFullPath+"/CVS ]", Project.MSG_DEBUG);
                        // skip to the end of this loop
                        //HEY!  there must be a cleaner way!!
                        log("This dir is a generated dir with no CVS dir "+parentDirFullPath, Project.MSG_INFO);
                        
                    } else {
                        
                        ce = cvsEntriesCache.get(parentDirFullPath);
                        
                        if ( ce == null ) {
                            ce = new CvsEntries(parentDirFullPath);  //passing parentDirFullPath
                            cvsEntriesCache.put(parentDirFullPath, ce);
                        }
                        
                        if ( generatedFileHash == null ) {
                            generatedFileHash=new Hashtable<String, String>();
                        }
                        
                        
                        String localizableFileOnly = oneLocalizableFile.substring(oneLocalizableFile.lastIndexOf(File.separator)+1);
                        if (localizableFileOnly == null) { log("NULL LOCALIZABLE FILE"); }
                        
                        String ceRev = ce.getRevnoByFileName(localizableFileOnly);
                        
                        if ( ceRev == null ) {
                            // If this file is NOT in ce (doesn't exist in CVS/Entries)
                            // there is something wrong with the build working directory.
                            // We will collect these file names & report at the end.
                            
                            //Maybe we don't want to add them.  THe build generates lots of files which aren't in CVS.
                            // error.add(localizableFiles[k]);
                            
                        } else {
                            log("CEREV: "+ceRev, Project.MSG_DEBUG);
                            // compare revnos
                            
                            String genRev = generatedFileHash.get(oneLocalizableFile);
                            log("GENREV "+genRev + "\tFN: "+oneLocalizableFile, Project.MSG_DEBUG);
                            
                            if ( genRev == null || ! ceRev.equals(genRev)) {
                                //Update changed fileHash
                                changed.put(oneLocalizableFile, ceRev);
                                
                                //Update generatedFileHash
                                generatedFileHash.put(oneLocalizableFile, ceRev);
                                
                                log("I got this far, but there was nothing CHANGED: "+oneLocalizableFile, Project.MSG_DEBUG);
                            }
                        }
                    }
                    
                } //for localizable files
                     */
                
                // PRINT TO "allfiles" list.
                boolean success = false;
                success = printToAllFile(topdirs[i], topModule, localizableHash.toArray(localizableFiles));
                if (! success) {
                    log("ERROR: Print to All File in "+topdirs[i]+", "+topModule+" failed.", Project.MSG_ERR);
                }
                
                //make tar here
                // FILTER ALL FILE, KEEP ONLY UNIQUE LINES
                success = filterAllFile(topdirs[i], topModule);
                if (! success) {
                    log("ERROR: Cannot filter ${module}.l10n.list.all file for "+topdirs[i]+", "+topModule, Project.MSG_ERR);
                }
                
                // IN HERE PRINT TO GENERATED FILE.
                success = printToFile(topdirs[i], topModule);
                
                if (! success) {
                    log("ERROR: Print to File in "+topdirs[i]+", "+topModule+" failed.", Project.MSG_ERR);
                }
                
                // toDir should be build/topDir
                // baseDir should be fullpathtoTopDir
                log("BUILDDIR "+buildDir, Project.MSG_VERBOSE);
                File tDir = new File(buildDir+File.separator+topdirs[i]);
                int lio = topdirs[i].lastIndexOf(File.separator);
                String shortTopdir = topdirs[i].substring(lio+1);
                
                // make a tar no matter what.
                // At a minimum it will have
                // file module.l10n.list.all.
                // THEN delete files.
                
                mkTars(topdirs[i]+File.separator, buildDir+File.separator+shortTopdir+File.separator+topModule+".tar", topdirs[i]+File.separator+topModule+File.separator+topModule+"."+allFile, topModule);
                Delete delete = (Delete)p.createTask("delete");
                FileSet fs = new FileSet();
                fs.setDir(new File(topdirs[i]+File.separator+topModule));
                String includes = topModule+"."+generatedFile+","+ topModule+"."+changedFile+","+ topModule+"."+allFile;
                fs.setIncludes(includes);
                delete.addFileset(fs);
                
                delete.setVerbose(true);
                delete.execute();
                
                // Clean up before moving on.
                
                log("CLEARING "+topdirs[i] + " " + topModule, Project.MSG_VERBOSE);
                generatedFileHash.clear();
                changed.clear();
                error.clear();
                
            } // while modules
        } //for topdirs
        
        // Tar everything
        log("ABOUT TO MAKE THE BIG TAR: "+distDir+"/l10n-"+buildNumber+".tar.gz", Project.MSG_INFO);
        
        // Check to make sure that the build dir exists,
        // & that there are little tars to tar... or we get a "basedir dne"
        // exception.
        
        File bd = new File(buildDir);
        if (bd.exists()) {
            File tarFile = new File(buildDir+"/l10n-"+buildNumber+".tar");
            
            Tar tar = (Tar)p.createTask("tar");
            tar.setBasedir(bd);
            tar.setDestFile(tarFile);
            Tar.TarLongFileMode mode = new Tar.TarLongFileMode();
            mode.setValue(Tar.TarLongFileMode.GNU);
            tar.setLongfile(mode);
            ////// automatically includes all in the basedir
            tar.execute();
            
            if ( tarFile.exists() ) {
                GZip gzip = (GZip)p.createTask("gzip");
                gzip.setSrc(new File(buildDir+"/l10n-"+buildNumber+".tar"));
                gzip.setZipfile(new File(distDir+"/l10n-"+buildNumber+".tar.gz"));
                gzip.execute();
            } else {
                log("NO tar file, can't gzip"+buildDir+"/l10n-"+buildNumber+".tar", Project.MSG_WARN);
            }
        } else {
            log("No files in builddir.  No kit to build", Project.MSG_WARN);
        }
        
        
        
        
    } // execute()
    
    public void mkTars(String srcDir, String fullTarfilePath, String fullIncludesFilePath, String module) {
        //String td = srcDir.substring(srcDir.lastIndexOf(File.separator)+1);
        log("SRCDIR = "+srcDir, Project.MSG_VERBOSE);
        log("FULL TARFILE PATH = "+fullTarfilePath, Project.MSG_VERBOSE);
        
        File incBaseDir = new File(srcDir);
        Tar tar = (Tar)p.createTask("tar");
        tar.setBasedir(new File(srcDir));
        File tarFile = new File(fullTarfilePath);
        tar.setDestFile(tarFile);
        File destDir = tarFile.getParentFile();
        if ((! destDir.exists()) && (! destDir.isDirectory())) {
            destDir.mkdirs();
        }
        
        File incFile = new File(fullIncludesFilePath);
        
        if (incFile.exists() ) {
            tar.setIncludes(module+File.separator+module+"."+allFile);
            tar.setIncludes(module+File.separator+module+"."+generatedFile);
            tar.setIncludes(module+File.separator+module+"."+changedFile);
            tar.setIncludesfile(incFile);
            Tar.TarLongFileMode mode = new Tar.TarLongFileMode();
            mode.setValue(Tar.TarLongFileMode.GNU);
            tar.setLongfile(mode);
            tar.execute();
        } else {
            log("Cannot make tarfile "+fullTarfilePath+", because include list file "+fullIncludesFilePath+" does not exist", Project.MSG_WARN);
        }
        
    }
    
    /** Filter content of ALL FILE to contain only unique lines
     * @param fullTopDir
     * @param module
     * @return true if file I/O operations were successfull, false otherwise
     */
    private boolean filterAllFile(String fullTopDir, String module) {
        String topModule = null;
        int firstSlashIndex = module.indexOf('/');
        if (firstSlashIndex < 0) {
            topModule = module;
        } else {
            topModule = module.substring(0, firstSlashIndex);
        }
        String allFilename = fullTopDir+File.separator+topModule+File.separator+topModule+"."+allFile;
        try {
            BufferedReader br = new BufferedReader(new FileReader(allFilename+".tmp"));
            String line = null;
            HashSet<String> hs = new HashSet<String>();
            while ((line=br.readLine()) != null) {
                hs.add(line);
            }
            br.close();
            LinkedList<String> ll = new LinkedList<String>();
            ll.addAll(hs);
            java.util.Collections.sort(ll);
            FileWriter allWrite = new FileWriter(allFilename, false);
            Iterator<String> it = ll.listIterator();
            while (it.hasNext()) {
                allWrite.write(it.next()+"\n");
            }
            allWrite.close();
            Delete df = (Delete) this.getProject().createTask("delete");
            df.setDir(new File(fullTopDir+File.separator+topModule));
            df.setIncludes(topModule+"."+allFile+".tmp");
            df.execute();
        } catch (IOException ioe) {
            // consider throwing BuildException instead
            log("IN filterAllFile() - TOPDIR: "+fullTopDir+" MODULE: "+module+" IOException "+ioe, Project.MSG_ERR);
            return false;
            
        }
        return true;
    }
    
    /**
     * Prints list of all localizable files found in respective module
     * @param fullTopDir - filesystem path to root of you working repository
     * @param module - directory path name for which this call is being done
     * @param localizables - list of localizable files
     * @return true if file i/o operations were successful
     */
    public boolean printToAllFile(String fullTopDir, String module,String[] localizables) {
        try {
            int lastSlashIndex = fullTopDir.lastIndexOf('/');
            String topDir= fullTopDir.substring(lastSlashIndex+1);
            String topModule = null;
            int firstSlashIndex = module.indexOf('/');
            if (firstSlashIndex < 0) {
                topModule = module;
            } else {
                topModule = module.substring(0, firstSlashIndex);
            }
            File f = new File(buildDir+ File.separator +topDir );
            f.mkdirs();
            String allFilename = fullTopDir+File.separator+topModule+File.separator+topModule+"."+allFile+".tmp";
            log("Writing to All File: "+allFilename,Project.MSG_DEBUG);
            
            if ( localizables != null ) {
                FileWriter allWrite = new FileWriter(allFilename, true);
                for (int i=0; i<localizables.length; i++) {
                    if (localizables[i] == null) {
                        // found null, thus the rest of array is also null
                        // assumption based on HashSet.toArray(String[] s) javadoc
                        break;
                    }
                    int lio = localizables[i].lastIndexOf(fullTopDir);
                    if (lio >= 0) {
                        String moduleFileName= localizables[i].substring(lio+fullTopDir.length()+1).replace(File.separatorChar, '/');
                        allWrite.write(moduleFileName+"\n");
                    } else {
                        log("Error: NO TOPDIR HERE: "+ localizables[i]+ " FTD: "+fullTopDir+" LIO: "+lio, Project.MSG_ERR);
                    }
                    
                }
                allWrite.close();
            }
            
        } catch (IOException ioe) {
            // consider throwing BuildException instead
            log("IN printToAllFile() - TOPDIR: "+fullTopDir+" MODULE: "+module+" IOException "+ioe, Project.MSG_ERR);
            return false;
        }
        return true;
    }
    
    public boolean printToFile(String fullTopDir, String module) {
        String topModule = null;
        int firstSlashIndex = module.indexOf('/');
        if (firstSlashIndex < 0) {
            topModule = module;
        } else {
            topModule = module.substring(0, firstSlashIndex);
        }
        
        log("IN printToFile FULLTOPDIR "+fullTopDir+ ", MODULE " + module + ", TOPMODULE " + topModule, Project.MSG_DEBUG);
        
        try {
            
            int lastSlashIndex = fullTopDir.lastIndexOf('/');
            String topDir= fullTopDir.substring(lastSlashIndex+1);
            
                /*  No need to mkdirs now - printToAllFile is making them.
                        File f = new File(buildDir+ File.separator +topDir );
                        f.mkdirs();
                 */
            
            String genFilename = fullTopDir+File.separator+topModule+File.separator+topModule+"."+generatedFile;
            FileWriter genWrite = new FileWriter(genFilename,true);
            log("Writing Generated File: "+genFilename, Project.MSG_VERBOSE);
            String changedFilename = fullTopDir+File.separator+topModule+File.separator+topModule+"."+changedFile;
            FileWriter changedWrite = new FileWriter(changedFilename,true);
            log("Writing Changed File: "+changedFilename, Project.MSG_VERBOSE);
            
            
            if ( generatedFileHash == null ) {
                generatedFileHash = new Hashtable<String, String>();
            } else {
                // sort the generatedFiles list
                LinkedList<String> ll = new LinkedList<String>();
                ll.addAll(generatedFileHash.keySet());
                java.util.Collections.sort(ll);
                Iterator<String> it = ll.listIterator();
                while (it.hasNext()) {
                    String genFileKey = it.next();
                    int lioTopDir =  genFileKey.lastIndexOf(topDir);
                    String moduleFileName = genFileKey.substring(lioTopDir+topDir.length()+1).replace(File.separatorChar, '/') ;
                    
                    genWrite.write(moduleFileName+"\t"+generatedFileHash.get(genFileKey)+"\n");
                }
                
                // sort the changedFiles list
                ll = new LinkedList<String>();
                ll.addAll(changed.keySet());
                java.util.Collections.sort(ll);
                it = null;
                it = ll.listIterator();
                while (it.hasNext()) {
                    String changedFileKey = it.next();
                    
                    int lio = changedFileKey.lastIndexOf(topDir+File.separator+topModule);
                    String moduleFileName;
                    if (lio >= 0) {
                        
                        moduleFileName=changedFileKey.substring(changedFileKey.lastIndexOf(topDir+File.separator+topModule)+topDir.length()+1).replace(File.separatorChar, '/');
                        changedWrite.write(moduleFileName+"\n");
                    } else {
                        // If we get here, the cache is probably not really being cleared.
                        
                        //The cache is not being cleared
                        // OR we have wild-card characters
                        // consider throwing BuildException instead
                        log("WARNING: L10n.list file error. Each item in your list should" +
                                " reference the current module.  If this is a global l10n file" +
                                " used over several modules use the property ${l10n-module}" +
                                " as a place-holder.  This error occurred in " + module +
                                ".", Project.MSG_WARN);
                        log("Contact program administrator.\r\t"+ changedFileKey+ "\r\tTD "+
                                topDir+File.separator+module +" LIO "+lio, Project.MSG_VERBOSE);
                    }
                    
                    //changedWrite.write(changedFileKey+"\n");
                    //Changed should have a list of fullpath in order to do copy
                }
            }
            
            if (! error.isEmpty() ) {
                String errorFileName = buildDir+File.separator+"l10n-errors.txt";
                FileWriter errorWrite = new FileWriter(errorFileName, true);
                log("Writing following errors to a log file "+errorFileName, Project.MSG_ERR);
                for (Enumeration e = error.elements(); e.hasMoreElements();) {
                    String ee = (String)e.nextElement();
                    errorWrite.write(ee+"\n");
                    log("Error: "+ee, Project.MSG_ERR);
                }
                log("Finished writing errors to a log file "+errorFileName, Project.MSG_ERR);
                errorWrite.close();
            }
            
            genWrite.close();
            changedWrite.close();
            
        } catch (IOException ioe) {
            log("IOException printToFile()"+ioe, Project.MSG_ERR);
            return false;
        }
        return true;
    }
    
    /** You can use this function independently of "execute()", but
     * you have to set these attributes: localizableFile,
     * globalFile (if "read global" is used in any l10n.list
     * files), excludePattern (if desired), includePattern (if
     * desired)
     * @param topRoot
     * @param module
     * @return String[]
     */
    public String[] getLocalizableFiles(File topRoot, String module) {
        String[] lfs = null;
        StringBuffer[] sbholder=new StringBuffer[2];
        StringBuffer sbi= new StringBuffer();
        StringBuffer sbe = new StringBuffer();
        String includeS="";
        String excludeS="";
        log("IN getLocalizableFiles(File "+topRoot.getName()+", " + module.replace('/', File.separatorChar) +")", Project.MSG_DEBUG);
        
        if( fullPropHash == null) {
            p = getProject() ;
            @SuppressWarnings("unchecked")
            Hashtable<String,String> props = p.getProperties();
            fullPropHash = props;
        }
        fullPropHash.put("l10n-module", module);
        
        /** Look for local L10N list (localizableFile), if not found, look
         * for global include file, first in path relative to project basedir
         * and then without specifying a path
         */
        boolean incres = false; // includes file resolved
        File includes = new File(topRoot.getAbsolutePath()+ File.separator + module.replace('/', File.separatorChar) + File.separator + localizableFile);
        if ( ! includes.exists() || includes.length() <= 0 ) {
            log("FILE IS too short to mess with "+ module, Project.MSG_DEBUG);
            if (globalFile == null) {
                return lfs;
            }
            includes = new File(this.getProject().getBaseDir(), globalFile.replace('/', File.separatorChar));
            if ( ! includes.exists() || includes.length() <= 0 ) {
                log("Global file " + includes.getAbsolutePath() + "does not exist", Project.MSG_DEBUG);
                includes = new File(globalFile.replace('/', File.separatorChar));
                if ( ! includes.exists() || includes.length() <= 0 ) {
                    log("Global file " + includes.getAbsolutePath() + "does not exist", Project.MSG_DEBUG);
                } else {
                    incres = true;
                }
            } else {
                incres = true;
            }
        } else {
            incres = true;
        }
        if ( ! incres ) {
            return lfs;
        }
        
        try {
            sbholder = processListFile(topRoot, includes, module);
        } catch (java.io.IOException ioe) {
            log("Error processing file. "+ioe, Project.MSG_WARN);
        }
        
        if (sbholder != null) {
            sbi = sbholder[0];
            sbe = sbholder[1];
            
            sbe.append(" "+excludePattern);
            sbi.append(" "+includePattern);
            
            log("INC "+sbi.toString(), Project.MSG_DEBUG);
            log("EXC "+sbe.toString(), Project.MSG_DEBUG);
        }
        
        this.fileset = new FileSet();
        
        if ( sbi !=null) {
            this.setIncludes(sbi.toString());
        }
        
        if (sbe != null ) {
            this.setExcludes(sbe.toString());
        }
        
        DirectoryScanner ds = this.getDirectoryScanner(topRoot);
        
        ds.scan();
        lfs = ds.getIncludedFiles();
        
        for (int k=0; k<lfs.length; k++) {
            log("LFS "+topRoot+File.separator+lfs[k], Project.MSG_DEBUG);
            lfs[k] = topRoot+File.separator+lfs[k].trim();
        }
        
        log("THERE ARE "+lfs.length + " FILES in INCL FILES", Project.MSG_DEBUG);
        return lfs;
    }
    
    public Hashtable<String, String> getGeneratedFiles(File topDir, String mod) {
        // NOTE: This method will return 'null' 100% of the time if there
        // are no l10n.list.generated files.
        // At this writing, this functionality is not used.
        // EG 1/03
        
        Hashtable<String, String> h=new Hashtable<String, String>();
        // Read generated File
        try {
            
            String topDirFullPath = topDir.getAbsolutePath();
            log("topDirFullPath: "+topDirFullPath, Project.MSG_DEBUG);
            
            BufferedReader inBuff = new BufferedReader(new FileReader(new File(topDir+File.separator+mod.replace('/', File.separatorChar),generatedFile)));
            boolean eof = false;
            while (! eof) {
                String line = inBuff.readLine();
                if (line==null) {
                    eof = true;
                } else {
                    log("LL " + line, Project.MSG_DEBUG);
                    
                    int tabIndex = line.indexOf("\t");
                    if (tabIndex > 0) {
                        String filename = line.substring(0,tabIndex);
                        String revision = line.substring(tabIndex+1);
                        h.put(topDirFullPath+File.separator+filename, revision);
                    } else {
                        log("There's no tab in this line"+"["+line+"]", Project.MSG_INFO);
                    }
                    
                }
                
            } //while
        } catch(java.io.FileNotFoundException e) {
            // Warning: Generated File: "+generatedFile+" in "+
            // topDir+File.separator+mod+" not found.
            // Adding all files to changed list."
            return(null);
        } catch(java.io.IOException e) {
            log("IN getGeneratedFiles('"+topDir+"','"+mod+"'); - IOException "+ e.getMessage(), Project.MSG_WARN);
        }
        
        return h;
    }
    
    // Accessor Methods
    
    public void setTopdirs(String s) {
        StringTokenizer st = new StringTokenizer(s);
        String[] tops = new String[st.countTokens()];
        int i=0;
        while (st.hasMoreTokens()) {
            tops[i++]=st.nextToken();
        }
        
        if (false) {
            for (int j=0; j<tops.length; j++) {
                log("TOPS "+tops[j], Project.MSG_INFO);
            }
        }
        
        this.topdirs = tops;
        
    }
    public void setModules(String s) {
        StringTokenizer st = new StringTokenizer(s,",");
        HashSet<String> modSet = null;
        
        modules = new Hashtable<String, HashSet<String>>();
        int modCnt = 0;
        while (st.hasMoreTokens()) {
            String fullMod=st.nextToken().trim();
            String topMod = null;
            log("ITEM IN MODLIST: "+fullMod, Project.MSG_DEBUG);
            int index = fullMod.indexOf('/');
            if (index >= 0) {
                topMod = fullMod.substring(0,index);
            } else {
                topMod = fullMod;
            }
            log("Top module is "+topMod, Project.MSG_DEBUG);
            if (modules.containsKey(topMod)) {
                modSet = modules.get(topMod);
                modSet.add( fullMod );
            } else {
                modSet = new HashSet<String>();
                modSet.add( fullMod );
                modules.put(topMod, modSet);
            }
            modCnt++;
        }
        allmodules = new String[ modCnt ];
        int i = 0;
        Iterator<String> keyit = modules.keySet().iterator();
        while (keyit.hasNext()) {
            String key = keyit.next();
            HashSet sm = modules.get(key);
            @SuppressWarnings("unchecked")
            Iterator<String> valit = sm.iterator();
            while (valit.hasNext()) {
                allmodules[i] = valit.next();
                i++;
            }
        }
    }
    
    private String resolveProperties(String line) {
        boolean skipit = false;
        while (line.indexOf("${") >= 0 && skipit == false) {
            
            String propertyName;
            String value="";
            String res="";
            String pre="";
            
            propertyName=line.substring(line.indexOf("${")+2,line.indexOf("}"));
            
            if (fullPropHash.containsKey(propertyName)) {
                
                value=fullPropHash.get(propertyName);
                res = line.substring(line.indexOf("}")+1);
                pre= line.substring(0,line.indexOf("{")-1);
                line=pre+value+res;
                log("LINE is now "+line, Project.MSG_VERBOSE);
            } else {
                log("Uninterpretable property in line: '"+line+"', property '"+propertyName+"'. Interpreting the entire line literally.", Project.MSG_WARN);
                skipit=true;
            }
        }
        return line;
    }
    
    public void executeLocalTarget(File topRoot, String module, String target) throws BuildException {
        log("Going to execute custom target '"+target+"' in directory '"+topRoot.getAbsolutePath()+File.separator+module.replace('/', File.separatorChar)+"'", Project.MSG_INFO);
        
        Ant ant = (Ant) this.getProject().createTask("ant");
        ant.setDir(new File(topRoot, module.replace('/', File.separatorChar)));
        ant.setTarget(target);
        ant.setTaskName("custom-call-"+module+"-"+target);
        ant.execute();
        log("Finished execution of custom target '"+target+"' in directory '"+topRoot.getAbsolutePath()+File.separator+module.replace('/', File.separatorChar)+"'", Project.MSG_INFO);
    }

    public StringBuffer[] processListFile(File topRoot, File inc,String module) throws IOException,BuildException {
        log("Reading "+ module+"'s list file: "+inc.toString(), Project.MSG_INFO);
        StringBuffer[] sbholder=new StringBuffer[2];
        StringBuffer sbi = new StringBuffer();
        StringBuffer sbe = new StringBuffer();
        
        
        if(inc.exists() && inc.length() >0) {
            //Read Includes File.
            BufferedReader br = new BufferedReader(new FileReader(inc));
            String line;
            while((line=br.readLine()) != null) {
                boolean skipit=false;
                // For each line check if there are any properties which
                // should be interpreted first.
                line = resolveProperties(line);
                if (line.trim().indexOf("#") == 0) {
                    log("Skipping commented-out line: '" + line + "'", Project.MSG_DEBUG);
                } else if (line.indexOf("exclude") >= 0) {
                    sbe.append(" "+line.trim().substring("exclude".length()+1));
                    log("Added exclude: '" + line.trim().substring("exclude".length()+1) + "'", Project.MSG_DEBUG);
                } else if (line.indexOf("antcall") >= 0) {
                    String target = line.trim().substring("antcall".length()+1).trim();
                    executeLocalTarget(topRoot, module, target);
                } else if (line.indexOf("read global") >= 0) {
                    if (globalFile != null && ! globalFile.equals("") && !(inc.getAbsolutePath().equals((new File (globalFile)).getAbsolutePath()))) {
                        log("Loading and interpreting global includes/excludes file "+globalFile+" for module "+module, Project.MSG_INFO);
                        StringBuffer[] globalarray = processListFile(topRoot, new File(globalFile),module);
                        if (globalarray[0] != null) {
                            sbi.append(" "+globalarray[0]);
                        }
                        if (globalarray[1] != null) {
                            sbe.append(" "+globalarray[1]);
                        }
                    }
                } else if( line.trim().startsWith( "read")) {
                    String l = line.trim() ;
                    l = l.substring( 4) ;
                    l = l.trim() ;
                    StringBuffer[] sbarr = processListFile( topRoot, new File( l),  module) ;
                    if ( sbarr[0] != null ) { sbi.append( " " + sbarr[ 0]) ;}
                    if ( sbarr[1] != null ) { sbe.append( " " + sbarr[ 1]) ;}
                } else {
                    sbi.append(" "+line);
                }
                if (DEBUG) {
                    log("GLOBAL "+line.indexOf("read global")+" EXCLUDES "+line.indexOf("exclude")+ " FILE "+inc.toString(), Project.MSG_DEBUG);
                }
            }
            br.close();
        }
        sbholder[0]=sbi;
        sbholder[1]=sbe;
        return sbholder;
    }
    
    public void setDistDir(String s) {
        log("DIST DIR SHOULD BE: "+s, Project.MSG_DEBUG);
        this.distDir=s;
    }
    public void setBuildDir(String s) {
        this.buildDir=s;
    }
    public void setDebug(boolean s) {
        this.DEBUG=s;
    }
    public void setLocalizableFile(String s) {
        this.localizableFile=s;
    }
    public void setExcludePattern(String s) {
        this.excludePattern=s;
    }
    public void setIncludePattern(String s) {
        this.includePattern=s;
    }
    
    public void setBuildNumber(String s) {
        this.buildNumber=s;
    }
    public void setGeneratedFile(String s) {
        this.generatedFile=s;
    }
    public void setChangedFile(String s) {
        this.changedFile=s;
    }
    public void setGlobalFile(String s) {
        this.globalFile=s;
    }
}
