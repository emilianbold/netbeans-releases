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
package org.netbeans.modules.refactoring.php;

import java.util.Collection;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.php.delete.PhpDeleteRefactoringUI;
import org.netbeans.modules.refactoring.php.delete.SafeDeleteSupport;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport;
import org.netbeans.modules.refactoring.php.rename.PhpRenameRefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Radek Matous
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position = 400)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

    @Override
    public boolean canFindUsages(Lookup lookup) {
        FileObject fo = isFromEditor(lookup) ? getFileObject(lookup) : null;
        return fo != null && RefactoringUtils.isRefactorable(fo) ? !(RefactoringUtils.isOutsidePhp(lookup, fo) && RefactoringUtils.isOutsidePHPDoc(lookup, fo)) : false;
    }

    private FileObject getFileObject(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        Node n = (nodes.size() == 1) ? nodes.iterator().next() : null;
        DataObject dob = (n != null) ? n.getLookup().lookup(DataObject.class) : null;
        return (dob != null) ? dob.getPrimaryFile() : null;
    }

    private boolean isFromEditor(Lookup lookup) {
        //TODO: is from editor? review, improve
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        return ec != null && ec.getOpenedPanes() != null;
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringUtils.isFromEditor(ec)) {
            new RefactoringTask.TextComponentTask(ec) {

                @Override
                protected RefactoringUI createRefactoringUI(final PHPParseResult info, final int offset) {
                    WhereUsedSupport ctx = WhereUsedSupport.getInstance(info, offset);
                    if (ctx != null && ctx.getName() != null) {
                        return new WhereUsedQueryUI(ctx);
                    }
                    return null;
                }
            }.run();
        }
    }

    @Override
    public boolean canRename(Lookup lookup) {
        return canFindUsages(lookup);
    }

    @Override
    public void doRename(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringUtils.isFromEditor(ec)) {
            new RefactoringTask.TextComponentTask(ec) {

                @Override
                protected RefactoringUI createRefactoringUI(final PHPParseResult info, final int offset) {
                    WhereUsedSupport ctx = WhereUsedSupport.getInstance(info, offset);
                    if (ctx != null && ctx.getName() != null) {
                        final FileObject fileObject = ctx.getModelElement().getFileObject();
                        FileType fileType = PhpSourcePath.getFileType(fileObject);
                        if (!fileType.equals(FileType.INTERNAL)) {
                            return new PhpRenameRefactoringUI(ctx);
                        }
                    }
                    return null;
                }
            }.run();
        }
    }

    @Override
    public boolean canDelete(Lookup lookup) {
        FileObject fo = getFileObject(lookup);
        return fo != null && RefactoringUtils.isRefactorable(fo);
    }

    @Override
    public void doDelete(Lookup lookup) {
        final boolean regularDelete = lookup.lookup(ExplorerContext.class) != null;
        //file or folder refactoring
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        assert nodes.size() == 1;
        Node currentNode = nodes.iterator().next();
        new RefactoringTask.NodeToFileTask(currentNode) {

            @Override
            protected RefactoringUI createRefactoringUI(PHPParseResult info) {
                SafeDeleteSupport ctx = SafeDeleteSupport.getInstance(info);
                if (ctx != null) {
                    final FileObject fileObject = ctx.getModel().getFileScope().getFileObject();
                    FileType fileType = PhpSourcePath.getFileType(fileObject);
                    if (!fileType.equals(FileType.INTERNAL)) {
                        return new PhpDeleteRefactoringUI(ctx, regularDelete);
                    }
                }
                return null;
            }
        }.run();
    }
}
