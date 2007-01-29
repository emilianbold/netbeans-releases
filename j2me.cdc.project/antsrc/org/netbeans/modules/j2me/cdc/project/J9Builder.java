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

package org.netbeans.modules.j2me.cdc.project;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class J9Builder extends Task {
    
    private File home;
    private String mainclass;
    private String args;
    private File dist;
    private String jvmargs;
    private String id;    
    private String platform;    
    private boolean xlet;
    private boolean applet;
    
    private List<FileSet> filesets = new LinkedList<FileSet>(); 
    
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }    
    
    public void execute() throws BuildException {      
        if (dist == null){
            throw new BuildException("Dist can not be null!");
        }
        
        assert id != null;
        assert mainclass != null;

        //StringBuffer sb = new StringBuffer();
        List<String> arguments = new ArrayList<String>();
        if (jvmargs != null && jvmargs.length() != 0){
            arguments.add(jvmargs);
        }

        if (!xlet){
            arguments.add("-cp");
            arguments.add(createPath());
            if (!applet){
                arguments.add(mainclass);
            } else if (applet){
                arguments.add("com.ibm.oti.appletviewer.AppletViewer");
            }
            
            if (args != null && args.length() != 0 && !applet){
                arguments.add(args);
            }            
        } else if (xlet){
            StringTokenizer st = new StringTokenizer(mainclass, ";");
            arguments.add("com.ibm.oti.xlet.XletApplicationManager");
            while(st.hasMoreElements()){
                arguments.add("-name:" + st.nextElement());
                arguments.add("-path:" + createPath());
                if (args != null && args.length() != 0 && !applet){
                    arguments.add("-args:" + args);
                }
            }
        } 
        
        if (applet){            
            arguments.add("file:/C:/private/" + id + "/applet.html");
            if (home == null || !home.exists()){
                throw new BuildException("Home does not exist!");
            }
            generateHtml();
        }
        
        //sb.append("\r\n");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dist);
            for (Iterator it = arguments.iterator(); it.hasNext(); ){
                fos.write(((String)it.next()).getBytes());
                if (it.hasNext()){
                    fos.write(" ".getBytes());
                }
            }
        } catch (IOException ex) {
            throw new BuildException("Can not write " + dist.toString(), ex);
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {
                throw new BuildException("Can not close " + dist.toString(), ex);
            }
        }
    }

    private String createPath(){
        StringBuffer sb = new StringBuffer();
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++){
                if ( platform.startsWith("semc")){
                    sb.append("C:\\private\\" + id + "\\" + files[i] + ";D:\\private\\" + id + "\\" + files[i]);
                } else if (platform.startsWith("nokia")){
                    sb.append(files[i]);
                } else {
                    throw new BuildException("Unknown target platform " + platform);
                }
                if (i+1 < files.length){
                    sb.append(";");
                }
            }
        }        
        return sb.toString();
    }
    
    private void generateHtml() throws BuildException {        
        File f = null; 
        if ( platform.startsWith("semc")){
            f = new File(home, "epoc32\\winscw\\c\\private\\" + id + "\\applet.html");
        } else if (platform.startsWith("nokia")){
            f = new File(home, "epoc32\\wins\\c\\PP_Applications\\applet" + id + ".html"); 
        } else {
            throw new BuildException("Unknown target platform " + platform);
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(f));
            pw.println("<HTML>"); 
            pw.println("<HEAD>"); 
            pw.println("<TITLE> A Simple Program </TITLE>"); 
            pw.println("</HEAD>"); 
            pw.println("<BODY>"); 
            pw.println("<APPLET CODE=\"" + mainclass + ".class\" WIDTH=240 HEIGHT=320>"); 
            pw.println("</APPLET>"); 
            pw.println("</BODY>");
            pw.println("</HTML>");      
        } catch (IOException ex) {
            throw new BuildException("HTML can not be writen");
        } finally {
            if (pw != null)
                pw.close();
        }
    }
    
    public String getMainclass() {
        return mainclass;
    }

    public void setMainclass(String mainclass) {
        this.mainclass = mainclass;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public File getDist() {
        return dist;
    }

    public void setDist(File dist) {
        this.dist = dist;
    }

    public String getJvmargs() {
        return jvmargs;
    }

    public void setJvmargs(String jvmargs) {
        this.jvmargs = jvmargs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isXlet() {
        return xlet;
    }

    public void setXlet(boolean xlet) {
        this.xlet = xlet;
    }

    public boolean isApplet() {
        return applet;
    }

    public void setApplet(boolean applet) {
        this.applet = applet;
    }

    public File getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform.toLowerCase();
    }
    
}
