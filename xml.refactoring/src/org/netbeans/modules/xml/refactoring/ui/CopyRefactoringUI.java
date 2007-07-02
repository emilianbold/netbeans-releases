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