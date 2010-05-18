/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2me.cdc.project;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide/ant/lib/ant.jar from your IDE installation directory (or any
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
    private String jarName;
    
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
        if (jarName != null) {
            if ( platform.startsWith("semc")){
                sb.append("C:\\private\\" + id + "\\" + jarName + ";D:\\private\\" + id + "\\" + jarName);
            } else if (platform.startsWith("nokia")){
                sb.append(jarName);
            } else {
                throw new BuildException("Unknown target platform " + platform);
            }
        }
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++){
                if (sb.length() > 0) sb.append(";");
                if ( platform.startsWith("semc")){
                    sb.append("C:\\private\\" + id + "\\" + files[i] + ";D:\\private\\" + id + "\\" + files[i]);
                } else if (platform.startsWith("nokia")){
                    sb.append(files[i]);
                } else {
                    throw new BuildException("Unknown target platform " + platform);
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
    
    public void setJarName(String jarName) {
    	this.jarName = jarName;
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
