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
    
    public /*static*/ class Include {
        public String file;
        /** Path to the entity file.
         * It included as an xml-entity pointer in master .xml file.
         */
	public void setFile (String f) {
	    file = f;
	}
    }

    private List includes = new LinkedList();
    private List groups = new LinkedList ();

    private String desc_name;
    private File desc;
    private File desc_ent;

    /** Description file to create. */
    public void setDesc (String d) {
	desc_name = d;
        desc = new File (desc_name);
    }
    public Group createGroup () {
	Group g = new Group ();
	groups.add (g);
	return g;
    }

    public Include createInclude () {
        Include i = new Include ();
        includes.add (i);
        return i;
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
                String ent_name=desc_name.toString();                
                int xml_idx = ent_name.indexOf(".xml");
                if (xml_idx != -1) {
                    ent_name = ent_name.substring (0, xml_idx) + ".ent";
                } else {
                    ent_name = ent_name + ".ent";
                }
                desc_ent = new File(ent_name);               
                
                PrintWriter pw = new PrintWriter (new OutputStreamWriter (os));
		pw.println ("<?xml version='1.0'?>");
		pw.println ();
                pw.println ("<!DOCTYPE module_updates [");
                // Would be better to follow order of groups and includes
                pw.println ("    <!ENTITY entity SYSTEM \"" + desc_ent.getName() + "\">");
                Iterator ic = includes.iterator();
                int inc_num=0;
                while (ic.hasNext ()) {
                    inc_num+=1;
                    Include i = (Include) ic.next ();                    
                    pw.println ("    <!ENTITY include" + inc_num + " SYSTEM \"" + i.file + "\">");
                }                
                pw.println ("]>");
		pw.println ();
                ic = null;

		SimpleDateFormat format = new SimpleDateFormat ("ss/mm/HH/dd/MM/yyyy");
		format.setTimeZone (TimeZone.getTimeZone ("GMT"));
		String date = format.format (new Date ());
		pw.println ("<module_updates timestamp=\"" + date + "\">");
                pw.println ("    &entity;");
                ic = includes.iterator();
                inc_num=0;
                while (ic.hasNext ()) {
                    inc_num+=1;
                    ic.next ();                    
                    pw.println ("    &include" + inc_num + ";");
                }
                ic = null;

                pw.println ("</module_updates>");
		pw.println ();
		pw.close ();
                
                os = new FileOutputStream (desc_ent);
                pw = new PrintWriter (new OutputStreamWriter (os));
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
			try {
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
			} catch (IOException ioe) {
			    throw new BuildException("Cannot access nbm file: " + n.file, ioe, location);
			}
		    }
		    pw.println ("</module_group>");
		}
		pw.print (licenses.toString ());
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
