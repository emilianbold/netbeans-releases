/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/** Makes an XML file representing update information from NBMs.
 *
 * @author Jesse Glick
 */
public class MakeUpdateDesc extends MatchingTask {

    protected boolean usedMatchingTask = false;
    /** Set of NBMs presented as a folder in the Update Center. */
    public /*static*/ class Group {
        public Vector nbms = new Vector ();
        public Vector filesets = new Vector ();
        public Vector scanners = new Vector ();
	public String name;

        /** Displayed name of the group. */
	public void setName (String s) {
	    name = s;
	}

        /** Add fileset to the group of NetBeans modules **/
        public void addFileSet (FileSet set) {
            filesets.add(set);
        }
    }
    
    /** pointer to another xml entity to include **/
    public class Entityinclude {
        public String file;
        /** Path to the entity file.
         * It included as an xml-entity pointer in master .xml file.
         */
	public void setFile (String f) {
	    file = f;
	}
    }

    private Vector entityincludes = new Vector();
    private Vector groups = new Vector ();
    private Vector filesets = new Vector ();

    private String desc_name;
    private File desc;
    private File desc_ent;

    /** Description file to create. */
    public void setDesc (String d) {
	desc_name = d;
        desc = new File (desc_name);
    }

    /** Module group to create **/
    public Group createGroup () {
	Group g = new Group ();
	groups.add (g);
	return g;
    }

    /** External XML entity include **/
    public Entityinclude createEntityinclude () {
        Entityinclude i = new Entityinclude ();
        entityincludes.add (i);
        return i;
    }

   /**
     * Adds a set of files (nested fileset attribute).
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

    
    private String dist_base;
   /**
    * Set distribution base, which will be enforced
    */
    public void setDistBase(String dbase) {
        dist_base = dbase;
    }
    
