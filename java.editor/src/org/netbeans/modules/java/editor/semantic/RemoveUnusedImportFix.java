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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class RemoveUnusedImportFix implements Fix {
    
    public static RemoveUnusedImportFix create(FileObject file, TreePathHandle importToRemove) {
        return new RemoveUnusedImportFix(file, Collections.singletonList(importToRemove), "FIX_Remove_Unused_Import");
    }
    
    public static RemoveUnusedImportFix create(FileObject file, List<TreePathHandle> importsToRemove) {
        return new RemoveUnusedImportFix(file, importsToRemove, "FIX_All_Remove_Unused_Import");
    }
    
    private FileObject file;
    private List<TreePathHandle> importsToRemove;
    private String bundleKey;
    
    private RemoveUnusedImportFix(FileObject file, List<TreePathHandle> importsToRemove, String bundleKey) {
        this.file = file;
        this.importsToRemove = importsToRemove;
        this.bundleKey = bundleKey;
    }
    
    public String getText() {
        return NbBundle.getMessage(RemoveUnusedImportFix.class, bundleKey);
    }

    public ChangeInfo implement() {
        JavaSource js = JavaSource.forFileObject(file);
        
        try {
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {}
                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(Phase.PARSED);
                    
                    CompilationUnitTree nueCUT = copy.getCompilationUnit();
                    
                    for (TreePathHandle handle : importsToRemove) {
                        TreePath tp = handle.resolve(copy);
                        
                        if (tp == null) {
                            //cannot resolve
                            Logger.getLogger(RemoveUnusedImportFix.class.getName()).info("Cannot resolve import to remove."); //NOI18N
                            return ;
                        }
                        
                        nueCUT = copy.getTreeMaker().removeCompUnitImport(nueCUT, (ImportTree) tp.getLeaf());
                    }
                    
                    copy.rewrite(copy.getCompilationUnit(), nueCUT);
                }
            }).commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }

}
