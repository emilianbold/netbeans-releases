/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.net.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.FileSet;

/** Makes a <code>.nbm</code> (<b>N</b>et<b>B</b>eans <b>M</b>odule) file.
 *
 * @author Jesse Glick
 */
public class MakeNBM extends MatchingTask {

    /** The same syntax may be used for either <samp>&lt;license&gt;</samp> or
     * <samp>&lt;description&gt;</samp> subelements.
     * <p>By setting the property <code>makenbm.nocdata</code> to <code>true</code>,
     * you can avoid using XML <code>CDATA</code> (for compatibility with older versions
     * of Auto Update which could not handle it).
     */
    public class Blurb {
        /** You may embed a <samp>&lt;file&gt;</samp> element inside the blurb.
         * If there is text on either side of it, that will be separated
         * with a line of dashes automatically.
         * But use nested <samp>&lt;text&gt;</samp> for this purpose.
         */
	public class FileInsert {
            /** File location. */
	    public void setLocation (File file) throws BuildException {
                log("Including contents of " + file, Project.MSG_VERBOSE);
		long lmod = file.lastModified ();
		if (lmod > mostRecentInput) mostRecentInput = lmod;
		addSeparator ();
		try {
		    InputStream is = new FileInputStream (file);
		    try {
			Reader r = new InputStreamReader (is, "UTF-8"); //NOI18N
			char[] buf = new char[4096];
			int len;
			while ((len = r.read (buf)) != -1)
			    text.append (buf, 0, len);
		    } finally {
			is.close ();
		    }
		} catch (IOException ioe) {
                    throw new BuildException ("Exception reading blurb from " + file, ioe, getLocation ());
		}
	    }
	}
	private StringBuffer text = new StringBuffer ();
	private String name = null;
        /** There may be freeform text inside the element. Prefer to use nested elements. */
	public void addText (String t) {
	    addSeparator ();
	    // Strips indentation. Needed because of common style:
	    // <description>
	    //   Some text here.
	    //   And another line.
	    // </description>
	    t = getProject().replaceProperties(t.trim());
	    int min = Integer.MAX_VALUE;
	    StringTokenizer tok = new StringTokenizer (t, "\n"); //NOI18N
	    boolean first = true;
	    while (tok.hasMoreTokens ()) {
		String line = tok.nextToken ();
		if (first) {
		    first = false;
		} else {
		    int i;
		    for (i = 0;
			 i < line.length () &&
			     Character.isWhitespace (line.charAt (i));
			 i++)
			;
		    if (i < min) min = i;
		}
	    }
	    if (min == 0) {
		text.append (t);
	    } else {
		tok = new StringTokenizer (t, "\n"); //NOI18N
		first = true;
		while (tok.hasMoreTokens ()) {
		    String line = tok.nextToken ();
		    if (first) {
			first = false;
		    } else {
			text.append ('\n'); //NOI18N
			line = line.substring (min);
		    }
		    text.append (line);
		}
	    }
	}
        /** Contents of a file to include. */
	public FileInsert createFile () {
	    return new FileInsert ();
	}
        /** Text to include literally. */
        public class Text {
            public void addText(String t) {
                Blurb.this.addText(t);
            }
        }
        // At least on Ant 1.3, mixed content does not work: all the text is added
        // first, then all the file inserts. Need to use subelements to be sure.
        /** Include nested literal text. */
        public Text createText() {
            return new Text();
        }
	private void addSeparator () {
	    if (text.length () > 0) {
		// some sort of separator
		if (text.charAt (text.length () - 1) != '\n') //NOI18N
		    text.append ('\n'); //NOI18N
		text.append ("-----------------------------------------------------\n"); //NOI18N
	    }
	}
	public String getText () {
            String nocdata = getProject().getProperty("makenbm.nocdata"); //NOI18N
            if (nocdata != null && Project.toBoolean(nocdata)) {
                return xmlEscape(text.toString());
            } else {
                return "<![CDATA[" + text.toString () + "]]>"; //NOI18N
            }
	}
        /** You can either set a name for the blurb, or using the <code>file</code> attribute does this.
         * The name is mandatory for licenses, as this identifies the license in
         * an update description.
         */
	public void setName (String name) {
	    this.name = name;
	}
	public String getName () {
	    return name;
	}
        /** Include a file (and set the license name according to its basename). */
	public void setFile (File file) {
	    // This actually adds the text and so on:
	    new FileInsert ().setLocation (file);
	    // Default for the name too, as a convenience.
	    if (name == null) name = file.getName ();
	}
    }

