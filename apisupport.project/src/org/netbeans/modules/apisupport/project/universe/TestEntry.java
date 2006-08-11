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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Representation of jarfile with tests 
 */
public final class TestEntry {
    private static final String JAR_NAME = "tests.jar"; // NOI18N
    private static final String QA_FUNCTIONAL = "qa-functional"; // NOI18N
    private static final String UNIT = "unit"; // NOI18N;
    // hardcoded location of testdistribution relatively to nb cvs
    private static final String TEST_DIST_DIR = "nbbuild/build/testdist"; // NOI18N;
    private final String codeNameBase;
    private boolean unit;
    private final String cluster;
    
    private final File jarFile;
    /**
     * Creates a new instance of TestEntry
     */
    private TestEntry(File jarFile,String codeNameBase,boolean unit,String cluster) {
        this.jarFile = jarFile;
        this.codeNameBase = codeNameBase;
        this.unit = unit;
        this.cluster = cluster;
        
    }
    /**
     * get TestEntry for jarfile with tests
     * 
     * @param jarFile input file with tests
     * @return null when the file is not jarfile with tests
     */
    public static TestEntry get(File jarFile) {
        // testtype/cluster/codenamebase/testsjar
        String path = jarFile.getPath().replace(File.separatorChar,'/');
        if (path.endsWith(JAR_NAME)) {
            String tokens[] = path.split("/");
            int len = tokens.length;
            if (len > 3 ) {
               String cnb = tokens[len - 2].replace('-','.') ;
               String cluster = tokens[len - 3];
               String testType = tokens[len - 4];
               boolean unit = true;
               if (!testType.equals(UNIT)) {
                   if (testType.equals(QA_FUNCTIONAL)) {
                       unit = false;
                   } else {
                       return null;
                   }
               }
               return new TestEntry(jarFile,cnb,unit,cluster);
            }
        }
        return null;
    }  
    public String getCodeNameBase() {
        return codeNameBase;
    }

    public boolean isUnit() {
        return unit;
    }

    public String getCluster() {
        // set default cluster for modules in module suite
        return (cluster == null) ? "cluster" : cluster; // NOI18N
    }

    public File getJarFile() {
        return jarFile;
    }
    
    /** Get root folder of binary tests distribution.
     */
    public File getTestDistRoot() {
        return getJarFile().getParentFile().getParentFile().getParentFile().getParentFile();
    }
    
    /** Get source dir with tests.
     *  @return null if source dir was not located 
     */
    public URL getSrcDir() throws IOException {
        String nborgPath = getNetBeansOrgPath();
        if (nborgPath != null) {
            return new File(getNBCVSRoot(),nborgPath).toURL(); 
        } 
        File testDistDir = getTestDistRoot();
        Project prj = FileOwnerQuery.getOwner(FileUtil.toFileObject(testDistDir));
        if (prj != null) {
            // ModuleSuite
            SubprojectProvider subprojects = (SubprojectProvider) prj.getLookup().lookup(SubprojectProvider.class);
            if (subprojects != null) {
                Set  projects = subprojects.getSubprojects();
                for (Iterator it = projects.iterator() ; it.hasNext();) {
                    Project p = (Project)it.next();
                    if (p instanceof NbModuleProject) {
                    NbModuleProject nbm = (NbModuleProject) p;
                    if (nbm != null && nbm.getCodeNameBase().equals(getCodeNameBase())) {
                        FileObject file = (isUnit()) ? nbm.getTestSourceDirectory() : nbm.getFunctionalTestSourceDirectory();
                        if (file != null) {
                            return file.getURL();
                        }
                    } 
                    }
                }
            }
        }
        return null;
    }
    
    File getNBCVSRoot() {
        File rootDir = getTestDistRoot();
        String path = rootDir.getAbsolutePath().replace(File.separatorChar,'/');
        File nbcvs = null;
        // hardcoded location of testdistribution relatively to nb cvs
        if (path.endsWith(TEST_DIST_DIR)) { 
            nbcvs = rootDir.getParentFile().getParentFile().getParentFile();
        }
        return nbcvs;
    } 
    
    public String getNetBeansOrgPath () throws IOException {
        File nbcvs =  getNBCVSRoot();
        if (nbcvs != null && ModuleList.isNetBeansOrg(nbcvs) ) {
            ModuleList list = ModuleList.getModuleList(new File(nbcvs,"core")); // NOI18N
            ModuleEntry entry = list.getEntry(codeNameBase);
            if (entry == null) {
                return null;
            }
            return entry.getNetBeansOrgPath() + "/test/" + getTestType() + "/src";
        }
        return null;
    }

    public String getTestType() {
        return (isUnit()) ? UNIT : QA_FUNCTIONAL;
    }

    /** 
     *  Get project for TestEntry
     *  @return null when project was not found
     */
    public Project getProject() {
        try {
            URL  url = getSrcDir();
            if (url != null) {
                URI uri = url.toURI();
                if (uri != null) {
                    return FileOwnerQuery.getOwner(uri);
                }
            }
        } catch (IOException ex) {
            Util.err.notify(ex);
        } catch (URISyntaxException ex) {
            Util.err.notify(ex);
        }
        return null;
    }
} 
