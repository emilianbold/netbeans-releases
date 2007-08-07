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
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
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
        public List<FileSet> filesets = new ArrayList<FileSet>();
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

    private List<Entityinclude> entityincludes = new ArrayList<Entityinclude>();
    private List<Group> groups = new ArrayList<Group>();
    private List<FileSet> filesets = new ArrayList<FileSet>();

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
        filesets.add(set);
    }

    private boolean automaticGrouping;
    /**
     * Turn on if you want modules added to the root fileset
     * to be automatically added to a group based on their display category (if set).
     */
    public void setAutomaticgrouping(boolean b) {
        automaticGrouping = b;
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

    public @Override void execute () throws BuildException {
        Group root = new Group();
        for (FileSet fs : filesets) {
            root.addFileSet(fs);
        }
        groups.add(root);
	if (desc.exists ()) {
	    // Simple up-to-date check.
	    long time = desc.lastModified ();
	    boolean uptodate = true;

	CHECK:
            for (Group g : groups) {
                for (FileSet n : g.filesets) {
                    if ( n != null ) {
                        DirectoryScanner ds = n.getDirectoryScanner(getProject());
                        String[] files = ds.getIncludedFiles();
                        File bdir = ds.getBasedir();
                        for (String file : files) {
                            File n_file = new File(bdir, file);
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
        
        Map<String,Collection<Module>> modulesByGroup = loadNBMs();
        boolean targetClustersDefined = false;
        for (Collection<Module> modules : modulesByGroup.values()) {
            for (Module m : modules) {
                targetClustersDefined |= m.xml.getAttributeNode("targetcluster") != null;
            }
        }
        boolean use25DTD = false;
        for (Collection<Module> modules : modulesByGroup.values()) {
            for (Module m : modules) {
                Element manifest = ((Element) m.xml.getElementsByTagName("manifest").item(0));
                use25DTD |= (m.autoload || m.eager ||
                        manifest.getAttribute("AutoUpdate-Show-In-Client") != null ||
                        manifest.getAttribute("AutoUpdate-Essential-Module") != null);
            }
        }
        
        // XXX Apparently cannot create a doc with entities using DOM 2.
	try {
            desc.delete();
            OutputStream os = new FileOutputStream(desc);
	    try {
                
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); //NOI18N
		pw.println ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //NOI18N
		pw.println ();
                DateFormat format = new SimpleDateFormat("ss/mm/HH/dd/MM/yyyy"); //NOI18N
                format.setTimeZone(TimeZone.getTimeZone("GMT")); //NOI18N
                String date = format.format(new Date());
                
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
                    if (use25DTD) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\" [");
                    } else if (targetClustersDefined) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_4.dtd\" [");
                    } else {
                        // #74866: no need for targetcluster, so keep compat w/ 5.0 AU.
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\" [");
                    }
                    // Would be better to follow order of groups and includes
                    pw.println ("    <!ENTITY entity SYSTEM \"" + xmlEscape(desc_ent.getName()) + "\">"); //NOI18N
                    int inc_num=0;
                    for (int i=0; i<entityincludes.size(); i++) {
                        Entityinclude ei = entityincludes.get(i);
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
                
                    os = new FileOutputStream(desc_ent);
                    pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); //NOI18N
                    pw.println ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"); //NOI18N
                    pw.println ("<!-- external entity include " + date + " -->");
                    pw.println ();
                    
                } else {
                    if (use25DTD) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\">");
                    } else if (targetClustersDefined) {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_4.dtd\">");
                    } else {
                        pw.println("<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.3//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_3.dtd\">");
                    }
                    pw.println ("<module_updates timestamp=\"" + date + "\">"); //NOI18N
                    pw.println ();
                }

                pw.println ();
		Map<String,Element> licenses = new HashMap<String,Element>();
                Set<String> licenseNames = new HashSet<String>();
                
                for (Map.Entry<String,Collection<Module>> entry : modulesByGroup.entrySet()) {
                    String groupName = entry.getKey();
                    // Don't indent; embedded descriptions would get indented otherwise.
                    log("Creating group \"" + groupName + "\"");
                    if (groupName != null) {
                        pw.println("<module_group name=\"" + xmlEscape(groupName) + "\">");
                        pw.println();
                    }
                    for (Module m : entry.getValue()) {
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
                            module.setAttribute("distribution", prefix + m.relativePath);
                        }
                        NodeList licenseList = module.getElementsByTagName("license");
                        if (licenseList.getLength() > 0) {
                            Element license = (Element) licenseList.item(0);
                            // XXX ideally would compare the license texts to make sure they actually match up
                            licenses.put(license.getAttribute("name"), license);
                            module.removeChild(license);
                        }
                        if (m.autoload) {
                            module.setAttribute("autoload", "true");
                        }
                        if (m.eager) {
                            module.setAttribute("eager", "true");
                        }
                        pw.flush();
                        XMLUtil.write(module, os);
                        pw.println();
                    }
                    if (groupName != null) {
                        pw.println("</module_group>");
                        pw.println();
                    }
		}
                pw.flush();
                for (Element license : licenses.values()) {
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
        public String relativePath;
        public boolean autoload, eager;
    }
    
    private Map<String,Collection<Module>> loadNBMs() throws BuildException {
        final Collator COLL = Collator.getInstance(/* XXX any particular locale? */);
        // like COLL but handles nulls ~ ungrouped modules (sorted to top):
        Comparator<String> groupNameComparator = new Comparator<String>() {
            public int compare(String gn1, String gn2) {
                return gn1 != null ?
                    (gn2 != null ? COLL.compare(gn1, gn2) : 1) :
                    (gn2 != null ? -1 : 0);
            }
        };
        Map<String,Collection<Module>> r = automaticGrouping ?
            // generally will be creating groups on the fly, so sort them:
            new TreeMap<String,Collection<Module>>(groupNameComparator) :
            // preserve explicit order of <group>s:
            new LinkedHashMap<String,Collection<Module>>();
        // sort modules by display name (where available):
        Comparator<Module> moduleDisplayNameComparator = new Comparator<Module>() {
            public int compare(Module m1, Module m2) {
                int res = COLL.compare(getName(m1), getName(m2));
                return res != 0 ? res : System.identityHashCode(m1) - System.identityHashCode(m2);
            }
            String getName(Module m) {
                Element mani = (Element) m.xml.getElementsByTagName("manifest").item(0);
                String displayName = mani.getAttribute("OpenIDE-Module-Name");
                if (displayName.length() > 0) {
                    return displayName;
                } else {
                    return mani.getAttribute("OpenIDE-Module");
                }
            }
        };
        for (Group g : groups) {
            Collection<Module> modules = r.get(g.name);
            if (modules == null) {
                modules = new TreeSet<Module>(moduleDisplayNameComparator);
                r.put(g.name, modules);
            }
            for (FileSet fs : g.filesets) {
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                for (String file : ds.getIncludedFiles()) {
                    File n_file = new File(fs.getDir(getProject()), file);
                    try {
                        ZipFile zip = new ZipFile(n_file);
                        try {
                            ZipEntry entry = zip.getEntry("Info/info.xml");
                            if (entry == null) {
                                throw new BuildException("NBM " + n_file + " was malformed: no Info/info.xml", getLocation());
                            }
                            EntityResolver nullResolver = new EntityResolver() {
                                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                                }
                            };
                            Module m = new Module();
                            InputStream is = zip.getInputStream(entry);
                            try {
                                m.xml = XMLUtil.parse(new InputSource(is), false, false, null, nullResolver).getDocumentElement();
                            } finally {
                                is.close();
                            }
                            m.nbm = n_file;
                            m.relativePath = file.replace(File.separatorChar, '/');
                            Collection<Module> moduleCollection = modules;
                            Element manifest = ((Element) m.xml.getElementsByTagName("manifest").item(0));
                            if (automaticGrouping && g.name == null) {
                                // insert modules with no explicit grouping into group acc. to manifest:
                                String categ = manifest.getAttribute("OpenIDE-Module-Display-Category");
                                if (categ.length() > 0) {
                                    moduleCollection = r.get(categ);
                                    if (moduleCollection == null) {
                                        moduleCollection = new TreeSet<Module>(moduleDisplayNameComparator);
                                        r.put(categ, moduleCollection);
                                    }
                                }
                            }
                            String cnb = manifest.getAttribute("OpenIDE-Module").replaceFirst("/\\d+$", "");
                            entry = zip.getEntry("netbeans/config/Modules/" + cnb.replace('.', '-') + ".xml");
                            if (entry != null) {
                                is = zip.getInputStream(entry);
                                try {
                                    NodeList nl = XMLUtil.parse(new InputSource(is), false, false, null, nullResolver).getElementsByTagName("param");
                                    for (int i = 0; i < nl.getLength(); i++) {
                                        String name = ((Element) nl.item(i)).getAttribute("name");
                                        String value = ((Text) nl.item(i).getFirstChild()).getData();
                                        if (name.equals("autoload") && value.equals("true")) {
                                            m.autoload = true;
                                        }
                                        if (name.equals("eager") && value.equals("true")) {
                                            m.eager = true;
                                        }
                                    }
                                } finally {
                                    is.close();
                                }
                            }
                            moduleCollection.add(m);
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
