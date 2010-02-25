/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.spi.Query;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka
 */
class FindInQuerySupport {
    private IssueTable table;
    private Query query;
    private FindInQueryBar bar;

    private FindInQuerySupport(TopComponent tc) {
        bar = new FindInQueryBar(this);

        ActionMap actionMap = tc.getActionMap();
        CallbackSystemAction a = SystemAction.get(org.openide.actions.FindAction.class);
        actionMap.put(a.getActionMapKey(), new FindAction());                
    }

    static FindInQuerySupport create(TopComponent tc) {
        return new FindInQuerySupport(tc);
    }

    void setQuery(Query query) {
        this.query = query;
    }

    FindInQueryBar getFindBar() {
        return bar;
    }

    void reset() {
        getIssueTable().resetFilterBySummary();
    }

    protected void updatePattern() {        
        getIssueTable().setFilterBySummary(bar.getText(), bar.getRegularExpression(), bar.getWholeWords(), bar.getMatchCase());
    }

    protected void cancel() {
        reset();
        bar.setVisible(false);
    }

    protected void switchHighlight(boolean on) {
        getIssueTable().switchFilterBySummaryHighlight(on);
    }

    private class FindAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (bar.isVisible()) {
                updatePattern();
            } else {
                bar.setVisible(true);
                bar.requestFocusInWindow();
                updatePattern();
            }
        }
    }

    private IssueTable getIssueTable() {
        if(table == null) {
            return IssueTableSupport.getInstance().find(query);
        }
        return table;
    }

}
