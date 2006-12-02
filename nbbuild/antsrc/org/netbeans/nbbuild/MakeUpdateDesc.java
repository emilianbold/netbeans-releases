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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Makes an XML file representing update information from NBMs.
 *
 * @author Jesse Glick
 */
public class MakeUpdateDesc extends MatchingTask {

    protected boolean usedMatchingTask = false;
    /** Set of NBMs presented as a folder in the Update Center. */
    public /*static*/ class Group {
        public Vector<FileSet> filesets = new Vector<FileSet> ();
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

    private Vector<Entityinclude> entityincludes = new Vector<Entityinclude>();
    private Vector<Group> groups = new Vector<Group> ();
    private Vector<FileSet> filesets = new Vector<FileSet> ();

    private File desc;

    /** Description file to create. */
    public void setDesc(File d) {
        desc = d;
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
            root.addFileSet(filesets.elementAt(i));
        }
        groups.addElement(root);
	if (desc.exists ()) {
	    // Simple up-to-date check.
	    long time = desc.lastModified ();
	    boolean uptodate = true;

	CHECK:
            for (int i=0; i<groups.size(); i++) {
		Group g = groups.elementAt(i);
                for (int j=0; j<g.filesets.size(); j++) {
		    FileSet n = g.filesets.elementAt(j);
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
        
        Map<String,List<Module>> modulesByGroup = loadNBMs();
        boolean targetClustersDefined = false;
        Iterator it1 = modulesByGroup.values().iterator();
        while (it1.hasNext()) {
            Iterator it2 = ((List) it1.next()).iterator();
            while (it2.hasNext()) {
                Module m = (Module) it2.next();
                targetClustersDefined |= m.xml.getAttributeNode("targetcluster") != null;
            }
        }
        
        // XXX Apparently cannot create a doc with entities using DOM 2.
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
                    String ent_name = desc.getAbsolutePath();
                    int xml_idx = ent_name.indexOf(".xml"); //NOI18N
                    if (xml_idx != -1) {
                        ent_name = ent_name.substring (0, xml_idx) + ".ent"; //NOI18N
                    } else {
                        ent_name = ent_name + ".ent"; //NOI18N
                    }
                    File desc_ent = new File(ent_name);
                    desc_ent.delete();
                    if (targetClustersDefined) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_4.dtd\" [");
                    } else {
                        // #74866: no need for targetcluster, so keep compat w/ 5.0 AU.
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\" [");
                    }
                    // Would be better to follow order of groups and includes
                    pw.println ("    <!ENTITY entity SYSTEM \"" + xmlEscape(desc_ent.getName()) + "\">"); //NOI18N
                    int inc_num=0;
                    for (int i=0; i<entityincludes.size(); i++) {
                        Entityinclude ei = entityincludes.elementAt(i);
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
                    if (targetClustersDefined) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_4.dtd\">");
                    } else {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\">");
                    }
                    pw.println ("<module_updates timestamp=\"" + date + "\">"); //NOI18N
                    pw.println ();
                }

                pw.println ();
		Map<String,Element> licenses = new HashMap<String,Element>();
		java.util.Set<String> licenseNames = new java.util.HashSet<String> ();
                
                Iterator<Map.Entry<String,List<Module>>> modulesByGroupIt = modulesByGroup.entrySet().iterator();
                while (modulesByGroupIt.hasNext()) {
                    Map.Entry<String,List<Module>> entry = modulesByGroupIt.next();
                    String groupName = entry.getKey();
                    // Don't indent; embedded descriptions would get indented otherwise.
                    log("Creating group \"" + groupName + "\"");
                    if (!groupName.equals("root")) {
                        pw.println("<module_group name=\"" + xmlEscape(groupName) + "\">");
                        pw.println();
                    }
                    List<Module> modules = entry.getValue();
                    Iterator<Module> modulesIt = modules.iterator();
                    while (modulesIt.hasNext()) {
                        Module m = modulesIt.next();
                        Element module = m.xml;
                        if (module.getAttribute("downloadsize").equals("0")) {
                            module.setAttribute("downloadsize", Long.toString(m.nbm.length()));
                        }
                        Element manifest = (Element) module.getElementsByTagName("manifest").item(0);
                        String name = manifest.getAttribute("OpenIDE-Module-Name");
                        if (name.length() > 0) {
                            log(" Adding module " + name + " (" + m.nbm.getAbsolutePath() + ")");
                        }
                        if (dist_base != null) {
                            // fix/enforce distribution URL base
                            String prefix;
                            if (dist_base.equals(".")) {
                                prefix = "";
                            } else {
                                prefix = dist_base + "/";
                            }
                            module.setAttribute("distribution", prefix + m.nbm.getName());
                        }
                        NodeList licenseList = module.getElementsByTagName("license");
                        if (licenseList.getLength() > 0) {
                            Element license = (Element) licenseList.item(0);
                            // XXX ideally would compare the license texts to make sure they actually match up
                            licenses.put(license.getAttribute("name"), license);
                            module.removeChild(license);
                        }
                        pw.flush();
                        XMLUtil.write(module, os);
                        pw.println();
                    }
                    if (!groupName.equals("root")) {
                        pw.println("</module_group>");
                        pw.println();
                    }
		}
                pw.flush();
                Iterator it = licenses.values().iterator();
                while (it.hasNext()) {
                    Element license = (Element) it.next();
                    XMLUtil.write(license, os);
                }
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

    private static class Module {
        public Module() {}
        public Element xml;
        public File nbm;
    }
    
    private Map<String,List<Module>> loadNBMs() throws BuildException {
        Map<String,List<Module>> r = new LinkedHashMap<String,List<Module>>();
        for (Group g : groups) {
            List<Module> modules = new ArrayList<Module>();
            r.put(g.name, modules);
            for (int fsi = 0; fsi < g.filesets.size(); fsi++) {
                FileSet fs = g.filesets.elementAt(fsi);
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                String[] files = ds.getIncludedFiles();
                for (int fid = 0; fid < files.length; fid++) {
                    File n_file = new File(fs.getDir(getProject()), files[fid]);
                    try {
                        ZipFile zip = new ZipFile(n_file);
                        try {
                            ZipEntry entry = zip.getEntry("Info/info.xml");
                            if (entry == null) {
                                throw new BuildException("NBM " + n_file + " was malformed: no Info/info.xml", getLocation());
                            }
                            InputStream is = zip.getInputStream(entry);
                            try {
                                Module m = new Module();
                                m.xml = XMLUtil.parse(new InputSource(is), false, false, null, new EntityResolver() {
                                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                                        return new InputSource(new ByteArrayInputStream(new byte[0]));
                                    }
                                }).getDocumentElement();
                                m.nbm = n_file;
                                modules.add(m);
                            } finally {
                                is.close();
                            }
                        } finally {
                            zip.close();
                        }
                    } catch (Exception e) {
                        throw new BuildException("Cannot access nbm file: " + n_file, e, getLocation());
                    }
                }
            }
        }
        return r;
    }
        
}
