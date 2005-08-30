/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Provides sources for module build products.
 * XXX can we also translate xtest/lib/nbjunit.jar -> xtest/nbjunit/src/?
 */
public final class SourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    private final NbModuleProject project;
    private URL moduleJarUrl;
    private URL classesUrl;
    private URL testClassesUrl;
    private Map/*<URL, SourceForBinaryQuery.Result>*/ cache = new HashMap ();

    public SourceForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        //System.err.println("findSourceRoot: " + binaryRoot);
        SourceForBinaryQuery.Result res = (SourceForBinaryQuery.Result) cache.get (binaryRoot);
        if (res == null) {
            URL binaryJar = FileUtil.getArchiveFile(binaryRoot);
            if (binaryJar != null) {
                File binaryJarF = new File(URI.create(binaryJar.toExternalForm()));
                File moduleJarF = new File(URI.create(
                        FileUtil.getArchiveFile(getModuleJarUrl()).toExternalForm()));
                // XXX should be more strict (e.g. compare also clusters)
                if (binaryJarF.getName().equals(moduleJarF.getName())) {
                    FileObject srcDir = project.getSourceDirectory();
                    //System.err.println("\t-> " + srcDir);
                    if (srcDir != null) {
                        res = new Result(new FileObject[] {srcDir});
                        return res;
                    }
                }
            }
            if (binaryRoot.equals(getClassesUrl())) {
                FileObject srcDir = project.getSourceDirectory();
                if (srcDir != null) {
                    res = new Result(new FileObject[] {srcDir});
                }
            } else if (binaryRoot.equals(getTestClassesUrl())) {
                FileObject testSrcDir = project.getTestSourceDirectory();
                if (testSrcDir != null) {
                    res = new Result (new FileObject[] {testSrcDir});
                }
            } else {
                // Check extra compilation units.
                Iterator/*<Map.Entry>*/ ecus = project.getExtraCompilationUnits().entrySet().iterator();
                ECUS: while (ecus.hasNext()) {
                    Map.Entry entry = (Map.Entry) ecus.next();
                    Element pkgrootEl = (Element) entry.getValue();
                    Iterator/*<Element>*/ pkgrootKids = Util.findSubElements(pkgrootEl).iterator();
                    while (pkgrootKids.hasNext()) {
                        Element kid = (Element) pkgrootKids.next();
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
                            res = new Result(new FileObject[] {(FileObject) entry.getKey()});
                            break ECUS;
                        }
                    }
                }
            }
            if (res != null) {
                this.cache.put(binaryRoot,res);
            }
        }
        return res;
    }
    
    private URL getModuleJarUrl() {
        if (moduleJarUrl == null) {
            File actualJar = project.getModuleJarLocation();
            moduleJarUrl = Util.urlForJar(actualJar);
            //System.err.println("Module JAR: " + moduleJarUrl);
        }
        return moduleJarUrl;
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
               
        private FileObject[] res;
        
        public Result (FileObject[] res) {
            this.res = res;
            assert res != null && !Arrays.asList(res).contains(null);
        }
        
        public FileObject[] getRoots () {
            return this.res;
        }
        
        public void addChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
        public void removeChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
    }
    
}
