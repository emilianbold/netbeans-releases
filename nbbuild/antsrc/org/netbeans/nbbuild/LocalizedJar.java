/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.util.Map; // override org.apache.tools.ant.Map
import java.util.zip.*;
import java.util.jar.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.*;

/** Create a JAR file with locale variants.
 * Whenever files are found which should be localized, or are the result
 * of localization, places them in separate JAR files named according to the locale,
 * in a <samp>locale/</samp> subdirectory of the directory containing the master JAR.
 * Each sub-JAR gets a manifest which just has some informational tags
 * indicating its purpose (locale and branding):
 * <code>X-Informational-Archive-Locale</code> and/or <code>X-Informational-Archive-Branding</code>.
 * The values may be e.g. <code>ja</code> or <code>f4j_ce</code>; or <code>-</code>
 * if there is no suffix for this JAR.
 * You can control the available locales; brandings; and set of files which should
 * always be considered part of the localizable base kit.
 * You can use the "branding" and "locale" subelements to control the branded
 * and localized .jar files that will be produced.  Also, you can set the global
 * properties "locjar.brands" and "locjar.locales" to comma-separated
 * lists of branding or locale identifiers so that NetBeans-based projects can
 * brand or localize NetBeans without having to maintain modified versions of all
 * the individual Ant scripts
 * Originally this Ant task didn't recognize files below a directory with the
 * same name as a locale as being localized.  Now it does so by default.
 * <p>Based on <code>&lt;zip&gt;</code> and <code>&lt;jar&gt;</code> tasks in Ant,
 * but not feasible to simply subclass or compose them.
 * @see <a href="http://www.netbeans.org/devhome/docs/i18n/index.html">NetBeans I18N documentation</a>
 * @author Jesse Glick
 */
public class LocalizedJar extends MatchingTask {

    private List localeKits = new LinkedList (); // List<FileSet>
    private List locales = new LinkedList (); // List<LocaleOrB>
    private List brandings = new LinkedList (); // List<LocaleOrB>
    private File jarFile;
    private File baseDir;
    private boolean doCompress = false;
    private static long emptyCrc = new CRC32 ().getValue ();
    private List filesets = new LinkedList (); // List<FileSet>
    private File manifest;
    private boolean checkPathLocale = true ;
    private boolean warnMissingDir = false ;
    private boolean warnMissingDirSet = false ;

    /** Locale or branding specifier.
     * Represents a complete locale or branding suffix,
     * e.g. <code>ja</code> or <code>ja_JP</code>
     * (but not simply <code>JP</code>).
     * For branding, e.g. <code>f4j</code> or <code>f4j_ce</code>.
     * You must include all relevant suffixes if you expect them to match
     * (but the task will handle a branding suffix combined with a locale
     * suffix, in that order).
     */
    public class LocaleOrB {
        String n;
        /** The suffix. */
        public void setName (String n) {
            this.n = n;
        }
    }

    /** Distinguish particular files that will be considered localizable.
     * This nested <samp>&lt;localekit&gt;</samp> has the same syntax as a FileSet.
     * So typically you will build a JAR from a fileset containing all build
     * products within a given directory; the "locale kit" should be a fileset
     * matching all properties files, for example, and maybe icons which you would
     * expect someone to try to localize--in general, anything which will be referenced
     * in code using a localized lookup (<code>NbBundle</code>).
     * While files with recognized localized (or branded) suffixes automatically go into
     * marked JARs in the <samp>locale/</samp> subdirectory, files considered part of the
     * locale kit also always go into this subdirectory; if they have no suffix, they will
     * be placed in a suffixless locale JAR.
     * So localizers can simply look at this JAR file and brand/localize it as they see fit.
     */
    public void addLocalekit (FileSet fs) {
        localeKits.add (fs);
    }

    /** Add a recognized locale suffix. */
    public LocaleOrB createLocale () {
        LocaleOrB l = new LocaleOrB ();
        locales.add (l);
        return l;
    }

