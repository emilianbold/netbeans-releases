/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.util.jar.*;

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
         */
	public class FileInsert {
            /** File location. */
	    public void setLocation (File file) throws BuildException {
		long lmod = file.lastModified ();
		if (lmod > mostRecentInput) mostRecentInput = lmod;
		addSeparator ();
		try {
		    InputStream is = new FileInputStream (file);
		    try {
			Reader r = new InputStreamReader (is);
			char[] buf = new char[4096];
			int len;
			while ((len = r.read (buf)) != -1)
			    text.append (buf, 0, len);
		    } finally {
			is.close ();
		    }
		} catch (IOException ioe) {
		    throw new BuildException ("Exception reading blurb from " + file, ioe, getLocation1 ());
		}
	    }
	}
	private StringBuffer text = new StringBuffer ();
	private String name = null;
        /** There may be freeform text inside the element. */
	public void addText (String t) {
	    addSeparator ();
	    // Strips indentation. Needed because of common style:
	    // <description>
	    //   Some text here.
	    //   And another line.
	    // </description>
	    t = t.trim ();
	    int min = Integer.MAX_VALUE;
	    StringTokenizer tok = new StringTokenizer (t, "\n");
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
		tok = new StringTokenizer (t, "\n");
		first = true;
		while (tok.hasMoreTokens ()) {
		    String line = tok.nextToken ();
		    if (first) {
			first = false;
		    } else {
			text.append ('\n');
			line = line.substring (min);
		    }
		    text.append (line);
		}
	    }
	}
	public FileInsert createFile () {
	    return new FileInsert ();
	}
	private void addSeparator () {
	    if (text.length () > 0) {
		// some sort of separator
		if (text.charAt (text.length () - 1) != '\n')
		    text.append ('\n');
		text.append ("-----------------------------------------------------\n");
	    }
	}
	public String getText () {
            if (Boolean.valueOf (getProject ().getProperty ("makenbm.nocdata")).booleanValue ()) {
                int max = text.length ();
                StringBuffer text2 = new StringBuffer ((int) (max * 1.1 + 1));
                for (int i = 0; i < max; i++) {
                    char c = text.charAt (i);
                    switch (c) {
                    case '<':
                        text2.append ("&lt;");
                        break;
                    case '>':
                        text2.append ("&gt;");
                        break;
                    case '&':
                        text2.append ("&amp;");
                        break;
                    default:
                        text2.append (c);
                        break;
                    }
                }
                return text2.toString ();
            } else {
                return "<![CDATA[" + text.toString () + "]]>";
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
	// Javac 1.2 workaround, maybe:
	private Location getLocation1 () {
	    return getLocation0 ();
	}
    }
    // Javac 1.2 workaround:
    private Location getLocation0 () {
	return location;
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
    private String homepage = null;
    private String distribution = null;
    private Blurb license = null;
    private Blurb description = null;
    private Signature signature = null;
    long mostRecentInput = 0L;

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
    /** Module manifest needed for versioning. */
    public void setManifest (File manifest) {
	this.manifest = manifest;
	long lmod = manifest.lastModified ();
	if (lmod > mostRecentInput) mostRecentInput = lmod;
    }
    /** URL to a home page describing the module. */
    public void setHomepage (String homepage) {
	this.homepage = homepage;
    }
    /** URL where this NBM file is expected to be downloadable from. */
    public void setDistribution (String distribution) {
	this.distribution = distribution;
    }
    public Blurb createLicense () {
	return (license = new Blurb ());
    }
    public Blurb createDescription () {
	return (description = new Blurb ());
    }
    public Signature createSignature () {
	return (signature = new Signature ());
    }
    
    public void execute () throws BuildException {
	if (file == null)
	    throw new BuildException ("must set file for makenbm", location);
	if (manifest == null)
	    throw new BuildException ("must set manifest for makenbm", location);
	// Will create a file Info/info.xml to be stored alongside netbeans/ contents.
	File infodir = new File (topdir, "Info");
	infodir.mkdirs ();
	File infofile = new File (infodir, "info.xml");
	boolean skipInfo = false;
	if (infofile.exists ()) {
	    // Check for up-to-date w.r.t. manifest and maybe license file.
	    long iMod = infofile.lastModified ();
	    if (mostRecentInput < iMod)
		skipInfo = true;
	}
	if (! skipInfo) {
	    log ("Creating NBM info file " + infofile);
	    Attributes attr;
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
	    try {
		OutputStream infoStream = new FileOutputStream (infofile);
		try {
		    PrintStream ps = new PrintStream (infoStream);
		    // Begin writing XML.
		    ps.println ("<?xml version='1.0'?>");
		    ps.println ();
		    String codenamebase = attr.getValue ("OpenIDE-Module");
		    if (codenamebase == null)
			throw new BuildException ("invalid manifest, does not contain OpenIDE-Module", location);
		    // Strip major release number if any.
		    int idx = codenamebase.lastIndexOf ('/');
		    if (idx != -1) codenamebase = codenamebase.substring (0, idx);
		    ps.println ("<module codenamebase=\"" + codenamebase + "\"");
		    if (homepage != null)
			ps.println ("        homepage=\"" + homepage + "\"");
		    if (distribution != null)
			ps.println ("        distribution=\"" + distribution + "\"");
		    // Here we only write a name for the license.
		    if (license != null) {
			String name = license.getName ();
			if (name == null)
			    throw new BuildException ("Every license must have a name or file attribute", location);
			ps.println ("        license=\"" + name + "\"");
		    }
		    ps.println ("        downloadsize=\"0\"");
		    ps.println (">");
		    if (description != null) {
			ps.print ("  <description>");
			ps.print (description.getText ());
			ps.println ("</description>");
                        /* Not needed since Auto Update now reads this attribute directly:
		    } else {
                        // Automatically inherit description from manifest.
                        String longDesc = attr.getValue ("OpenIDE-Module-Long-Description");
                        if (longDesc != null) {
                            ps.print ("  <description>");
                            ps.print (longDesc);
                            ps.println ("</description>");
                        }
                        */
                    }
		    // Write manifest attributes.
		    ps.print ("  <manifest ");
		    boolean firstline = true;
		    Iterator it = attr.entrySet ().iterator ();
		    while (it.hasNext ()) {
			if (firstline)
			    firstline = false;
			else
			    ps.print ("            ");
			Map.Entry entry = (Map.Entry) it.next ();
                        // [PENDING] ought to escape the value for characters in ["&<>]
			ps.println (entry.getKey () + "=\"" + entry.getValue () + "\"");
		    }
		    ps.println ("  />");
		    // Maybe write out license text.
		    if (license != null) {
			ps.print ("  <license name=\"" + license.getName () + "\">");
			ps.print (license.getText ());
			ps.println ("</license>");
		    }
		    ps.println ("</module>");
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
	Jar jar = (Jar) project.createTask ("jar");
	jar.setJarfile (file);
	//jar.setBasedir (topdir.getAbsolutePath ());
//	jar.setCompress ("true");
        try {
            Class[] params = {Boolean.TYPE};
            Object[] objs = {Boolean.TRUE};
            // Trying to invoke setCompess with parameter Boolean - ANT 1.3
            Zip.class.getDeclaredMethod("setCompress",params).invoke(jar,objs);
            // Looks like ANT 1.3 - I hope that is ANT 1.3
        }
        catch (NoSuchMethodException ex)
        {
            // Looks like ANT 1.2 so use setCompress with String
            Class[] params = {String.class};
            Object[] objs = {"true"};
            try {
                Zip.class.getDeclaredMethod("setCompress",params).invoke(jar,objs);
            }
            catch (Exception ex1) {
                throw new BuildException(ex1.fillInStackTrace());
            }
        }
        catch (Exception ex) {
            throw new BuildException(ex.fillInStackTrace());
        }
	//jar.createInclude ().setName ("netbeans/");
	//jar.createInclude ().setName ("Info/info.xml");
        FileSet fs = fileset;		//makes in apperance to excludes and includes files defined in XML
//        fs.setRefid(fileset.ref);
        fs.setDir (topdir);
        fs.createInclude ().setName ("netbeans/");
	fs.createInclude ().setName ("Info/info.xml");
        jar.addFileset (fs);
	jar.setLocation (location);
	jar.init ();
	jar.execute ();
	// Maybe sign it.
	if (signature != null && file.lastModified () != jarModified) {
	    if (signature.keystore == null)
		throw new BuildException ("must define keystore attribute on <signature/>");
	    if (signature.storepass == null)
		throw new BuildException ("must define storepass attribute on <signature/>");
	    if (signature.alias == null)
		throw new BuildException ("must define alias attribute on <signature/>");
            if (signature.storepass.equals ("?")) {
                log ("Not signing NBM file " + file + "; no stored-key password provided", Project.MSG_WARN);
            } else {
                log ("Signing NBM file " + file);
                SignJar signjar = (SignJar) project.createTask ("signjar");
                signjar.setKeystore (signature.keystore.getAbsolutePath ());
                signjar.setStorepass (signature.storepass);
                signjar.setJar (file.getAbsolutePath ());
                signjar.setAlias (signature.alias);
                signjar.setLocation (location);
                signjar.init ();
                signjar.execute ();
            }
	}
    }

}