    // Similar to org.openide.xml.XMLUtil methods.
    private static String xmlEscape(String s) {
        int max = s.length();
        StringBuffer s2 = new StringBuffer((int)(max * 1.1 + 1));
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    s2.append("&lt;"); //NOI18N
                    break;
                case '>':
                    s2.append("&gt;"); //NOI18N
                    break;
                case '&':
                    s2.append("&amp;"); //NOI18N
                    break;
                case '"':
                    s2.append("&quot;"); //NOI18N
                    break;
                default:
                    s2.append(c);
                    break;
            }
        }
        return s2.toString();
    }

    public void execute () throws BuildException {
        Group root = new Group();
        root.setName("root"); //NOI18N
        for (int i=0; i < filesets.size(); i++) {
            root.addFileSet((FileSet) filesets.elementAt(i));
        }
        groups.addElement(root);
	if (desc.exists ()) {
	    // Simple up-to-date check.
	    long time = desc.lastModified ();
	    boolean uptodate = true;

	CHECK:
            for (int i=0; i<groups.size(); i++) {
		Group g = (Group) groups.elementAt(i);
                for (int j=0; j<g.filesets.size(); j++) {
		    FileSet n = (FileSet) g.filesets.elementAt(j);
                    if ( n != null ) {
                        DirectoryScanner ds = n.getDirectoryScanner(getProject());
                        String[] files = ds.getIncludedFiles();
                        File bdir = ds.getBasedir();
                        for (int k=0; k <files.length; k++) {
                            File n_file = new File(bdir, files[k]);
                            if (n_file.lastModified () > time) {
                                uptodate = false;
                                break CHECK;
                            }
                        }
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
            desc.delete();
	    java.io.OutputStream os = new java.io.FileOutputStream (desc);
	    try {
                
                java.io.PrintWriter pw = new java.io.PrintWriter (new java.io.OutputStreamWriter (os, "UTF-8")); //NOI18N
		pw.println ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //NOI18N
		pw.println ();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat ("ss/mm/HH/dd/MM/yyyy"); //NOI18N
		format.setTimeZone (java.util.TimeZone.getTimeZone ("GMT")); //NOI18N
		String date = format.format (new java.util.Date ());
                
            if ( entityincludes.size() > 0 ) {
                    // prepare .ent file
                    String ent_name=desc_name.toString();                
                    int xml_idx = ent_name.indexOf(".xml"); //NOI18N
                    if (xml_idx != -1) {
                        ent_name = ent_name.substring (0, xml_idx) + ".ent"; //NOI18N
                    } else {
                        ent_name = ent_name + ".ent"; //NOI18N
                    }
                    desc_ent = new File(ent_name);               
                    desc_ent.delete();
                    pw.println ("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\" ["); //NOI18N
                    // Would be better to follow order of groups and includes
                    pw.println ("    <!ENTITY entity SYSTEM \"" + xmlEscape(desc_ent.getName()) + "\">"); //NOI18N
                    int inc_num=0;
                    for (int i=0; i<entityincludes.size(); i++) {
                        Entityinclude ei = (Entityinclude) entityincludes.elementAt(i);
                        pw.println ("    <!ENTITY include" + i + " SYSTEM \"" + xmlEscape(ei.file) + "\">"); //NOI18N
                    }
                    pw.println ("]>"); //NOI18N
                    pw.println ();
                    pw.println ("<module_updates timestamp=\"" + xmlEscape(date) + "\">"); //NOI18N
                    pw.println ("    &entity;"); //NOI18N
                    for (int i=0; i<entityincludes.size(); i++) {
                        pw.println ("    &include" + i + ";"); //NOI18N
                    }
                    pw.println ("</module_updates>"); //NOI18N
                    pw.println ();
                    pw.flush ();
                    pw.close ();
                
                    os = new java.io.FileOutputStream (desc_ent);
                    pw = new java.io.PrintWriter (new java.io.OutputStreamWriter (os, "UTF-8")); //NOI18N
                    pw.println ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //NOI18N
                    pw.println ("<!-- external entity include " + date + " -->");
                    pw.println ();
                    
                } else {
                    pw.println ("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\">"); //NOI18N
                    pw.println ("<module_updates timestamp=\"" + date + "\">"); //NOI18N
                    pw.println ();
                }

                pw.println ();
		StringBuffer licenses = new StringBuffer ();
		java.util.Set licenseNames = new java.util.HashSet (); // Set<String>
                
                for (int gi=0; gi < groups.size(); gi++) {
		    Group g = (Group) groups.elementAt(gi);
		    // Don't indent; embedded descriptions would get indented otherwise.
                    log ("Creating group \"" + g.name + "\"");
                    if ( ! g.name.equals("root")) { //NOI18N
                        pw.println ("<module_group name=\"" + xmlEscape(g.name) + "\">"); //NOI18N
                        pw.println ();
                    }
                    for (int fsi=0; fsi < g.filesets.size(); fsi++) {
                        FileSet fs = (FileSet) g.filesets.elementAt(fsi);
                        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                        String[] files = ds.getIncludedFiles();
                        for (int fid=0; fid < files.length; fid++) {
                            File n_file = new File(fs.getDir(getProject()), files[fid]);
                            try {
                                long size = n_file.length ();
                                java.util.zip.ZipFile zip = new java.util.zip.ZipFile (n_file);
                                try {
                                    java.util.zip.ZipEntry entry = zip.getEntry ("Info/info.xml"); //NOI18N
                                    if (entry == null) {
                                        throw new BuildException ("NBM " + n_file + " was malformed: no Info/info.xml", getLocation());
                                    }
                                    java.io.InputStream is = zip.getInputStream (entry);
                                    try {
                                        java.io.BufferedReader r = new java.io.BufferedReader (new java.io.InputStreamReader (is, "UTF-8")); //NOI18N
                                        String line = r.readLine ();
                                        if (!line.startsWith ("<?xml")) { //NOI18N
                                            throw new BuildException("Strange info.xml line: " + line, getLocation());
                                        }
                                        // next line probably blank, no problem though
                                    INFOXML:
                                        while ((line = r.readLine ()) != null) {
                                            String dummyDownloadSize = "downloadsize=\"0\""; //NOI18N
                                            int idx = line.indexOf (dummyDownloadSize);
                                            if (idx != -1) {
                                                line = line.substring (0, idx) +
                                                    "downloadsize=\"" + size + "\"" + //NOI18N
                                                    line.substring (idx + dummyDownloadSize.length ());
                                            }
                                            String dummyModuleName = "OpenIDE-Module-Name=\""; //NOI18N
                                            idx = line.indexOf(dummyModuleName);
                                            if (idx != -1) {
                                                String mn = line.substring (idx + dummyModuleName.length () - 1);
                                                log (" Adding module   " + mn + " (" + n_file.getAbsolutePath() + ")");
                                            }
                                            if (dist_base != null) {
                                                // fix/enforce distribution URL base
                                                String dummyDistribution = "distribution=\""; //NOI18N
                                                idx = line.indexOf (dummyDistribution);
                                                if (idx != -1) {
                                                    String line1 = line.substring (0, idx) + "distribution=\""; //NOI18N
                                                    log ("distribution line1  : \"" + line1 + "\"", Project.MSG_DEBUG);
                                                    String pomline = line.substring (idx + dummyDistribution.length ());
                                                    log ("distribution pomline: \"" + pomline + "\"", Project.MSG_DEBUG);
                                                    String line2;
                                                    int idx2 = pomline.indexOf("\""); //NOI18N
                                                    if (idx2 != -1) {
                                                        line2 = pomline.substring (idx2);
                                                        log ("distribution line2: \"" + line2 + "\"", Project.MSG_DEBUG);
                                                    } else {
                                                        throw new BuildException("Strange info.xml line: " + line, getLocation());
                                                    }
                                                    String newline = line1 + dist_base + "/" + n_file.getName() + line2;
                                                    if (!newline.equals(line)) {
                                                        log (" <- distribution fixed from: \"" + line + "\"", Project.MSG_DEBUG);
                                                        log (" -> distribution fixed to  : \"" + newline + "\"", Project.MSG_VERBOSE);
                                                        line = newline;
                                                    }
                                                }
                                            }

                                            String docType = "<!DOCTYPE module"; //NOI18N
                                            idx = line.indexOf(docType);
                                            if (idx != -1) 
                                                continue; //Do nothing, it shouldn't be included
                                            String licenseMarker = "<license name=\""; //NOI18N
                                            idx = line.indexOf (licenseMarker);
                                            if (idx != -1) {
                                                int idx2 = line.indexOf ("\"", idx + licenseMarker.length ()); //NOI18N
                                                if (idx2 == -1) {
                                                    throw new BuildException("Strange info.xml line: " + line, getLocation());
                                                }
                                                String name = line.substring (idx + licenseMarker.length (), idx2);
                                                // [PENDING] ideally would compare the license texts to make sure they actually match up
                                                boolean copy = ! licenseNames.contains (name);
                                                licenseNames.add (name);
                                                do {
                                                    if (copy) {
                                                        licenses.append (line);
                                                        licenses.append ('\n'); //NOI18N
                                                    }
                                                    if (line.indexOf ("</license>") != -1) { //NOI18N
                                                        licenses.append ('\n'); //NOI18N
                                                        continue INFOXML;
                                                    }
                                                } while ((line = r.readLine ()) != null);
                                            } else {
                                                // Non-license line.
                                                pw.println (line);
                                            }
                                        }
                                    } finally {
                                        is.close ();
                                        pw.println ();
                                    }
                                } finally {
                                    zip.close ();
                                }
                            } catch (IOException ioe) {
                                throw new BuildException("Cannot access nbm file: " + n_file, ioe, getLocation());
                            }
                        }
		    }
                    if ( ! g.name.equals("root")) { //NOI18N
                        pw.println ("</module_group>"); //NOI18N
                        pw.println ();
                    }
		}
		pw.print (licenses.toString ());
                if ( entityincludes.size() <= 0 ) {
                    pw.println ("</module_updates>"); //NOI18N
                    pw.println ();
                }
                pw.flush ();
		pw.close ();
	    } finally {
                os.flush ();
		os.close ();
	    }
	} catch (IOException ioe) {
	    desc.delete ();
	    throw new BuildException("Cannot create update description", ioe, getLocation());
	}
    }

}