    /** Add a recognized branding suffix. */
    public LocaleOrB createBranding () {
        LocaleOrB l = new LocaleOrB ();
        brandings.add (l);
        return l;
    }

    /** JAR file to create.
     * In fact this is the location of the "base" JAR;
     * locale-specific JARs may be created in the <samp>locale/</samp> subdirectory
     * of the directory containing this JAR, and will be named according to the name
     * of this JAR.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setJarfile (File jarFile) {
        if (! jarFile.getName ().endsWith (".jar")) {
            throw new BuildException ("jarfile attribute must be a file with *.jar extension");
        }
        if (jarFile.getParentFile () == null) {
            throw new BuildException ("jarfile attribute must have a containing directory");
        }
        this.jarFile = jarFile;
    }

    /** Base directory to JAR.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setBasedir (File baseDir) {
        this.baseDir = baseDir;
    }

    /** Turn on or off compression (default off).
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setCompress (String compress) {
        doCompress = Project.toBoolean (compress);
    }

    /** A set of files to JAR up.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void addFileset (FileSet set) {
        filesets.add (set);
    }

    /** Manifest file for the JAR.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setManifest (File manifest) {
	this.manifest = manifest;
    }

    /** By default this is true.  If set to false, then this task will
     * not recognize files below a directory with the same name as a
     * locale as being localized (unless the simple filename also
     * includes the locale).
     */
    public void setCheckPathLocale( boolean doit) {
      checkPathLocale = doit ;
    }

    /** This is false by default, in which case missing dirs in the
     * filesets cause a BuildException to be thrown.  If true, then
     * a warning is printed but the build will continue.
     * This task will also look for a global property 
     * "locjar.warnMissingDir" if this attribute isn't set.
     */
    public void setWarnMissingDir( boolean b) {
      warnMissingDir = b ;
      warnMissingDirSet = true ;
    }

    public void execute () throws BuildException {

        // Sanity checks:
        if (baseDir == null && filesets.size () == 0) {
            throw new BuildException ("basedir attribute must be set, or at least one fileset must be given!");
        }
        if (jarFile == null) {
            throw new BuildException ("You must specify the JAR file to create!");
        }
        if (manifest != null && ! manifest.isFile ()) {
            throw new BuildException ("The specified manifest does not actually exist.");
        }

	// If needed, warn that directories are missing. //
	if( shouldWarnMissingDir() && warnIfMissingDir()) {

	  // Stop if dirs were missing. //
	  return ;
	}

	// Look for global locales or brandings to use. //
	addGlobalLocaleAndBranding() ;

        //System.err.println ("Stage #1");
        // First find out which files need to be archived.
        Map allFiles = new HashMap (); // all files to do something with; Map<String,File> from JAR path to actual file
        // Populate it.
        {
            List scanners = new ArrayList (filesets.size () + 1); // List<FileScanner>
            if (baseDir != null) {
                scanners.add (getDirectoryScanner (baseDir));
            }
            Iterator it = filesets.iterator ();
            while (it.hasNext ()) {
                scanners.add(((FileSet) it.next()).getDirectoryScanner(getProject()));
            }
            it = scanners.iterator ();
            while (it.hasNext ()) {
                FileScanner scanner = (FileScanner) it.next ();
                File thisBaseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                for (int i = 0; i < files.length; i++) {
                    String name = files[i].replace (File.separatorChar, '/');
                    if (name.equalsIgnoreCase ("META-INF/MANIFEST.MF")) {
                        log ("Warning: ignoring META-INF/MANIFEST.MF found among scanned files", Project.MSG_WARN);
                        continue;
                    }
                    allFiles.put (name, new File (thisBaseDir, files[i]));
                }
            }
        }

        //System.err.println ("Stage #2");
        // Now find all files which should always be put into a locale
        // kit (e.g. dir/locale/name.jar, no special locale or
        // branding, but distinguished as localizable/brandable).
        Set localeKitFiles = new HashSet (); // Set<File>; all locale-kit files
        // Populate this one.
        {
            Iterator it = localeKits.iterator ();
            while (it.hasNext ()) {
                FileScanner scanner = ((FileSet) it.next()).getDirectoryScanner(getProject());
                File thisBaseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                for (int i = 0; i < files.length; i++) {
                    localeKitFiles.add (new File (thisBaseDir, files[i]));
                }
            }
        }

        //System.err.println ("Stage #3");
        // Compute list of supported locales and brandings.
        List locales2 = new LinkedList (); // List<String>; all locales
        List brandings2 = new LinkedList (); // List<String>; all brandings
        // Initialize above two.
        {
            Iterator it = locales.iterator ();
            while (it.hasNext ()) {
                locales2.add (((LocaleOrB) it.next ()).n);
            }
            it = brandings.iterator ();
            while (it.hasNext ()) {
                brandings2.add (((LocaleOrB) it.next ()).n);
            }
            class InverseLengthComparator implements Comparator {
                public int compare (Object o1, Object o2) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return s2.length () - s1.length ();
                }
            }
            Comparator c = new InverseLengthComparator ();
            Collections.sort (locales2, c);
            Collections.sort (brandings2, c);
        }

