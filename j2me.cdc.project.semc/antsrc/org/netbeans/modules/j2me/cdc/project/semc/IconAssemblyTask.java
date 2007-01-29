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
