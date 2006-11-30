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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;

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
        
        File   file         = new File(fileName);
        File   directory    = new File(directoryName);
        
        try {
            JarOutputStream output = new JarOutputStream(new FileOutputStream(file));
            
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
                    writer.write("is-empty=\"" + entry.isEmpty() + "\"");
                } else {
                    writer.write("type=\"file\" ");
                    writer.write("size=\"" + entry.getSize() + "\" ");
                    writer.write("modified=\"" + entry.getLastModified() + "\" ");
                    writer.write("md5=\"" + entry.getMd5() + "\" ");
                    writer.write("crc32=\"" + entry.getCrc32() + "\" ");
                    writer.write("sha1=\"" + entry.getSha1() + "\" ");
                    writer.write("packed-jar=\"" + entry.isPackedJarFile() + "\" ");
                    writer.write("signed-jar=\"" + entry.isSignedJarFile() + "\" ");
                }
                writer.write(">" + entry.getName() + "</entry>\n");
            }
            writer.write("</files-list>\n");
            
            output.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    private void browse(File parent, JarOutputStream output) throws IOException {
        FileInputStream fis = null;
        for (File child: parent.listFiles()) {
            log("    visiting " + child);
            
            String    childPath = child.getAbsolutePath();
            String    childName = childPath.substring(offset + 1).replace('\\', '/');
            
            FileEntry childEntry;
            JarEntry  jarEntry;
            if (child.isDirectory()) {
                childName = childName + "/";
                childEntry = new FileEntry(child, childName);
                
                log("        archiving directory: " + childName);
                output.putNextEntry(new JarEntry(childName));
                
                browse(child, output);
            } else {
                childEntry = new FileEntry(child, childName);
                
                if (childEntry.isJarFile() && !childEntry.isSignedJarFile()) {
                    File temp   = new File(child.getPath() + ".tmp");
                    File packed = new File(child.getPath() + ".pack.gz");
                    
                    if (AntUtils.pack(child, packed) && 
                            AntUtils.unpack(child, temp) && 
                            AntUtils.verify(temp)) {
                        child.delete();
                        temp.delete();
                        
                        child     = packed;
                        childName = packed.getPath().substring(offset + 1).replace('\\', '/');
                        
                        childEntry = new FileEntry(child, childName);
                        childEntry.setPackedJarFile(true);
                    } else {
                        packed.delete();
                        temp.delete();
                    }
                }
                
                log("        archiving file: " + childName);
                jarEntry = new JarEntry(childName);
                jarEntry.setTime(childEntry.getLastModified());
                jarEntry.setSize(childEntry.getSize());
                output.putNextEntry(jarEntry);
                
                fis = new FileInputStream(child);
                AntUtils.copy(fis, output);
                fis.close();
            }
            
            entries.add(childEntry);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class FileEntry {
        private long    size  = 0;
        
        private String  md5           = null;
        private String  sha1          = null;
        private String  crc32         = null;
        
        private boolean directory     = false;
        private boolean empty         = false;
        private boolean jarFile       = false;
        private boolean packedJarFile = false;
        private boolean signedJarFile = false;
        
        private long    lastModified  = 0;
        
        private String  name          = null;
        
        public FileEntry(File file, String name) throws IOException {
            this.directory     = file.isDirectory();
            
            if (!directory) {
                this.size          = file.length();
                
                this.md5           = AntUtils.getMd5(file);
                this.sha1          = AntUtils.getSha1(file);
                this.crc32         = AntUtils.getCrc32(file);
                
                this.jarFile       = AntUtils.isJarFile(file);
                if (jarFile) {
                    this.packedJarFile = false; // we cannot determine this
                    this.signedJarFile = AntUtils.isSigned(file);
                }
                
                this.lastModified  = file.lastModified();
                this.name          = name;
            } else {
                this.empty         = AntUtils.isEmpty(file);
            }
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public String getMd5() {
            return md5;
        }
        
        public void setMd5(String md5) {
            this.md5 = md5;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }
        
        public String getCrc32() {
            return crc32;
        }
        
        public void setCrc32(String crc32) {
            this.crc32 = crc32;
        }
        
        public boolean isDirectory() {
            return directory;
        }
        
        public void setDirectory(boolean directory) {
            this.directory = directory;
        }
        
        public boolean isEmpty() {
            return empty;
        }
        
        public void setEmpty(boolean empty) {
            this.empty = empty;
        }
        
        public boolean isJarFile() {
            return jarFile;
        }
        
        public void setJarFile(boolean jarFile) {
            this.jarFile = jarFile;
        }
        
        public boolean isPackedJarFile() {
            return packedJarFile;
        }
        
        public void setPackedJarFile(boolean packedJarFile) {
            this.packedJarFile = packedJarFile;
        }
        
        public boolean isSignedJarFile() {
            return signedJarFile;
        }
        
        public void setSignedJarFile(boolean signedJarFile) {
            this.signedJarFile = signedJarFile;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
