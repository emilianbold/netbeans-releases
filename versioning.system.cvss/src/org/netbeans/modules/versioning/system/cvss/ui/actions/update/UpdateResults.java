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
package org.netbeans.modules.versioning.system.cvss.ui.actions.update;

import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.BorderLayout;
import java.text.DateFormat;

/**
 * Displays files that have been updated.
 * 
 * @author Maros Sandor
 */
class UpdateResults extends JComponent {
    
    private final List<DefaultFileInfoContainer> updates;
    
    public UpdateResults(List<DefaultFileInfoContainer> output, String cvsRoot, String contextDisplayName) {
        this.updates = filter(output);
        String time = DateFormat.getTimeInstance().format(new Date());
        setName(NbBundle.getMessage(UpdateResults.class, "CTL_UpdateResults_Title", cvsRoot, contextDisplayName, time)); // NOI18N
        setLayout(new BorderLayout());
        if (updates.size() == 0) {
            add(new NoContentPanel(NbBundle.getMessage(UpdateResults.class, "MSG_NoFilesUpdated"))); // NOI18N
        } else {
            UpdateResultsTable urt = new UpdateResultsTable();
            urt.setTableModel(createNodes());
            add(urt.getComponent());
        }
    }

    private List<DefaultFileInfoContainer> filter(List<DefaultFileInfoContainer> l) {
        List<DefaultFileInfoContainer> list = new ArrayList<DefaultFileInfoContainer>(l.size());
        for (DefaultFileInfoContainer update : l) {
            if ("UPGYC".indexOf(update.getType()) != -1) { // NOI18N
                list.add(update);
            }
        }
        return list;
    }

    private UpdateResultNode[] createNodes() {
        UpdateResultNode [] nodes = new UpdateResultNode[updates.size()];
        int idx = 0;
        for (DefaultFileInfoContainer info : updates) {
            nodes[idx++] = new UpdateResultNode(info);
        }
        return nodes;
    }
}
