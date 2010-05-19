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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.project.anttasks.cli;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.bpel.project.anttasks.util.PackageCatalogArtifacts;
import org.xml.sax.SAXException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import java.lang.reflect.Method;

public class CliGenerateCatalogTask extends Task {

    public void setClasspathRef(Reference reference) {
        myReference = reference;
    }

    public void setBuildDirectory(String buildDirectory) {
        myBuildDirectory = buildDirectory;
    }
    
    public void setSourceDirectory(String sourceDirectory) {
        mySourceDirectory = sourceDirectory;
    }
    
    public void generate() throws BuildException {
//System.out.println();
//System.out.println("  1");
        if (mySourceDirectory == null) {
            throw new BuildException("No directory is set for source files."); // NOI18N
        }
//System.out.println("  2");
        File sourceDirectory = new File(mySourceDirectory);
        File buildDirectory = new File(myBuildDirectory);
        
        try {
//System.out.println("  3");
            new PackageCatalogArtifacts().doCopy(sourceDirectory, buildDirectory);
//System.out.println("  4");
        }
        catch (SAXException e) {
            throw new BuildException("Failed to create an XML catalog.", e); // NOI18N
        }
        catch (IOException e) {
            throw new BuildException("Failed to create an XML catalog.", e); // NOI18N
        }
//System.out.println("  5");
//System.out.println();
    }

    @Override
    public void execute() throws BuildException { 
        try {
            myClassLoader = new AntClassLoader(); 
            initClassLoader();
            Class antTaskClass =  Class.forName("org.netbeans.modules.bpel.project.anttasks.cli.CliGenerateCatalogTask", true, myClassLoader); // NOI18N
            Thread.currentThread().setContextClassLoader(myClassLoader);
            Object genJBIInstObj = antTaskClass.newInstance();

            Method driver = antTaskClass.getMethod("setBuildDirectory", new Class[] {String.class}); // NOI18N
            Object[] param = new Object[] {myBuildDirectory};
            driver.invoke(genJBIInstObj, param);
                       
            driver = antTaskClass.getMethod("setSourceDirectory", new Class[] {String.class}); // NOI18N
            param = new Object[] {mySourceDirectory};
            driver.invoke(genJBIInstObj, param);   

            driver = antTaskClass.getMethod("generate", (Class[]) null); // NOI18N
            driver.invoke(genJBIInstObj, (Object[]) null);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("Errors were found.", e); // NOI18N
        }
    }
    
    private void initClassLoader() {
        Path path = new Path(getProject());
        path.setRefid(myReference);
        
        Path parentPath = new Path(getProject());
        ClassLoader loader = getClass().getClassLoader();

        if (loader instanceof AntClassLoader) {
            parentPath.setPath(((AntClassLoader) loader).getClasspath());
            ((AntClassLoader) loader).setParent(null);
            parentPath.add(path);
            path = parentPath;
        }        
        myClassLoader.setClassPath(path);
        myClassLoader.setParent(null);
        myClassLoader.setParentFirst(false);
    }
    
    private Reference myReference;
    private String myBuildDirectory;
    private String mySourceDirectory;
    private AntClassLoader myClassLoader;
}
