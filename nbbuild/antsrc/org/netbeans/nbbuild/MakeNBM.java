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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.ZipFileSet;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Makes a <code>.nbm</code> (<b>N</b>et<b>B</b>eans <b>M</b>odule) file.
 *
 * @author Jesse Glick
 */
public class MakeNBM extends Task {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd");
    
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
                boolean html = file.getName().endsWith(".html") || file.getName().endsWith(".htm");
                log("Including contents of " + file + " (HTML mode: " + html + ")", Project.MSG_VERBOSE);
		long lmod = file.lastModified ();
		if (lmod > mostRecentInput) mostRecentInput = lmod;
		addSeparator ();
		try {
		    InputStream is = new FileInputStream (file);
		    try {
			BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null) {
                            if (html) {
                                // Clean out any markup first. First tags:
                                line = line.replaceAll("</?[a-zA-Z0-9_.:-]+( +[a-zA-Z0-9_.:-]+( *= *([^ \"]+|\"[^\"]*\"))?)*/?>", "");
                                // DOCTYPE:
                                line = line.replaceAll("<![a-zA-Z]+[^>]*>", "");
                                // Comments (single-line only at the moment):
                                line = line.replaceAll("<!--([^-]|-[^-])*-->", "");
                                // Common named character entities:
                                line = line.replaceAll("&quot;", "\"");
                                line = line.replaceAll("&nbsp;", " ");
                                line = line.replaceAll("&copy;", "\u00A9");
                                line = line.replaceAll("&apos;", "'");
                                line = line.replaceAll("&lt;", "<");
                                line = line.replaceAll("&gt;", ">");
                                line = line.replaceAll("&amp;", "&");
                            }
                            line = line.replaceAll("[\\p{Cntrl}&&[^\t]]", ""); // #74546
                            text.append(line);
                            text.append('\n');
                        }
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
		if (text.charAt (text.length () - 1) != '\n')
		    text.append ('\n');
		text.append ("-----------------------------------------------------\n");
	    }
	}
        public org.w3c.dom.Text getTextNode(Document ownerDoc) {
            // XXX Current XMLUtil.write anyway does not preserve CDATA sections, it seems.
            String nocdata = getProject().getProperty("makenbm.nocdata");
            if (nocdata != null && Project.toBoolean(nocdata)) {
                return ownerDoc.createTextNode(text.toString());
            } else {
                return ownerDoc.createCDATASection(text.toString());
            }
	}
        /** @deprecated */
        @Deprecated
        public void setName(String name) {
            getProject().log(getLocation() + ": the 'name' attribute on <license> is deprecated", Project.MSG_WARN);
        }
	public String getName () {
            if (name == null) {
                name = crcOf(text);
            }
	    return name;
	}
        private String crcOf(StringBuffer text) {
            CRC32 crc = new CRC32();
            try {
                crc.update(text.toString().replaceAll("\\s+", " ").trim().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new BuildException(ex);
            }
            return Long.toHexString(crc.getValue()).toUpperCase(Locale.ENGLISH);
        }
        /** Include a file (and set the license name according to its basename). */
	public void setFile (File file) {
	    // This actually adds the text and so on:
	    new FileInsert ().setLocation (file);
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

    /** <samp>&lt;signature&gt;</samp> subelement for signing the NBM. */
    public /*static*/ class Signature {
        public File keystore;
        public String storepass, alias;
        /** Path to the keystore (private key). */
        public void setKeystore(File f) {
            keystore = f;
        }
        /** Password for the keystore.
         * If a question mark (<samp>?</samp>), the NBM will not be signed
         * and a warning will be printed.
         */
        public void setStorepass(String s) {
            storepass = s;
        }
        /** Alias for the private key. */
        public void setAlias(String s) {
            alias = s;
        }
    }
    
    private File productDir = null;
    private File file = null;
    private File manifest = null;
    /** see #13850 for explanation */
    private String moduleName = null;
    private String homepage = null;
    private String distribution = "";
    private String needsrestart = null;
    private String moduleauthor = null;
    private String releasedate = null;
    private String global = null;
    private String targetcluster = null;
    private String jarSignerMaxMemory = "96m";
    private Blurb license = null;
    private Blurb description = null;
    private Blurb notification = null;
    private Signature signature = null;
    private long mostRecentInput = 0L;
    private boolean isStandardInclude = true;
    private Vector<ExternalPackage> externalPackages = null;
    
    /** Include netbeans directory - default is true */
    public void setIsStandardInclude(boolean isStandardInclude) {
        this.isStandardInclude = isStandardInclude;
    }
    
    /** Directory of the product's files */
    public void setProductDir( File dir ) {
        productDir = dir;
    }
    
    /** Name of resulting NBM file. */
    public void setFile(File file) {
        this.file = file;
    }
    
    /** Module manifest needed for versioning.
     * @deprecated Use {@link #setModule} instead.
     */
    @Deprecated
    public void setManifest(File manifest) {
        this.manifest = manifest;
        long lmod = manifest.lastModified();
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
    public void setModule(String module) {
        this.moduleName = module;
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
    /** Sets name of module author */
    public void setModuleauthor (String author) {
        this.moduleauthor = author;
    }
    /** Install globally? */
    public void setGlobal (String isGlobal) {
        this.global = isGlobal;
    }
    /** Sets pattern for target cluster */
    public void setTargetcluster (String targetCluster) {
        this.targetcluster = targetCluster;
    }
    /** Maximum memory allowed to be used by jarsigner task. Default is 96 MB. */
    public void setJarSignerMaxMemory (String jsmm) {
        this.jarSignerMaxMemory = jsmm;
    }
    /** Release date of NBM. */
    public void setReleasedate (String date) {
        this.releasedate = date;
    }
    /** URL where this NBM file is expected to be downloadable from. */
    public void setDistribution (String distribution) throws BuildException {
        this.distribution = distribution;
        if (!(this.distribution.equals(""))) {
            // check the URL
            try {
                URI uri = java.net.URI.create(this.distribution);
            } catch (IllegalArgumentException ile) {
                throw new BuildException("Distribution URL \"" + this.distribution + "\" is not a valid URI", ile, getLocation());
            }
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
	    externalPackages = new Vector<ExternalPackage>();
	externalPackages.add( externalPackage );
	return externalPackage;
    }
    
    private ZipFileSet main = null;
    
    public ZipFileSet createMain () {
        return (main = new ZipFileSet());
    }

    public void execute () throws BuildException {
        if (productDir == null) {
            throw new BuildException("must set directory of compiled product", getLocation());
        }
	if (file == null) {
	    throw new BuildException("must set file for makenbm", getLocation());
        }
        if (manifest == null && moduleName == null) {
            throw new BuildException("must set module for makenbm", getLocation());
        }
        if (manifest != null && moduleName != null) {
            throw new BuildException("cannot set both manifest and module for makenbm", getLocation());
        }

    File file;
    String rootDir = getProject ().getProperty ("nbm.target.dir");
    if (rootDir != null && !rootDir.equals ("")) { 
        file = new File (rootDir, this.file.getName ());
    } else {
        file = this.file;
    }

	// If desired, override the license and/or URL. //
        overrideURLIfNeeded() ;
	overrideLicenseIfNeeded() ;

        File module = new File( productDir, moduleName );
        // Will create a file Info/info.xml to be stored in tmp
        Attributes attr = null;
        if (module != null) {
            // The normal case; read attributes from its manifest and maybe bundle.
            long mMod = module.lastModified();
            if (mostRecentInput < mMod) mostRecentInput = mMod;
            try {
                JarFile modulejar = new JarFile(module);
                try {
                    attr = modulejar.getManifest().getMainAttributes();
                    String bundlename = attr.getValue("OpenIDE-Module-Localizing-Bundle");
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
                            File variant = new File(new File(module.getParentFile(), "locale"), module.getName());
                            if (!variant.isFile()) throw new BuildException(bundlename + " not found in " + module, getLocation());
                            long vmMod = variant.lastModified();
                            if (mostRecentInput < vmMod) mostRecentInput = vmMod;
                            ZipFile variantjar = new ZipFile(variant);
                            try {
                                bundleentry = variantjar.getEntry(bundlename);
                                if (bundleentry == null) throw new BuildException(bundlename + " not found in " + module + " nor in " + variant, getLocation());
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
                            if (! name.startsWith("OpenIDE-Module-")) continue;
                            attr.putValue(name, (String)entry.getValue());
                        }
                    } // else all loc attrs in main manifest, OK
                } finally {
                    modulejar.close();
                }
            } catch (IOException ioe) {
                throw new BuildException("exception while reading " + module, ioe, getLocation());
            }
        }
        
        if (mostRecentInput < file.lastModified()) {
            log("Skipping NBM creation as most recent input is younger: " + mostRecentInput + " than the target file: " + file.lastModified(), Project.MSG_VERBOSE);
            return;
        } else {
            log("Most recent input: " + mostRecentInput + " file: " + file.lastModified(), Project.MSG_DEBUG);
        }
        
        
        Document infoXmlContents = createInfoXml(attr);
        File infofile;
        try {
	    infofile = File.createTempFile("info",".xml");
            OutputStream infoStream = new FileOutputStream (infofile);
            try {
                XMLUtil.write(infoXmlContents, infoStream);
            } finally {
                infoStream.close ();
            }
        } catch (IOException e) {
            throw new BuildException("exception when creating Info/info.xml", e, getLocation());
        }
        infofile.deleteOnExit();
        ZipFileSet infoXML = new ZipFileSet();
        infoXML.setFile( infofile );
        infoXML.setFullpath("Info/info.xml");

        String codename = attr.getValue("OpenIDE-Module");
        if (codename == null)
 	    new BuildException( "Can't get codenamebase" );
 	
 	UpdateTracking tracking = new UpdateTracking(productDir.getAbsolutePath());
 	String files[] = tracking.getListOfNBM( codename );
	log("Going to update module_tracking.xml record for "+codename+" in file "+file.getName(), Project.MSG_DEBUG);
	String mtdir = (new File(productDir.getAbsolutePath())).getParent();
	log("  attempting to use module_tracking.xml file in directory "+mtdir,Project.MSG_DEBUG);
        ModuleTracking mt = new ModuleTracking (mtdir);
        ModuleTracking.Module mtm = (ModuleTracking.Module) mt.getModulesByCodeName().get(codename);
        if (mtm != null) {
	    log("  file: \""+mtm.getNbmFileName()+"\" => \""+file.getName()+"\"",Project.MSG_DEBUG);
            mtm.setNbmFileName(file.getName());
	    log("  homepage: \""+mtm.getNbmHomePage()+ "\" => \"" + homepage + "\"",Project.MSG_DEBUG);
            mtm.setNbmHomePage(homepage);
	    log("  moduleauthor: \""+mtm.getNbmModuleAuthor()+ "\" => \"" + moduleauthor + "\"", Project.MSG_DEBUG);
            mtm.setNbmModuleAuthor(moduleauthor);
	    log("  needsrestart: \""+mtm.getNbmNeedsRestart()+ "\" => \"" + needsrestart + "\"", Project.MSG_DEBUG);
            mtm.setNbmNeedsRestart(needsrestart);
	    log("  global: \""+mtm.getNbmIsGlobal()+ "\" => \"" + global + "\"", Project.MSG_DEBUG);
            mtm.setNbmIsGlobal(global);
            // XXX this makes no sense. Why should targetcluster be defined in update_tracking/*.xml?
            // (In fact it is not, but I can't tell why it isn't. -jglick)
	    log("  targetcluster: \""+mtm.getNbmTargetCluster ()+ "\" => \"" + targetcluster + "\"", Project.MSG_DEBUG);
            mtm.setNbmTargetCluster (targetcluster);
	    log("  releasedate: \""+mtm.getNbmReleaseDate() + "\" => \"" + releasedate+"\"", Project.MSG_DEBUG);
            mtm.setNbmReleaseDate(releasedate);
            mt.write();
        } else {
	    log("ModuleTracking.Module record doesn't exist for "+codename+" and file "+file.getName(), Project.MSG_DEBUG);
        }
        
 	ZipFileSet fs = new ZipFileSet();
 	fs.setDir( productDir );
 	for (int i=0; i < files.length; i++)
 	    fs.createInclude().setName( files[i] );
 	fs.setPrefix("netbeans/");

	// JAR it all up together.
	long jarModified = file.lastModified (); // may be 0
	//log ("Ensuring existence of NBM file " + file);
	Jar jar = (Jar) getProject().createTask("jar");
    
        jar.setDestFile(file);
        jar.addZipfileset(fs);
        jar.addFileset (infoXML);
        if (main != null) { // Add the main dir
            main.setPrefix("main"); // use main prefix
            jar.addZipfileset(main);
        }
            
        jar.setCompress(true);
	jar.setLocation(getLocation());
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
            if (signature.storepass.equals ("?") || signature.storepass.indexOf("${") != -1 || !signature.keystore.exists()) {
                log ("Not signing NBM file " + file + "; no stored-key password provided or keystore (" 
		     + signature.keystore.toString() + ") doesn't exist", Project.MSG_WARN);
            } else {
                log ("Signing NBM file " + file);
                SignJar signjar = (SignJar) getProject().createTask("signjar");
                try { // Signatures changed in various Ant versions.
                    try {
                        SignJar.class.getMethod("setKeystore", File.class).invoke(signjar, signature.keystore);
                    } catch (NoSuchMethodException x) {
                        SignJar.class.getMethod("setKeystore", String.class).invoke(signjar, signature.keystore.getAbsolutePath());
                    }
                    try {
                        SignJar.class.getMethod("setJar", File.class).invoke(signjar, file);
                    } catch (NoSuchMethodException x) {
                        SignJar.class.getMethod("setJar", String.class).invoke(signjar, file.getAbsolutePath());
                    }
                } catch (BuildException x) {
                    throw x;
                } catch (Exception x) {
                    throw new BuildException(x);
                }
                signjar.setStorepass (signature.storepass);
                signjar.setAlias (signature.alias);
                signjar.setLocation(getLocation());
                signjar.setMaxmemory(this.jarSignerMaxMemory);
                signjar.init ();
                signjar.execute ();
            }
	}
    }

    private Document createInfoXml(final Attributes attr) throws BuildException {
        DOMImplementation domimpl;
        try {
            domimpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException x) {
            throw new BuildException(x, getLocation());
        }
        String pub, sys;
        if (attr.getValue("AutoUpdate-Show-In-Client") != null || attr.getValue("AutoUpdate-Essential-Module") != null) {
            pub = "-//NetBeans//DTD Autoupdate Module Info 2.5//EN";
            sys = "http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd";
        } else if (targetcluster != null && !("".equals(targetcluster))) {
            pub = "-//NetBeans//DTD Autoupdate Module Info 2.4//EN";
            sys = "http://www.netbeans.org/dtds/autoupdate-info-2_4.dtd";
        } else {
            // #74866: no need for targetcluster, so keep compat w/ 5.0 AU.
            pub = "-//NetBeans//DTD Autoupdate Module Info 2.3//EN";
            sys = "http://www.netbeans.org/dtds/autoupdate-info-2_3.dtd";
        }
        Document doc = domimpl.createDocument(null, "module", domimpl.createDocumentType("module", pub, sys));
        String codenamebase = attr.getValue("OpenIDE-Module");
        if (codenamebase == null) {
            throw new BuildException("invalid manifest, does not contain OpenIDE-Module", getLocation());
        }
        // Strip major release number if any.
        int idx = codenamebase.lastIndexOf('/');
        if (idx != -1) codenamebase = codenamebase.substring(0, idx);
        Element module = doc.getDocumentElement();
        module.setAttribute("codenamebase", codenamebase);
        if (homepage != null) {
            module.setAttribute("homepage", homepage);
        }
        if (distribution != null) {
            module.setAttribute("distribution", distribution);
        } else {
            throw new BuildException("NBM distribution URL is not set", getLocation());
        }
        // Here we only write a name for the license.
        if (license != null) {
            String name = license.getName();
            if (name == null) {
                throw new BuildException("Every license must have a name or file attribute", getLocation());
            }
            module.setAttribute("license", name);
        }
        module.setAttribute("downloadsize", "0");
        if (needsrestart != null) {
            module.setAttribute("needsrestart", needsrestart);
        }
        if (global != null && !("".equals(global))) {
            module.setAttribute("global", global);
        }
        if (targetcluster != null && !("".equals(targetcluster))) {
            module.setAttribute("targetcluster", targetcluster);
        }
        if (moduleauthor != null) {
            module.setAttribute("moduleauthor", moduleauthor);
        }
        if (releasedate == null || "".equals(releasedate)) {
            // if date is null, set today
            releasedate = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
        }
        module.setAttribute("releasedate", releasedate);
        if (description != null) {
            module.appendChild(doc.createElement("description")).appendChild(description.getTextNode(doc));
        }
        if (notification != null) {
            module.appendChild(doc.createElement("module_notification")).appendChild(notification.getTextNode(doc));
        }
        if (externalPackages != null) {
            Enumeration exp = externalPackages.elements();
            while (exp.hasMoreElements()) {
                ExternalPackage externalPackage = (ExternalPackage) exp.nextElement();
                if (externalPackage.name == null ||
                        externalPackage.targetName == null ||
                        externalPackage.startUrl == null)
                    throw new BuildException("Must define name, targetname, starturl for external package");
                Element el = doc.createElement("external_package");
                el.setAttribute("name", externalPackage.name);
                el.setAttribute("target_name", externalPackage.targetName);
                el.setAttribute("start_url", externalPackage.startUrl);
                if (externalPackage.description != null) {
                    el.setAttribute("description", externalPackage.description);
                }
                module.appendChild(el);
            }
        }
        // Write manifest attributes.
        Element el = doc.createElement("manifest");
        List<String> attrNames = new ArrayList<String>(attr.size());
        Iterator it = attr.keySet().iterator();
        while (it.hasNext()) {
            attrNames.add(((Attributes.Name)it.next()).toString());
        }
        Collections.sort(attrNames);
        it = attrNames.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            // Ignore irrelevant attributes (cf. www/www/dtds/autoupdate-catalog-*.dtd
            //  and www/www/dtds/autoupdate-info-*.dtd):
            // XXX better would be to enumerate the attrs it *does* recognize!
            if (!name.startsWith("OpenIDE-Module") && !name.startsWith("AutoUpdate-")) continue;
            if (name.equals("OpenIDE-Module-Localizing-Bundle")) continue;
            if (name.equals("OpenIDE-Module-Install")) continue;
            if (name.equals("OpenIDE-Module-Layer")) continue;
            if (name.equals("OpenIDE-Module-Description")) continue;
            if (name.equals("OpenIDE-Module-Package-Dependency-Message")) continue;
            if (name.equals("OpenIDE-Module-Public-Packages")) continue;
            if (name.equals("OpenIDE-Module-Friends")) continue;
            el.setAttribute(name, attr.getValue(name));
        }
        module.appendChild(el);
        // Maybe write out license text.
        if (license != null) {
            el = doc.createElement("license");
            el.setAttribute("name", license.getName());
            el.appendChild(license.getTextNode(doc));
            module.appendChild(el);
        }
        return doc;
    }
   

  /** This returns true if the license should be overridden. */
  protected boolean overrideLicense() {
    return( getLicenseOverride() != null) ;
  }

  /** Get the license to use if the license should be overridden,
   *  otherwise return null.
   */
  protected String getLicenseOverride() {
    String s = getProject().getProperty( "makenbm.override.license") ;
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
    String s = getProject().getProperty( "makenbm.override.url") ;
    if( s != null) {
      if( s.equals( "")) {
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
      license.setFile( getProject().resolveFile( getLicenseOverride())) ;
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