        //System.err.println ("Stage #4");
        // Analyze where everything goes.
        Set jars = new HashSet (); // Set<File>; JAR files to build
        Map localeMarks = new HashMap (); // Map<File,String>; JAR files to locale (or null for basic JAR, "-" for blank)
        Map brandingMarks = new HashMap (); // Map<File,String>; JAR files to branding (or null for basic JAR, "-" for blank)
        Map router = new HashMap (); // Map<File,Map<String,File>>; JAR files to map of JAR path to actual file (file may be null for dirs)
        {
	    String localeDir ;
            Iterator it = allFiles.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry entry = (Map.Entry) it.next ();
                String path = (String) entry.getKey ();

//log( "==> Examining file: " + path) ;

                File file = (File) entry.getValue ();
                // First see if it matches a known branding, locale, or pair of one of each.
                String testpath = path;
                int idx = testpath.lastIndexOf ('/');
                if (idx != -1) testpath = testpath.substring (idx + 1);
                idx = testpath.lastIndexOf ('.');
                if (idx != -1) testpath = testpath.substring (0, idx);
                String thisLocale = null;
                Iterator it2 = locales2.iterator ();
                while (it2.hasNext ()) {
                    String tryLocale = (String) it2.next ();
                    if (testpath.endsWith ("_" + tryLocale)) {
                        thisLocale = tryLocale;
                        testpath = testpath.substring (0, testpath.length () - 1 - tryLocale.length ());
                        break;
                    }
                }
                String thisBranding = null;
                it2 = brandings2.iterator ();
                while (it2.hasNext ()) {
                    String tryBranding = (String) it2.next ();
                    if (testpath.endsWith ("_" + tryBranding)) {
                        thisBranding = tryBranding;
                        break;
                    }
                }
                File thisjar; // JAR to send this file to

		// Check if this file has a parent directory with the //
		// same name as one of the locales.		      //
		localeDir = checkInLocaleDir( file, locales2) ;
		if( localeDir != null) {
		  thisLocale = localeDir ;
		}


/*
if( thisLocale != null) {
  log( "    Locale: " + thisLocale) ;
}
if( thisBranding != null) {
  log( "    Branding: " + thisBranding) ;
}
if( localeKitFiles.contains( file)) {
  log( "    Localizable file.") ;
} */

                if (thisLocale != null || thisBranding != null || localeKitFiles.contains (file)) {
                    String name = jarFile.getName ();
                    // We know jarFile is a *.jar so this is safe:
                    name = name.substring (0, name.length () - 4);
                    if (thisBranding != null) {
                        name += '_' + thisBranding;
                    }
                    if (thisLocale != null) {
                        name += '_' + thisLocale;
                    }
                    name += ".jar";
                    thisjar = new File (new File (jarFile.getParentFile (), "locale"), name);
                    localeMarks.put (thisjar, ((thisLocale != null) ? thisLocale : "-"));
                    brandingMarks.put (thisjar, ((thisBranding != null) ? thisBranding : "-"));
                } else {
                    thisjar = jarFile;
                    localeMarks.put (thisjar, null);
                    brandingMarks.put (thisjar, null);
                }
                jars.add (thisjar);
                Map files = (Map) router.get (thisjar);
                if (files == null) {
                    files = new TreeMap ();
                    router.put (thisjar, files);
                }
                files.put (path, file);
            }
        }

        //System.err.println ("Stage #5");
        // Go through JARs one by one, and build them (if necessary).
        {
            List jars2 = new ArrayList (jars);
            class FileNameComparator implements Comparator {
                public int compare (Object o1, Object o2) {
                    File f1 = (File) o1;
                    File f2 = (File) o2;
                    return f1.toString ().compareTo (f2.toString ());
                }
            }
            Comparator c = new FileNameComparator ();
            Collections.sort (jars2, c);
            Iterator it = jars2.iterator ();
            while (it.hasNext ()) {
                File jar = (File) it.next ();
                Map files = (Map) router.get (jar); // Map<String,File>
                if (jar.exists ()) {
                    // Do an up-to-date check first.
                    long time = jar.lastModified ();
                    if (manifest == null || manifest.lastModified () <= time) {
                        boolean upToDate = true;
                        Iterator it2 = files.values ().iterator ();
                        while (it2.hasNext ()) {
                            File f = (File) it2.next ();
                            if (f.lastModified () > time) {
                                upToDate = false;
                                break;
                            }
                        }
                        if (upToDate) {
                            // Skip this JAR.
                            continue;
                        }
                    }
                }
                log ("Building localized jar: " + jar);
                IOException closing = null;
                try {
                    jar.getParentFile ().mkdirs ();
                    ZipOutputStream out = new ZipOutputStream (new FileOutputStream (jar));
                    try {
                        out.setMethod (doCompress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
                        String localeMark = (String) localeMarks.get (jar);
                        String brandingMark = (String) brandingMarks.get (jar);
                        Set addedDirs = new HashSet (); // Set<String>
                        // Add the manifest.
                        InputStream is;
                        long time;
                        if (manifest != null && localeMark == null && brandingMark == null) {
                            // Master JAR, and it has a manifest.
                            is = new FileInputStream (manifest);
                            time = manifest.lastModified ();
                        } else {
                            // Some subsidiary JAR.
                            is = MatchingTask.class.getResourceAsStream ("/org/apache/tools/ant/defaultManifest.mf");
                            time = System.currentTimeMillis ();
                        }
                        java.util.jar.Manifest mani;
                        try {
                            mani = new java.util.jar.Manifest (is);
                        } finally {
                            is.close ();
                        }
                        Attributes attr = mani.getMainAttributes ();
                        if (! attr.containsKey (Attributes.Name.MANIFEST_VERSION)) {
                            attr.put (Attributes.Name.MANIFEST_VERSION, "1.0");
                        }
                        if (localeMark != null) {
                            attr.putValue ("X-Informational-Archive-Locale", localeMark);
                        }
                        if (brandingMark != null) {
                            attr.putValue ("X-Informational-Archive-Branding", brandingMark);
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                        mani.write (baos);
                        byte[] bytes = baos.toByteArray ();
                        addToJar (new ByteArrayInputStream (bytes), new ByteArrayInputStream (bytes),
                                  out, "META-INF/MANIFEST.MF", time, addedDirs);
                        // Now regular files.
                        Iterator it2 = files.entrySet ().iterator ();
                        while (it2.hasNext ()) {
                            Map.Entry entry = (Map.Entry) it2.next ();
                            String path = (String) entry.getKey ();
                            File file = (File) entry.getValue ();
                            addToJar (new FileInputStream (file), new FileInputStream (file),
                                      out, path, file.lastModified (), addedDirs);
                        }

			// If desired, write the root of the srcDir to a file. //
			writeSrcDir() ;
                    } finally {
                        try {
                            out.close ();
                        } catch (IOException ex) {
                            closing = ex;
                        }
                    }

                    if (closing != null) {
                        // if there was a closing exception and no other one
                        throw closing;
                    }
                } catch (IOException ioe) {
                    String msg = "Problem creating JAR: " + ioe.getMessage ();
                    if (! jar.delete ()) {
                        msg += " (and the JAR is probably corrupt but I could not delete it)";
                    }
                    throw new BuildException(msg, ioe, getLocation());
                }
            }
        }

    } // end execute()

    private void addToJar (InputStream in1, InputStream in2, ZipOutputStream out,
                           String path, long lastModified, Set addedDirs) throws IOException {
        try {
            if (path.endsWith ("/")) {
                throw new IOException ("Bad path: " + path);
            }
            // Add parent dirs as needed:
            int pos = -1;
            while ((pos = path.indexOf ('/', pos + 1)) != -1) {
                String dir = path.substring (0, pos + 1);
                if (! addedDirs.contains (dir)) {
                    addedDirs.add (dir);
                    ZipEntry ze = new ZipEntry (dir);
                    ze.setSize (0);
                    ze.setMethod (ZipEntry.STORED);
                    ze.setCrc (emptyCrc);
                    ze.setTime (lastModified);
                    out.putNextEntry (ze);
                }
            }
            // Add the file itself:
            ZipEntry ze = new ZipEntry (path);
            ze.setMethod (doCompress ? ZipEntry.DEFLATED : ZipEntry.STORED);
            ze.setTime (lastModified);
            long size = 0;
            CRC32 crc = new CRC32 ();
            byte[] buf = new byte[4096];
            int read;
            while ((read = in1.read (buf)) != -1) {
                crc.update (buf, 0, read);
                size += read;
            }
            in1.close ();
            ze.setCrc (crc.getValue ());
            ze.setSize (size);
            out.putNextEntry (ze);
            while ((read = in2.read (buf)) != -1) {
                out.write (buf, 0, read);
            }
        } finally {
            in2.close ();
            in1.close ();
        }
    } // end addToJar()


  // If the name of any parent directory of this file is the same as //
  // one of the locales, return the locale.			     //
  protected String checkInLocaleDir( File file,
				     List locales) {

    // See if this functionality is disabled. //
    if( !checkPathLocale) {
      return null ;
    }

    int idx ;
    String loc, locale_dir, ret = null ;
    String path = file.getPath() ;
    Iterator iter = locales.iterator() ;

    // For each locale. //
    while( iter.hasNext()) {
      loc = (String) iter.next() ;

      // If the path contains a dir with the same name as the //
      // locale.					      //
      locale_dir = new String( file.separator) ;
      locale_dir += loc ;
      locale_dir += file.separator ;
      idx = path.indexOf( locale_dir) ;
      if( idx != -1) {

	// Stop and return this locale. //
	ret = loc ;
	break ;
      }
    }

    return( ret) ;
  }

  ////////////////////////////////////////////////////////////////////
  // This section of code supports the feature that this class will //
  // look for global properties that specify locales and brandings  //
  // that should be used.					    //
  protected void addGlobalLocaleAndBranding() {
    addGlobals( getGlobalLocaleVarName(), locales) ;
    addGlobals( getOldGlobalLocaleVarName(), locales) ;
    addGlobals( getGlobalBrandingVarName(), brandings) ;
    addGlobals( getOldGlobalBrandingVarName(), brandings) ;
  }

  protected String getGlobalLocaleVarName() {
    return( new String( "locjar.locales")) ;
  }

  protected String getGlobalBrandingVarName() {
    return( new String( "locjar.brands")) ;
  }

  // For backwards compatibility. //
  protected String getOldGlobalLocaleVarName() {
    return( new String( "locjar_global_locales")) ;
  }

  // For backwards compatibility. //
  protected String getOldGlobalBrandingVarName() {
    return( new String( "locjar_global_brands")) ;
  }

  protected void addGlobals( String var_name,
			     List list) {
    String prop = null ;
    StringTokenizer tokenizer = null ;
    String tok = null ;
    LocaleOrB lorb = null ;

    // Foreach string in the global list. //
    prop = getProject().getProperty( var_name) ;
    if( prop != null && !prop.equals( "")) {
      tokenizer = new StringTokenizer( prop, ", ") ;
      while( tokenizer.hasMoreTokens()) {
	tok = tokenizer.nextToken() ;

	// Add a new entry in the given list. //
	lorb = new LocaleOrB() ;
	lorb.setName( tok) ;
	list.add( lorb) ;
      }
    }
  }
  //////////////////////////////////////////////////////////////////////

  protected boolean shouldWarnMissingDir() {
    String s ;
    boolean ret = false ;	// Default false. //

    // If the attribute is set, use its value. //
    if( warnMissingDirSet) {
      ret = warnMissingDir ;
    }

    // Otherwise use the global property value, if set. //
    else {
      s = getProject().getProperty("locjar.warnMissingDir");
      if( s != null && !s.trim().equals( "")) {
	ret = getProject().toBoolean(s);
      }
    }

    return( ret) ;
  }

  // If any dir's don't exist, warn the user and return true. //
  protected boolean warnIfMissingDir() {
    ListIterator iter ;
    FileSet fileset ;
    File dir ;
    boolean ret = false ;

    // Print warning if the basedir doesn't exist. //
    if( baseDir != null && !baseDir.exists()) {
      ret = true ;
      printMissingDirWarning( baseDir) ;
    }

    // For each fileset. //
    iter = filesets.listIterator() ;
    if( iter != null) {
      while( iter.hasNext()) {

	// Print warning if the dir doesn't exist. //
	fileset = (FileSet) iter.next() ;
	dir = fileset.getDir(getProject());
	if( dir != null && !dir.exists()) {
	  ret = true ;
	  printMissingDirWarning( dir) ;
	}
      }
    }
    return( ret) ;
  }

  // Warn the user that the given dir doesn't exist. //
  protected void printMissingDirWarning( File dir) {
    log( "WARNING: Skipping this task: Directory " + dir.getPath() + " doesn't exist.") ;
  }

  protected boolean shouldWriteSrcDir() {
    boolean ret = false ;
    String s = getProject().getProperty("locjar.writeSrcDir");
    if( s != null && getProject().toBoolean(s)) {
      ret = true ;
    }
    return( ret) ;
  }

  protected void writeSrcDir() {
    String name ;
    int idx, fromIdx ;
    OutputStreamWriter osw ;
    FileOutputStream fos ;
    File file ;

    if( shouldWriteSrcDir() && jarFile != null && baseDir != null) {
      name = jarFile.getPath() ;
      fromIdx = getNetbeansStartIdx() ;
      idx = name.indexOf( File.separator+"netbeans"+File.separator, fromIdx) ;
      if( idx != -1) {
	try {
	  file = new File( name.substring( 0, idx) + File.separator + "srcdir.properties") ;
	  fos = new FileOutputStream( file) ;
	  osw = new OutputStreamWriter( fos) ;
	  osw.write( "srcdir=" + baseDir + "\n") ;
	  osw.close() ;
	  fos.close() ;
	}
	catch( Exception e) {
	  System.out.println( "ERROR: " + e.getMessage()) ;
	  e.printStackTrace() ;
	  throw new BuildException() ;
	}
      }
      else {
	throw new BuildException( "ERROR: Couldn't find netbeans dir to write srcdir.properties to.") ;
      }
    }
  }

  // Return the index to start searching from to find the "netbeans"
  // directory into which the "srcdir.properties" file will be
  // written.
  protected int getNetbeansStartIdx() {
    int startIdx = 0 ;
    int idx ;

    idx = baseDir.getPath().lastIndexOf( File.separator+
					 "netbeans"+File.separator) ;
    if( idx != -1) {
      startIdx = idx + 1 ;
    }
    return( startIdx) ;
  }
}
