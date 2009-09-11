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
package org.netbeans.modules.bugtracking.ui.query;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Maros Sandor
 */
public class QueryAction extends SystemAction {

    public QueryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(QueryAction.class, "CTL_QueryAction"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(QueryAction.class);
    }

    public void actionPerformed(ActionEvent ev) {
        openQuery(null);
    }

    public static void openQuery(Query query) {
//        Repository repository = BugtrackingOwnerSupport.getInstance()
//                                .getRepository(BugtrackingOwnerSupport.ContextType
//                                               .SELECTED_FILE_AND_ALL_PROJECTS) ;
        openQuery(query, null);
    }

    public static void openQuery(final Query query, final Repository repositoryToSelect) {
        openQuery(query, repositoryToSelect, false);
    }

    public static void openQuery(final Query query, final Repository repository, final boolean suggestedSelectionOnly) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BugtrackingManager.LOG.log(Level.FINE, "QueryAction.openQuery start. query [{0}]", new Object[] {query != null ? query.getDisplayName() : null});
                QueryTopComponent tc = null;
                if(query != null) {
                    tc = QueryTopComponent.find(query);
                }
                if(tc == null) {
                    tc = new QueryTopComponent();
                    tc.init(query, repository, suggestedSelectionOnly);
                }
                if(!tc.isOpened()) {
                    tc.open();
                }
                tc.requestActive();
                BugtrackingManager.LOG.log(Level.FINE, "QueryAction.openQuery finnish. query [{0}]", new Object[] {query != null ? query.getDisplayName() : null});
            }
        });
    }

    public static void closeQuery(final Query query) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TopComponent tc = null;
                if(query != null) {
                    tc = WindowManager.getDefault().findTopComponent(query.getDisplayName());
                }
                if(tc != null) {
                    tc.close();
                }
            }
        });
    }
}
