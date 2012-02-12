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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass, Openable, JavaRefactoringUIFactory {
    private AbstractRefactoring refactoring;
    private String oldName = null;
    private String dispOldName;
    private String newName;
    private RenamePanel panel;
    private boolean fromListener = false;
    private TreePathHandle handle;
    private ElementHandle elementHandle;
    private FileObject byPassFolder;
    private boolean byPassPakageRename;
    private boolean pkgRename = true;
    private Lookup lookup;

    private RenameRefactoringUI(Lookup lookup) {
        this.lookup = lookup;
    }
    
    private RenameRefactoringUI(TreePathHandle handle, CompilationInfo info) {
        this.handle = handle;
        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
        Element element = handle.resolveElement(info);
        if (UIUtilities.allowedElementKinds.contains(element.getKind())) {
            elementHandle = ElementHandle.create(element);
        }
        oldName = element.getSimpleName().toString();
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(false, handle.getFileObject()));
        } else {
            refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(true, true, RefactoringUtils.getFileObject(handle)));
        }
        dispOldName = oldName;

    }
    
    private RenameRefactoringUI(FileObject file, TreePathHandle handle, CompilationInfo info) {
        if (handle!=null) {
            this.handle = handle;
            this.refactoring = new RenameRefactoring(Lookups.fixed(file, handle));
            Element element = handle.resolveElement(info);
            if (UIUtilities.allowedElementKinds.contains(element.getKind())) {
                elementHandle = ElementHandle.create(element);
            }
            oldName = element.getSimpleName().toString();
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(file));
            oldName = file.getName();
        }
        dispOldName = oldName;
        ClasspathInfo cpInfo = handle==null?JavaRefactoringUtils.getClasspathInfoFor(file):RefactoringUtils.getClasspathInfoFor(handle);
        refactoring.getContext().add(cpInfo);
    }

    private RenameRefactoringUI(NonRecursiveFolder file) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(file));
        oldName = RefactoringUtils.getPackageName(file.getFolder());
        refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(file.getFolder()));
        dispOldName = oldName;
        pkgRename = true;
    }
    
    
    private RenameRefactoringUI(FileObject fileObject, String newName, TreePathHandle handle, CompilationInfo info) {
        if (handle!=null) {
            this.refactoring = new RenameRefactoring(Lookups.fixed(fileObject, handle));
            Element element = handle.resolveElement(info);
            if (UIUtilities.allowedElementKinds.contains(element.getKind())) {
                elementHandle = ElementHandle.create(element);
            }
        } else {
            this.refactoring = new RenameRefactoring(Lookups.fixed(fileObject));
        }
        oldName = newName;
        this.dispOldName = fileObject.getName();
        ClasspathInfo cpInfo = handle==null?JavaRefactoringUtils.getClasspathInfoFor(fileObject):RefactoringUtils.getClasspathInfoFor(handle);
        refactoring.getContext().add(cpInfo);
        fromListener = true;
    }
    
    private RenameRefactoringUI(NonRecursiveFolder jmiObject, String newName) {
        this.refactoring = new RenameRefactoring(Lookups.singleton(jmiObject));
        refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(jmiObject.getFolder()));
        oldName = newName;
        this.dispOldName = RefactoringUtils.getPackageName(jmiObject.getFolder());
        fromListener = true;
        pkgRename = true;
    }
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String name = oldName;
            String suffix = "";
            if (handle != null) {
                ElementKind kind = handle.getElementHandle().getKind();
                if (kind!=null && (kind.isClass() || kind.isInterface())) {
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
            suffix = suffix + " " + this.dispOldName; // NOI18N
            panel = new RenamePanel(handle,  name, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename") + " " + suffix, !fromListener, fromListener && !byPassPakageRename);
        }
        return panel;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            ((RenameRefactoring) refactoring).setSearchInComments(panel.searchJavadoc());
            JavaRenameProperties properties = refactoring.getContext().lookup(JavaRenameProperties.class);
            if (properties==null) {
                properties = new JavaRenameProperties();
                refactoring.getContext().add(properties);
            }
            properties.setIsRenameGettersSetters(panel.isRenameGettersSetters());
            properties.setIsRenameTestClass(panel.isRenameTestClass());
            
        }// else {
//            ((MoveClassRefactoring) refactoring).setTargetPackageName(newName);
//        }
        return refactoring.checkParameters();
    }
    
    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (!panel.isUpdateReferences()) 
            return null;
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            JavaRenameProperties properties = refactoring.getContext().lookup(JavaRenameProperties.class);
            if (properties==null) {
                properties = new JavaRenameProperties();
                refactoring.getContext().add(properties);
            }
            properties.setIsRenameGettersSetters(panel.isRenameGettersSetters());
            
        }// else {
