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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;

/** Makes an XML file representing update information from NBMs.
 *
 * @author Jesse Glick
 */
public class MakeUpdateDesc extends Task {

    /** Set of NBMs presented as a folder in the Update Center. */
    public /*static*/ class Group {
	public List nbms = new LinkedList ();
	public String name;
        /** Displayed name of the group. */
	public void setName (String s) {
	    name = s;
	}
        // [PENDING] should support nested <fileset>s to match *.nbm?
	public Nbm createNbm () {
	    Nbm n = new Nbm ();
	    nbms.add (n);
	    return n;
	}
    }

    /** One NBM file in the group. */
    public /*static*/ class Nbm {
	public File file;
        /** Path to the NBM file.
         * Its <samp>Info/info.xml</samp> will be parsed for information to include.
         */
	public void setFile (File f) {
	    file = f;
	}
    }

    private List groups = new LinkedList ();
    private File desc;

    /** Description file to create. */
    public void setDesc (File d) {
	desc = d;
    }
    public Group createGroup () {
	Group g = new Group ();
	groups.add (g);
	return g;
    }

    public void execute () throws BuildException {
	if (desc.exists ()) {
	    // Simple up-to-date check.
	    long time = desc.lastModified ();
	    boolean uptodate = true;
	    Iterator it = groups.iterator ();
	CHECK:
	    while (it.hasNext ()) {
		Group g = (Group) it.next ();
		Iterator it2 = g.nbms.iterator ();
		while (it2.hasNext ()) {
		    Nbm n = (Nbm) it2.next ();
		    if (n.file.lastModified () > time) {
			uptodate = false;
			break CHECK;
		    }
		}
	    }
	    if (uptodate) return;
	}
	log ("Creating update description " + desc.getAbsolutePath ());
	// [PENDING] this would be easier with a proper XML read/write library;
	// unfortunately I don't have docs for any handy at the moment.
	// So e.g. it is assumed that <license> is on its own line(s), etc.
	try {
	    OutputStream os = new FileOutputStream (desc);
	    try {
		PrintWriter pw = new PrintWriter (new OutputStreamWriter (os));
		pw.println ("<?xml version='1.0'?>");
		pw.println ();
		SimpleDateFormat format = new SimpleDateFormat ("ss/mm/HH/dd/MM/yyyy");
		format.setTimeZone (TimeZone.getTimeZone ("GMT"));
		String date = format.format (new Date ());
		pw.println ("<module_updates timestamp=\"" + date + "\">");
		StringBuffer licenses = new StringBuffer ();
		Set licenseNames = new HashSet (); // Set<String>
		Iterator it = groups.iterator ();
		while (it.hasNext ()) {
		    Group g = (Group) it.next ();
		    // Don't indent; embedded descriptions would get indented otherwise.
		    pw.println ("<module_group name=\"" + g.name + "\">");
		    Iterator it2 = g.nbms.iterator ();
		    while (it2.hasNext ()) {
			Nbm n = (Nbm) it2.next ();
			long size = n.file.length ();
			ZipFile zip = new ZipFile (n.file);
			try {
			    ZipEntry entry = zip.getEntry ("Info/info.xml");
			    if (entry == null)
				throw new BuildException ("NBM " + n.file + " was malformed: no Info/info.xml", location);
			    InputStream is = zip.getInputStream (entry);
			    try {
				BufferedReader r = new BufferedReader (new InputStreamReader (is));
				String line = r.readLine ();
				if (! line.startsWith ("<?xml"))
				    throw new BuildException ("Strange info.xml line: " + line, location);
				// next line probably blank, no problem though
			    INFOXML:
				while ((line = r.readLine ()) != null) {
				    String dummyDownloadSize = "downloadsize=\"0\"";
				    int idx = line.indexOf (dummyDownloadSize);
				    if (idx != -1) {
					line = line.substring (0, idx) +
					    "downloadsize=\"" + size + "\"" +
					    line.substring (idx + dummyDownloadSize.length ());
				    }
				    String licenseMarker = "<license name=\"";
				    idx = line.indexOf (licenseMarker);
				    if (idx != -1) {
					int idx2 = line.indexOf ("\"", idx + licenseMarker.length ());
					if (idx2 == -1)
					    throw new BuildException ("Strange info.xml line: " + line, location);
					String name = line.substring (idx + licenseMarker.length (), idx2);
					// [PENDING] ideally would compare the license texts to make sure they actually match up
					boolean copy = ! licenseNames.contains (name);
					licenseNames.add (name);
					do {
					    if (copy) {
						licenses.append (line);
						licenses.append ('\n');
					    }
					    if (line.indexOf ("</license>") != -1)
						continue INFOXML;
					} while ((line = r.readLine ()) != null);
				    } else {
					// Non-license line.
					pw.println (line);
				    }
				}
			    } finally {
				is.close ();
			    }
			} finally {
			    zip.close ();
			}
		    }
		    pw.println ("</module_group>");
		}
		pw.print (licenses.toString ());
		pw.println ("</module_updates>");
		pw.close ();
	    } finally {
		os.close ();
	    }
	} catch (IOException ioe) {
	    desc.delete ();
	    throw new BuildException ("Cannot create update description", ioe, location);
	}
    }

}
