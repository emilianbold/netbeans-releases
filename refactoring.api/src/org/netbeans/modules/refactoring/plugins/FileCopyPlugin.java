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
package org.netbeans.modules.refactoring.plugins;

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class FileCopyPlugin implements RefactoringPlugin {
    private SingleCopyRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileCopyPlugin(SingleCopyRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        elements.add(refactoring, new CopyFile(refactoring.getRefactoringSource().lookup(FileObject.class), elements.getSession()));
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
    
    private class CopyFile extends SimpleRefactoringElementImplementation {
        
        private FileObject fo;
        private RefactoringSession session;
        private DataObject newOne;
        public CopyFile(FileObject fo, RefactoringSession session) {
            this.fo = fo;
            this.session = session;
        }
        public String getText() {            
            return NbBundle.getMessage(FileCopyPlugin.class, "TXT_CopyFile", fo.getNameExt());
        }
        
        public String getDisplayText() {
            return getText();
        }
        
        public void performChange() {
            try {
                FileObject fo = FileHandlingFactory.getOrCreateFolder(refactoring.getTarget().lookup(URL.class));
                FileObject source = refactoring.getRefactoringSource().lookup(FileObject.class);
                DataObject dob = DataObject.find(source);
                newOne = dob.copy(DataFolder.findFolder(fo));
                newOne.rename(refactoring.getNewName());
                refactoring.getContext().add(newOne.getPrimaryFile());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        @Override
        public void undoChange() {
            try {
                if (newOne != null) {
                    newOne.delete();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return fo;
        }
        
        public PositionBounds getPosition() {
            return null;
        }
    }
}
