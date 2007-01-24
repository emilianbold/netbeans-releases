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
 *
 * $Id$
 */
package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;
import org.netbeans.installer.infra.build.ant.utils.FileEntry;

/**
 *
 * @author Kirill Sorokin
 */
public class Package extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String METAINF_ENTRY    = "META-INF/";
    private static final String FILES_LIST_ENTRY = "META-INF/files.list";
    private static final String MANIFEST_ENTRY   = "META-INF/manifest.mf";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String fileName;
    private String directoryName;
    
    private List<FileEntry> entries = new LinkedList<FileEntry>();
    private int offset = 0;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setFile(final String fileName) {
        this.fileName = fileName;
    }
    
    public void setDirectory(final String directoryName) {
        this.directoryName = directoryName;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        AntUtils.setProject(getProject());
        
        String string       = null;
        
        File file      = new File(fileName);
        File directory = new File(directoryName);
        
        JarOutputStream output = null;
        try {
            output = new JarOutputStream(new FileOutputStream(file));
            output.setLevel(9);
            
            log("browsing, packing, archiving directory");
            offset = directory.getCanonicalPath().length();
            browse(directory.getCanonicalFile(), output);
            
            log("adding manifest and files list");
            output.putNextEntry(new JarEntry(METAINF_ENTRY));
            
            output.putNextEntry(new JarEntry(MANIFEST_ENTRY));
            output.write("Manifest-Version: 1.0\n\n".getBytes("UTF-8"));
            
            output.putNextEntry(new JarEntry(FILES_LIST_ENTRY));
            OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
            
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<files-list>\n");
            for (FileEntry entry: entries) {
                writer.write("    <entry ");
                if (entry.isDirectory()) {
                    writer.write("type=\"directory\" ");
                    writer.write("empty=\"" + entry.isEmpty() + "\" ");
                    writer.write("modified=\"" + entry.getLastModified() + "\" ");
                    writer.write("permissions=\"" + entry.getPermissions() + "\"");
                } else {
                    writer.write("type=\"file\" ");
                    writer.write("size=\"" + entry.getSize() + "\" ");
                    writer.write("md5=\"" + entry.getMd5() + "\" ");
                    writer.write("jar=\"" + entry.isJarFile() + "\" ");
                    writer.write("packed=\"" + entry.isPackedJarFile() + "\" ");
                    writer.write("signed=\"" + entry.isSignedJarFile() + "\" ");
                    writer.write("modified=\"" + entry.getLastModified() + "\" ");
                    writer.write("permissions=\"" + entry.getPermissions() + "\"");
                }
                
                writer.write(">" + entry.
                        getName().
                        replace("&", "&amp;").
                        replace("<", "&lt;").
                        replace(">", "&gt;") + "</entry>\n");
            }
            writer.write("</files-list>\n");
            
            writer.flush();
            writer.close();
            
            output.flush();
            output.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    private void browse(File parent, JarOutputStream output) throws IOException {
        FileInputStream fis = null;
        List<File> toSkip = new LinkedList<File>();
        
        for (File child: parent.listFiles()) {
            if (toSkip.contains(child)) {
                log("    skipping " + child);
                continue;
            }
            
            log("    visiting " + child);
            
            String    path     = child.getAbsolutePath();
            String    name     = path.substring(offset + 1).replace('\\', '/');
            FileEntry entry    = null;
            JarEntry  jarEntry = null;
            
            if (child.isDirectory()) {
                log("        archiving directory: " + name);
                
                name  = name + "/";
                entry = new FileEntry(child, name);
                
                output.putNextEntry(new JarEntry(name));
                
                browse(child, output);
            } else {
                // if the source file comes in already packed, we need to unpack it 
                // first and then process normally
                if (child.getName().endsWith(".jar.pack.gz")) {
                    log("        it is a packed jar - attempting unpacking");
                    File unpacked = new File(child.getPath().substring(
                            0, 
                            child.getPath().length() - 8));
                    File temp = null;
                    
                    if (unpacked.exists()) {
                        temp = File.createTempFile(
                                "xxx", 
                                null, 
                                child.getParentFile());
                        temp.delete();
                        unpacked.renameTo(temp);
                    }
                    
                    if (AntUtils.unpack(child, unpacked)) {
                        child.delete();
                        if (temp != null) {
                            temp.delete();
                        }
                        child = unpacked.getAbsoluteFile();
                        
                        log("        successfully unpacked - processing file: " + child);
                    } else {
                        unpacked.delete();
                        if (temp != null) {
                            temp.renameTo(unpacked);
                        }
                    }
                }
                
                entry = new FileEntry(child, name);
                
                if (entry.isJarFile() && !entry.isSignedJarFile()) {
                    File backup = new File(child.getPath() + ".bak");
                    File packed = new File(child.getPath() + ".pack.gz");
                    
                    // if the packed form of this jar already exists, we need to 
                    // clean it up
                    if (packed.exists()) {
                        log("        packed jar already exists - deleting it");
                        packed.delete();
                        toSkip.add(packed);
                    }
                    
                    AntUtils.copy(child, backup);
                    
                    if (AntUtils.pack(child, packed) &&
                            AntUtils.unpack(packed, child) &&
                            AntUtils.verify(child)) {
                        name  = packed.getPath().
                                substring(offset + 1).replace('\\', '/');
                        
                        entry = new FileEntry(child, name);
                        entry.setJarFile(true);
                        entry.setPackedJarFile(true);
                        entry.setSignedJarFile(false);
                        
                        child.delete();
                        backup.delete();
                        
                        child = packed;
                    } else {
                        AntUtils.copy(backup, child);
                        
                        packed.delete();
                        backup.delete();
                    }
                }
                
                log("        archiving file: " + name);
                jarEntry = new JarEntry(name);
                jarEntry.setTime(entry.getLastModified());
                jarEntry.setSize(entry.getSize());
                output.putNextEntry(jarEntry);
                
                fis = new FileInputStream(child);
                AntUtils.copy(fis, output);
                fis.close();
            }
            
            entries.add(entry);
        }
    }
}
