/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.GZip;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.types.FileSet;
import org.netbeans.nbbuild.utils.cvsutils.CvsEntries;

/**
 * This task was created to create L10N kits.
 * The xml to call this task might look like:
 * <l10nTask topdirs="${f4jroot} ${nbroot}"  modules="${all_modules}" localizableFile="${localizableF}" generatedFile="${genFile}" changedFile="${changedFile}" buildDir="${l10n-build}" distDir="${l10n-dist}" buildNumber="${buildnumber}"/>
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
    private String[] modules;
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
    private Hashtable<String, CvsEntries> cvsEntriesCache=new Hashtable<String, CvsEntries>();
    private Hashtable<String, String> changed = new Hashtable<String, String>();
    private Hashtable<String, String> generatedFileHash = new Hashtable<String, String>();
    private Hashtable<String, String> fullPropHash = null ;

    private Project p;
    private boolean readGlobalFile=false; //have we already read globalFile?
    private StringBuffer[] globalsbholder= new StringBuffer[2];
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

        System.out.print("topdirs:\t");
        for (int j=0; j<topdirs.length; j++) {
            System.out.print(topdirs[j]+"\t");
        }

        System.out.print("\nmodules:\t");
        for (int i=0; i<modules.length; i++) {
            System.out.print(modules[i]+"\t");
        }

        System.out.println("\ndistDir:\t"+distDir);
        System.out.println("buildDir:\t"+buildDir);
        System.out.println("localizableFile:\t"+localizableFile);
        System.out.println("generatedFile:\t"+generatedFile);
        System.out.println("changedFile:\t"+changedFile);
        System.out.println("globalFile:\t"+globalFile);
        System.out.println("buildNumber:\t"+buildNumber);


        CvsEntries ce;

        p=this.getProject();

        for (int i=0; i<topdirs.length; i++) {
            // if (DEBUG)  System.out.println("STARTING TOPDIR "+topdirs[i]);
            for(int j=0; j<modules.length; j++) {
                if (modules[j] != null && ! modules[j].equals("")) {
                    @SuppressWarnings("unchecked")
                    Hashtable<String,String> props = p.getProperties();
                    fullPropHash = props;

                    // System.out.println("IN FOR MODULES "+modules[j]);
                    // System.out.println("\tSTARTING MODULE "+modules[j]);

                    File f = new File(topdirs[i]+File.separator+modules[j]+File.separator+localizableFile);
                    // System.out.println("Localizable file is: "+f.getAbsolutePath());

                    if ( f.exists() ) {
                        System.out.println("\t\tFILE exists"+topdirs[i]+File.separator+modules[j]+File.separator+localizableFile);
                        File topDir = new File(topdirs[i]);
                        File modDir = new File(topdirs[i]+File.separator+modules[j]);

                        // moved getGeneratedFiles from here to only getGeneratedFiles
                        // if there are localizableFiles.

                        localizableFiles = getLocalizableFiles(topDir, modules[j]);
                        if (localizableFiles == null ) {
                            if (DEBUG) {
                                System.out.println("\t\tNo Localizable Files for this module."+topDir+File.separator+modules[j]);
                            }
                        } else {

                            //don't look for the generated Files if there are no localizableFiles
                            generatedFileHash = getGeneratedFiles(topDir, modules[j]);
                            //if ( generatedFileHash == null ) { System. out.println("\t\tGENFILEHASH is null"); }

                                        /* DO PRE PROCESSING
                                                int lastSlashIndex;
                                                String parentDirFullPath;
                                                String cacheDirFullPath;
                                         */

                            for (int k=0; k<localizableFiles.length; k++) {

                                // DO SOME PRE-PROCESSING HERE. CACHE DIR REACTION (don't check for existance of same dir over & over again).


                                int lastSlashIndex = localizableFiles[k].lastIndexOf(File.separator);
                                String parentDirFullPath = localizableFiles[k].substring(0, lastSlashIndex);

                                // Check that the CVS/ dir exists.
                                // If it does not, assume we are dealing with a generated dir
                                // which does not need to be examined by me.
                                if ( ! new File(parentDirFullPath+"/CVS").exists() ) {
                                    //System.out.println("CVSDIR doesn't exist ["+parentDirFullPath+"/CVS ]");
                                    // skip to the end of this loop
                                    //HEY!  there must be a cleaner way!!
                                    System.out.println("This dir is a generated dir with no CVS dir "+parentDirFullPath);

                                } else {

                                    // PRINT TO "allfiles" list.
                                    boolean success = printToAllFile(topdirs[i], modules[j], localizableFiles);
                                    if (! success) {
                                        System.out.println("ERROR: Print to All File in "+topdirs[i]+", "+modules[j]+"failed.");

                                    }

                                    ce = cvsEntriesCache.get(parentDirFullPath);

                                    if ( ce == null ) {
                                        ce = new CvsEntries(parentDirFullPath);  //passing parentDirFullPath
                                        cvsEntriesCache.put(parentDirFullPath, ce);
                                    }

                                    if ( generatedFileHash == null ) {
                                        generatedFileHash=new Hashtable<String, String>();
                                    }


                                    String localizableFileOnly = localizableFiles[k].substring(localizableFiles[k].lastIndexOf(File.separator)+1);
                                    if (localizableFileOnly == null) { System.out.println("NULL LOCALIZABLE FILE"); }

                                    String ceRev = ce.getRevnoByFileName(localizableFileOnly);

                                    if ( ceRev == null ) {
                                        // If this file is NOT in ce (doesn't exist in CVS/Entries)
                                        // there is something wrong with the build working directory.
                                        // We will collect these file names & report at the end.

                                        //Maybe we don't want to add them.  THe build generates lots of files which aren't in CVS.
                                        // error.add(localizableFiles[k]);

                                    } else {
                                        // System.out.println("CEREV: "+ceRev);
                                        // compare revnos

                                        String genRev = generatedFileHash.get(localizableFiles[k]);
                                        // if (DEBUG) System.out.println("GENREV "+genRev + "\tFN: "+localizableFiles[k]);

                                        if ( genRev == null || ! ceRev.equals(genRev)) {
                                            //Update changed fileHash
                                            changed.put(localizableFiles[k], ceRev);


                                            //Update generatedFileHash
                                            generatedFileHash.put(localizableFiles[k], ceRev);


                                            //if (DEBUG) System.out.println("I got this far, but there was nothing CHANGED: "+localizableFiles[k]);
                                        }
                                    }
                                }

                            } //for localizable files

                            // IN HERE PRINT TO GENERATED FILE.
                            boolean success = printToFile(topdirs[i], modules[j]);


                            if (! success) {
                                System.out.println("ERROR: Print to File in "+topdirs[i]+", "+modules[j]+"failed.");
                            }

                            // toDir should be build/topDir
                            // baseDir should be fullpathtoTopDir
                            // if (DEBUG) System.out.println("BUILDDIR "+buildDir);
                            File tDir = new File(buildDir+File.separator+topdirs[i]);
                            int lio = topdirs[i].lastIndexOf(File.separator);
                            String shortTopdir = topdirs[i].substring(lio+1);


                            // make a tar no matter what.
                            // At a minimum it will have
                            // file module.l10n.list.all.
                            // THEN delete files.

                            mkTars(topdirs[i]+File.separator, buildDir+File.separator+shortTopdir+File.separator+modules[j]+".tar", topdirs[i]+File.separator+modules[j]+File.separator+modules[j]+"."+changedFile, modules[j]);
                            // if (! changed.isEmpty() ) {
                            Delete delete = (Delete)p.createTask("delete");
                            FileSet fs = new FileSet();
                            fs.setDir(new File(topdirs[i]+File.separator+modules[j]));
                            String includes = modules[j]+"."+generatedFile+","+ modules[j]+"."+changedFile+","+ modules[j]+"."+allFile;
                            fs.setIncludes(includes);
                            delete.addFileset(fs);

                            delete.setVerbose(true);
                            delete.execute();
                            //}


                            // Clean up before moving on.

                            // if (DEBUG) System.out.println("CLEARING "+topdirs[i] + " " + modules[j]);
                            generatedFileHash.clear();
                            changed.clear();
                            error.clear();

                        } // if File: topDirs[i]/modules[j]/localizableFile exists
                    } // if localizableFiles is not null
                } // if module isn't null
            } //for module
        } //for topdirs

        // Tar everything
        if (DEBUG) System.out.println("ABOUT TO MAKE THE BIG TAR: "+distDir+"/l10n-"+buildNumber+".tar.gz");

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
                System.out.println("NO tar file, can't gzip"+buildDir+"/l10n-"+buildNumber+".tar");
            }
        } else {
            System.out.println("No files in builddir.  No kit to build");
        }




    } // execute()

    public void mkTars(String srcDir, String fullTarfilePath, String fullIncludesFilePath, String module) {
        //String td = srcDir.substring(srcDir.lastIndexOf(File.separator)+1);
        if (DEBUG) {
            // System.out.println("SRCDIR = "+srcDir);
            //System.out.println("FULL TARFILE PATH = "+fullTarfilePath);
        }

        File incBaseDir = new File(srcDir);
        Tar tar = (Tar)p.createTask("tar");
        tar.setBasedir(new File(srcDir));
        tar.setDestFile(new File(fullTarfilePath));

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
        }

    }

    public boolean printToAllFile(String fullTopDir, String module, String[] localizableFiles) {
        try {
            int lastSlashIndex = fullTopDir.lastIndexOf(File.separator);
            String topDir= fullTopDir.substring(lastSlashIndex+1);

            File f = new File(buildDir+ File.separator +topDir );
            f.mkdirs();

            FileWriter allWrite = new FileWriter(fullTopDir+File.separator+module+File.separator+module+"."+allFile);

            if ( localizableFiles != null ) {
                for (int i=0; i<localizableFiles.length; i++) {
                    int lio = localizableFiles[i].lastIndexOf(fullTopDir);
                    if (lio >= 0) {
                        String moduleFileName= localizableFiles[i].substring(lio+fullTopDir.length()+1);
                        allWrite.write(moduleFileName+"\n");
                    } else {
                        System.out.println("Error: NO TOPDIR HERE: "+ localizableFiles[i]+ " FTD: "+fullTopDir+" LIO "+lio);
                    }

                }
            }
            allWrite.close();

        } catch (IOException ioe) {
            System.out.println("IOException"+ioe);
            return false;
        }
        return true;

    }
    public boolean printToFile(String fullTopDir, String module) {

        // if (DEBUG) System.out.println("IN printToFile FULLTOPDIR"+fullTopDir+ " MODULE" + module);

        try {

            int lastSlashIndex = fullTopDir.lastIndexOf(File.separator);
            String topDir= fullTopDir.substring(lastSlashIndex+1);

                /*  No need to mkdirs now - printToAllFile is making them.
                        File f = new File(buildDir+ File.separator +topDir );
                        f.mkdirs();
                 */

            FileWriter genWrite = new FileWriter(fullTopDir+File.separator+module+File.separator+module+"."+generatedFile);
            FileWriter changedWrite = new FileWriter(fullTopDir+File.separator+module+File.separator+module+"."+changedFile);


            if ( generatedFileHash == null ) {
                generatedFileHash = new Hashtable<String, String>();
            } else {

                for (Enumeration<String> g = generatedFileHash.keys() ; g.hasMoreElements() ;) {
                    String genFileKey = g.nextElement();
                    int lioTopDir =  genFileKey.lastIndexOf(topDir);
                    String moduleFileName = genFileKey.substring(lioTopDir+topDir.length()+1) ;

                    genWrite.write(moduleFileName+"\t"+generatedFileHash.get(genFileKey)+"\n");
                }

                // To make sure the changeFile & generatedFile appear in the tar,
                // make them the first lines in the file.
                for (Enumeration<String> c = changed.keys() ; c.hasMoreElements() ;) {
                    String changedFileKey = c.nextElement();

                    int lio = changedFileKey.lastIndexOf(topDir+File.separator+module);
                    String moduleFileName;
                    if (lio >= 0) {

                        moduleFileName=changedFileKey.substring(changedFileKey.lastIndexOf(topDir+File.separator+module)+topDir.length()+1);
                        changedWrite.write(moduleFileName+"\n");
                    } else {
                        // If we get here, the cache is probobly not really being cleared.

                        //The cache is not being cleared
                        // OR we have wild-card characters
                        System.out.println("WARNING: L10n.list file error. Each item in your list should reference the current module.  If this is a global l10n file used over several modules use the property ${l10n-module} as a place-holder.  This error occurred in "+module+".");
                        // Contact program administrator.\r\t"+ changedFileKey+ "\r\tTD "+topDir+File.separator+module +" LIO "+lio);
                    }

                    //changedWrite.write(changedFileKey+"\n");
                    //Changed should have a list of fullpath in order to do copy
                }
            }

            if (! error.isEmpty() ) {
                FileWriter errorWrite = new FileWriter(buildDir+File.separator+"l10n-errors.txt", true);
                for (Enumeration e = error.elements(); e.hasMoreElements();) {
                    String ee = (String)e.nextElement();
                    errorWrite.write(ee+"\n");
                    System.out.println("Error: "+ee);
                }
                errorWrite.close();
            }


            genWrite.close();
            changedWrite.close();


        } catch (IOException ioe) {
            System.out.println("IOException printToFile()"+ioe);
            return false;
        }
        return true;
    }

    /** You can use this function independently of "execute()", but
     * you have to set these attributes: localizableFile,
     * globalFile (if "read global" is used in any l10n.list
     * files), excludePattern (if desired), includePattern (if
     * desired)
     */
    public String[] getLocalizableFiles(File topRoot, String module) {
        String[] lfs = null;
        StringBuffer[] sbholder=new StringBuffer[2];
        StringBuffer sbi= new StringBuffer();
        StringBuffer sbe = new StringBuffer();
        String includeS="";
        String excludeS="";
        // if (DEBUG) System.out.println("\t\tIN getLocalizableFiles(File "+topRoot.getName()+", " + module+")");

        if( fullPropHash == null) {
            p = getProject() ;
            @SuppressWarnings("unchecked")
            Hashtable<String,String> props = p.getProperties();
            fullPropHash = props;
        }
        fullPropHash.put("l10n-module", module);

        try {
            File includes = new File(topRoot.getCanonicalPath() + File.separator + module + File.separator + localizableFile);
            if ( ! includes.exists() || includes.length() <= 0 ) {
                //if (DEBUG) System.out.println("FILE IS too short to mess with "+ module);
                return lfs;
            }

            try {
                sbholder = processListFile(includes,module);
            } catch (java.io.IOException ioe) {
                System.out.println("Error processing file. "+ioe);
            }

            if (sbholder != null) {
                sbi = sbholder[0];
                sbe = sbholder[1];

                sbe.append(" "+excludePattern);
                sbi.append(" "+includePattern);

                if (DEBUG) {
                    System.out.println("INC "+sbi.toString());
                    System.out.println("EXC "+sbe.toString());
                }
            }

            this.fileset = new FileSet();

            // this.setIncludesfile(includes);
            if ( sbi !=null) {
                this.setIncludes(sbi.toString());
                //changed to accomodate excludes in file
            }

            if (sbe != null ) {
                this.setExcludes(sbe.toString());
                // Sets the set of exclude patterns.
                // Patterns may be separated by a comma or a space.
            }

            DirectoryScanner ds = this.getDirectoryScanner(topRoot);

            ds.scan();
            lfs = ds.getIncludedFiles();

            for (int k=0; k<lfs.length; k++) {
                // if (DEBUG) System.out.println("\t\t\tLFS "+lfs[k]);
                lfs[k] = topRoot+File.separator+lfs[k].trim();
                //lfs[k] = topRoot+File.separator+lfs[k];
            }

            // if (DEBUG) System.out.println("THERE ARE "+lfs.length + " FILES in INCL FILES");
        } catch(java.io.IOException e) {
            System.out.println(e.getMessage());
        }
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

            String topDirFullPath = topDir.getCanonicalPath();
            // if (DEBUG) System.out.println("\t\ttopDirFullPath: "+topDirFullPath);

            BufferedReader inBuff = new BufferedReader(new FileReader(new File(topDir+File.separator+mod,generatedFile)));
            boolean eof = false;
            while (! eof) {
                String line = inBuff.readLine();
                if (line==null) {
                    eof = true;
                } else {
                    //System.out.println("LL "+ line);

                    int tabIndex = line.indexOf("\t");
                    if (tabIndex > 0) {
                        String filename = line.substring(0,tabIndex);
                        String revision = line.substring(tabIndex+1);
                        h.put(topDirFullPath+File.separator+filename, revision);
                    } else {
                        System.out.println("There's no tab in this line"+"["+line+"]");

                    }

                }

            } //while
        } catch(java.io.FileNotFoundException e) {
            // Warning: Generated File: "+generatedFile+" in "+
            // topDir+File.separator+mod+" not found.
            // Adding all files to changed list."
            return(null);
        } catch(java.io.IOException e) {
            System.out.println("IOException "+ e);
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
                System.out.println("TOPS "+tops[j]);
            }
        }

        this.topdirs = tops;

    }
    public void setModules(String s) {
        StringTokenizer st = new StringTokenizer(s,",");
        HashSet<String> modSet = new HashSet<String>(); //This will guarantee that there will be no duplications
        String fullMod = null;

        while (st.hasMoreTokens()) {
            fullMod=st.nextToken().trim();;

            // if (DEBUG) System.out.println("ITEM IN MODLIST: "+fullMod);

            int index = fullMod.indexOf(File.separator);
            if (index >= 0) {
                //Check that the mod doesn't have a slash
                // (if it does, keep only what is to the left of the slash)
                fullMod=fullMod.substring(0,index);

                // if (DEBUG) System.out.println("CHANGED FULLMOD & it's NOW "+fullMod);
            }
            modSet.add( fullMod );
        }
        this.modules = new String[ modSet.size() ];
        Iterator<String> it = modSet.iterator();

        int i=0;
        while( it.hasNext() )
            this.modules[i++] = it.next();

    }

    public StringBuffer[] processListFile(File inc,String module) throws IOException {
        System.out.println("Reading list file: "+inc.toString());
        StringBuffer[] sbholder=new StringBuffer[2];
        StringBuffer sbi = new StringBuffer();
        StringBuffer sbe = new StringBuffer();


        if(inc.exists() && inc.length() >0) {
            //Read Includes File.
            BufferedReader br = new BufferedReader(new FileReader(inc));
            String line;
            while((line=br.readLine()) != null && line.indexOf("#") <0) {
                boolean skipit=false;
                // For each line check if there are any properties which
                // should be interpreted first.
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
                        // System.out.println("LINE is now "+line);
                    } else {
                        System.out.println("Uninterpretable property in l10n file:"+inc.toString()+". "+propertyName+" Interpreting the entire line literally.");
                        skipit=true;
                    }
                }

                if (line.indexOf("exclude") >= 0) {
                    sbe.append(" "+line.substring("exclude".length()+1));
                } else if (line.indexOf("read global") >= 0) {

                    if (globalFile != null && ! globalFile.equals("")) {
                        if (readGlobalFile==true) {
                            System.out.println("Already read it");
                            //We did it once, don't do it again.
                            // just copy in the values.
                            sbi.append(" "+globalsbholder[0]);
                            sbe.append(" "+globalsbholder[1]);
                        } else {
                            globalsbholder[0]= new StringBuffer();
                            globalsbholder[1]= new StringBuffer();
                            StringBuffer[] globalarray = processListFile(new File(globalFile),module);
                            if (globalarray[0] != null) {
                                sbi.append(" "+globalarray[0]);
                                globalsbholder[0].append(" "+globalarray[0]);
                                //put it where we can get it back.
                            }
                            if (globalarray[1] != null) {
                                sbe.append(" "+globalarray[1]);
                                globalsbholder[1].append(" "+globalarray[1]);
                                //put it where we can get it back.
                            }
                        }
                        readGlobalFile = true ;
                    }
                } else if( line.trim().startsWith( "read")) {
                    String l = line.trim() ;
                    l = l.substring( 4) ;
                    l = l.trim() ;
                    StringBuffer[] sbarr = processListFile( new File( l),
                            module) ;
                    sbi.append( " " + sbarr[ 0]) ;
                    sbe.append( " " + sbarr[ 1]) ;
                } else {
                    sbi.append(" "+line);
                }
                // System.out.println("GLOBAL"+line.indexOf("read global")+" EXCLUDES "+line.indexOf("exclude")+ "FILE"+inc.toString());
            } //while
            br.close();
        }
        sbholder[0]=sbi;
        sbholder[1]=sbe;
        return sbholder;
    }

    public void setDistDir(String s) {
        // if (DEBUG) System.out.println("DIST DIR SHOULD BE: "+s);
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
