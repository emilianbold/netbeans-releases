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



package org.netbeans.modules.refactoring.java;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.junit.diff.LineDiff;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jiri Prox
 */
public abstract class RefactoringTestCase extends LogTestCase{
    
    /**
     * Map of files involved in refactoring. Files are stored as Strings as FQN + extension.
     */
    private Map<String,LinkedList<String>> refactoredFiles;
    
    /**
     * List of source files (and folders) in project before performing refactoring. Names stored as FQN + extension
     */
    private List<String> projectSources;
    
    /** Creates a new instance of DummyTest
     * @param name Name of current test
     */
    public RefactoringTestCase(String name) {
        super(name);
    }
    
    protected void addRefactoringElement(RefactoringElement element) {
        FileObject fo = element.getParentFile();
        String relPath = getRelativeFileName(fo);
        if(!refactoredFiles.keySet().contains(relPath)) { //new file
            try {
                File fBackUp = new File(getWorkDir(),getRelativeFileName(fo));
                File oFile = FileUtil.toFile(fo);
                if(!oFile.isDirectory()) copyFile(oFile, fBackUp);
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
            refactoredFiles.put(relPath, new LinkedList<String>());
        }
        List<String> list = refactoredFiles.get(relPath);
        list.add(element.getDisplayText());
    }
    
    protected void dumpRefactoredFiles() {
        for (String fileName: refactoredFiles.keySet()) {
            ref(fileName);
            ref("--------------------");
            for(String text : refactoredFiles.get(fileName)) {
                ref(text);
            }
            ref("--------------------");
            try {
                File actualFile = new File(classPathWorkDir,fqn2FilePath(fileName));
                File backupedFile = new File(getWorkDir(),fileName);
                if(backupedFile.exists()) {
                    log("Original file:");
                    log(backupedFile);
                }
                if(!actualFile.exists()) {
                    ref("File was deleted");
                } else {
                    log("Actual file:");
                    log(actualFile);
                    File diff = File.createTempFile("refactoring", null);
                    LineDiff ldiff = new LineDiff();
                    ldiff.diff(actualFile,backupedFile, diff);
                    ref(diff);
                    diff.delete();
                }
                ref("\n");
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }
    
    /**
     * Compares two list of sources and dumps a new files
     * @param originalSources Original projects sources
     * @param currentSources New projects sources
     */
    protected void dumpNewFiles(List<String> originalSources,List<String> currentSources) {
        log("Original sources: "+originalSources);
        log("Current sources: "+currentSources);
        for (String fileName : currentSources) {
            if(!originalSources.contains(fileName)) {
                File file = new File(classPathWorkDir,fqn2FilePath(fileName));
                ref(fileName);
                ref("--------------------");
                if(file.isDirectory()) ref("Directory created");
                else ref(file);
                ref("\n");
            }
        }
    }
    
    private void dumpDir(File dir, List<String> result) {
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.getName().startsWith(".")) continue;  // skipping hidden files
            result.add(getRelativeFileName(FileUtil.toFileObject(file)));
            if(file.isDirectory()) dumpDir(file, result);
        }
    }
    
    
    private List<String> getProjectSources() {
        List<String> list = new LinkedList<String>();
        dumpDir(classPathWorkDir, list);
        return list;
    }
    
    
    
    private String fqn2FilePath(String pkg) {
        if(pkg.endsWith(".java")) {
            String cutExtension  = pkg.substring(0, pkg.length()-5);
            return cutExtension.replace('.', '/')+".java";
        } else {
            return pkg.replace('.', '/');
        }
    }
    
    public boolean perform(AbstractRefactoring absRefactoring, ParameterSetter parameterSetter) {
        refactoredFiles = new TreeMap<String, LinkedList<String>>();
        projectSources = getProjectSources();
        Problem problem = absRefactoring.preCheck();
        boolean fatal = false;
        while(problem!=null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if(fatal) return  false;
        parameterSetter.setParameters();
        problem = absRefactoring.fastCheckParameters();
        while(problem!=null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if(fatal) return  false;
        problem = absRefactoring.checkParameters();
        while(problem!=null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if(fatal) return  false;
        RefactoringSession rs = RefactoringSession.create("Session");
        try {
            absRefactoring.prepare(rs);
            Collection<RefactoringElement> elems = rs.getRefactoringElements();
            for (RefactoringElement refactoringElement : elems) {
                addRefactoringElement(refactoringElement);
            }
            rs.doRefactoring(true);
            dumpRefactoredFiles();
            dumpNewFiles(projectSources, getProjectSources());
        } catch (Throwable t) {
            t.printStackTrace();
            t.printStackTrace(log);            
        }
        return true;
    }
    
}