//            ((MoveClassRefactoring) refactoring).setTargetPackageName(newName);
//        }
        return refactoring.fastCheckParameters();
    }

    @Override
    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenamePanel.class, "DSC_Rename")).format (
                    new Object[] {dispOldName, newName}
                );
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        String postfix;
        if (handle==null) {
            postfix = ".JavaPackage";//NOI18N
        } else {
            ElementKind k = handle.getElementHandle().getKind();
            
            if (k==null) {
                postfix = "";
            } else if (k.isClass() || k.isInterface())
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
    
    @Override
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    @Override
    public void doRefactoringBypass() throws IOException {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dob = null;
                    if (byPassFolder != null) {
                        dob = DataFolder.findFolder(byPassFolder);
                    } else {
                        FileObject fob = refactoring.getRefactoringSource().lookup(FileObject.class);
                        if (fob != null) {
                            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
                        }
                    }
                    final DataObject dobFin = dob;
                    FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                        @Override
                        public void run() throws IOException {
                            if (dobFin != null) {
                                dobFin.rename(panel.getNameValue());
                            } else {
                                NonRecursiveFolder pack = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
                                if (pack != null) {
                                    renamePackage(pack.getFolder(), panel.getNameValue());
                                }
                            }
                        }
                    });
                    
                } catch (IOException iOException) {
                    Exceptions.printStackTrace(iOException);
                }
            }
        });
    }
    
    private void renamePackage(FileObject source, String name) {
        //copy/paste from PackageNode.setName()
        FileObject root = ClassPath.getClassPath(source, ClassPath.SOURCE).findOwnerRoot(source);

        name = name.replace('.', '/') + '/';           //NOI18N
        String oldName = dispOldName.replace('.', '/') + '/';     //NOI18N
        int i;
        for (i = 0; i < oldName.length() && i < name.length(); i++) {
            if (oldName.charAt(i) != name.charAt(i)) {
                break;
            }
        }
        i--;
        int index = oldName.lastIndexOf('/', i);     //NOI18N
        String commonPrefix = index == -1 ? null : oldName.substring(0, index);
        String toCreate = (index + 1 == name.length()) ? "" : name.substring(index + 1);    //NOI18N
        try {
            FileObject commonFolder = commonPrefix == null ? root : root.getFileObject(commonPrefix);
            FileObject destination = commonFolder;
            StringTokenizer dtk = new StringTokenizer(toCreate, "/");    //NOI18N
            while (dtk.hasMoreTokens()) {
                String pathElement = dtk.nextToken();
                FileObject tmp = destination.getFileObject(pathElement);
                if (tmp == null) {
                    tmp = destination.createFolder(pathElement);
                }
                destination = tmp;
            }
            DataFolder sourceFolder = DataFolder.findFolder(source);
            DataFolder destinationFolder = DataFolder.findFolder(destination);
            DataObject[] children = sourceFolder.getChildren();
            for (int j = 0; j < children.length; j++) {
                if (children[j].getPrimaryFile().isData()) {
                    children[j].move(destinationFolder);
                }
            }
            while (!commonFolder.equals(source)) {
                if (source.getChildren().length == 0) {
                    FileObject tmp = source;
                    source = source.getParent();
                    tmp.delete();
                } else {
                    break;
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    @Override
    public void open() {
        if (elementHandle!=null) {
            ElementOpen.open(handle.getFileObject(), elementHandle);
        }
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        if (packages.length == 1) {
            return new RenameRefactoringUI(packages[0]);
        }
        assert handles.length == 1;
        TreePathHandle selectedElement = handles[0];
        Element selected = handles[0].resolveElement(info);
        if (selected == null) {
            logger().log(Level.INFO, "doRename: " + handles[0], new NullPointerException("selected")); // NOI18N
            return null;
        }
        if (selected.getKind() == ElementKind.CONSTRUCTOR) {
            selected = selected.getEnclosingElement();
            TreePath path = info.getTrees().getPath(selected);
            if (path == null) {
                logger().log(Level.INFO, "doRename: " + selected, new NullPointerException("selected")); // NOI18N
                return null;
            }
            selectedElement = TreePathHandle.create(path, info);
        }
        if (selected.getKind() == ElementKind.PACKAGE) {
            final FileObject pkg = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(selected.toString().replace('.', '/'));
            if (pkg != null) {
                NonRecursiveFolder folder = new NonRecursiveFolder() {

                    @Override
                    public FileObject getFolder() {
                        return pkg;
                    }
                };
                return new RenameRefactoringUI(folder);
            } else {
                if (selected.getSimpleName().length() != 0) {
                    return new RenameRefactoringUI(selectedElement, info);
                } else {
                    TreePath path = selectedElement.resolve(info);
                    if (path != null && path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                        return new RenameRefactoringUI(selectedElement.getFileObject(), null, info);
                    } else {
                        return null;
                    }
                }
            }
        } else if (selected instanceof TypeElement && !((TypeElement) selected).getNestingKind().isNested()) {
            ElementHandle<TypeElement> handle = ElementHandle.create((TypeElement) selected);
            FileObject f = SourceUtils.getFile(handle, info.getClasspathInfo());
            if (f != null && selected.getSimpleName().toString().equals(f.getName())) {
                return new RenameRefactoringUI(f == null ? info.getFileObject() : f, selectedElement, info);
            } else {
                return new RenameRefactoringUI(selectedElement, info);
            }
        } else {
            return new RenameRefactoringUI(selectedElement, info);
        }
    }
    
    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new RenameRefactoringUI(lookup);
    }
            private static Logger logger() {
        return Logger.getLogger(RefactoringActionsProvider.class.getName());
    }

}
