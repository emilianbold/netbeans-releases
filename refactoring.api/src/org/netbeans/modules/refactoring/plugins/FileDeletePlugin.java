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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.refactoring.plugins;

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author  Jan Becicka
 */
public class FileDeletePlugin implements RefactoringPlugin {
    private SafeDeleteRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileDeletePlugin(SafeDeleteRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        for (FileObject fo: refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
                elements.addFileChange(refactoring, new DeleteFile(fo, elements));
            }
        return null;
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
        
    public Problem checkParameters() {
        return null;
    }

    public void cancelRequest() {
    }
    //TODO: Move out this inner class
    static class DeleteFile extends SimpleRefactoringElementImplementation {
        
        private final URL res;
        private String filename;
        private RefactoringElementsBag session;
        public DeleteFile(FileObject fo, RefactoringElementsBag session) {
            try {
                this.res = fo.getURL();
            } catch (FileStateInvalidException ex) {
                throw new IllegalStateException(ex);
            }
            this.filename = fo.getNameExt();
            this.session = session;
        }
        public String getText() {
            return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeleteFile", filename);
        }

        public String getDisplayText() {
            return getText();
        }

        BackupFacility.Handle id;
        public void performChange() {
            try {
                FileObject fo = URLMapper.findFileObject(res);
                if (fo == null) {
                    throw new IOException(res.toString());
                }
                id = BackupFacility.getDefault().backup(fo);
                DataObject.find(fo).delete();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public void undoChange() {
            try {
                id.restore();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return URLMapper.findFileObject(res);
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

}

//                if (fo.isFolder()) {
//                    //TODO:Move deletion of packages to a separate plugin constructed by RefactoringPluginFactory
//                    dataObject.getNodeDelegate().destroy();
//                }else{
