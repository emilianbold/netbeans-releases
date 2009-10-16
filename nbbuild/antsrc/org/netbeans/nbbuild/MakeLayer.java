/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.UpToDate;

/** Create a fragment of a module's XML layer.
 *
 * @author  Michal Zlamal
 */
public class MakeLayer extends MatchingTask {

    private File dest = null;
    private File topdir = null;
    private boolean absolutePath = false;

    /** Target file containing list of all classes. */
    public void setDestfile(File f) {
        dest = f;
    }

    /** Set the top directory.
     * There should be subdirectories under this matching pacgages.
     */
    public void setTopdir (File t) {
        topdir = t;
    }
    
    /** Set whether there is absolute path in top dir or not
     * default value is false
     */
    public void setAbsolutePath( boolean absolutePath ) {
        this.absolutePath = absolutePath;
    }

    
    public void execute()  throws BuildException {
        if (topdir == null) {
            throw new BuildException("You must set at topdir attribute", getLocation());
        }
        if (dest == null) {
            throw new BuildException("You must specify output file", getLocation());
        }
        UpToDate upToDate = (UpToDate) this.getProject().createTask( "uptodate" ); //NOI18N
        fileset.setDir( topdir );
        upToDate.addSrcfiles( fileset );
        upToDate.setTargetFile( dest );
        upToDate.setProperty(dest.getAbsolutePath() + ".property"); //NOI18N
        upToDate.execute();
        if (this.getProject().getProperty(dest.getAbsolutePath() + ".property") != null) //NOI18N
            return;
        
        int lengthAdjust = (absolutePath) ? 0 : 1;
        FileWriter layerFile;
        try {
            layerFile = new FileWriter(dest);
        }
        catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
        
        FileScanner scanner = getDirectoryScanner (topdir);
        for (String file : scanner.getIncludedFiles()) {
            File aFileName = new File(topdir, file);
            try {
                layerFile.write(("<file name=\""+aFileName.getName()+"\"\n").replace(File.separatorChar,'/')); //NOI18N
                layerFile.write(("  url=\""+aFileName.getAbsolutePath().substring(topdir.getAbsolutePath().length()+lengthAdjust)+"\"/>\n").replace(File.separatorChar,'/')); //NOI18N
            }
            catch(IOException ex) {
                throw new BuildException("I/O error while writing layer file "+dest.getAbsolutePath(), ex, getLocation());
            }
        }
        
        try {
            layerFile.close();
        }
        catch (IOException e) {
            throw new BuildException("I/O error when trying to close layer file "+dest.getAbsolutePath(), e, getLocation());
        }
    }
}



