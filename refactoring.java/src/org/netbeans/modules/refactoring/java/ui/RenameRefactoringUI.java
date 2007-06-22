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
package org.netbeans.modules.refactoring.java.ui;

import java.io.IOException;
import java.text.MessageFormat;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private final AbstractRefactoring refactoring;
    private String oldName = null;
    private String dispOldName;
    private String newName;
    private RenamePanel panel;
    private boolean fromListener = false;
    private TreePathHandle handle;
    private FileObject byPassFolder;
    private boolean byPassPakageRename;
    private boolean pkgRename = true;


//    RenameRefactoringUI(ElementHandle jmiObject, FileObject folder, boolean packageRename) {
//        this.jmiObject = jmiObject;
//        pkgRename = packageRename;
//        if (jmiObject == null || jmiObject instanceof JavaPackage) {
//            if (folder == null) {
//                folder = JavaMetamodel.getManager().getClassPath().findResource(((JavaPackage) jmiObject).getName().replace('.','/'));
//                if (folder == null) {
//                    //package declaration is invalid
//                    Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
//                    DataObject dob = (DataObject) nodes[0].getCookie(DataObject.class);
//                    folder = dob.getFolder().getPrimaryFile();
//                }
//            }
//            this.refactoring = createMoveClassRefactoring(folder, packageRename);
//            String packageName = jmiObject != null ? ((JavaPackage) jmiObject).getName():null;
//            if (!packageRename) {
//                oldName = folder.getName();
//            } else {
//                if (packageName != null) {
//                    oldName = "".equals(packageName) ? NbBundle.getBundle("org.netbeans.modules.java.project.Bundle").getString("LBL_DefaultPackage") : packageName;
//                } else {
//                    oldName = ClassPath.getClassPath(folder, ClassPath.SOURCE).getResourceName(folder).replace('/','.');
//                }
//            }
//        } else if (jmiObject instanceof JavaClass) {
//            this.refactoring = new RenameRefactoring(jmiObject);
//            Object comp = jmiObject.refImmediateComposite();
//            if (comp instanceof Resource) {
//                FileObject fo = JavaModel.getFileObject((Resource)comp);
//                String name = fo.getName();
//                for (Iterator iter = ((Resource)comp).getClassifiers().iterator(); iter.hasNext(); ) {
//                    Object obj = iter.next();
//                    if (obj instanceof JavaClass) {
//                        String sName = ((JavaClass) obj).getSimpleName();
//                        if (name.equals(sName)) {
//                            name = ((JavaClass) jmiObject).getSimpleName();
//                            break;
//                        }
//                    } // if
//                } // for
//                oldName = name;
//            } else {
//                oldName = ((JavaClass) jmiObject).getSimpleName();
//            }
//        } else {
//            this.refactoring = new RenameRefactoring(jmiObject);
//            if (jmiObject instanceof NamedElement) {
//                if (jmiObject instanceof Resource) {
//                    oldName = JavaMetamodel.getManager().getDataObject((Resource)jmiObject).getName();
//                } else
//                    oldName = ((NamedElement) jmiObject).getName();
//            } else {
//                oldName = "";
//            }
//        }
//        dispOldName = oldName;
//    }
    
    public RenameRefactoringUI(TreePathHandle handle, CompilationInfo info) {
        this.handle = handle;
        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
        oldName = handle.resolveElement(info).getSimpleName().toString();
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(handle));
        dispOldName = oldName;

        //this(jmiObject, (FileObject) null, true);
    }
    
    public RenameRefactoringUI(FileObject file, TreePathHandle handle, CompilationInfo info) {
        if (handle!=null) {
            handle = handle;
            this.refactoring = new RenameRefactoring(Lookups.fixed(file, handle));
            oldName = handle.resolveElement(info).getSimpleName().toString();
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(file));
            oldName = file.getName();
        }
        dispOldName = oldName;
        ClasspathInfo cpInfo = handle==null?RetoucheUtils.getClasspathInfoFor(file):RetoucheUtils.getClasspathInfoFor(handle);
        refactoring.getContext().add(cpInfo);
        //this(jmiObject, (FileObject) null, true);
    }

    public RenameRefactoringUI(NonRecursiveFolder file) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(file));
        oldName = RetoucheUtils.getPackageName(file.getFolder());
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(file.getFolder()));
        dispOldName = oldName;
        pkgRename = true;
        //this(jmiObject, (FileObject) null, true);
    }
    
    
