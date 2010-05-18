/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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

package org.netbeans.modules.j2me.cdc.project.ricoh;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.DOMElementWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author suchys
 */
public class DalpBuilder extends Task {
    
    private List filesets = new LinkedList(); // List<FileSet>
    
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
                if ("false".equals(getProject().getProperty("ricoh.icon.invalid"))){
                    pw.println("    <icon href=\"./" + getProject().getProperty("dist.jar") + "\" basepath=\"current\" location=\"jar\">" + iconName + "</icon>");
                }
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
            pw.println("    <application-ver>"             + getProject().getProperty("application.version")            + "</application-ver>");
            pw.println("    <offline-allowed/>");
            pw.println("  </information>");
            
            pw.println("  <security>");
            pw.println("    <all-permissions/>");
            pw.println("  </security>");
            
            pw.println("  <resources>");
            //pw.println("    <dsdk version=\"1.0\"/>");
            pw.println("    <dsdk version=\"" + getProject().getProperty("ricoh.dalp.resources.dsdk.version") + "\"/>");
            
            // we support just local installation
            {
                String version = getProject().getProperty("application.version") != null ? "version=\"" + getProject().getProperty("application.version") + "\"" : "";
                pw.println("    <jar href=\"./"   + getProject().getProperty("dist.jar") + "\" "
                                + version
                                + " basepath=\"current\" main=\"true\" />");
            }

            addLibraries(pw);
            if ((getProject().getProperty("main.class") != null) || ("".equals(getProject().getProperty("main.class")) == false))
                pw.println("    <encode-file>" + getProject().getProperty("main.class").toLowerCase() + "</encode-file>");
            pw.println("  </resources>");
            
            String mainClassStr, visibleStr;
            //We support just xlets
            mainClassStr = getProject().getProperty("main.class");
            visibleStr   = getProject().getProperty("ricoh.dalp.application-desc.visible");
            
            pw.println("  <application-desc type=\"xlet"   + 
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
            
            addDisplayModes(pw);
            
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

            String version = getProject().getProperty("application.version") != null ? "version=\"" + getProject().getProperty("application.version") + "\"" : "";
            for (int i = 0; i < files.length; i++)
            {
                pw.println("    <jar href=\"./" + files[i] + "\" " + version + " basepath=\"current\" />");                
            }
        }        
    }
       
    private void addDisplayModes(PrintWriter pw)
    {
        if ("COLOR".equals(getProject().getProperty("ricoh.dalp.display-mode.type")))
        {
            if (getProject().getProperty("ricoh.dalp.display-mode.is-hvga-support").equals("true"))
                pw.println("<display-mode size=\"HVGA\" type=\"COLOR\"/>");
            if (getProject().getProperty("ricoh.dalp.display-mode.is-vga-support").equals("true"))
                pw.println("<display-mode size=\"VGA\" type=\"COLOR\"/>");
            if (getProject().getProperty("ricoh.dalp.display-mode.is-wvga-support").equals("true"))
                pw.println("<display-mode size=\"WVGA\" type=\"COLOR\"/>");
            if (getProject().getProperty("ricoh.dalp.display-mode.is-4line-support").equals("true"))
                pw.println("<display-mode size=\"4LINE\" type=\"COLOR\"/>");
        }
        else
        if ("MONO".equals(getProject().getProperty("ricoh.dalp.display-mode.type")))
        {
            if (getProject().getProperty("ricoh.dalp.display-mode.is-hvga-support").equals("true"))
                pw.println("<display-mode size=\"HVGA\" type=\"MONO\"/>");
            if (getProject().getProperty("ricoh.dalp.display-mode.is-vga-support").equals("true"))
                pw.println("<display-mode size=\"VGA\" type=\"MONO\"/>");
            if (getProject().getProperty("ricoh.dalp.display-mode.is-wvga-support").equals("true"))
                pw.println("<display-mode size=\"WVGA\" type=\"MONO\"/>");
            if (getProject().getProperty("ricoh.dalp.display-mode.is-4line-support").equals("true"))
                pw.println("<display-mode size=\"4LINE\" type=\"MONO\"/>");
        }            
        else
        //default supports HVGA
            pw.println("<display-mode size=\"HVGA\" type=\"MONO\"/>");
    }
}
