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

package org.netbeans.modules.j2me.cdc.project.semc;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide/ant/lib/ant.jar from your IDE installation directory (or any
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import org.apache.tools.ant.*;

/** 
 * @author suchys
 */
public class IconAssemblyTask extends Task {
    
    private File home;
    private String uid;
    private String count;
    
    public void execute() throws BuildException {
        if (home == null)
            throw new BuildException("Home is null!");
        
        if (count != null && (count.length() == 0 || "0".equals(count) || count.indexOf("${") != -1)){
            count = null;
        }
        
        try {
            log("Searching PProLauncher00000000sc.rls.template", Project.MSG_VERBOSE);
            File rlsTemplate = new File(home, "epoc32\\tools\\ppro-custom-launcher\\templates\\PProLauncher00000000sc.rls.template");
            File rlsTemplateOrig = new File(home, "epoc32\\tools\\ppro-custom-launcher\\templates\\PProLauncher00000000sc.rls.template.orig");
            
            if (!rlsTemplate.exists()){
            log("PProLauncher00000000sc.rls.template was not found", Project.MSG_WARN);
                return;
            }
            File tmpFile = File.createTempFile("PProLauncher00000000sc.rls.template", "tmp");
            //TODO - any number of spaces
            Pattern patternDriveRemapping = Pattern.compile("^.*rls_string STRING_r_icon_file +.*$"); 
            BufferedReader br = new BufferedReader( new FileReader(rlsTemplate) );
            PrintWriter pw = new PrintWriter (new FileWriter(tmpFile));
            String line;
            boolean found = false;
            while((line = br.readLine()) != null){
                if ( patternDriveRemapping.matcher(line).matches()){
                    log("rls_string STRING_r_icon_file pattern found", Project.MSG_VERBOSE);
                    if ( count != null )
                        pw.println("rls_string STRING_r_icon_file \"z:\\\\resource\\\\apps\\\\" + uid + ".mbm\"");
                    else 
                        pw.println("//rls_string STRING_r_icon_file \"z:\\\\resource\\\\apps\\\\" + uid + ".mbm\"");
                    found = true;
                } else {
                    pw.println(line);
                }
            }
            if (!found && count != null){
                pw.println();
                pw.println("/*&");
                pw.println("reference to icon - added by CDC plugin for NetBeans IDE");
                pw.println("*/");
                pw.println("rls_string STRING_r_icon_file \"z:\\\\resource\\\\apps\\\\" + uid + ".mbm\"");
                pw.println();
            }

            br.close();
            pw.close();

            File rssTemplate = new File(home, "epoc32\\tools\\ppro-custom-launcher\\templates\\PProLauncher00000000_loc.rss.template");
            File rssTemplateOrig = new File(home, "epoc32\\tools\\ppro-custom-launcher\\templates\\PProLauncher00000000_loc.rss.template.orig");
            if (!rssTemplate.exists()){
            log("PProLauncher00000000_loc.rss.template was not found", Project.MSG_WARN);
                return;
            }
            File tmpFile2 = File.createTempFile("PProLauncher00000000_loc.rss.template", "tmp");

            //TODO number of spaces!
    //        Pattern caption = Pattern.compile("^\\s*caption = STRING_r_caption +.*$"); 
            Pattern numOfIcons = Pattern.compile("^.*number_of_icons +.*$"); 
            Pattern iconFile = Pattern.compile("^.*icon_file +.*$"); 

            br = new BufferedReader( new FileReader(rssTemplate) );
            pw = new PrintWriter (new FileWriter(tmpFile2));
            while((line = br.readLine()) != null){
                if ( numOfIcons.matcher(line).matches()){                
                    log("number_of_icons pattern found", Project.MSG_VERBOSE);
                    if ( count != null ){
                        pw.println("\t\t\t\tnumber_of_icons = " + count + "; //modified by CDC plugin for NetBeans IDE");
                    } else {
                        pw.println("//\t\t\t\tnumber_of_icons = 0; //modified by CDC plugin for NetBeans IDE");
                    }
                } else if ( iconFile.matcher(line).matches() ){                
                    log("icon_file pattern found", Project.MSG_VERBOSE);
                    if ( count != null ){
                        pw.println("\t\t\t\ticon_file = STRING_r_icon_file; //modified by CDC plugin for NetBeans IDE");
                    } else {
                        pw.println("//\t\t\t\ticon_file = STRING_r_icon_file; //modified by CDC plugin for NetBeans IDE");
                    }
                } else {
                    pw.println(line);
                }

            }

            br.close();
            pw.close();

            if (!rlsTemplateOrig.exists() || !rssTemplateOrig.exists()){
                copyFiles(rlsTemplate, rlsTemplateOrig);
                copyFiles(rssTemplate, rssTemplateOrig);
                File note = new File(home, "epoc32\\tools\\ppro-custom-launcher\\templates\\README.txt");
                PrintWriter pwnote = new PrintWriter(new FileWriter(note));
                pwnote.println("Original files PProLauncher00000000sc.rls.template and PProLauncher00000000_loc.rss.template");
                pwnote.println("were renamed to PProLauncher00000000sc.rls.template.orig and PProLauncher00000000_loc.rss.template.orig");
                pwnote.println("by NetBeans IDE");
                pwnote.close();
            }
            
            //now copy the tmp file
            copyFiles(tmpFile, rlsTemplate);
            tmpFile.delete();

            copyFiles(tmpFile2, rssTemplate);
            tmpFile2.delete();
        } catch (IOException ioEx){
            throw new BuildException(ioEx.getMessage());
        }
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

    public File getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
    
}
