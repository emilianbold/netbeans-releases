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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Java;

/** Makes a <code>.nbm</code> (<b>N</b>et<b>B</b>eans <b>M</b>odule) file.
 *
 * @author Jesse Glick
 */
public class MakeNBM extends Task {

    public static class Description {
	private StringBuffer text = new StringBuffer ();
	public Description () {
	}
	public Description (String text) {
	    addText (text);
	}
	public void addText (String text) {
	    this.text.append (text);
	}
	public String getText () {
	    return text.toString ();
	}
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
    private File license = null;
    private Description description = new Description ("(no description)");
    private Signature signature = null;

    public void setFile (String file) {
	this.file = file;
    }
    public void setTopdir (String topdir) {
	this.topdir = topdir;
    }
    public void setManifest (File manifest) {
	this.manifest = manifest;
    }
    public void setHomepage (String homepage) {
	this.homepage = homepage;
    }
    public void setDistribution (String distribution) {
	this.distribution = distribution;
    }
    public void setLicense (File license) {
	this.license = license;
    }
    public Description createDescription () {
	return (description = new Description ());
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
	    if (manifest.lastModified () < iMod &&
		(license == null || license.lastModified () < iMod))
		return;
	}
	InputStream manifestStream = null;
	InputStream licenseStream = null;
	OutputStream infoStream = null;
	try {
	    // Read module manifest for main attributes.
	    manifestStream = new FileInputStream (manifest);
	    Attributes attr = new Manifest (manifestStream).getMainAttributes ();
	    if (license != null)
		licenseStream = new FileInputStream (license);
	    infoStream = new FileOutputStream (infofile);
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
	    String licenseSimple;
	    if (license == null)
		licenseSimple = null;
	    else
		licenseSimple = license.getName ();
	    if (licenseSimple != null)
		ps.println ("        license=\"" + licenseSimple + "\"");
	    ps.println ("        downloadsize=\"0\"");
	    ps.println (">");
	    ps.print ("  <description>");
	    ps.print (description.getText ().trim ());
	    ps.println ("</description>");
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
	    if (licenseSimple != null) {
		ps.print ("  <license name=\"" + licenseSimple + "\">");
		byte[] buf = new byte[4096];
		int read;
		while ((read = licenseStream.read (buf)) != -1)
		    ps.write (buf, 0, read);
		ps.println ("</license>");
	    }
	    ps.println ("</module>");
	} catch (IOException e) {
	    throw new BuildException ("exception when creating Info/info.xml", e, location);
	} finally {
	    try {
		if (manifestStream != null) manifestStream.close ();
		if (licenseStream != null) licenseStream.close ();
		if (infoStream != null) infoStream.close ();
	    } catch (IOException e2) {
		throw new BuildException ("exception when closing streams", e2, location);
	    }
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
