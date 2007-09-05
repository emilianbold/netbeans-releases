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

package org.netbeans.updater;

import java.io.*;
import java.util.*;
import java.util.jar.*;

//import org.openide.util.NbBundle;

/** Class used by autoupdate module for the work with module files and
 * for installing / uninstalling modules
 *
 * @author  Petr Hrebejk, Ales Kemr
 * @version
 */
public final class ModuleUpdater extends Thread {

    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty ("file.separator");
    private static final String PATH_SEPARATOR = System.getProperty ("path.separator");    

    /** Relative name of update directory */
    private static final String UPDATE_DIR = "update"; // NOI18N

    /** Relative name of directory where the .NBM files are downloaded */
    static final String DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N

    /** Relative name of backup directory */
    private static final String BACKUP_DIR = UPDATE_DIR + FILE_SEPARATOR + "backup"; // NOI18N

    /** The name of zip entry containing netbeans files */
    public static final String UPDATE_NETBEANS_DIR = "netbeans"; // NOI18N

    /** The name of zip entry containing java_extension files */
    public static final String UPDATE_JAVA_EXT_DIR = "java_ext"; // NOI18N

    /** The name of zip entry containing files for external installer */
    public static final String UPDATE_MAIN_DIR = "main"; // NOI18N
        
    /** Name of external installer parameters file*/
    private static final String JVM_PARAMS_FILE = "main.properties"; // NOI18N

    /** Extension of the distribution files */
    public static final String NBM_EXTENSION = "nbm"; // NOI18N

    /** The name of the log file */
    public static final String LOG_FILE_NAME = "update.log"; // NOI18N

    /** The name of the install_later file */
    public static final String LATER_FILE_NAME = "install_later.xml"; // NOI18N
    
    public static final char SPACE = ' ';
    public static final char QUOTE = '\"';
    
    /** files that are supposed to be installed (when running inside the ide) */
    private Set<File> installOnly;
    /** found files in various cluster/update/download folders */
    private Set<File> installFiles;
    
    /** Should the thread stop */
    private volatile boolean stop = false;

    private volatile boolean suspend = false;

    /** Total length of unpacked files */
    private long totalLength;

    private static boolean fromInstall = false;
    
    /** Creates new ModuleUpdater */
    @Override
    public void run() {
        try {

            checkStop();

            installFiles = new HashSet<File> ();
            for (File cluster : UpdateTracking.clusters (true)) {
                installFiles.addAll (getModulesToInstall (cluster));
                deleteInstall_Later (cluster);
            }

            if (installOnly != null) {
                // keep only those that we really wish to install
                installFiles.retainAll (installOnly);
                    }

            if (installFiles.isEmpty ()) {
                endRun();
            }

            checkStop();

            totalLength();

            checkStop();

            unpack();
            
            for (File cluster: UpdateTracking.clusters (true)) {
                deleteAdditionalInfo (cluster);
            }

        } catch (Exception x) {
            x.printStackTrace ();
        } finally {
            UpdaterFrame.getUpdaterFrame().unpackingFinished();
        }
    }
    
    private void deleteInstall_Later (File cluster) {
        File later = new File (cluster, FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + LATER_FILE_NAME);
        if ( later.exists() ) {
            later.delete();
        }
    }

    private void deleteAdditionalInfo (File cluster) {
        File additional = new File (cluster, FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + UpdateTracking.ADDITIONAL_INFO_FILE_NAME);
        if (additional != null && additional.exists ()) {
            additional.delete ();
        }
    }

    /** ends the run of update */
    void endRun() {
        stop = true;
    }

    /** checks wheter ends the run of update */
    private void checkStop() {

        if ( suspend )
            while ( suspend );

        if ( stop ) {
            if (UpdaterFrame.getUpdaterFrame().isFromIDE ()) {
                UpdaterFrame.getUpdaterFrame().unpackingFinished();
            } else {
                System.exit( 0 );
            }
        }
    }

