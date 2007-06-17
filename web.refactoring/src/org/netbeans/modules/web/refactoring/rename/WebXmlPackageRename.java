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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Handles package rename.
 *
 * @author Erno Mononen
 */
public class WebXmlPackageRename extends BaseWebXmlRename{
    
    /**
     * The folder or package being renamed.
     */
    private final FileObject pkg;
    private final RenameRefactoring rename;
    
    public WebXmlPackageRename(FileObject webDD, WebApp webModel, FileObject pkg, RenameRefactoring rename) {
        super(webDD, webModel);
        this.pkg = pkg;
        this.rename = rename;
    }
    
    private String getNewName(FileObject fo){
        String fqn = RefactoringUtil.getQualifiedName(fo);
        
        if (isPackage()){
            return rename.getNewName() + "." + getUnqualifiedName(fqn);
        }
        
        FileObject folder = rename.getRefactoringSource().lookup(FileObject.class);
        ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        FileObject root = classPath.findOwnerRoot(folder);
        
        String prefix = FileUtil.getRelativePath(root, folder.getParent()).replace('/','.');
        String oldName = buildName(prefix, folder.getName());
        String newName = buildName(prefix, rename.getNewName());
        int oldNameIndex = fqn.lastIndexOf(oldName) + oldName.length();
        return newName + fqn.substring(oldNameIndex, fqn.length());
                
    }
    
    private String getUnqualifiedName(String fqn){
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot <= 0){
            return fqn;
        }
        return fqn.substring(lastDot + 1);
    }
    
    private boolean isPackage(){
        return rename.getRefactoringSource().lookup(NonRecursiveFolder.class) != null;
    }
    
    private boolean isFolder(){
        FileObject folder = rename.getRefactoringSource().lookup(FileObject.class);
        return folder != null && folder.isFolder();
    }
    
    private void collectChildren(List<FileObject> result, FileObject fileObject){
        for(FileObject child : fileObject.getChildren()){
            if (RefactoringUtil.isJavaFile(child)){
                result.add(child);
            } else if (child.isFolder()){
                collectChildren(result, child);
            }
        }
    }
    private String buildName(String prefix, String name){
        if (prefix.length() == 0){
            return name;
        }
        return prefix + "." + name;
    }
    
    
    protected List<RenameItem> getRenameItems() {
        List<RenameItem> result = new ArrayList<BaseWebXmlRename.RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        collectChildren(fos, pkg);
        for (FileObject each : fos){
            String oldFqn = RefactoringUtil.getQualifiedName(each);
            String newFqn = getNewName(each);
            result.add(new RenameItem(newFqn, oldFqn));
        }
        return result;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}
