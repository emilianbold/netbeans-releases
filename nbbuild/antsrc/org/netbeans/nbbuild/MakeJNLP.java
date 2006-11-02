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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

/** Generates JNLP files for signed versions of the module JAR files.
 *
 * @author Jaroslav Tulach
 */
public class MakeJNLP extends Task {
    /** the files to work on */
    private org.apache.tools.ant.types.FileSet files;
    private SignJar signTask;
    
    public org.apache.tools.ant.types.FileSet createModules() 
    throws BuildException {
        if (files != null) throw new BuildException("modules can be created just once");
        files = new org.apache.tools.ant.types.FileSet();
        return files;
    }
    
    private SignJar getSignTask() {
        if (signTask == null) {
            signTask = (SignJar)getProject().createTask("signjar");
        }
        return signTask;
    }
    
    private File target;
    public void setDir(File t) {
        target = t;
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
     * Should deafult to "&lt;all-permissions/&gt;"
     */
    public void setPermissions(String s) {
        permissions = s;
    }
    
    public void execute() throws BuildException {
        if (target == null) throw new BuildException("Output dir must be provided");
        if (files == null) throw new BuildException("modules must be provided");
        
        try {
            generateFiles();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void generateFiles() throws IOException, BuildException {
        DirectoryScanner scan = files.getDirectoryScanner(getProject());
        String[] arr = scan.getIncludedFiles();
        for (int i = 0; i < arr.length; i++) {
            File jar = new File (files.getDir(getProject()), arr[i]);
            
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
            
            Map localizedFiles = verifyExtensions(jar, theJar.getManifest(), dashcnb, codenamebase, verify);
            

            File signed = new File(target, jar.getName());
            File jnlp = new File(target, dashcnb + ".jnlp");
            
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
            writeJNLP.write("  <resources>\n");
            writeJNLP.write("     <jar href='"); writeJNLP.write(jar.getName()); writeJNLP.write("'/>\n");
            
            processExtensions(jar, theJar.getManifest(), writeJNLP, dashcnb, codebase);
            
            writeJNLP.write("  </resources>\n");
            
            {
                // write down locales
                Iterator it = localizedFiles.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry)it.next();
                    String locale = (String)e.getKey();
                    List files = (List)e.getValue();
                    
                    writeJNLP.write("  <resources locale='" + locale + "'>\n");

                    Iterator fit = files.iterator();
                    while (fit.hasNext()) {
                        File n = (File)fit.next();
                        File t = new File(target, n.getName());
                        
                        getSignTask().setJar(n);
                        getSignTask().setSignedjar(t);
                        getSignTask().execute();
                        
                        writeJNLP.write("     <jar href='"); writeJNLP.write(n.getName()); writeJNLP.write("'/>\n");
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

            getSignTask().setJar(jar);
            getSignTask().setSignedjar(signed);
            getSignTask().execute();
                
                
            theJar.close();
        }
        
    }
    
    private Map verifyExtensions(File f, Manifest mf, String dashcnb, String codebasename, boolean verify) throws IOException, BuildException {
        Map localizedFiles = new HashMap();
        
        
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
        
        HashMap fileToOwningModule = new HashMap();
        try {
            ModuleSelector.readUpdateTracking(getProject(), ut.toString(), fileToOwningModule);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new BuildException(ex);
        } catch (org.xml.sax.SAXException ex) {
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
    
    private static void removeWithLocales(Map removeFrom, String removeWhat, File clusterRoot, Map<String,List<File>> recordLocales) {
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
                    
                    List l = (List)recordLocales.get(locale);
                    if (l == null) {
                        l = new ArrayList();
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
        //System.err.println(nblibJar + ".isFile=" + nblibJar.isFile());
        if (nblibJar.isFile()) {
            File ext = new File(target, "ant-nblib-" + nblibJar.getName());
            fileWriter.write("    <jar href='" + ext.getName() + "'/>\n");
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
            
            if (isSigned (e)) {
                Copy copy = (Copy)getProject().createTask("copy");
                copy.setFile(e);
                File t = new File(target, e.getName());
                copy.setTofile(t);
                copy.execute();
                
                String  extJnlpName = dashcnb + "-ext-" + n + ".jnlp";
                File jnlp = new File(target, extJnlpName);

                FileWriter writeJNLP = new FileWriter(jnlp);
                writeJNLP.write("<?xml version='1.0' encoding='UTF-8'?>\n");
                writeJNLP.write("<jnlp spec='1.0+' codebase='" + codebase + "' >\n");
                writeJNLP.write("  <information>\n");
                writeJNLP.write("   <title>" + n + "</title>\n");
                writeJNLP.write("   <vendor>NetBeans</vendor>\n");
                writeJNLP.write("  </information>\n");
                writeJNLP.write(permissions +"\n");
                writeJNLP.write("  <resources>\n");
                writeJNLP.write("     <jar href='"); writeJNLP.write(e.getName()); writeJNLP.write("'/>\n");
                writeJNLP.write("  </resources>\n");
                writeJNLP.write("  <component-desc/>\n");
                writeJNLP.write("</jnlp>\n");
                writeJNLP.close();
                
                fileWriter.write("    <extension name='" + e.getName() + "' href='" + extJnlpName + "'/>\n");
            } else {
                File ext = new File(target, e.getName());
                
                fileWriter.write("    <jar href='" + e.getName() + "'/>\n");

                getSignTask().setJar(e);
                getSignTask().setSignedjar(ext);
                getSignTask().execute();
            }
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
    
    private static boolean isSigned(File f) throws IOException {
        JarFile jar = new JarFile(f);
        Enumeration en = jar.entries();
        while (en.hasMoreElements()) {
            JarEntry e = (JarEntry)en.nextElement();
            if (e.getName().endsWith(".SF")) {
                jar.close();
                return true;
            }
        }
        jar.close();
        return false;
    }
    
}
