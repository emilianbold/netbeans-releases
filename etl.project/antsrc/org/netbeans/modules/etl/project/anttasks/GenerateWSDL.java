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
/*
 * GenerateWSDL.java
 *
 * Created on May 20, 2005, 7:47 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.netbeans.modules.etl.project.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

/**
 *
 */
public class GenerateWSDL extends Task {

    private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private boolean buildFailed = false;

    /** Creates a new instance of GenerateWSDL */
    public GenerateWSDL() {
    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return mSrcDirectoryLocation;
    }

    /**
     * @param srcDirectoryLocation
     *            The srcDirectoryLocation to set.
     */
    public void setSrcDirectoryLocation(String srcDirectoryLocation) {
        mSrcDirectoryLocation = srcDirectoryLocation;
    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getBuildDirectoryLocation() {
        return mBuildDirectoryLocation;
    }

    /**
     * @param buildDirectoryLocation
     *            The srcDirectoryLocation to set.
     */
    public void setBuildDirectoryLocation(String buildDirectoryLocation) {
        mBuildDirectoryLocation = buildDirectoryLocation;
    }

    public static List getFilesRecursively(File dir, FileFilter filter) {
        List ret = new ArrayList();
        if (!dir.isDirectory()) {
            return ret;
        }
        File[] fileNdirs = dir.listFiles(filter);
        for (int i = 0, I = fileNdirs.length; i < I; i++) {
            if (fileNdirs[i].isDirectory()) {
                ret.addAll(getFilesRecursively(fileNdirs[i], filter));
            } else {
                ret.add(fileNdirs[i]);
            }
        }
        return ret;
    }

    public static List getFilesRecursively(File dir, String[] extensions) {
        FileFilter filter = null;
        if (extensions[0].equals(".etl")) {
            filter = new ETLExtensionFilter(extensions);
        } else {
            filter = new EngineExtensionFilter(extensions);
        }
        return getFilesRecursively(dir, filter);
    }

    public void execute() throws BuildException {
        File srcDir = new File(mSrcDirectoryLocation);
        File bulldDir = getBuildDir();
        if (!srcDir.exists()) {
            throw new BuildException("Directory " + mSrcDirectoryLocation + " does not exit.");
        }

        String[] etlExt = new String[]{".etl"};
        List etlFiles = getFilesRecursively(srcDir, etlExt);

        generateEngineFiles(etlFiles);

        String[] engineExt = new String[]{"_engine.xml"};
        List etlEngineFiles = getFilesRecursively(bulldDir, engineExt);
        Map<String, Definition> wsdlMap = new HashMap<String, Definition>();

        try {
            for (int i = 0, I = etlEngineFiles.size(); i < I; i++) {
                File f = (File) etlEngineFiles.get(i);
                // TODO generate endpoint and jbi artifacts based on projectName
                // and Engine file name
                // String engineFileName =
                // srcDir.getAbsoluteFile().getParentFile().getName() +
                // f.getName().substring(0, f.getName().indexOf(".xml"));
                String engineFileName = f.getName().substring(0, f.getName().indexOf(".xml"));

                WsdlGenerator wg = new WsdlGenerator(f, engineFileName, srcDir.getCanonicalPath());
                Definition def = wg.generateWsdl();
                wsdlMap.put(def.getQName().getLocalPart(), def);
            }

            ETLMapWriter ew = new ETLMapWriter(wsdlMap, bulldDir.getCanonicalPath());
            ew.writeMap();

            // todo: This needed to be done ONLY at the build time....

            if (bulldDir.exists()) {
                // create META-INF if needed..
                String metaDirFile = bulldDir.getCanonicalPath() + "/META-INF";
                File metaDir = new File(metaDirFile);
                if (!metaDir.exists()) {
                    metaDir.mkdir();
                }

            }
            JBIFileWriter fw = new JBIFileWriter(bulldDir.getCanonicalPath() + "/META-INF/jbi.xml",
                    bulldDir.getCanonicalPath() + "/etlmap.xml");
            fw.writeJBI();
            
            if(buildFailed) throw new BuildException("Unable to build one or more engine files.");
            
        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }
    }

    private void generateEngineFiles(List etlFiles) {
        Iterator iterator = etlFiles.iterator();
        EngineFileGenerator g = new EngineFileGenerator();
        while (iterator.hasNext()) {
            File element = (File) iterator.next();
            try {
                g.generateEngine(element, getBuildDir());                
            } catch (Exception e) {
                buildFailed = true;
                //TODO Log it
            }
        }
      
        //Cleanup ETL ClI Generator
        g.cleanupEtlCliGenerator();

    }

    private File getBuildDir() {
        File bulldDir = null;

        Project p = this.getProject();
        if (p != null) {
            String projPath = p.getProperty("basedir") + File.separator;
            String buildDirLoc = projPath + p.getProperty(IcanproProjectProperties.BUILD_DIR);
            bulldDir = new File(buildDirLoc);
        } else {
            bulldDir = new File(mBuildDirectoryLocation);
        }
        return bulldDir;
    }

    public static void main(String[] args) {
        GenerateWSDL task = new GenerateWSDL();
        task.setSrcDirectoryLocation("test");
        task.setBuildDirectoryLocation("test/build");
        task.execute();

    }
}
