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
package org.netbeans.modules.xml.refactoring.ui;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** Refactoring UI object for Copy  refactoring.
 *
 * @author Sonali Kochar
 */
public class CopyRefactoringUI implements RefactoringUI  {
    // reference to pull up refactoring this UI object corresponds to
    private SingleCopyRefactoring refactoring;
    // UI panel for collecting parameters
    private CopyPanel panel;
    private Model target;
    private boolean disable;
    private String oldFileName;
    private FileObject targetFile;
        
    public CopyRefactoringUI(Model target) {
        this.target=target;
        refactoring = new SingleCopyRefactoring(Lookups.singleton(target));
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
        refactoring.getContext().add(transaction);
        disable = false;
        targetFile = target.getModelSource().getLookup().lookup(FileObject.class);
        oldFileName = targetFile.getName();
    }
    
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String pkgName = targetFile.getParent().getName();
            panel = new CopyPanel(parent,
                    getName() + " - " + oldFileName, 
                    pkgName, 
                    targetFile,
                    oldFileName);
            panel.setCombosEnabled(!disable);
        }
        return panel;
    }

    public Problem setParameters() {
        setupRefactoring();
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        if (panel==null)
            return null;
        setupRefactoring();
        return refactoring.fastCheckParameters();
    }
    
    private void setupRefactoring() {
        refactoring.setNewName(panel.getNewName());
        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + "/" + panel.getPackageName().replace('.','/'))));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return NbBundle.getMessage(CopyRefactoringUI.class, "DSC_CopyClass", refactoring.getNewName()); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(CopyRefactoringUI.class, "LBL_CopyClass"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CopyRefactoringUI.class);
    }
    
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    public void doRefactoringBypass() throws IOException {
        //Transferable t = paste.paste();
        Model model = refactoring.getRefactoringSource().lookup(Model.class);
        FileObject source = model.getModelSource().getLookup().lookup(FileObject.class);
        FileObject target = SharedUtils.getOrCreateFolder(refactoring.getTarget().lookup(URL.class));
        if (source!=null) {
            DataObject sourceDo = DataObject.find(source);
            DataFolder targetFolder = DataFolder.findFolder(target);
            sourceDo.copy(targetFolder).rename(panel.getNewName());
        }
    }
}
