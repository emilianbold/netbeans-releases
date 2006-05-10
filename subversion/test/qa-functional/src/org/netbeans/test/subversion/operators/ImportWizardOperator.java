/*
 * ImportWizardOperator.java
 *
 * Created on 10/05/06 11:54
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.test.subversion.operators.actions.ImportAction;

/**
 * Class implementing all necessary methods for handling "ImportWizardOperator" NbDialog.
 * 
 * 
 * @author peter
 * @version 1.0
 */
public class ImportWizardOperator extends WizardOperator {

    /**
     * Creates new ImportWizardOperator that can handle it.
     */
    public ImportWizardOperator() {
        super("Import");
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
    public void doImport(String repositoryURL, String password, String importMessage, String repositoryFolder) {
        if(repositoryURL == null) {
            throw new JemmyException("CVS root must not be null."); // NOI18N
        }
        if(importMessage == null) {
            throw new JemmyException("Import message must not be null."); // NOI18N
        }
        if(repositoryFolder == null) {
            throw new JemmyException("Repository Folder must not be null."); // NOI18N
        }
        ImportWizardOperator.invoke();
        RepositoryStepOperator rso = new RepositoryStepOperator();
        if(password != null) {
            rso.setPassword(password);
        }
        rso.setRepositoryURL(repositoryURL);
        rso.next();
        FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
        folderToImportOper.setImportMessage(importMessage);
        folderToImportOper.setRepositoryFolder(repositoryFolder);
        folderToImportOper.finish();
    }
}