    /** Can be used to restrict the set of NBM files that should be installed.
     */
    public void setInstallOnly (File[] files) {
        installOnly = new HashSet<File> ();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            try {
                f = f.getCanonicalFile ();
            } catch (IOException ex) {
                // ok, just use regular file
            }
            installOnly.add (f);
        }
    }

    /** Determines size of unpacked modules */
    private void totalLength() {
        totalLength = 0L;

        UpdaterFrame.setLabel( Localization.getBrandedString( "CTL_PreparingUnpack" ) );
        UpdaterFrame.setProgressRange( 0, installFiles.size ());

        Iterator<File> it = installFiles.iterator ();
        for( int i = 0; i < installFiles.size (); i++ ) {

            JarFile jarFile = null;

            try {
                UpdaterFrame.setProgressValue( i + 1 );

                jarFile = new JarFile( it.next () );
                Enumeration<JarEntry> entries = jarFile.entries();
                while( entries.hasMoreElements() ) {
                    JarEntry entry = entries.nextElement();

                    checkStop();

                    if ( ( entry.getName().startsWith( UPDATE_NETBEANS_DIR ) ||
                            entry.getName().startsWith( ModuleUpdater.UPDATE_JAVA_EXT_DIR ) ||
                            entry.getName().startsWith( UPDATE_MAIN_DIR) ) &&
                            !entry.isDirectory() ) {
                        totalLength += entry.getSize();
                    }
                }
            }
            catch ( java.io.IOException e ) {
                // Ignore non readable files
            }
            finally {
                try {
                    if ( jarFile != null )
                        jarFile.close();
                }
                catch ( java.io.IOException e ) {
                    // We can't close the file do nothing
                    // System.out.println( "Cant close : " + e ); // NOI18N
                }
            }
        }
    }


    /** Unpack the distribution files into update directory */

    private void unpack ()  {
        long bytesRead = 0L;
        boolean hasMainClass;

        // System.out.println("Total lengtg " + totalLength ); // NOI18N

        UpdaterFrame.setLabel( "" ); // NOI18N
        UpdaterFrame.setProgressRange( 0, totalLength );
        
        fromInstall = true;
        
        ArrayList<UpdateTracking> allTrackings = new ArrayList<UpdateTracking> ();
        Map<ModuleUpdate, UpdateTracking.Version> l10ns = 
                new HashMap<ModuleUpdate, UpdateTracking.Version>();
        
        for (File cluster : UpdateTracking.clusters (true)) {
            Set<File> nbms = getModulesToInstall (cluster);
            if (nbms.isEmpty ()) {
                continue;
            }
            
            UpdateTracking tracking = UpdateTracking.getTracking (cluster, true);
            if (tracking == null) {
                throw new RuntimeException ("No update_tracking file in cluster " + cluster);
            }
            allTrackings.add (tracking);

            nbms.retainAll (installFiles);
            
            File[] nbmFiles = nbms.toArray (new File[0]);
            for( int i = 0; i < nbmFiles.length; i++ ) {                        
                UpdateTracking.Version version;
                UpdateTracking.Module modtrack;
                
                UpdaterFrame.getUpdaterFrame ().unpackingIsRunning ();
                
                ModuleUpdate mu = null;
                try {
                    mu = new ModuleUpdate( nbmFiles[i], fromInstall );
                } catch (RuntimeException re) {
                    if (nbmFiles [i].exists ()) {
                        if (! nbmFiles[i].delete ()) {
                            System.out.println("Error: File " + nbmFiles [i] + " cannot be deleted. Propably file lock on the file."); // NOI18N
                            assert false : "Error: File " + nbmFiles [i] + " cannot be deleted. Propably file lock on the file.";
                            nbmFiles[i].deleteOnExit ();
                        }
                    }
                    continue;
                }
                assert mu != null : "Module update is not null for file: " + nbmFiles[i]; // NOI18N
                if ( mu.isL10n() ) {
                    modtrack = null;
                    version = tracking.createVersion( "0" ); // NOI18N
                    l10ns.put( mu, version );
                } else {
                    modtrack = tracking.readModuleTracking( ! fromInstall, mu.getCodenamebase(), true );
                    // find origin for file
                    UpdateTracking.AdditionalInfo info = UpdateTracking.getAdditionalInformation (cluster);
                    String origin = info != null && info.getSource (nbmFiles [i].getName ()) != null ?
                        info.getSource (nbmFiles [i].getName ()) : UpdateTracking.UPDATER_ORIGIN;
                    version = modtrack.addNewVersion (mu.getSpecification_version (), origin);
                }
                // input streams should be released, but following is needed
                //System.gc();

                hasMainClass = false;
                UpdaterFrame.setLabel( Localization.getBrandedString("CTL_UnpackingFile") + "  " + nbmFiles[i].getName() ); //NOI18N
                UpdaterFrame.setProgressValue( bytesRead );
                JarFile jarFile = null;

                try {
                    jarFile = new JarFile( nbmFiles[i] );
                    Enumeration entries = jarFile.entries();
                    while( entries.hasMoreElements() ) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        checkStop();
                        if ( entry.getName().startsWith( UPDATE_NETBEANS_DIR ) ) {
                            if (! entry.isDirectory ()) {
                                String pathTo = entry.getName ().substring (UPDATE_NETBEANS_DIR.length () + 1);
                                // path without netbeans prefix
                                if ( mu.isL10n() )
                                    version.addL10NFileWithCrc( pathTo, Long.toString( entry.getCrc() ), mu.getSpecification_version());
                                else
                                    version.addFileWithCrc( pathTo, Long.toString( entry.getCrc() ) );

                                File destFile = new File (cluster, entry.getName ().substring (UPDATE_NETBEANS_DIR.length()));
                                if ( destFile.exists() ) {
                                    File bckFile = new File( getBackupDirectory (cluster), entry.getName() );
                                    bckFile.getParentFile ().mkdirs ();
                                    // System.out.println("Backing up" ); // NOI18N
                                    copyStreams( new FileInputStream( destFile ), new FileOutputStream( bckFile ), -1 );
                                } else {
                                    destFile.getParentFile ().mkdirs ();
                                }
                                bytesRead = copyStreams( jarFile.getInputStream( entry ), new FileOutputStream( destFile ), bytesRead );
                                UpdaterFrame.setProgressValue( bytesRead );
                            }
                        } else if ( entry.getName().startsWith( UPDATE_MAIN_DIR )&&
                                  !entry.isDirectory() ) {
                            // run main                  
                            File destFile = new File (getMainDirectory (cluster),
                                entry.getName().substring(UPDATE_MAIN_DIR.length() + 1) );
                            destFile.getParentFile ().mkdirs ();
                            hasMainClass = true;
                            bytesRead = copyStreams( jarFile.getInputStream( entry ), new FileOutputStream( destFile ), bytesRead );
                            UpdaterFrame.setProgressValue( bytesRead );
                        }
                    }
                    if ( hasMainClass ) {                    
                        MainConfig mconfig = new MainConfig (getMainDirString (cluster) + FILE_SEPARATOR + JVM_PARAMS_FILE, cluster);
                        if (mconfig.isValid()) {
                            String java_path = System.getProperty ("java.home") + FILE_SEPARATOR
                                + "bin"  + FILE_SEPARATOR + "java";                              // NOI18N
                            java_path = quoteString( java_path );
                            String torun = java_path + " -cp " + quoteString (getMainDirString (cluster) + mconfig.getClasspath() ) + mconfig.getCommand();  // NOI18N
                            startCommand(torun);

                            deleteDir( getMainDirectory (cluster) );
                        }
                    }
                }
                catch ( java.io.IOException e ) {
                    // Ignore non readable files
                    e.printStackTrace ();
                }
                finally {
                    try {
                        if ( jarFile != null )
                            jarFile.close();
                    }
                    catch ( java.io.IOException e ) {
                        // We can't close the file do nothing
                        // System.out.println("Can't close : " + e ); // NOI18N
                    }
                    //System.out.println("Dleting :" + nbmFiles[i].getName() + ":" + nbmFiles[i].delete() ); // NOI18N

                    if (! nbmFiles[i].delete ()) {
                        System.out.println("Error: Cannot delete " + nbmFiles [i]); // NOI18N
                        nbmFiles [i].deleteOnExit ();
                    }
                }
                if (! mu.isL10n ()) {
                    modtrack.write ();
                    modtrack.writeConfigModuleXMLIfMissing ();
                }
            }
        }
        
        for (UpdateTracking t: allTrackings) {
            // update_tracking of l10n's
            for (Map.Entry<ModuleUpdate, UpdateTracking.Version> entry: l10ns.entrySet()) {
                ModuleUpdate mod = entry.getKey();
                UpdateTracking.Version version = entry.getValue();
                UpdateTracking.Module modtrack = t.readModuleTracking( 
                    ! mod.isFromInstall(), 
                    mod.getCodenamebase(), 
                    true 
                );
                modtrack.addL10NVersion( version );
                modtrack.write();
            }
            t.deleteUnusedFiles ();            
        }
    }

    private void startCommand(String torun) {
        Runtime runtime=Runtime.getRuntime();
        Process proces;            
        try {
            proces=runtime.exec(parseParameters( torun ));
            final Process proc2 = proces;
            new Thread() {
                public void run() {
                    try {
                        InputStreamReader stream= new InputStreamReader (proc2.getErrorStream());
                        BufferedReader reader= new BufferedReader(stream);
                        String vystup;
                        do {
                            vystup = reader.readLine();
                            if (vystup!=null)
                                System.out.println(vystup);
                        } while (vystup != null);
                    } catch (Exception e) {
                        e.printStackTrace();
                  }
                }
            }.start();
            int x=proces.waitFor();
        }
        catch (Exception e){
          e.printStackTrace();
        }
    }
    
    /** The directory where to backup old versions of modules */
    public File getBackupDirectory (File activeCluster) {
        // #72960: Backup file created in wrong cluster
        File backupDirectory = new File (activeCluster, BACKUP_DIR);
        if (! backupDirectory.isDirectory ()) {
            backupDirectory.mkdirs();
        }

        return backupDirectory;
    }

    /** Gets the netbeans directory */
    private File getMainDirectory (File activeCluster) {
        // #72918: Post-install cannot write into platform cluster
        File mainDirectory = new File (activeCluster, FILE_SEPARATOR + UPDATE_DIR + FILE_SEPARATOR + UPDATE_MAIN_DIR);
        if (! mainDirectory.isDirectory ()) {
            mainDirectory.mkdirs();
        }

        return mainDirectory;
    }
    
    private String getMainDirString (File activeCluster) {
        return getMainDirectory (activeCluster).getPath ();
    }
    
     /** Quotes string correctly, eg. removes all quotes from the string and adds 
      * just one at the start and
      * second one at the end.
      * @param s string to be quoted
      * @return correctly quoted string
      */
     public static final String quoteString(String s) {
         if ( s.indexOf( SPACE ) > -1 ) {
             StringBuffer sb = new StringBuffer(s);
             int i = 0;
             while ( i < sb.length() ) {
                 if ( sb.charAt(i) == QUOTE )
                     sb.deleteCharAt( i );
                 else
                     i++;
             }
             sb.insert( 0, QUOTE );
             sb.append( QUOTE );
             return sb.toString();
         }
         return s;
     }    

    /**
     * It takes the current progress value so it can update progress
     * properly, and also return the new progress value after the
     * copy is done.
     *
     * @param progressVal The current progress bar value.  If this is
     *          negative, we don't want to update the progress bar.
     */
    private long copyStreams( InputStream src, OutputStream dest,
                               long progressVal ) throws java.io.IOException {

        BufferedInputStream bsrc = new BufferedInputStream( src );
        BufferedOutputStream bdest = new BufferedOutputStream( dest );

        int count = 0;

        int c;

        try {
            while( ( c = bsrc.read() ) != -1 ) {
                bdest.write( c );
                count++;
                if ( count > 8500 ) {
                    if (progressVal >= 0) {
                        progressVal += count;
                        UpdaterFrame.setProgressValue( progressVal );
                    }

                    count = 0;
                    checkStop();
                }
            }
            // Just update the value, no need to update the
            // GUI yet.   Caller can do that.
            if (progressVal >= 0) {
                progressVal += count;
            }
        }
        finally {
            bsrc.close();
            bdest.close();
            src.close();
            dest.close();
        }
        return progressVal;

    }

    /** Test whether the user has rights to write into directory */

    private static boolean canWrite( File dir, boolean create ) {
        if ( !dir.exists() && create )

            dir.mkdirs();

        if ( !dir.isDirectory() || !dir.canWrite() )
            return false;



        File tmp = null;

        try {
            tmp = File.createTempFile( "test", "access", dir ); // NOI18N
        }
        catch ( java.io.IOException e ) {
            return false;
        }

        if ( tmp == null )
            return false;

        boolean cw = tmp.canWrite();
        if (cw)
            tmp.delete();

        return cw;
    }

    private void deleteDir(File dir) {
        File[] files=dir.listFiles();
        for( int j = 0; j < files.length; j++ ) {
            if ( files[j].isDirectory() ) {
                deleteDir( files[j] );
                if (! files[j].delete()) {
                    System.out.println("Error: Cannot delete " + files [j]); //NOI18N
                    assert false : "Cannot delete " + files [j];
                }
            }
        }
    }
        
    /** [Copied from org.openide.util.Utilities]
     * Parses parameters from a given string in shell-like manner.
     * Users of the Bourne shell (e.g. on Unix) will already be familiar
     * with the behavior.
     * For example, when using {@link org.openide.execution.NbProcessDescriptor}
     * you should be able to:
     * <ul>
     * <li>Include command names with embedded spaces, such as 
     * <code>c:\Program Files\jdk\bin\javac</code>.
     * <li>Include extra command arguments, such as <code>-Dname=value</code>.
     * <li>Do anything else which might require unusual characters or
     *     processing. For example:
     * <p><code><pre>
     * "c:\program files\jdk\bin\java" -Dmessage="Hello /\\/\\ there!" -Xmx128m
     * </pre></code>
     * <p>This example would create the following executable name and arguments: 
     * <ol>
     * <li> <code>c:\program files\jdk\bin\java</code>
     * <li> <code>-Dmessage=Hello /\/\ there!</code>
     * <li> <code>-Xmx128m</code>
     * </ol>
     * Note that the command string does not escape its backslashes--under the assumption
     * that Windows users will not think to do this, meaningless escapes are just left
     * as backslashes plus following character.
     * </ul>
     * <em>Caveat</em>: even after parsing, Windows programs (such as
     * the Java launcher)
     * may not fully honor certain
     * characters, such as quotes, in command names or arguments. This is because programs
     * under Windows frequently perform their own parsing and unescaping (since the shell
     * cannot be relied on to do this). On Unix, this problem should not occur.
     * @param s a string to parse
     * @return an array of parameters
     */
     private static String[] parseParameters(String s) {
         int NULL = 0x0;  // STICK + whitespace or NULL + non_"
         int INPARAM = 0x1; // NULL + " or STICK + " or INPARAMPENDING + "\ // NOI18N
         int INPARAMPENDING = 0x2; // INPARAM + \
         int STICK = 0x4; // INPARAM + " or STICK + non_" // NOI18N
         int STICKPENDING = 0x8; // STICK + \
         Vector<String> params = new Vector<String>(5,5);
         char c;
 
         int state = NULL;
         StringBuilder buff = new StringBuilder(20);
         int slength = s.length();
         for (int i = 0; i < slength; i++) {
             c = s.charAt(i);
             if (Character.isWhitespace(c)) {
                 if (state == NULL) {
                     if (buff.length () > 0) {
                         params.addElement(buff.toString());
                         buff.setLength(0);
                     }
                 } else if (state == STICK) {
                     params.addElement(buff.toString());
                     buff.setLength(0);
                     state = NULL;
                 } else if (state == STICKPENDING) {
                     buff.append('\\');
                     params.addElement(buff.toString());
                     buff.setLength(0);
                     state = NULL;
                 } else if (state == INPARAMPENDING) {
                     state = INPARAM;
                     buff.append('\\');
                     buff.append(c);
                 } else {    // INPARAM
                     buff.append(c);
                 }
                 continue;
             }
 
             if (c == '\\') {
                 if (state == NULL) {
                     ++i;
                     if (i < slength) {
                         char cc = s.charAt(i);
                         if (cc == '"' || cc == '\\') {
                             buff.append(cc);
                         } else if (Character.isWhitespace(cc)) {
                             buff.append(c);
                             --i;
                         } else {
                             buff.append(c);
                             buff.append(cc);
                         }
                     } else {
                         buff.append('\\');
                         break;
                     }
                     continue;
                 } else if (state == INPARAM) {
                     state = INPARAMPENDING;
                 } else if (state == INPARAMPENDING) {
                     buff.append('\\');
                     state = INPARAM;
                 } else if (state == STICK) {
                     state = STICKPENDING;
                 } else if (state == STICKPENDING) {
                     buff.append('\\');
                     state = STICK;
                 }
                 continue;
             }
 
             if (c == '"') {
                 if (state == NULL) {
                     state = INPARAM;
                 } else if (state == INPARAM) {
                     state = STICK;
                 } else if (state == STICK) {
                     state = INPARAM;
                 } else if (state == STICKPENDING) {
                     buff.append('"');
                     state = STICK;
                 } else { // INPARAMPENDING
                     buff.append('"');
                     state = INPARAM;
                 }
                 continue;
             }
 
             if (state == INPARAMPENDING) {
                 buff.append('\\');
                 state = INPARAM;
             } else if (state == STICKPENDING) {
                 buff.append('\\');
                 state = STICK;
             }
             buff.append(c);
         }
         // collect
         if (state == INPARAM) {
             params.addElement(buff.toString());
         } else if ((state & (INPARAMPENDING | STICKPENDING)) != 0) {
             buff.append('\\');
             params.addElement(buff.toString());
         } else { // NULL or STICK
             if (buff.length() != 0) {
                 params.addElement(buff.toString());
             }
         }
         String[] ret = new String[params.size()];
         params.copyInto(ret);
         return ret;
     }


    
    /** read jvm parameters from jvm parameters file */
    class MainConfig extends Object {
        
        /** The names of properties from jvm parameters file */
        private final String PAR_MAIN = "mainClass";               // NOI18N
        private final String PAR_RELCP = "relativeClassPath";      // NOI18N
        private final String PAR_JVMPAR = "jvm.parameters";        // NOI18N
        private final String PAR_MAINARGS = "mainClass.arguments"; // NOI18N
        
        /** The names of variables allow to use in jvm parameters file */
        private final String VAR_IDE_HOME = "%IDE_HOME%";          // NOI18N
        private final String VAR_IDE_USER = "%IDE_USER%";          // NOI18N
        private final String VAR_FILE_SEPARATOR = "%FS%";          // NOI18N        
        private final String VAR_JAVA_HOME = "%JAVA_HOME%";        // NOI18N        
    
        /** joined all parameters of jvm java command */
        private String parameters = ""; // NOI18N
        private String classpath = ""; // NOI18N
        
        /** is jvm parameters file in valid stucture */
        private boolean valid = false;
        private final File activeCluster;
        
        public MainConfig (String spath, File activeCluster) {
            valid = readParms(spath);
            this.activeCluster = activeCluster;
        }
        
        /** returns all parameters needed by jvm java command */
        public String getCommand() {
            return parameters;
        }
        
        /** returns all parameters needed by jvm java command */
        public String getClasspath() {
            return classpath;
        }
        
        /** is jvm parameters file in valid stucture */
        public boolean isValid() {
            return valid;
        }
        
        /** read jvm parameters from jvm parameters file */
        private boolean readParms(String spath) {
            Properties details = new Properties();
            FileInputStream fis = null;
            try {
                details.load(fis = new FileInputStream(spath)); // NOI18N
            } catch (IOException e) {            
                return false;
            } finally {
                if (fis != null) try { fis.close(); } catch (IOException e) { /* ignore */ };
            }
            
            String mainclass;
            String relpath;
            String jvmparms;
            String mainargs;        
        
            relpath = details.getProperty(PAR_RELCP,null);
            if (relpath != null) {
                relpath = replaceVars( relpath );
                StringTokenizer token = new StringTokenizer( relpath, PATH_SEPARATOR, false );
                while ( token.hasMoreTokens() ) {
                    classpath = classpath + PATH_SEPARATOR + changeRelative( token.nextToken() );
                }
            }
        
            parameters = "";
            jvmparms = details.getProperty(PAR_JVMPAR,null);
            if (jvmparms != null)
                parameters = parameters + " " + jvmparms;  // NOI18N
            
            mainclass = details.getProperty(PAR_MAIN,null);
            if (mainclass == null)
                return false;
            else
                parameters = parameters + " " + mainclass;  // NOI18N
            
            mainargs = details.getProperty(PAR_MAINARGS,null);
            if (mainargs != null)
                parameters = parameters + " " + mainargs;  // NOI18N

            parameters = replaceVars( parameters );
            return true;            
        }
        
        private String replaceVars(String original) {
            original = replaceAll(original,VAR_IDE_HOME,
                org.netbeans.updater.UpdateTracking.getPlatformDir ().getPath());
            original = replaceAll(original,VAR_IDE_USER,
                org.netbeans.updater.UpdateTracking.getPlatformDir ().getPath());            
            original = replaceAll(original,VAR_FILE_SEPARATOR,
                FILE_SEPARATOR);            
            original = replaceAll(original,VAR_JAVA_HOME,
                System.getProperty ("java.home"));
            return original;
        }
        
        private String changeRelative(String path) {
            if ( new File( path ).isAbsolute() )
                return path;
            else
                return getMainDirString (this.activeCluster) + FILE_SEPARATOR + path;
        }
        
        
        /** replace all occurences of String what by String repl in the String sin */
        private String replaceAll(String sin, String what, String repl) {
            StringBuffer sb = new StringBuffer(sin);
            int i = sb.toString().indexOf(what);
            int len = what.length();
            while ( i > -1 ) {
                sb.replace(i,i + len,repl);
                i = sb.toString().indexOf(what,i+1);                
            }

            return sb.toString();
        }
    }
    
    /** Compute the list of modules that should be installed into this 
     * cluster.
     * @param File root of cluster
     * @return List<File> of nbm files
     */
    private static Set<File> getModulesToInstall (File cluster) {
        
        class NbmFilter implements java.io.FilenameFilter {
            public boolean accept (File dir, String name) {
                return name.endsWith (ModuleUpdater.NBM_EXTENSION);
            }
        }
        
        File idir = new File (cluster, ModuleUpdater.DOWNLOAD_DIR);
        try {
            idir = idir.getCanonicalFile ();
        } catch (IOException ioe) {
            System.out.println("ERROR: " + ioe);
        }
        File[] arr = idir.listFiles (new NbmFilter ());
        
        if (arr == null) {
            return Collections.emptySet ();
        } else {
            return new HashSet<File> (Arrays.asList (arr));
        }
    }

}