    public class ExternalPackage {
	String name = null;
	String targetName = null;
	String startUrl = null;
	String description = null;

	public void setName(String n) {
	    this.name = n;
	}

	public void setTargetName(String t) {
	    this.targetName = t;
	}

	public void setStartURL(String u) {
	    this.startUrl = u;
	}
	
	public void setDescription(String d) {
	    this.description = d;
	}

    }

    // Similar to org.openide.xml.XMLUtil methods.
    private static String xmlEscape(String s) {
        int max = s.length();
        StringBuffer s2 = new StringBuffer((int)(max * 1.1 + 1));
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': //NOI18N
                    s2.append("&lt;"); //NOI18N
                    break;
                case '>': //NOI18N
                    s2.append("&gt;"); //NOI18N
                    break;
                case '&': //NOI18N
                    s2.append("&amp;"); //NOI18N
                    break;
                case '"': //NOI18N
                    s2.append("&quot;"); //NOI18N
                    break;
                default:
                    s2.append(c);
                    break;
            }
        }
        return s2.toString();
    }

    /** <samp>&lt;signature&gt;</samp> subelement for signing the NBM. */
    public /*static*/ class Signature {
	public File keystore;
	public String storepass, alias;
        /** Path to the keystore (private key). */
	public void setKeystore (File f) {
	    keystore = f;
	}
        /** Password for the keystore.
         * If a question mark (<samp>?</samp>), the NBM will not be signed
         * and a warning will be printed.
         */
	public void setStorepass (String s) {
	    storepass = s;
	}
        /** Alias for the private key. */
	public void setAlias (String s) {
	    alias = s;
	}
    }

    private File file = null;
    private File topdir = null;
    private File manifest = null;
    /** see #13850 for explanation */
    private File module = null;
    private String homepage = null;
    private String distribution = null;
    private String needsrestart = null;
    private Blurb license = null;
    private Blurb description = null;
    private Blurb notification = null;    
    private Signature signature = null;
    private long mostRecentInput = 0L;
    private boolean isStandardInclude = true;
    private Vector externalPackages = null;

    /** Include netbeans directory - default is true */
    public void setIsStandardInclude(boolean isStandardInclude) {
	this.isStandardInclude = isStandardInclude;
    }

    /** Name of resulting NBM file. */
    public void setFile (File file) {
	this.file = file;
    }
    /** Top directory.
     * Expected to contain a subdirectory <samp>netbeans/</samp> with the
     * desired contents of the NBM.
     * Will create <samp>Info/info.xml</samp> with metadata.
     */
    public void setTopdir (File topdir) {
	this.topdir = topdir;
    }
    /** Module manifest needed for versioning.
     * @deprecated Use {@link #setModule} instead.
     */
    public void setManifest (File manifest) {
	this.manifest = manifest;
	long lmod = manifest.lastModified ();
	if (lmod > mostRecentInput) mostRecentInput = lmod;
        log(getLocation() + "The 'manifest' attr on <makenbm> is deprecated, please use 'module' instead", Project.MSG_WARN);
    }
    /** Module JAR needed for generating the info file.
     * Information may be gotten either from its manifest,
     * or if it declares OpenIDE-Module-Localizing-Bundle in its
     * manifest, from that bundle.
     * The base locale variant, if any, is also checked if necessary
     * for the named bundle.
     * Currently no other locale variants of the module are examined;
     * the information is available but there is no published specification
     * of what the resulting variant NBMs (or variant information within
     * the NBM) should look like.
     */
    public void setModule(File module) {
        this.module = module;
        // mostRecentInput updated below...
    }
    /** URL to a home page describing the module. */
    public void setHomepage (String homepage) {
	this.homepage = homepage;
    }
    /** Does module need IDE restart to be installed? */
    public void setNeedsrestart (String needsrestart) {
        this.needsrestart = needsrestart;
    }
    /** URL where this NBM file is expected to be downloadable from. */
    public void setDistribution (String distribution) throws BuildException {
        if (distribution.startsWith("http://")) { //NOI18N
            this.distribution = distribution;
        } else if (!(distribution.equals(""))) {
            // workaround for typical bug in build script
            this.distribution = "http://" + distribution; //NOI18N
        } else {
            throw new BuildException("Distribution URL is empty, check build.xml file", location);
        }
        // check the URL
        try {
            URI uri = java.net.URI.create(this.distribution);
        } catch (IllegalArgumentException ile) {
            throw new BuildException ("Distribution URL \""+this.distribution+"\" is not a valid URI", ile, location);
        }
    }
    public Blurb createLicense () {
	return (license = new Blurb ());
    }
    public Blurb createNotification () {
	return (notification = new Blurb ());
    }    
    public Blurb createDescription () {
        log(getLocation() + "The <description> subelement in <makenbm> is deprecated except for emergency patches, please ensure your module has an OpenIDE-Module-Long-Description instead", Project.MSG_WARN);
	return (description = new Blurb ());
    }
    public Signature createSignature () {
	return (signature = new Signature ());
    }

    public ExternalPackage createExternalPackage(){
	ExternalPackage externalPackage = new ExternalPackage ();
	if (externalPackages == null)
	    externalPackages = new Vector();
	externalPackages.add( externalPackage );
	return externalPackage;
    }

    public void execute () throws BuildException {
	if (file == null)
	    throw new BuildException ("must set file for makenbm", location);
        if (manifest == null && module == null)
            throw new BuildException ("must set module for makenbm", location);
        if (manifest != null && module != null)
            throw new BuildException("cannot set both manifest and module for makenbm", location);

    File file;
    String rootDir = getProject ().getProperty ("nbm.target.dir");
    if (rootDir != null) {
        file = new File (rootDir, this.file.getName ());
    } else {
        file = this.file;
    }

	// If desired, override the license and/or URL. //
        overrideURLIfNeeded() ;
	overrideLicenseIfNeeded() ;

	// Will create a file Info/info.xml to be stored alongside netbeans/ contents.
	File infodir = new File (topdir, "Info"); //NOI18N
	infodir.mkdirs ();
	File infofile = new File (infodir, "info.xml"); //NOI18N
	if (infofile.exists ()) {
            infofile.delete();
        }
        Attributes attr = null;
        if (module != null) {
            // The normal case; read attributes from its manifest and maybe bundle.
            long mMod = module.lastModified();
            if (mostRecentInput < mMod) mostRecentInput = mMod;
            try {
                JarFile modulejar = new JarFile(module);
                try {
                    attr = modulejar.getManifest().getMainAttributes();
                    String bundlename = attr.getValue("OpenIDE-Module-Localizing-Bundle"); //NOI18N
                    if (bundlename != null) {
                        Properties p = new Properties();
                        ZipEntry bundleentry = modulejar.getEntry(bundlename);
                        if (bundleentry != null) {
                            InputStream is = modulejar.getInputStream(bundleentry);
                            try {
                                p.load(is);
                            } finally {
                                is.close();
                            }
                        } else {
                            // Not found in main JAR, check locale variant JAR.
                            File variant = new File(new File(module.getParentFile(), "locale"), module.getName()); //NOI18N
                            if (! variant.isFile()) throw new BuildException(bundlename + " not found in " + module, location); //NOI18N
                            long vmMod = variant.lastModified();
                            if (mostRecentInput < vmMod) mostRecentInput = vmMod;
                            ZipFile variantjar = new ZipFile(variant);
                            try {
                                bundleentry = variantjar.getEntry(bundlename);
                                if (bundleentry == null) throw new BuildException(bundlename + " not found in " + module + " nor in " + variant, location);
                                InputStream is = variantjar.getInputStream(bundleentry);
                                try {
                                    p.load(is);
                                } finally {
                                    is.close();
                                }
                            } finally {
                                variantjar.close();
                            }
                        }
                        // Now pick up attributes from the bundle.
                        Iterator it = p.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry)it.next();
                            String name = (String)entry.getKey();
                            if (! name.startsWith("OpenIDE-Module-")) continue; //NOI18N
                            attr.putValue(name, (String)entry.getValue());
                        }
                    } // else all loc attrs in main manifest, OK
                } finally {
                    modulejar.close();
                }
            } catch (IOException ioe) {
                throw new BuildException("exception while reading " + module, ioe, location);
            }
        } // else we will read attr later if info file is out of date
	boolean skipInfo = false;
	if (infofile.exists ()) {
	    // Check for up-to-date w.r.t. manifest and maybe license file.
	    long iMod = infofile.lastModified ();
	    if (mostRecentInput < iMod)
		skipInfo = true;
	}
	if (! skipInfo) {
	    log ("Creating NBM info file " + infofile);
            if (manifest != null) {
                // Read module manifest for main attributes.
                try {
                    InputStream manifestStream = new FileInputStream (manifest);
                    try {
                        attr = new Manifest (manifestStream).getMainAttributes ();
                    } finally {
                        manifestStream.close ();
                    }
                } catch (IOException e) {
                    throw new BuildException ("exception when reading manifest " + manifest, e, location);
                }
            } // else we read attr before
	    try {
		OutputStream infoStream = new FileOutputStream (infofile);
		try {
                    PrintWriter ps = new PrintWriter(new OutputStreamWriter(infoStream, "UTF-8")); //NOI18N
		    // Begin writing XML.
                    ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //NOI18N
                    ps.println("<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Autoupdate Module Info 2.0//EN\" \"http://www.netbeans.org/dtds/autoupdate-info-2_0.dtd\">"); //NOI18N
		    String codenamebase = attr.getValue ("OpenIDE-Module"); //NOI18N
		    if (codenamebase == null)
			throw new BuildException ("invalid manifest, does not contain OpenIDE-Module", location);
		    // Strip major release number if any.
		    int idx = codenamebase.lastIndexOf ('/'); //NOI18N
		    if (idx != -1) codenamebase = codenamebase.substring (0, idx);
		    ps.println ("<module codenamebase=\"" + xmlEscape(codenamebase) + "\""); //NOI18N
		    if (homepage != null)
                        ps.println ("        homepage=\"" + xmlEscape(homepage) + "\""); //NOI18N
		    if (distribution != null) {
                        ps.println ("        distribution=\"" + xmlEscape(distribution) + "\""); //NOI18N
                    } else {
                        throw new BuildException ("NBM distribution URL is not set", location);
                    }
		    // Here we only write a name for the license.
		    if (license != null) {
			String name = license.getName ();
			if (name == null)
			    throw new BuildException ("Every license must have a name or file attribute", location);
                        ps.println ("        license=\"" + xmlEscape(name) + "\""); //NOI18N
		    }
		    ps.println ("        downloadsize=\"0\""); //NOI18N
                    if (needsrestart != null)
                        ps.println ("        needsrestart=\"" + xmlEscape(needsrestart) + "\""); //NOI18N
		    ps.println (">"); //NOI18N
		    if (description != null) {
			ps.print ("  <description>"); //NOI18N
			ps.print (description.getText ());
			ps.println ("</description>"); //NOI18N
                    }
                    if (notification != null) {
                        ps.print("  <module_notification>"); //NOI18N
                        ps.print(notification.getText());
                        ps.println("</module_notification>"); //NOI18N
		    }
		    if (externalPackages != null) {
			Enumeration exp = externalPackages.elements();
			while (exp.hasMoreElements()) {
			    ExternalPackage externalPackage = (ExternalPackage) exp.nextElement();
			    if (externalPackage.name == null || 
				externalPackage.targetName == null ||
				externalPackage.startUrl == null)
				throw new BuildException("Must define name, targetname, starturl for external package");
			    ps.print("  <external_package "); //NOI18N
			    ps.print("name=\""+xmlEscape(externalPackage.name)+"\" "); //NOI18N
			    ps.print("target_name=\""+xmlEscape(externalPackage.targetName)+"\" "); //NOI18N
			    ps.print("start_url=\""+xmlEscape(externalPackage.startUrl)+"\""); //NOI18N
			    if (externalPackage.description != null)
				ps.print(" description=\""+xmlEscape(externalPackage.description)+"\""); //NOI18N
			    ps.println("/>"); //NOI18N
			}
		    }
		    // Write manifest attributes.
		    ps.print ("  <manifest "); //NOI18N
		    boolean firstline = true;
                    List attrNames = new ArrayList(attr.size()); // List<String>
                    Iterator it = attr.keySet().iterator();
                    while (it.hasNext()) {
                        attrNames.add(((Attributes.Name)it.next()).toString());
                    }
                    Collections.sort(attrNames);
                    it = attrNames.iterator();
                    while (it.hasNext()) {
                        String name = (String)it.next();
                        // Ignore irrelevant attributes (cf. www/www/dtds/autoupdate-catalog-2_0.dtd
                        //  and www/www/dtds/autoupdate-info-2_0.dtd):
                        if (! name.startsWith("OpenIDE-Module")) continue; //NOI18N
                        if (name.equals("OpenIDE-Module-Localizing-Bundle")) continue; //NOI18N
                        if (name.equals("OpenIDE-Module-Install")) continue; //NOI18N
                        if (name.equals("OpenIDE-Module-Layer")) continue; //NOI18N
                        if (name.equals("OpenIDE-Module-Description")) continue; //NOI18N
                        if (name.equals("OpenIDE-Module-Package-Dependency-Message")) continue; //NOI18N
                        if (name.equals("OpenIDE-Module-Public-Packages")) continue; //NOI18N
			if (firstline)
			    firstline = false;
			else
			    ps.print ("            ");
                        ps.println(name + "=\"" + xmlEscape(attr.getValue(name)) + "\""); //NOI18N
		    }
		    ps.println ("  />"); //NOI18N
		    // Maybe write out license text.
		    if (license != null) {
                        ps.print ("  <license name=\"" + xmlEscape(license.getName ()) + "\">"); //NOI18N
			ps.print (license.getText ());
			ps.println ("</license>"); //NOI18N
		    }
		    ps.println ("</module>"); //NOI18N
                    ps.flush();
		} finally {
		    infoStream.close ();
		}
	    } catch (IOException e) {
		throw new BuildException ("exception when creating Info/info.xml", e, location);
	    }
	}
	// JAR it all up together.
	long jarModified = file.lastModified (); // may be 0
	//log ("Ensuring existence of NBM file " + file);
	Jar jar = (Jar) project.createTask ("jar"); //NOI18N
    
	jar.setJarfile (file);
	//jar.setBasedir (topdir.getAbsolutePath ());
        jar.setCompress(true);
	//jar.createInclude ().setName ("netbeans/"); //NOI18N
	//jar.createInclude ().setName ("Info/info.xml"); //NOI18N
        jar.addFileset (getFileSet());
	jar.setLocation (location);
	jar.init ();
	jar.execute ();

	// Print messages if we overrode anything. //
	if( file.lastModified () != jarModified) {
	  if( overrideLicense()) {
	    log( "Overriding license with: " + getLicenseOverride()) ;
	  }
	  if( overrideURL()) {
	    log( "Overriding homepage URL with: " + getURLOverride()) ;
	  }
	}

	// Maybe sign it.
	if (signature != null && file.lastModified () != jarModified) {
	    if (signature.keystore == null)
		throw new BuildException ("must define keystore attribute on <signature/>");
	    if (signature.storepass == null)
		throw new BuildException ("must define storepass attribute on <signature/>");
	    if (signature.alias == null)
		throw new BuildException ("must define alias attribute on <signature/>");
            if (signature.storepass.equals ("?") || !signature.keystore.exists()) {
                log ("Not signing NBM file " + file + "; no stored-key password provided or keystore (" 
		     + signature.keystore.toString() + ") doesn't exist", Project.MSG_WARN);
            } else {
                log ("Signing NBM file " + file);
                SignJar signjar = (SignJar) project.createTask ("signjar"); //NOI18N
                //I have to use Reflection API, because there was changed API in ANT1.5
                try {
                    try {
                        Class[] paramsT = {String.class};
                        Object[] paramsV1 = {signature.keystore.getAbsolutePath()};
                        Object[] paramsV2 = {file.getAbsolutePath()};
                        signjar.getClass().getDeclaredMethod( "setKeystore", paramsT ).invoke( signjar, paramsV1 ); //NOI18N
                        signjar.getClass().getDeclaredMethod( "setJar", paramsT ).invoke( signjar, paramsV2 ); //NOI18N
                    } catch (NoSuchMethodException ex1) {
                        //Probably ANT 1.5
                        try {
                            Class[] paramsT = {File.class};
                            Object[] paramsV1 = {signature.keystore};
                            Object[] paramsV2 = {file};
                            signjar.getClass().getDeclaredMethod( "setKeystore", paramsT ).invoke( signjar, paramsV1 ); //NOI18N
                            signjar.getClass().getDeclaredMethod( "setJar", paramsT ).invoke( signjar, paramsV2 ); //NOI18N
                        }
                        catch (NoSuchMethodException ex2) {
			    //Probably ANT1.5.2
			    try {
				Class[] paramsT1 = {File.class};
				Class[] paramsT2 = {String.class};
				Object[] paramsV1 = {signature.keystore.getAbsolutePath()};
				Object[] paramsV2 = {file};
				signjar.getClass().getDeclaredMethod( "setKeystore", paramsT2 ).invoke( signjar, paramsV1 ); //NOI18N
				signjar.getClass().getDeclaredMethod( "setJar", paramsT1 ).invoke( signjar, paramsV2 ); //NOI18N
			    }
			    catch (NoSuchMethodException ex3) {
				
				throw new BuildException("Unknown ANT version, only ANT 1.4.1 is currently supported and ANT 1.4.1+ is acceptable.");
			    }
                        }
                    }
                } catch (IllegalAccessException ex3) {
                    throw new BuildException(ex3);
                } catch (java.lang.reflect.InvocationTargetException ex4) {
                    throw new BuildException(ex4);
                }
                signjar.setStorepass (signature.storepass);
                signjar.setAlias (signature.alias);
                signjar.setLocation (location);
                signjar.init ();
                signjar.execute ();
            }
	}
    }
   
    // Reflection access from MakeListOfNBM:
    
    FileSet getFileSet() {
        FileSet fs = fileset;		//makes in apperance to excludes and includes files defined in XML
        fs.setDir (topdir);

	if (isStandardInclude) {
	    fs.createInclude ().setName ("netbeans/"); //NOI18N
	    fs.createExclude ().setName ("netbeans/update_tracking/*.xml"); //NOI18N
        }

	fs.createInclude ().setName ("Info/info.xml"); //NOI18N
        return fs;
    }

    Attributes getAttributes() throws IOException {
        if (manifest != null) {
            InputStream is = new FileInputStream(manifest);
            try {
                return new Manifest(is).getMainAttributes();
            } finally {
                is.close();
            }
        } else if (module != null) {
            JarFile jar = new JarFile(module);
            try {
                return jar.getManifest().getMainAttributes();
            } finally {
                jar.close();
            }
        } else {
            throw new IOException(location + "must give either 'manifest' or 'module' on <makenbm>");
        }
    }

  /** This returns true if the license should be overridden. */
  protected boolean overrideLicense() {
    return( getLicenseOverride() != null) ;
  }

  /** Get the license to use if the license should be overridden,
   *  otherwise return null.
   */
  protected String getLicenseOverride() {
    String s = getProject().getProperty( "makenbm.override.license") ; //NOI18N
    if( s != null) {
      if( s.equals( "")) {
	s = null ;
      }
    }
    return( s) ;
  }

  /** This returns true if the homepage URL should be overridden. */
  protected boolean overrideURL() {
    return( getURLOverride() != null) ;
  }

  /** Get the homepage URL to use if it should be overridden,
   *  otherwise return null.
   */
  protected String getURLOverride() {
    String s = getProject().getProperty( "makenbm.override.url") ; //NOI18N
    if( s != null) {
      if( s.equals( "")) { //NOI18N
	s = null ;
      }
    }
    return( s) ;
  }

  /** If required, this will create a new license using the override
   *  license file.
   */
  protected void overrideLicenseIfNeeded() {
    if( overrideLicense()) {
      license = new Blurb() ;
      license.setFile( new File( getLicenseOverride())) ;
    }
  }

  /** If required, this will set the homepage URL using the
   *  override value.
   */
  protected void overrideURLIfNeeded() {
    if( overrideURL()) {
      homepage = getURLOverride() ;
    }
  }

}
