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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.xml.sax.SAXException;

/** Generates JNLP files for signed versions of the module JAR files.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public class MakeJNLP extends Task {
    /** the files to work on */
    private FileSet files;
    private SignJar signTask;
    
    public FileSet createModules() 
    throws BuildException {
        if (files != null) throw new BuildException("modules can be created just once");
        files = new FileSet();
        return files;
    }
    
    private SignJar getSignTask() {
        if (signTask == null) {
            signTask = (SignJar)getProject().createTask("signjar");
        }
        return signTask;
    }
    
    private File targetFile;
    public void setDir(File t) {
        targetFile = t;
    }
    
    public void setAlias(String a) {
        getSignTask().setAlias(a);
    }
    
    public void setStorePass(String p) {
        getSignTask().setStorepass(p);
    }
    
    public void setKeystore(String k) {
        getSignTask().setKeystore(k);
    }
    
    private String codebase = "$$codebase";
    public void setCodebase(String s) {
        this.codebase = s;
    }
    
    private boolean verify;
    public void setVerify(boolean v) {
        this.verify = v;
    }
    
    private String verifyExcludes;
    /** Comma separated list of allowed excluded names of files during verify
     * phase.
     */
    public void setVerifyExcludes(String s) {
        this.verifyExcludes = s;
    }

    private String permissions = "<all-permissions/>";
    /**
     * XML fragment pasted into the security part of the .jnlp file.
     * Should default to "&lt;all-permissions/&gt;"
     */
    public void setPermissions(String s) {
        permissions = s;
    }
    
    private FileSet indirectJars;
    /**
     * Other JARs which should be copied into the destination directory and referred to as resources,
     * even though they are not listed as Class-Path extensions of the module and would not normally
     * be in its effective classpath. The basedir of the fileset should be a cluster root; for each
     * such JAR, a file META-INF/clusterpath/$relpath will be inserted in the JAR, where $relpath is the
     * relative path within the cluster. This permits the JAR to be located at runtime in a flat classpath,
     * using ClassLoader.getResource.
     */
    public void addIndirectJars(FileSet fs) {
        indirectJars = fs;
    }
    
    private boolean signJars = true;
    /**
     * Whether the final jars should be signed or not. Defaults to true
     * (if not supplied).
     */
    public void setSignJars(boolean s) {
        this.signJars = s;
    }

    /**
     * Signs or copies the given files according to the signJars variable value.
     */
    private void signOrCopy(File from, File to) {
        if (!from.exists() && from.getParentFile().getName().equals("locale")) {
            // skip missing locale files, probably the best fix for #103301
            log("Localization file " + from + " is referenced, but cannot be found. Skipping.", Project.MSG_WARN);
            return;
        }
        
        if (signJars) {
            getSignTask().setJar(from);
            getSignTask().setSignedjar(to);
            getSignTask().execute();
        } else {
            Copy copy = (Copy)getProject().createTask("copy");
            copy.setFile(from);
            copy.setTofile(to);
            copy.execute();
        }
    }
    
    @Override
    public void execute() throws BuildException {
        if (targetFile == null) throw new BuildException("Output dir must be provided");
        if (files == null) throw new BuildException("modules must be provided");
        
        try {
            generateFiles();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void generateFiles() throws IOException, BuildException {
        Set<String> indirectJarPaths = Collections.emptySet();
        if (indirectJars != null) {
            DirectoryScanner scan = indirectJars.getDirectoryScanner(getProject());
            indirectJarPaths = new HashSet<String>();
            for (String f : scan.getIncludedFiles()) {
                indirectJarPaths.add(f.replace(File.pathSeparatorChar, '/'));
            }
        }

        DirectoryScanner scan = files.getDirectoryScanner(getProject());
        for (String f : scan.getIncludedFiles()) {
            File jar = new File (files.getDir(getProject()), f);
            
            if (!jar.canRead()) {
                throw new BuildException("Cannot read file: " + jar);
            }
            
            JarFile theJar = new JarFile(jar);
            String codenamebase = theJar.getManifest().getMainAttributes().getValue("OpenIDE-Module");
            if (codenamebase == null) {
                throw new BuildException("Not a NetBeans Module: " + jar);
            }
            {
                int slash = codenamebase.indexOf('/');
                if (slash >= 0) {
                    codenamebase = codenamebase.substring(0, slash);
                }
            }
            String dashcnb = codenamebase.replace('.', '-');
            
            String title;
            String oneline;
            String shrt;
            String osDep = null;
            
            {
                String bundle = theJar.getManifest().getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle");
                Properties prop = new Properties();
                if (bundle != null) {
                    ZipEntry en = theJar.getEntry(bundle);
                    if (en == null) {
                        throw new BuildException("Cannot find entry: " + bundle + " in file: " + jar);
                    }
                    InputStream is = theJar.getInputStream(en);
                    prop.load(is);
                    is.close();
                }
                title = prop.getProperty("OpenIDE-Module-Name", codenamebase);
                oneline = prop.getProperty("OpenIDE-Module-Short-Description", title);
                shrt = prop.getProperty("OpenIDE-Module-Long-Description", oneline);
            }
            
            {
                String osMan = theJar.getManifest().getMainAttributes().getValue("OpenIDE-Module-Requires");
                if (osMan != null && osMan.indexOf("org.openide.modules.os.MacOSX") >= 0) { // NOI18N
                    osDep = "Mac OS X"; // NOI18N
                }
            }
            
            Map<String,List<File>> localizedFiles = verifyExtensions(jar, theJar.getManifest(), dashcnb, codenamebase, verify, indirectJarPaths);
            
            new File(targetFile, dashcnb).mkdir();

            File signed = new File(new File(targetFile, dashcnb), jar.getName());
            File jnlp = new File(targetFile, dashcnb + ".jnlp");
            
            StringWriter writeJNLP = new StringWriter();
            writeJNLP.write("<?xml version='1.0' encoding='UTF-8'?>\n");
            writeJNLP.write("<jnlp spec='1.0+' codebase='" + codebase + "' >\n");
            writeJNLP.write("  <information>\n");
            writeJNLP.write("   <title>" + title + "</title>\n");
            writeJNLP.write("   <vendor>NetBeans</vendor>\n");
            writeJNLP.write("   <description kind='one-line'>" + oneline + "</description>\n");
            writeJNLP.write("   <description kind='short'>" + shrt + "</description>\n");
            writeJNLP.write("  </information>\n");
            writeJNLP.write(permissions +"\n");
            if (osDep == null) {
                writeJNLP.write("  <resources>\n");
            } else {
                writeJNLP.write("  <resources os='" + osDep + "'>\n");
            }
            writeJNLP.write("     <jar href='" + dashcnb + '/' + jar.getName() + "'/>\n");
            
            processExtensions(jar, theJar.getManifest(), writeJNLP, dashcnb, codebase);
            processIndirectJars(writeJNLP, dashcnb, codebase);
            
            writeJNLP.write("  </resources>\n");
            
            {
                // write down locales
                for (Map.Entry<String,List<File>> e : localizedFiles.entrySet()) {
                    String locale = e.getKey();
                    List<File> allFiles = e.getValue();
                    
                    writeJNLP.write("  <resources locale='" + locale + "'>\n");

                    for (File n : allFiles) {
                        log("generating locale " + locale + " for " + n, Project.MSG_VERBOSE);
                        String name = n.getName();
                        String clusterRootPrefix = jar.getParent() + File.separatorChar;
                        String absname = n.getAbsolutePath();
                        if (absname.startsWith(clusterRootPrefix)) {
                            name = absname.substring(clusterRootPrefix.length()).replace('/', '-');
                        }
                        File t = new File(new File(targetFile, dashcnb), name);

                        signOrCopy(n, t);
                        writeJNLP.write("    <jar href='" + dashcnb + '/' + name + "'/>\n");
                    }

                    writeJNLP.write("  </resources>\n");
                    
                }
            }        
            
            writeJNLP.write("  <component-desc/>\n");
            writeJNLP.write("</jnlp>\n");
            writeJNLP.close();
            
            FileWriter w = new FileWriter(jnlp);
            w.write(writeJNLP.toString());
            w.close();

            signOrCopy(jar, signed);
            theJar.close();
        }
        
    }
    
    private Map<String,List<File>> verifyExtensions(File f, Manifest mf, String dashcnb, String codebasename, boolean verify, Set<String> indirectJarPaths) throws IOException, BuildException {
        Map<String,List<File>> localizedFiles = new HashMap<String,List<File>>();
        
        
        File clusterRoot = f.getParentFile();
        String moduleDirPrefix = "";
        File updateTracking;
        for(;;) {
            updateTracking = new File(clusterRoot, "update_tracking");
            if (updateTracking.isDirectory()) {
                break;
            }
            moduleDirPrefix = clusterRoot.getName() + "/" + moduleDirPrefix;
            clusterRoot = clusterRoot.getParentFile();
            if (clusterRoot == null || !clusterRoot.exists()) {
                if (!verify) {
                    return localizedFiles;
                }
                
                throw new BuildException("Cannot find update_tracking directory for module " + f);
            }
        }
        
        File ut = new File(updateTracking, dashcnb + ".xml");
        if (!ut.exists()) {
            throw new BuildException("The file " + ut + " for module " + codebasename + " cannot be found");
        }
        
        Map<String,String> fileToOwningModule = new HashMap<String,String>();
        try {
            ModuleSelector.readUpdateTracking(getProject(), ut.toString(), fileToOwningModule);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (ParserConfigurationException ex) {
            throw new BuildException(ex);
        } catch (SAXException ex) {
            throw new BuildException(ex);
        }
        
        log("project files: " + fileToOwningModule, Project.MSG_DEBUG);
        String name = relative(f, clusterRoot);
        log("  removing: " + name, Project.MSG_DEBUG);
        removeWithLocales(fileToOwningModule, name, clusterRoot, localizedFiles);
        name = "config/Modules/" + dashcnb + ".xml";
        log("  removing: " + name, Project.MSG_DEBUG);
        removeWithLocales(fileToOwningModule, name, clusterRoot, localizedFiles);
        name = "config/ModuleAutoDeps/" + dashcnb + ".xml";
        log("  removing: " + name, Project.MSG_DEBUG);
        removeWithLocales(fileToOwningModule, name, clusterRoot, localizedFiles);
        name = "update_tracking/" + dashcnb + ".xml";
        log("  removing: " + name, Project.MSG_DEBUG);
        removeWithLocales(fileToOwningModule, name, clusterRoot, localizedFiles);
        
        
        
        
        String path = mf.getMainAttributes().getValue("Class-Path");
        if (path != null) {
            StringTokenizer tok = new StringTokenizer(path, ", ");
            while(tok.hasMoreElements()) {
                String s = tok.nextToken();
                File e = new File(f.getParentFile(), s);
                String r = relative(e, clusterRoot);
                removeWithLocales(fileToOwningModule, r, clusterRoot, localizedFiles);
            }
        }

        fileToOwningModule.remove("ant/nblib/" + dashcnb + ".jar");

        fileToOwningModule.keySet().removeAll(indirectJarPaths);
        
        if (verifyExcludes != null) {
            StringTokenizer tok = new StringTokenizer(verifyExcludes, ", ");
            while(tok.hasMoreElements()) {
                removeWithLocales(fileToOwningModule, tok.nextToken(), clusterRoot, localizedFiles);
            }
        }
            
        
        if (verify) {
            if (!fileToOwningModule.isEmpty()) {
                throw new BuildException(
                    "Cannot build JNLP for module " + f + " as these files are in " +
                    "module's NBM, but are not referenced from any path:\n" + fileToOwningModule.keySet()
                );
            }
        }
        
        return localizedFiles;
    }
    
    private static void removeWithLocales(Map<String,String> removeFrom, String removeWhat, File clusterRoot, Map<String,List<File>> recordLocales) {
        if (removeFrom.remove(removeWhat) != null && removeWhat.endsWith(".jar")) {
            int basedir = removeWhat.lastIndexOf('/');
            String base = basedir == -1 ? "" : removeWhat.substring(0, basedir);
            String name = removeWhat.substring(basedir + 1, removeWhat.length() - 4);
            Pattern p = Pattern.compile(base + "/locale/" + name + "(|_[a-zA-Z0-9_]+)\\.jar");
            
            Iterator it = removeFrom.keySet().iterator();
            while (it.hasNext()) {
                String s = (String)it.next();
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String locale = m.group(1).substring(1);
                    
                    List<File> l = recordLocales.get(locale);
                    if (l == null) {
                        l = new ArrayList<File>();
                        recordLocales.put(locale, l);
                    }
                    l.add(new File(clusterRoot, s.replace('/', File.separatorChar)));
                    it.remove();
                }
            }
        }
    }

    private void processExtensions(File f, Manifest mf, Writer fileWriter, String dashcnb, String codebase) throws IOException, BuildException {

        File nblibJar = new File(new File(new File(f.getParentFile().getParentFile(), "ant"), "nblib"), dashcnb + ".jar");
        if (nblibJar.isFile()) {
            File ext = new File(new File(targetFile, dashcnb), "ant-nblib-" + nblibJar.getName());
            fileWriter.write("    <jar href='" + dashcnb + '/' + ext.getName() + "'/>\n");
            getSignTask().setJar(nblibJar);
            getSignTask().setSignedjar(ext);
            getSignTask().execute();
        }

        String path = mf.getMainAttributes().getValue("Class-Path");
        if (path == null) {
            return;
        }
        
        StringTokenizer tok = new StringTokenizer(path, ", ");
        while(tok.hasMoreElements()) {
            String s = tok.nextToken();
            
            File e = new File(f.getParentFile(), s);
            if (!e.canRead()) {
                throw new BuildException("Cannot read extension " + e + " referenced from " + f);
            }
            String n = e.getName();
            if (n.endsWith(".jar")) {
                n = n.substring(0, n.length() - 4);
            }
            
            if (isSigned(e) != null) {
                Copy copy = (Copy)getProject().createTask("copy");
                copy.setFile(e);
                File t = new File(new File(targetFile, dashcnb), s.replace('/', '-'));
                copy.setTofile(t);
                copy.execute();
                
                String extJnlpName = t.getName().replaceFirst("\\.jar$", "") + ".jnlp";
                File jnlp = new File(new File(targetFile, dashcnb), extJnlpName);

                FileWriter writeJNLP = new FileWriter(jnlp);
                writeJNLP.write("<?xml version='1.0' encoding='UTF-8'?>\n");
                writeJNLP.write("<jnlp spec='1.0+' codebase='" + codebase + "' >\n");
                writeJNLP.write("  <information>\n");
                writeJNLP.write("    <title>" + n + "</title>\n");
                writeJNLP.write("    <vendor>NetBeans</vendor>\n");
                writeJNLP.write("  </information>\n");
                writeJNLP.write(permissions +"\n");
                writeJNLP.write("  <resources>\n");
                writeJNLP.write("    <jar href='" + dashcnb + '/' + t.getName() + "'/>\n");
                writeJNLP.write("  </resources>\n");
                writeJNLP.write("  <component-desc/>\n");
                writeJNLP.write("</jnlp>\n");
                writeJNLP.close();
                
                fileWriter.write("    <extension name='" + e.getName().replaceFirst("\\.jar$", "") + "' href='" + dashcnb + '/' + extJnlpName + "'/>\n");
            } else {
                File ext = new File(new File(targetFile, dashcnb), s.replace('/', '-'));
                
                fileWriter.write("    <jar href='" + dashcnb + '/' + ext.getName() + "'/>\n");

                signOrCopy(e, ext);
            }
        }
    }

    private void processIndirectJars(Writer fileWriter, String dashcnb, String codebase) throws IOException, BuildException {
        if (indirectJars == null) {
            return;
        }
        DirectoryScanner scan = indirectJars.getDirectoryScanner(getProject());
        for (String f : scan.getIncludedFiles()) {
            File jar = new File(scan.getBasedir(), f);
            String rel = f.replace(File.separatorChar, '/');
            String sig = isSigned(jar);
            // javaws will reject .zip files even with signatures.
            String rel2 = rel.endsWith(".jar") ? rel : rel.replaceFirst("(\\.zip)?$", ".jar");
            File ext = new File(new File(targetFile, dashcnb), rel2.replace('/', '-').replaceFirst("^modules-", ""));
            Zip jartask = (Zip) getProject().createTask("jar");
            jartask.setDestFile(ext);
            ZipFileSet zfs = new ZipFileSet();
            zfs.setSrc(jar);
            if (sig != null) {
                // Need to cancel original signature since we are adding one entry to the JAR.
                zfs.setExcludes("META-INF/" + sig + ".*");
            }
            jartask.addZipfileset(zfs);
            zfs = new ZipFileSet();
            File blank = File.createTempFile("empty", "");
            blank.deleteOnExit();
            zfs.setFile(blank);
            zfs.setFullpath("META-INF/clusterpath/" + rel);
            jartask.addZipfileset(zfs);
            jartask.execute();
            blank.delete();
            fileWriter.write("    <jar href='" + dashcnb + '/' + ext.getName() + "'/>\n");
            getSignTask().setJar(ext);
            getSignTask().setSignedjar(null);
            getSignTask().execute();
        }
    }
    
    private static String relative(File file, File root) {
        String sfile = file.toString().replace(File.separatorChar, '/');
        String sroot = (root.toString() + File.separator).replace(File.separatorChar, '/');
        if (sfile.startsWith(sroot)) {
            return sfile.substring(sroot.length());
        }
        return sfile;
    }
    
    /** return alias if signed, or null if not */
    private static String isSigned(File f) throws IOException {
        JarFile jar = new JarFile(f);
        try {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                Matcher m = SF.matcher(en.nextElement().getName());
                if (m.matches()) {
                    return m.group(1);
                }
            }
            return null;
        } finally {
            jar.close();
        }
    }
    private static final Pattern SF = Pattern.compile("META-INF/(.+)\\.SF");
    
}
