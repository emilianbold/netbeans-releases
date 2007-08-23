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
package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.ui.commit.CommitTable;
import org.netbeans.modules.subversion.ui.commit.CommitTableModel;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.util.Context;
import org.openide.util.HelpCtx;

/**
 * @author Tomas Stupka
 */
public class ImportPreviewStep extends AbstractStep {
    
    private PreviewPanel previewPanel;
    private Context context;
    private CommitTable table;    
    
    public ImportPreviewStep(Context context) {
        this.context = context;
    }
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportPreviewStep.class);
    }    

    protected JComponent createComponent() {
        if (previewPanel == null) {
            previewPanel = new PreviewPanel();

            //TableSorter sorter = SvnModuleConfig.getDefault().getImportTableSorter();
            //if(sorter==null) {
                table = new CommitTable(previewPanel.tableLabel, CommitTable.IMPORT_COLUMNS, new String[] { CommitTableModel.COLUMN_NAME_PATH });    
            //} else {
            //    table = new CommitTable(previewPanel.tableLabel, CommitTable.IMPORT_COLUMNS, sorter);
            //}                                    
            
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
    
    public void storeTableSorter() {
        //SvnModuleConfig.getDefault().setImportTableSorter(table.getSorter());        
    }

}

