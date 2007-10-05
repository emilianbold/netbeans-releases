/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * JarFinder.java
 *
 * Created on November 26, 2001, 3:46 PM
 */

package org.netbeans.xtest;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.io.File;

/** This task finds files from attribute 'files' in directories from attribute
 * 'dirs' and creates path which writes to property which name is in attribute 'property'.
 *
 * Example:
 *
 * Suppose directory c:\\jemmy contains files jemmy.jar and jelly.jar and
 * directory c:\\jdbcdrivers contains file driver.zip
 *
 * <jar-finder dirs="c:\\jemmy;c:\\jdbcdrivers" files="driver.zip;jemmy.jar;jelly.jar" 
 *             property="myclasspath"/>
 *
 * property myclasspath will contain "c:\\jdbcdrivers\\driver.zip;c:\\jemmy\\jemmy.jar;c:\\jemmy\\jelly.jar"
 *
 * @author lm97939
 */
public class JarFinder extends Task {

    private String dirlist,filelist,property;
    
    public void setDirs(String d) {
        dirlist = d;
    }
    
    public void setFiles(String f) {
        filelist=f;
    }
    
    public void setProperty(String p) {
        property = p;
    }

    public void execute() throws BuildException {
        if (dirlist == null) throw new BuildException("Attribute dirs is empty.");
        if (filelist == null) throw new BuildException("Attribute files is empty.");
        if (property == null) throw new BuildException("Attribute property is empty.");
        
        StringBuffer buffer = new StringBuffer();
        StringTokenizer filetokens = new StringTokenizer(filelist,",;"+File.pathSeparator);
        boolean found = false;
        while (filetokens.hasMoreTokens()) {
            found = false;
            String file = filetokens.nextToken();
            File ffile = getProject().resolveFile(file);
            if (ffile.exists()) {
                found = true;
                if (buffer.length() > 0) buffer.append(File.pathSeparator);
                buffer.append(ffile.getAbsolutePath());
                continue;
            }
            StringTokenizer dirtokens = new StringTokenizer(dirlist,",;"+File.pathSeparator);
            while (dirtokens.hasMoreTokens()) {
                String dir = dirtokens.nextToken();
                File fdir = getProject().resolveFile(dir);
                if (!fdir.exists() || !fdir.isDirectory()) 
                    throw new BuildException("Directory " + fdir.getAbsolutePath() + " not found (from '" + dirlist + "').");
                ffile = new File(fdir,file);
                if (ffile.exists()) {
                    found = true;
                    if (buffer.length() > 0) buffer.append(File.pathSeparator);
                    buffer.append(ffile.getAbsolutePath());
                    break;
                }
            }
            if (found == false) 
                throw new BuildException("File "+file+" was not found in any directory of "+dirlist+".");
        }
        log ("Setting property " + property + " to " + buffer.toString());
        getProject().setProperty(property,buffer.toString());
    }

}