//    private MoveClassRefactoring createMoveClassRefactoring(FileObject folder, boolean packageRename) {
//        return new MoveClassRefactoring(folder, packageRename);
//    }

    RenameRefactoringUI(FileObject jmiObject, String newName, TreePathHandle handle, CompilationInfo info) {
        if (handle!=null) {
            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject, handle));
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject));
        }
        //this.jmiObject = jmiObject;
        oldName = newName;
        //[FIXME] this should be oldName of refactored object
        this.dispOldName = newName;
        ClasspathInfo cpInfo = handle==null?RetoucheUtils.getClasspathInfoFor(jmiObject):RetoucheUtils.getClasspathInfoFor(handle);
        refactoring.getContext().add(cpInfo);
        fromListener = true;
    }
    
    RenameRefactoringUI(NonRecursiveFolder jmiObject, String newName) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(jmiObject));
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(jmiObject.getFolder()));
        //this.jmiObject = jmiObject;
        oldName = newName;
        //[FIXME] this should be oldName of refactored object
        this.dispOldName = newName;
        fromListener = true;
        pkgRename = true;
    }
    
    
    
    
//    RenameRefactoringUI(FileObject folder, String newName, boolean packageRename) {
//        this.refactoring = createMoveClassRefactoring(folder, packageRename);
//        pkgRename = packageRename;
//        oldName = newName;
//        //[FIXME] this should be oldName of refactored object
//        this.dispOldName = newName;
//        fromListener = true;
//        this.byPassFolder = folder;
//        this.byPassPakageRename = packageRename;
//        
//    }
    
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String name = oldName;
            String suffix = "";
            if (handle != null) {
                ElementKind kind = RetoucheUtils.getElementKind(handle);
                if (kind.isClass() || kind.isInterface()) {
                    suffix  = kind.isInterface() ? getString("LBL_Interface") : getString("LBL_Class");
                } else if (kind == ElementKind.METHOD) {
                    suffix = getString("LBL_Method");
                } else if (kind == ElementKind.FIELD) {
                    suffix = getString("LBL_Field");
                } else if (kind == ElementKind.LOCAL_VARIABLE) {
                    suffix = getString("LBL_LocalVar");
                } else if (kind == ElementKind.PACKAGE || (handle == null && fromListener)) {
                    suffix = pkgRename ? getString("LBL_Package") : getString("LBL_Folder");
                } else if (kind == ElementKind.PARAMETER) {
                    suffix = getString("LBL_Parameter");
                }
            }
            suffix = suffix + " " + name; // NOI18N
            panel = new RenamePanel(name, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename") + " " + suffix, !fromListener, fromListener && !byPassPakageRename);
        }
        return panel;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            ((RenameRefactoring) refactoring).setSearchInComments(panel.searchJavadoc());            
        }// else {
//            ((MoveClassRefactoring) refactoring).setTargetPackageName(newName);
//        }
        return refactoring.checkParameters();
    }
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (!panel.isUpdateReferences()) 
            return null;
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
        }// else {
//            ((MoveClassRefactoring) refactoring).setTargetPackageName(newName);
//        }
        return refactoring.fastCheckParameters();
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenamePanel.class, "DSC_Rename")).format (
                    new Object[] {dispOldName, newName}
                );
    }

    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        String postfix;
        if (handle==null) {
            postfix = ".JavaPackage";//NOI18N
        } else {
            ElementKind k = RetoucheUtils.getElementKind(handle);
            
            if (k.isClass() || k.isInterface())
                postfix = ".JavaClass";//NOI18N
            else if (k == ElementKind.METHOD)
                postfix = ".Method";//NOI18N
            else if (k.isField())
                postfix = ".Field";//NOI18N
            else
                postfix = "";
        }
        
        return new HelpCtx(RenameRefactoringUI.class.getName() + postfix);
    }
    
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(panel.getNameValue());
    }
}
