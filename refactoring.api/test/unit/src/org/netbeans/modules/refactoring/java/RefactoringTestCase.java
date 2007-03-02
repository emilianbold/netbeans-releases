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
    
    public interface ParameterSetter {
        
        void setParameters();
    }
    
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
                copyFile(oFile, fBackUp);
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
                log("Original file:");
                log(backupedFile);                                
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
            ref.print(problem.getMessage());
            if(problem.isFatal()) fatal = true;
            problem = problem.getNext();
        }
        if(fatal) return  false;
        parameterSetter.setParameters();
        problem = absRefactoring.fastCheckParameters();
        while(problem!=null) {
            ref.print(problem.getMessage());
            if(problem.isFatal()) fatal = true;
            problem = problem.getNext();
        }
        if(fatal) return  false;
        problem = absRefactoring.checkParameters();
        while(problem!=null) {
            ref.print(problem.getMessage());
            if(problem.isFatal()) fatal = true;
            problem = problem.getNext();
        }
        if(fatal) return  false;
        RefactoringSession rs = RefactoringSession.create("Session");
        absRefactoring.prepare(rs);
        Collection<RefactoringElement> elems = rs.getRefactoringElements();
        for (RefactoringElement refactoringElement : elems) {
            addRefactoringElement(refactoringElement);
        }
        rs.doRefactoring(true);
        dumpRefactoredFiles();
        dumpNewFiles(projectSources, getProjectSources());
        return true;
    }
    
}
