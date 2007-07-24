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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Provides sources for module build products.
 * XXX can we also translate xtest/lib/nbjunit.jar -> xtest/nbjunit/src/?
 */
public final class SourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    private final NbModuleProject project;
    private String clusterPath;
    private URL classesUrl;
    private URL testClassesUrl;
    private Map<URL,SourceForBinaryQuery.Result> cache = new HashMap<URL,SourceForBinaryQuery.Result>();
    
    public SourceForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        //System.err.println("findSourceRoot: " + binaryRoot);
        SourceForBinaryQuery.Result res = cache.get(binaryRoot);
        if (res == null) {
            URL binaryJar = FileUtil.getArchiveFile(binaryRoot);
            if (binaryJar != null) {
                File binaryJarF = new File(URI.create(binaryJar.toExternalForm()));
                FileObject srcDir = null;
                if (binaryJarF.getAbsolutePath().endsWith(getModuleJarClusterPath())) {
                    srcDir = project.getSourceDirectory();
                } else {
                    // maybe tests.jar in testdistribution
                    TestEntry entry = TestEntry.get(binaryJarF);
                    if (entry != null && project.getCodeNameBase().equals(entry.getCodeNameBase())) {
                        srcDir = ( entry.isUnit() ) ? project.getTestSourceDirectory() : 
                               project.getFunctionalTestSourceDirectory();
                    }
                }
                if (srcDir != null) {
                    res = new Result(srcDir);
                    return res;
                }
            }
            if (binaryRoot.equals(getClassesUrl())) {
                FileObject srcDir = project.getSourceDirectory();
                if (srcDir != null) {
                    res = new Result(srcDir);
                }
            } else if (binaryRoot.equals(getTestClassesUrl())) {
                FileObject testSrcDir = project.getTestSourceDirectory();
                if (testSrcDir != null) {
                    res = new Result(testSrcDir);
                }
            } else {
                // Check extra compilation units.
                ECUS: for (Map.Entry<FileObject,Element> entry : project.getExtraCompilationUnits().entrySet()) {
                    for (Element kid : Util.findSubElements(entry.getValue())) {
                        if (!kid.getLocalName().equals("built-to")) { // NOI18N
                            continue;
                        }
                        String rawtext = Util.findText(kid);
                        assert rawtext != null : "Null content for <built-to> in " + project;
                        String text = project.evaluator().evaluate(rawtext);
                        if (text == null) {
                            continue;
                        }
                        File loc = project.getHelper().resolveFile(text);
                        URL u = Util.urlForDirOrJar(loc);
                        if (u.equals(binaryRoot)) {
                            res = new Result(entry.getKey());
                            break ECUS;
                        }
                    }
                }
            }
            if (res != null) {
                cache.put(binaryRoot,res);
            }
        }
        return res;
    }
    
    private String getModuleJarClusterPath() {
        if (clusterPath == null) { // XXX should listen to changes on cluster property?
            File cluster = project.getHelper().resolveFile(project.evaluator().evaluate("${cluster}")); // NOI18N
            clusterPath = PropertyUtils.relativizeFile(cluster.getParentFile(), 
                   project.getModuleJarLocation()).replace('/', File.separatorChar);
        }
        return clusterPath;
    }
    
    private URL getClassesUrl() {
        if (classesUrl == null) {
            File classesDir = project.getClassesDirectory();
            classesUrl = Util.urlForDir(classesDir);
        }
        return classesUrl;
    }
    
    private URL getTestClassesUrl() {
        if (testClassesUrl == null && project.supportsUnitTests()) {
            File testClassesDir = project.getTestClassesDirectory();
            testClassesUrl = Util.urlForDir(testClassesDir);
        }
        return testClassesUrl;
    }
    
    
    private static class Result implements SourceForBinaryQuery.Result {
               
        private FileObject res;
        
        public Result(FileObject res) {
            assert res != null;
            this.res = res;
        }
        
        public FileObject[] getRoots () {
            return new FileObject[] {res};
        }
        
        public void addChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
        public void removeChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
    }
    
}
