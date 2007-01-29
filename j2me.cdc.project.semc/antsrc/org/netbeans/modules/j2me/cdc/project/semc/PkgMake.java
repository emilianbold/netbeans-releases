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

package org.netbeans.modules.j2me.cdc.project.semc;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class PkgMake extends Task {
    
    private File workdir;
    private String id;
    private String appname;
    private String appicon;
    private String vendor;
    private String version;
    private File cert;
    private File key;
    private File logo;
    private boolean logoInstallOnly;
    private String pass;
    private File dll;
    private List filesets = new LinkedList(); // List<FileSet>
    
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }    
    
    public void execute() throws BuildException {        
        BufferedReader br = null;
        StringBuffer newFileContent = new StringBuffer();
        File pkg = new File(workdir, "PProLauncher" + id + ".pkg");
        if (!pkg.exists()){
            throw new BuildException("PProLauncher" + id + ".pkg does not exists!");
        }
        if (!pkg.canWrite()){
            log("PProLauncher" + id + ".pkg can not be written!", Project.MSG_WARN);
            throw new BuildException("PProLauncher" + id + ".pkg can not be written!");
        }
        try {            
            br = new BufferedReader(new FileReader(pkg));
            String line;
            boolean found = false;
            Pattern vendorMatcher = Pattern.compile("^%.*$"); 
            Pattern headerMatcher = Pattern.compile("^#.*$"); 
            Pattern logoMatcher = Pattern.compile("^=.*$"); 
            Pattern iconMatcher = Pattern.compile("^\\\"" + id + ".mbm.*$"); 

            while ((line = br.readLine()) != null){
                if (line.indexOf(";\"app.jar\"") != -1){
                    found = true;
                    Iterator it = filesets.iterator();
                    while (it.hasNext()) {
                        FileSet fs = (FileSet)it.next();
                        DirectoryScanner ds = fs.getDirectoryScanner(project);
                        File basedir = ds.getBasedir();
                        String[] files = ds.getIncludedFiles();
                        for (int i = 0; i < files.length; i++){
                            //does the icon goes to \Private as well?
                            if (files[i].toLowerCase().endsWith("dll")){
                                newFileContent.append("\"" + files[i] + "\" - \"!:\\sys\\bin\\" +  files[i] + "\"\r\n");  
                            //} else if (files[i].toLowerCase().endsWith("mbm")){ //TODO!!!!
                            //  newFileContent.append("\"" + files[i] + "\" - \"!:\\Resource\\Apps\\" + id + "\\" +  files[i] + "\"\r\n");  
                            } else {
                                newFileContent.append("\"" + files[i] + "\" - \"!:\\Private\\" + id + "\\" +  files[i] + "\"\r\n");  
                            }
                        }
                    }                    
                    continue;
                }
                else if (headerMatcher.matcher(line).matches()){
                    Pattern versionVerifier = Pattern.compile("^\\d{1,2}[.]\\d{1,2}[.]\\d{1,2}$"); 
                    if (version == null || !versionVerifier.matcher(version.trim()).matches() ){
                        throw new BuildException("Property version is not set or have wrong format!");
                    }
                    version = version.trim().replace('.', ',');
                    newFileContent.append("#{\"" + appname + "\"}, (0x" + id.toUpperCase() + "), " + version + ", TYPE=SA\r\n");
                    continue;
                } else if (vendorMatcher.matcher(line).matches()) {//replace with vendor name
                    if (vendor == null){
                        throw new BuildException("Property vendor is not set!");
                    }
                    newFileContent.append("%{\"" + vendor + "\"}\r\n");
                    continue;
                } else if (logoMatcher.matcher(line).matches()) {
                    //only remove, will be added at the end of file
                    continue;                    
                } else if (iconMatcher.matcher(line).matches()) {
                    //only remove, will be added at the end of file
                    continue;
                }
                newFileContent.append(line);
                newFileContent.append("\r\n");
            }
        } catch (FileNotFoundException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            if (br != null)                
                try {
                    br.close();
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
        }
        if (appicon != null){
            File iconFile = new File(workdir, appicon);
            if (iconFile.exists()){ //verify if icon is ready for embedding
                newFileContent.append("; Icon added by NetBeans CDC Plugin\r\n");
                newFileContent.append("\"" + appicon + "\" - \"!:\\Resource\\Apps\\" +  appicon + "\"\r\n");  
            }
        }
        if (logo != null && logo.exists()){
            String ext = logo.toString().toLowerCase();
            int i = ext.lastIndexOf('.');
            if (i != -1){
                ext = ext.substring(i+1);
            }
            String mimeType = null;
            if (ext.endsWith("png")){
                mimeType = "image/png";
            } else if (ext.endsWith("gif")){
                mimeType = "image/gif";
            } else if (ext.endsWith("bmp")){
                mimeType = "image/bmp";
            } else if (ext.endsWith("mbm")){
                mimeType = "image/x-mbm";
            } else if (ext.endsWith("jpg")){
                mimeType = "image/jpeg";
            } else if (ext.endsWith("jpeg")){
                mimeType = "image/jpeg";
            } else {
                log("Unknown mime type for logo, skipping", Project.MSG_WARN);
            }
            
            if (mimeType != null){
                try {
                    File f  = new File(workdir, id + "-logo." + ext);
                    if (!f.exists())
                        copyFiles(logo, f);
                } catch (IOException ioEx){
                } 
                newFileContent.append("; Logo added by NetBeans CDC Plugin\r\n");
                if (logoInstallOnly){
                    newFileContent.append("=\"" + id + "-logo." + ext + "\", \"" + mimeType + "\", \"\"");
                } else {
                    newFileContent.append("=\"" + id + "-logo." + ext + "\", \"" + mimeType + "\", \"!:\\Private\\" + id + "\\" + id + "-logo." + ext + "\"");
                }
                newFileContent.append("\r\n");
            }
        }
        BufferedOutputStream bos = null;
        try {            
            bos = new BufferedOutputStream(new FileOutputStream(new File(pkg.getAbsolutePath())));
            bos.write(newFileContent.toString().getBytes());
        } catch (FileNotFoundException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            if (bos != null)                
                try {
                    bos.close();
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
        }

    }

    public File getWorkdir() {
        return workdir;
    }

    public void setWorkdir(File workdir) {
        this.workdir = workdir;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getAppicon() {
        return appicon;
    }

    public void setAppicon(String appicon) {
        this.appicon = appicon;
    }

    public File getCert() {
        return cert;
    }

    public void setCert(File cert) {
        this.cert = cert;
    }

    public File getKey() {
        return key;
    }

    public void setKey(File key) {
        this.key = key;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }  

    public File getDll() {
        return dll;
    }

    public void setDll(File dll) {
        this.dll = dll;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getLogo() {
        return logo;
    }

    public void setLogo(File logo) {
        this.logo = logo;
    }

    public boolean isLogoInstallOnly() {
        return logoInstallOnly;
    }

    public void setLogoInstallOnly(boolean logoInstallOnly) {
        this.logoInstallOnly = logoInstallOnly;
    }

    private static void copyFiles(File in, File out) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream( in ));
            bos = new BufferedOutputStream( new FileOutputStream( out ));
            byte[] data = new byte[1024];
            int read;
            while((read = bis.read(data)) != -1) {
                bos.write(data, 0, read);
            };
        } catch (IOException ioEx){
            throw ioEx;
        } finally {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
        }
    }
}
