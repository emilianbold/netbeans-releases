/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.ui.commit.CommitTable;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.util.Context;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class ImportPreviewStep extends AbstractStep {
    
    private PreviewPanel previewPanel;
    private Context context;
    private CommitTable table;    
    
    public ImportPreviewStep(Context context) {
        this.context =context;
    }
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportStep.class);
    }    

    protected JComponent createComponent() {
        if (previewPanel == null) {
            previewPanel = new PreviewPanel();

            table = new CommitTable(previewPanel.tableLabel, CommitTable.IMPORT_COLUMNS);
            previewPanel.tableLabel.setText(org.openide.util.NbBundle.getMessage(ImportPreviewStep.class, "CTL_Import_CommitTitle")); // NOI18N
            JComponent component = table.getComponent();
            previewPanel.tablePanel.setLayout(new BorderLayout());
            previewPanel.tablePanel.add(component, BorderLayout.CENTER);
        }
        return previewPanel;              
    }

    protected void validateBeforeNext() {
        validateUserInput();
    }       

    public void validateUserInput() {
        if(table != null && table.getCommitFiles().size() > 0) {
            valid();
        } else {
            invalid(org.openide.util.NbBundle.getMessage(ImportPreviewStep.class, "CTL_Import_NothingToImport")); // NOI18N
        }        
    }    

    public void setup(String repositoryPath, String rootLocalPath) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(context, FileInformation.STATUS_LOCAL_CHANGE);

        if (files.length == 0) {
            return;
        }

        if(repositoryPath != null) {
            table.setRootFile(repositoryPath, rootLocalPath);
        }

        SvnFileNode[] nodes;        
        ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(files.length);

        for (int i = 0; i<files.length; i++) {
            File file = files[i];
            SvnFileNode node = new SvnFileNode(file);
            nodesList.add(node);
        }
        nodes = nodesList.toArray(new SvnFileNode[files.length]);
        table.setNodes(nodes);

        validateUserInput();
    }

    public Map getCommitFiles() {
        return table.getCommitFiles();
    }

}

