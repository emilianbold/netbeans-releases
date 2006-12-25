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
 * $Id$
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author Dmitry Lipin
 */
public class WriteFileList extends Task{
    private String dir;
    private String output;
    private String mask;
    
    private void check(String s, String  desc) throws BuildException {
        if(s==null) {
            throw new BuildException("Error! Parameter '" + desc + "' can`t be null!!");
        }
    }
    
    private void write(StringBuilder sb, String s) {
        sb.append(s);
        sb.append(System.getProperty("line.separator"));
        
    }
    private void listFile(File parent, File f, StringBuilder sb) throws IOException {
        String path = f.getPath();
        String parentPath = parent.getPath();
        path = path.substring(parentPath.length());
        path = path.replaceAll("\\\\","/");
        if(path.length()>0) {
            path = path.substring(1);
        }
        
        if(f.isFile()) {
            if(path.length()>0 && path.matches(mask)) {
                write(sb, path);
            }
        } else if(f.isDirectory()) {
            
            if(path.length()>0) {
                path = path + "/";
                if(path.matches(mask)) {
                    write(sb, path);
                }
            }
            File  [] dirs  = f.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.isDirectory());
                } }
            );
            
            for(File file: dirs) {
                listFile(parent, file, sb);
            }
            
            File  [] files  = f.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.isFile() && !pathname.isDirectory());
                } }
            );
            for(File file: files) {
                listFile(parent, file, sb);
            }
        }
    }
    public void execute() throws BuildException {
        check(dir,"starting directory");
        check(output,"output file");
        check(mask,"file mask");
        
        File root = new File(dir);
        File outFile = new File(output);
        FileOutputStream fos = null;
        
        try {
            StringBuilder sb = new StringBuilder();
            listFile(root, root, sb);
            fos = new FileOutputStream(outFile);
            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }
        }
        
    }
    
    public void setDir(final String dir) {
        this.dir = dir;
    }
    
    public void setOutput(final String output) {
        this.output = output;
    }
    
    public void setMask(final String mask) {
        this.mask = mask;
    }
    
}
