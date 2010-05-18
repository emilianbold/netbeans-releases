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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class EpocIniPath extends Task {
    
    private File file;
    private File workingdir;    
    
    public void execute() throws BuildException {
        if (file == null){
            throw new BuildException("File is null");
        }
        if (!file.exists()){
            throw new BuildException("File epoc.ini does not exists");            
        }
        BufferedReader br = null;
        StringBuffer newFileContent = new StringBuffer();
        boolean found = false;
        String lineOrig = "";
        try {            
            Pattern patternDriveRemapping = Pattern.compile("^\\s*_epoc_drive_d +.*$"); 
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                if ( patternDriveRemapping.matcher(line).matches()){
                    found = true;
                    lineOrig = line;
                    line = "#" + line; //comment out 
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
        
        if (found && !file.canWrite()){
            log("epoc.ini can not be written! Make it writable or comment out line \'" + lineOrig + "\'", Project.MSG_WARN);
            throw new BuildException("epoc.ini can not be written!");
        }
        
        if (found){
            log("epoc.ini includes \'" + lineOrig + "\' remapping of epoc system path! Commenting out as it can cause execution trouble for certain application types", Project.MSG_WARN);
            BufferedOutputStream bos = null;
            try {            
                bos = new BufferedOutputStream(new FileOutputStream(new File(file.getAbsolutePath())));
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
   }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getWorkingdir() {
        return workingdir;
    }

    public void setWorkingdir(File workingdir) {
        this.workingdir = workingdir;
    }
    
}
