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
import java.util.*;
import java.util.ArrayList;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

/**
 * This class generating javadoc
 * @author  Michal Zlamal
 */
public class GenerateJavadoc extends Task
{
    private File dest;
    private List<String> modules = new ArrayList<String>();
    private String packageNames = null;
    private List<File> topdirs = new ArrayList<File>();

    /** Target directory to unpack to (top of IDE installation). */
    public void setDestdir(File f) {
        dest = f;
    }

    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ",");
        modules = new ArrayList<String>();
        while (tok.hasMoreTokens ())
            modules.add(tok.nextToken ());
    }

    /** Comma-separated list of modules to include. */
    public void setPackageNames (String s) {
        packageNames = s;
    }

    /** Set the top directory.
     * There should be subdirectories under this for each named module.
     */
    public void setTopdir (File t) {
        topdirs.add (t);
    }

    /** Nested topdir addition. */
    public class Topdir {
        /** Path to an extra topdir. */
        public void setPath (File t) {
            topdirs.add (t);
        }
    }

    /** Add a nested topdir.
     * If there is more than one topdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later topdirs may override build
     * products in earlier topdirs.)
     */
    public Topdir createTopdir () {
        return new Topdir ();
    }

    public void execute () throws BuildException {

        if (topdirs.isEmpty ()) {
            throw new BuildException("You must set at least one topdir attribute", getLocation());
        }

/*        Delete delete = (Delete) project.createTask ("delete");

        delete.setDir (dest);
        delete.init ();
        delete.setLocation (location);
        delete.execute ();*/
        Path path = new Path(getProject());
        for (File topdir : topdirs) {
            for (String module : modules) {
                File sources = new File (new File (topdir, module), "javadoc-temp/");
                if (! sources.exists ()) { //testing existination of 'javadoc-temp' dir if existing - skiping dafult source dirs...
                    sources = new File (new File (topdir, module), "src/");
                    if (sources.exists ())
                        path.append(new Path(getProject(), sources.getPath()));
                    sources = new File (new File (topdir, module), "libsrc/");
                    if (sources.exists ())
                        path.append(new Path(getProject(), sources.getPath()));
                }
                else {
                    path.append(new Path(getProject(), sources.getPath()));
                }
            }
        }
        Javadoc javaDoc = (Javadoc) getProject().createTask("javadoc");
        javaDoc.setSourcepath( path );
        
        javaDoc.setDestdir( dest );
        if (packageNames != null)
            javaDoc.setPackagenames(packageNames);
        else javaDoc.setPackagenames("org.netbeans.*,com.sun.*");
        javaDoc.setUse(true);
        javaDoc.setMaxmemory("256M");
        javaDoc.execute();

    }
}
