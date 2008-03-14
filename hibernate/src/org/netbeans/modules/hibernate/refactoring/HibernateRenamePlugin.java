/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.refactoring;

import com.sun.source.tree.Tree.Kind;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.openide.filesystems.FileObject;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.RenamedClassName;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;

/**
 * This plugin modifies the Hibernate mapping files accordingly when the referenced
 * Java class or/and package names are changed
 * 
 * @author Dongmei Cao
 */
public class HibernateRenamePlugin implements RefactoringPlugin {

    private RenameRefactoring refactoring;

    public HibernateRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
        return;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        FileObject fo = null;
        if (treePathHandle != null && treePathHandle.getKind() == Kind.CLASS) {
            fo = treePathHandle.getFileObject();
        }
        if (fo == null) {
            fo = refactoring.getRefactoringSource().lookup(FileObject.class);
        }
        boolean recursive = true;
        if (fo == null) {
            NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            if (folder != null) {
                recursive = false;
                fo = folder.getFolder();
            }
        }
        if (fo == null) {
            return null;
        }

        // Find the mapping files in this project
        Project proj = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
        HibernateEnvironment env = new HibernateEnvironment(proj);
        List<FileObject> mFileObjs = env.getAllHibernateMappingFileObjects();
        if (mFileObjs == null || mFileObjs.size() == 0) {
            // OK, no mapping files at all. 
            return null;
        }

        try {
            if (treePathHandle != null) {
                RenamedClassName clazz = null;

                // Figure out the old name and new name
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    clazz = HibernateRefactoringUtil.getRenamedClassName(treePathHandle, js, refactoring.getNewName());
                }

                if (clazz != null) {
                    String oldBinaryName = clazz.getOldBinaryName();
                    String newBinaryName = clazz.getNewBinaryName();
                    if (oldBinaryName != null && newBinaryName != null) {

                        Map<FileObject, PositionBounds> occurrences = 
                                HibernateRefactoringUtil.getJavaClassOccurrences(mFileObjs, oldBinaryName);

                        for (FileObject mFileObj : occurrences.keySet()) {
                            HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(mFileObj,
                                    oldBinaryName,
                                    newBinaryName,
                                    occurrences.get(mFileObj));
                            refactoringElements.add(refactoring, elem);
                        }
                        
                        refactoringElements.registerTransaction(new JavaClassRenameTransaction(occurrences.keySet(), oldBinaryName, newBinaryName));
                    }
                }
            } else if (fo.isFolder()) {
                String oldPackageName = HibernateRefactoringUtil.getPackageName(fo);
                // If the rename is not recursive (e.g, "a.b.c" -> "x.b.c"), the new name is the whole package name.
                String newPackageName = recursive ? HibernateRefactoringUtil.getRenamedPackageName(fo, refactoring.getNewName()) : refactoring.getNewName();
                if (oldPackageName != null && newPackageName != null) {
                    Map<FileObject, List<PositionBounds>> occurrences = 
                            HibernateRefactoringUtil.getJavaPackageOccurrences(mFileObjs, oldPackageName);

                    for (FileObject mFileObj : occurrences.keySet()) {
                        List<PositionBounds> locations = occurrences.get(mFileObj);
                        
                        for( PositionBounds loc : locations) {
                            HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(mFileObj,
                                    oldPackageName,
                                    newPackageName,
                                    loc);
                            refactoringElements.add(refactoring, elem);
                        }
                    }
                    
                    refactoringElements.registerTransaction(new JavaPackageRenameTransaction(occurrences.keySet(), oldPackageName, newPackageName));
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
}
