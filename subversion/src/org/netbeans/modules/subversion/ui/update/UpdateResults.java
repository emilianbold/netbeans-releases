/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.subversion.ui.update;

import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.BorderLayout;
import java.text.DateFormat;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Displays files that have been updated.
 * 
 * @author Maros Sandor
 */
class UpdateResults extends JComponent {
        
    private final List<FileUpdateInfo> results;
    
    public UpdateResults(List<FileUpdateInfo> results, SVNUrl url, String contextDisplayName) {
        this.results = results;
        String time = DateFormat.getTimeInstance().format(new Date());
        setName(NbBundle.getMessage(UpdateResults.class, "CTL_UpdateResults_Title", url.toString(), contextDisplayName, time)); // NOI18N
        setLayout(new BorderLayout());
        if (results.size() == 0) {
            add(new NoContentPanel(NbBundle.getMessage(UpdateResults.class, "MSG_NoFilesUpdated"))); // NOI18N
        } else {
            UpdateResultsTable urt = new UpdateResultsTable();
            urt.setTableModel(createNodes());
            add(urt.getComponent());
        }
    }

    private UpdateResultNode[] createNodes() {
        UpdateResultNode [] nodes = new UpdateResultNode[results.size()];
        int idx = 0;
        for (FileUpdateInfo info : results) {
            nodes[idx++] = new UpdateResultNode(info);
        }
        return nodes;
    }   
    
}
