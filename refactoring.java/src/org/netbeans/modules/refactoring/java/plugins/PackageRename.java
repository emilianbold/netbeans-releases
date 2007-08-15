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
package org.netbeans.modules.refactoring.java.plugins;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class PackageRename implements RefactoringPluginFactory{
    
    /** Creates a new instance of PackageRename */
    public PackageRename() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof RenameRefactoring) {
            if (refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class)!=null) {
                return new PackageRenamePlugin((RenameRefactoring) refactoring);
            }
        }
        return null;
    }
    
    public class PackageRenamePlugin implements RefactoringPlugin {
        private RenameRefactoring refactoring;
        
        /** Creates a new instance of PackageRenamePlugin */
        public PackageRenamePlugin(RenameRefactoring refactoring) {
            this.refactoring = refactoring;
        }
        
        public Problem preCheck() {
            return null;
        }
        
        public Problem prepare(RefactoringElementsBag elements) {
            elements.addFileChange(refactoring, new RenameNonRecursiveFolder(refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class), elements));
            return null;
        }
        
        public Problem fastCheckParameters() {
            String newName = refactoring.getNewName();
            if (!RetoucheUtils.isValidPackageName(newName)) {
                String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage")).format(
                        new Object[] {newName}
                );
                return new Problem(true, msg);
            }
            
            ClassPath projectClassPath = ClassPath.getClassPath(refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class).getFolder(), ClassPath.SOURCE);
            if (projectClassPath.findResource(newName.replace('.','/'))!=null) {
                String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class,"ERR_PackageExists")).format(
                        new Object[] {newName}
                );
                return new Problem(true, msg);
            }
            return null;
        }
        
        public Problem checkParameters() {
            return null;
        }
        
        public void cancelRequest() {
        }
        
        private class RenameNonRecursiveFolder extends SimpleRefactoringElementImplementation {
            
            private FileObject folder;
            private RefactoringElementsBag session;
            private String oldName;
            private FileObject root;
            private String currentName;
            
            
            public RenameNonRecursiveFolder(NonRecursiveFolder nrfo, RefactoringElementsBag session) {
                this.folder = nrfo.getFolder();
                this.session = session;
                ClassPath cp = ClassPath.getClassPath(
                        folder, ClassPath.SOURCE);
                this.currentName = cp.getResourceName(folder, '.', false);
                this.oldName = this.currentName;
                this.root = cp.findOwnerRoot(folder);
                
            }
            
            public String getText() {                
                return NbBundle.getMessage(PackageRename.class, "TXT_RenamePackage") + folder.getNameExt();
            }
            
            public String getDisplayText() {
                return getText();
            }
            
            public void performChange() {
                setName(refactoring.getNewName());
            }
            
            public void undoChange() {
                setName(oldName);
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(folder.getParent());
            }
            
            public FileObject getParentFile() {
                return folder.getParent();
            }
            
            public PositionBounds getPosition() {
                return null;
            }
            
            /**
             *copy paste from PackageViewChildren
             */
            public void setName(String name) {
                if (currentName.equals(name)) {
                    return;
                }
//            if (!isValidPackageName (name)) {
//                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
//                        NbBundle.getMessage(PackageViewChildren.class,"MSG_InvalidPackageName"), NotifyDescriptor.INFORMATION_MESSAGE));
//                return;
//            }
                name = name.replace('.','/')+'/';           //NOI18N
                currentName = currentName.replace('.','/')+'/';     //NOI18N
                int i;
                for (i=0; i<currentName.length() && i< name.length(); i++) {
                    if (currentName.charAt(i) != name.charAt(i)) {
                        break;
                    }
                }
                i--;
                int index = currentName.lastIndexOf('/',i);     //NOI18N
                String commonPrefix = index == -1 ? null : currentName.substring(0,index);
                String toCreate = (index+1 == name.length()) ? "" : name.substring(index+1);    //NOI18N
                try {
                    FileObject commonFolder = commonPrefix == null ? this.root : this.root.getFileObject(commonPrefix);
                    FileObject destination = commonFolder;
                    StringTokenizer dtk = new StringTokenizer(toCreate,"/");    //NOI18N
                    while (dtk.hasMoreTokens()) {
                        String pathElement = dtk.nextToken();
                        FileObject tmp = destination.getFileObject(pathElement);
                        if (tmp == null) {
                            tmp = destination.createFolder(pathElement);
                        }
                        destination = tmp;
                    }
                    FileObject folder = this.folder;
                    DataFolder sourceFolder = DataFolder.findFolder(folder);
                    DataFolder destinationFolder = DataFolder.findFolder(destination);
                    DataObject[] children = sourceFolder.getChildren();
                    for (int j=0; j<children.length; j++) {
                        if (children[j].getPrimaryFile().isData()) {
                            children[j].move(destinationFolder);
                        }
                    }
                    while (!commonFolder.equals(folder)) {
                        if (folder.getChildren().length==0) {
                            FileObject tmp = folder;
                            folder = folder.getParent();
                            tmp.delete();
                        } else {
                            break;
                        }
                    }
                    this.folder = destinationFolder.getPrimaryFile();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
                this.currentName = name;
            }
        }
    }
}
