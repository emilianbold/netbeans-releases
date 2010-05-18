/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.project.anttasks;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import org.netbeans.modules.wlm.project.anttasks.wsdl.WSDLOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;


/**
 * @author sgenipudi
 */
public class XformGenerator {

    private WSDLModel mClientWSDLModel = null;
    private File mProjectDir = null;
    private String mProjectName = null;
    private File mBuildDir = null;
    private static boolean mbCommandline = false;
    private boolean mAlways= false;
    private boolean mGenerate = false;


    public XformGenerator() {
    }

    public static void setCommandlineMode(boolean value) {
        mbCommandline = value;
    }

    public static boolean isCommandlineMode() {
        return mbCommandline;
    }

    public void generate(File projectDir, File buildDir, boolean always, boolean generate) {
        //Get the static WSDL File 
        //Build will copy the static wsdl to build directory 
        //assume the template is already there in the build directory.
        this.mProjectDir = projectDir;
        this.mBuildDir = buildDir;
        this.mAlways = always;
        this.mGenerate = generate;
        //Scan the project dir and process each workflow files
        //  Load Static Task Client WSDL Template
        //  Set defenition Name/ TargetName space in the defenitions

        // From Workflow file, 
        //  get the Operation
        // From Operation get the WSDL Model
        // From Operation 
        //   get the Input and 
        //           Output messages
        //  For each Input and Output message
        //      Get the part information
        //      Get the schema type for each part
        //      If the schema is File  then get the File information
        //      If this is inline schema then serialize the schema to File 
        //          and store the File Information
        //     Generate XForm based on schema

        //Implementation start:
        // Iterate the source directory and find out each Task Workflow files
        processSourceDir();

    }

    /**
     * Proces the source directory to generate JBI.xml
     * @param sourceDir
     */
    private void processSourceDir() {
        processFileObject(this.mProjectDir);
    }

    /**
     * Process the file object to generate JBI.xml
     * @param file BPEL file location
     */
    private void processFileObject(File file) {
        if (file.isDirectory()) {
            processFolder(file);
        } else {
            processFile(file);
        }
    }

    /**
     * Process the folder to generate JBI.xml
     * @param fileDir  Folder location
     */
    private void processFolder(File fileDir) {
        File[] children = fileDir.listFiles();

        for (int i = 0; i < children.length; i++) {
            processFileObject(children[i]);
        }
    }

    /**
     * Process the file to generate JBI.xml
     * @param file input file
     * @throws Exception 
     */
    protected void processFile(File file) {
        String fileName = file.getName();
        String fileExtension = null;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileExtension = fileName.substring(dotIndex + 1);
        }

        if (fileExtension != null && fileExtension.equalsIgnoreCase("wf")) {
            ArrayList<WSDLOperation> listOfOperations = 
                new ArrayList<WSDLOperation>();
            try {
                Util.generateXForm(file, mBuildDir, mProjectDir, mAlways, mGenerate);
            } catch (RuntimeException e) {
                // TODO Auto-generated catch block
                throw e;
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }

    }

 
}
