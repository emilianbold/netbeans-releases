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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Java;

/** Makes a <code>.nbm</code> (<b>N</b>et<b>B</b>eans <b>M</b>odule) file.
 *
 * @author Jesse Glick
 */
public class MakeNBM extends Task {

    public class Blurb {
	private StringBuffer text = new StringBuffer ();
	private File file = null;
	private String name = null;
	public Blurb () {
	}
	public void addText (String text) {
	    // [PENDING] should also strip initial indentation, probably...
	    // I.e. if every newline is followed by at least n spaces, then
	    // strip n spaces after every newline (but leave any extra spaces,
	    // other than trimming them).
	    // Needed because of common style:
	    // <description>
	    //   Some text here.
	    //   And another line.
	    // </description>
	    this.text.append (text.trim ());
	}
	public String getText () throws BuildException {
	    if (file != null) {
		if (text.length () > 0)
		    text.append ("\n\n"); // some sort of separator
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
		    throw new BuildException ("Exception reading blurb from " + file, ioe, getLocation0 ());
		}
	    }
	    return text.toString ();
	}
	public void setName (String name) {
	    this.name = name;
	}
	public String getName () {
	    return name;
	}
	public void setFile (File file) {
	    this.file = file;
	    long lmod = file.lastModified ();
	    if (lmod > mostRecentInput) mostRecentInput = lmod;
	    name = file.getName ();
	}
    }
    // Javac 1.2 workaround:
    private Location getLocation0 () {
	return location;
    }

    public static class Signature {
	public File keystore;
	public String storepass, alias;
	public void setKeystore (File f) {
	    keystore = f;
	}
	public void setStorepass (String s) {
	    storepass = s;
	}
	public void setAlias (String s) {
	    alias = s;
	}
    }

    private String file = null;
    private String topdir = ".";
    private File manifest = null;
    private String homepage = null;
    private String distribution = null;
    private Blurb license = null;
    private Blurb description = null;
    private Signature signature = null;
    long mostRecentInput = 0L;

    public void setFile (String file) {
	this.file = file;
    }
    public void setTopdir (String topdir) {
	this.topdir = topdir;
    }
    public void setManifest (File manifest) {
	this.manifest = manifest;
	long lmod = manifest.lastModified ();
	if (lmod > mostRecentInput) mostRecentInput = lmod;
    }
    public void setHomepage (String homepage) {
	this.homepage = homepage;
    }
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
	File infodir = project.resolveFile (topdir + "/Info");
	infodir.mkdirs ();
	File infofile = new File (infodir, "info.xml");
	if (infofile.exists ()) {
	    // Check for up-to-date w.r.t. manifest and maybe license file.
	    long iMod = infofile.lastModified ();
	    if (mostRecentInput < iMod)
		return;
	}
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
	// JAR it all up together.
	Jar jar = (Jar) project.createTask ("jar");
	jar.setJarfile (file);
	jar.setBasedir (topdir);
	jar.setCompress ("true");
	jar.createInclude ().setName ("netbeans/");
	jar.createInclude ().setName ("Info/info.xml");
	jar.setLocation (location);
	jar.init ();
	jar.execute ();
	// Maybe sign it.
	if (signature != null) {
	    if (signature.keystore == null)
		throw new BuildException ("must define keystore attribute on <signature/>");
	    if (signature.storepass == null)
		throw new BuildException ("must define storepass attribute on <signature/>");
	    if (signature.alias == null)
		throw new BuildException ("must define alias attribute on <signature/>");
	    Java java = (Java) project.createTask ("java");
	    java.setClassname ("sun.security.tools.JarSigner");
	    java.setArgs ("-keystore " + signature.keystore + " -storepass " + signature.storepass +
			  " " + file + " " + signature.alias);
	    java.setLocation (location);
	    java.init ();
	    java.execute ();
	}
    }

}
