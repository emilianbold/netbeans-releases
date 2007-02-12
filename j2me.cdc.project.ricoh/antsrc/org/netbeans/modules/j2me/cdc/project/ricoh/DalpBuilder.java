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

package org.netbeans.modules.j2me.cdc.project.ricoh;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.BuildException;


/**
 * @author suchys
 */
public class DalpBuilder extends Task {
    
    private List filesets = new LinkedList(); // List<FileSet>
    
    private boolean execMode = false;
    
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }    

    public void execute() throws BuildException {    
        Hashtable props = this.getProject().getProperties();
        PrintWriter pw = null;
                
        try {
            pw = new PrintWriter (new FileOutputStream (file));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            //pw.println("<dalp dsdk=\"\" version=\"0.10\">");  
            pw.println("<dalp dsdk=\"\" version=\"" + getProject().getProperty("ricoh.dalp.version") + "\" spec=\"" + getProject().getProperty("ricoh.dalp.resources.dsdk.version")  + "\">");  
            
            pw.println("  <information>");
            pw.println("    <product-id>" + getProject().getProperty("ricoh.application.uid") + "</product-id>");
            pw.println("    <title>"      + getProject().getProperty("application.name")      + "</title>");
            pw.println("    <vendor>"     + getProject().getProperty("application.vendor")    + "</vendor>");
            if ("true".equals((getProject().getProperty("ricoh.dalp.information.is-icon-used"))) && (iconName != null))
            {
                  pw.println("    <icon href=\"./" + getProject().getProperty("dist.jar") + "\" basepath=\"current\" location=\"jar\">" + iconName + "</icon>");
//                pw.println("    <icon href=\"" + iconName + "\" location=\"jar\"/>");
            }
            else
            {
                  pw.println("    <abbreviation>"          + getProject().getProperty("ricoh.dalp.information.abbreviation") + "</abbreviation>");
            }
            pw.println("    <description>"                 + getProject().getProperty("application.description")        + "</description>");
            pw.println("    <description type=\"detail\">" + getProject().getProperty("application.description.detail") + "</description>");
            pw.println("    <telephone>"                   + getProject().getProperty("ricoh.application.telephone")    + "</telephone>");
            pw.println("    <fax>"                         + getProject().getProperty("ricoh.application.fax")          + "</fax>");
            pw.println("    <e-mail>"                      + getProject().getProperty("ricoh.application.email")        + "</e-mail>");
            pw.println("    <application-ver>"             + getProject().getProperty("deployment.number")            + "</application-ver>");
            pw.println("    <offline-allowed/>");
            pw.println("  </information>");
            
            pw.println("  <security>");
            pw.println("    <all-permissions/>");
            pw.println("  </security>");
            
            pw.println("  <resources>");
            //pw.println("    <dsdk version=\"1.0\"/>");
            pw.println("    <dsdk version=\"" + getProject().getProperty("ricoh.dalp.resources.dsdk.version") + "\"/>");
            if (execMode || "sdcard".equals(getProject().getProperty("ricoh.install-server.deploy-method")) || "httppost".equals(getProject().getProperty("ricoh.install-server.deploy-method")))
            {
                pw.println("    <jar href=\"./"   + getProject().getProperty("dist.jar") + 
                                 "\" version=\""  + getProject().getProperty("deployment.number") + 
                                 "\" basepath=\"current\" main=\"true\" />");
            }
            else
            {  
                pw.println("    <jar href=\"http://" + getProject().getProperty("ricoh.install-server") + 
                                    ":" + getProject().getProperty("ricoh.install-server.web-port") + 
                                          getProject().getProperty("ricoh.install-server.path") + 
                                    "/" + getProject().getProperty("dist.jar") +
                                    "\" version=\""  + getProject().getProperty("deployment.number") + 
                                    "\" basepath=\"current\" main=\"true\" />");
            }
            
            addLibraries(pw);
            if ((getProject().getProperty("main.class") != null) || ("".equals(getProject().getProperty("main.class")) == false))
                pw.println("    <encode-file>" + getProject().getProperty("main.class").toLowerCase() + "</encode-file>");
            pw.println("  </resources>");
            
            String appTypeStr, mainClassStr, visibleStr;
            appTypeStr = getProject().getProperty("ricoh.application.type");
            if ("xlet".equals(appTypeStr))
            {
                mainClassStr = getProject().getProperty("main.class");
                visibleStr   = getProject().getProperty("ricoh.dalp.application-desc.visible");
            }
            else
            {
                appTypeStr = "server";
                mainClassStr = "";
                visibleStr   = "false";
            }
            
            pw.println("  <application-desc type=\""       + appTypeStr +
                                        "\" main-class=\"" + mainClassStr + 
                                        "\" visible=\""    + visibleStr + "\">");
            String args = getProject().getProperty("application.args");
            StringTokenizer st = new StringTokenizer(args, " ");
            
            //might work, might not, unsure
            while (st.hasMoreTokens()){
                pw.println("    <argument>" + st.nextToken() + "</argument>");
            }                    
            
            pw.println("  </application-desc>");
            
            String dsdkVerProp = getProject().getProperty("ricoh.dalp.resources.dsdk.version");
            
            if ("1.0".equals(dsdkVerProp))
            {
                pw.println("  <install mode=\""         + getProject().getProperty("ricoh.dalp.install.mode") +
                                   "\" destination=\""  + getProject().getProperty("ricoh.dalp.install.destination") + "\"/>");    
            }
            else
            {
                pw.println("  <install mode=\""         + getProject().getProperty("ricoh.dalp.install.mode") +
                                   "\" destination=\""  + getProject().getProperty("ricoh.dalp.install.destination") + 
                                   "\" work-dir=\""     + getProject().getProperty("ricoh.dalp.install.work-dir") + "\"/>");
            }
            
            pw.println("</dalp>");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        } 
        finally 
        {
            if (pw != null)
                pw.close();
        }
    }
        
    private File file;
    private String iconName;
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setExecMode(boolean execMode) {
        this.execMode = execMode;
    }
    
    public String getIconName(){
        return iconName;
    }
    
    public void setIconName(String iconName){
        this.iconName = iconName;
    }

    private void addLibraries(PrintWriter pw) {
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            boolean isRemoteDeploy = !("sdcard".equals(getProject().getProperty("ricoh.install-server.deploy-method")) || "httppost".equals(getProject().getProperty("ricoh.install-server.deploy-method")));
            for (int i = 0; i < files.length; i++)
            {
                if (isRemoteDeploy)
                    pw.println("    <jar href=\"http://" + getProject().getProperty("ricoh.install-server") + 
                                    ":" + getProject().getProperty("ricoh.install-server.web-port") + 
                                          getProject().getProperty("ricoh.install-server.path") + 
                                                "/" + files[i] + "\" version=\"" + getProject().getProperty("deployment.number") + "\" basepath=\"current\" />");                
                else
                    pw.println("    <jar href=\"./" + files[i] + "\" version=\"" + getProject().getProperty("deployment.number") + "\" basepath=\"current\" />");                
            }
        }        
    }
}
