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

package org.netbeans.modules.j2me.cdc.project.savaje;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class SavajeJnlpBuilder extends Task {
    private File dir;
    private String file;
    
    private String codebase;
    private String extensionBase;
    
    private String distJar;
    private String smallIcon;
    private String focusedIcon;
    
    private String applicationTitle;
    private String applicationVendor;
    private String applicationIcon;
    private String applicationDescription;
    private String mainClass;
    private String applicationArgs;
    private boolean debug;
    private String  debugPort;
    
    private List filesets = new LinkedList(); // List<FileSet>
    
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }    

    public void setDir(File dir) {
        this.dir = dir;
    }
    
    public void setFile(String file) {
        this.file = file;
    }
    
    public void setCodebase(String codebase) {
        this.codebase = codebase;
    }
    
    public void setExtensionBase(String extensionBase) {
        this.extensionBase = extensionBase;
    }
    
    public void setDistJar(String distJar) {
        this.distJar = distJar;
    }
    
    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }
    
    public void setFocusedIcon(String focusedIcon) {
        this.focusedIcon = focusedIcon;
    }
    
    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }
    
    public void setApplicationVendor(String applicationVendor) {
        this.applicationVendor = applicationVendor;
    }
    
    public void setApplicationIcon(String applicationIcon) {
        this.applicationIcon = applicationIcon;
    }
    
    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }
    
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    
    public void setApplicationArgs(String applicationArgs) {
        this.applicationArgs = applicationArgs;
    }
    
    
    public void execute() throws BuildException {
        if (!dir.exists()) throw new BuildException("Target directory does not exist");
        if (!dir.isDirectory()) throw new BuildException("Target directory is a file");
        
        if (file == null) throw new BuildException("Target file name is not specified");
        if (distJar == null) throw new BuildException("Target file name is not specified");
        if (codebase == null) throw new BuildException("Codebase is not specified");
        //if (extensionBase == null) throw new BuildException("Extension base is not specified");
        if (applicationTitle == null) throw new BuildException("Application title is not specified");
        if (applicationVendor == null) throw new BuildException("Application vendor is not specified");
        if (applicationDescription == null) throw new BuildException("Application icon is not specified");
        if (mainClass == null) throw new BuildException("main class is not specified");
        
        final File targetFile = new File(dir, file);
        final FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(targetFile);
            final PrintWriter printW = new PrintWriter(fileWriter);
            
            printW.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
            printW.println("<jnlp codebase=\"sb:///" + codebase + "/\">");
            printW.println();
            printW.println("  <resources>");
            printW.println("    <j2se version=\"1.4+\"/>");
            printW.println("    <jar href=\"lib/classes.jar\"/>");
            if (smallIcon != null && smallIcon.length() != 0 && smallIcon.indexOf("${") == -1 )
                printW.println("    <property name=\"icon.small\" value=\"" + smallIcon + "\"/>");
            if (focusedIcon != null && focusedIcon.length() != 0 && focusedIcon.indexOf("${") == -1)
                printW.println("    <property name=\"icon.focused\" value=\"" + focusedIcon + "\"/>");
            //printW.println("    <extension href=\"" + extensionBase + file + "\"/>");
            printW.println("  </resources>");
            printW.println();
            if (debug && debugPort != null && debugPort.length() != 0){
                this.getProject().log("Adding debug information to jnlp file.");
                printW.println("  <resources os=\"savaJe\">");
                printW.println("     <property name=\"flag\" value=\"-g\"/>");
                printW.println("  </resources>");
                printW.println();
                
                String dbg = "port=" + debugPort + " suspend=y "; //NOI18N (note there must be a whitespace at the end!
                if (applicationArgs == null){
                    applicationArgs = dbg;
                } else {
                    applicationArgs = dbg + applicationArgs;
                }
            }
            printW.println("  <information>");
            printW.println("    <title>" + applicationTitle + "</title>");
            printW.println("    <vendor>" + applicationVendor + "</vendor>");
            if (applicationIcon != null && applicationIcon.length() != 0 && applicationIcon.indexOf("${") == -1 ){
                printW.println("    <icon href=\"" + applicationIcon + "\"/>");
            }
            if (applicationDescription.length() != 0)
                printW.println("    <description>" + applicationDescription + "</description>");
            printW.println("  </information>");
            printW.println();
            printW.println("  <application-desc main-class=\"" + mainClass + "\">");
            if (applicationArgs != null) {
                final StringTokenizer stringTokenizer = new StringTokenizer(applicationArgs, " ");
                while (stringTokenizer.hasMoreTokens()) {
                    printW.println("    <argument>" + stringTokenizer.nextToken() + "</argument>");
                }
            }
            printW.println("  </application-desc>");
            printW.println();
            printW.println("</jnlp>");
            
            printW.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setDebugPort(String debugPort) {
        this.debugPort = debugPort;
    }
}
