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
package org.netbeans.jellytools.modules.javacvs;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.modules.javacvs.actions.ImportAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;

/**
 * Class implementing all necessary methods for handling "Import into Repository" wizard.
 * It is opened from main menu CVS|Import into Repository.
 * <br>
 * Usage:<br>
 * <pre>
 *      ImportWizardOperator.invoke();
 *      CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
 *      cvsRootOper.setPassword("password");
 *      cvsRootOper.setCVSRoot(":pserver:user@host:repository");
 *      cvsRootOper.next();
 *      FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
 *      folderToImportOper.setFolderToImport("/tmp/myLocalfolder");
 *      folderToImportOper.setImportMessage("Import message");
 *      folderToImportOper.setRepositoryFolder("folder");
 *      folderToImportOper.finish();
 * </pre>
 * 
 * 
 * @author Jiri.Skrivanek@sun.com
 * @see BrowseRepositoryFolderOperator
 * @see CVSRootStepOperator
 * @see FolderToImportStepOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.ImportAction
 */
public class ImportWizardOperator extends WizardOperator {
    
    /** Waits for dialog with "Import Project Options" title. */
    public ImportWizardOperator() {
        super(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", 
                "BK0007")
              );
    }
    
    /** Invokes new wizard and returns instance of ImportWizardOperator.
     * @return instance of ImportWizardOperator
     */
    public static ImportWizardOperator invoke() {
        new ImportAction().perform();
        return new ImportWizardOperator();
    }

    /** Invokes new wizard on given node and returns instance of ImportWizardOperator.
     * @param node node on which to invoke wizard
     * @return instance of ImportWizardOperator
     */
    public static ImportWizardOperator invoke(Node node) {
        new ImportAction().perform(node);
        return new ImportWizardOperator();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Goes through the wizard and fill supplied parameter.
     * @param cvsRoot CVS root.
     * @param password password - can be null
     * @param folderToImport local folder to import
     * @param importMessage import message
     * @param repositoryFolder repository folder
     */
    public void doImport(String cvsRoot, String password, String folderToImport, String importMessage, String repositoryFolder) {
        if(cvsRoot == null) {
            throw new JemmyException("CVS root must not be null."); // NOI18N
        }
        if(folderToImport == null) {
            throw new JemmyException("Folder to Import must not be null."); // NOI18N
        }
        if(importMessage == null) {
            throw new JemmyException("Import message must not be null."); // NOI18N
        }
        if(repositoryFolder == null) {
            throw new JemmyException("Repository Folder must not be null."); // NOI18N
        }
        ImportWizardOperator.invoke();
        CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
        if(password != null) {
            cvsRootOper.setPassword(password);
        }
        cvsRootOper.setCVSRoot(cvsRoot);
        cvsRootOper.next();
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        folderToImportOper.setFolderToImport(folderToImport);
        folderToImportOper.setImportMessage(importMessage);
        folderToImportOper.setRepositoryFolder(repositoryFolder);
        folderToImportOper.finish();
    }
}