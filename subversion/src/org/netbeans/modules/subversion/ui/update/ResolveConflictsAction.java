/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.subversion.ui.update;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Show basic conflict resolver UI (provided by the diff module).
 *
 * @author Petr Kuzel
 */
public class ResolveConflictsAction extends ContextAction {

    public ResolveConflictsAction() {
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "ResolveConflicts";  // NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        Context ctx = getCachedContext(nodes);
        return Subversion.getInstance().getStatusCache().containsFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT, true);
    }

    protected void performContextAction(Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        Context ctx = getContext(nodes);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        resolveConflicts(files);
    }

    static void resolveConflicts(final File[] files) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final List<File> filteredFiles = removeFolders(files);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (filteredFiles.size() == 0) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
                            DialogDisplayer.getDefault().notify(nd);
                        } else {
                            for (File file : filteredFiles) {
                                ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                                executor.exec();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Filters the array and returns only existing files, not folders.
     * I/O access
     * @param files
     * @return
     */
    private static List removeFolders(File[] files) {
        LinkedList<File> filteredFiles = new LinkedList<File>();
        for (File file : files) {
            if (file.isFile()) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }

    public boolean asynchronous() {
        return false;
    }

}
