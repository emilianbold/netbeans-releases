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
package org.netbeans.modules.xml.refactoring.spi;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.refactoring.DeleteRequest;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.FileRenameRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.impl.RefactoringUtil;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Shared utilities for service implementation code.
 *
 * @author Nam Nguyen
 */
public class SharedUtils {
    
    public static void renameTarget(RenameRequest request) throws IOException {
        if (request == null || request.getTargetModel() == null) return;
        Model model = request.getTargetModel();
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            request.getNameableTarget().setName(request.getNewName());
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
        
        request.setRenamedTarget(request.getNameableTarget());
    }
    
    public static void deleteTarget(DeleteRequest request) throws IOException {
        if (request == null || request.getTargetModel() == null) return;
        Model model = request.getTargetModel();
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            model.removeChildComponent(request.getTarget());
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
        request.setDone(true);
    }

    public static void addCascadeDeleteErrors(DeleteRequest request, Class<? extends Model> referencingModelType) {
        for (UsageGroup usage : request.getUsages().getUsages()) {
            if (! (referencingModelType.isAssignableFrom(usage.getModel().getClass()))) {
                continue;
            }
            String msg = NbBundle.getMessage(RefactoringUtil.class, "MSG_CascadeDeleteNotSupported");
            for (Object o : usage.getItems()) {
                Usage i = (Usage) o; //strange i have to do this
                request.addError(new ErrorItem(i.getComponent(), msg));
            }
        }
    }
    
    public static void addCascadeDeleteErrors(DeleteRequest request, RefactoringEngine engine) {
        for (UsageGroup usage : request.getUsages().getUsages()) {
	    if (! usage.getEngine().equals(engine)) {
                continue;
            }
            String msg = NbBundle.getMessage(RefactoringUtil.class, "MSG_CascadeDeleteNotSupported");
            for (Object o : usage.getItems()) {
                Usage i = (Usage) o; //strange i have to do this
                request.addError(new ErrorItem(i.getComponent(), msg));
            }
        }
    }
 
    public static boolean isWritable(FileObject fo) {
        boolean canLock = false;
        FileLock lock = null;
        try {
            lock = fo.lock();
            canLock = true;
        } catch(IOException ioe) {
            if (lock != null) lock.releaseLock();
        }
        return fo != null && fo.canWrite() && canLock;
    }

    public static String getURI(FileObject fo) {
        return FileUtil.toFile(fo).toURI().toString();
    }
    
    public static void renameFile(FileRenameRequest request) throws IOException {
        FileObject referencedFO = request.getFileObject();
        RefactoringUtil.saveTargetFile(request);
        referencedFO = renameFile(referencedFO, request.getNewFileName());
        refreshCatalogModel(request, referencedFO);
    }

    public static ProjectCatalogSupport getCatalogSupport(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p != null) {
            return (ProjectCatalogSupport) p.getLookup().lookup(ProjectCatalogSupport.class);
        }
        return null;
    }
    
    public static void undoRenameFile(FileRenameRequest request) throws IOException {
        CatalogModel cat = (CatalogModel)
            request.getTargetModel().getModelSource().getLookup().lookup(CatalogModel.class);
        FileObject fo = request.getFileObject();
        fo = renameFile(fo, request.getOldFileName());
        refreshCatalogModel(request, fo);
    }

    public static FileObject renameFile(FileObject fo, String newName) throws IOException {
        String extension = fo.getExt();
        FileLock lock = null;
        try {
            lock = fo.lock();
            fo.rename(lock, newName, extension);
            return fo;
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }
    
    public static void refreshCatalogModel(FileRenameRequest request, FileObject referencedFO) {
        for (UsageGroup ug : request.getUsages().getUsages()) {
            FileObject referencingFO = ug.getFileObject();
            ProjectCatalogSupport pcs = getCatalogSupport(referencingFO);
            if (pcs == null) continue;
            for (Component uc : ug.getRefactorComponents()) {
                String reference = ug.getEngine().getModelReference(uc);
                if (reference == null) continue;
                try {
                    if (pcs != null && pcs.removeCatalogEntry(new URI(reference))) {
                        pcs.createCatalogEntry(referencingFO, referencedFO);
                    }
                } catch(Exception ex) {
                    Logger.getLogger(SharedUtils.class.getName()).log(Level.FINE, ex.getMessage());
                }
            }
        }
    }
}
